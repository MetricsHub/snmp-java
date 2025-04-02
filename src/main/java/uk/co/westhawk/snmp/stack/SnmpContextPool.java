// NAME
//      $RCSfile: SnmpContextPool.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.22 $
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

import java.util.*;
import uk.co.westhawk.snmp.event.*;

/**
 * This class contains the pool of SNMP v1 contexts.
 * This class reuses the existings contexts instead of creating a new
 * one every time.
 * <p>
 * Every time a property changes the pool is checked for a SnmpContext
 * context that matches all the new properties of this class. If no such
 * context exists, a new one is made.
 * The PDUs associated with the old context remain associated with the
 * old context. 
 * </p>
 *
 * <p>
 * A counter indicates the number of times the context is referenced.
 * The counter is decreased when <code>destroy()</code> is called. 
 * When the counter reaches zero, the context is released.
 * </p>
 *
 * <p>
 * Note that because the underlying context can change when a property
 * is changed and the PDUs remain associated with the old context, all 
 * properties have to be set BEFORE a PDU is sent.
 * </p>
 *
 * <p>
 * Thanks to Seon Lee (slee@virtc.com) for reporting thread safety
 * problems.
 * </p>
 *
 * @see SnmpContext
 * @see SnmpContextv2cPool
 * @see SnmpContextv3Pool
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.22 $ $Date: 2009/03/05 13:27:41 $
 */
public class SnmpContextPool implements SnmpContextFace {
    private static final String version_id = "@(#)$Id: SnmpContextPool.java,v 3.22 2009/03/05 13:27:41 birgita Exp $ Copyright Westhawk Ltd";

    protected static Hashtable contextPool;

    protected SnmpContext context = null;
    protected String hostname, socketType, bindAddr;
    protected int hostPort;
    protected String community = SnmpContextFace.DEFAULT_COMMUNITY;

    /**
     * Constructor, using the Standard socket.
     *
     * @param host The host to which the PDU will be sent
     * @param port The port where the SNMP server will be
     * @see SnmpContext#SnmpContext(String, int)
     */
    public SnmpContextPool(String host, int port) throws java.io.IOException {
        this(host, port, SnmpContextFace.DEFAULT_COMMUNITY, null, STANDARD_SOCKET);
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
     * @see SnmpContext#SnmpContext(String, int, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    public SnmpContextPool(String host, int port, String typeSocket)
            throws java.io.IOException {
        this(host, port, SnmpContextFace.DEFAULT_COMMUNITY, null, typeSocket);
    }

    /**
     * Constructor.
     * Parameter typeSocket should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @since 4_12
     *
     * @param host       The host to which the PDU will be sent
     * @param port       The port where the SNMP server will be
     * @param comm       The community name.
     * @param typeSocket The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    public SnmpContextPool(String host, int port, String comm, String typeSocket)
            throws java.io.IOException {
        this(host, port, comm, null, typeSocket);
    }

    /**
     * Constructor.
     * Parameter typeSocket should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the PDU will be sent
     * @param port        The port where the SNMP server will be
     * @param comm        The community name.
     * @param bindAddress The local address the server will bind to
     * @param typeSocket  The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     * @since 4_14
     */
    public SnmpContextPool(String host, int port, String comm, String bindAddress, String typeSocket)
            throws java.io.IOException {
        initPools();
        hostname = host;
        hostPort = port;
        community = comm;
        bindAddr = bindAddress;
        socketType = typeSocket;

        context = getMatchingContext();
    }

    private static synchronized void initPools() {
        if (contextPool == null) {
            contextPool = new Hashtable(5);
        }
    }

    public int getVersion() {
        return SnmpConstants.SNMP_VERSION_1;
    }

    public String getHost() {
        return hostname;
    }

    public int getPort() {
        return hostPort;
    }

    public String getBindAddress() {
        return bindAddr;
    }

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

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String newCommunity) {
        if (newCommunity != null
                &&
                newCommunity.equals(community) == false) {
            community = newCommunity;
            try {
                context = getMatchingContext();
            } catch (java.io.IOException exc) {
            }
        }
    }

