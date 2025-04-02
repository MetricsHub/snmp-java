// NAME
//      $RCSfile: IsHostReachableBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.14 $
// CREATED
//      $Date: 2006/01/26 12:38:59 $
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
 * This bean will determine whether the host+port is up, by sending an
 * UpSincePdu.
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
 * @see SNMPBean#addPropertyChangeListener
 * @see SNMPBean#action
 * @see UpSincePdu
 * 
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.14 $ $Date: 2006/01/26 12:38:59 $
 *
 */
public class IsHostReachableBean extends SNMPBean implements Observer {
    private static final String version_id = "@(#)$Id: IsHostReachableBean.java,v 1.14 2006/01/26 12:38:59 birgit Exp $ Copyright Westhawk Ltd";

    private UpSincePdu pdu;

    private Date upSince = null;
    private boolean isReachable = false;

    private SimpleDateFormat dateFormat;

    /**
     * The default constructor.
     */
    public IsHostReachableBean() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * The constructor that will set the host and the port no.
     *
     * @param h the hostname
     * @param p the port no
     * @see SNMPBean#setHost
     * @see SNMPBean#setPort
     */
    public IsHostReachableBean(String h, int p) {
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
    public IsHostReachableBean(String h, int p, String b) {
        this();
        setHost(h);
        setPort(p);
        setBindAddress(b);
    }

    /**
     * Returns the date when the host went up. This might be null is no
     * answer is received yet.
     * 
     * @return the date
     *
     * @see SNMPBean#getMessage()
     * @see #isReachable()
     */
    public Date getUpSinceDate() {
        return upSince;
    }

    /**
     * Answer is set according to the received SNMP response.
     * 
     * @see #getUpSinceDate()
     */
    protected void setUpSinceDate(Date d) {
        upSince = d;
    }

    /**
     * Indicates whether the host + port is reachable.
     * The host + port is reachable if a valid response is received from a
     * UpSincePdu request.
     * 
     * @return is the host + port + community name valid
     *
     * @see SNMPBean#getMessage()
     * @see #getUpSinceDate()
     */
    public boolean isReachable() {
        return isReachable;
    }

    /**
     * The reachable property is set according to the succes of making
     * a connection and the response of the SNMP request.
     * This method will fire a Property Change Event.
     *
     * @see #isReachable()
     * @see SNMPBean#addPropertyChangeListener
     */
    protected void setReachable(boolean b) {
        boolean old;
        old = isReachable;
        isReachable = b;

        firePropertyChange("Reachable", new Boolean(old),
                new Boolean(isReachable));
    }

    /**
     * This method performs the request and wait for it. It is not
     * recommended to use the waitForSelf option, preferred is the action()
     * method.
     * Use it if it is really necessary to wait.
     *
     * @see SnmpContext
     * @see #action()
     * @see Pdu#waitForSelf()
     */
    public void waitForSelfAction()
            throws PduException, IOException {
        action(true);
    }

    /**
     * This method actually performs the request. All properties should be
     * set before this method is called.
     *
     * This will create a new SnmpContext and send a UpSincePdu SNMP
     * request.
     * If the connection fails, the reachable property will be set to false.
     *
     * @see SnmpContext
     */
    public void action()
            throws PduException, IOException {
        action(false);
    }

    protected void action(boolean wait)
            throws PduException, IOException {
        if (host != null
                &&
                host.length() > 0
                &&
                port > 0) {
            try {
                if (context != null) {
                    context.destroy();
                }
                context = new SnmpContext(host, port, bindAddr, socketType);
                context.setCommunity(community);
                setMessage("Connection to host " + host
                        + " is made succesfully");

                if (wait == true) {
                    pdu = new UpSincePdu(context, null);
                    if (pdu.waitForSelf()) {
                        this.update((Observable) pdu, (Object) null);
                    } else {
                        setMessage("No SNMP Response from " + host);
                    }
                } else {
                    pdu = new UpSincePdu(context, this);
                }
            } catch (IOException exc) {
                setMessage("IOException: " + exc.getMessage());
                setReachable(false);
            } catch (RuntimeException exc) {
                setMessage("RuntimeException: " + exc.getMessage());
                setReachable(false);
            }
        }
    }

    /**
     * The update method according to the Observer interface, it will be
     * called when the Pdu response is received.
     * If there is no error it means that the host is reachable. If it is
     * not reachable the message will contain the type of error, if it is
     * reachable the message will say since when the host is up.
     */
    public void update(Observable obs, Object ov) {
        pdu = (UpSincePdu) obs;

        if (pdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
            setUpSinceDate(pdu.getDate());
            setMessage("Host " + host + " is up since "
                    + dateFormat.format(getUpSinceDate()));
            setReachable(true);
        } else {
            setUpSinceDate(null);
            setMessage("SNMP Response: " + pdu.getErrorStatusString());
            setReachable(false);
        }
    }

    /**
     * Destroys the context.
     * 
     * @since 4_14
     */
    public void freeResources() {
        if (context != null) {
            context.destroy();
            context = null;
        }
    }

}
