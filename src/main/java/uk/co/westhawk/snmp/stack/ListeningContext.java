// NAME
//      $RCSfile: ListeningContext.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.12 $
// CREATED
//      $Date: 2009/03/05 13:24:00 $
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

import java.io.*;
import java.util.*;

import uk.co.westhawk.snmp.event.*;
import uk.co.westhawk.snmp.net.*;
import uk.co.westhawk.snmp.util.*;


/**
 * The ListeningContext class will enable this stack to receive packets.
 * This class replaces the deprecated DefaultTrapContext class.
 * The context will only start receiving (or listen for) packets when there is
 * at least one listener registered. 
 *
 * <p>
 * Two kind of listeners can be added; 
 * the normal and unhandled PDU listeners.
 * The normal PDU listeners are added via the
 * <code>addRawPduListener()</code> method, 
 * the unhandled PDU listeners are added via the 
 * <code>addUnhandledRawPduListener()</code>.
 * Both these listeners provide undecoded events.
 * </p>
 *
 * <p>
 * The SnmpContext classes provide functionality for decoded PDU and
 * trap events. These classes will register themselves via the
 * <code>addRawPduListener()</code> to the ListeningContext object and 
 * only pass the (decoded) event on if it matches their configuration.
 * </p>
 *
 * <p>
 * On UNIX and Linux operating systems the default port where PDUs and 
 * traps are sent (i.e. <em>161</em> and <em>162</em>) can only be opened 
 * as root.
 * </p>
 *
 * <p>
 * Only one process can listen on a certain port. To prevent more than
 * one ListeningContext listening on the same port, use the
 * ListeningContextPool class.
 * </p>
 *
 * @see ListeningContextPool
 * @see AbstractSnmpContext#addTrapListener
 * @see AbstractSnmpContext#addRequestPduListener
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.12 $ $Date: 2009/03/05 13:24:00 $
 */
public class ListeningContext implements ListeningContextFace, Runnable {
    private static final String version_id = "@(#)$Id: ListeningContext.java,v 3.12 2009/03/05 13:24:00 birgita Exp $ Copyright Westhawk Ltd";

    private Object soc_lock = new Object();
    // thanks to Nick Sheen nsheen@tippingpoint.com for pointing out that "" is
    // interned by the VM
    private static int counter;
    private ContextSocketFace soc;
    private Thread me;
    private String basename;
    private volatile boolean stopRequested;
    // thanks to Nick Sheen nsheen@tippingpoint.com for pointing out that volatile
    // is needed here

    protected int maxRecvSize;
    protected String typeSocket;
    protected int hostPort;
    protected String bindAddr;

    transient private RawPduReceivedSupport pduSupport, unhandledSupport;

