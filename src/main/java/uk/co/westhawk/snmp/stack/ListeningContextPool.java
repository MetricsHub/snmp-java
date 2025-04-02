// NAME
//      $RCSfile: ListeningContextPool.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.7 $
// CREATED
//      $Date: 2009/03/05 13:27:41 $
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
 * This class contains the pool of listening contexts. The usage of this
 * class will prevent more than one ListeningContext trying to listen to
 * the same port.
 *
 * @see ListeningContext
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.7 $ $Date: 2009/03/05 13:27:41 $
 */
public class ListeningContextPool implements ListeningContextFace {
    private static final String version_id = "@(#)$Id: ListeningContextPool.java,v 3.7 2009/03/05 13:27:41 birgita Exp $ Copyright Westhawk Ltd";

    protected static Hashtable contextPool;

    protected ListeningContext context = null;
    protected String socketType;
    protected String bindAddr;
    protected int hostPort;

    /**
     * Constructor, using the Standard socket type.
     *
     * @param port The local port where packets are received
     *
     * @see #ListeningContextPool(int, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     */
    public ListeningContextPool(int port) {
        this(port, null, SnmpContextBasisFace.STANDARD_SOCKET);
    }

    /**
     * Constructor, using the Standard socket type.
     *
     * If bindAddress is null, it will accept connections on
     * any/all local addresses. If you want to listen to
     * <ul>
     * <li>
     * IPv4 only interfaces, use address "0.0.0.0"
     * </li>
     * <li>
     * IPv6 only interfaces, use address "::"
     * </li>
     * </ul>
     *
     * @param port        The local port where packets are received
     * @param bindAddress The local address the server will bind to
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     */
    public ListeningContextPool(int port, String bindAddress) {
        this(port, bindAddress, SnmpContextBasisFace.STANDARD_SOCKET);
    }

    /**
     * Constructor.
     *
     * If bindAddress is null, it will accept connections on
     * any/all local addresses. If you want to listen to
     * <ul>
     * <li>
     * IPv4 only interfaces, use address "0.0.0.0"
     * </li>
     * <li>
     * IPv6 only interfaces, use address "::"
     * </li>
     * </ul>
     *
     * The typeSocket will indicate which type of socket to use. This way
     * different handlers can be provided.
     *
     * <p>
     * Note, the TCP_SOCKET does not provide functionality to send a
     * response back. Listening on such a socket is only useful when
     * listening for traps.
     * </p>
     *
     * @param port        The local port where packets are received
     * @param bindAddress The local address the server will bind to
     * @param typeSocket  The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    public ListeningContextPool(int port, String bindAddress, String typeSocket) {
        initPools();
        hostPort = port;
        bindAddr = bindAddress;
        socketType = typeSocket;

        context = getMatchingContext();
    }

    private static synchronized void initPools() {
        if (contextPool == null) {
            contextPool = new Hashtable(5);
        }
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

    public int getMaxRecvSize() {
        int res = SnmpContextBasisFace.MSS;
        if (context != null) {
            res = context.getMaxRecvSize();
        }
        return res;
    }

    /**
     * Sets the maximum number of bytes this context will read from the
     * socket. By default this will be set to <code>MSS</code> (i.e. 1300).
     * Only the current context will be affected, <em>not</em> to all the
     * contexts in the pool.
     *
     * @param no The new size
     *
     * @see SnmpContextBasisFace#MSS
     * @see AbstractSnmpContext#getMaxRecvSize()
     */
    public void setMaxRecvSize(int no) {
        if (context == null) {
            context = getMatchingContext();
        }
        context.setMaxRecvSize(no);
    }

