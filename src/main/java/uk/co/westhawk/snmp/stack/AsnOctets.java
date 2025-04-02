// NAME
//      $RCSfile: AsnOctets.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.39 $
// CREATED
//      $Date: 2006/03/23 14:54:10 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1995, 1996 by West Consulting BV
 *
 * Permission to use, copy, modify, and distribute this software
 * for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that
 * both the copyright notice and this permission notice appear in
 * supporting documentation.
 * This software is provided "as is" without express or implied
 * warranty.
 * author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * original version by hargrave@dellgate.us.dell.com (Jordan Hargrave)
 */

/*
 * Copyright (C) 1996 - 2006 by Westhawk Ltd
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

import uk.co.westhawk.snmp.util.*;
import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

/**
 * This class represents the ASN.1 Octet class.
 * It can be used for Octets, Ip Addresses and Opaque primitive types. 
 *
 * The Octets type (ASN_OCTET_STR) is used for some text convensions. 
 * This class supports the DateAndTime, DisplayString and
 * InternationalDisplayString and Ipv6Address text convensions. 
 * <br/>
 * Note, the SNMP representation of IPv4 and IPv6 is different:
 * <ul>
 *    <li>IPv4: IPADDRESS (or ASN_OCTET_STR, see rfc 4001)</li>
 *    <li>IPv6: ASN_OCTET_STR</li>
 * </ul>
 * See also 
 * <a href="http://www.ietf.org/rfc/rfc2465.txt">IPV6-TC</a>,
 * <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>,
 * <a href="http://www.ietf.org/rfc/rfc4001.txt">INET-ADDRESS-MIB</a>.
 *
 * @see SnmpConstants#ASN_OCTET_STR
 * @see SnmpConstants#IPADDRESS
 * @see SnmpConstants#OPAQUE
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.39 $ $Date: 2006/03/23 14:54:10 $
 */
public class AsnOctets extends AsnObject {
    private static final String version_id = "@(#)$Id: AsnOctets.java,v 3.39 2006/03/23 14:54:10 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * The hexadecimal prefix that is used when printing a hexadecimal
     * number in toString(). By default this is "0x".
     */
    public static String HEX_PREFIX = "0x";

    /**
     * The object that is used in toCalendar() to format the calendar
     * representation of the Octets according to the DateAndTime text
     * convension.
     * The pattern is "yyyy-M-d,HH:mm:ss.SS,z".
     *
     * @see #getCalendar()
     * @see #toCalendar()
     * @see SimpleDateFormat
     */
    public static SimpleDateFormat CALFORMAT = new SimpleDateFormat("yyyy-M-d,HH:mm:ss.SS,z");

    /**
     * The default AsnOctetsPrintableFace object.
     *
     * @see #setPrintable
     * @see DefaultAsnOctetsPrintable
     */
    public static AsnOctetsPrintableFace printableObject = new DefaultAsnOctetsPrintable();

    byte value[];

    /** Cache the hash code for the OID */
    private int hash = 0;

    /**
     * Constructor. The type of the AsnOctets defaults to ASN_OCTET_STR.
     *
     * @param s The byte array representing the AsnOctets
     * @see SnmpConstants#ASN_OCTET_STR
     */
    public AsnOctets(byte s[])
            throws IllegalArgumentException {
        this(s, ASN_OCTET_STR);
    }

