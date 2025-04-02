// NAME
//      $RCSfile: SnmpContextv3Pool.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.27 $
// CREATED
//      $Date: 2009/03/05 13:27:41 $
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
import uk.co.westhawk.snmp.util.*;
import uk.co.westhawk.snmp.event.*;
import java.util.*;

/**
 * This class contains the pool of SNMP v3 contexts.
 * This class reuses the existings contexts instead of creating a new
 * one every time.
 * <p>
 * Every time a property changes the pool is checked for a SnmpContextv3
 * context that matches all the new properties of this class. If no such
 * context exists, a new one is made.
 * The PDUs associated with the old context remain associated with the
 * old context.
 * </p>
 *
 * <p>
 * A counter indicates the number of times the context is referenced.
 * The counter is decreased when <code>destroy()</code> is called.
 * When the counter
 * reaches zero, the context is released.
 * </p>
 *
 * <p>
 * Note that because the underlying context can change when a property
 * is changed and the PDUs remain associated with the old context, all
 * properties have to be set BEFORE a PDU is sent.
 * </p>
 *
 * @see SnmpContextv3
 * @see SnmpContextPool
 * @see SnmpContextv2cPool
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.27 $ $Date: 2009/03/05 13:27:41 $
 */
public class SnmpContextv3Pool implements SnmpContextv3Face {
    private static final String version_id = "@(#)$Id: SnmpContextv3Pool.java,v 3.27 2009/03/05 13:27:41 birgita Exp $ Copyright Westhawk Ltd";

    protected static Hashtable contextPool;

    protected String hostname, socketType, bindAddr;
    protected int hostPort;

    protected SnmpContextv3 context = null;
    protected String userName = "";
    protected boolean useAuthentication = false;
    protected String userAuthenticationPassword = "";
    protected boolean usePrivacy = false;
    protected String userPrivacyPassword = "";
    protected int authenticationProtocol = MD5_PROTOCOL;
    protected byte[] contextEngineId = new byte[0];
    protected String contextName = DEFAULT_CONTEXT_NAME;
    protected UsmAgent usmAgent = null;

    protected boolean hasChanged = false;
    protected int privacyProtocol = DES_ENCRYPT;

    /**
     * Constructor, using the Standard socket.
     *
     * @param host The host to which the PDU will be sent
     * @param port The port where the SNMP server will be
     * @see SnmpContextv3#SnmpContextv3(String, int)
     */
    public SnmpContextv3Pool(String host, int port) throws java.io.IOException {
        this(host, port, null, STANDARD_SOCKET);
    }

    /**
     * Constructor.
     * Parameter typeSocket should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host       The host to which the PDU will be sent
     * @param port       The port where the SNMP server will be
     * @param typeSocket The type of socket to use.
     *
     * @see SnmpContextv3#SnmpContextv3(String, int, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    public SnmpContextv3Pool(String host, int port, String typeSocket)
            throws java.io.IOException {
        this(host, port, null, typeSocket);
    }

    /**
     * Constructor.
     * Parameter typeSocket should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the PDU will be sent
     * @param port        The port where the SNMP server will be
     * @param bindAddress The local address the server will bind to
     * @param typeSocket  The type of socket to use.
     *
     * @see SnmpContextv3#SnmpContextv3(String, int, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     *
     * @since 4_14
     */
    public SnmpContextv3Pool(String host, int port, String bindAddress, String typeSocket)
            throws java.io.IOException {
        initPools();
        hostname = host;
        hostPort = port;
        bindAddr = bindAddress;
        socketType = typeSocket;

        // No point in creating a context, a lot of the parameters
        // are probably going to be set.
        // context = getMatchingContext();
    }

    private static synchronized void initPools() {
        if (contextPool == null) {
            contextPool = new Hashtable(5);
        }
    }

    /**
     * Returns the SNMP version of the context.
     *
     * @return The version
     */
    public int getVersion() {
        return SnmpConstants.SNMP_VERSION_3;
    }

