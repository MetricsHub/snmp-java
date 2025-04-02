// NAME
//      $RCSfile: InterfacePdu.java,v $
// DESCRIPTION
//      [given below inOctet javadoc format]
// DELTA
//      $Revision: 3.19 $
// CREATED
//      $Date: 2006/11/29 16:12:50 $
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
 
package uk.co.westhawk.snmp.pdu;

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
import java.util.*;


/**
 * The InterfacePdu class asks one interface for information, useful for admin
 * purposes.
 * See <a href="http://www.ietf.org/rfc/rfc2863.txt">IF-MIB</a>.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.19 $ $Date: 2006/11/29 16:12:50 $
 * @see InterfacesPdu
 *
 */
public class InterfacePdu extends GetPdu {
    private static final String version_id = "@(#)$Id: InterfacePdu.java,v 3.19 2006/11/29 16:12:50 birgit Exp $ Copyright Westhawk Ltd";

    // see rfc 2863

    /**
     * ifNumber -
     * The number of network interfaces (regardless of their current state)
     * present on this system.
     */
    final static String IFNUMBER = "1.3.6.1.2.1.2.1.0";

    /**
     * sysUpTime -
     * The time (in hundredths of a second) since the network management
     * portion of the system was last re-initialized.
     */
    final static String SYS_UPTIME = "1.3.6.1.2.1.1.3";

    /**
     * ifDescr -
     * A textual string containing information about the
     * interface. This string should include the name of
     * the manufacturer, the product name and the version
     * of the hardware interface.
     */
    final static String DESCR = "1.3.6.1.2.1.2.2.1.2";

    /**
     * ifOperStatus -
     * The current operational state of the interface.
     * The testing(3) state indicates that no operational
     * packets can be passed.
     */
    final static String OPR_STATUS = "1.3.6.1.2.1.2.2.1.8";

    /**
     * ifInOctets -
     * The total number of octets received on the
     * interface, including framing characters.
     */
    final static String IN_OCTETS = "1.3.6.1.2.1.2.2.1.10";

    /**
     * ifOutOctets -
     * The total number of octets transmitted outOctets of the
     * interface, including framing characters.
     */
    final static String OUT_OCTETS = "1.3.6.1.2.1.2.2.1.16";

    /**
     * The current operational state is up
     */
    final public static String UP = "up";
    /**
     * The current operational state is down
     */
    final public static String DOWN = "down";
    /**
     * The current operational state is testing
     */
    final public static String TESTING = "testing";
    /**
     * The current operational state is unknown
     */
    final public static String UNKNOWN = "unknown";

    long sysUpTime;
    int operStatus;
    long inOctet;
    long outOctet;
    long speed;
    int index;
    String descr;
    boolean valid = false;

    /**
     * Constructor.
     * It permits this package to create this PDU without data.
     *
     * @param con The context of the request
     */
    InterfacePdu(SnmpContextBasisFace con) {
        super(con);
    }

    /**
     * Constructor that will send the request immediately.
     *
     * @param con    the SnmpContextBasisFace
     * @param o      the Observer that will be notified when the answer is received
     * @param interf the index of the requested interface
     */
    public InterfacePdu(SnmpContextBasisFace con, Observer o, int interf)
            throws PduException, java.io.IOException {
        super(con);

        addOids(interf);
        if (o != null) {
            addObserver(o);
        }
        index = interf;
        send();
    }

    /**
     * Returns the index of the interface.
     * 
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the time (in hundredths of a second) since the network management
     * portion of the system was last re-initialized.
     */
    public long getSysUpTime() {
        return sysUpTime;
    }

    /**
     * Returns the description of the interface.
     * 
     * @return the description
     */
    public String getDescription() {
        return descr;
    }

    /**
     * Returns the operational state of the interface.
     * 
     * @return the operational state
     */
    public int getOperStatus() {
        return operStatus;
    }

