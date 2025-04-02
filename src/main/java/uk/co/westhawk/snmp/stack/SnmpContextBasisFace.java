// NAME
//      $RCSfile: SnmpContextBasisFace.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.9 $
// CREATED
//      $Date: 2006/11/29 16:25:19 $
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
import uk.co.westhawk.snmp.event.*;
import uk.co.westhawk.snmp.net.*;

/**
 * This interface contains the SNMP context interface that is needed 
 * by every PDU to send a SNMP v1, v2c and v3 request. The context also
 * provides functionality to receive incoming PDUs.
 *
 * @see SnmpContext
 * @see SnmpContextv2c
 * @see SnmpContextv3
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.9 $ $Date: 2006/11/29 16:25:19 $
 */
public interface SnmpContextBasisFace {
    static final String version_id = "@(#)$Id: SnmpContextBasisFace.java,v 3.9 2006/11/29 16:25:19 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * The default port number where SNMP requests are sent to (161).
     */
    public final static int DEFAULT_PORT = 161;
    /**
     * The Standard Socket type.
     */
    public final static String STANDARD_SOCKET = "Standard";
    /**
     * The TCP Socket type.
     */
    public final static String TCP_SOCKET = "TCP";

    /**
     * The Maximum number of outstanding PDUs one context can handle at a
     * given moment in time.
     */
    final static int MAXPDU = 20; // if you have more than 20 oustanding PDUS
                                  // change the algorythm :-)
    /**
     * The Maximum size of a message in octets (1300).
     */
    final static int MSS = 1300; // maximum recv size;

    /**
     * Returns the SNMP version of the context.
     *
     * @see SnmpConstants#SNMP_VERSION_1
     * @see SnmpConstants#SNMP_VERSION_2c
     * @see SnmpConstants#SNMP_VERSION_3
     *
     * @return The version
     */
    public int getVersion();

    /**
     * Returns the host.
     *
     * @return The host
     */
    public String getHost();

    /**
     * Returns the port number.
     *
     * @return The port no
     */
    public int getPort();

    /**
     * Returns the local bind address.
     * If bindAddress is null, then the system will pick up a valid local
     * address to bind the socket.
     *
     * @return The local bind address
     * @since 4_14
     */
    public String getBindAddress();

    /**
     * Returns the type of socket.
     *
     * @see #STANDARD_SOCKET
     * @see #TCP_SOCKET
     * @return The type of socket
     */
    public String getTypeSocket();

    /**
     * Returns the IP address string
     * aaa.bbb.ccc.ddd (IPv4) or a:b:c:d:e:f:g:h (IPv6)
     * of the host the packets where sent to.
     *
     * @return The IP address of the host the packets where sent to.
     * @see ContextSocketFace#getSendToHostAddress
     * @since 4_14
     */
    public String getSendToHostAddress();

    /**
     * Returns the IP address string
     * aaa.bbb.ccc.ddd (IPv4) or a:b:c:d:e:f:g:h (IPv6)
     * of the (latest) host the packets where received from.
     *
     * @return The IP address of the (latest) host the packets where received from.
     * @see ContextSocketFace#getReceivedFromHostAddress
     * @since 4_14
     */
    public String getReceivedFromHostAddress();

    /**
     * Adds a PDU to the context. This is for internal use only and should
     * NOT be called by the developer.
     * This is called by the the Pdu itself and is added to the interface to
     * cover the different kind of Contexts.
     *
     * @param pdu the PDU
     * @return whether the PDU has been successfully added
     */
    public boolean addPdu(Pdu pdu)
            throws java.io.IOException, PduException;

    /**
     * Removes a PDU from the context. This is for internal use only and should
     * NOT be called by the developer.
     * This is called by the the PDU itself and is added to the interface to
     * cover the different kind of Contexts.
     *
     * @param requestId the PDU request id
     * @return whether the PDU has been successfully removed
     */
    public boolean removePdu(int requestId);

    /**
     * Encodes a PDU. This is for internal use only and should
     * NOT be called by the developer.
     * This is called by the the PDU itself and is added to the interface to
     * cover the different kind of Contexts.
     *
     * @param msg_type The message type
     * @param rId      The message id
     * @param errstat  The error status
     * @param errind   The error index
     * @param ve       The varbind list
     * @param obj      Additional object (only used in SNMPv3)
     * @return The encoded packet
     */
    public byte[] encodePacket(byte msg_type, int rId, int errstat, int errind,
            java.util.Enumeration ve, Object obj)
            throws java.io.IOException, EncodingException;

