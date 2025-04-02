// NAME
//      $RCSfile: AscendActiveSessionBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.13 $
// CREATED
//      $Date: 2006/01/25 18:08:55 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1998 - 2006 by Westhawk Ltd
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
import java.awt.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.beans.*;

/**
 * <p>
 * This bean is written for the Ascend Router from 
 * <a href="http://www.lucent.com">Lucent Technologies, Inc</a>.
 *
 * The Ascend Enterprise MIBs can be downloaded from
 * <a
 * href="http://www.oidview.com/mibs/detail.html?VID=529">ByteSphere</a>.
 * </p>
 *
 * <p>
 * This bean collects information about the active session on an Ascend router.
 * </p>
 *
 * <p>
 * The properties in the parent classes should be set, before calling
 * the action() method. Via a PropertyChangeEvent the application/applet
 * will be notified. 
 * </p>
 *
 * @see SNMPBean#setHost
 * @see SNMPBean#setPort
 * @see SNMPBean#setCommunityName
 * @see SNMPRunBean#setUpdateInterval
 * @see SNMPBean#addPropertyChangeListener
 * @see SNMPBean#action
 * @see GetNextPdu_vec
 * 
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.13 $ $Date: 2006/01/25 18:08:55 $
 *
 */
public class AscendActiveSessionBean extends SNMPRunBean implements Observer {
    private static final String version_id = "@(#)$Id: AscendActiveSessionBean.java,v 1.13 2006/01/25 18:08:55 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * A unique number identifying this active session.
     * See the session.mib
     */
    public final static String ssnActiveCallReferenceNum = "1.3.6.1.4.1.529.12.3.1.1";
    /**
     * The name of the remote user.
     * See the session.mib
     */
    public final static String ssnActiveUserName = "1.3.6.1.4.1.529.12.3.1.4";
    /**
     * The IP address of the remote user.
     * See the session.mib
     */
    public final static String ssnActiveUserIPAddress = "1.3.6.1.4.1.529.12.3.1.5";
    /**
     * The subnet mask of the remote user.
     * See the session.mib
     */
    public final static String ssnActiveUserSubnetMask = "1.3.6.1.4.1.529.12.3.1.6";
    /**
     * The current service provided to the remote user.
     * See the session.mib
     */
    public final static String ssnActiveCurrentService = "1.3.6.1.4.1.529.12.3.1.7";

    private final static int NR_OID = 5;

    private final static int none = 1;
    private final static int other = 2; // none of the following
    private final static int ppp = 3; // Point-To-Point Protocol
    private final static int slip = 4; // Serial Line IP
    private final static int mpp = 5; // Multichannel PPP
    private final static int x25 = 6; // X.25
    private final static int combinet = 7; // Combinet
    private final static int frameRelay = 8; // Frame Relay
    private final static int euraw = 9;
    private final static int euui = 10;
    private final static int telnet = 11; // telnet
    private final static int telnetBinary = 12; // binary telnet
    private final static int rawTcp = 13; // raw TCP
    private final static int terminalServer = 14; // terminal server
    private final static int mp = 15; // Multilink PPP
    private final static int virtualConnect = 16; // Virtual Connect to a modem
    private final static int dchannelX25 = 17; // D Channel X.25
    private final static int dtpt = 18; // DTPT session to ZGR

    private final static String msg_service[] = {
            "",
            "none",
            "other",
            "ppp",
            "slip",
            "mpp",
            "x25",
            "combinet",
            "frameRelay",
            "euraw",
            "euui",
            "telnet",
            "telnetBinary",
            "rawTcp",
            "terminalServer",
            "mp",
            "virtualConnect",
            "dchannelX25",
            "dtpt"
    };

    private GetNextPdu_vec pdu;

    private boolean isGetNextInFlight;
    private Date lastUpdateDate = null;

    private int callReferenceNum = 0;
    private String userName = "";
    private String userIPAddress = "0.0.0.0";
    private String userSubnetMask = "0.0.0.0";
    private String currentService = "none";

