// NAME
//      $RCSfile: StandardSocket.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.13 $
// CREATED
//      $Date: 2009/03/05 15:56:19 $
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
import java.net.*;
import uk.co.westhawk.snmp.stack.*;

/**
 * This is a wrapper class around the standard DatagramSocket. This
 * sends packets over UDP, which is standard in SNMP.
 *
 * @see DatagramSocket
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 1.13 $ $Date: 2009/03/05 15:56:19 $
 */
public class StandardSocket implements ContextSocketFace {
    static final String version_id = "@(#)$Id: StandardSocket.java,v 1.13 2009/03/05 15:56:19 birgita Exp $ Copyright Westhawk Ltd";

    private DatagramSocket soc = null;
    private InetAddress sendToHostAddr;
    private int sendToHostPort;
    private InetAddress receiveFromHostAddr;
    private int receiveFromHostPort;
    private InetAddress locBindAddr = null;

    public StandardSocket() {
    }

    public void create(int port, String bindAddr) throws IOException {
        sendToHostPort = port;
        receiveFromHostPort = sendToHostPort; // initialise (once!)
        try {
            locBindAddr = null;
            if (bindAddr != null) {
                locBindAddr = InetAddress.getByName(bindAddr);
            }
            soc = new DatagramSocket(sendToHostPort, locBindAddr);
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
            InetSocketAddress isa = new InetSocketAddress(locBindAddr, 0);
            soc = new DatagramSocket(isa);
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
        if (soc != null) {
            SocketAddress sa = soc.getLocalSocketAddress();
            if (sa != null) {
                res = sa.toString();
            }
        }
        return res;
    }

    public String getRemoteSocketAddress() {
        String res = null;
        if (soc != null) {
            SocketAddress sa = soc.getRemoteSocketAddress();
            if (sa != null) {
                res = sa.toString();
            }
        }
        return res;
    }

    public StreamPortItem receive(int maxRecvSize) throws IOException {
        StreamPortItem item = null;
        if (soc != null) {
            byte[] data = new byte[maxRecvSize];
            DatagramPacket p = new DatagramPacket(data, maxRecvSize);

            // timeout will throw an exception every 1000 secs whilst idle
            // it is caught and ignored, but as a side effect it loops,
            // checking 'me'
            soc.setSoTimeout(1000);

            soc.receive(p);
            receiveFromHostAddr = p.getAddress();
            receiveFromHostPort = p.getPort();

            ByteArrayInputStream in = null;
            in = new ByteArrayInputStream(p.getData(), 0, p.getLength());
            item = new StreamPortItem(receiveFromHostAddr.getHostAddress(),
                    receiveFromHostPort, in);
            p = null;
        }
        return item;
    }

    public void send(byte[] packet) throws IOException {
        if (soc != null) {
            DatagramPacket pack = new DatagramPacket(packet, packet.length,
                    sendToHostAddr, sendToHostPort);
            soc.send(pack);
        }
    }

    public void close() {
        if (soc != null) {
            soc.close();
            soc = null;
        }
    }

}
