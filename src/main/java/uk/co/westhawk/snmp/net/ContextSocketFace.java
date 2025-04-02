// NAME
//      $RCSfile: ContextSocketFace.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.12 $
// CREATED
//      $Date: 2006/02/09 14:30:19 $
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

package uk.co.westhawk.snmp.net;

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

/**
 * The interface for the different type of sockets.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 1.12 $ $Date: 2006/02/09 14:30:19 $
 */
public interface ContextSocketFace {
    static final String version_id = "@(#)$Id: ContextSocketFace.java,v 1.12 2006/02/09 14:30:19 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Creates the socket. This socket is used when listening for incoming
     * requests or traps.
     * Use only one (1) of the two create methods.
     *
     * @param port     The local port number were we receive (listen for)
     *                 packets
     * @param bindAddr The local address the server will bind to when
     *                 listening
     *
     * @see #create(String, int, String)
     */
    public void create(int port, String bindAddr) throws IOException;

    /**
     * Creates the socket. This socket is used to send out our requests. Use
     * only one (1) of the two create methods.
     *
     * @param host     The name of the host that is to receive our packets
     * @param port     The port number of the host
     * @param bindAddr The local address the server will bind to when
     *                 sending packets
     *
     * @see #create(int, String)
     */
    public void create(String host, int port, String bindAddr) throws IOException;

    /**
     * Returns the IP address of the host we sent the packet to.
     * Only applicable when sending requests, not when (only) listening for
     * incoming requests or traps.
     *
     * @return The IP address, or null when the hostname cannot be determined
     * @see #getReceivedFromHostAddress
     */
    public String getSendToHostAddress();

    /**
     * Returns the IP address of the (latest) host of the packet we received.
     * Only applicable when anything has been received.
     * When sending a request, this will in most cases be the same host
     * where we sent the original packet to.
     *
     * @return The IP address, or null when the hostname cannot be determined
     * @see #getSendToHostAddress
     */
    public String getReceivedFromHostAddress();

    /**
     * Returns the address of the endpoint this socket is bound to, or null
     * if it is not bound yet.
     *
     * @since 4_14
     */
    public String getLocalSocketAddress();

    /**
     * Returns the address of the endpoint this socket is connected to, or
     * null if it is unconnected.
     *
     * @since 4_14
     */
    public String getRemoteSocketAddress();

    /**
     * Receives a packet from this socket. The StreamPortItem object
     * contains the host and port the packet came from.
     *
     * @param maxRecvSize the maximum number of bytes to receive
     * @return the packet
     */
    public StreamPortItem receive(int maxRecvSize) throws IOException;

    /**
     * Sends a packet from this socket.
     * It can throw an "java.io.IOException: Invalid argument" when the
     * bind address is incorrect for some reason.
     *
     * @param packet the packet
     */
    public void send(byte[] packet) throws IOException;

    /**
     * Closes this socket.
     */
    public void close();

}
