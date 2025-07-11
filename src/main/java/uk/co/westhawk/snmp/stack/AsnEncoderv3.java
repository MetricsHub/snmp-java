// NAME
//      $RCSfile: AsnEncoderv3.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.5 $
// CREATED
//      $Date: 2009/03/05 12:48:59 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1995, 1996 by West Consulting BV
 *
 * Permission to use, copy, modify, and distribute this software
 * for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that
 * both the copyright notice and this permission notice appear in
 * supporting documentation.
 * This software is provided "as is" without express or implied
 * warranty.
 * author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * original version by hargrave@dellgate.us.dell.com (Jordan Hargrave)
 */

/*
 * Copyright (C) 1996 - 2006 by Westhawk Ltd
 * <a href="www.westhawk.co.uk">www.westhawk.co.uk</a>
 *
 * Permission to use, copy, modify, and distribute this software
 * for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that
 * both the copyright notice and this permission notice appear in
 * supporting documentation.
 * This software is provided "as is" without express or implied
 * warranty.
 * author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 */

package uk.co.westhawk.snmp.stack;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * SNMP Java Client
 * ჻჻჻჻჻჻
 * Copyright 2023 MetricsHub, Westhawk
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import uk.co.westhawk.snmp.util.SnmpUtilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

/**
 * This class contains the v3 specific methods to encode a Pdu into bytes.
 * We split the original class AsnEncoder into four classes.
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.5 $ $Date: 2009/03/05 12:48:59 $
 */
class AsnEncoderv3 extends AsnEncoderBase {
	private static final String version_id = "@(#)$Id: AsnEncoderv3.java,v 3.5 2009/03/05 12:48:59 birgita Exp $ Copyright Westhawk Ltd";

