// NAME
//      $RCSfile: RawPduReceivedSupport.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.5 $
// CREATED
//      $Date: 2006/02/09 14:30:18 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2005 - 2006 by Westhawk Ltd
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
package uk.co.westhawk.snmp.event;

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
import uk.co.westhawk.snmp.stack.*;

/**
 * This is a utility class that can be used by classes that support 
 * raw pdu listener functionality. 
 * You can use an instance of this class as a member field
 * of your class and delegate various work to it. 
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.5 $ $Date: 2006/02/09 14:30:18 $
 */
public class RawPduReceivedSupport {
    public static final String version_id = "@(#)$Id: RawPduReceivedSupport.java,v 1.5 2006/02/09 14:30:18 birgit Exp $ Copyright Westhawk Ltd";

    private Object source;
    private transient Vector pduListeners;

    /**
     * The constructor.
     *
     * @param src The source (ListeningContext) of the pdu events when they are
     *            fired.
     */
    public RawPduReceivedSupport(Object src) {
        source = src;
    }

    /**
     * Removes all the listeners.
     */
    public synchronized void empty() {
        if (pduListeners != null) {
            pduListeners.removeAllElements();
        }
    }

    /**
     * Returns the number of listeners.
     *
     * @return The number of listeners.
     */
    public synchronized int getListenerCount() {
        int c = 0;
        if (pduListeners != null) {
            c = pduListeners.size();
        }
        return c;
    }

    /**
     * Adds the specified pdu listener to receive pdus.
     */
    public synchronized void addRawPduListener(RawPduListener listener) {
        if (pduListeners == null) {
            pduListeners = new Vector(5);
        }
        if (pduListeners.contains(listener) == false) {
            pduListeners.addElement(listener);
        }
    }

    /**
     * Removes the specified pdu listener.
     */
    public synchronized void removeRawPduListener(RawPduListener listener) {
        if (pduListeners != null) {
            pduListeners.removeElement(listener);
        }
    }

    /**
     * Fires an undecoded pdu event.
     * The event is fired to all listeners, unless one of them consumes it.
     * The idea is that for undecoded pdus it is very unlikely that more
     * than one party (usually SnmpContext objects) is interested.
     *
     * @param version     The SNMP version of the pdu
     * @param hostAddress The IP address of the host where the pdu came from
     * @param hostPort    The remote port number of the host where the pdu came from
     * @param message     The pdu in bytes
     *
     * @return Whether or not the event has been consumed.
     */
    public boolean fireRawPduReceived(int version, String hostAddress, int hostPort, byte[] message) {
        boolean isConsumed = false;
        Vector copyOfListeners = null;
        if (pduListeners != null) {
            synchronized (pduListeners) {
                copyOfListeners = (Vector) pduListeners.clone();
            }
        }

        if (copyOfListeners != null) {
            int l = message.length;
            int sz = copyOfListeners.size();
            for (int i = sz - 1; i >= 0 && isConsumed == false; i--) {
                RawPduListener listener = (RawPduListener) copyOfListeners.elementAt(i);

                byte[] copyOfMessage = new byte[l];
                System.arraycopy(message, 0, copyOfMessage, 0, l);
                RawPduEvent evt = new RawPduEvent(source, version, hostAddress, copyOfMessage, hostPort);
                listener.rawPduReceived(evt);
                isConsumed = (evt.isConsumed());
            }
        }
        return isConsumed;
    }

}
