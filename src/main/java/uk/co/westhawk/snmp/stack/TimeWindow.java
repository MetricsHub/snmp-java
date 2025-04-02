// NAME
//      $RCSfile: TimeWindow.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.15 $
// CREATED
//      $Date: 2007/04/12 12:55:28 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2000 - 2006 by Westhawk Ltd
 * <a href="www.westhawk.co.uk">www.westhawk.co.uk</a>
 *
 * Permission to use, copy, modify, and distribute this software
 * for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that
 * both the copyright notice and this permission notice appear in
 * supporting documentation.
 * This software is provided "as is" without express or implied
 * warranty.
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
 * TimeWindow contains the lookup tables for the engine Id information.
 * TimeWindow should be created only once. Use the 
 * <code>getCurrent()</code> 
 * method to access any other method, i.e. 
 * <pre>
 * if (TimeWindow.getCurrent() == null)
 * {
 *     TimeWindow timew = new TimeWindow();
 * }
 * boolean known = TimeWindow.getCurrent().isSnmpEngineIdKnown(hostaddr, port);
 * </pre>
 *
 * <p>
 * This class contains two lookup tables. One that maps the
 * host address+port onto the SNMP engine ID and one that keeps the SNMP
 * engine ID with the timeline details about this engine.
 * </p>
 *
 * @see #getCurrent()
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.15 $ $Date: 2007/04/12 12:55:28 $
 */
public class TimeWindow {
    private static final String version_id = "@(#)$Id: TimeWindow.java,v 3.15 2007/04/12 12:55:28 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * The maximum number of seconds the engine time in the PDU is allowed
     * to differ from my estimated engine time. Values <code>150</code>.
     */
    public final static int MaxTimeDifference = 150;

    private static TimeWindow current = null;

    // lookup table hostaddr:port -> engine id
    private Hashtable hostLookup;

    // lookup table engine id -> TimeWindowNode
    private Hashtable engineLookup;

    private long startTime;

    /**
     * Constructor.
     */
    public TimeWindow() {
        if (current == null) {
            current = this;

            hostLookup = new Hashtable(5);
            engineLookup = new Hashtable(5);
        }
    }

    /**
     * Returns the current reference to this class.
     * TimeWindow should be created only once. Use this method
     * to access any other method, i.e.
     * 
     * <pre>
     * if (TimeWindow.getCurrent() == null) {
     *     TimeWindow timew = new TimeWindow();
     * }
     * boolean known = TimeWindow.getCurrent().isSnmpEngineIdKnown(hostaddr, port);
     * </pre>
     * 
     * @return the current time window
     */
    public static TimeWindow getCurrent() {
        return current;
    }

    /**
     * Returns the snmp engine ID. This method will lookup the engine ID
     * based on the host address and port. If it the engine ID is not known, null
     * will be returned.
     *
     * @param hostaddr The host address of the engine ID
     * @param port     The port number of the engine ID
     * @return the snmp engine ID
     * @see #isSnmpEngineIdKnown(String, int)
     */
    public String getSnmpEngineId(String hostaddr, int port) {
        String key = getKey(hostaddr, port);
        String snmpEngineId = (String) hostLookup.get(key);
        return snmpEngineId;
    }

    /**
     * Returns if the snmp engine ID is known. This method will lookup if the
     * engine ID belonging to this hostaddr and port is known.
     * <p>
     * When the SNMP engine ID is known, this doesn't necessarily mean that
     * the timeline details of this engine ID are known, since it takes a
     * second discovery step to find out.
     * </p>
     *
     * @param hostaddr The host address of the engine ID
     * @param port     The port number of the engine ID
     * @return whether the snmp engine ID is known
     */
    public boolean isSnmpEngineIdKnown(String hostaddr, int port) {
        String key = getKey(hostaddr, port);
        return hostLookup.containsKey(key);
    }

    /**
     * Sets the SNMP engine ID that belongs to the specified hostaddr and port.
     * The old SNMP engine ID (if any) will be overwritten.
     *
     * @param hostaddr     The host address of the engine ID
     * @param port         The port number of the engine ID
     * @param snmpEngineId The engine ID
     */
    public void setSnmpEngineId(String hostaddr, int port, String snmpEngineId) {
        String key = getKey(hostaddr, port);
        if (AsnObject.debug > 4) {
            System.out.println();
            System.out.println(getClass().getName() + ".setSnmpEngineId(): hostaddr '"
                    + hostaddr + "', port '" + port
                    + "', snmpEngineId '" + snmpEngineId
                    + "', key '" + key + "'");
        }
        hostLookup.put(key, snmpEngineId);
    }