    public boolean addPdu(Pdu pdu)
            throws java.io.IOException, PduException {
        if (context == null) {
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

    /**
     * Encodes a PDU packet.
     */
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
     * Destroys all the contexts (v1 and v2c) in the pool and empties the pool.
     * The underlying implementation uses the same hashtable for both the v1
     * and the v2c contexts.
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
    protected SnmpContext getMatchingContext() throws java.io.IOException {
        SnmpContextPoolItem item = null;
        SnmpContext newContext = null;
        String hashKey = getHashKey();

        destroy();
        synchronized (contextPool) {
            int count = 0;
            if (contextPool.containsKey(hashKey)) {
                item = (SnmpContextPoolItem) contextPool.get(hashKey);
                newContext = (SnmpContext) item.getContext();
                count = item.getCounter();
            } else {
                newContext = new SnmpContext(hostname, hostPort, bindAddr, socketType);
                newContext.setCommunity(community);
                item = new SnmpContextPoolItem(newContext);
                contextPool.put(hashKey, item);
            }
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
        System.out.println(title + " " + contextPool.size() + " context(s)");
        Enumeration keys = contextPool.keys();
        int i = 0;
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            SnmpContextPoolItem item = (SnmpContextPoolItem) contextPool.get(key);
            if (item != null) {
                int count = item.getCounter();
                SnmpContext cntxt = (SnmpContext) item.getContext();

                if (cntxt == context) {
                    System.out.println("\tcurrent context: ");
                }
                System.out.println("\tcontext " + i + ": " + key + ", count: " + count
                        + ", " + cntxt.toString() + "\n"
                        + ", " + cntxt.getDebugString());
                i++;
            }
        }
    }

    /**
     * Returns the hash key. This key is built out of all properties. It
     * serves as key for the hashtable of (v1) contexts.
     *
     * @return The hash key
     */
    public String getHashKey() {
        String str = hostname
                + "_" + hostPort
                + "_" + bindAddr
                + "_" + socketType
                + "_" + community
                + "_v" + getVersion();
        return str;
    }

    /**
     * Adds the specified trap listener. The listener will be added to the
     * current context, <em>not</em> to all the contexts in the hashtable.
     *
     * @see SnmpContext#addTrapListener
     */
    public void addTrapListener(TrapListener l) throws java.io.IOException {
        if (context != null) {
            context.addTrapListener(l);
        }
    }

    /**
     * Removes the specified trap listener. The listener will be removed
     * from the current context, <em>not</em> from all the contexts in the
     * hashtable.
     *
     * @see SnmpContext#removeTrapListener
     */
    public void removeTrapListener(TrapListener l) throws java.io.IOException {
        if (context != null) {
            context.removeTrapListener(l);
        }
    }

    public void addTrapListener(TrapListener l, int port) throws java.io.IOException {
        if (context != null) {
            context.addTrapListener(l, port);
        }
    }

    public void removeTrapListener(TrapListener l, int port) throws java.io.IOException {
        if (context != null) {
            context.removeTrapListener(l, port);
        }
    }

    public void addTrapListener(TrapListener l, ListeningContextPool lcontext) throws java.io.IOException {
        if (context != null) {
            context.addTrapListener(l, lcontext);
        }
    }

    public void removeTrapListener(TrapListener l, ListeningContextPool lcontext) throws java.io.IOException {
        if (context != null) {
            context.removeTrapListener(l, lcontext);
        }
    }

    public void addRequestPduListener(RequestPduListener l) throws java.io.IOException {
        if (context != null) {
            context.addRequestPduListener(l);
        }
    }

    public void removeRequestPduListener(RequestPduListener l) throws java.io.IOException {
        if (context != null) {
            context.removeRequestPduListener(l);
        }
    }

    public void addRequestPduListener(RequestPduListener l, int port) throws java.io.IOException {
        if (context != null) {
            context.addRequestPduListener(l, port);
        }
    }

    public void removeRequestPduListener(RequestPduListener l, int port) throws java.io.IOException {
        if (context != null) {
            context.removeRequestPduListener(l, port);
        }
    }

    public void addRequestPduListener(RequestPduListener l, ListeningContextPool lcontext) throws java.io.IOException {
        if (context != null) {
            context.addRequestPduListener(l, lcontext);
        }
    }

    public void removeRequestPduListener(RequestPduListener l, ListeningContextPool lcontext)
            throws java.io.IOException {
        if (context != null) {
            context.removeRequestPduListener(l, lcontext);
        }
    }

    /**
     * Processes the incoming PDU with the current context.
     *
     * @see SnmpContext#processIncomingPdu
     */
    public Pdu processIncomingPdu(byte[] message)
            throws DecodingException, java.io.IOException {
        Pdu pdu = null;
        if (context != null) {
            pdu = context.processIncomingPdu(message);
        }
        return pdu;
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return The string
     */
    public String toString() {
        String res = "";
        if (context != null) {
            res = context.toString();
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
