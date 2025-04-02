// NAME
//      $RCSfile: TCPSocket.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.8 $
// CREATED
//      $Date: 2009/03/05 15:56:19 $
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
import java.net.*;
import uk.co.westhawk.snmp.stack.*;

/**
 * This is a wrapper class around the standard Socket. This sends
 * packets over TCP.  
 *
 * <p>
 * This is part of what is called "Reliable SNMP". SNMP usually goes
 * over UDP. That is why there is a retry mechanisme in SNMP. Sending
 * SNMP over TCP will only work is your agent listens on TCP.
 * See
 * <a href="http://www.ietf.org/rfc/rfc3430.txt">RFC 3430</a>
 * </p>
 *
 * <p>
 * When listening for incoming packets, the Socket, that is created when
 * accepting an incoming connection, is closed at the end.
 * Because of this, it is NOT possible to send a response back over the
 * same connection.
 * </p>
 *
 * @see Socket
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.8 $ $Date: 2009/03/05 15:56:19 $
 */
public class TCPSocket implements ContextSocketFace {
    static final String version_id = "@(#)$Id: TCPSocket.java,v 1.8 2009/03/05 15:56:19 birgita Exp $ Copyright Westhawk Ltd";

    private ServerSocket serverSoc = null;
    private Socket clientSoc = null;
    private InetAddress sendToHostAddr;
    private int sendToHostPort;
    private InetAddress receiveFromHostAddr;
    private int receiveFromHostPort;
    private InetAddress locBindAddr;

    public TCPSocket() {
    }

    public void create(int port, String bindAddr) throws IOException {
        sendToHostPort = port;
        receiveFromHostPort = sendToHostPort; // initialise (once!)
        try {
            locBindAddr = null;
            if (bindAddr != null) {
                locBindAddr = InetAddress.getByName(bindAddr);
            }
            serverSoc = new ServerSocket(sendToHostPort, 50, locBindAddr);
        } catch (SocketException exc) {
            String str = "Socket problem: port=" + port + ", bindAddr="
                    + bindAddr + " " + exc.getMessage();
            throw (new IOException(str));
        }
    }

    public void create(String host, int port, String bindAddr) throws IOException {
        sendToHostPort = port;
        receiveFromHostPort = sendToHostPort; // initialise (once!)
        try {
            sendToHostAddr = InetAddress.getByName(host);
            receiveFromHostAddr = sendToHostAddr; // initialise (once!)
            locBindAddr = null;
            if (bindAddr != null) {
                locBindAddr = InetAddress.getByName(bindAddr);
            }
            clientSoc = new Socket(sendToHostAddr, sendToHostPort, locBindAddr, 0);
        } catch (SocketException exc) {
            String str = "Socket problem: host=" + host + ", port=" + port
                    + ", bindAddr=" + bindAddr + " " + exc.getMessage();
            throw (new IOException(str));
        } catch (UnknownHostException exc) {
            String str = "Cannot find host " + host + " " + exc.getMessage();
            throw (new IOException(str));
        }
    }

    public String getReceivedFromHostAddress() {
        String res = null;
        if (receiveFromHostAddr != null) {
            res = receiveFromHostAddr.getHostAddress();
        }
        return res;
    }

    public String getSendToHostAddress() {
        String res = null;
        if (sendToHostAddr != null) {
            res = sendToHostAddr.getHostAddress();
        }
        return res;
    }

    public String getLocalSocketAddress() {
        String res = null;
        if (serverSoc != null) {
            SocketAddress sa = serverSoc.getLocalSocketAddress();
            if (sa != null) {
                res = sa.toString();
            }
        } else if (clientSoc != null) {
            SocketAddress sa = clientSoc.getLocalSocketAddress();
            if (sa != null) {
                res = sa.toString();
            }
        }
        return res;
    }

    public String getRemoteSocketAddress() {
        String res = null;
        if (clientSoc != null) {
            SocketAddress sa = clientSoc.getRemoteSocketAddress();
            if (sa != null) {
                res = sa.toString();
            }
        } else if (serverSoc != null) {
            //
        }
        return res;
    }

    public StreamPortItem receive(int maxRecvSize) throws IOException {
        StreamPortItem item = null;
        if (serverSoc != null) {
            byte[] data = new byte[maxRecvSize];

            // timeout will throw an exception every 1000 secs whilst idle
            // it is caught and ignored, but as a side effect it loops,
            // checking 'me'
            serverSoc.setSoTimeout(1000);

            Socket newSocket = serverSoc.accept();

            // copy newSocketIn into in
            InputStream newSocketIn = newSocket.getInputStream();
            newSocketIn.read(data, 0, data.length);

            receiveFromHostAddr = newSocket.getInetAddress();
            receiveFromHostPort = newSocket.getPort();

            ByteArrayInputStream in = null;
            in = new ByteArrayInputStream(data, 0, data.length);
            item = new StreamPortItem(receiveFromHostAddr.getHostAddress(),
                    receiveFromHostPort, in);

            newSocketIn.close();
            newSocket.close();

            newSocketIn = null;
            newSocket = null;
        } else if (clientSoc != null) {
            byte[] data = new byte[maxRecvSize];

            // timeout will throw an exception every 1000 secs whilst idle
            // it is caught and ignored, but as a side effect it loops,
            // checking 'me'
            clientSoc.setSoTimeout(1000);

            InputStream cin = clientSoc.getInputStream();
            cin.read(data, 0, data.length);

            receiveFromHostAddr = clientSoc.getInetAddress();
            receiveFromHostPort = clientSoc.getPort();

            ByteArrayInputStream in = null;
            in = new ByteArrayInputStream(data, 0, data.length);
            item = new StreamPortItem(receiveFromHostAddr.getHostAddress(),
                    receiveFromHostPort, in);
        }
        return item;
    }

    public void send(byte[] packet) throws IOException {
        if (clientSoc != null) {
            OutputStream out = clientSoc.getOutputStream();
            out.write(packet);
            out.flush();
        }
    }

    public void close() {
        try {
            if (clientSoc != null) {
                clientSoc.close();
            }
        } catch (IOException exc) {
        }

        try {
            if (serverSoc != null) {
                serverSoc.close();
            }
        } catch (IOException exc) {
        }

        serverSoc = null;
        clientSoc = null;
    }

}