    /**
     * Destroys the current context.
     *
     * <p>
     * Note that by calling this method the whole stack will stop listening
     * for packets on the port this context was listening on! The listeners
     * added via the SnmpContext classes are affected as well.
     * </p>
     *
     * @see #destroyPool()
     * @see ListeningContextPool#destroy()
     */
    public void destroy() {
        synchronized (contextPool) {
            if (context != null) {
                String hashKey = context.getHashKey();

                int count = 0;
                Item item = (Item) contextPool.get(hashKey);

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
     * <p>
     * Note that by calling this method the whole stack will stop listening
     * for any packets! The listeners added via the
     * SnmpContext classes are affected as well.
     * </p>
     *
     * @see #destroy()
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
            Item item = (Item) copyOfPool.get(key);
            if (item != null) {
                ListeningContext cntxt = (ListeningContext) item.getContext();
                cntxt.destroy();
            }
        }
        copyOfPool.clear();
    }

    /**
     * Returns a context from the pool.
     * This methods checks for an existing context that matches all our
     * properties. If such a context does not exist, a new one is created and
     * added to the pool.
     *
     * @return A context from the pool
     * @see #getHashKey
     */
    protected ListeningContext getMatchingContext() {
        Item item = null;
        ListeningContext newContext = null;
        String hashKey = getHashKey();

        destroy();
        synchronized (contextPool) {
            int count = 0;
            if (contextPool.containsKey(hashKey)) {
                item = (Item) contextPool.get(hashKey);
                newContext = item.getContext();
                count = item.getCounter();
            } else {
                newContext = new ListeningContext(hostPort, bindAddr, socketType);
                item = new Item(newContext);
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
        Hashtable copyOfPool = null;
        synchronized (contextPool) {
            copyOfPool = (Hashtable) contextPool.clone();
        }

        System.out.println(title + " " + copyOfPool.size());
        Enumeration keys = copyOfPool.keys();
        int i = 0;
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Item item = (Item) copyOfPool.get(key);

            if (item != null) {
                int count = item.getCounter();
                ListeningContext cntxt = item.getContext();

                System.out.println("\tcontext: " + key + ", count: " + count
                        + ", index: " + i + ", " + cntxt.toString());
                if (cntxt == context) {
                    System.out.println("\t\tcurrent context");
                }
                i++;
            }
        }
    }

    /**
     * Returns the hash key. This key is built out of all properties. It
     * serves as key for the pool of contexts.
     *
     * @return The hash key
     */
    public String getHashKey() {
        String str = hostPort
                + "_" + bindAddr
                + "_" + socketType;
        return str;
    }

    public void addRawPduListener(RawPduListener l)
            throws java.io.IOException {
        if (context != null) {
            context.addRawPduListener(l);
        }
    }

    public void removeRawPduListener(RawPduListener l) {
        if (context != null) {
            context.removeRawPduListener(l);
        }
    }

    /**
     * Removes the specified PDU listener from all the contexts in the pool.
     *
     * @see ListeningContext#removeRawPduListener
     */
    public void removeRawPduListenerFromPool(RawPduListener l) {
        Hashtable copyOfPool = null;

        if (contextPool != null) {
            synchronized (contextPool) {
                copyOfPool = (Hashtable) contextPool.clone();
            }

            Enumeration keys = copyOfPool.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                Item item = (Item) copyOfPool.get(key);

                if (item != null) {
                    ListeningContext cntxt = item.getContext();
                    cntxt.removeRawPduListener(l);
                }
            }
        }
    }

    public void addUnhandledRawPduListener(RawPduListener l)
            throws java.io.IOException {
        if (context != null) {
            context.addUnhandledRawPduListener(l);
        }
    }

    public void removeUnhandledRawPduListener(RawPduListener l) {
        if (context != null) {
            context.removeUnhandledRawPduListener(l);
        }
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

    class Item {
        private ListeningContext context = null;
        private int counter = 0;

        /**
         * Constructor.
         *
         * @param con The context
         */
        Item(ListeningContext con) {
            context = con;
            counter = 0;
        }

        ListeningContext getContext() {
            return context;
        }

        int getCounter() {
            return counter;
        }

        void setCounter(int i) {
            counter = i;
        }

        /**
         * Returns a string representation of the object.
         * 
         * @return The string
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer("Item[");
            buffer.append("context=").append(context.toString());
            buffer.append(", counter=").append(counter);
            buffer.append("]");
            return buffer.toString();
        }
    } // end Item

} // end ListeningContextPool
