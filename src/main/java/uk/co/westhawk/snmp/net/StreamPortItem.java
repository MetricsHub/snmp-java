// NAME
//      $RCSfile: StreamPortItem.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.4 $
// CREATED
//      $Date: 2006/02/09 14:14:50 $
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

/**
 * This is a holder class that associates the incoming packet stream
 * with the remote port it came from.
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.4 $ $Date: 2006/02/09 14:14:50 $
 */
public class StreamPortItem {
    static final String version_id = "@(#)$Id: StreamPortItem.java,v 1.4 2006/02/09 14:14:50 birgit Exp $ Copyright Westhawk Ltd";

    private String hostAddress;
    private int port;
    private ByteArrayInputStream stream;

    /**
     * Constructor.
     *
     * @param address The host address
     * @param newPort The remote port number
     * @param in      The incoming message
     */
    public StreamPortItem(String address, int newPort, ByteArrayInputStream in) {
        hostAddress = address;
        port = newPort;
        stream = in;
    }

    /**
     * Returns the host addres where the message came from.
     *
     * @return The host address
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Returns the remote port where the message came from.
     *
     * @return The remote port number
     */
    public int getHostPort() {
        return port;
    }

    /**
     * Returns incoming message (or a copy of it).
     *
     * @return The message
     */
    public ByteArrayInputStream getStream() {
        return stream;
    }

    /**
     * Returns the string representation.
     *
     * @return The string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append("[");
        buffer.append("hostAddress=").append(hostAddress);
        buffer.append(", hostPort=").append(port);
        buffer.append(", #bytes=").append(stream.available());
        buffer.append("]");
        return buffer.toString();
    }

}