    /**
     * Constructor, using the Standard socket type.
     *
     * @param port The local port where packets are received
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     */
    public ListeningContext(int port) {
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
    public ListeningContext(int port, String bindAddress) {
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
     *
     * The typeSocketA will indicate which type of socket to use. This way
     * different handlers can be provided.
     * This parameter should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * <p>
     * Note, the TCP_SOCKET does not provide functionality to send a
     * response back. Listening on such a socket is only useful when
     * listening for traps.
     * </p>
     *
     * @param port        The local port where packets are received
     * @param bindAddress The local address the server will bind to
     * @param typeSocketA The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    public ListeningContext(int port, String bindAddress, String typeSocketA) {
        hostPort = port;
        bindAddr = bindAddress;
        typeSocket = typeSocketA;

        basename = "" + hostPort + "_" + bindAddr;

        pduSupport = new RawPduReceivedSupport(this);
        unhandledSupport = new RawPduReceivedSupport(this);
        maxRecvSize = SnmpContextBasisFace.MSS;
    }

    public int getPort() {
        return hostPort;
    }

    public String getBindAddress() {
        return bindAddr;
    }

    public String getTypeSocket() {
        return typeSocket;
    }

    public int getMaxRecvSize() {
        return maxRecvSize;
    }

    public void setMaxRecvSize(int no) {
        maxRecvSize = no;
    }

    /**
     * This method will stop the thread listening for packets.
     * All transmitters, PDUs in flight and traplisteners will be removed
     * when run() finishes.
     *
     * <p>
     * It closes the socket.
     * The thread will actually stop/finish when the run() finishes. Since
     * the socket is closed, the run() will fall through almost instantly.
     * </p>
     *
     * <p>
     * Note that by calling this method the whole stack will stop listening
     * for packets on this particular port! The listeners added via the
     * SnmpContext classes are affected as well.
     * </p>
     *
     * <p>
     * When you add a new listener, the context will start listening again.
     * </p>
     *
     * <p>
     * Note: The thread(s) will not die immediately; this will take about
     * half a minute.
     * </p>
     */
    public void destroy() {
        synchronized (soc_lock) {
            stopRequested = true;
            if (soc != null) {
                if (AsnObject.debug > 12) {
                    System.out.println(getClass().getName() + ".destroy(): Closing socket ");
                }
                soc.close();
            }
        }
    }

    /**
     * We wait for any incoming PDUs and fire a rawpdu received (undecoded) event
     * if we do.
     * 
     * <p>
     * The undecoded events are fired to all normal listeners (added via
     * addRawPduListener()), until one of them consumes it.
     * The SnmpContext classes will consume the event if it matches their
     * configuration.
     * </p>
     *
     * <p>
     * If none of them consume the event, the undecoded events are fired to
     * all unhandled PDU listeners (added via addUnhandledRawPduListener()),
     * until one of them consumes it.
     * </p>
     *
     * @see RawPduReceivedSupport#fireRawPduReceived
     * @see #addRawPduListener(RawPduListener)
     * @see #addUnhandledRawPduListener(RawPduListener)
     */
    public void run() {
        // while It is visible
        while (!stopRequested) {
            // block for incoming packets
            me.yield();
            try {
                if (stopRequested) {
                    break;
                }

                StreamPortItem item = soc.receive(maxRecvSize);
                ByteArrayInputStream in = item.getStream();

                String hostAddress = item.getHostAddress();
                int port = item.getHostPort();

                // read the bytes of the input stream into bu
                int nb = in.available();
                byte[] bu = new byte[nb];
                in.read(bu);
                in.close();

                if (AsnObject.debug > 10) {
                    SnmpUtilities.dumpBytes(getClass().getName()
                            + ".run(): Received from "
                            + hostAddress
                            + ", from port " + port
                            + ": ", bu);
                }
                KickProcessIncomingMessage thread = new KickProcessIncomingMessage(hostAddress, port, bu);
                thread.start();
            } catch (IOException exc) {
                if (exc instanceof InterruptedIOException) {
                    if (AsnObject.debug > 15) {
                        System.out.println(getClass().getName() + ".run(): Idle recv " + exc.getMessage());
                    }
                } else {
                    if (AsnObject.debug > 0) {
                        System.out.println(getClass().getName() + ".run(): IOException: " + exc.getMessage());
                    }
                }
            } catch (Exception exc) {
                if (AsnObject.debug > 0) {
                    System.out.println(getClass().getName() + ".run(): Exception: " + exc.getMessage());
                    exc.printStackTrace();
                }
            } catch (Error err) {
                if (AsnObject.debug > 0) {
                    System.out.println(getClass().getName() + ".run(): Error: " + err.getMessage());
                    err.printStackTrace();
                }
            }
        }

        me = null;
        soc = null;
        pduSupport.empty();
        unhandledSupport.empty();
    }

    public void addRawPduListener(RawPduListener listener)
            throws IOException {
        synchronized (soc_lock) {
            pduSupport.addRawPduListener(listener);
            startListening();
        }
    }

    public void removeRawPduListener(RawPduListener listener) {
        synchronized (soc_lock) {
            pduSupport.removeRawPduListener(listener);
            destroyIfNoListeners();
        }
    }

    public void addUnhandledRawPduListener(RawPduListener listener)
            throws IOException {
        synchronized (soc_lock) {
            unhandledSupport.addRawPduListener(listener);
            startListening();
        }
    }

    public void removeUnhandledRawPduListener(RawPduListener listener) {
        synchronized (soc_lock) {
            unhandledSupport.removeRawPduListener(listener);
            destroyIfNoListeners();
        }
    }

    /**
     * Creates the socket and starts listening for PDUs if we didn't do so
     * already.
     * This method is called in addRawPduListener() and
     * addUnhandledRawPduListener().
     *
     * @exception IOException Thrown when the socket cannot be created.
     * @see #addRawPduListener
     * @see #addUnhandledRawPduListener
     */
    private void startListening()
            throws IOException {
        if (soc == null) {
            // create tempSoc first, so that when 'create' fails, soc
            // will remain null.
            ContextSocketFace tempSoc = AbstractSnmpContext.getSocket(typeSocket);
            if (tempSoc != null) {
                tempSoc.create(hostPort, bindAddr);
                soc = tempSoc;

                if (AsnObject.debug > 12) {
                    System.out.println(getClass().getName() + ".startListening()"
                            + ": soc.getLocalSocketAddress() = " + soc.getLocalSocketAddress());
                    System.out.println(getClass().getName() + ".startListening()"
                            + ": soc.getRemoteSocketAddress() = " + soc.getRemoteSocketAddress());
                }
            }
        }
        if (me == null) {
            stopRequested = false;
            me = new Thread(this, basename + "_Listen");
            me.setPriority(me.NORM_PRIORITY);
            me.start();
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
                + "_" + typeSocket;
        return str;
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return The string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("ListeningContext[");
        buffer.append("port=").append(hostPort);
        buffer.append(", bindAddress=").append(bindAddr);
        buffer.append(", socketType=").append(typeSocket);
        buffer.append(", #rawPduListeners=").append(pduSupport.getListenerCount());
        buffer.append(", #rawPduUnhandledListeners=").append(unhandledSupport.getListenerCount());
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Processes an incoming packet.
     *
     * @see #run
     */
    protected void processIncomingMessage(String hostAddress,
            int port, byte[] bu) throws DecodingException, IOException {
        AsnDecoderBase rpdu = new AsnDecoderBase();
        ByteArrayInputStream in = new ByteArrayInputStream(bu);
        AsnSequence asnTopSeq = rpdu.getAsnSequence(in);
        int version = rpdu.getSNMPVersion(asnTopSeq);

        boolean isConsumed = pduSupport.fireRawPduReceived(version, hostAddress, port, bu);
        if (isConsumed == false) {
            unhandledSupport.fireRawPduReceived(version, hostAddress, port, bu);
        }
    }

    /**
     * Only destroy this object when there are no more listeners.
     *
     * Thanks to Jeremy Stone (Jeremy.Stone@cyclone-technology.com).
     * 
     * @since 6.1
     */
    private void destroyIfNoListeners() {
        if (pduSupport.getListenerCount() == 0
                && unhandledSupport.getListenerCount() == 0) {
            destroy();
        }
    }

    class KickProcessIncomingMessage extends Thread {
        /**
         * This class makes sure that dealing with an incoming packet is
         * done at a separate thread so the ListeningContext can go back
         * listening immediately.
         * This will at some point be replaced by a Thread pool of
         * some kind.
         */
        private String hostAddress;
        private int port;
        private byte[] bu;

        KickProcessIncomingMessage(String newHostAddress, int newPort,
                byte[] newBu) {
            hostAddress = newHostAddress;
            port = newPort;
            bu = newBu;
            this.setPriority(Thread.MIN_PRIORITY);
            this.setName(newHostAddress + "_" + newPort
                    + "_KickProcessIncomingMessage_" + counter);
            counter++;
        }

        public void run() {
            try {
                processIncomingMessage(hostAddress, port, bu);
            } catch (IOException exc) {
                if (AsnObject.debug > 0) {
                    System.out.println(getClass().getName() + ".run(): IOException: " + exc.getMessage());
                }
            } catch (DecodingException exc) {
                if (AsnObject.debug > 1) {
                    System.out.println(getClass().getName() + ".run(): DecodingException: " + exc.getMessage());
                }
            }
        }
    }

}
