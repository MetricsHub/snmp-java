// NAME
//      $RCSfile: RawPduEvent.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.5 $
// CREATED
//      $Date: 2006/02/09 14:30:18 $
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
package uk.co.westhawk.snmp.event;

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

import uk.co.westhawk.snmp.stack.*;


/**
 * The RawPduEvent class. This class is delivered when a undecoded pdu 
 * is received.
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.5 $ $Date: 2006/02/09 14:30:18 $
 */
public class RawPduEvent extends java.util.EventObject {
    private static final String version_id = "@(#)$Id: RawPduEvent.java,v 1.5 2006/02/09 14:30:18 birgit Exp $ Copyright Westhawk Ltd";

    protected boolean consumed = false;

    private int version;
    private String hostAddress;
    private int hostPort = -1;
    private byte[] message;

    /**
     * The constructor for an undecoded pdu event. The ListeningContext
     * class will fire undecoded pdu events.
     *
     * @param source The source (ListeningContext) of the event
     * @param v      The SNMP version of the pdu
     * @param hn     The IP address of the host where the pdu came from
     * @param prt    The remote port number of the host where the pdu came from
     * @param mess   The pdu in bytes
     *
     * @see #getVersion()
     * @see #getHostAddress()
     * @see #getHostPort()
     * @see #getMessage()
     */
    public RawPduEvent(Object source, int v, String hn, byte[] mess, int prt) {
        super(source);
        version = v;
        hostAddress = hn;
        message = mess;
        hostPort = prt;
    }

    /**
     * The SNMP version number of the pdu.
     *
     * @return The version number.
     * @see #getHostAddress()
     * @see #getMessage()
     * @see SnmpConstants#SNMP_VERSION_1
     * @see SnmpConstants#SNMP_VERSION_2c
     * @see SnmpConstants#SNMP_VERSION_3
     */
    public int getVersion() {
        return version;
    }

    /**
     * The IP address of the host where the pdu came from.
     * Note, this is not necessarily the same as the IpAddress field in the
     * SNMPv1 Pdu.
     *
     * @return The IP address of the host or null.
     * @see #getVersion()
     * @see #getMessage()
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * The pdu SNMP message in bytes. This is a copy of the original
     * message.
     *
     * @return The pdu in bytes.
     * @see #getVersion()
     * @see #getHostAddress()
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * The remote port number of the host where the pdu came from.
     *
     * @return The remote port number of the host or -1.
     * @see #getVersion()
     * @see #getMessage()
     *
     */
    public int getHostPort() {
        return hostPort;
    }

    /**
     * Consumes this event so that it will not be sent to any other
     * listeners.
     */
    public void consume() {
        consumed = true;
    }

    /**
     * Returns whether or not this event has been consumed.
     * 
     * @see #consume
     */
    public boolean isConsumed() {
        return consumed;
    }

}
