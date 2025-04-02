// NAME
//      $RCSfile: SNMPRunBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.9 $
// CREATED
//      $Date: 2006/02/09 14:20:09 $
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
 * This bean forms the base of the Runnable SNMP beans.
 * It extends the SNMPBean class.
 * </p>
 *
 * <p>
 * This bean is used when SNMP requests have to be sent continuously,
 * instead of just once.
 * For that purpose the update interval <em>setUpdateInterval()</em> can 
 * be set. The default value is <em>2000</em>, i.e. 2 msec.
 * </p>
 *
 * <p>
 * When implementing an extention of this class, the method
 * <em>action()</em> should 
 * be implemented. It should somehow start the run by calling
 * <em>setRunning(true)</em>.
 * Stopping the thread might be called by the application or applet.
 * </p>
 *
 * @see OneInterfaceBean
 * @see InterfaceIndexesBean
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.9 $ $Date: 2006/02/09 14:20:09 $
 */
public abstract class SNMPRunBean extends SNMPBean implements Runnable {
    private static final String version_id = "@(#)$Id: SNMPRunBean.java,v 1.9 2006/02/09 14:20:09 birgit Exp $ Copyright Westhawk Ltd";

    protected int interval = 2000;
    protected Thread me = null;
    protected boolean running = false;

    /**
     * Method according to the Runnable interface. This method should
     * provide the continuous sending of a Pdu, with a sleeping interval set
     * by setUpdateInterval().
     *
     * @see #setUpdateInterval(int)
     * @see #setRunning
     */
    public abstract void run();

    /**
     * The default constructor
     */
    public SNMPRunBean() {
    }

    /**
     * Returns the update interval. This is the interval that the
     * bean will sleep between 2 requests.
     *
     * @return the update interval in msec
     * @see #setUpdateInterval(int)
     * @see #setUpdateInterval(String)
     */
    public int getUpdateInterval() {
        return interval;
    }

    /**
     * Sets the update interval. This is the interval that the
     * bean will sleep between 2 requests.
     * The default will be <em>2000</em> (= 2 sec).
     *
     * @param i the interval in msec
     * @see #getUpdateInterval
     * @see #setUpdateInterval(String)
     */
    public void setUpdateInterval(int i) {
        if (interval != i) {
            interval = i;
        }
    }

    /**
     * Sets the update interval as String.
     *
     * @param i the interval in msec as String
     * @see #getUpdateInterval
     * @see #setUpdateInterval(int)
     */
    public void setUpdateInterval(String i) {
        int iNo;
        try {
            iNo = Integer.valueOf(i.trim()).intValue();
            setUpdateInterval(iNo);
        } catch (NumberFormatException exp) {
        }
    }

    /**
     * Returns if the bean is running.
     * 
     * @return the running mode
     * @see #setRunning
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Starts or stops the thread.
     * Starting this thread should be a result of calling the action()
     * method, so should be implemented in the action method of any child
     * class.
     *
     * Stopping this thread may be called by the application of applet.
     * This method does NOT call Thread.stop() anymore. Every run() method
     * should check isRunning(), so that when setRunning(false) is called,
     * the run() method will stop running!!
     *
     * @see #isRunning
     * @see SNMPBean#action()
     */
    public synchronized void setRunning(boolean b) {
        if (running != b) {
            running = b;

            if (running) {
                if (me == null) {
                    me = new Thread(this);
                    me.setPriority(Thread.MIN_PRIORITY);
                }
                me.start();
            }
        }
    }

}