    /**
     * The default constructor.
     */
    public AscendActiveSessionBean() {
    }

    /**
     * The constructor that will set the host and the port no.
     *
     * @param h the hostname
     * @param p the port no
     * @see SNMPBean#setHost
     * @see SNMPBean#setPort
     */
    public AscendActiveSessionBean(String h, int p) {
        this(h, p, null);
    }

    /**
     * The constructor that will set the host, the port no and the local
     * bind address.
     *
     * @param h the hostname
     * @param p the port no
     * @param b the local bind address
     * @see SNMPBean#setHost
     * @see SNMPBean#setPort
     * @see SNMPBean#setBindAddress
     *
     * @since 4_14
     */
    public AscendActiveSessionBean(String h, int p, String b) {
        this();
        setHost(h);
        setPort(p);
        setBindAddress(b);
    }

    /**
     * Returns the date of the moment when this bean was last updated.
     * This might be null when the first time the update was not finished.
     *
     * @return the last update date
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * Returns the name of the remote user of the Ascend active session.
     *
     * @return the name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the IP address of the remote user of the Ascend active
     * session.
     *
     * @return the IP address
     */
    public String getUserIPAddress() {
        return userIPAddress;
    }

    /**
     * Returns the subnet mask of the remote user of the Ascend active
     * session.
     *
     * @return the subnet mask
     */
    public String getUserSubnetMask() {
        return userSubnetMask;
    }

    /**
     * Returns the current service provided to the remote user of the Ascend
     * active session.
     *
     * @return the current service
     */
    public String getCurrentService() {
        return currentService;
    }

    /**
     * This method starts the action of the bean. It will initialises
     * all variables before starting.
     */
    public void action() {
        if (isHostPortReachable()) {
            lastUpdateDate = new Date();
            isGetNextInFlight = false;
            setRunning(true);
        }
    }

    /**
     * Implements the running of the bean.
     *
     * It will send the Pdu, if the previous one is not still in flight.
     * 
     * @see SNMPRunBean#isRunning()
     */
    public void run() {
        while (context != null && isRunning()) {
            if (isGetNextInFlight == false) {
                // start the GetNext loop again
                isGetNextInFlight = true;
                pdu = new GetNextPdu_vec(context, NR_OID);
                pdu.addObserver(this);

                pdu.addOid(ssnActiveCallReferenceNum);
                pdu.addOid(ssnActiveUserName);
                pdu.addOid(ssnActiveUserIPAddress);
                pdu.addOid(ssnActiveUserSubnetMask);
                pdu.addOid(ssnActiveCurrentService);
                try {
                    pdu.send();
                } catch (PduException exc) {
                    System.out.println("PduException " + exc.getMessage());
                } catch (IOException exc) {
                    System.out.println("IOException " + exc.getMessage());
                }
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException ix) {
                ;
            }
        }
    }

    /**
     * This method is called when the Pdu response is received. When all
     * answers are received it will fire the property change event.
     *
     * The answers are stored in a hashtable, this is done because the speed
     * can only be calculated with the previous answer.
     *
     * @see SNMPBean#addPropertyChangeListener
     */
    public void update(Observable obs, Object ov) {
        int service;
        varbind[] var;

        pdu = (GetNextPdu_vec) obs;

        if (pdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
            var = (varbind[]) ov;
            callReferenceNum = ((AsnInteger) var[0].getValue()).getValue();

            if (callReferenceNum > 0) {
                userName = ((AsnOctets) var[1].getValue()).getValue();
                userIPAddress = ((AsnOctets) var[2].getValue()).getValue();
                userSubnetMask = ((AsnOctets) var[3].getValue()).getValue();

                service = ((AsnInteger) var[4].getValue()).getValue();
                currentService = msg_service[service];
            }

            // the GetNext loop has ended
            lastUpdateDate = new Date();
            isGetNextInFlight = false;
            firePropertyChange("services", null, null);
        }
    }

}
