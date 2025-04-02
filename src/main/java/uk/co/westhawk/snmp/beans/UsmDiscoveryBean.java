// NAME
//      $RCSfile: UsmDiscoveryBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.21 $
// CREATED
//      $Date: 2009/03/05 15:51:42 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2000 - 2006 by Westhawk Ltd
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
import java.util.*;

/**
 * <p>
 * This bean performs the SNMPv3 USM discovery process.
 * </p>
 *
 * <p>
 * The process consists of two steps: first the SNMP engine ID has to be
 * discovered, second the timeline details of the SNMP engine ID have to
 * be discovered. For the last step the username of the principal is
 * needed. 
 * </p>
 *
 * <p>
 * See <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
 * </p>
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.21 $ $Date: 2009/03/05 15:51:42 $
 */
public class UsmDiscoveryBean {
    private static final String version_id = "@(#)$Id: UsmDiscoveryBean.java,v 1.21 2009/03/05 15:51:42 birgita Exp $ Copyright Westhawk Ltd";

    private SnmpContextv3Pool context;
    private String userName = null;
    private String userAuthPassword = null;
    private String userPrivPassword = null;
    private int authProtocol, privProtocol;
    private int retry_intervals[] = { 500, 1000, 2000, 5000, 5000 };

    /**
     * Constructor.
     *
     * @param host The host to discover
     * @param port The port to discover
     * @see SnmpContextv3#SnmpContextv3(String, int)
     * @see #startDiscovery()
     */
    public UsmDiscoveryBean(String host, int port)
            throws java.io.IOException {
        this(host, port, null, SnmpContextBasisFace.STANDARD_SOCKET);
    }

    /**
     * Constructor.
     *
     * @param host        The host to discover
     * @param port        The port to discover
     * @param bindAddr    The local address the server will bind to
     * @param typeSocketA The type of socket to use.
     *
     * @see SnmpContextv3#SnmpContextv3(String, int, String, String)
     * @see #startDiscovery()
     */
    public UsmDiscoveryBean(String host, int port, String bindAddr, String typeSocketA)
            throws java.io.IOException {
        context = new SnmpContextv3Pool(host, port, bindAddr, typeSocketA);
    }

    /**
     * Sets the user's authentication details.
     * With these details the time line details will be retrieved.
     * If the user details are not set, only the engine ID will be
     * discovered.
     * 
     * <p>
     * The time line details only need to be known when the user want to
     * send authenticated message to this SNMP engine.
     * </p>
     *
     * @param newUserName     The user name
     * @param newUserPassword The user authentication password
     * @param protocol        The user authentication protocol
     *
     * @see SnmpContextv3#setUserName(String)
     * @see SnmpContextv3#setUserAuthenticationPassword(String)
     * @see SnmpContextv3#setAuthenticationProtocol(int)
     */
    public void setAuthenticationDetails(String newUserName, String newUserPassword,
            int protocol) {
        userName = newUserName;
        userAuthPassword = newUserPassword;
        authProtocol = protocol;
    }

    /**
     * Sets the user's privacy details.
     * With these details the time line details will be retrieved if needed.
     * 
     * <p>
     * The time line details only need to be known when the user want to
     * send authenticated message to this SNMP engine.
     * </p>
     *
     * @param newUserPassword The user privacy password
     *
     * @see SnmpContextv3#setUserPrivacyPassword(String)
     */
    public void setPrivacyDetails(String newUserPassword, int protocol) {
        userPrivPassword = newUserPassword;
        privProtocol = protocol;
    }

    /**
     * Sets the retry intervals of the PDU. The length of the array
     * corresponds with the number of retries. Each entry in the array
     * is the number of milliseconds of each try.
     *
     * <p>
     * If used, please set before sending!
     * </p>
     *
     * The default is {500, 1000, 2000, 5000, 5000}.
     * It is good practice to make the interval bigger with each retry,
     * if the numbers are the same the chance of collision is higher.
     *
     * @param rinterval The interval in msec of each retry
     * @see Pdu#setRetryIntervals(int[])
     */
    public void setRetryIntervals(int rinterval[]) {
        retry_intervals = rinterval;
    }