    /**
     * Constructor to create a specific type of AsnOctets.
     *
     * @param s The byte array representing the AsnOctets
     * @param t The type of the AsnOctets
     * @see SnmpConstants#ASN_OCTET_STR
     * @see SnmpConstants#IPADDRESS
     * @see SnmpConstants#OPAQUE
     */
    public AsnOctets(byte s[], byte t)
            throws IllegalArgumentException {
        value = s;
        type = t;
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }
    }

    /**
     * Constructor. The type of the AsnOctets defaults to ASN_OCTET_STR.
     *
     * @param s The character array representing the AsnOctets
     * @see SnmpConstants#ASN_OCTET_STR
     */
    public AsnOctets(char s[]) {
        int idx;

        value = new byte[s.length];
        type = ASN_OCTET_STR;
        for (idx = 0; idx < s.length; idx++) {
            value[idx] = (byte) s[idx];
        }
    }

    /**
     * Constructor. The type of the AsnOctets defaults to ASN_OCTET_STR.
     *
     * @param s The string representing the AsnOctets
     * @see SnmpConstants#ASN_OCTET_STR
     */
    public AsnOctets(String s) {
        this(s.toCharArray());
    }

    /**
     * Constructor to create an ASN IP Address.
     * If the address represents an IPv4 address, the asn type will be
     * set to IPADDRESS. If it represents an IPv6 address, the asn type
     * will be set to ASN_OCTET_STR.
     *
     * <br/>
     * Note, the SNMP representation of IPv4 and IPv6 is different:
     * <ul>
     * <li>IPv4: IPADDRESS (or ASN_OCTET_STR, see rfc 4001)</li>
     * <li>IPv6: ASN_OCTET_STR</li>
     * </ul>
     * See also
     * <a href="http://www.ietf.org/rfc/rfc2465.txt">IPV6-TC</a>,
     * <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>,
     * <a href="http://www.ietf.org/rfc/rfc4001.txt">INET-ADDRESS-MIB</a>.
     *
     * @param iad The Inet Address
     *
     * @see #AsnOctets(Inet4Address, byte)
     */
    public AsnOctets(InetAddress iad)
            throws IllegalArgumentException {
        this(iad.getAddress(), ASN_OCTET_STR);
        if (iad instanceof java.net.Inet4Address) {
            // IPv4
            type = IPADDRESS;
        } else {
            // IPv6 is ASN_OCTET_STR, so do nothing
        }
    }

    /**
     * Constructor to create an ASN IPv4 Address.
     * If the address is an IPv4 address, it can either be represented
     * by IPADDRESS or as ASN_OCTET_STR.
     *
     * See also
     * <a href="http://www.ietf.org/rfc/rfc2465.txt">IPV6-TC</a>,
     * <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>,
     * <a href="http://www.ietf.org/rfc/rfc4001.txt">INET-ADDRESS-MIB</a>.
     *
     * @param iad The IPv4 Inet Address
     * @param t   The type of the AsnOctets
     *
     * @see #AsnOctets(InetAddress)
     * @see SnmpConstants#IPADDRESS
     * @see SnmpConstants#ASN_OCTET_STR
     * @since 4_14
     */
    public AsnOctets(java.net.Inet4Address iad, byte t)
            throws IllegalArgumentException {
        this(iad.getAddress(), t);
    }

    /**
     * Constructor for DateAndTime text convension.
     * See
     * <a href="http://www.ietf.org/rfc/rfc2579.txt">SNMPv2-TC</a>
     *
     * <pre>
     *      field  octets  contents                  range
     *      -----  ------  --------                  -----
     *        1      1-2   year*                     0..65536
     *        2       3    month                     1..12
     *        3       4    day                       1..31
     *        4       5    hour                      0..23
     *        5       6    minutes                   0..59
     *        6       7    seconds                   0..60
     *                     (use 60 for leap-second)
     *        7       8    deci-seconds              0..9
     *
     *        8       9    direction from UTC        '+' / '-'
     *        9      10    hours from UTC*           0..13
     *       10      11    minutes from UTC          0..59
     *
     * SYNTAX       OCTET STRING (SIZE (8 | 11))
     * </pre>
     *
     * @since 4_14
     */
    public AsnOctets(Calendar cal) {
        value = new byte[11];
        type = ASN_OCTET_STR;

        int year = cal.get(Calendar.YEAR);
        // Calendar: 0=January
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int msec = cal.get(Calendar.MILLISECOND);
        int msecGMT = cal.get(Calendar.ZONE_OFFSET);

        // The value of year is in network-byte order
        // Is this correct?
        value[0] = (byte) ((year / 256) % 256);
        value[1] = (byte) (year % 256);

        value[2] = (byte) (month & 0xFF);
        value[3] = (byte) (day & 0xFF);
        value[4] = (byte) (hour & 0xFF);
        value[5] = (byte) (min & 0xFF);
        value[6] = (byte) (sec & 0xFF);
        value[7] = (byte) ((msec / 100) & 0xFF);

        char dir = '\0';
        if (msecGMT < 0) {
            dir = '-';
            msecGMT = msecGMT * -1;
        } else {
            dir = '+';
        }
        value[8] = (byte) dir;

        if (msecGMT == 0) {
            value[9] = 0x00;
            value[10] = 0x00;
        } else {
            int minGMT = (int) (((double) msecGMT) / 1000.0 / 60.0);
            if (minGMT == 0) {
                value[9] = 0x00;
                value[10] = 0x00;
            } else {
                int hourGMT = (int) (minGMT / 60.0);
                minGMT = minGMT - (hourGMT * 60);
                value[9] = (byte) (hourGMT & 0xFF);
                value[10] = (byte) (minGMT & 0xFF);
            }
        }
    }

    /**
     * Constructor.
     *
     * @param in  The input stream from which the value should be read
     * @param len The length of the AsnOctets
     */
    public AsnOctets(InputStream in, int len) throws IOException {
        value = new byte[len];
        if (len != 0) {
            if (len == in.read(value, 0, len)) {
                String str = "";
                // str = new String(value);
            } else {
                throw new IOException("AsnOctets(): Not enough data");
            }
        } else {
            // if len is zero, the in.read will return -1
            // a length of zero is a valid case.
            ;
        }
    }

    /**
     * Sets the global hexadecimal prefix. This prefix will be used in
     * toString() when it prints out a hexadecimal number. It is not
     * used in toHex(). The default is "0x".
     *
     * @see #toString()
     * @see #toHex()
     * @see #HEX_PREFIX
     */
    public static void setHexPrefix(String newPrefix) {
        HEX_PREFIX = newPrefix;
    }

    /**
     * Sets the global AsnOctetsPrintableFace printableObject. This
     * object will be used in the toString() and the
     * toInternationalDisplayString() methods.
     *
     * @see #toString
     * @see #toInternationalDisplayString
     * @since 4_14
     */
    public static void setPrintable(AsnOctetsPrintableFace obj) {
        if (obj != null) {
            printableObject = obj;
        }
    }

    /**
     * Returns the String value. Calls toString().
     *
     * @return The value of the AsnOctets
     * @see #toString()
     */
    public String getValue() {
        return toString();
    }

    /**
     * Returns the bytes. This returns a copy of the internal byte array.
     *
     * @return The bytes of the AsnOctets
     */
    public byte[] getBytes() {
        int len = value.length;
        byte[] bytea = new byte[len];
        System.arraycopy(value, 0, bytea, 0, len);
        return bytea;
    }

    /**
     * Returns the string representation of the AsnOctets.
     * <p>
     * The string will have one of the following formats:
     * </p>
     * <ul>
     * <li>if this class represents an IP Address (v4), it will call
     * toIpAddress()</li>
     * <li>&lt;prefix&gt;aa[:bb]*, if this class represents a non-printable
     * string or has type OPAQUE.
     * The output will be in hexadecimal numbers (see toHex()). It will be prefixed
     * according to the hex. prefix value</li>
     * <li>a printable string, if this class seems printable</li>
     * </ul>
     *
     * <p>
     * When the type is ASN_OCTET_STR, this method uses the
     * AsnOctetsPrintableFace.isPrintable() to determine whether or not
     * the string is printable. If it is printable, it will use
     * AsnOctetsPrintableFace.toInternationalDisplayString() to
     * transform the Octets to a String.
     * </p>
     *
     * <br/>
     * Note, the SNMP representation of IPv4 and IPv6 is different:
     * <ul>
     * <li>IPv4: IPADDRESS (or ASN_OCTET_STR, see rfc 4001)</li>
     * <li>IPv6: ASN_OCTET_STR</li>
     * </ul>
     * See also
     * <a href="http://www.ietf.org/rfc/rfc2465.txt">IPV6-TC</a>,
     * <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>,
     * <a href="http://www.ietf.org/rfc/rfc4001.txt">INET-ADDRESS-MIB</a>.
     *
     * @see #HEX_PREFIX
     * @see #setHexPrefix(String)
     * @see #toHex
     * @see #toIpAddress
     * @see AsnOctetsPrintableFace#isPrintable
     * @see AsnOctetsPrintableFace#toInternationalDisplayString
     * @return The string representation of the AsnOctets
     */
    public String toString() {
        return toString(printableObject);
    }

    /**
     * As toString(), but this methods will use this specific, one-off
     * AsnOctetsPrintableFace object.
     *
     * @see #toString()
     * @since 4_14
     */
    public String toString(AsnOctetsPrintableFace face) {
        String str = "";

        if (type == IPADDRESS) {
            // for IPv4 only
            str = toIpAddress();
        } else if (type == OPAQUE) {
            str = HEX_PREFIX + toHex();
        } else {
            boolean isPrintable = face.isPrintable(value);
            if (isPrintable) {
                str = face.toInternationalDisplayString(value);
            } else {
                str = HEX_PREFIX + toHex();
            }
        }
        return str;
    }

    int size() {
        return value.length;
    }

    void write(OutputStream out, int pos) throws IOException {
        int idx;

        // Output header
        AsnBuildHeader(out, type, value.length);
        if (debug > 10) {
            System.out.println("\tAsnOctets(): value = " + toString()
                    + ", pos = " + pos);
        }

        // Output data
        for (idx = 0; idx < value.length; idx++) {
            out.write(value[idx]);
        }
    }

    /**
     * Returns this Octet as an IP Address string. The format is
     * aaa.bbb.ccc.ddd (IPv4) or a:b:c:d:e:f:g:h (IPv6).
     *
     * Note, the SNMP representation of IPv4 and IPv6 is different:
     * <ul>
     * <li>IPv4: IPADDRESS (or ASN_OCTET_STR, see rfc 4001)</li>
     * <li>IPv6: ASN_OCTET_STR</li>
     * </ul>
     * See also
     * <a href="http://www.ietf.org/rfc/rfc2465.txt">IPV6-TC</a>,
     * <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>,
     * <a href="http://www.ietf.org/rfc/rfc4001.txt">INET-ADDRESS-MIB</a>.
     *
     * @return The IP Address representation.
     * @see #toString
     * @see #getIpAddress
     * @see SnmpConstants#ASN_OCTET_STR
     * @see SnmpConstants#IPADDRESS
     */
    /*
     * TODO: use Java's java.net.InetAddress, so it can be used for IPv4, and IPv6.
     */
    public String toIpAddress() {
        /*
         * TODO: Does this work? Yes, but will not compile in jdk 1.2.X, and
         * in order to load (a part of) the stack in Oracle, I need
         * 1.2.X
         */
        /*
         * String str = "";
         * try
         * {
         * InetAddress iad = InetAddress.getByAddress(value);
         * str = iad.getHostAddress();
         * }
         * catch (java.net.UnknownHostException exc) { }
         */

        /*
        */
        StringBuffer sb = new StringBuffer(39);
        int length;
        long val;
        length = value.length;
        if (length > 0) {
            if (length > 4) {
                // IPv6
                // Nicked this code from Inet6Address.numericToTextFormat
                for (int i = 0; i < length / 2; i++) {
                    sb.append(Integer.toHexString(((value[i << 1] << 8) & 0xff00)
                            | (value[(i << 1) + 1] & 0xff)));
                    if (i < ((length / 2) - 1)) {
                        sb.append(":");
                    }
                }
            } else {
                // IPv4
                for (int i = 0; i < length - 1; i++) {
                    val = getPositiveValue(i);
                    sb.append(String.valueOf(val)).append(".");
                }
                val = getPositiveValue(length - 1);
                sb.append(String.valueOf(val));
            }
        }
        return sb.toString();
    }

    /**
     * Returns this Octet as an IP Address.
     *
     * Note, the SNMP representation of IPv4 and IPv6 is different:
     * <ul>
     * <li>IPv4: IPADDRESS (or ASN_OCTET_STR, see rfc 4001)</li>
     * <li>IPv6: ASN_OCTET_STR</li>
     * </ul>
     * See also
     * <a href="http://www.ietf.org/rfc/rfc2465.txt">IPV6-TC</a>,
     * <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>,
     * <a href="http://www.ietf.org/rfc/rfc4001.txt">INET-ADDRESS-MIB</a>.
     *
     * @return The IP Address representation.
     * @exception RuntimeException Thrown when the Octets does
     *                             not represent an InetAddress or when the method
     *                             internally throws
     *                             an java.net.UnknownHostException
     *
     * @see #toString
     * @see #toIpAddress
     * @see SnmpConstants#ASN_OCTET_STR
     * @see SnmpConstants#IPADDRESS
     * @since 4_14
     */
    public InetAddress getIpAddress()
            throws RuntimeException {
        InetAddress iad = null;
        try {
            iad = InetAddress.getByAddress(value);
        } catch (java.net.UnknownHostException exc) {
            throw new RuntimeException(exc);
        }
        return iad;
    }

    /**
     * Returns the positive long for an octet. Only if type is IPADDRESS
     * can the value be negative anyway.
     */
    private long getPositiveValue(int index) {
        long val = (long) value[index];
        if (val < 0) {
            val += 256;
        }
        return val;
    }

    /**
     * Returns this Octet as an hexadecimal String, without any prefix.
     *
     * @return The hex representation.
     * @see #toString
     */
    public String toHex() {
        int length;
        StringBuffer buffer = new StringBuffer("");

        length = value.length;
        if (length > 0) {
            for (int i = 0; i < length - 1; i++) {
                buffer.append(SnmpUtilities.toHex(value[i])).append(":");
            }
            buffer.append(SnmpUtilities.toHex(value[length - 1]));
        }

        return buffer.toString();
    }

    /**
     * Returns this Octet as a display string (text convension). In contrast to the
     * method toString(), this method does not try to guess whether or
     * not this string is printable, it just converts it to a
     * String, using "US-ASCII" character set.
     *
     * <p>
     * DisplayString
     * represents textual information taken from the NVT
     * ASCII character set, as defined in pages 4, 10-11
     * of <a href="http://www.ietf.org/rfc/rfc854.txt">RFC 854</a>.
     * Any object defined using this syntax
     * may not exceed 255 characters in length.
     * Basicly it is US-ASCII with some changes.
     * </p>
     *
     * @return The string representation.
     * @see #toString
     */
    public String toDisplayString() {
        String str = "";
        int length;

        length = value.length;
        if (length > 0) {
            try {
                str = new String(value, "US-ASCII");
            } catch (UnsupportedEncodingException exc) {
                str = new String(value);
            }
            str = str.trim();
        }

        return str;
    }

    /**
     * Returns this Octet as an international display string (text
     * convension).
     * It calls AsnOctetsPrintableFace.toInternationalDisplayString().
     *
     * See
     * <a href="http://www.ietf.org/rfc/rfc2790.txt">HOST-RESOURCES-MIB</a>
     * 
     * @see AsnOctetsPrintableFace#toInternationalDisplayString
     * @since 4_14
     */
    public String toInternationalDisplayString() {
        return toInternationalDisplayString(printableObject);
    }

    /**
     * As toInternationalDisplayString(), but this methods will use this
     * specific, one-off AsnOctetsPrintableFace object.
     * 
     * @see #toInternationalDisplayString
     * @since 4_14
     */
    public String toInternationalDisplayString(AsnOctetsPrintableFace face) {
        return face.toInternationalDisplayString(value);
    }

    /**
     * Returns the String representation according to the DateAndTime
     * convension.
     * This string it returns is not exactly the same as the
     * DISPLAY-HINT indicates.
     * See
     * <a href="http://www.ietf.org/rfc/rfc2579.txt">SNMPv2-TC</a>
     *
     * @since 4_14
     * @exception RuntimeException Thrown when the number of
     *                             Octets does not represent the DateAndTime length.
     * @see #CALFORMAT
     */
    public String toCalendar()
            throws RuntimeException {
        Calendar cal = this.getCalendar();
        Date date = cal.getTime();
        return CALFORMAT.format(date);
    }

    /**
     * Returns the Octets as Calendar according to the DateAndTime text
     * convension. You can only call this method if
     * the syntax of this Octet is the DateAndTime text convension.
     *
     * @exception RuntimeException Thrown when the number of
     *                             Octets does not represent the DateAndTime length.
     *
     * @since 4_14
     * @see #AsnOctets(Calendar)
     */
    public Calendar getCalendar()
            throws RuntimeException {
        Calendar cal = Calendar.getInstance();
        if (value.length == 8 || value.length == 11) {
            int year = (int) ((getPositiveValue(0) * 256) + getPositiveValue(1));
            // Calendar: 0=January
            int month = value[2] - 1;
            int day = value[3];
            int hour = value[4];
            int min = value[5];
            int sec = value[6];
            int msec = value[7] * 100;
            cal.set(year, month, day, hour, min, sec);
            cal.set(Calendar.MILLISECOND, msec);

            if (value.length == 11) {
                char dir = (char) value[8];
                int hourUTC = value[9];
                int minUTC = value[10];
                int secUTC = (hourUTC * 60) * 60;

                int msecGMT = secUTC * 1000;
                if (dir == '-') {
                    msecGMT = msecGMT * -1;
                }

                cal.set(Calendar.ZONE_OFFSET, msecGMT);
            }
        } else {
            throw new RuntimeException("AsnOctets is not DateAndTime");
        }
        return cal;
    }

    /**
     * Converts this Octet to its corresponding sub-identifiers.
     * Each octet will be encoded in a separate sub-identifier, by
     * converting the octet into a positive long.
     * 
     * <p>
     * Use this method when building an OID when this Octet specifies a
     * conceptual row. For example ipNetToMediaEntry, see
     * <a href="http://www.ietf.org/rfc/rfc2011.txt">IP-MIB</a>
     * or SnmpCommunityEntry, see
     * <a href="http://www.ietf.org/rfc/rfc3584.txt">SNMP-COMMUNITY-MIB</a>
     * </p>
     *
     * <p>
     * The variable <code>length_implied</code> indicates that this MIB variable
     * is preceded by the IMPLIED keyword:
     * </p>
     * <ul>
     * <li>
     * The IMPLIED keyword can only be present for an Octet having
     * a variable-length syntax (e.g., variable-length strings or object
     * identifier-valued objects).
     * </li>
     * <li>
     * The IMPLIED keyword can only be associated with the last
     * object in the INDEX clause.
     * </li>
     * <li>
     * The IMPLIED keyword may not be used on a variable-length
     * string Octet if that string might have a value of zero-length.
     * </li>
     * </ul>
     *
     * <p>
     * If the length is implied, no extra sub-identifier will be created to
     * indicate its length. <br/>
     * If the length is not implied, the first
     * sub-identifier will be the length of the Octet.
     * </p>
     *
     * <p>
     * If this Octet is of type IPADDRESS, length_implied should be false.
     * </p>
     *
     * <p>
     * The mapping of the INDEX clause is
     * explained in <a href="http://www.ietf.org/rfc/rfc2578.txt">SNMPv2-SMI</a>,
     * section 7.7.
     * </p>
     *
     * @param length_implied Indicates if the length of this octet is
     *                       implied.
     *
     * @see AsnObjectId#add(long[])
     */
    public long[] toSubOid(boolean length_implied) {
        long sub_oid[];
        int index = 0;
        int length = value.length;

        if (length_implied) {
            sub_oid = new long[length];
        } else {
            sub_oid = new long[length + 1];
            sub_oid[0] = length;
            index++;
        }

        for (int i = 0; i < length; i++) {
            sub_oid[index] = getPositiveValue(i);
            index++;
        }
        return sub_oid;
    }

    /**
     * Compares this Octet to the specified object.
     * The result is <code>true</code> if and only if the argument is not
     * <code>null</code> and is an <code>AsnOctets</code> object that represents
     * the same sequence of octets as this Octet.
     *
     * @param anObject the object to compare this <code>AsnOctets</code>
     *                 against.
     * @return <code>true</code> if the <code>AsnOctets </code>are equal;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof AsnOctets) {
            AsnOctets anotherOctet = (AsnOctets) anObject;
            int n = value.length;
            if (n == anotherOctet.value.length) {
                byte v1[] = value;
                byte v2[] = anotherOctet.value;
                int i = 0;
                int j = 0;
                while (n-- != 0) {
                    if (v1[i++] != v2[j++]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a hash code for this Octet. The hash code for a
     * <code>AsnOctets</code> object is computed as
     * <blockquote>
     * 
     * <pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre>
     * 
     * </blockquote>
     * using <code>int</code> arithmetic, where <code>s[i]</code> is the
     * <i>i</i>th character of the Octet, <code>n</code> is the length of
     * the Octet, and <code>^</code> indicates exponentiation.
     * (The hash value of the empty Octet is zero.)
     *
     * @return a hash code value for this Octet.
     */
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            int off = 0;
            byte val[] = value;
            int len = value.length;

            for (int i = 0; i < len; i++) {
                h = 31 * h + val[off++];
            }
            hash = h;
        }
        return h;
    }

}
