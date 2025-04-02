// NAME
//      $RCSfile: Transmitter.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.15 $
// CREATED
//      $Date: 2007/02/05 15:01:20 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1996 - 1998 by Westhawk Ltd (www.westhawk.nl)
 * Copyright (C) 1998 - 2006 by Westhawk Ltd 
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

import java.util.*;

/**
 * Transmitter is a thread that sends PDUs, when done
 * it waits.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.15 $ $Date: 2007/02/05 15:01:20 $
*/
class Transmitter extends Object implements Runnable {
    private static final String version_id = "@(#)$Id: Transmitter.java,v 3.15 2007/02/05 15:01:20 birgita Exp $ Copyright Westhawk Ltd";

    Pdu pdu = null;
    Thread me;
    String myName;

    Transmitter(String name) {
        me = new Thread(this, name);
        me.setPriority(me.MIN_PRIORITY);
        if (AsnObject.debug > 12) {
            System.out.println("Transmitter(): Made thread " + name);
        }
        myName = name;
        me.start();
    }

    /**
     * The method for the Runnable interface.
     * 
     * It will do a sit() untill stand() wakes it up. The Pdu will then
     * be transmitted
     *
     * @see #sit()
     * @see #stand()
     * @see Pdu#transmit()
     */
    public void run() {
        while (me != null) {
            sit();
            synchronized (this) {
                if (pdu != null) {
                    pdu.transmit();
                    // I will say this only once....
                    pdu = null;
                }
            }
        }
    }

    /**
     * Returns the string representation of the Transmitter.
     *
     * @return The string of the Transmitter
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append("[");
        buffer.append("name=").append(myName);
        buffer.append("]");
        return buffer.toString();
    }

    synchronized void setPdu(Pdu p) {
        pdu = p;
    }

    /**
     * This method is the counterpart of stand().
     *
     * It does not more than waiting to be notified.
     *
     * @see #stand()
     * @see #run()
     */
    synchronized void sit() {
        while ((me != null) && (pdu == null)) {
            try {
                wait();
            } catch (InterruptedException iw) {
                ;
            }

        }
    }

    /**
     * This method is the counterpart of sit().
     *
     * The Pdu will call this method when it is sent.
     * This method will notify itself and in the end it is transmitted
     * in run.
     *
     * @see #sit()
     * @see #run()
     * @see Pdu#send
     */
    synchronized void stand() {
        notifyAll();
    }

    /**
     * It may be sleeping (as opposed to wait()ing
     * so send it a kick.
     */
    void interruptMe() {
        // unsafe ?
        if (me != null) {
            me.interrupt();
        }
    }

    void destroy() {
        me = null;
        pdu = null;
        stand();
    }
}
