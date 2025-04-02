// NAME
//      $RCSfile: InterfaceIndexesBean.java,v $
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
 * This bean collects information about the number of interfaces on a
 * system and their index.
 * </p>
 *
 * <p>
 * The number is the number of network interfaces (regardless of
 * their current state) present on this system. The number 
 * will remain the same untill the next time the SNMP server is
 * restarted. The interfaces may go up and down while running.
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
 * @see InterfaceGetNextPdu
 * 
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.13 $ $Date: 2006/01/25 18:08:55 $
 *
 */
public class InterfaceIndexesBean extends SNMPRunBean implements Observer {
    private static final String version_id = "@(#)$Id: InterfaceIndexesBean.java,v 1.13 2006/01/25 18:08:55 birgit Exp $ Copyright Westhawk Ltd";

    private InterfaceGetNextPdu pdu;
    private Hashtable interfaceHash;

    private boolean isGetNextInFlight;
    private Date lastUpdateDate = null;

    /**
     * The default constructor.
     */
    public InterfaceIndexesBean() {
        interfaceHash = new Hashtable();
    }

    /**
     * The constructor that will set the host and the port no.
     *
     * @param h the hostname
     * @param p the port no
     * @see SNMPBean#setHost
     * @see SNMPBean#setPort
     */
    public InterfaceIndexesBean(String h, int p) {
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
    public InterfaceIndexesBean(String h, int p, String b) {
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
     * Returns the indexes (as Strings) of the current interfaces (the list
     * of ifIndex).
     */
    public Enumeration getInterfaceIndexes() {
        return interfaceHash.keys();
    }

    /**
     * Returns the indexes (as Strings) of the current interfaces (the list
     * of ifIndex).
     *
     * @since 4_14
     */
    public Set getInterfaceIndexSet() {
        return interfaceHash.keySet();
    }

    /**
     * Returns the number of current interfaces.
     */
    public synchronized int getInterfaceCount() {
        return interfaceHash.size();
    }

    /**
     * This method starts the action of the bean. It will initialises
     * all variables before starting.
     */
    public void action() {
        if (isHostPortReachable()) {
            interfaceHash.clear();
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
                pdu = new InterfaceGetNextPdu(context);
                pdu.addObserver(this);
                pdu.addOids(null);
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
        InterfaceGetNextPdu prev;
        String hashKey;

        pdu = (InterfaceGetNextPdu) obs;
        if (pdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
            hashKey = String.valueOf(pdu.getIfIndex());
            if ((prev = (InterfaceGetNextPdu) interfaceHash.get(hashKey)) != null) {
                pdu.getSpeed(prev);
            }

            // update the hashtable with the new answer
            interfaceHash.put(hashKey, pdu);

            // perform the GetNext on the just received answer
            prev = pdu;
            pdu = new InterfaceGetNextPdu(context);
            pdu.addObserver(this);
            pdu.addOids(prev);
            try {
                pdu.send();
            } catch (PduException exc) {
                System.out.println("PduException " + exc.getMessage());
            } catch (IOException exc) {
                System.out.println("IOException " + exc.getMessage());
            }
        } else {
            // the GetNext loop has ended
            lastUpdateDate = new Date();
            isGetNextInFlight = false;
            firePropertyChange("InterfaceIndexes", null, null);
        }
    }

}