    /**
     * Checks if the engine ID is OK. If there is no engine ID known for
     * this hostaddr and port, the specified engine ID is added to the table.
     * <p>
     * If there is
     * already an engine ID for this hostaddr and port, the method returns true
     * if the specified engine ID is the same as the existing one, and false if
     * they differ. In the latter case the engine ID in the table is not updated.
     * </p>
     *
     * @param hostaddr     The host address of the engine ID
     * @param port         The port number of the engine ID
     * @param snmpEngineId The engine ID
     * @return whether the engine ID matches the stored engine ID
     *
     * @see #setSnmpEngineId(String, int, String)
     */
    public boolean isEngineIdOK(String hostaddr, int port, String snmpEngineId) {
        boolean ok = true;
        String key = getKey(hostaddr, port);
        if (hostLookup.containsKey(key) == false) {
            setSnmpEngineId(hostaddr, port, snmpEngineId);
        } else {
            String myEngineId = getSnmpEngineId(hostaddr, port);
            if (myEngineId.equalsIgnoreCase(snmpEngineId) == false) {
                ok = false;
            }
        }

        if (AsnObject.debug > 4) {
            System.out.println();
            System.out.println(getClass().getName() + ".isEngineIdOK(): hostaddr '"
                    + hostaddr + "', port '" + port
                    + "', snmpEngineId '" + snmpEngineId
                    + "', ok " + ok);
        }
        return ok;
    }

    /**
     * Returns if the timeline details of this snmp engine ID are known.
     *
     * @param snmpEngineId The engine ID
     * @return whether the timeline details are known
     */
    public boolean isTimeLineKnown(String snmpEngineId) {
        return engineLookup.containsKey(snmpEngineId);
    }

    /**
     * Returns if the time details are outside the time window.
     * When a response or report is received, the stack first checks the time
     * window before updating it. It always does an update afterwards, even if
     * the message was outside the time window!
     *
     * @param snmpEngineId The SNMP engine ID
     * @param bootsA       The SNMP engine boots
     * @param timeA        The SNMP engine time
     * @return true if outside or when no details can be found, false if
     *         inside time window
     * @see #updateTimeWindow(String, int, int, boolean)
     */
    public boolean isOutsideTimeWindow(String snmpEngineId, int bootsA,
            int timeA) {
        boolean isOut = false;

        TimeWindowNode node = getTimeLine(snmpEngineId);
        if (node != null) {
            int bootsL = node.getSnmpEngineBoots();
            int timeL = node.getSnmpEngineTime();
            if (bootsA == TimeWindowNode.maxTime
                    ||
                    bootsA < bootsL
                    ||
                    (bootsA == bootsL && timeA < (timeL - MaxTimeDifference))) {
                isOut = true;
            }
        } else {
            // We don't have any info, so by definition it is not out.
            isOut = false;
        }
        return isOut;
    }

    /**
     * Tries to update the time window and returns if succeeded.
     * When a response or report is received, first check the time window
     * before updating it.
     *
     * <p>
     * An update will only occur if the message was authentic
     * and the bootsA and timeA meet the requirements.
     * New data will be inserted if the (bootsA &gt; 0), irrespectively
     * whether the message was authentic or not.
     * </p>
     *
     * @param snmpEngineId The SNMP engine ID
     * @param bootsA       The SNMP engine boots
     * @param timeA        The SNMP engine time
     * @return true if update succeeded, or false when not succeeded or when
     *         no details could be found.
     * @see #isOutsideTimeWindow(String, int, int)
     */
    public boolean updateTimeWindow(String snmpEngineId, int bootsA, int timeA, boolean isAuthentic) {
        boolean updated = false;

        TimeWindowNode node = getTimeLine(snmpEngineId);
        if (node != null) {
            if (isAuthentic) {
                int bootsL = node.getSnmpEngineBoots();
                int latestL = node.getLatestReceivedEngineTime();

                if (bootsA > bootsL
                        ||
                        (bootsA == bootsL && timeA > latestL)) {
                    synchronized (this) {
                        node.setSnmpEngineBoots(bootsA);
                        node.setSnmpEngineTime(timeA);
                        updated = true;
                    }
                }
            }
        } else if (bootsA > 0 || timeA > 0) {
            // @since 5_2, initially it didn't save when bootsA equals zero
            node = new TimeWindowNode(snmpEngineId, bootsA, timeA);
            setTimeLine(snmpEngineId, node);
        }

        if (AsnObject.debug > 4) {
            System.out.println();
            System.out.println(getClass().getName() + ".updateTimeWindow(): snmpEngineId '"
                    + snmpEngineId
                    + "', bootsA " + bootsA
                    + ", timeA " + timeA
                    + ", isAuthentic " + isAuthentic
                    + ", updated " + updated);
        }
        return updated;
    }