    /**
     * Starts the discovery. This method will send a Pdu to discover the
     * SNMP engine ID. Set the user details before calling this method, if
     * you want the time line details to be discovered as well.
     *
     * <p>
     * This is a blocking call! It will return when it has done the whole
     * discovery.
     * </p>
     *
     * @see #setAuthenticationDetails(String, String, int)
     * @see #discoveryEngineId
     * @see #discoveryTimeLine
     */
    // Thanks to "Cochran, Steve A." <steve@more.net>;
    // freeResources(); is called before throwing an exception
    public void startDiscovery()
            throws PduException, java.io.IOException {
        try {
            discoveryEngineId();
        } catch (PduException exc) {
            // You shouldn't really get an exception when doing engineID
            // discovery.
            freeResources(); // <-- Steve
            throw new PduException("Engine ID discovery: "
                    + exc.getMessage());
        }

        try {
            discoveryTimeLine();
        } catch (PduException exc) {
            // You can get an exception for authPriv. I noticed that when
            // testing against MG-SOFT.
            // Only throw the exception if the timeline was not discovered.

            TimeWindow tWindow = TimeWindow.getCurrent();
            String engineId = tWindow.getSnmpEngineId(context.getSendToHostAddress(),
                    context.getPort());
            if (tWindow.isTimeLineKnown(engineId) == false) {
                freeResources(); // <-- Steve
                throw new PduException("Timeline discovery: "
                        + exc.getMessage());
            }
        }

        if (AsnObject.debug > 4) {
            System.out.println(getClass().getName() + ".startDiscovery(): "
                    + "Done, context " + context.toString());
        }

        freeResources();
    }

    /**
     * Destroy the context in use.
     */
    public void freeResources() {
        context.destroy();
        context = null;
    }

    /**
     * Starts the discovery of the SNMP engine ID.
     *
     * <p>
     * This is a blocking call! It will return when it has done the whole
     * discovery.
     * </p>
     *
     * @see #startDiscovery
     * @see #discoveryTimeLine
     */
    protected void discoveryEngineId()
            throws PduException, java.io.IOException {
        DiscoveryPdu pdu;

        TimeWindow tWindow = TimeWindow.getCurrent();
        String engineId = tWindow.getSnmpEngineId(context.getHost(),
                context.getPort());

        // Just check again to be sure
        if (engineId == null) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".discoveryEngineId(): "
                        + "Starting discovery Engine ID ...");
            }
            context.setUserName("");
            context.setUseAuthentication(false);
            context.setUsePrivacy(false);
            context.setContextEngineId(new byte[0]);
            context.setContextName("");

            pdu = new DiscoveryPdu(context);
            pdu.setRetryIntervals(retry_intervals);
            pdu.send();
            pdu.waitForSelf();
            varbind[] vars = pdu.getResponseVarbinds();
        }
    }

    /**
     * Starts the discovery of the Time line.
     *
     * <p>
     * This is a blocking call! It will return when it has done the whole
     * discovery.
     * </p>
     *
     * @see #startDiscovery
     * @see #discoveryEngineId
     */
    protected void discoveryTimeLine()
            throws PduException, java.io.IOException {
        DiscoveryPdu pdu;

        TimeWindow tWindow = TimeWindow.getCurrent();
        String engineId = tWindow.getSnmpEngineId(context.getSendToHostAddress(),
                context.getPort());

        // The engineId should be known by now.
        // Only do timeline discovery if it is not known yet.
        if (tWindow.isTimeLineKnown(engineId) == false && userName != null) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".discoveryTimeLine(): "
                        + "Starting discovery Timeline ...");
            }
            context.setUserName(userName);
            context.setUserAuthenticationPassword(userAuthPassword);
            context.setUseAuthentication(true);
            context.setAuthenticationProtocol(authProtocol);
            context.setContextEngineId(new byte[0]);
            context.setContextName("");

            if (userPrivPassword != null) {
                context.setUsePrivacy(true);
                context.setUserPrivacyPassword(userPrivPassword);
                context.setPrivacyProtocol(privProtocol);
            } else {
                context.setUsePrivacy(false);
            }

            pdu = new DiscoveryPdu(context);
            pdu.setRetryIntervals(retry_intervals);
            pdu.send();
            pdu.waitForSelf();
            varbind[] vars = pdu.getResponseVarbinds();

            if (AsnObject.debug > 4) {
                System.out.println("Did discovery time line");
            }
        }
    }

}