    /**
     * Returns the string representation of the operational state of the
     * interface.
     * 
     * @return the operational state as string
     * @see #getOperStatus()
     * @see #getOperStatusString(int)
     */
    public String getOperStatusString() {
        return getOperStatusString(operStatus);
    }

    /**
     * Returns the string representation of a operational state.
     * 
     * @see #getOperStatusString()
     */
    public String getOperStatusString(int status) {
        String str = null;
        switch (status) {
            case 1:
                str = UP;
                break;
            case 2:
                str = DOWN;
                break;
            case 3:
                str = TESTING;
                break;
            default:
                str = UNKNOWN;
        }
        return str;
    }

    /**
     * Returns the total number of octets received on the
     * interface, including framing characters.
     */
    public long getInOctet() {
        return inOctet;
    }

    /**
     * Returns the total number of octets transmitted outOctets of the
     * interface, including framing characters.
     */
    public long getOutOctet() {
        return outOctet;
    }

    /**
     * Calculates the speed of the interface. This is done by providing the
     * method with <i>the previous value of this interface</i>. An interface
     * is marked by its index.
     *
     * @param old The previous value of this interface
     */
    public long getSpeed(InterfacePdu old) {
        long speed = -1;
        if ((this.operStatus < 1) || (old.operStatus < 1)
                ||
                !this.valid || !old.valid) {
            return -1;
        }
        long tdif = (this.sysUpTime - old.sysUpTime);
        if (tdif != 0) {
            speed = 100 * ((this.inOctet - old.inOctet)
                    + (this.outOctet - old.outOctet)) / tdif;
        }
        return speed;
    }

    void addOids(int interf) {
        addOid(SYS_UPTIME + ".0");
        addOid(DESCR + "." + interf);
        addOid(OPR_STATUS + "." + interf);
        addOid(IN_OCTETS + "." + interf);
        addOid(OUT_OCTETS + "." + interf);
    }

    /**
     * The value of the request is set. This will be called by
     * Pdu.fillin().
     *
     * @param n   the index of the value
     * @param res the value
     * @see Pdu#new_value
     */
    protected void new_value(int n, varbind res) {
        AsnObject obj = res.getValue();
        if (getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
            try {
                switch (n) {
                    case 0:
                        sysUpTime = ((AsnUnsInteger) obj).getValue();
                        break;
                    case 1:
                        descr = ((AsnOctets) obj).getValue();
                        break;
                    case 2:
                        operStatus = ((AsnInteger) obj).getValue();
                        break;
                    case 3:
                        inOctet = ((AsnUnsInteger) obj).getValue();
                        break;
                    case 4:
                        outOctet = ((AsnUnsInteger) obj).getValue();
                        valid = true;
                        break;
                    default:
                        valid = false;
                }
            } catch (ClassCastException exc) {
                sysUpTime = 0;
                descr = null;
                operStatus = 0;
                inOctet = 0;
                outOctet = 0;
                valid = false;
            }
        } else {
            valid = false;
        }
    }

    /**
     * This method notifies all observers.
     * This will be called by Pdu.fillin().
     * 
     * <p>
     * Unless an exception occurred the Object to the update() method of the
     * Observer will be a varbind, so any AsnObject type can be returned.
     * In the case of an exception, that exception will be passed.
     * </p>
     */
    protected void tell_them() {
        notifyObservers(this);
    }

    /**
     * Returns how many interfaces are present.
     *
     * @return the number of interfaces
     */
    public static int getNumIfs(SnmpContextBasisFace con)
            throws PduException, java.io.IOException {
        int ifCount = 0;

        if (con != null) {
            OneIntPdu numIfs = new OneIntPdu(con, IFNUMBER);
            boolean answered = numIfs.waitForSelf();
            boolean timedOut = numIfs.isTimedOut();
            if (answered == true && timedOut == false) {
                ifCount = numIfs.getValue().intValue();
            }
        }
        return ifCount;
    }

}
