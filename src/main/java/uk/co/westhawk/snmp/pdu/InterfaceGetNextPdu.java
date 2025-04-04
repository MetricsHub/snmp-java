// NAME
//      $RCSfile: InterfaceGetNextPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.17 $
// CREATED
//      $Date: 2008/05/06 10:17:06 $
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
import uk.co.westhawk.snmp.pdu.*;
import java.util.*;


/**
 * The class InterfaceGetNextPdu.
 *
 * This file is auto generated by the StubBrowser utility, using Mibble.
 * See the uk/co/westhawk/stub/ directory.
 *
 * Added speed parameter and methods by hand.

 * Make sure that you replace the package name and classname placeholders. 
 * Also, move this file to the correct package directory.
 * If these things are not done, this class will not compile correctly!!
 *
 * @version $Revision: 3.17 $ $Date: 2008/05/06 10:17:06 $
 */
public class InterfaceGetNextPdu extends InterfaceGetNextPduStub {
    private static final String version_id = "@(#)$Id: InterfaceGetNextPdu.java,v 3.17 2008/05/06 10:17:06 birgita Exp $ Copyright Westhawk Ltd";

    protected long _speed;

    /**
     * Constructor.
     *
     * @param con The context of the request
     */
    public InterfaceGetNextPdu(SnmpContextBasisFace con) {
        super(con);
    }

    /**
     * Returns the last calculates speed.
     *
     * @see #getSpeed(InterfaceGetNextPdu)
     */
    public long getSpeed() {
        return _speed;
    }

    /**
     * Calculates the speed of the interface. This is done by providing the
     * method with <i>the previous value of this interface</i>. An interface
     * is marked by its index. Do <i>not</i> confuse it
     * with <i>the previous interface ifInOctets the MIB</i>.
     * Total number of octets (received and transmitted) per second.
     *
     * @param old The previous value of this interface
     */
    public long getSpeed(InterfaceGetNextPdu old) {
        _speed = -1;
        if (this._ifOperStatus > 0
                &&
                old._ifOperStatus > 0
                &&
                this._valid
                &&
                old._valid) {
            long tdiff = (this._sysUpTime - old._sysUpTime);
            if (tdiff != 0) {
                long inO = this._ifInOctets - old._ifInOctets;
                long outO = this._ifOutOctets - old._ifOutOctets;

                _speed = 100 * (inO + outO) / tdiff;
            }
        } else {
            _speed = -1;
        }
        return _speed;
    }

    /**
     * Returns how many interfaces are present.
     *
     * @return the number of interfaces
     */
    public static int getIfNumber(SnmpContextBasisFace con)
            throws PduException, java.io.IOException {
        int ifNumber = 0;

        if (con != null) {
            OneIntPdu ifNumberPdu = new OneIntPdu(con, ifNumber_OID + ".0");
            boolean answered = ifNumberPdu.waitForSelf();
            boolean timedOut = ifNumberPdu.isTimedOut();
            if (timedOut == false) {
                Integer intValue = ifNumberPdu.getValue();
                if (intValue != null) {
                    ifNumber = intValue.intValue();
                }
            }
        }
        return ifNumber;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append("[");
        buffer.append(super.toString());
        buffer.append(", speed=").append(_speed);
        buffer.append("]");
        return buffer.toString();
    }

}