    /**
     * Clear all timing information for the given engine ID.
     *
     * This stinks, but occasionally the router's time window will
     * slip outside of the acceptable window or will reboot without
     * updating its "reboots" parameter. If you care more about
     * security than functionality then never ever use this.
     * Added on request of Steve A Cochran (steve@more.net).
     * 
     * @param snmpEngineId The engine to clear
     *
     * @since 5_2
     */
    public void clearTimeWindow(String snmpEngineId) {
        if (engineLookup.containsKey(snmpEngineId)) {
            // Remove from engine lookup table
            engineLookup.remove(snmpEngineId);

            // Remove any entries in the hostLookup table that point to
            // this snmpEngineId
            Vector v = new Vector();
            Iterator i = hostLookup.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) (i.next());
                if ((hostLookup.get(key)).equals(snmpEngineId)) {
                    v.add(key);
                }
            }
            i = v.iterator();
            while (i.hasNext()) {
                hostLookup.remove(i.next());
            }
        }
    }

    /**
     * Updates the estimated engine time of all gathered time details.
     * It calculates the seconds that passed since the last call and
     * updates all time window nodes accordingly.
     *
     * @see #setTimeLine(String, TimeWindowNode)
     * @see #getTimeLine(String)
     */
    protected void updateTimeWindows() {
        if (engineLookup.size() > 0) {
            long now = System.currentTimeMillis();
            long milli = now - startTime;
            int sec = (int) (milli / 1000L);

            long lostMillis = milli - (sec * 1000L);
            if (lostMillis < 0L) {
                lostMillis = 0L;
            }
            startTime = now - lostMillis;

            Enumeration nodes = engineLookup.elements();
            while (nodes.hasMoreElements()) {
                TimeWindowNode node = (TimeWindowNode) nodes.nextElement();
                node.incrementSnmpEngineTime(sec);
            }
        } else {
            startTime = System.currentTimeMillis();
        }
    }

    /**
     * Returns the key to the engine ID lookup table, based on the specified
     * host address and port.
     *
     * @param hostaddr The host address
     * @param port     The port
     * @return the key
     */
    protected String getKey(String hostaddr, int port) {
        return hostaddr + ":" + port;
    }

    /**
     * Returns the timeline details of the snmp engine ID.
     * If there are no matching timeline details for this engine ID, null
     * will be returned.
     * The timeline details will be updated before the node is retrieved
     * from the table.
     *
     * @param snmpEngineId The engine ID
     * @return The timeline details
     * @see #updateTimeWindows()
     */
    protected TimeWindowNode getTimeLine(String snmpEngineId) {
        updateTimeWindows();
        TimeWindowNode node = (TimeWindowNode) engineLookup.get(snmpEngineId);
        return node;
    }

    /**
     * Sets the timeline details of the snmp engine ID.
     * The timeline details will be updated before the node is put
     * in the table.
     *
     * @param snmpEngineId The engine ID
     * @param newNode      The added time window node node
     * @return The timeline details
     * @see #updateTimeWindows()
     */
    protected TimeWindowNode setTimeLine(String snmpEngineId,
            TimeWindowNode newNode) {
        updateTimeWindows();
        engineLookup.put(snmpEngineId, newNode);
        if (AsnObject.debug > 4) {
            System.out.println();
            System.out.println(getClass().getName() + ".setTimeLine(): snmpEngineId "
                    + snmpEngineId
                    + ", node " + newNode);
        }
        return newNode;
    }

    /**
     * Returns the string representation.
     *
     * @since 4_14
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(this.getClass().getName());
        buffer.append("[");

        Enumeration enum1 = hostLookup.keys();
        while (enum1.hasMoreElements()) {
            String key = (String) enum1.nextElement();
            String snmpEngineId = (String) hostLookup.get(key);
            TimeWindowNode node = (TimeWindowNode) engineLookup.get(snmpEngineId);
            buffer.append("\n\t(");
            if (node == null) {
                buffer.append("key=").append(key);
                buffer.append(", engineId=").append(snmpEngineId);
            } else {
                buffer.append("key=").append(key).append(", ");
                buffer.append(node.toString());
            }
            buffer.append(") ");
        }

        buffer.append("]");
        return buffer.toString();
    }

}
