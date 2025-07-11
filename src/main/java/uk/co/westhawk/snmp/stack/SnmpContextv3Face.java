// NAME
//      $RCSfile: SnmpContextv3Face.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.20 $
// CREATED
//      $Date: 2009/03/05 12:56:17 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2000 - 2006 by Westhawk Ltd
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
import uk.co.westhawk.snmp.pdu.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This interface contains the SNMP context interface that is needed by every 
 * PDU to send a SNMP v3 request.
 * 
 * See <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>,
 * <a href="http://www.ietf.org/rfc/rfc3411.txt">RFC 3411</a>,
 * <a href="http://www.ietf.org/rfc/rfc3826.txt">RFC 3826 (AES)</a>
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.20 $ $Date: 2009/03/05 12:56:17 $
 */
public interface SnmpContextv3Face extends SnmpContextBasisFace {
    static final String version_id = "@(#)$Id: SnmpContextv3Face.java,v 3.20 2009/03/05 12:56:17 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * The SNMPv1 security model. This has value 1.
     * The stack does not implement this security model.
     */
    public final static int SNMPv1_Security_Model = (byte) (0x1);

    /**
     * The SNMPv2c security model. This has value 2.
     * The stack does not implement this security model.
     */
    public final static int SNMPv2c_Security_Model = (byte) (0x2);

    /**
     * The USM security model. This has value 3.
     * This stack only implements this security model!
     */
    public final static int USM_Security_Model = (byte) (0x3);

    /**
     * The MD5 protocol type.
     */
    public final static int MD5_PROTOCOL = 0;

    /**
     * The SHA-1 protocol type.
     */
    public final static int SHA1_PROTOCOL = 1;

    /**
     * The DES encryption type.
     */
    public final static int DES_ENCRYPT = 2;

    /**
     * The AES 128 encryption type.
     */
    public final static int AES_ENCRYPT = 3;

    /**
     * The AES 192 encryption type.
     */
    public static final int AES192_ENCRYPT = 8;

    /**
     * The AES 256 encryption type.
     */
    public static final int AES256_ENCRYPT = 9;

    /**
     * The default value for the (security) user name. This is
     * "initial".
     */
    public final static String DEFAULT_USERNAME = "initial";

    /**
     * The default Context Name. This is the zero length string, i.e. "".
     */
    public final static String DEFAULT_CONTEXT_NAME = "";

    /**
     * The array with the String represensations of the protocols.
     */
    public final static String PROTOCOL_NAMES[] = {
            "MD5",
            "SHA1",
            "DES",
            "AES",
            "SHA256",
            "SHA512",
            "SHA224",
            "SHA384",
            "AES192",
            "AES256",
    };

    /**
     * The NoAuth protocol type.
     */
    public static final int NO_AUTH_PROTOCOL = PROTOCOL_NAMES.length;

    /**
     * The SHA-256 protocol type.
     */
    public static final int SHA256_PROTOCOL = 4;

    /**
     * The SHA-512 protocol type.
     */
    public static final int SHA512_PROTOCOL = 5;

    /**
     * The SHA-224 protocol type.
     */
    public static final int SHA224_PROTOCOL = 6;

    /**
     * The SHA-224 protocol type.
     */
    public static final int SHA384_PROTOCOL = 7;
    
