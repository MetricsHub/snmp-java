// NAME
//      $RCSfile: SnmpContextv2c.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.16 $
// CREATED
//      $Date: 2009/03/05 12:54:04 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2001 - 2006 by Westhawk Ltd
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

import java.net.*;
import java.io.*;
import java.util.*;
import uk.co.westhawk.snmp.event.*;

/**
 * This class contains the SNMP v2c context that is needed by every PDU to
 * send a SNMP v2c request.
 *
 * <p>
 * <code>destroy()</code> should be called when the context is no longer
 * used. This is the only way the threads will be stopped and garbage
 * collected.
 * </p>
 *
 * @see SnmpContextv2cFace
 * @see SnmpContextv2cPool
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.16 $ $Date: 2009/03/05 12:54:04 $
 */
public class SnmpContextv2c extends SnmpContext
        implements SnmpContextv2cFace, Cloneable {
    private static final String version_id = "@(#)$Id: SnmpContextv2c.java,v 3.16 2009/03/05 12:54:04 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * Constructor.
     *
     * @param host The host to which the PDU will be sent
     * @param port The port where the SNMP server will be
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int)
     */
    public SnmpContextv2c(String host, int port) throws IOException {
        super(host, port);
    }

    /**
     * Constructor.
     * Parameter typeSocketA should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the Pdu will be sent
     * @param port        The port where the SNMP server will be
     * @param typeSocketA The local address the server will bind to
     *
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int, String)
     */
    public SnmpContextv2c(String host, int port, String typeSocketA)
            throws IOException {
        super(host, port, typeSocketA);
    }

    /**
     * Constructor.
     * Parameter typeSocketA should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the PDU will be sent
     * @param port        The port where the SNMP server will be
     * @param bindAddress The local address the server will bind to
     * @param typeSocketA The type of socket to use.
     *
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int, String, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     * @since 4_14
     */
    public SnmpContextv2c(String host, int port, String bindAddress, String typeSocketA)
            throws IOException {
        super(host, port, bindAddress, typeSocketA);
    }

    public int getVersion() {
        return SnmpConstants.SNMP_VERSION_2c;
    }

    public byte[] encodePacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, Object obj)
            throws IOException, EncodingException {
        byte[] packet = null;
        if (isDestroyed == true) {
            throw new EncodingException("Context can no longer be used, since it is already destroyed");
        } else {
            AsnEncoderv2c enc = new AsnEncoderv2c();
            ByteArrayOutputStream bay = enc.EncodeSNMPv2c(this, msg_type, rId, errstat,
                    errind, ve);

            int sz = bay.size();
            if (sz > maxRecvSize) {
                throw new EncodingException("Packet size (" + sz
                        + ") is > maximum size (" + maxRecvSize + ")");
            }
            packet = bay.toByteArray();
        }
        return packet;
    }

    /**
     * Processes an incoming SNMP v2c response.
     */
    protected void processIncomingResponse(ByteArrayInputStream in)
            throws DecodingException, IOException {
        AsnDecoderv2c rpdu = new AsnDecoderv2c();
        AsnPduSequence pduSeq = rpdu.DecodeSNMPv2c(in, getCommunity());
        if (pduSeq != null) {
            // got a message
            Integer rid = new Integer(pduSeq.getReqId());
            Pdu answ = getPdu(rid);
            if (answ != null) {
                answ.fillin(pduSeq);
            } else {
                if (AsnObject.debug > 3) {
                    System.out.println(
                            getClass().getName() + ".processIncomingResponse(): No Pdu with reqid " + rid.intValue());
                }
            }
        } else {
            if (AsnObject.debug > 3) {
                System.out.println(getClass().getName() + ".processIncomingResponse(): Error - missing seq input");
            }
        }
    }

    public Pdu processIncomingPdu(byte[] message)
            throws DecodingException, IOException {
        AsnDecoderv2c rpdu = new AsnDecoderv2c();
        ByteArrayInputStream in = new ByteArrayInputStream(message);
        AsnPduSequence pduSeq = rpdu.DecodeSNMPv2c(in, getCommunity());

        Pdu pdu = null;
        if (pduSeq != null) {
            byte type = pduSeq.getRespType();
            switch (type) {
                case SnmpConstants.GET_REQ_MSG:
                    pdu = new GetPdu(this);
                    break;
                case SnmpConstants.GETNEXT_REQ_MSG:
                    pdu = new GetNextPdu(this);
                    break;
                case SnmpConstants.SET_REQ_MSG:
                    pdu = new SetPdu(this);
                    break;
                case SnmpConstants.GETBULK_REQ_MSG:
                    pdu = new GetBulkPdu(this);
                    break;
                case SnmpConstants.INFORM_REQ_MSG:
                    pdu = new InformPdu(this);
                    break;
                // case SnmpConstants.GET_RSP_MSG:
                // A longly response should never be received here.
                // They should come in via the processIncomingResponse
                // route.
                // case SnmpConstants.GET_RPRT_MSG:
                // Reports are part of v3 timeliness communication
                case SnmpConstants.TRPV2_REQ_MSG:
                    pdu = new TrapPduv2(this);
                    break;
                default:
                    if (AsnObject.debug > 3) {
                        System.out.println(getClass().getName()
                                + ".ProcessIncomingPdu(): PDU received with type "
                                + pduSeq.getRespTypeString()
                                + ". Ignoring it.");
                    }
            }

            if (pdu != null) {
                pdu.fillin(pduSeq);
            }
        }
        return pdu;
    }

    /**
     * Returns a clone of this SnmpContextv2c.
     *
     * @exception CloneNotSupportedException Thrown when the constructor
     *                                       generates an IOException
     */
    public Object clone() throws CloneNotSupportedException {
        SnmpContextv2c clContext = null;
        try {
            clContext = new SnmpContextv2c(hostname, hostPort, bindAddr, typeSocket);
            clContext.setCommunity(new String(community));
        } catch (IOException exc) {
            throw new CloneNotSupportedException("IOException "
                    + exc.getMessage());
        }
        return clContext;
    }

    /**
     * Returns the hash key. This key is built out of all properties. It
     * serves as key for a hashtable of (v2c) contexts.
     *
     * @since 4_14
     * @return The hash key
     */
    public String getHashKey() {
        return super.getHashKey();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return The string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("SnmpContextv2c[");
        buffer.append("host=").append(hostname);
        buffer.append(", port=").append(hostPort);
        buffer.append(", bindAddress=").append(bindAddr);
        buffer.append(", socketType=").append(typeSocket);
        buffer.append(", community=").append(community);
        buffer.append(", #trapListeners=").append(trapSupport.getListenerCount());
        buffer.append(", #pduListeners=").append(pduSupport.getListenerCount());
        buffer.append("]");
        return buffer.toString();
    }

}
