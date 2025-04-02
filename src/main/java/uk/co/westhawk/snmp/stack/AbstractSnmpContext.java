// NAME
//      $RCSfile: AbstractSnmpContext.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.33 $
// CREATED
//      $Date: 2009/03/05 12:48:04 $
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

import java.io.*;
import java.util.*;
import uk.co.westhawk.snmp.net.*;
import uk.co.westhawk.snmp.event.*;
import uk.co.westhawk.snmp.util.*;

/**
 * This class contains the abstract SNMP context that is needed by every
 * Pdu to send a SNMP v1, v2c or v3 request.
 * The context also provides functionality to receive PDUs.
 *
 * <p>
 * <code>destroy()</code> should be called when the context is no longer
 * used. This is the only way the threads will be stopped and garbage
 * collected.
 * </p>
 *
 * @see SnmpContext
 * @see SnmpContextv2c
 * @see SnmpContextv3
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.33 $ $Date: 2009/03/05 12:48:04 $
 */
public abstract class AbstractSnmpContext extends Object
        implements SnmpContextBasisFace, Runnable, RawPduListener {
    private static final String version_id = "@(#)$Id: AbstractSnmpContext.java,v 3.33 2009/03/05 12:48:04 birgita Exp $ Copyright Westhawk Ltd";

    private ContextSocketFace soc;
    private Transmitter[] transmitters;
    private Pdu[] pdus;
    private Thread me;
    private String basename;
    private volatile boolean stopRequested;
    // thanks to Nick Sheen nsheen@tippingpoint.com for pointing out that volatile
    // is needed here

    protected String typeSocket;
    protected String hostname;
    protected String bindAddr;
    protected int hostPort;
    protected int maxRecvSize;
    protected boolean isDestroyed;
    protected boolean anyPduExpectingResponse = false;
    protected RequestPduReceivedSupport pduSupport;
    protected TrapReceivedSupport trapSupport;

    /**
     * Processes an incoming response. Has to be overload by each context.
     * This is called in the run() method.
     *
     * @see #run
     */
    protected abstract void processIncomingResponse(ByteArrayInputStream in)
            throws DecodingException, IOException;

    /**
     * Encodes a PDU. This is for internal use only and should
     * NOT be called by the developer.
     * This is called by the the PDU itself and is added to the interface to
     * cover the different kind of Contexts.
     * Has to be overload by each context.
     *
     * @param msg_type The message type
     * @param rId      The message id
     * @param errstat  The error status
     * @param errind   The error index
     * @param ve       The varbind list
     * @param obj      Additional object (only used in SNMPv3)
     * @return The encoded packet
     */
    public abstract byte[] encodePacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, Object obj)
            throws IOException, EncodingException;

    /**
     * Processes an incoming pdu (but not a response). Has to be overload by each
     * context.
     * 
     * @see #rawPduReceived
     */
    public abstract Pdu processIncomingPdu(byte[] message) throws DecodingException, IOException;

    /**
     * Returns the SNMP version of this context. Has to be overload by each
     * context.
     */
    public abstract int getVersion();

    /**
     * Constructor.
     * The Standard socket type will be used.
     *
     * @param host The host to which the Pdu will be sent
     * @param port The port where the SNMP server will be
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     */
    protected AbstractSnmpContext(String host, int port) throws IOException {
        this(host, port, null, STANDARD_SOCKET);
    }

    /**
     * Constructor.
     * Parameter typeSocketA should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the Pdu will be sent
     * @param port        The port where the SNMP server will be
     * @param typeSocketA The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    protected AbstractSnmpContext(String host, int port, String typeSocketA)
            throws IOException {
        this(host, port, null, typeSocketA);
    }

    /**
     * Constructor.
     *
     * If bindAddress is null, then the system will pick up a valid local
     * address to bind the socket.
     *
     * The typeSocket will indicate which type of socket to use. This way
     * different handlers can be provided.
     * It should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the Pdu will be sent
     * @param port        The port where the SNMP server will be
     * @param bindAddress The local address the server will bind to
     * @param typeSocketA The type of socket to use.
     *
     * @exception IOException Thrown when the socket cannot be
     *                        created.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     * @since 4_14
     */
    protected AbstractSnmpContext(String host, int port, String bindAddress, String typeSocketA)
            throws IOException {
        pdus = new Pdu[MAXPDU];
        hostname = host;
        hostPort = port;
        bindAddr = bindAddress;
        typeSocket = typeSocketA;
        transmitters = new Transmitter[MAXPDU];
        basename = host + "_" + port + "_" + bindAddress;
        trapSupport = new TrapReceivedSupport(this);
        pduSupport = new RequestPduReceivedSupport(this);

        isDestroyed = false;
        stopRequested = false;
        maxRecvSize = MSS;

        soc = getSocket(typeSocket);
        if (soc != null) {
            soc.create(hostname, hostPort, bindAddr);
            if (AsnObject.debug > 12) {
                System.out.println(getClass().getName()
                        + ": soc.getLocalSocketAddress() = "
                        + soc.getLocalSocketAddress());
                System.out.println(getClass().getName()
                        + ": soc.getRemoteSocketAddress() = "
                        + soc.getRemoteSocketAddress());
            }
        }
    }

    /**
     * Returns a new socket, based on a particular type.
     * Parameter type is first compare do STANDARD_SOCKET and TCP_SOCKET. If
     * that doesn't match, type is assumed to be a fully qualified classname.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    static ContextSocketFace getSocket(String type) throws IOException {
        ContextSocketFace sf = null;
        if (type != null) {
            String className = null;
            if (type.equals(STANDARD_SOCKET)) {
                className = "uk.co.westhawk.snmp.net.StandardSocket";
            } else if (type.equals(TCP_SOCKET)) {
                className = "uk.co.westhawk.snmp.net.TCPSocket";
            } else {
                className = type;
            }

            try {
                Class cl = Class.forName(className);
                Object obj = cl.newInstance();
                sf = (ContextSocketFace) obj;
            } catch (ClassNotFoundException exc) {
                String str = "AbstractSnmpContext.getSocket(): ClassNotFound problem " + exc.getMessage() + ", type="
                        + type;
                throw (new IOException(str));
            } catch (InstantiationException exc) {
                String str = "AbstractSnmpContext.getSocket(): Instantiation problem " + exc.getMessage() + ", type="
                        + type;
                throw (new IOException(str));
            } catch (IllegalAccessException exc) {
                String str = "AbstractSnmpContext.getSocket(): IllegalAccess problem " + exc.getMessage() + ", type="
                        + type;
                throw (new IOException(str));
            } catch (ClassCastException exc) {
                String str = "AbstractSnmpContext.getSocket(): ClassCast problem " + exc.getMessage() + ", type="
                        + type;
                throw (new IOException(str));
            }

            if (sf == null) {
                String str = "AbstractSnmpContext.getSocket(): Cannot create socket " + type;
                throw (new IOException(str));
            } else {
                if (AsnObject.debug > 12) {
                    System.out.println("AbstractSnmpContext.getSocket(): New socket " + sf.getClass().getName());
                }
            }
        }
        return sf;
    }

    public String getHost() {
        return hostname;
    }

    /**
     * Returns the IP address string
     * aaa.bbb.ccc.ddd (IPv4) or a:b:c:d:e:f:g:h (IPv6)
     * of the host.
     *
     * @return The IP address of the host
     * @deprecated As of 4_14, use {@link #getSendToHostAddress()}
     */
    public String getHostAddress() {
        return getSendToHostAddress();
    }

    public String getSendToHostAddress() {
        String res = "";
        if (soc != null) {
            res = soc.getSendToHostAddress();
        }
        return res;
    }

    public String getReceivedFromHostAddress() {
        String res = "";
        if (soc != null) {
            res = soc.getReceivedFromHostAddress();
        }
        return res;
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

    /**
     * Returns the maximum number of bytes this context will read from the
     * socket. By default this will be set to <code>MSS</code> (i.e. 1300).
     *
     * @since 4_12
     * @see SnmpContextBasisFace#MSS
     * @see #setMaxRecvSize(int)
     * @return The number
     */
    public int getMaxRecvSize() {
        return maxRecvSize;
    }

    /**
     * Sets the maximum number of bytes this context will read from the
     * socket. By default this will be set to <code>MSS</code> (i.e. 1300).
     * The default size seems a reasonable size. The problem usually occurs
     * when sending Bulk requests.
     *
     * <p>
     * If a packet arrives that is bigger than the maximum size of received
     * bytes, the stack will try to decode it nevertheless. The usual
     * error that will occur is:
     * </p>
     * 
     * <pre>
     * Error message: "Incorrect packet. No of bytes received less than packet length."
     * </pre>
     *
     * <p>
     * Although UDP datagrams can be fragmented (fragmentation is part of
     * the network layer (IP), not the transport layer (UDP/TCP)), some
     * firewalls reject incoming fragments. Therefor it is best not to set
     * maxRecvSize higher than the largest packet size you can get through
     * your network topology.
     * </p>
     *
     * <p>
     * Thanks to Pete Kazmier (pete@kazmier.com) for the suggestion.
     * </p>
     *
     * <em style="color:red;">
     * Note, this property is NOT supported in any of the SNMPContextXXPool
     * classes.
     * </em>
     *
     * @since 4_12
     * @see SnmpContextBasisFace#MSS
     * @see #getMaxRecvSize()
     * @param no The new number
     */
    public void setMaxRecvSize(int no) {
        maxRecvSize = no;
    }

    /**
     * Returns the thread usage of the AbstractSnmpContext.
     * It returns a String in the form of <code>=PO=QR--------------0</code>.
     *
     * <p>
     * The String represents the array of transmitters.
     * Each character represents a transmitter slot.
     * The transmitters form a thread pool of a maximum size, MAXPDU.
     * Each transmitter is used to wait for one PDU response at a given
     * moment in time.
     * When the response is received the transmitter will stop running, but
     * is not destroyed. It will be reused.
     * </p>
     *
     * <p>
     * Meaning of each character:
     * </p>
     * <ul>
     * <li><code>-</code> a transmitter slot has not yet been allocated a
     * thread</li>
     * <li><code>=</code> there is a thread but it is idle</li>
     * <li><code>A->Z</code> the thread is transmitting a Pdu</li>
     * <li>
     * The last character represents the context's recv thread:
     * <ul>
     * <li><code>0</code> there isn't one</li>
     * <li><code>1</code> it exists but isn't running</li>
     * <li><code>2</code> it exists and is alive.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @since 4_12
     * @return The thread usage of the AbstractSnmpContext
     */
    public String getDebugString() {
        char[] cret = new char[MAXPDU + 1];
        for (int i = 0; i < MAXPDU; i++) {
            if (transmitters[i] != null) {
                if (pdus[i] != null) {
                    cret[i] = (char) ('A' + (pdus[i].getReqId() % 26));
                } else {
                    cret[i] = '=';
                }
            } else {
                cret[i] = '-';
            }
        }

        char res = '0';
        if (me != null) {
            res++;
            if (me.isAlive()) {
                res++;
            }
        }
        cret[MAXPDU] = res;

        return new String(cret);
    }

    /**
     * This method will stop the thread.
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
     * Note: The thread(s) will not die immediately; this will take about
     * half a minute.
     * </p>
     *
     * @see ListeningContext#destroy()
     * @see ListeningContextPool#destroyPool()
     */
    public synchronized void destroy() {
        if (isDestroyed == false) {
            stopRequested = true;
            if (AsnObject.debug > 12) {
                System.out.println(getClass().getName() + ".destroy(): Closing socket ");
            }
            soc.close();
            isDestroyed = true;

            // If run() has been started, then it will destroy the
            // transmitter threads when it finishes. Otherwise they must be
            // destroyed here.
            if (me == null) {
                freeTransmitters();
            }
        }
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * This method will stop the thread.
     * All transmitters, PDUs in flight and traplisteners will be removed
     * when run() finishes.
     * <p>
     * It does NOT close the socket.
     * The thread will actually stop/finish when the run() finishes. That is when
     * a packet arrives on the socket or when the socket times out.
     * </p>
     *
     * <p>
     * We have deprecated this method since there is no point in stopping
     * the context, but not destroying it. The context cannot start again anyway.
     * The difference between destroy() and stop() was not very clear.
     * </p>
     *
     * @deprecated As of version 4_12, should use {@link #destroy()}
     * @see #destroy()
     */
    public synchronized void stop() {
        stopRequested = true;
    }

    /**
     * We wait for any incoming packets. After receiving one, decode
     * the packet into an Pdu. The Pdu will notify the observers waiting
     * for an response.
     *
     * <p>
     * Thanks to Chris Barlock &lt;barlock@us.ibm.com&gt; who reported a
     * NullPointerException in run() on variable 'me' and introduced the
     * variable stopRequested.
     * </p>
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

                if (AsnObject.debug > 10) {
                    int nb = in.available();
                    byte[] bu = new byte[nb];
                    in.read(bu);
                    in.reset();

                    SnmpUtilities.dumpBytes(getClass().getName()
                            + ".run(): Received from "
                            + item.getHostAddress()
                            + ", from port " + item.getHostPort()
                            + ": ", bu);
                }
                processIncomingResponse(in);
            } catch (IOException exc) {
                if (exc instanceof InterruptedIOException) {
                    if (AsnObject.debug > 15) {
                        System.out.println(getClass().getName() + ".run(): Idle recv " + exc.getMessage());
                    }
                } else if (exc instanceof java.net.SocketException) {
                    if (AsnObject.debug > 15) {
                        System.out.println(getClass().getName() + ".run(): SocketException " + exc.getMessage());
                    }
                } else {
                    if (AsnObject.debug > 0) {
                        System.out.println(getClass().getName() + ".run(): "
                                + exc.getClass().getName() + " " + exc.getMessage());
                        exc.printStackTrace();
                    }
                }
            } catch (DecodingException exc) {
                if (AsnObject.debug > 1) {
                    System.out.println(getClass().getName() + ".run(): DecodingException: " + exc.getMessage());
                }
            } catch (Exception exc) {
                if (AsnObject.debug > 1) {
                    System.out.println(getClass().getName() + ".run(): Exception: " + exc.getMessage());
                    exc.printStackTrace();
                }
            } catch (Error err) {
                if (AsnObject.debug > 1) {
                    System.out.println(getClass().getName() + ".run(): Error: " + err.getMessage());
                    err.printStackTrace();
                }
            }
        }

        freeTransmitters();

        trapSupport.empty();
        pduSupport.empty();

        // This used to actually create a listener. I do think this bug
        // has been fixed, since no socket will be created in
        // ListeningContextPool, unless a listener has been added.
        ListeningContextPool lcontext = new ListeningContextPool(ListeningContextFace.DEFAULT_TRAP_PORT, bindAddr,
                typeSocket);
        lcontext.removeRawPduListenerFromPool(this);

        me = null;
        soc = null;
    }

    /*
     * By moving activate() from the constructor to here, the parameter
     * maxRecvSize, changed in setMaxRecvSize(), gets a chance to actually
     * change before run() starts.
     * Thanks to Dave Hunt <dave.hunt@csipros.com> who reported this
     * problem.
     */
    public synchronized void sendPacket(byte[] p) {
        if (isDestroyed == false) {
            activate();
            try {
                if (AsnObject.debug > 10) {
                    SnmpUtilities.dumpBytes("Sending to "
                            + soc.getSendToHostAddress() + ": ", p);
                }

                // Seen it throw an "java.io.IOException: Invalid argument"
                // when the bind address was wrong, i.e. the packet reach
                // the host over the interface
                soc.send(p);
            } catch (IOException exc) {
                if (AsnObject.debug > 0) {
                    System.out.println(getClass().getName() + ".sendPacket(): "
                            + exc.getClass().getName()
                            + " " + exc.getMessage());
                    exc.printStackTrace();
                }
            }
        }
    }

    Pdu getPdu(Integer ReqId) {
        return getPdu(ReqId.intValue());
    }

    Pdu getPdu(int rid) {
        Pdu ret = null;
        for (int i = 0; i < MAXPDU; i++) {
            if ((pdus[i] != null) && (pdus[i].getReqId() == rid)) {
                ret = pdus[i];
                break;
            }
        }
        return ret;
    }

    public synchronized boolean removePdu(int rid) {
        boolean ret = false;
        for (int i = 0; i < MAXPDU; i++) {
            if ((pdus[i] != null) && (pdus[i].getReqId() == rid)) {
                pdus[i] = null;
                ret = true;
                break;
            }
        }
        return ret;
    }

    public synchronized boolean addPdu(Pdu p)
            throws IOException, PduException {
        boolean done = false;
        if (isDestroyed == true) {
            throw new EncodingException("Context can no longer be used, since it is already destroyed");
        } else {
            // I only want to start the receive thread when any of the
            // context's PDUs is actually expecting a response. See activate().
            if (anyPduExpectingResponse == false) {
                anyPduExpectingResponse = p.isExpectingResponse();
            }
            for (int i = 0; i < MAXPDU; i++) {
                if (pdus[i] == null) {
                    pdus[i] = p;
                    pdus[i].setTrans(getTrans(i));
                    done = true;
                    break;
                }
            }
        }
        return done;
    }

    public void addTrapListener(TrapListener l) throws IOException {
        addTrapListener(l, ListeningContextFace.DEFAULT_TRAP_PORT);
    }

    public void removeTrapListener(TrapListener l) throws IOException {
        removeTrapListener(l, ListeningContextFace.DEFAULT_TRAP_PORT);
    }

    public void addTrapListener(TrapListener l, int port)
            throws IOException {
        ListeningContextPool lcontext = new ListeningContextPool(port, bindAddr, typeSocket);
        addTrapListener(l, lcontext);
    }

    public void removeTrapListener(TrapListener l, int port)
            throws IOException {
        ListeningContextPool lcontext = new ListeningContextPool(port, bindAddr, typeSocket);
        removeTrapListener(l, lcontext);
    }

    public void addTrapListener(TrapListener l, ListeningContextPool lcontext)
            throws IOException {
        trapSupport.addTrapListener(l);
        lcontext.addRawPduListener(this);
    }

    public void removeTrapListener(TrapListener l, ListeningContextPool lcontext)
            throws IOException {
        trapSupport.removeTrapListener(l);
        if (trapSupport.getListenerCount() == 0
                &&
                pduSupport.getListenerCount() == 0) {
            lcontext.removeRawPduListener(this);
        }
    }

    public void addRequestPduListener(RequestPduListener l)
            throws IOException {
        addRequestPduListener(l, SnmpContextBasisFace.DEFAULT_PORT);
    }

    public void removeRequestPduListener(RequestPduListener l)
            throws IOException {
        removeRequestPduListener(l, SnmpContextBasisFace.DEFAULT_PORT);
    }

    public void addRequestPduListener(RequestPduListener l, int port) throws IOException {
        ListeningContextPool lcontext = new ListeningContextPool(port, bindAddr, typeSocket);
        addRequestPduListener(l, lcontext);
    }

    public void removeRequestPduListener(RequestPduListener l, int port) throws IOException {
        ListeningContextPool lcontext = new ListeningContextPool(port, bindAddr, typeSocket);
        removeRequestPduListener(l, lcontext);
    }

    public void addRequestPduListener(RequestPduListener l, ListeningContextPool lcontext) throws IOException {
        pduSupport.addRequestPduListener(l);
        lcontext.addRawPduListener(this);
    }

    public void removeRequestPduListener(RequestPduListener l, ListeningContextPool lcontext) throws IOException {
        pduSupport.removeRequestPduListener(l);
        if (trapSupport.getListenerCount() == 0
                &&
                pduSupport.getListenerCount() == 0) {
            lcontext.removeRawPduListener(this);
        }
    }

    /**
     * Invoked when an undecoded pdu is received.
     * First the version and the hostaddress are checked, if correct
     * an attempt is made to decode the pdu.
     * When successful the original event is consumed and a decoded pdu event
     * is passed on the listeners.
     *
     * @see RawPduReceivedSupport#fireRawPduReceived
     * @see RequestPduReceivedSupport#fireRequestPduReceived
     * @see TrapReceivedSupport#fireTrapReceived
     */
    public void rawPduReceived(RawPduEvent evt) {
        String hostAddress = evt.getHostAddress();
        int version = evt.getVersion();
        if (version == this.getVersion()) {
            if (hostAddress != null && hostAddress.equals(this.getSendToHostAddress()) == true) {
                byte[] message = evt.getMessage();
                Pdu pdu = null;
                try {
                    pdu = processIncomingPdu(message);
                    if (pdu != null) {
                        evt.consume();
                        int port = evt.getHostPort();

                        if (pdu.getMsgType() == SnmpConstants.TRP_REQ_MSG
                                ||
                                pdu.getMsgType() == SnmpConstants.TRPV2_REQ_MSG) {
                            trapSupport.fireTrapReceived(pdu, port);
                        } else {
                            pduSupport.fireRequestPduReceived(pdu, port);
                        }
                    } else {
                        // somehow the context matches, but the pdu type is
                        // not recognised.
                    }
                } catch (DecodingException exc) {
                    if (AsnObject.debug > 2) {
                        System.out.println(
                                getClass().getName() + ".rawPduReceived(): DecodingException: " + exc.getMessage());
                    }
                } catch (IOException exc) {
                    if (AsnObject.debug > 0) {
                        System.out.println(getClass().getName() + ".rawPduReceived(): IOException " + exc.getMessage());
                    }
                }

            } else {
                if (AsnObject.debug > 5) {
                    System.out.println(getClass().getName() + ".rawPduReceived(): "
                            + "Pdu host (" + hostAddress
                            + "), does not correspond with context host ("
                            + this.getSendToHostAddress() + ")");
                }
            }
        } else {
            if (AsnObject.debug > 5) {
                String theirs = SnmpUtilities.getSnmpVersionString(version);
                String ours = SnmpUtilities.getSnmpVersionString(this.getVersion());
                System.out.println(getClass().getName() + ".rawPduReceived(): "
                        + "Pdu version " + theirs
                        + ", does not correspond with context version "
                        + ours);
            }
        }
    }

    Transmitter getTrans(int i) {
        if (transmitters[i] == null) {
            transmitters[i] = new Transmitter(basename + "_v" + getVersion() + "_Trans" + i);
        }
        return transmitters[i];
    }

    /**
     * Creates and starts the Receive thread that allows this context to
     * receive packets.
     * Subclasses may override this to adjust the threading behaviour.
     *
     * @see PassiveSnmpContext#activate()
     * @see PassiveSnmpContextv2c#activate()
     */
    protected void activate() {
        // Only start the thread when 'me' is null (i.e. no thread is running)
        // AND when anyPduExpectingResponse is true.
        // This way a context that only sends (for example) traps, will not
        // start a listing thread.
        if (me == null && anyPduExpectingResponse == true) {
            me = new Thread(this, basename + "_v" + getVersion() + "_Receive");
            me.setPriority(me.MAX_PRIORITY);
            me.start();
        }
    }

    /**
     * Frees the transmitters.
     *
     * @see #run()
     * @see #destroy()
     * @since 5_1
     */
    // In version 5_0, this code lived in run().
    // Thanks to Vincent Deconinck <vdeconinck@tiscalinet.be>
    protected void freeTransmitters() {
        for (int i = 0; i < MAXPDU; i++) {
            if (transmitters[i] != null) {
                transmitters[i].destroy();
                transmitters[i] = null;
            }
            if (pdus[i] != null) {
                pdus[i] = null;
            }
        }
    }

    /**
     * Returns a clone of this SnmpContext.
     *
     * @since 4_14
     * @exception CloneNotSupportedException Thrown when the constructor
     *                                       generates an IOException or when in one
     *                                       of the Pool classes.
     */
    public abstract Object clone() throws CloneNotSupportedException;

    /**
     * Returns the hash key. This key is built out of all properties.
     *
     * @since 4_14
     * @return The hash key
     */
    public abstract String getHashKey();

}
