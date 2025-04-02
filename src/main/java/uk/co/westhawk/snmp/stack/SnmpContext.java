// NAME
//      $RCSfile: SnmpContext.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.25 $
// CREATED
//      $Date: 2009/03/05 12:51:29 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1995, 1996 by West Consulting BV
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

/*
 * Copyright (C) 1996 - 2006 by Westhawk Ltd
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
 * This class contains the SNMP v1 context that is needed by every PDU to
 * send a SNMP v1 request.
 *
 * <p>
 * <code>destroy()</code> should be called when the context is no longer
 * used. This is the only way the threads will be stopped and garbage
 * collected.
 * </p>
 *
 * @see SnmpContextFace
 * @see SnmpContextPool
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.25 $ $Date: 2009/03/05 12:51:29 $
 */
public class SnmpContext extends AbstractSnmpContext
        implements SnmpContextFace, Cloneable {
    private static final String version_id = "@(#)$Id: SnmpContext.java,v 3.25 2009/03/05 12:51:29 birgita Exp $ Copyright Westhawk Ltd";

    String community = SnmpContextFace.DEFAULT_COMMUNITY;

    /**
     * Constructor.
     *
     * @param host The host to which the PDU will be sent
     * @param port The port where the SNMP server will be
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int)
     */
    public SnmpContext(String host, int port) throws IOException {
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
    public SnmpContext(String host, int port, String typeSocketA)
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
    public SnmpContext(String host, int port, String bindAddress, String typeSocketA)
            throws IOException {
        super(host, port, bindAddress, typeSocketA);
    }

    public int getVersion() {
        return SnmpConstants.SNMP_VERSION_1;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String newCommunity) {
        community = newCommunity;
    }

    public byte[] encodePacket(byte msg_type, int rId, int errstat,
            int errind, Enumeration ve, Object obj)
            throws IOException, EncodingException {
        byte[] packet = null;
        if (isDestroyed == true) {
            throw new EncodingException("Context can no longer be used, since it is already destroyed");
        } else {
            AsnEncoderv1 enc = new AsnEncoderv1();
            ByteArrayOutputStream bay = enc.EncodeSNMP(this, msg_type, rId, errstat,
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

    public byte[] encodePacket(byte msg_type, String enterprise,
            byte[] IpAddress, int generic_trap, int specific_trap,
            long timeTicks, Enumeration ve)
            throws IOException, EncodingException {
        byte[] packet = null;
        AsnEncoderv1 enc = new AsnEncoderv1();
        ByteArrayOutputStream bay = enc.EncodeSNMP(this, msg_type, enterprise,
                IpAddress, generic_trap, specific_trap, timeTicks, ve);

        int sz = bay.size();
        if (sz > maxRecvSize) {
            throw new EncodingException("Packet size (" + sz
                    + ") is > maximum size (" + maxRecvSize + ")");
        }
        packet = bay.toByteArray();
        return packet;
    }

    /**
     * Processes an incoming SNMP v1 response.
     */
    protected void processIncomingResponse(ByteArrayInputStream in)
            throws DecodingException, IOException {
        AsnDecoderv1 rpdu = new AsnDecoderv1();
        AsnSequence seqPdu = rpdu.DecodeSNMP(in, getCommunity());
        if (seqPdu instanceof AsnPduSequence) {
            AsnPduSequence pduSeq = (AsnPduSequence) seqPdu;
            if (pduSeq != null) {
                // got a message
                Integer rid = new Integer(pduSeq.getReqId());
                Pdu answ = getPdu(rid);
                if (answ != null) {
                    answ.fillin(pduSeq);
                } else {
                    if (AsnObject.debug > 3) {
                        System.out.println(getClass().getName() + ".processIncomingResponse(): No Pdu with reqid "
                                + rid.intValue());
                    }
                }
            } else {
                if (AsnObject.debug > 3) {
                    System.out.println(getClass().getName() + ".processIncomingResponse(): Error - missing seq input");
                }
            }
        } else {
            // it must be a Trap
        }
    }

    public Pdu processIncomingPdu(byte[] message)
            throws DecodingException, IOException {
        AsnDecoderv1 rpdu = new AsnDecoderv1();
        ByteArrayInputStream in = new ByteArrayInputStream(message);

        Pdu pdu = null;
        AsnSequence seqPdu = rpdu.DecodeSNMP(in, getCommunity());
        if (seqPdu instanceof AsnTrapPduv1Sequence) {
            AsnTrapPduv1Sequence pduSeq = (AsnTrapPduv1Sequence) seqPdu;
            if (pduSeq != null) {
                TrapPduv1 trapPdu = new TrapPduv1(this);
                trapPdu.fillin(pduSeq);
                pdu = trapPdu;
            }
        } else {
            // must be an ordinary PDU
            AsnPduSequence pduSeq = (AsnPduSequence) seqPdu;
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
                    // case SnmpConstants.GET_RSP_MSG:
                    // A longly response should never be received here.
                    // They should come in via the processIncomingResponse
                    // route.
                    // case SnmpConstants.GETBULK_REQ_MSG:
                    // not in v1
                    // case SnmpConstants.INFORM_REQ_MSG:
                    // not in v1
                    // case SnmpConstants.GET_RPRT_MSG:
                    // not in v1
                    // case SnmpConstants.TRPV2_REQ_MSG:
                    // not in v1
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
        }
        return pdu;
    }

    /**
     * Returns a clone of this SnmpContext.
     *
     * @exception CloneNotSupportedException Thrown when the constructor
     *                                       generates an IOException
     */
    public Object clone() throws CloneNotSupportedException {
        SnmpContext clContext = null;
        try {
            clContext = new SnmpContext(hostname, hostPort, bindAddr, typeSocket);
            clContext.setCommunity(new String(community));
        } catch (IOException exc) {
            throw new CloneNotSupportedException("IOException "
                    + exc.getMessage());
        }
        return clContext;
    }

    /**
     * Returns the hash key. This key is built out of all properties. It
     * serves as key for a hashtable of (v1) contexts.
     *
     * @since 4_14
     * @return The hash key
     */
    public String getHashKey() {
        String str = hostname
                + "_" + hostPort
                + "_" + bindAddr
                + "_" + typeSocket
                + "_" + community
                + "_v" + getVersion();
        return str;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return The string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("SnmpContext[");
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
