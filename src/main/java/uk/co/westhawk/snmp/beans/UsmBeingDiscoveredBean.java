// NAME
//      $RCSfile: UsmBeingDiscoveredBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.8 $
// CREATED
//      $Date: 2009/03/05 15:51:42 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2005 - 2006 by Westhawk Ltd
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

package uk.co.westhawk.snmp.beans;

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
import uk.co.westhawk.snmp.pdu.*;
import uk.co.westhawk.snmp.event.*;
import uk.co.westhawk.snmp.util.*;
import java.util.*;

/**
 * <p>
 * This bean handles being discovered as an authoritative engine by an
 * non-authoritative engine (SNMPv3 only).
 * </p>
 *
 * <p>
 * The discovery process consists of two steps: 
 * 1. first the SNMP engine ID has to be discovered, 
 * second the timeline details of the SNMP engine ID have to be discovered. 
 * <br/>
 * 2. For the last step the username of the principal is needed. 
 * <br/>
 * All these parameters should be provided by (your own) UsmAgent.
 * </p>
 *
 * <p>
 * This class is not very efficient. The private context
 * discEngineIdContextIn will be the same for every SnmpContextv3 that
 * creates this bean.
 * </p>
 *
 * <p>
 * See <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
 * </p>
 *
 * @see SnmpContextv3#addRequestPduListener
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.8 $ $Date: 2009/03/05 15:51:42 $
 */
public class UsmBeingDiscoveredBean implements RequestPduListener {
    private static final String version_id = "@(#)$Id: UsmBeingDiscoveredBean.java,v 1.8 2009/03/05 15:51:42 birgita Exp $ Copyright Westhawk Ltd";

    private UsmAgent usmAgent = null;
    private SnmpContextv3 context = null;

    // the context to receive incoming requests
    private SnmpContextv3Discovery discEngineIdContextIn;
    private SnmpContextv3Discovery discTimeLineContextIn;

    // the context to send the answer back
    private SnmpContextv3 discEngineIdContextOut;
    private SnmpContextv3 discTimeLineContextOut;

    /**
     * Constructor.
     *
     * @param myUsmAgent The usmAgent that will provide all the
     *                   authoritative engine parameters
     * @param myContext  The context
     *
     * @see SnmpContextv3#addRequestPduListener
     */
    public UsmBeingDiscoveredBean(SnmpContextv3 myContext, UsmAgent myUsmAgent)
            throws java.io.IOException {
        usmAgent = myUsmAgent;
        context = myContext;

        if (AsnObject.debug > 4) {
            System.out.println(getClass().getName() + " Constructor:"
                    + "usmAgent=" + usmAgent.toString()
                    + "context=" + context.toString());
        }
        usmAgent.setSnmpContext(context);

        discEngineIdContextIn = new SnmpContextv3Discovery(context.getSendToHostAddress(),
                context.getPort(), context.getBindAddress(), context.getTypeSocket());
        discEngineIdContextIn.setUserName("");
        discEngineIdContextIn.setUseAuthentication(false);
        discEngineIdContextIn.setUsePrivacy(false);
        discEngineIdContextIn.setContextEngineId(SnmpUtilities.toBytes(usmAgent.getSnmpEngineId()));
        discEngineIdContextIn.setContextName("");
        discEngineIdContextIn.setUsmAgent(usmAgent);

        discTimeLineContextIn = new SnmpContextv3Discovery(context.getSendToHostAddress(),
                context.getPort(), context.getBindAddress(), context.getTypeSocket());
        discTimeLineContextIn.setUserName(context.getUserName());
        discTimeLineContextIn.setUseAuthentication(context.isUseAuthentication());
        discTimeLineContextIn.setAuthenticationProtocol(context.getAuthenticationProtocol());
        discTimeLineContextIn.setUserAuthenticationPassword(context.getUserAuthenticationPassword());
        discTimeLineContextIn.setUsePrivacy(context.isUsePrivacy());
        discTimeLineContextIn.setPrivacyProtocol(context.getPrivacyProtocol());
        discTimeLineContextIn.setUserPrivacyPassword(context.getUserPrivacyPassword());
        discTimeLineContextIn.setContextEngineId(SnmpUtilities.toBytes(usmAgent.getSnmpEngineId()));
        discTimeLineContextIn.setContextName("");
        discTimeLineContextIn.setUsmAgent(usmAgent);
    }

