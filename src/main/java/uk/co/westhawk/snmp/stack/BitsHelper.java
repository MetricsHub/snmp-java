// NAME
//      $RCSfile: BitsHelper.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.1 $
// CREATED
//      $Date: 2006/03/23 15:09:48 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2006 by Westhawk Ltd
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

import java.io.*;

/**
 * This class help with the BITS construct. The AsnOctets class is
 * growing too big, because of all the different representations.
 *
 * Note that BITS is different from the ANS.1 BIT STRING. Apparently BIT
 * STRING was in v1, but was depreceated in favour of BITS.
 *
 * <pre>
 * An instance with bits 0, 3 and 10 set will have the value:
 *
 *      10010000 00100000
 *      ^  ^       ^
 * bit  0  3      10
 *
 * so will consist of the two bytes (octets) 0x90 &amp; 0x20.
 * </pre>
 *
 * See also
 * section 7.1.4. of
 * <a href="http://www.ietf.org/rfc/rfc2578.txt">SNMPv2-SMI</a>
 * and section 8. (3) of
 * <a href="http://www.ietf.org/rfc/rfc3417.txt">SNMPv2-TM</a>.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.1 $ $Date: 2006/03/23 15:09:48 $
 * @since 5_0
 */
public class BitsHelper {
    private static final String version_id = "@(#)$Id: BitsHelper.java,v 3.1 2006/03/23 15:09:48 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Sets or unsets the flag (bit) on the specified index. The bit
     * will be set to zero if toset is false, set to one if toset is
     * true.
     *
     * Note, as a side effect the size of the asn octet might grow.
     *
     * @param oct   The AsnOctets that represents the BITS
     * @param index The index (0 - X)
     * @param toset Whether to set (true) or unset (false) the bit
     */
    public static void setFlagged(AsnOctets oct, int index, boolean toset)
            throws IllegalArgumentException {
        if (index < 0) {
            throw new IllegalArgumentException("Illegal value index (" + index
                    + "). Shoud be greater than 0.");
        } else {
            byte mask;
            int byteNo = index / 8;
            int bitNo = index % 8;

            // See if we've got enough bytes in our array.
            // Reallocate if necessary.
            int len = oct.value.length;
            if (byteNo >= len) {
                int newLen = byteNo + 1;
                byte[] newValue = new byte[newLen];
                System.arraycopy(oct.value, 0, newValue, 0, len);
                oct.value = newValue;
            }

            if (toset == true) {
                mask = (byte) (0x80 >>> bitNo);
                oct.value[byteNo] = (byte) (oct.value[byteNo] | mask);
            } else {
                mask = (byte) (0x7F >>> bitNo);
                oct.value[byteNo] = (byte) (oct.value[byteNo] & mask);
            }
        }
    }

    /**
     * Returns if the flag (bit) on the specified index is set. The bit
     * will be set to zero if toset is false, set to one if toset is
     * true.
     *
     * @param oct   The AsnOctets that represents the BITS
     * @param index The index (0 - X)
     * @return Whether the bit is set (true) or unset (false)
     */
    public static boolean isFlagged(AsnOctets oct, int index)
            throws IllegalArgumentException {
        boolean isFlagged = false;
        if (index < 0) {
            throw new IllegalArgumentException("Illegal value index (" + index
                    + "). Shoud be greater than 0.");
        } else {
            byte mask;
            int byteNo = index / 8;
            int bitNo = index % 8;

            int len = oct.value.length;
            if (byteNo < len) {
                // Shift the bit we're interested in to the far left.
                // If the bit is one, the byte will be negative.
                byte res = (byte) (oct.value[byteNo] << bitNo);
                isFlagged = (res < 0);
            }
        }
        return isFlagged;
    }

}