    /**
     * Sends an encoded PDU. This is for internal use only and should
     * NOT be called by the developer.
     * This is called by the the PDU itself and is added to the interface to
     * cover the different kind of Contexts.
     *
     * @param packet The encoded packet
     */
    public void sendPacket(byte[] packet);

    /**
     * Removes the resouces held by this context. Should be called by the
     * user/developer when the context is no longer needed.
     */
    public void destroy();

    /**
     * Returns whether or not this context has been destroyed.
     * 
     * @since 4_14
     */
    public boolean isDestroyed();

    /**
     * Adds the specified trap listener to receive traps on the default trap
     * port <em>162</em> from the host that matches this context.
     *
     * <p>
     * The ListeningContext class will do the actual listening for traps.
     * This context will add itself to a ListeningContextPool object and
     * will only pass the event to its listeners if the pdu matches this
     * context and is a trap pdu.
     * </p>
     *
     * @param l The trap listener
     * @see #addTrapListener(TrapListener, int)
     * @see ListeningContextFace#DEFAULT_TRAP_PORT
     */
    public void addTrapListener(TrapListener l) throws java.io.IOException;

    /**
     * Removes the specified trap listener from listening for packets on
     * the default trap port <em>162</em>.
     *
     * <p>
     * The listener will not be removed from all ListeningContext objects
     * that are in the ListeningContextPool. In order to do that, use
     * ListeningContextPool.removeTrapListenerFromPool()
     * </p>
     *
     * @param l The trap listener
     * @see #removeTrapListener(TrapListener, int)
     * @see ListeningContextFace#DEFAULT_TRAP_PORT
     */
    public void removeTrapListener(TrapListener l) throws java.io.IOException;

    /**
     * Adds the specified trap listener to receive traps on the specified
     * port from the host that matches this context.
     *
     * <p>
     * The ListeningContext class will do the actual listening for traps.
     * This context will add itself to a ListeningContextPool object and
     * will only pass the event to its listeners if the pdu matches this
     * context and is a trap pdu.
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#addRawPduListener(RawPduListener)
     *
     * @param l    The trap listener
     * @param port The port the traps are received on
     * @since 4_14
     */
    public void addTrapListener(TrapListener l, int port) throws java.io.IOException;

    /**
     * Removes the specified trap listener from listening for packets on the
     * specified port.
     *
     * <p>
     * The listener will not be removed from all ListeningContext objects
     * that are in the ListeningContextPool. In order to do that, use
     * ListeningContextPool.removeTrapListenerFromPool()
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#removeRawPduListener(RawPduListener)
     * @see ListeningContextPool#removeRawPduListenerFromPool(RawPduListener)
     *
     * @param l    The trap listener
     * @param port The port the traps are received on
     * @since 4_14
     */
    public void removeTrapListener(TrapListener l, int port) throws java.io.IOException;

    /**
     * Adds the specified trap listener to receive traps on the specified
     * listening context that matches this context.
     *
     * <p>
     * The ListeningContext class will do the actual listening for traps.
     * This context will add itself to a ListeningContextPool object and
     * will only pass the event to its listeners if the pdu matches this
     * context and is a trap pdu.
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#addRawPduListener(RawPduListener)
     *
     * @param l        The trap listener
     * @param lcontext The listening context
     * @since 4_14
     */
    public void addTrapListener(TrapListener l, ListeningContextPool lcontext) throws java.io.IOException;

    /**
     * Removes the specified trap listener from listening for packets on the
     * specified listening context.
     *
     * <p>
     * The listener will not be removed from all ListeningContext objects
     * that are in the ListeningContextPool. In order to do that, use
     * ListeningContextPool.removeTrapListenerFromPool()
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#removeRawPduListener(RawPduListener)
     * @see ListeningContextPool#removeRawPduListenerFromPool(RawPduListener)
     *
     * @param l        The trap listener
     * @param lcontext The listening context
     * @since 4_14
     */
    public void removeTrapListener(TrapListener l, ListeningContextPool lcontext) throws java.io.IOException;

    /**
     * Adds the specified request pdu listener to receive PDUs on the
     * default request pdu port <em>161</em> from the host that matches
     * this context.
     *
     * <p>
     * The ListeningContext class will do the actual listening for PDUs.
     * This context will add itself to a ListeningContextPool object and
     * will only pass the event to its listeners if the pdu matches this
     * context and is a request pdu.
     * </p>
     *
     * <p>
     * Don't use the TCP_SOCKET when listening for request PDUs. It doesn't
     * provide functionality to send a response back.
     * </p>
     *
     * @see #addRequestPduListener(RequestPduListener, int)
     * @see SnmpContextBasisFace#DEFAULT_PORT
     *
     * @param l The request PDU listener
     * @since 4_14
     */
    public void addRequestPduListener(RequestPduListener l) throws java.io.IOException;

