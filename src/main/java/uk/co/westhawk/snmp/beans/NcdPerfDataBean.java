// NAME
//      $RCSfile: NcdPerfDataBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.12 $
// CREATED
//      $Date: 2006/01/17 17:43:53 $
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
 * This bean collects information about a 
 * <a href="http://www.pc.ibm.com/networkstation/">IBM Network Computer</a>. 
 * </p>
 *
 * <p>
 * It provide the following data:
 * </p>
 * <ul>
 * <li> the speed of the Ethernet Card </li>
 * <li> the available memory in bytes </li>
 * <li> the name of the user that is logged in </li>
 * </ul>
 *
 * <p>
 * A message will be set if some of the data is not available
 * </p>
 *
 * <p>
 * This class uses the NCDware v3.2 MIB, which was very kindly provided
 * to me by <a href="http://www.ncd.com/">Network Computing Devices, inc
 * (NCD)</a>. This MIB is an extention of the older versions of the 
 * <a href="ftp://ftp.ncd.com/pub/ncd/Archive/NCD-Articles/NCD_X_Terminals/SNMP/3.3.2/mib.my">MIB</a>
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
 * @see #setUpdateInterval(int)
 * @see #setUpdateInterval(String)
 * @see SNMPBean#addPropertyChangeListener
 * @see SNMPBean#action
 * 
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.12 $ $Date: 2006/01/17 17:43:53 $
 */
