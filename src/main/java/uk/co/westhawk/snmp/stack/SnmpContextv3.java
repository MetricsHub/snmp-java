// NAME
//      $RCSfile: SnmpContextv3.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.31 $
// CREATED
//      $Date: 2009/03/05 13:12:50 $
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

import uk.co.westhawk.snmp.event.*;
import uk.co.westhawk.snmp.beans.*;

/**
 * This class contains the SNMP v3 context that is needed by every PDU to
 * send a SNMP v3 request.
 * Most of the work is done by SnmpContextv3Basis, like doing discovery. 
 *
 * <p>
 * Now that the stack can send traps and receive requests, 
 * it needs to be able to act as an
 * authoritative SNMP engine. This is done via the interface UsmAgent.
 * The DefaultUsmAgent is not guaranteed to work; agents (or rather 
 * authoritative engines) <em>should</em> provide a better implementation.
 * </p>
 *
 * <p>
 * This class adds a UsmBeingDiscoveredBean as listener. This bean
 * handles any incoming discovery PDU. Only when acting as
 * authoritative engine should there be any discovery PDU.
 * </p>
 *
 * @see SnmpContextv3Face
 * @see SnmpContextv3Pool
 * @see TimeWindow
 * @see DefaultUsmAgent
 * @see UsmAgent
 * @see #setUsmAgent(UsmAgent)
 * @see UsmDiscoveryBean
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.31 $ $Date: 2009/03/05 13:12:50 $
 */
public class SnmpContextv3 extends SnmpContextv3Basis {
    private static final String version_id = "@(#)$Id: SnmpContextv3.java,v 3.31 2009/03/05 13:12:50 birgita Exp $ Copyright Westhawk Ltd";

    private UsmBeingDiscoveredBean myDiscBean = null;