    /**
     * Returns the host.
     *
     * @return The host
     */
    public String getHost() {
        return hostname;
    }

    /**
     * Returns the port number.
     *
     * @return The port no
     */
    public int getPort() {
        return hostPort;
    }

    public String getBindAddress() {
        return bindAddr;
    }

    /**
     * Returns the type of socket.
     *
     * @return The type of socket
     */
    public String getTypeSocket() {
        return socketType;
    }

    public String getSendToHostAddress() {
        String res = null;
        if (context != null) {
            res = context.getSendToHostAddress();
        }
        return res;
    }

    public String getReceivedFromHostAddress() {
        String res = null;
        if (context != null) {
            res = context.getReceivedFromHostAddress();
        }
        return res;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String newUserName) {
        if (newUserName != null && newUserName.equals(userName) == false) {
            userName = newUserName;
            hasChanged = true;
        }
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean newUseAuthentication) {
        if (newUseAuthentication != useAuthentication) {
            useAuthentication = newUseAuthentication;
            hasChanged = true;
        }
    }

    public String getUserAuthenticationPassword() {
        return userAuthenticationPassword;
    }

    public void setUserAuthenticationPassword(String newUserAuthenticationPd) {
        if (newUserAuthenticationPd != null
                &&
                newUserAuthenticationPd.equals(userAuthenticationPassword) == false) {
            userAuthenticationPassword = newUserAuthenticationPd;
            hasChanged = true;
        }
    }

    public void setPrivacyProtocol(int protocol) throws IllegalArgumentException {
        if (protocol == AES_ENCRYPT || protocol == DES_ENCRYPT) {
            if (protocol != privacyProtocol) {
                privacyProtocol = protocol;
                hasChanged = true;
            }
        } else {
            hasChanged = false;
            throw new IllegalArgumentException("Privacy Protocol "
                    + "should be DES or AES");
        }
    }

    public void setAuthenticationProtocol(int protocol)
            throws IllegalArgumentException {
        if (AUTH_PROTOCOLS.contains(protocol)) {
            if (protocol != authenticationProtocol) {
                authenticationProtocol = protocol;
                hasChanged = true;
            }
        } else {
            hasChanged = false;
            throw new IllegalArgumentException("Authentication Protocol "
                    + "should be MD5 or SHA1 or SHA256 or SHA384 or SHA512");
        }
    }

    public int getPrivacyProtocol() {
        return privacyProtocol;
    }

    public int getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    public boolean isUsePrivacy() {
        return usePrivacy;
    }

    public void setUsePrivacy(boolean newUsePrivacy) {
        if (newUsePrivacy != usePrivacy) {
            usePrivacy = newUsePrivacy;
            hasChanged = true;
        }
    }

    public String getUserPrivacyPassword() {
        return userPrivacyPassword;
    }

    public void setUserPrivacyPassword(String newUserPrivacyPd) {
        if (newUserPrivacyPd != null
                &&
                newUserPrivacyPd.equals(userPrivacyPassword) == false) {
            userPrivacyPassword = newUserPrivacyPd;
            hasChanged = true;
        }
    }

    public void setContextEngineId(byte[] newContextEngineId)
            throws IllegalArgumentException {
        if (newContextEngineId != null) {
            if (newContextEngineId.equals(contextEngineId) == false) {
                contextEngineId = newContextEngineId;
                hasChanged = true;
            }
        } else {
            hasChanged = false;
            throw new IllegalArgumentException("contextEngineId is null");
        }
    }

    public byte[] getContextEngineId() {
        return contextEngineId;
    }

    public void setContextName(String newContextName) {
        if (newContextName != null
                &&
                newContextName.equals(contextName) == false) {
            contextName = newContextName;
            hasChanged = true;
        }
    }

    public String getContextName() {
        return contextName;
    }

    public void setUsmAgent(UsmAgent newAgent) {
        if (newAgent != null
                &&
                newAgent != usmAgent) {
            usmAgent = newAgent;
            hasChanged = true;
        }
    }

    public UsmAgent getUsmAgent() {
        return usmAgent;
    }

