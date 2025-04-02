// NAME
//      $RCSfile: DefaultAsnOctetsPrintable.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.3 $
// CREATED
//      $Date: 2006/01/17 17:43:54 $
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

/**
 * Default implementation of AsnOctetsPrintableFace.
 * This class has no effect on the way AsnOctets with type IPADDRESS 
 * or OPAQUE are printed.
 *
 * <p>
 * When the type is ASN_OCTET_STR, the method tries to guess whether
 * or not the string is printable; without the knowledge of the MIB
 * it cannot distinguish between OctetString and any textual
 * conventions, like DisplayString, InternationalDisplayString or DateAndTime.
 * </p>
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.3 $ $Date: 2006/01/17 17:43:54 $
 */
public class DefaultAsnOctetsPrintable implements AsnOctetsPrintableFace {
    static final String version_id = "@(#)$Id: DefaultAsnOctetsPrintable.java,v 3.3 2006/01/17 17:43:54 birgit Exp $ Copyright Westhawk Ltd";

    public DefaultAsnOctetsPrintable() {
    }

    /**
     * Returns whether or not the AsnOctets' byte array represent a printable
     * string or not.
     *
     * <p>
     * This method can only make a rough guess. There is no way it always
     * gets it right.
     * It is much better to embed MIB knowledge in your implementation, and
     * use toCalendar() or toDisplayString(), than calling toString().
     * </p>
     *
     * @see AsnOctets#toCalendar()
     * @see AsnOctets#toDisplayString()
     * @see AsnOctets#toHex()
     * @see AsnOctets#toString()
     */
    public boolean isPrintable(byte[] value) {
        int length = value.length;
        int b = ' '; // the first printable char in the ASCII table
        int e = '~'; // the last printable char in the ASCII table

        /*
         * About the test for 'value[i] == 0':
         * (Quote from one of our customers:)
         * I've seen cases where there are embedded nulls in a sysdescr
         * - not always at the end either - and we need to get complete
         * data back from the device even in this situation.
         */

        boolean isPrintable = true;
        int i = 0;
        while (i < length && isPrintable) {
            isPrintable = ((value[i] >= b && value[i] <= e)
                    ||
                    Character.isWhitespace((char) value[i])
                    ||
                    value[i] == 0);
            i++;
        }

        return isPrintable;
    }

    /**
     * Returns the String according to the platform's default character set.
     *
     * @see AsnOctetsPrintableFace#toInternationalDisplayString(byte[] value)
     */
    public String toInternationalDisplayString(byte[] value) {
        String str = "";
        if (value.length > 0) {
            // this will use the platform's default charset.
            str = new String(value).trim();
        }
        return str;
    }

}
