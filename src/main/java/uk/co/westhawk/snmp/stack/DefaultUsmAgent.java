// NAME
//      $RCSfile: DefaultUsmAgent.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.10 $
// CREATED
//      $Date: 2009/03/05 15:51:42 $
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

import uk.co.westhawk.snmp.beans.UsmDiscoveryBean;

/**
 * This implementation of UsmAgent tries to discover the parameters by
 * doing the default USM discovery process on localhost. 
 *
 * <p>
 * Note that it is not guaranteed that the agent will allow discovery 
 * by itself. Also, if the SNMP agent reboots while the stack is
 * running, it will not pick up the new boots and time.
 * </p>
 *
 * <p>
 * Users are advised and encouraged to provide a better, more accurate
 * implementation of UsmAgent.
 * </p>
 *
 * <p>
 * See <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
 * </p>
 * @see SnmpContextv3
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.10 $ $Date: 2009/03/05 15:51:42 $
 */
public class DefaultUsmAgent implements UsmAgent {
    static final String version_id = "@(#)$Id: DefaultUsmAgent.java,v 3.10 2009/03/05 15:51:42 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * The default name of the local host, <em>localhost</em>.
     */
    public final static String LOCAL_HOST = "localhost";

    /**
     * The default port number of the local host, <em>161</em>.
     */
    public final static int LOCAL_PORT = 161;

    private SnmpContextv3Basis context;
    private String hostname;
    private String hostaddress;
    private int port;

    public DefaultUsmAgent() {
        try {
            setAgentName(LOCAL_HOST);
        } catch (java.net.UnknownHostException exc) {
            hostname = LOCAL_HOST;
            hostaddress = "127.0.0.1";
        }
        setAgentPort(LOCAL_PORT);
    }

    /**
     * Returns the authoritative SNMP Engine ID. If the discovery failed,
     * <em>null</em> will be returned.
     *
     * @return The Engine ID
     */
    public String getSnmpEngineId() {
        TimeWindow tWindow = TimeWindow.getCurrent();
        String engineId = tWindow.getSnmpEngineId(hostaddress, port);
        return engineId;
    }

    /**
     * Returns the authoritative Engine Boots. If the discovery failed,
     * <em>1</em> will be returned.
     *
     * @return The Engine Boots
     */
    public int getSnmpEngineBoots() {
        int boots = 1;
        TimeWindowNode node = null;
        TimeWindow tWindow = TimeWindow.getCurrent();
        String engineId = tWindow.getSnmpEngineId(hostaddress, port);
        if (engineId != null) {
            node = tWindow.getTimeLine(engineId);
        }
        if (node != null) {
            boots = node.getSnmpEngineBoots();
        }
        return boots;
    }

    /**
     * Returns the authoritative Engine Time. If the discovery failed,
     * <em>1</em> will be returned.
     *
     * @return The Engine Time
     */
    public int getSnmpEngineTime() {
        int time = 1;
        TimeWindowNode node = null;
        TimeWindow tWindow = TimeWindow.getCurrent();
        String engineId = tWindow.getSnmpEngineId(hostaddress, port);
        if (engineId != null) {
            node = tWindow.getTimeLine(engineId);
        }
        if (node != null) {
            time = node.getSnmpEngineTime();
        }
        return time;
    }

    /**
     * Sets the SNMP context. It will do a discovery if needed.
     */
    public void setSnmpContext(SnmpContextv3Basis c) {
        context = c;
        try {
            discoverIfNeeded();
        } catch (PduException exc) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".setSnmpContext(): "
                        + exc.getMessage());
            }
        } catch (java.io.IOException exc) {
            if (AsnObject.debug > 4) {
                System.out.println(getClass().getName() + ".setSnmpContext(): "
                        + exc.getMessage());
            }
        }
    }

    /**
     * Sets my own hostname, i.e. the name of the agent or authoritative
     * engine. By default <em>localhost</em> is used.
     * Sets the hostaddress as well, using java.net.InetAddress.
     *
     * @see #LOCAL_HOST
     * @exception java.net.UnknownHostException Thrown when java.net.InetAddress
     *                                          cannot
     *                                          resolve the name
     */
    public void setAgentName(String host)
            throws java.net.UnknownHostException {
        hostname = host;
        java.net.InetAddress ipAddr = java.net.InetAddress.getByName(hostname);
        hostaddress = ipAddr.getHostAddress();
    }

    /**
     * Sets my own port number, i.e. the port number of the agent
     * or authoritative engine. By default <em>161</em> is used.
     *
     * @see #LOCAL_PORT
     */
    public void setAgentPort(int p) {
        port = p;
    }

    /**
     * This discovers the USM timeliness of hostname and port as set by
     * setAgentName() and setAgentPort(), but with all the other details
     * taken from the SNMPv3 context.
     *
     * @see #setAgentName
     * @see #setAgentPort
     */
    void discoverIfNeeded()
            throws java.io.IOException, PduException {
        UsmDiscoveryBean discBean = null;
        boolean isNeeded = false;

        TimeWindow tWindow = TimeWindow.getCurrent();
        String engineId = tWindow.getSnmpEngineId(hostaddress, port);
        if (engineId == null) {
            isNeeded = true;
            discBean = new UsmDiscoveryBean(hostname,
                    port, context.getBindAddress(), context.getTypeSocket());
        }

        if (context.isUseAuthentication()) {
            if (isNeeded) {
                discBean.setAuthenticationDetails(context.getUserName(),
                        context.getUserAuthenticationPassword(),
                        context.getAuthenticationProtocol());
            } else if (tWindow.isTimeLineKnown(engineId) == false) {
                isNeeded = true;
                discBean = new UsmDiscoveryBean(
                        hostname, port, context.getBindAddress(),
                        context.getTypeSocket());
                discBean.setAuthenticationDetails(context.getUserName(),
                        context.getUserAuthenticationPassword(),
                        context.getAuthenticationProtocol());
            }

            if (isNeeded && context.isUsePrivacy()) {
                discBean.setPrivacyDetails(context.getUserPrivacyPassword(),
                        context.getPrivacyProtocol());
            }
        }

        if (isNeeded) {
            discBean.startDiscovery();
            discBean.freeResources();
        }
    }

    /**
     * @since 4_14
     */
    public long getUsmStatsUnknownEngineIDs() {
        return 0;
    }

    /**
     * @since 4_14
     */
    public long getUsmStatsNotInTimeWindows() {
        return 0;
    }

}