    public boolean addDiscoveryPdu(DiscoveryPdu pdu)
            throws java.io.IOException, PduException, IllegalArgumentException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        return context.addDiscoveryPdu(pdu);
    }

    public boolean addPdu(Pdu pdu)
            throws java.io.IOException, PduException, IllegalArgumentException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        return context.addPdu(pdu);
    }

    public boolean removePdu(int requestId) {
        boolean res = false;
        if (context != null) {
            res = context.removePdu(requestId);
        }
        return res;
    }

    public byte[] encodeDiscoveryPacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, Object obj)
            throws java.io.IOException, EncodingException {
        byte[] res = null;
        if (context != null) {
            res = context.encodeDiscoveryPacket(msg_type, rId, errstat, errind, ve, obj);
        }
        return res;
    }

    public byte[] encodePacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, Object obj)
            throws java.io.IOException, EncodingException {
        byte[] res = null;
        if (context != null) {
            res = context.encodePacket(msg_type, rId, errstat, errind, ve,
                    obj);
        }
        return res;
    }

    public void sendPacket(byte[] packet) {
        if (context != null) {
            context.sendPacket(packet);
        }
    }

    /**
     * Releases the resources held by this context. This method will
     * decrement the reference counter. When the reference counter reaches
     * zero the actual context is removed from the pool and destroyed.
     */
    public void destroy() {
        synchronized (contextPool) {
            if (context != null) {
                String hashKey = context.getHashKey();

                int count = 0;
                SnmpContextPoolItem item = (SnmpContextPoolItem) contextPool.get(hashKey);
                if (item != null) {
                    count = item.getCounter();
                    count--;
                    item.setCounter(count);
                }

                if (count <= 0) {
                    contextPool.remove(hashKey);
                    context.destroy();
                }
                context = null;
            }
        }
    }

    /**
     * Destroys all the contexts in the pool and empties the pool.
     *
     * @see #destroy()
     * @since 4_14
     */
    public void destroyPool() {
        Hashtable copyOfPool = null;

        synchronized (contextPool) {
            synchronized (contextPool) {
                copyOfPool = (Hashtable) contextPool.clone();
            }
            contextPool.clear();
        }
        context = null;
        hasChanged = true;

        Enumeration keys = copyOfPool.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            SnmpContextPoolItem item = (SnmpContextPoolItem) copyOfPool.get(key);
            if (item != null) {
                SnmpContextBasisFace cntxt = (SnmpContextBasisFace) item.getContext();
                cntxt.destroy();
            }
        }
        copyOfPool.clear();
    }

    public boolean isDestroyed() {
        boolean isDestroyed = true;
        if (context != null) {
            isDestroyed = context.isDestroyed();
        }
        return isDestroyed;
    }

    /**
     * Returns a context from the pool.
     * The pre-existing context (if there is any) is destroyed.
     * This methods checks for an existing context that matches all our
     * properties. If such a context does not exist, a new one is created and
     * added to the pool.
     *
     * @return A context from the pool
     * @see #getHashKey
     */
    protected SnmpContextv3 getMatchingContext()
            throws java.io.IOException, IllegalArgumentException {
        SnmpContextPoolItem item = null;
        SnmpContextv3 newContext = null;
        String hashKey = getHashKey();

        destroy();
        synchronized (contextPool) {
            int count = 0;
            if (contextPool.containsKey(hashKey)) {
                item = (SnmpContextPoolItem) contextPool.get(hashKey);
                newContext = (SnmpContextv3) item.getContext();
                count = item.getCounter();
            } else {
                newContext = new SnmpContextv3(hostname, hostPort, bindAddr, socketType);
                newContext.setContextEngineId(contextEngineId);
                newContext.setContextName(contextName);
                newContext.setUserName(userName);
                newContext.setUseAuthentication(useAuthentication);
                newContext.setUserAuthenticationPassword(userAuthenticationPassword);
                newContext.setAuthenticationProtocol(authenticationProtocol);
                newContext.setUsePrivacy(usePrivacy);
                newContext.setUserPrivacyPassword(userPrivacyPassword);
                newContext.setUsmAgent(usmAgent);
                newContext.setPrivacyProtocol(privacyProtocol);

                item = new SnmpContextPoolItem(newContext);
                contextPool.put(hashKey, item);
            }
            hasChanged = false;
            count++;
            item.setCounter(count);
        }
        return newContext;
    }

    /**
     * Dumps the pool of contexts. This is for debug purposes.
     * 
     * @param title The title of the dump
     */
    public void dumpContexts(String title) {
        try {
            if (hasChanged == true) {
                context = getMatchingContext();
            }
        } catch (java.io.IOException exc) {
            if (AsnObject.debug > 0) {
                System.out.println(getClass().getName() + ".dumpContexts(): " + exc.getMessage());
            }
        }

        System.out.println(title + " " + contextPool.size() + " context(s)");
        Enumeration keys = contextPool.keys();
        int i = 0;
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            SnmpContextPoolItem item = (SnmpContextPoolItem) contextPool.get(key);
            if (item != null) {
                int count = item.getCounter();
                SnmpContextv3 cntxt = (SnmpContextv3) item.getContext();

                if (cntxt == context) {
                    System.out.println("\tcurrent context: ");
                }
                System.out.println("\tcontext " + i + ": " + key + ", count: " + count
                        + ", " + cntxt.toString() + "\n"
                        + ", " + cntxt.getDebugString());
                i++;
            }
        }
        System.out.println("\thasChanged: " + hasChanged);
    }

    /**
     * Returns the hash key. This key is built out of all properties. It
     * serves as key for the hashtable of (v3) contexts.
     *
     * @return The hash key
     */
    public String getHashKey() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(hostname);
        buffer.append("_").append(hostPort);
        buffer.append("_").append(bindAddr);
        buffer.append("_").append(socketType);
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

    public void addTrapListener(TrapListener l) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.addTrapListener(l);
    }

    public void removeTrapListener(TrapListener l) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.removeTrapListener(l);
    }

    public void addTrapListener(TrapListener l, int port) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.addTrapListener(l, port);
    }

    public void removeTrapListener(TrapListener l, int port) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.removeTrapListener(l, port);
    }

    public void addTrapListener(TrapListener l, ListeningContextPool lcontext) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.addTrapListener(l, lcontext);
    }

    public void removeTrapListener(TrapListener l, ListeningContextPool lcontext) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.removeTrapListener(l, lcontext);
    }

    public void addRequestPduListener(RequestPduListener l) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.addRequestPduListener(l);
    }

    public void removeRequestPduListener(RequestPduListener l) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.removeRequestPduListener(l);
    }

    public void addRequestPduListener(RequestPduListener l, int port) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.addRequestPduListener(l, port);
    }

    public void removeRequestPduListener(RequestPduListener l, int port) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.removeRequestPduListener(l, port);
    }

    public void addRequestPduListener(RequestPduListener l, ListeningContextPool lcontext) throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.addRequestPduListener(l, lcontext);
    }

    public void removeRequestPduListener(RequestPduListener l, ListeningContextPool lcontext)
            throws java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }
        context.removeRequestPduListener(l, lcontext);
    }

    public Pdu processIncomingPdu(byte[] message)
            throws DecodingException, java.io.IOException {
        if (hasChanged == true || context == null) {
            context = getMatchingContext();
        }

        Pdu pdu = null;
        pdu = context.processIncomingPdu(message);
        return pdu;
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return The string
     */
    public String toString() {
        String res = "";
        try {
            if (hasChanged == true || context == null) {
                context = getMatchingContext();
            }
            res = context.toString();
        } catch (java.io.IOException exc) {
            if (AsnObject.debug > 0) {
                System.out.println(getClass().getName() + ".toString(): " + exc.getMessage());
            }
        }

        return res;
    }

    /**
     * This method is not supported. It will throw a CloneNotSupportedException.
     *
     * @since 4_14
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

}
