// NAME
//      $RCSfile: SnmpContextv3Basis.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.17 $
// CREATED
//      $Date: 2009/03/05 15:51:42 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2005 - 2006 by Westhawk Ltd
 * <a href="www.westhawk.co.uk">www.westhawk.co.uk</a>
 *
 * Permission to use, copy, modify, and distribute this software
 * for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that
 * both the copyright notice and this permission notice appear in
 * supporting documentation.
 * This software is provided "as is" without express or implied
 * warranty.
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

import uk.co.westhawk.snmp.beans.UsmDiscoveryBean;
import uk.co.westhawk.snmp.event.RequestPduListener;
import uk.co.westhawk.snmp.pdu.DiscoveryPdu;
import uk.co.westhawk.snmp.util.SnmpUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.function.BiFunction;

/**
 * This class contains the basis for the SNMP v3 contexts that is needed 
 * by every PDU to send a SNMP v3 request.
 *
 * <p>
 * This class will perform the v3 discovery of the SNMP engine ID and
 * time line if necessary. This is done with the classes
 * <code>TimeWindow</code> and <code>UsmDiscoveryBean</code>.
 * </p>
 *
 * <p>
 * Now that the stack can send traps and receive requests, 
 * it needs to be able to act as an
 * authoritative SNMP engine. This is done via the interface UsmAgent.
 * The DefaultUsmAgent is not guaranteed to work; agents (or rather 
 * authoritative engines) <em>should</em> provide a better implementation.
 * </p>
 *
 * <p>
 * This class will use the User Security Model (USM) as described in 
 * <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
 * See also <a href="http://www.ietf.org/rfc/rfc3411.txt">RFC 3411</a>.
 * </p>
 *
 * <p>
 * It is advised to set all the properties of this class before any PDU,
 * using this class, is sent. 
 * All properties are being used to encode the message. Some properties are 
 * being used to decode the Response or Report PDU. 
 * When any of these last properties were changed in between flight there 
 * is a possibility the decoding fails, causing a
 * <code>DecodingException</code>. 
 * </p>
 * 
 * <p>
 * <code>destroy()</code> should be called when the context is no longer
 * used. This is the only way the threads will be stopped and garbage
 * collected.
 * </p>
 *
 * @see SnmpContextv3Face
 * @see SnmpContextv3Pool
 * @see TimeWindow
 * @see UsmAgent
 * @see DefaultUsmAgent
 * @see #setUsmAgent(UsmAgent)
 * @see UsmDiscoveryBean
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.17 $ $Date: 2009/03/05 15:51:42 $
 */