public class NcdPerfDataBean extends SNMPBean
        implements PropertyChangeListener {
    private static final String version_id = "@(#)$Id: NcdPerfDataBean.java,v 1.12 2006/01/17 17:43:53 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Used as propertyName when a propertyChangeEvent is fired because
     * the speed was updated
     */
    public final static String speedPropertyName = "Speed";

    /**
     * Used as propertyName when a propertyChangeEvent is fired because
     * the memory was updated
     */
    public final static String memoryPropertyName = "Memory";

    /**
     * Used as propertyName when a propertyChangeEvent is fired because
     * the user name was updated
     */
    public final static String userPropertyName = "User";

    /**
     * Used as propertyName when a propertyChangeEvent is fired because
     * a message was set
     */
    public final static String messagePropertyName = "Message";

    /**
     * Used as name when no one is logged in.
     */
    public final static String noLogin = "no one is logged in";

    /**
     * Used as name when the name is not available.
     */
    public final static String noName = "not available";

    private ethernet ethernetData;
    private user userData;
    private memory memoryData;

    private Date lastUpdateDate = null;

    private int interval = 3000;
    private long speed = -1;
    private long memoryAvail = -1;
    private String userName = "";

    /**
     * The default constructor.
     */
    public NcdPerfDataBean() {
    }

    /**
     * Returns the speed (bits per second) of the ethernet card interface.
     * 
     * @return the speed (b/s)
     */
    public long getSpeed() {
        return speed;
    }

    /**
     * Returns the amount of RAM memory in bytes which is
     * currently available.
     *
     * @return the memory in bytes
     */
    public long getMemory() {
        return memoryAvail;
    }

    /**
     * Returns the login name of the user that is logged in.
     *
     * @return the user name
     * @see #noLogin
     * @see #noName
     */
    public String getUserName() {
        return userName;
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
     * The default will be <em>3000</em> (= 3 sec).
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
     * Returns the date of the moment when this bean was last updated.
     * This might be null when the first time the update was not finished.
     *
     * @return the last update date
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * This method starts sending the SNMP request. All properties should be
     * set before this method is called.
     *
     * The actual sending will take place in the run method.
     * It makes a new snmp context and initialises all variables before
     * starting.
     */
    public void action() {
        if (isHostPortReachable()) {
            lastUpdateDate = new Date();

            if (ethernetData != null) {
                ethernetData.setRunning(false);
            }
            if (userData != null) {
                userData.setRunning(false);
            }
            if (memoryData != null) {
                memoryData.setRunning(false);
            }
            ethernetData = new ethernet(host, port, community, interval, context);
            userData = new user(host, port, community, interval, context);
            memoryData = new memory(host, port, community, interval, context);

            ethernetData.addPropertyChangeListener(this);
            userData.addPropertyChangeListener(this);
            memoryData.addPropertyChangeListener(this);
        }
    }

    protected void setMessage(String st) {
        message = st;
        firePropertyChange(NcdPerfDataBean.messagePropertyName,
                null, message);
    }

    /**
     * This method is called when a new value of the speed, memory, user or
     * message is available. It will propagate this event to any listeners.
     *
     * @see #speedPropertyName
     * @see #memoryPropertyName
     * @see #userPropertyName
     * @see #messagePropertyName
     */
    public void propertyChange(PropertyChangeEvent e) {
        Object src = e.getSource();
        Object oldV = e.getOldValue();
        Object newV = e.getNewValue();
        String propName = e.getPropertyName();

        if (propName.equals(messagePropertyName)) {
            setMessage((String) newV);
        } else {
            if (src == ethernetData) {
                speed = ((Long) newV).longValue();
            } else if (src == userData) {
                userName = (String) newV;
            } else if (src == memoryData) {
                memoryAvail = ((Long) newV).longValue();
            }

            firePropertyChange(e.getPropertyName(), oldV, newV);
        }
    }

}

/**
 * <p>
 * This class is the super class of the classes ethernet, user and
 * memory.
 * </p>
 *
 * @see user
 * @see ethernet
 * @see memory
 */
abstract class ncdPart extends SNMPRunBean {
    Thread me;
    protected boolean isPduInFlight;

    public abstract void doPdu()
            throws PduException, IOException;

    public ncdPart(String h, int p, String c, int i, SnmpContext con) {
        // reuse the context of the NcdPrefData class, so do not call
        // isHostPortReachable() !
        context = con;
        setHost(h);
        setPort(p);
        setCommunityName(c);
        setUpdateInterval(i);

        action();
    }

    protected void setMessage(String st) {
        message = st;
        firePropertyChange(NcdPerfDataBean.messagePropertyName,
                null, message);
    }

    public void action() {
        setRunning(true);
    }

    /**
     *
     * @see SNMPRunBean#isRunning()
     */
    public void run() {
        while (context != null && isRunning()) {
            if (isPduInFlight == false) {
                isPduInFlight = true;
                try {
                    doPdu();
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

}

/**
 * <p>
 * This class collects information about the ethernet card.
 * </p>
 *
 * <p>
 * The first time it will search the interface table to find the entry
 * describing the ethernet card. After it has been found, it will ask
 * for the information to calculate the speed.
 * </p>
 *
 * <p>
 * If the ethernet card is not found, this class assumes that it is not
 * available at all, and will not try again.
 * </p>
 *
 * @see user
 * @see memory
 */
class ethernet extends ncdPart implements Observer {
    private final static int ethernetType = 6;
    private final static int statusUp = 1;

    private final static String sysUpTime = "1.3.6.1.2.1.1.3.0";
    private final static String ifIndex = "1.3.6.1.2.1.2.2.1.1";
    private final static String ifType = "1.3.6.1.2.1.2.2.1.3";
    private final static String ifOperStatus = "1.3.6.1.2.1.2.2.1.8";
    private final static String ifInOctets = "1.3.6.1.2.1.2.2.1.10";
    private final static String ifOutOctets = "1.3.6.1.2.1.2.2.1.16";

    private long speed = -1;
    private long prevSpeed = -1;
    private long prevSys = -1;
    private int prevOper = -1;
    private long prevInO = -1;
    private long prevOutO = -1;
    private int index = -1;

    GetNextPdu_vec typePdu;
    GetPdu_vec ethernetPdu;

    private boolean first = true;
    private boolean foundEthernet = false;

    public ethernet(String h, int p, String c, int i, SnmpContext con) {
        super(h, p, c, i, con);
        first = true;
        foundEthernet = false;

        speed = -1;
        prevSpeed = -1;
        prevSys = -1;
        prevOper = -1;
        prevInO = -1;
        prevOutO = -1;
    }

    public long getSpeed() {
        return speed;
    }

    public void doPdu() throws PduException, IOException {
        if (first) {
            // first go looking which interface is the ethernet one
            typePdu = new GetNextPdu_vec(context, 2);
            typePdu.addObserver(this);
            typePdu.addOid(ifIndex);
            typePdu.addOid(ifType);
            typePdu.send();

            first = false;
        } else if (foundEthernet) {
            ethernetPdu = new GetPdu_vec(context, 5);
            ethernetPdu.addObserver(this);
            ethernetPdu.addOid(sysUpTime);
            ethernetPdu.addOid(ifType + "." + index);
            ethernetPdu.addOid(ifOperStatus + "." + index);
            ethernetPdu.addOid(ifInOctets + "." + index);
            ethernetPdu.addOid(ifOutOctets + "." + index);
            ethernetPdu.send();
        }
    }

    public void update(Observable obs, Object ov) {
        varbind[] vars;

        if (obs == typePdu) {
            if (typePdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
                vars = (varbind[]) ov;
                if (vars[0].getOid().toString().startsWith(ifIndex)) {
                    int i = ((AsnInteger) vars[0].getValue()).getValue();
                    int t = ((AsnInteger) vars[1].getValue()).getValue();

                    if (t == ethernetType) {
                        // found it!
                        index = i;
                        foundEthernet = true;
                        isPduInFlight = false;
                    } else {
                        // not found it, ask for the next one
                        typePdu = new GetNextPdu_vec(context, 2);
                        typePdu.addObserver(this);
                        typePdu.addOid(vars[0]);
                        typePdu.addOid(vars[1]);
                        try {
                            typePdu.send();
                        } catch (PduException exc) {
                            System.out.println("PduException " + exc.getMessage());
                        } catch (IOException exc) {
                            System.out.println("IOException " + exc.getMessage());
                        }
                    }
                } else {
                    setMessage("Ethernet interface not available!");
                }
            } else {
                setMessage("Ethernet interface not available!");
            }
        } else {
            speed = -1;
            int oper = -1;
            if (ethernetPdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
                vars = (varbind[]) ov;

                long sys = ((AsnUnsInteger) vars[0].getValue()).getValue();
                int t = ((AsnInteger) vars[1].getValue()).getValue();
                oper = ((AsnInteger) vars[2].getValue()).getValue();
                long inO = ((AsnUnsInteger) vars[3].getValue()).getValue();
                long outO = ((AsnUnsInteger) vars[4].getValue()).getValue();

                if (t == ethernetType) {
                    if (oper == statusUp && prevOper == statusUp) {
                        long tdif = (sys - prevSys);
                        if (tdif != 0) {
                            speed = ((inO - prevInO) + (outO - prevOutO))
                                    / tdif * 100;

                            firePropertyChange(NcdPerfDataBean.speedPropertyName,
                                    new Long(prevSpeed),
                                    new Long(speed));
                        }
                    }

                    prevSys = sys;
                    prevInO = inO;
                    prevOutO = outO;
                } else {
                    // the index has changed, start searching again
                    first = true;
                    foundEthernet = false;
                }
            }
            prevSpeed = speed;
            prevOper = oper;

            isPduInFlight = false;
        }
    }

}

/**
 * <p>
 * This class collects information about the user that is logged in.
 * </p>
 *
 * <p>
 * The first time it will search the NCD environment table to find the
 * entry describing the environment variable USER. After it has been found,
 * it will ask for the information to calculate the speed.
 * </p>
 *
 * <p>
 * When it cannot be found, it will try to see the difference between
 * the situation where nobody is logged in and the situation where such
 * information will never be avaible since it does not concern a NCD.
 * </p>
 *
 * <p>
 * It is not so easy as it may seem. If the machine is shut off, it seem
 * if the machine is not a NCD.
 * </p>
 *
 * @see ethernet
 * @see memory
 */
class user extends ncdPart implements Observer {
    private final static String USER = "USER";

    private final static String ncdPrefEnvVarTableIndex = "1.3.6.1.4.1.82.2.3.15.54.1.1";
    private final static String ncdPrefEnvVarTableName = "1.3.6.1.4.1.82.2.3.15.54.1.2";
    private final static String ncdPrefEnvVarTableValue = "1.3.6.1.4.1.82.2.3.15.54.1.3";

    private int index = -1;
    private String name = "";
    private String prevName = "";

    GetNextPdu_vec namePdu;
    GetPdu_vec userPdu;

    private boolean first = true;
    private boolean isNcd = false;
    private boolean foundUser = false;

    public user(String h, int p, String c, int i, SnmpContext con) {
        super(h, p, c, i, con);
        first = true;
        isNcd = false;
        foundUser = false;

        userPdu = null;
        name = "";
        prevName = "";
    }

    public String getUserName() {
        return name;
    }

    public void doPdu() throws PduException, IOException {
        if (first
                ||
                (isNcd && !foundUser)) {
            // first go looking which interface is the ethernet one
            namePdu = new GetNextPdu_vec(context, 3);
            namePdu.addObserver(this);
            namePdu.addOid(ncdPrefEnvVarTableIndex);
            namePdu.addOid(ncdPrefEnvVarTableName);
            namePdu.addOid(ncdPrefEnvVarTableValue);
            namePdu.send();

            first = false;
        } else if (foundUser) {
            userPdu = new GetPdu_vec(context, 3);
            userPdu.addObserver(this);
            userPdu.addOid(ncdPrefEnvVarTableIndex + "." + index);
            userPdu.addOid(ncdPrefEnvVarTableName + "." + index);
            userPdu.addOid(ncdPrefEnvVarTableValue + "." + index);
            userPdu.send();
        }
    }

    public void update(Observable obs, Object ov) {
        varbind[] vars;

        if (obs == namePdu) {

            if (namePdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
                vars = (varbind[]) ov;
                if (vars[0].getOid().toString().startsWith(ncdPrefEnvVarTableIndex)) {
                    // as soon as one response concerns the NCD environment
                    // table, I assume it concerns a NCD machine.
                    isNcd = true;

                    int i = ((AsnInteger) vars[0].getValue()).getValue();
                    String n = ((AsnOctets) vars[1].getValue()).getValue();
                    String v = "";
                    if (vars[2].getValue() instanceof AsnOctets) {
                        v = ((AsnOctets) vars[2].getValue()).getValue();
                    }

                    if (n.equals(USER)) {
                        // found it!
                        index = i;
                        foundUser = true;
                        name = v;
                        isPduInFlight = false;
                    } else {
                        // not found it, ask for the next one
                        namePdu = new GetNextPdu_vec(context, 3);
                        namePdu.addObserver(this);
                        namePdu.addOid(vars[0]);
                        namePdu.addOid(vars[1]);
                        namePdu.addOid(vars[2]);
                        try {
                            namePdu.send();
                        } catch (PduException exc) {
                            System.out.println("PduException " + exc.getMessage());
                        } catch (IOException exc) {
                            System.out.println("IOException " + exc.getMessage());
                        }
                    }
                } else {
                    if (isNcd) {
                        name = NcdPerfDataBean.noLogin;
                    } else {
                        name = NcdPerfDataBean.noName;
                    }
                    if (!name.equals(prevName)) {
                        firePropertyChange(NcdPerfDataBean.userPropertyName,
                                prevName, name);
                    }

                    prevName = name;
                    isPduInFlight = false;
                }
            } else {
                prevName = name;
                isPduInFlight = false;
            }
        } else {
            name = "";
            if (userPdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
                vars = (varbind[]) ov;

                int i = ((AsnInteger) vars[0].getValue()).getValue();
                String n = ((AsnOctets) vars[1].getValue()).getValue();
                String v = "";
                if (vars[2].getValue() instanceof AsnOctets) {
                    v = ((AsnOctets) vars[2].getValue()).getValue();
                }

                name = v;
                if (!name.equals(prevName)) {
                    firePropertyChange(NcdPerfDataBean.userPropertyName,
                            prevName, name);
                }

            } else {
                first = true;
                isNcd = true;
                foundUser = false;
            }

            prevName = name;
            isPduInFlight = false;
        }
    }

}

/**
 * <p>
 * This class collects information about the available memory.
 * </p>
 *
 * <p>
 * If this information is not available, probably because it is not a
 * NCD machine, this class will not try again.
 * </p>
 *
 * @see ethernet
 * @see user
 */
class memory extends ncdPart implements Observer {
    private final static String ncdSysMemAvail = "1.3.6.1.4.1.82.2.1.1.2.0";

    GetPdu memoryPdu;
    private long memory = -1;
    private long prevMemory = -1;

    private boolean first = true;
    private boolean isAvailable = false;

    public memory(String h, int p, String c, int i, SnmpContext con) {
        super(h, p, c, i, con);

        first = true;
        isAvailable = false;
    }

    public long getMemory() {
        return memory;
    }

    public void doPdu() throws PduException, IOException {
        if (first || isAvailable) {
            memoryPdu = new GetPdu(context);
            memoryPdu.addOid(ncdSysMemAvail);
            memoryPdu.addObserver(this);
            first = false;
        }
    }

    public void update(Observable obs, Object ov) {
        varbind var;

        memory = -1;
        if (memoryPdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
            var = (varbind) ov;

            memory = ((AsnUnsInteger) var.getValue()).getValue();

            firePropertyChange(NcdPerfDataBean.memoryPropertyName,
                    new Long(prevMemory),
                    new Long(memory));

            isAvailable = true;
        } else {
            setMessage("Memory data not available!");
        }
        prevMemory = memory;
        isPduInFlight = false;
    }

}
