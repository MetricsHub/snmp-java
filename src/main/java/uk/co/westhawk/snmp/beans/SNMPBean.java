// NAME
//      $RCSfile: SNMPBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.12 $
// CREATED
//      $Date: 2006/01/26 12:38:30 $
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
 * This bean forms the base of the SNMP beans.
 * </p>
 *
 * <p>
 * It provides the setting of the properties that are always needed when
 * sending a SNMP request:
 * </p>
 * <ul>
 * <li>host, default is <em>localhost</em></li>
 * <li>port, default is <em>161</em></li>
 * <li>community name, default is <em>public</em></li>
 * <li>bindAddr, default is <em>null</em></li>
 * <li>socket type, default is <em>Standard</em></li>
 * </ul>
 *
 * <p>
 * The bean will only come into action when the method <em>action()</em> is
 * called. This should be done after the properties have been set.
 * </p>
 *
 * <p>
 * This bean also provide the methods for adding and removing property
 * change listeners and firing property change events.
 * </p>
 *
 * <p>
 * Note: This bean will create a socket. This might result in a
 * SecurityException because of the Java security policy.
 * </p>
 * <ul>
 * <li>
 * If you are an applet: you're only connect back to its source web server
 * </li>
 * <li>
 * If you are an application or servlet: java allows you to send it. The 
 * host might refuse your connection.
 * </li>
 * </ul>
 *
 * <p>
 * The SNMP request will only succeed if:
 * </p>
 * <ul>
 * <li>
 * java security policy allows the opening of the socket
 * </li>
 * <li>
 * the host/port combination is reachable from your location (you can
 * check that with the ping (command line) command. 
 * </li>
 * <li>
 * the combination of OID and community name is correct
 * </li>
 * </ul>
 *
 * @see SNMPRunBean
 * @see IsHostReachableBean
 * @see OneInterfaceBean
 * @see InterfaceIndexesBean
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.12 $ $Date: 2006/01/26 12:38:30 $
 *
 */
public abstract class SNMPBean {
    private static final String version_id = "@(#)$Id: SNMPBean.java,v 1.12 2006/01/26 12:38:30 birgit Exp $ Copyright Westhawk Ltd";

    protected SnmpContext context = null;

    protected String host = "localhost";
    protected String bindAddr = null;
    protected int port = 161;
    protected String community = "public";
    protected String socketType = SnmpContextBasisFace.STANDARD_SOCKET;
    protected String message = "";

    protected Vector propertyChangeListener = null;

    /**
     * This method should send the SNMP request. All properties should be
     * set before this method is called.
     */
    public abstract void action()
            throws PduException, IOException;

    /**
     * The default constructor.
     */
    public SNMPBean() {
        propertyChangeListener = new Vector();
    }

    /**
     * The constructor that will set the host and the port no.
     *
     * @param h the hostname
     * @param p the port no
     * @see #setHost
     * @see #setPort
     */
    public SNMPBean(String h, int p) {
        this(h, p, null, SnmpContextBasisFace.STANDARD_SOCKET);
    }

    /**
     * The constructor that will set the host, the port no and the local
     * bind address.
     *
     * @param h the hostname
     * @param p the port no
     * @param b the local bind address
     * @param t the socket type
     * @see #setHost
     * @see #setPort
     * @see #setBindAddress
     * @see #setSocketType
     *
     * @since 4_14
     */
    public SNMPBean(String h, int p, String b, String t) {
        this();
        setHost(h);
        setPort(p);
        setBindAddress(b);
        setSocketType(t);
    }

    /**
     * Returns the host (name or ipadress) which is addressed. This is the
     * host where the SNMP server is running.
     * 
     * @return the name of the host
     * @see #setHost
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host (name or ipadress) which will be asked for the SNMP request.
     * This is the host where the SNMP server is running.
     * The default is <em>localhost</em>.
     *
     * This bean will only start the request when action is called.
     *
     * @see #getHost
     */
    public void setHost(String h) {
        if (h != null && h.length() > 0 && !host.equals(h)) {
            host = h;
        }
    }

    /**
     * Returns the port no which is used. That is the no of the port on the
     * host where the SNMP server is running.
     * 
     * @see #setPort(int)
     * @see #setPort(String)
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port no which is used. That should be the no of the port on the
     * host where the SNMP server is running.
     * The default is <em>161</em>.
     *
     * This bean will only start the request when action is called.
     *
     * @param p The number of the port
     * @see #getPort
     * @see #setPort(String)
     */
    public void setPort(int p) {
        if (p > 0 && port != p) {
            port = p;
        }
    }

    /**
     * Sets the port no which is used as a String.
     *
     * @param p The number of the port as a String
     * @see #getPort
     * @see #setPort(int)
     */
    public void setPort(String p) {
        int pNo;
        try {
            pNo = Integer.valueOf(p.trim()).intValue();
            setPort(pNo);
        } catch (NumberFormatException exp) {
        }
    }

    /**
     * Returns the community name that is used to send the SNMP request.
     * 
     * @see #setCommunityName
     */
    public String getCommunityName() {
        return community;
    }

    /**
     * Sets the community name that is used to send the SNMP request. The
     * default is <em>public</em>.
     * 
     * @see #getCommunityName
     */
    public void setCommunityName(String c) {
        if (c != null & !community.equals(c)) {
            community = c;
            if (context != null) {
                context.setCommunity(community);
            }
        }
    }

    /**
     * Returns the local bind address.
     * 
     * @return the name of the local bind address
     * @see #setBindAddress
     * @since 4_14
     */
    public String getBindAddress() {
        return bindAddr;
    }

    /**
     * Sets the local bind address.
     * The default is <em>null</em>.
     *
     * This bean will only start the request when action is called.
     *
     * @see #getBindAddress
     * @since 4_14
     */
    public void setBindAddress(String b) {
        bindAddr = b;
    }

    /**
     * Returns the socket type.
     * 
     * @return the socket type.
     * @see #setSocketType
     * @since 4_14
     */
    public String getSocketType() {
        return socketType;
    }

    /**
     * Sets socket type.
     * The default is <em>null</em>.
     *
     * This bean will only start the request when action is called.
     *
     * @see #getSocketType
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @since 4_14
     */
    public void setSocketType(String t) {
        socketType = t;
    }

    /**
     * Indicates whether the host is reachable.
     * This method will try to make a new SnmpContext, if this works, it is
     * reachable.
     * If it does not work, the message will be set.
     *
     * @see SnmpContext
     */
    protected boolean isHostPortReachable() {
        boolean res = true;
        if (host != null
                &&
                host.length() > 0
                &&
                port > 0) {
            try {
                if (context != null) {
                    context.destroy();
                    context = null;
                }

                context = new SnmpContext(host, port, bindAddr, socketType);
                context.setCommunity(community);
                setMessage("Connection to host " + host
                        + " is made succesfully");
            } catch (IOException exc) {
                res = false;
                setMessage("IOException: " + exc.getMessage());
            } catch (RuntimeException exc) {
                res = false;
                setMessage("RuntimeException: " + exc.getMessage());
            }
        } else {
            res = false;
        }
        return res;
    }

    /**
     * Returns the message (if any). The message is used to give the user
     * feedback if anything is happened with the request or the connection.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the message (if any). The message is used to give the user
     * feedback if anything is happened (usually when something went wrong)
     * with the connection.
     *
     * @param st the message string
     * @see #getMessage()
     */
    protected void setMessage(String st) {
        message = st;
    }

    /**
     * Add a property change listener.
     * 
     * @see #removePropertyChangeListener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeListener.addElement(l);
    }

    /**
     * Remove a property change listener.
     * 
     * @see #addPropertyChangeListener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeListener.removeElement(l);
    }

    /**
     * Fire the property event.
     *
     * @see #removePropertyChangeListener
     * @see #addPropertyChangeListener
     * @see PropertyChangeEvent
     * @see PropertyChangeListener
     */
    protected void firePropertyChange(String property, Object old_v, Object new_v) {
        Vector listeners;
        synchronized (this) {
            listeners = (Vector) propertyChangeListener.clone();
        }

        PropertyChangeEvent event = new PropertyChangeEvent(this,
                property, old_v, new_v);

        int sz = listeners.size();
        for (int i = 0; i < sz; i++) {
            PropertyChangeListener l = (PropertyChangeListener) listeners.elementAt(i);
            l.propertyChange(event);
        }
    }

}