    /**
     * Authentication protocols codes that are supported by SNMPv3.
     */
    public static final Set<Integer> AUTH_PROTOCOLS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(MD5_PROTOCOL, SHA1_PROTOCOL,
                    SHA256_PROTOCOL, SHA512_PROTOCOL,
                    SHA224_PROTOCOL, SHA384_PROTOCOL)));
    /**
     * Privacy protocols codes that are supported by SNMPv3.
     */
    public static final Set<Integer> PRIVACY_PROTOCOLS = Collections.unmodifiableSet( new HashSet<>(
    		Arrays.asList(AES_ENCRYPT, AES192_ENCRYPT, AES256_ENCRYPT, DES_ENCRYPT)));
    
    public static final Set<Integer> AES_PRIVACY_PROTOCOLS = Collections.unmodifiableSet( new HashSet<>(
    		Arrays.asList(AES_ENCRYPT, AES192_ENCRYPT, AES256_ENCRYPT)));


    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUserName();

    /**
     * Sets the username.
     * This username will be used for all PDUs sent with this context.
     * The username corresponds to the 'msgUserName' in
     * <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
     * The default value is "initial".
     *
     * @param newUserName The new username
     */
    public void setUserName(String newUserName);

    /**
     * Returns if authentication is used or not.
     * By default no authentication will be used.
     *
     * @return true if authentication is used, false if not
     */
    public boolean isUseAuthentication();

    /**
     * Sets whether authentication has to be used.
     * By default no authentication will be used.
     *
     * @param newUseAuthentication The use of authentication
     */
    public void setUseAuthentication(boolean newUseAuthentication);

    /**
     * Returns the user authentication password.
     * This password will be transformed into the user authentication secret key.
     *
     * @return The user authentication password
     */
    public String getUserAuthenticationPassword();

    /**
     * Sets the user authentication password.
     * This password will be transformed into the user authentication secret
     * key. A user MUST set this password.
     *
     * @param newUserAuthPassword The user authentication password
     */
    public void setUserAuthenticationPassword(String newUserAuthPassword)
            throws IllegalArgumentException;

    /**
     * Sets the protocol to be used for authentication.
     * This can either be MD5 or SHA-1.
     * By default MD5 will be used.
     *
     * @param protocol The authentication protocol to be used
     * @see #MD5_PROTOCOL
     * @see #SHA1_PROTOCOL
     */
    public void setAuthenticationProtocol(int protocol)
            throws IllegalArgumentException;

    /**
     * Returns the protocol to be used for authentication.
     * This can either be MD5 or SHA-1.
     * By default MD5 will be used.
     *
     * @return The authentication protocol to be used
     * @see #MD5_PROTOCOL
     * @see #SHA1_PROTOCOL
     */
    public int getAuthenticationProtocol();

    /**
     * Sets the protocol to be used for privacy.
     * This can either be DES or AES.
     * By default DES will be used.
     *
     * @param protocol The privacy protocol to be used
     * @see #DES_ENCRYPT
     * @see #AES_ENCRYPT
     */
    public void setPrivacyProtocol(int protocol)
            throws IllegalArgumentException;

    /**
     * Returns the protocol to be used for privacy.
     * This can either be DES or AES.
     * By default DES will be used.
     *
     * @return The privacy protocol to be used
     * @see #DES_ENCRYPT
     * @see #AES_ENCRYPT
     */
    public int getPrivacyProtocol();

    /**
     * Returns if privacy is used or not.
     * By default no privacy will be used.
     *
     * @return true if privacy is used, false if not
     */
    public boolean isUsePrivacy();

    /**
     * Sets whether privacy has to be used.
     * By default no privacy will be used.
     *
     * @param newUsePrivacy The use of privacy
     */
    public void setUsePrivacy(boolean newUsePrivacy);

    /**
     * Returns the user privacy password.
     * This password will be transformed into the user privacy secret key.
     *
     * @return The user privacy password
     */
    public String getUserPrivacyPassword();

    /**
     * Sets the user privacy password.
     * This password will be transformed into the user privacy secret
     * key. A user MUST set this password.
     *
     * @param newUserAuthPassword The user privacy password
     */
    public void setUserPrivacyPassword(String newUserAuthPassword)
            throws IllegalArgumentException;

    /**
     * Sets the contextEngineID.
     * See <a href="http://www.ietf.org/rfc/rfc3411.txt">RFC 3411</a>.
     *
     * A contextEngineID uniquely
     * identifies an SNMP entity that may realize an instance of a context
     * with a particular contextName.
     * 
     * <p>
     * Note, when the stack is an authoritative engine, this parameter should
     * equal the UsmAgent.getSnmpEngineId(). See the StackUsage
     * documentation for an explanation.
     * </p>
     *
     * <p>
     * If the contextEngineID is of length zero, the encoder will use the
     * (discovered)
     * snmpEngineId.
     * </p>
     *
     * @see UsmAgent#getSnmpEngineId()
     * @param newContextEngineId The contextEngineID
     */
    public void setContextEngineId(byte[] newContextEngineId)
            throws IllegalArgumentException;

    /**
     * Returns the contextEngineID.
     *
     * @return The contextEngineID
     */
    public byte[] getContextEngineId();

    /**
     * Sets the contextName.
     * See <a href="http://www.ietf.org/rfc/rfc3411.txt">RFC 3411</a>.
     *
     * A contextName is used to name a context. Each contextName MUST be
     * unique within an SNMP entity.
     * By default this is "".
     *
     * @param newContextName The contextName
     * @see #DEFAULT_CONTEXT_NAME
     */
    public void setContextName(String newContextName);

    /**
     * Returns the contextName.
     *
     * @return The contextName
     */
    public String getContextName();

    /**
     * Adds a discovery PDU.
     * This is for internal use only and should
     * NOT be called by the developer.
     *
     * This is called by the the PDU itself and is added to the interface to
     * cover the different kind of Contexts.
     * 
     * @param pdu the discovery PDU
     * @return PDU is succesful added
     */
    public boolean addDiscoveryPdu(DiscoveryPdu pdu)
            throws java.io.IOException, PduException;

    /**
     * Encodes a discovery PDU.
     * This is for internal use only and should
     * NOT be called by the developer.
     *
     * This is called by the the PDU itself and is added to the interface to
     * cover the different kind of Contexts.
     * 
     * @return The encoded packet
     */
    public byte[] encodeDiscoveryPacket(byte msg_type, int rId, int errstat,
            int errind, java.util.Enumeration ve, Object obj)
            throws java.io.IOException, EncodingException;

    /**
     * Sets the UsmAgent, needed when this stack is used as authoritative
     * SNMP engine. This interface provides authentiation details, like its
     * clock and its Engine ID.
     * 
     * @since 4_14
     * @param agent The USM authoritative interface
     */
    public void setUsmAgent(UsmAgent agent);

    /**
     * Returns the UsmAgent.
     *
     * @since 4_14
     * @see #setUsmAgent
     */
    public UsmAgent getUsmAgent();

}