public abstract class SnmpContextv3Basis extends AbstractSnmpContext
        implements SnmpContextv3Face, Cloneable {
    private static final String version_id = "@(#)$Id: SnmpContextv3Basis.java,v 3.17 2009/03/05 15:51:42 birgita Exp $ Copyright Westhawk Ltd";

    public static final int AES128_KEY_LENGTH = 16;
    public static final int AES192_KEY_LENGTH = 24;
    public static final int AES256_KEY_LENGTH = 32;

    protected String userName = DEFAULT_USERNAME;
    protected boolean useAuthentication = false;
    protected String userAuthenticationPassword;
    protected byte[] userAuthKeyMD5 = null;
    protected byte[] userAuthKeySHA1 = null;
    protected byte[] userAuthKeySHA256 = null;
    protected byte[] userAuthKeySHA512 = null;
    protected byte[] userAuthKeySHA224 = null;
    protected byte[] userAuthKeySHA384 = null;
    protected int authenticationProtocol = MD5_PROTOCOL;
    protected int privacyProtocol = DES_ENCRYPT;
    protected boolean usePrivacy = false;
    protected String userPrivacyPassword;
    protected byte[] userPrivKeyMD5 = null;
    protected byte[] userPrivKeySHA1 = null;
    protected byte[] userPrivKeySHA256 = null;
    protected byte[] userPrivKeySHA512 = null;
    protected byte[] userPrivKeySHA224 = null;
    protected byte[] userPrivKeySHA384 = null;
    protected byte[] contextEngineId = new byte[0];
    protected String contextName = DEFAULT_CONTEXT_NAME;
    protected UsmAgent usmAgent = null;

    private Hashtable msgIdHash = new Hashtable(MAXPDU);
    private static int next_id = 1;

    /**
     * Constructor.
     *
     * @param host The host to which the PDU will be sent
     * @param port The port where the SNMP server will be
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int)
     */
    public SnmpContextv3Basis(String host, int port) throws IOException {
        this(host, port, null, STANDARD_SOCKET);
    }

    /**
     * Constructor.
     * Parameter typeSocketA should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the Pdu will be sent
     * @param port        The port where the SNMP server will be
     * @param typeSocketA The local address the server will bind to
     *
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int, String)
     */
    public SnmpContextv3Basis(String host, int port, String typeSocketA)
            throws IOException {
        this(host, port, null, typeSocketA);
    }

    /**
     * Constructor.
     * Parameter typeSocketA should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the PDU will be sent
     * @param port        The port where the SNMP server will be
     * @param bindAddress The local address the server will bind to
     * @param typeSocketA The type of socket to use.
     *
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     * @since 4_14
     */
    public SnmpContextv3Basis(String host, int port, String bindAddress, String typeSocketA)
            throws IOException {
        super(host, port, bindAddress, typeSocketA);

        if (TimeWindow.getCurrent() == null) {
            TimeWindow timew = new TimeWindow();
        }
        setUsmAgent(createUsmAgent());
    }

    public int getVersion() {
        return SnmpConstants.SNMP_VERSION_3;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username.
     * This username will be used for all PDUs sent with this context.
     * The username corresponds to the 'msgUserName' in
     * <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
     * The default value is "initial".
     *
     * @param newUserName The new username
     * @see #DEFAULT_USERNAME
     */
    public void setUserName(String newUserName) {
        userName = newUserName;
    }

    /**
     * Returns if authentication is used or not.
     * By default no authentication will be used.
     *
     * @return true if authentication is used, false if not
     */
    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    /**
     * Sets whether authentication has to be used.
     * By default no authentication will be used.
     *
     * @param newUseAuthentication The use of authentication
     */
    public void setUseAuthentication(boolean newUseAuthentication) {
        useAuthentication = newUseAuthentication;
    }

    /**
     * Returns the user authentication password.
     * This password will be transformed into the user authentication secret key.
     *
     * @return The user authentication password
     */
    public String getUserAuthenticationPassword() {
        return userAuthenticationPassword;
    }

    /**
     * Sets the user authentication password.
     * This password will be transformed into the user authentication secret
     * key. A user MUST set this password.
     *
     * @param newUserAuthPassword The user authentication password
     */
    public void setUserAuthenticationPassword(String newUserAuthPassword) {
        if (newUserAuthPassword != null
                &&
                newUserAuthPassword.equals(userAuthenticationPassword) == false) {
            userAuthenticationPassword = newUserAuthPassword;
            userAuthKeyMD5 = null;
            userAuthKeySHA1 = null;
            userAuthKeySHA256 = null;
            userAuthKeySHA512 = null;
            userAuthKeySHA224 = null;
            userAuthKeySHA384 = null;
        }
    }

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
            throws IllegalArgumentException {
        if (AUTH_PROTOCOLS.contains(protocol)) {
            if (protocol != authenticationProtocol) {
                authenticationProtocol = protocol;
            }
        } else {
            throw new IllegalArgumentException("Authentication Protocol "
                    + "should be MD5 or SHA1 or SHA256 or SHA512 or SHA224 or SHA384");
        }
    }

    /**
     * Returns the protocol to be used for authentication.
     * This can either be MD5 or SHA-1.
     * By default MD5 will be used.
     *
     * @return The authentication protocol to be used
     * @see #MD5_PROTOCOL
     * @see #SHA1_PROTOCOL
     */
    public int getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    /**
     * Sets the protocol to be used for privacy.
     * This can either be DES or AES.
     * By default DES will be used.
     *
     * @param protocol The privacy protocol to be used
     * @see SnmpContextv3Face#AES_ENCRYPT
     * @see SnmpContextv3Face#DES_ENCRYPT
     */
    public void setPrivacyProtocol(int protocol)
            throws IllegalArgumentException {
        if (PRIVACY_PROTOCOLS.contains(protocol)) {
            if (protocol != privacyProtocol) {
                privacyProtocol = protocol;
            }
        } else {
            throw new IllegalArgumentException("Privacy Encryption "
                    + "should be AES, AES192, AES256 or DES");
        }
    }

    /**
     * Returns the protocol to be used for privacy.
     * This can either be DES or AES.
     * By default DES will be used.
     *
     * @return The privacy protocol to be used
     * @see SnmpContextv3Face#AES_ENCRYPT
     * @see SnmpContextv3Face#DES_ENCRYPT
     */
    public int getPrivacyProtocol() {
        return privacyProtocol;
    }

    byte[] getAuthenticationPasswordKeyMD5() {
        if (userAuthKeyMD5 == null) {
            userAuthKeyMD5 = SnmpUtilities.passwordToKeyMD5(userAuthenticationPassword);
        }
        return userAuthKeyMD5;
    }

    byte[] getAuthenticationPasswordKeySHA1() {
        if (userAuthKeySHA1 == null) {
            userAuthKeySHA1 = SnmpUtilities.passwordToKeySHA1(userAuthenticationPassword);
        }
        return userAuthKeySHA1;
    }

    /**
     * Returns the authentication password key for SHA256.
     * 
     * @return the authentication password key for SHA256
     */
    byte[] getAuthenticationPasswordKeySHA256() {
        if (userAuthKeySHA256 == null) {
            userAuthKeySHA256 = SnmpUtilities.passwordToKeySHA256(userAuthenticationPassword);
        }
        return userAuthKeySHA256;
    }

    /**
     * Returns the authentication password key for SHA-384.
     *
     * @return the authentication password key for SHA-384
     */
    byte[] getAuthenticationPasswordKeySHA384() {
        if (userAuthKeySHA384 == null) {
            userAuthKeySHA384 = SnmpUtilities.passwordToKeySHA384(userAuthenticationPassword);
        }
        return userAuthKeySHA384;
    }

    /**
     * Returns the authentication password key for SHA-224.
     *
     * @return the authentication password key for SHA-224
     */
    byte[] getAuthenticationPasswordKeySHA224() {
        if (userAuthKeySHA224 == null) {
            userAuthKeySHA224 = SnmpUtilities.passwordToKeySHA224(userAuthenticationPassword);
        }
        return userAuthKeySHA224;
    }

    /**
     * Returns the authentication password key for SHa512.
     *
     * @return the authentication password key for SHA512
     */
    byte[] getAuthenticationPasswordKeySHA512() {
        if (userAuthKeySHA512 == null) {
            userAuthKeySHA512 = SnmpUtilities.passwordToKeySHA512(userAuthenticationPassword);
        }
        return userAuthKeySHA512;
    }

    byte[] getPrivacyPasswordKeyMD5() {
        if (userPrivKeyMD5 == null) {
            userPrivKeyMD5 = SnmpUtilities.passwordToKeyMD5(userPrivacyPassword);
        }
        return userPrivKeyMD5;
    }

    byte[] getPrivacyPasswordKeySHA1() {
        if (userPrivKeySHA1 == null) {
            userPrivKeySHA1 = SnmpUtilities.passwordToKeySHA1(userPrivacyPassword);
        }
        return userPrivKeySHA1;
    }

    /**
     * Returns the privacy password key for SHA256.
     * 
     * @return the privacy password key for SHA256
     */
    byte[] getPrivacyPasswordKeySHA256() {
        if (userPrivKeySHA256 == null) {
            userPrivKeySHA256 = SnmpUtilities.passwordToKeySHA256(userPrivacyPassword);
        }
        return userPrivKeySHA256;
    }

    /**
     * Returns the privacy password key for SHA-224.
     *
     * @return the privacy password key for SHA-224
     */
    byte[] getPrivacyPasswordKeySHA224() {
        if (userPrivKeySHA224 == null) {
            userPrivKeySHA224 = SnmpUtilities.passwordToKeySHA224(userPrivacyPassword);
        }
        return userPrivKeySHA224;
    }

    /**
     * Returns the privacy password key for SHA-384.
     *
     * @return the privacy password key for SHA-384
     */
    byte[] getPrivacyPasswordKeySHA384() {
        if (userPrivKeySHA384 == null) {
            userPrivKeySHA384 = SnmpUtilities.passwordToKeySHA384(userPrivacyPassword);
        }
        return userPrivKeySHA384;
    }

    /**
     * Returns the privacy password key for SHA512.
     *
     * @return the privacy password key for SHA512
     */
    byte[] getPrivacyPasswordKeySHA512() {
        if (userPrivKeySHA512 == null) {
            userPrivKeySHA512 = SnmpUtilities.passwordToKeySHA512(userPrivacyPassword);
        }
        return userPrivKeySHA512;
    }

    /**
     * Returns if privacy is used or not.
     * By default privacy is not used.
     *
     * @return true if privacy is used, false if not
     */
    public boolean isUsePrivacy() {
        return usePrivacy;
    }

    /**
     * Sets whether privacy has to be used.
     * By default privacy is not used.
     * Note, privacy (encryption) without authentication is not allowed.
     *
     * @param newUsePrivacy The use of privacy
     */
    public void setUsePrivacy(boolean newUsePrivacy) {
        usePrivacy = newUsePrivacy;
    }

    /**
     * Returns the user privacy password.
     * This password will be transformed into the user privacy secret key.
     *
     * @return The user privacy password
     */
    public String getUserPrivacyPassword() {
        return userPrivacyPassword;
    }

    /**
     * Sets the user privacy password.
     * This password will be transformed into the user privacy secret
     * key. A user <em>must</em> set this password in order to use privacy.
     *
     * @param newUserPrivacyPassword The user privacy password
     */
    public void setUserPrivacyPassword(String newUserPrivacyPassword) {
        if (newUserPrivacyPassword != null
                &&
                newUserPrivacyPassword.equals(userPrivacyPassword) == false) {
            userPrivacyPassword = newUserPrivacyPassword;
            userPrivKeyMD5 = null;
            userPrivKeySHA1 = null;
            userPrivKeySHA256 = null;
            userPrivKeySHA512 = null;
            userPrivKeySHA224 = null;
            userPrivKeySHA384 = null;
        }
    }

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
            throws IllegalArgumentException {
        if (newContextEngineId != null) {
            contextEngineId = newContextEngineId;
        } else {
            throw new IllegalArgumentException("contextEngineId is null");
        }
    }

    /**
     * Returns the contextEngineID.
     *
     * @return The contextEngineID
     */
    public byte[] getContextEngineId() {
        return contextEngineId;
    }

    /**
     * Sets the contextName.
     * See <a href="http://www.ietf.org/rfc/rfc3411.txt">RFC 3411</a>.
     *
     * A contextName is used to name a context. Each contextName MUST be
     * unique within an SNMP entity.
     * By default this is "" (the empty String).
     *
     * @param newContextName The contextName
     * @see #DEFAULT_CONTEXT_NAME
     */
    public void setContextName(String newContextName) {
        contextName = newContextName;
    }

    /**
     * Returns the contextName.
     *
     * @return The contextName
     */
    public String getContextName() {
        return contextName;
    }

    /**
     * Adds a discovery pdu. This method adds the PDU (without checking if
     * discovery is needed).
     *
     * @param pdu the discovery pdu
     * @return pdu is succesful added
     * @see AbstractSnmpContext#addPdu(Pdu)
     * @see #addPdu(Pdu)
     */
    public boolean addDiscoveryPdu(DiscoveryPdu pdu)
            throws IOException, PduException {
        // since this is a DiscoveryPdu we do not check for discovery :-)
        return this.addPdu(pdu, false);
    }

    /**
     * Adds a PDU. This method adds the PDU and blocks until it has all the
     * discovery parameters it needs.
     *
     * @param pdu the PDU
     * @return pdu is succesful added
     * @see AbstractSnmpContext#addPdu(Pdu)
     * @see #addDiscoveryPdu(DiscoveryPdu)
     */
    public boolean addPdu(Pdu pdu)
            throws IOException, PduException {
        return this.addPdu(pdu, true);
    }

    /**
     * Creates the USM agent.
     * 
     * @see DefaultUsmAgent
     * @see #isAuthoritative
     */
    protected UsmAgent createUsmAgent() {
        return new DefaultUsmAgent();
    }

    /**
     * Sets the UsmAgent, needed when this stack is used as authoritative
     * SNMP engine. This interface provides authentiation details, like its
     * clock and its Engine ID.
     * 
     * @see DefaultUsmAgent
     * @param agent The USM authoritative interface
     * @since 4_14
     */
    public void setUsmAgent(UsmAgent agent) {
        usmAgent = agent;
    }

    /**
     * Returns the UsmAgent.
     * 
     * @see #setUsmAgent
     * @since 4_14
     */
    public UsmAgent getUsmAgent() {
        return usmAgent;
    }

    /**
     * Adds a PDU. This method adds the PDU and checks if discovery is
     * needed depending on the parameter <code>checkDiscovery</code>.
     * If discovery is needed this method will block until it has done so.
     * Discovery is only needed if the stack is non authoritative.
     *
     * <p>
     * This method stores the SNMPv3 msgId and PDU
     * request id in a Hashtable.
     * Since the encoding only happens once and every retry sends the same
     * encoded packet, only one msgId is used.
     * </p>
     *
     * @param pdu            the PDU
     * @param checkDiscovery check if discovery is needed
     * @return pdu is succesful added
     * @see AbstractSnmpContext#addPdu(Pdu)
     * @see #addDiscoveryPdu(DiscoveryPdu)
     * @see #addPdu(Pdu)
     */
    protected boolean addPdu(Pdu pdu, boolean checkDiscovery)
            throws IOException, PduException {
        // TODO, when sending response or report, the msgId should be set!
        Integer msgId = pdu.snmpv3MsgId;
        if (msgId == null) {
            msgId = new Integer(next_id++);
        } else if (pdu.isExpectingResponse() == true) {
            // generate a new msgId, even if this is already set. The user
            // could be adding the same PDU more than once to the
            // context.
            msgId = new Integer(next_id++);
        }
        pdu.snmpv3MsgId = msgId;

        msgIdHash.put(msgId, new Integer(pdu.req_id));
        if (AsnObject.debug > 6) {
            System.out.println(getClass().getName() + ".addPdu(): msgId="
                    + msgId.toString() + ", Pdu reqId=" + pdu.req_id);
        }

        if (checkDiscovery == true && isAuthoritative(pdu.getMsgType()) == false) {
            discoverIfNeeded(pdu);
        }

        boolean added = super.addPdu(pdu);
        return added;
    }

    /**
     * Removes a PDU. This removes the PDU from the AbstractSnmpContext and
     * clears the link with the SNMPv3 msgId.
     *
     * @param rid the PDU request id
     * @return whether the PDU has been successfully removed
     * @see AbstractSnmpContext#removePdu(int)
     */
    public synchronized boolean removePdu(int rid) {
        boolean removed = super.removePdu(rid);
        if (removed) {
            Enumeration keys = msgIdHash.keys();
            Integer msgIdI = null;
            boolean found = false;
            while (keys.hasMoreElements() && found == false) {
                msgIdI = (Integer) keys.nextElement();
                Integer pduIdI = (Integer) msgIdHash.get(msgIdI);
                found = (pduIdI.intValue() == rid);
            }
            if (found) {
                msgIdHash.remove(msgIdI);
            }
        }
        return removed;
    }

    /**
     * Encodes a discovery PDU packet. This methods encodes without checking
     * if the discovery parameters are all known.
     */
    public byte[] encodeDiscoveryPacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, Object obj)
            throws IOException, EncodingException {
        String engineId = "";
        TimeWindow tWindow = TimeWindow.getCurrent();
        if (tWindow.isSnmpEngineIdKnown(getSendToHostAddress(), hostPort) == true) {
            engineId = tWindow.getSnmpEngineId(getSendToHostAddress(), hostPort);
        }
        TimeWindowNode node = new TimeWindowNode(engineId, 0, 0);

        return actualEncodePacket(msg_type, rId, errstat, errind, ve, node,
                obj);
    }

    /**
     * Encodes a PDU. This is for internal use only and should
     * NOT be called by the developer.
     * This is called by the the PDU itself and is added to the interface to
     * cover the different kind of Contexts.
     *
     * <p>
     * When the stack is
     * </p>
     * <ul>
     * <li>
     * authoritative, the timeline details are retrieved from the UsmAgent.
     * </li>
     * <li>
     * non authoritative, this methods first checks if all the discovery
     * parameters are known;
     * <ul>
     * <li>
     * If so, it encodes and returns the bytes.
     * </li>
     * <li>
     * If not, it will throw an EncodingException.
     * </li>
     * </ul>
     * </li>
     * </ul>
     *
     * @see #isAuthoritative(byte)
     * @param msg_type The message type
     * @param rId      The message id
     * @param errstat  The error status
     * @param errind   The error index
     * @param ve       The varbind list
     * @param obj      Additional object (only used in SNMPv3)
     * @return The encoded packet
     */
    public byte[] encodePacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, Object obj)
            throws IOException, EncodingException {
        TimeWindowNode node = null;
        if (isDestroyed == true) {
            throw new EncodingException("Context can no longer be used, since it is already destroyed");
        } else {
            TimeWindow tWindow = TimeWindow.getCurrent();
            if (isAuthoritative(msg_type) == true) {
                usmAgent.setSnmpContext(this);
                if (usmAgent.getSnmpEngineId() == null) {
                    throw new EncodingException("UsmAgent "
                            + usmAgent.getClass().getName()
                            + " should provide Engine ID!");
                }
                tWindow.updateTimeWindow(usmAgent.getSnmpEngineId(),
                        usmAgent.getSnmpEngineBoots(), usmAgent.getSnmpEngineTime(),
                        this.isUseAuthentication());
                node = tWindow.getTimeLine(usmAgent.getSnmpEngineId());
            } else {
                if (tWindow.isSnmpEngineIdKnown(getSendToHostAddress(), hostPort) == false) {
                    throw new EncodingException("Engine ID of host "
                            + getSendToHostAddress()
                            + ", port " + hostPort
                            + " is unknown (rId="
                            + rId + "). Perform discovery.");
                }
                String engineId = tWindow.getSnmpEngineId(getSendToHostAddress(), hostPort);
                node = new TimeWindowNode(engineId, 0, 0);

                if (isUseAuthentication()) {
                    if (tWindow.isTimeLineKnown(engineId) == true) {
                        node = tWindow.getTimeLine(engineId);
                    } else {
                        throw new EncodingException("Time Line of Engine ID of host "
                                + getSendToHostAddress() + ", port " + hostPort + " is unknown. "
                                + "Perform discovery.");
                    }
                }
            }
        }
        return actualEncodePacket(msg_type, rId, errstat, errind, ve, node,
                obj);
    }

    /**
     * Checks the sanity of the context and returns an error message when it
     * is not correct.
     */
    protected String checkContextSanity() {
        String ret = null;
        if (usePrivacy == true) {
            if (userPrivacyPassword == null) {
                ret = "userPrivacyPassword is null, but usePrivacy is true";
            } else if (userPrivacyPassword.length() == 0) {
                ret = "userPrivacyPassword is empty, but usePrivacy is true";
            } else if (useAuthentication == false) {
                ret = "useAuthentication is false, but usePrivacy is true";
            }
        }

        if (useAuthentication == true) {
            if (userAuthenticationPassword == null) {
                ret = "userAuthenticationPassword is null, but useAuthentication is true";
            } else if (userAuthenticationPassword.length() == 0) {
                ret = "userAuthenticationPassword is empty, but useAuthentication is true";
            }
        }
        return ret;
    }

    /**
     * Does the actual encoding.
     *
     * @see #encodeDiscoveryPacket
     * @see #encodePacket
     */
    protected byte[] actualEncodePacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, TimeWindowNode node, Object obj)
            throws IOException, EncodingException {
        AsnEncoderv3 enc = new AsnEncoderv3();
        String msg = checkContextSanity();
        if (msg != null) {
            throw new EncodingException(msg);
        }

        int msgId = ((Integer) obj).intValue();
        if (AsnObject.debug > 6) {
            System.out.println(getClass().getName() + ".actualEncodePacket(): msgId="
                    + msgId + ", Pdu reqId=" + rId);
        }
        byte[] packet = enc.EncodeSNMPv3(this, msgId, node,
                msg_type, rId, errstat, errind, ve);

        return packet;
    }

    /**
     * Processes an incoming SNMP v3 response.
     */
    protected void processIncomingResponse(ByteArrayInputStream in)
            throws DecodingException, IOException {
        AsnDecoderv3 rpdu = new AsnDecoderv3();
        // don't have to check for context sanity here: if the request was
        // fine, so should be the response
        byte[] bu = null;
        // need to duplicate the message for V3 to rewrite
        int nb = in.available();
        bu = new byte[nb];
        in.read(bu);
        in = new ByteArrayInputStream(bu);

        AsnSequence asnTopSeq = rpdu.DecodeSNMPv3(in);
        int msgId = rpdu.getMessageId(asnTopSeq);
        Integer rid = (Integer) msgIdHash.get(new Integer(msgId));
        if (rid != null) {
            if (AsnObject.debug > 6) {
                System.out.println(getClass().getName() + ".processIncomingResponse(): msgId="
                        + msgId + ", Pdu reqId=" + rid);
            }
            Pdu pdu = getPdu(rid);
            try {
                AsnPduSequence pduSeq = rpdu.processSNMPv3(this, asnTopSeq, bu, false);
                if (pduSeq != null) {
                    // got a message
                    Integer rid2 = new Integer(pduSeq.getReqId());
                    if (AsnObject.debug > 6) {
                        System.out.println(getClass().getName() + ".processIncomingResponse():"
                                + " rid2=" + rid2);
                    }

                    Pdu newPdu = null;
                    if (rid2.intValue() != rid.intValue()) {
                        newPdu = getPdu(rid2);
                        if (AsnObject.debug > 3) {
                            System.out.println(getClass().getName() + ".processIncomingResponse(): "
                                    + "pduReqId of msgId (" + rid.intValue()
                                    + ") != pduReqId of Pdu (" + rid2.intValue()
                                    + ")");
                        }
                        if (newPdu == null) {
                            if (AsnObject.debug > 3) {
                                System.out.println(getClass().getName() + ".processIncomingResponse(): "
                                        + "Using pduReqId of msgId (" + rid.intValue() + ")");
                            }
                        }
                    }

                    if (newPdu != null) {
                        pdu = newPdu;
                    }
                } else {
                    if (AsnObject.debug > 6) {
                        System.out.println(getClass().getName() + ".processIncomingResponse():"
                                + " pduSeq is null.");
                    }
                }

                if (pdu != null) {
                    pdu.fillin(pduSeq);
                } else {
                    if (AsnObject.debug > 6) {
                        System.out.println(getClass().getName() + ".processIncomingResponse(): No Pdu with reqid "
                                + rid.intValue());
                    }
                }
            } catch (DecodingException exc) {
                if (pdu != null) {
                    pdu.setErrorStatus(AsnObject.SNMP_ERR_DECODING_EXC, exc);
                    pdu.fillin(null);
                } else {
                    throw exc;
                }
            }
        } else {
            if (AsnObject.debug > 3) {
                System.out.println(getClass().getName() + ".processIncomingResponse(): Pdu of msgId " + msgId
                        + " is already answered");
            }
            rid = new Integer(-1);
        }
    }

    /**
     * Returns if we send this PDU in authoritative role or not.
     * The engine who sends a Response, a Trapv2 or a Report is
     * authoritative.
     *
     * @since 4_14
     * @return true if authoritative, false if not.
     */
    // Note: for when adding INFORM
    // When sending an INFORM, the receiver is the authoritative engine, so
    // the INFORM does NOT have to be added to this list!
    protected boolean isAuthoritative(byte msg_type) {
        return (msg_type == AsnObject.GET_RSP_MSG
                ||
                msg_type == AsnObject.TRPV2_REQ_MSG
                ||
                msg_type == AsnObject.GET_RPRT_MSG);
    }

    void discoverIfNeeded(Pdu pdu)
            throws IOException, PduException {
        UsmDiscoveryBean discBean = null;
        boolean isNeeded = false;

        TimeWindow tWindow = TimeWindow.getCurrent();
        String engineId = tWindow.getSnmpEngineId(getSendToHostAddress(), hostPort);
        if (engineId == null) {
            isNeeded = true;
            discBean = new UsmDiscoveryBean(
                    getSendToHostAddress(), hostPort, bindAddr, typeSocket);
            discBean.setRetryIntervals(pdu.getRetryIntervals());
        }

        if (isUseAuthentication()) {
            if (isNeeded) {
                discBean.setAuthenticationDetails(userName,
                        userAuthenticationPassword, authenticationProtocol);
            } else if (tWindow.isTimeLineKnown(engineId) == false) {
                isNeeded = true;
                discBean = new UsmDiscoveryBean(
                        getSendToHostAddress(), hostPort, bindAddr, typeSocket);
                discBean.setAuthenticationDetails(userName,
                        userAuthenticationPassword, authenticationProtocol);
                discBean.setRetryIntervals(pdu.getRetryIntervals());
            }

            if (isNeeded && isUsePrivacy()) {
                discBean.setPrivacyDetails(userPrivacyPassword, privacyProtocol);
            }
        }

        if (isNeeded) {
            discBean.startDiscovery();

        }

        // If contextEngineId is null or of length zero, set
        // it to the snmpEngineId.
        if (contextEngineId == null || contextEngineId.length == 0) {
            engineId = tWindow.getSnmpEngineId(getSendToHostAddress(), hostPort);
            setContextEngineId(SnmpUtilities.toBytes(engineId));
        }
    }

    /**
     * Adds the specified request pdu listener to receive PDUs on the
     * specified listening context that matches this context.
     * This method will call usmAgent.setSnmpContext(this).
     *
     * <p>
     * Don't use the TCP_SOCKET when listening for request PDUs. It doesn't
     * provide functionality to send a response back.
     * </p>
     *
     * @see AbstractSnmpContext#addRequestPduListener(RequestPduListener,
     *      ListeningContextPool)
     *
     * @param l        The request PDU listener
     * @param lcontext The listening context
     */
    public void addRequestPduListener(RequestPduListener l, ListeningContextPool lcontext)
            throws IOException {
        super.addRequestPduListener(l, lcontext);

        usmAgent.setSnmpContext(this);
        TimeWindow tWindow = TimeWindow.getCurrent();
        if (usmAgent.getSnmpEngineId() == null) {
            throw new IOException("UsmAgent "
                    + usmAgent.getClass().getName()
                    + " should provide Engine ID!");
        }
        tWindow.setSnmpEngineId(usmAgent.MYFAKEHOSTNAME, hostPort, usmAgent.getSnmpEngineId());
        tWindow.updateTimeWindow(usmAgent.getSnmpEngineId(),
                usmAgent.getSnmpEngineBoots(), usmAgent.getSnmpEngineTime(),
                this.isUseAuthentication());
    }

    /**
     * Copies all parameters into another SnmpContextv3.
     */
    public Object cloneParameters(SnmpContextv3Face clContext) {
        clContext.setUserName(new String(userName));
        clContext.setUseAuthentication(useAuthentication);
        if (userAuthenticationPassword != null) {
            clContext.setUserAuthenticationPassword(
                    new String(userAuthenticationPassword));
        }
        clContext.setAuthenticationProtocol(authenticationProtocol);

        clContext.setUsePrivacy(usePrivacy);
        if (userPrivacyPassword != null) {
            clContext.setUserPrivacyPassword(new String(userPrivacyPassword));
        }
        clContext.setPrivacyProtocol(privacyProtocol);

        clContext.setContextName(new String(contextName));

        int l = contextEngineId.length;
        byte[] newContextEngineId = new byte[l];
        System.arraycopy(contextEngineId, 0, newContextEngineId, 0, l);
        clContext.setContextEngineId(newContextEngineId);

        clContext.setUsmAgent(usmAgent);
        return clContext;
    }

    /**
     * Returns the hash key. This key is built out of all properties. It
     * serves as key for a hashtable of (v3) contexts.
     *
     * @since 4_14
     * @return The hash key
     */
    public String getHashKey() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(hostname);
        buffer.append("_").append(hostPort);
        buffer.append("_").append(bindAddr);
        buffer.append("_").append(typeSocket);
        buffer.append("_").append(useAuthentication);
        buffer.append("_").append(PROTOCOL_NAMES[authenticationProtocol]);
        buffer.append("_").append(PROTOCOL_NAMES[privacyProtocol]);
        buffer.append("_").append(userAuthenticationPassword);
        buffer.append("_").append(userName);
        buffer.append("_").append(usePrivacy);
        buffer.append("_").append(userPrivacyPassword);
        buffer.append("_").append(SnmpUtilities.toHexString(contextEngineId));
        buffer.append("_").append(contextName);
        buffer.append("_v").append(getVersion());

        return buffer.toString();
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return The string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(getClass().getName() + "[");
        buffer.append("host=").append(hostname);
        buffer.append(", sendToHost=").append(getSendToHostAddress());
        buffer.append(", port=").append(hostPort);
        buffer.append(", bindAddress=").append(bindAddr);
        buffer.append(", socketType=").append(typeSocket);
        buffer.append(", contextEngineId=").append(SnmpUtilities.toHexString(contextEngineId));
        buffer.append(", contextName=").append(contextName);
        buffer.append(", userName=").append(userName);
        buffer.append(", useAuthentication=").append(useAuthentication);
        buffer.append(", authenticationProtocol=").append(PROTOCOL_NAMES[authenticationProtocol]);
        buffer.append(", userAuthenticationPassword=").append(userAuthenticationPassword);
        buffer.append(", usePrivacy=").append(usePrivacy);
        buffer.append(", privacyProtocol=").append(PROTOCOL_NAMES[privacyProtocol]);
        buffer.append(", userPrivacyPassword=").append(userPrivacyPassword);
        buffer.append(", #trapListeners=").append(trapSupport.getListenerCount());
        buffer.append(", #pduListeners=").append(pduSupport.getListenerCount());
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Generates the privacy key based on the authentication protocol.
     *
     * @param engineId               The SNMP engine ID.
     * @param authenticationProtocol The authentication protocol.
     * @param privacyProtocol        The privacyProtocol.
     * @return The generated privacy key.
     */
    protected byte[] generatePrivacyKey(String engineId, int authenticationProtocol, int privacyProtocol) {
        byte[] derivedPrivacyKey;
        byte[] localizedPrivacyKey = null;
        byte[] localizedPrivacyKeyBase;
		switch (authenticationProtocol) {
			case MD5_PROTOCOL:
				if (privacyProtocol != AES_ENCRYPT && privacyProtocol != DES_ENCRYPT) {
	                throw new IllegalArgumentException("Unsupported privacy protocol for MD5: "
	                								+ PROTOCOL_NAMES[privacyProtocol]);
	            }
				derivedPrivacyKey = getPrivacyPasswordKeyMD5();
	            return SnmpUtilities.getLocalizedKeyMD5(derivedPrivacyKey, engineId);
			case SHA1_PROTOCOL:
				if (privacyProtocol != AES192_ENCRYPT && privacyProtocol != DES_ENCRYPT) {
	                throw new IllegalArgumentException("Unsupported privacy protocol for SHA1: " 
	                								+ PROTOCOL_NAMES[privacyProtocol]);
	            }
	            derivedPrivacyKey = getPrivacyPasswordKeySHA1();
	            return SnmpUtilities.getLocalizedKeySHA1(derivedPrivacyKey, engineId);
			case SHA224_PROTOCOL: {
				derivedPrivacyKey = getPrivacyPasswordKeySHA224();
				localizedPrivacyKeyBase = SnmpUtilities.getLocalizedKeySHA224(derivedPrivacyKey, engineId);
				switch (privacyProtocol) {
				case AES_ENCRYPT:
					return Arrays.copyOf(localizedPrivacyKeyBase, AES128_KEY_LENGTH);
				case AES192_ENCRYPT:
					return Arrays.copyOf(localizedPrivacyKeyBase, AES192_KEY_LENGTH);
				case DES_ENCRYPT:
					return localizedPrivacyKeyBase;
				default:
					throw new IllegalArgumentException(
							"Unsupported privacy protocol for SHA224: " + PROTOCOL_NAMES[privacyProtocol]);
				}
			}
			case SHA256_PROTOCOL: {
				derivedPrivacyKey = getPrivacyPasswordKeySHA256();
				localizedPrivacyKey = deriveKey(engineId, derivedPrivacyKey, SnmpUtilities::getLocalizedKeySHA256, privacyProtocol);
				return localizedPrivacyKey;
			}
			case SHA384_PROTOCOL: {
				derivedPrivacyKey = getPrivacyPasswordKeySHA384();
				localizedPrivacyKey = deriveKey(engineId, derivedPrivacyKey, SnmpUtilities::getLocalizedKeySHA384, privacyProtocol);
				return localizedPrivacyKey;
			}
			case SHA512_PROTOCOL: {
				derivedPrivacyKey = getPrivacyPasswordKeySHA512();
				localizedPrivacyKey = deriveKey(engineId, derivedPrivacyKey, SnmpUtilities::getLocalizedKeySHA512, privacyProtocol);
				return localizedPrivacyKey;
			}
			
			 default:
		            throw new IllegalArgumentException("Unsupported authentication protocol: " + authenticationProtocol);
		}
		
    }
    
    /**
     * Derives a final privacy key
     *
     * @param engineId          The SNMP engine ID.
     * @param derivedPrivacyKey The SNMP engine ID.
     * @param localizeKey         A function that localizes the key using the derived key and engine ID.
     * @param privacyProtocol   The privacyProtocol
     * @return
     */
    private byte[] deriveKey(
    		String engineId,
    		byte[] derivedPrivacyKey,
    		BiFunction<byte[],
    		String, byte[]> localizeKey,
    		int privacyProtocol) {
        byte[] localizedPrivacyKeyBase = localizeKey.apply(derivedPrivacyKey, engineId);
        switch (privacyProtocol) {
            case AES_ENCRYPT:  return Arrays.copyOf(localizedPrivacyKeyBase, AES128_KEY_LENGTH);
            case AES192_ENCRYPT: return Arrays.copyOf(localizedPrivacyKeyBase, AES192_KEY_LENGTH);
            case AES256_ENCRYPT:  return Arrays.copyOf(localizedPrivacyKeyBase, AES256_KEY_LENGTH);
            case DES_ENCRYPT:
				return localizedPrivacyKeyBase;
            default:
                throw new IllegalArgumentException("Unsupported privacy protocol: " + PROTOCOL_NAMES[privacyProtocol]);
        }
     }
 

    /**
     * Computes the fingerprint for the given SNMP message.
     *
     * @param snmpEngineId           The SNMP engine ID.
     * @param authenticationProtocol The authentication protocol.
     * @param computedFingerprint    The computed fingerprint.
     * @param message                The SNMP message.
     * @return The computed fingerprint.
     */
    protected byte[] computeFingerprint(String snmpEngineId, int authenticationProtocol, byte[] computedFingerprint,
            byte[] message) {
        if (authenticationProtocol == MD5_PROTOCOL) {
            byte[] passwKey = getAuthenticationPasswordKeyMD5();
            byte[] authkey = SnmpUtilities.getLocalizedKeyMD5(passwKey, snmpEngineId);
            computedFingerprint = SnmpUtilities.getFingerPrintMD5(authkey, message);
        } else if (authenticationProtocol == SHA1_PROTOCOL) {
            byte[] passwKey = getAuthenticationPasswordKeySHA1();
            byte[] authkey = SnmpUtilities.getLocalizedKeySHA1(passwKey, snmpEngineId);
            computedFingerprint = SnmpUtilities.getFingerPrintSHA1(authkey, message);
        } else if (authenticationProtocol == SHA256_PROTOCOL) {
            byte[] passwKey = getAuthenticationPasswordKeySHA256();
            byte[] authkey = SnmpUtilities.getLocalizedKeySHA256(passwKey, snmpEngineId);
            computedFingerprint = SnmpUtilities.getFingerPrintSHA256(authkey, message);
        } else if (authenticationProtocol == SHA512_PROTOCOL) {
            byte[] passwKey = getAuthenticationPasswordKeySHA512();
            byte[] authkey = SnmpUtilities.getLocalizedKeySHA512(passwKey, snmpEngineId);
            computedFingerprint = SnmpUtilities.getFingerPrintSHA512(authkey, message);
        } else if (authenticationProtocol == SHA224_PROTOCOL) {
            byte[] passwKey = getAuthenticationPasswordKeySHA224();
            byte[] authkey = SnmpUtilities.getLocalizedKeySHA224(passwKey, snmpEngineId);
            computedFingerprint = SnmpUtilities.getFingerPrintSHA224(authkey, message);
        } else if (authenticationProtocol == SHA384_PROTOCOL) {
            byte[] passwKey = getAuthenticationPasswordKeySHA384();
            byte[] authkey = SnmpUtilities.getLocalizedKeySHA384(passwKey, snmpEngineId);
            computedFingerprint = SnmpUtilities.getFingerPrintSHA384(authkey, message);
        }
        return computedFingerprint;
    }

}