	/**
	 * Encode SNMPv3 packet into bytes.
	 * 
	 * @param context      The SNMP context
	 * @param contextMsgId The message ID
	 * @param node         The time window node
	 * @param msg_type     The message type
	 * @param pduId        The PDU ID
	 * @param errstat      The error status
	 * @param errind       The error index
	 * @param ve           The enumeration
	 * @return The encoded SNMPv3 packet
	 */
	byte[] EncodeSNMPv3(SnmpContextv3Basis context, int contextMsgId, TimeWindowNode node, byte msg_type, int pduId,
			int errstat, int errind, Enumeration ve) throws IOException, EncodingException {

		// Prepare the encoded message output stream to be sent to the SNMP agent
		ByteArrayOutputStream encodedSnmpMessageOutputStream;

		AsnSequence asnSequence = new AsnSequence();

		// msgGlobalData = HeaderData
		AsnSequence asnHeaderData = new AsnSequence();
		asnHeaderData.add(new AsnInteger(contextMsgId));
		asnHeaderData.add(new AsnInteger(context.getMaxRecvSize()));
		asnHeaderData.add(new AsnOctets(getMessageFlags(context, msg_type)));
		asnHeaderData.add(new AsnInteger(context.USM_Security_Model));

		// msgData = ScopedPdu (plaintext or encrypted)
		AsnSequence asnPlainScopedPdu = new AsnSequence();
		asnPlainScopedPdu.add(new AsnOctets(context.getContextEngineId()));
		asnPlainScopedPdu.add(new AsnOctets(context.getContextName()));
		// PDU sequence.
		AsnObject asnPduObject = EncodePdu(msg_type, pduId, errstat, errind, ve);
		asnPlainScopedPdu.add(asnPduObject);

		// asnSecurityParameters
		if (AsnObject.debug > 10) {
			System.out.println("\nEncode USM: node " + node.toString());
		}
		AsnSequence asnSecurityObject = new AsnSequence();
		byte[] engineIdBytes = SnmpUtilities.toBytes(node.getSnmpEngineId());
		asnSecurityObject.add(new AsnOctets(engineIdBytes));
		asnSecurityObject.add(new AsnInteger(node.getSnmpEngineBoots()));
		asnSecurityObject.add(new AsnInteger(node.getSnmpEngineTime()));
		asnSecurityObject.add(new AsnOctets(context.getUserName()));
		AsnOctets fingerPrintOctets;
		int authenticationProtocol = context.getAuthenticationProtocol();

		byte[] dummyFingerprint;
		if (context.isUseAuthentication()) {
			dummyFingerprint = SnmpUtilities.initFingerprint(authenticationProtocol);
			fingerPrintOctets = new AsnOctets(dummyFingerprint);
		} else {
			fingerPrintOctets = new AsnOctets("");
		}
		asnSecurityObject.add(fingerPrintOctets);

		AsnOctets privacyAsnOctets;
		AsnOctets asnEncryptedScopedPdu = null;

		if (context.isUsePrivacy()) {
			int privacyProtocol = context.getPrivacyProtocol();
			// Retrieves the localized privacy key from the derived privacy key
			byte[] privacyKey = context.generatePrivacyKey(node.getSnmpEngineId(), authenticationProtocol, privacyProtocol);
			byte[] salt = null;
			if (SnmpContextv3Face.AES_PRIVACY_PROTOCOLS.contains(privacyProtocol)) {
				salt = SnmpUtilities.getSaltAES();
			} else {
				salt = SnmpUtilities.getSaltDES(node.getSnmpEngineBoots());
			}

			privacyAsnOctets = new AsnOctets(salt);
			encodedSnmpMessageOutputStream = new ByteArrayOutputStream();
			asnPlainScopedPdu.write(encodedSnmpMessageOutputStream);

			byte[] plaintext = encodedSnmpMessageOutputStream.toByteArray();
			byte[] encryptedText = null;
			if (SnmpContextv3Face.AES_PRIVACY_PROTOCOLS.contains(privacyProtocol)) {
				encryptedText = SnmpUtilities.AESencrypt(plaintext, privacyKey, node.getSnmpEngineBoots(),
						node.getSnmpEngineTime(), salt);
			} else {
				encryptedText = SnmpUtilities.DESencrypt(plaintext, privacyKey, salt);
			}

			asnEncryptedScopedPdu = new AsnOctets(encryptedText);
			if (AsnObject.debug > 10) {
				System.out.println("Encrypted body  with " + SnmpContextv3Face.PROTOCOL_NAMES[privacyProtocol]);
			}
		} else {
			privacyAsnOctets = new AsnOctets("");
		}
		asnSecurityObject.add(privacyAsnOctets);

		ByteArrayOutputStream secOut = new ByteArrayOutputStream();
		asnSecurityObject.write(secOut);
		byte[] bytes = secOut.toByteArray();
		AsnOctets asnSecurityParameters = new AsnOctets(bytes);

		asnSequence.add(new AsnInteger(SnmpConstants.SNMP_VERSION_3));
		asnSequence.add(asnHeaderData);
		asnSequence.add(asnSecurityParameters);
		if (context.isUsePrivacy()) {
			asnSequence.add(asnEncryptedScopedPdu);
		} else {
			asnSequence.add(asnPlainScopedPdu);
		}

		if (AsnObject.debug > 10) {
			System.out.println("\n" + getClass().getName() + ".EncodeSNMPv3(): ");
		}
		// Write SNMP object
		encodedSnmpMessageOutputStream = new ByteArrayOutputStream();
		asnSequence.write(encodedSnmpMessageOutputStream);

		int sz = encodedSnmpMessageOutputStream.size();
		if (sz > context.getMaxRecvSize()) {
			throw new EncodingException(
					"Packet size (" + sz + ") is > maximum size (" + context.getMaxRecvSize() + ")");
		}
		byte[] message = encodedSnmpMessageOutputStream.toByteArray();

		// can only do this at after building the whole message
		if (context.isUseAuthentication()) {
			byte[] computedFingerprint = null;

			// Calculate the fingerprint
			computedFingerprint = context.computeFingerprint(node.getSnmpEngineId(), authenticationProtocol,
					computedFingerprint, message);

			int usmPos = asnSecurityParameters.getContentsPos();
			int fpPos = fingerPrintOctets.getContentsPos();
			fpPos += usmPos;
			if (AsnObject.debug > 10) {
				int fpLength = fingerPrintOctets.getContentsLength();
				String str = "Pos finger print = " + fpPos + ", len = " + fpLength;
				SnmpUtilities.dumpBytes(str, computedFingerprint);
			}

			// Copy the fingerprint to the message
			SnmpUtilities.copyFingerprintToSnmpMessage(authenticationProtocol, computedFingerprint, message, fpPos);

		}
		return message;
	}

	private byte[] getMessageFlags(SnmpContextv3Basis context, byte messageType) throws EncodingException {
		byte authMask = (byte) (0x0);
		if (context.isUseAuthentication()) {
			authMask = (byte) (0x1);
		}
		byte privMask = (byte) (0x0);
		if (context.isUsePrivacy()) {
			if (context.isUseAuthentication()) {
				privMask = (byte) (0x2);
			} else {
				throw new EncodingException("Encryption without authentication is not allowed");
			}
		}
		byte reportMask = (byte) (0x0);
		if (context.isAuthoritative(messageType) == false) {
			reportMask = (byte) (0x4);
		}
		byte[] msgFlags = new byte[1];
		msgFlags[0] = (byte) (authMask | privMask | reportMask);
		return msgFlags;
	}
}