    /**
     * @param lcontext The listening context for incoming (discovery) requests
     * @see SnmpContextv3#addRequestPduListener
     */
    public void addRequestPduListener(ListeningContextPool lcontext)
            throws java.io.IOException {
        if (AsnObject.debug > 4) {
            System.out.println(getClass().getName()
                    + ".addRequestPduListener(" + lcontext.toString() + ")");
        }
        discEngineIdContextIn.addRequestPduListener(this, lcontext);
        discTimeLineContextIn.addRequestPduListener(this, lcontext);
    }

    /**
     * @param lcontext Stop listening on this listening context for incoming
     *                 (discovery) requests
     * @see SnmpContextv3#removeRequestPduListener
     */
    public void removeRequestPduListener(ListeningContextPool lcontext)
            throws java.io.IOException {
        if (AsnObject.debug > 4) {
            System.out.println(getClass().getName()
                    + ".removeRequestPduListener(" + lcontext.toString() + ")");
        }
        discEngineIdContextIn.removeRequestPduListener(this, lcontext);
        discTimeLineContextIn.removeRequestPduListener(this, lcontext);
    }

    /**
     * Receiving an incoming (discovery) PDU request.
     */
    public void requestPduReceived(RequestPduEvent evt) {
        Object src = evt.getSource();
        int port = evt.getHostPort();
        Pdu orgPdu = evt.getPdu();
        if (src == discEngineIdContextIn) {
            sendEngineIdReport(orgPdu, port);
        } else {
            sendTimeLineReport(orgPdu, port);
        }
    }

    /**
     * Send back the snmp engine ID.
     */
    protected void sendEngineIdReport(Pdu orgPdu, int port) {
        if (AsnObject.debug > 4) {
            System.out.println(getClass().getName() + ".sendEngineIdReport()");
        }

        try {
            if (discEngineIdContextOut != null) {
                discEngineIdContextOut.destroy();
            }
            discEngineIdContextOut = new SnmpContextv3(discEngineIdContextIn.getHost(),
                    port, discEngineIdContextIn.getBindAddress(),
                    discEngineIdContextIn.getTypeSocket());
            discEngineIdContextOut = (SnmpContextv3) discEngineIdContextIn.cloneParameters(discEngineIdContextOut);

            Pdu repPdu = new ReportPdu(discEngineIdContextOut, orgPdu);
            repPdu.addOid(usmStatsConstants.usmStatsUnknownEngineIDs,
                    new AsnUnsInteger(usmAgent.getUsmStatsUnknownEngineIDs()));
            repPdu.send();
        } catch (java.io.IOException iexc) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".sendEngineIdReport(): "
                        + "IOException: " + iexc.getMessage());
            }
        } catch (PduException pexc) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".sendEngineIdReport(): "
                        + "PduException: " + pexc.getMessage());
            }
        }
    }

    /**
     * Send back the time lininess
     */
    protected void sendTimeLineReport(Pdu orgPdu, int port) {
        if (AsnObject.debug > 4) {
            System.out.println(getClass().getName() + ".sendTimeLineReport()");
        }

        try {
            if (discTimeLineContextOut != null) {
                discTimeLineContextOut.destroy();
            }
            discTimeLineContextOut = new SnmpContextv3(discTimeLineContextIn.getHost(),
                    port, discTimeLineContextIn.getBindAddress(),
                    discTimeLineContextIn.getTypeSocket());
            discTimeLineContextOut = (SnmpContextv3) discTimeLineContextIn.cloneParameters(discTimeLineContextOut);

            Pdu repPdu = new ReportPdu(discTimeLineContextOut, orgPdu);
            repPdu.addOid(usmStatsConstants.usmStatsNotInTimeWindows,
                    new AsnUnsInteger(usmAgent.getUsmStatsNotInTimeWindows()));
            repPdu.send();
        } catch (java.io.IOException iexc) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".sendTimeLineReport(): "
                        + "IOException: " + iexc.getMessage());
            }
        } catch (PduException pexc) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".sendEngineIdReport(): "
                        + "PduException: " + pexc.getMessage());
            }
        }
    }

    /**
     * Destroys all the contexts in use.
     */
    public void freeResources() {
        discEngineIdContextIn.destroy();
        discEngineIdContextIn = null;

        discTimeLineContextIn.destroy();
        discTimeLineContextIn = null;

        if (discEngineIdContextOut != null) {
            discEngineIdContextOut.destroy();
            discEngineIdContextOut = null;
        }
        if (discTimeLineContextOut != null) {
            discTimeLineContextOut.destroy();
            discTimeLineContextOut = null;
        }
    }

}