    /**
     * Constructor.
     *
     * @param host The host to which the PDU will be sent
     * @param port The port where the SNMP server will be
     * @see AbstractSnmpContext#AbstractSnmpContext(String, int)
     */
    public SnmpContextv3(String host, int port) throws IOException {
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
    public SnmpContextv3(String host, int port, String typeSocketA)
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
    public SnmpContextv3(String host, int port, String bindAddress, String typeSocketA)
            throws IOException {
        super(host, port, bindAddress, typeSocketA);
    }

    /**
     * Makes sure the UsmBeingDiscoveredBean is added as RequestPduListener,
     * so that discovery requests are handled. When listening for incoming
     * requests, the stack become authoritative. You have (!) to create a
     * proper usmAgent so the stack can be discovered.
     *
     * <p>
     * Don't use the TCP_SOCKET when listening for request PDUs. It doesn't
     * provide functionality to send a response back.
     * </p>
     *
     * @see #removeRequestPduListener(RequestPduListener, ListeningContextPool)
     * @see UsmBeingDiscoveredBean
     * @see SnmpContextv3Basis#setUsmAgent(UsmAgent)
     *
     * @param l        The request PDU listener
     * @param lcontext The listening context
     */
    public void addRequestPduListener(RequestPduListener l, ListeningContextPool lcontext)
            throws IOException {
        super.addRequestPduListener(l, lcontext);

        if (myDiscBean == null) {
            myDiscBean = new UsmBeingDiscoveredBean(this, usmAgent);
        }
        myDiscBean.addRequestPduListener(lcontext);
    }

    /**
     * Removes the UsmBeingDiscoveredBean as listener.
     *
     * @see #addRequestPduListener(RequestPduListener, ListeningContextPool)
     *
     * @param l        The request PDU listener
     * @param lcontext The listening context
     */
    public void removeRequestPduListener(RequestPduListener l, ListeningContextPool lcontext)
            throws IOException {
        super.removeRequestPduListener(l, lcontext);
        if (myDiscBean != null) {
            myDiscBean.removeRequestPduListener(lcontext);
            myDiscBean.freeResources();
            myDiscBean = null;
        }
    }

    /**
     * Processes an incoming PDU, that is <em>not</em> a Discovery PDU.
     * <p>
     * See <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
     * </p>
     *
     * <p>
     * This method calls first processPotentialTrap and then
     * processPotentialRequest. The reason this code is split up is because
     * in one case the stack acts as authoritative engine and as non
     * authoritative engine in the other..
     * </p>
     *
     * @see #rawPduReceived
     * @see #processPotentialTrap
     * @see #processPotentialRequest
     */
    public Pdu processIncomingPdu(byte[] message)
            throws DecodingException, IOException {
        String msg = checkContextSanity();
        if (msg != null) {
            throw new DecodingException(msg);
        }
        int l = message.length;
        byte[] copyOfMessage1 = new byte[l];
        byte[] copyOfMessage2 = new byte[l];
        System.arraycopy(message, 0, copyOfMessage1, 0, l);
        System.arraycopy(message, 0, copyOfMessage2, 0, l);

        AsnDecoderv3 rpdu = new AsnDecoderv3();
        ByteArrayInputStream in = new ByteArrayInputStream(message);
        AsnSequence asnTopSeq = rpdu.DecodeSNMPv3(in);
        int messageId = rpdu.getMessageId(asnTopSeq);

        Pdu pdu = null;

        DecodingException encryptionDecodingException1 = null;
        IOException encryptionIOException1 = null;
        try {
            pdu = processPotentialTrap(rpdu, asnTopSeq, copyOfMessage1);
        } catch (DecodingException exc) {
            encryptionDecodingException1 = exc;
            if (AsnObject.debug > 3) {
                System.out.println(getClass().getName()
                        + ".processPotentialTrap(): DecodingException: "
                        + exc.getMessage());
            }
        } catch (IOException exc) {
            encryptionIOException1 = exc;
            if (AsnObject.debug > 3) {
                System.out.println(getClass().getName()
                        + ".processPotentialTrap(): IOException: "
                        + exc.getMessage());
            }
        }

        DecodingException encryptionDecodingException2 = null;
        IOException encryptionIOException2 = null;
        if (pdu == null) {
            try {
                pdu = processPotentialRequest(rpdu, asnTopSeq, copyOfMessage2);
            } catch (DecodingException exc) {
                encryptionDecodingException2 = exc;
                if (AsnObject.debug > 3) {
                    System.out.println(getClass().getName()
                            + ".processPotentialRequest(): DecodingException: "
                            + exc.getMessage());
                }
            } catch (IOException exc) {
                encryptionIOException2 = exc;
                if (AsnObject.debug > 3) {
                    System.out.println(getClass().getName()
                            + ".processPotentialRequest(): IOException: "
                            + exc.getMessage());
                }
            }
        }

        if (pdu != null) {
            pdu.snmpv3MsgId = new Integer(messageId);
        } else {
            if (encryptionIOException2 != null) {
                throw encryptionIOException2;
            }
            if (encryptionDecodingException2 != null) {
                throw encryptionDecodingException2;
            }
            if (encryptionIOException1 != null) {
                throw encryptionIOException1;
            }
            if (encryptionDecodingException1 != null) {
                throw encryptionDecodingException1;
            }
        }
        return pdu;
    }

    /**
     * Processes an incoming PDU, to see if it is a Trap.
     * This method is called by processIncomingPdu.
     *
     * When receiving traps the stack is non authoritative.
     *
     * @see #processIncomingPdu
     * @see #processPotentialRequest
     * @since 4_14
     */
    public Pdu processPotentialTrap(AsnDecoderv3 rpdu, AsnSequence asnTopSeq,
            byte[] message)
            throws DecodingException, IOException {
        // decode as Non Authoratative engine
        AsnPduSequence pduSeq = rpdu.processSNMPv3(this, asnTopSeq, message, false);
        Pdu pdu = null;
        if (pduSeq != null) {
            byte type = pduSeq.getRespType();
            if (type == SnmpConstants.TRPV2_REQ_MSG) {
                pdu = new TrapPduv2(this);
                pdu.fillin(pduSeq);

                if (AsnObject.debug > 3) {
                    System.out.println(getClass().getName()
                            + ".processPotentialTrap(): PDU received with type "
                            + pduSeq.getRespTypeString()
                            + ". Not ignoring it!");
                }
            } else {
                if (AsnObject.debug > 3) {
                    System.out.println(getClass().getName()
                            + ".processPotentialTrap(): PDU received is not TRPV2_REQ_MSG"
                            + ". Ignoring it.");
                }
            }
        } else {
            if (AsnObject.debug > 3) {
                System.out.println(getClass().getName()
                        + ".processPotentialTrap(): pduSeq == null"
                        + ". Ignoring it.");
            }
        }
        return pdu;
    }

    /**
     * Processes an incoming PDU, to see if it is a Request.
     * This method is called by processIncomingPdu.
     *
     * When receiving pdu requests the stack is authoritative.
     *
     * @see #processIncomingPdu
     * @see #processPotentialTrap
     * @since 4_14
     */
    public Pdu processPotentialRequest(AsnDecoderv3 rpdu, AsnSequence asnTopSeq,
            byte[] message)
            throws DecodingException, IOException {
        // decode as Authoratative engine
        AsnPduSequence pduSeq = rpdu.processSNMPv3(this, asnTopSeq, message, true);
        Pdu pdu = null;
        if (pduSeq != null) {
            byte type = pduSeq.getRespType();
            if (type == SnmpConstants.GET_REQ_MSG && pduSeq.isSnmpv3Discovery() == true) {
                if (AsnObject.debug > 3) {
                    System.out.println(getClass().getName()
                            + ".ProcessIncomingPdu(): received discovery pdu"
                            + ". Ignoring it.");
                }
            } else {
                switch (type) {
                    case SnmpConstants.GET_REQ_MSG:
                        pdu = new GetPdu(this);
                        pdu.fillin(pduSeq);
                        break;
                    case SnmpConstants.GETNEXT_REQ_MSG:
                        pdu = new GetNextPdu(this);
                        pdu.fillin(pduSeq);
                        break;
                    case SnmpConstants.SET_REQ_MSG:
                        pdu = new SetPdu(this);
                        pdu.fillin(pduSeq);
                        break;
                    case SnmpConstants.GETBULK_REQ_MSG:
                        pdu = new GetBulkPdu(this);
                        pdu.fillin(pduSeq);
                        break;
                    case SnmpConstants.INFORM_REQ_MSG:
                        pdu = new InformPdu(this);
                        pdu.fillin(pduSeq);
                        break;
                    // case SnmpConstants.GET_RSP_MSG:
                    // A lonely response should never be received here.
                    // They should come in via the processIncomingResponse
                    // route.
                    // case SnmpConstants.GET_RPRT_MSG:
                    // Reports are part of v3 timeliness communication.
                    // case SnmpConstants.TRPV2_REQ_MSG:
                    // Traps should have been decoded in
                    // processPotentialTrap() above
                    default:
                        if (AsnObject.debug > 3) {
                            System.out.println(getClass().getName()
                                    + ".processPotentialRequest(): PDU received with type "
                                    + pduSeq.getRespTypeString()
                                    + ". Ignoring it.");
                        }
                }

                if (pdu != null) {
                    if (AsnObject.debug > 3) {
                        System.out.println(getClass().getName()
                                + ".processPotentialRequest(): PDU received with type "
                                + pduSeq.getRespTypeString()
                                + ". Not ignoring it!");
                    }
                }
            }
        } else {
            if (AsnObject.debug > 3) {
                System.out.println(getClass().getName()
                        + "..processPotentialRequest(): pduSeq == null"
                        + ". Ignoring it.");
            }
        }
        return pdu;
    }

    /**
     * Returns a clone of this SnmpContextv3.
     *
     * @exception CloneNotSupportedException Thrown when the constructor
     *                                       generates an IOException
     */
    public Object clone() throws CloneNotSupportedException {
        SnmpContextv3 clContext = null;
        try {
            clContext = new SnmpContextv3(hostname, hostPort, bindAddr, typeSocket);
            clContext = (SnmpContextv3) cloneParameters(clContext);
        } catch (IOException exc) {
            throw new CloneNotSupportedException("IOException "
                    + exc.getMessage());
        }
        return clContext;
    }

}