    /**
     * Removes the specified request pdu listener from listening for packets
     * on the default request pdu port <em>161</em>.
     *
     * <p>
     * The listener will not be removed from all ListeningContext objects
     * that are in the ListeningContextPool. In order to do that, use
     * ListeningContextPool.removeRequestPduListenerFromPool()
     * </p>
     *
     * @see #removeRequestPduListener(RequestPduListener, int)
     * @see SnmpContextBasisFace#DEFAULT_PORT
     *
     * @param l The request PDU listener
     * @since 4_14
     */
    public void removeRequestPduListener(RequestPduListener l) throws java.io.IOException;

    /**
     * Adds the specified request pdu listener to receive PDUs on the specified
     * port from the host that matches this context.
     *
     * <p>
     * The ListeningContext class will do the actual listening for PDUs.
     * This context will add itself to a ListeningContextPool object and
     * will only pass the event to its listeners if the pdu matches this
     * context and is a request pdu.
     * </p>
     *
     * <p>
     * Don't use the TCP_SOCKET when listening for request PDUs. It doesn't
     * provide functionality to send a response back.
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#addRawPduListener(RawPduListener)
     *
     * @param l    The request PDU listener
     * @param port The port the request PDUs are received on
     * @since 4_14
     */
    public void addRequestPduListener(RequestPduListener l, int port) throws java.io.IOException;

    /**
     * Removes the specified request pdu listener from listening for packets
     * on the specified port.
     *
     * <p>
     * The listener will not be removed from all ListeningContext objects
     * that are in the ListeningContextPool. In order to do that, use
     * ListeningContextPool.removeRequestPduListenerFromPool()
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#removeRawPduListener(RawPduListener)
     * @see ListeningContextPool#removeRawPduListenerFromPool(RawPduListener)
     *
     * @param l    The request PDU listener
     * @param port The port the request PDUs are received on
     * @since 4_14
     */
    public void removeRequestPduListener(RequestPduListener l, int port) throws java.io.IOException;

    /**
     * Adds the specified request pdu listener to receive PDUs on the
     * specified listening context that matches this context.
     *
     * <p>
     * The ListeningContext class will do the actual listening for PDUs.
     * This context will add itself to a ListeningContextPool object and
     * will only pass the event to its listeners if the pdu matches this
     * context and is a request pdu.
     * </p>
     *
     * <p>
     * Don't use the TCP_SOCKET when listening for request PDUs. It doesn't
     * provide functionality to send a response back.
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#addRawPduListener(RawPduListener)
     *
     * @param l        The request PDU listener
     * @param lcontext The listening context
     * @since 4_14
     */
    public void addRequestPduListener(RequestPduListener l, ListeningContextPool lcontext) throws java.io.IOException;

    /**
     * Removes the specified request pdu listener from listening for packets
     * on the specified listening context.
     *
     * <p>
     * The listener will not be removed from all ListeningContext objects
     * that are in the ListeningContextPool. In order to do that, use
     * ListeningContextPool.removeRequestPduListenerFromPool()
     * </p>
     *
     * @see ListeningContextPool#ListeningContextPool(int, String, String)
     * @see ListeningContextPool#removeRawPduListener(RawPduListener)
     * @see ListeningContextPool#removeRawPduListenerFromPool(RawPduListener)
     *
     * @param l        The request PDU listener
     * @param lcontext The listening context
     * @since 4_14
     */
    public void removeRequestPduListener(RequestPduListener l, ListeningContextPool lcontext)
            throws java.io.IOException;

    /**
     * Processes an incoming PDU. The context will try to process the
     * incoming PDU, using the SNMP version and other security
     * parameters. If any of these do not correspond, a DecodingException
     * will be thrown.
     */
    public Pdu processIncomingPdu(byte[] message)
            throws DecodingException, java.io.IOException;

    /**
     * Returns a clone of this SnmpContext.
     *
     * @since 4_14
     * @exception CloneNotSupportedException Thrown when the constructor
     *                                       generates an IOException or when in one
     *                                       of the Pool classes.
     */
    public Object clone() throws CloneNotSupportedException;

    /**
     * Returns the hash key. This key is built out of all properties.
     *
     * @since 4_14
     * @return The hash key
     */
    public String getHashKey();
}
