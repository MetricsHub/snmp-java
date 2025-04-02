// NAME
//      $RCSfile: AsnObject.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.30 $
// CREATED
//      $Date: 2006/03/23 14:54:09 $
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

import java.io.*;
import java.util.*;
import uk.co.westhawk.snmp.util.*;

/**
 * This class represents the ASN.1 base class.
 * SMIv1 <a href="http://www.ietf.org/rfc/rfc1155.txt">RFC1155-SMI</a>.
 * SMIv2 <a href="http://www.ietf.org/rfc/rfc2578.txt">SNMPv2-SMI</a>.
 *
 * <pre>
 * &lt;ASN Object&gt; = &lt;type&gt; &lt;length&gt; &lt;ASN Object&gt;
 * or
 * &lt;ASN Object&gt; = &lt;type&gt; &lt;length&gt; &lt;value&gt;
 * </pre>
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.30 $ $Date: 2006/03/23 14:54:09 $
 */

public abstract class AsnObject extends Object implements SnmpConstants {
    private static final String version_id = "@(#)$Id: AsnObject.java,v 3.30 2006/03/23 14:54:09 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Indicates the level of debug printed. By default this is none
     * (0).
     * 
     * @see #setDebug(int)
     */
    public static int debug = 0;

    /**
     * The type of object.
     */
    protected byte type;

    /**
     * The starting position of the AsnObject in the SNMP sequence. That
     * is the position of the 'type' byte.
     */
    protected int startPos = 0;

    /**
     * The length of the header. That is the number of bytes of the 'type'
     * and 'length' fields. Since there are multiple valid encodings for
     * a given AsnObject, this length is not fixed!
     */
    protected int headerLength = 0;

    /**
     * The length of the actual contents. That is the same as the value of
     * the 'length' field.
     */
    protected int contentsLength = 0;

    /**
     * Flag to signal the object is of a correct type.
     */
    protected boolean isCorrect = true;

    abstract void write(OutputStream out, int pos)
            throws IOException, EncodingException;

    /**
     * Returns a string representation of the object.
     * 
     * @return The string
     */
    public abstract String toString();

    /**
     * Sets the new, global level of debug information for the stack package.
     * The default value is zero, i.e. no debug info at all. All debug
     * messages are printed to System.out.
     * <p>
     * The following messages will appear when debug is &gt; (greater than):
     * </p>
     * <ul>
     * <li><code>0</code> - IOExceptions that won't be thrown to the user</li>
     * <li><code>1</code> - DecodingExceptions that won't be thrown to the user
     * (received message with wrong SNMP version)</li>
     * <li><code>2</code> - DecodingExceptions when trying to decode traps</li>
     * <li><code>3</code> - processIncomingResponse errors: (corresponding Pdu
     * cannot be found)</li>
     * <li><code>4</code> - Discovery of EngineId, Timeline</li>
     * <li><code>5</code> - Receiving traps, version/host do not correspond</li>
     * <li><code>6</code> - Pdu messages, sending etc</li>
     * <li><code>10</code> - Asn decoding of incoming messages</li>
     * <li><code>12</code> - Transmitter messages, Socket messages</li>
     * <li><code>15</code> - Not so important IOExceptions, like
     * InterruptedIOException</li>
     * </ul>
     *
     * @param newDebug the new debug value
     */
    public static void setDebug(int newDebug) {
        debug = newDebug;
    }

    AsnObject AsnReadHeader(InputStream in) throws IOException {
        return AsnReadHeader(in, 0);
    }

    /**
     * @param pos The starting position, i.e. the pos the 'type' byte.
     */
    AsnObject AsnReadHeader(InputStream in, int pos) throws IOException {
        int len, got;
        AsnObject ret = null;
        int had = in.available();

        // Type byte
        type = (byte) in.read();
        if (type != -1) {
            len = getLengthPacket(in);
            int off = in.available();
            int headLength = had - off;

            got = 0;
            byte body[] = new byte[len];
            if (len > 0) {
                got = in.read(body, 0, len);
            }
            if (got > -1) {
                // Always decode, unless in.read returned an error
                // (i.e. got == -1):
                //
                // 1.
                // If we haven't received all bytes (i.e. got < len)
                // we will still try and decode as much as we can:
                // Since body[] has the length 'len' (i.e. the
                // length according to the packet), it will be padded with
                // zero
                //
                // 2.
                // If len was 0, got would have been been -1, but since
                // no read was performed, got is 0.
                // A len of 0 is a valid case however.

                ByteArrayInputStream buf = new ByteArrayInputStream(body);
                ret = AsnMakeMe(buf, type, len, pos, headLength);

                if (got != len) {
                    ret.isCorrect = false;
                    if (debug > 10) {
                        System.out.println("\nAsnObject.AsnReadHeader():"
                                + " incorrect packet. Length = " + (int) len
                                + ", Got = " + got);
                    }
                }
            } else {
                if (debug > 10) {
                    System.out.println("\nAsnObject.AsnReadHeader(): Length = "
                            + (int) len
                            + ", Got = " + got
                            + ". Not processing any further.");
                }
            }
        }
        return ret;
    }

    AsnObject AsnMakeMe(InputStream in, byte t, int len, int pos,
            int headLength) throws IOException {
        AsnObject me = this;
        type = t;

        switch (type) {
            case CONS_SEQ:
                me = new AsnSequence(in, len, (pos + headLength));
                break;
            case GET_REQ_MSG:
            case GETNEXT_REQ_MSG:
            case SET_REQ_MSG:
            case GETBULK_REQ_MSG:
            case INFORM_REQ_MSG:
            case GET_RSP_MSG:
            case GET_RPRT_MSG:
            case TRPV2_REQ_MSG:
                me = new AsnPduSequence(in, len, (pos + headLength));
                break;
            case TRP_REQ_MSG:
                me = new AsnTrapPduv1Sequence(in, len, (pos + headLength));
                break;
            case ASN_INTEGER:
                me = new AsnInteger(in, len);
                break;
            case TIMETICKS:
            case COUNTER:
            case GAUGE:
            case OBSOLETED_RFC1442_UINTEGER32:
                me = new AsnUnsInteger(in, len);
                break;
            case COUNTER64:
                me = new AsnUnsInteger64(in, len); // TODO test
                break;
            case ASN_OBJECT_ID:
                me = new AsnObjectId(in, len);
                break;
            case IPADDRESS:
            case ASN_OCTET_STR:
            case OPAQUE:
            case NSAP_ADDRESS:
                me = new AsnOctets(in, len);
                break;
            case ASN_NULL:
                me = new AsnNull(in, len);
                break;
            case SNMP_VAR_NOSUCHOBJECT:
            case SNMP_VAR_NOSUCHINSTANCE:
            case SNMP_VAR_ENDOFMIBVIEW:
                me = new AsnPrimitive(type);
                break;
            default:
                if (debug > 0) {
                    System.out.println("AsnObject.AsnMakeMe():"
                            + " Bad Type 0x" + SnmpUtilities.toHex(type));
                }
                me = new AsnNull(in, len);
                me.isCorrect = false;
                break;
        }
        me.type = type;
        me.startPos = pos;
        me.headerLength = headLength;
        me.contentsLength = len;

        if (debug > 10) {
            System.out.println("AsnObject.AsnMakeMe():"
                    + " headerLength = " + me.headerLength
                    + ", contentsLength = " + me.contentsLength);
            System.out.println("\ttype = 0x"
                    + SnmpUtilities.toHex(type)
                    + ", " + me.getRespTypeString()
                    + ", " + me.getClass().getName()
                    + ": value = " + me.toString()
                    + ", startPos = " + me.startPos
                    + ", correct = " + me.isCorrect);
        }
        return me;
    }

    /**
     * Adds a child to the sequence
     */
    AsnObject add(AsnObject child) {
        return child;
    }

    /**
     * recursively look for a pduSequence object
     * if not overridden - then not a sequence -
     */
    AsnObject findPdu() {
        return null;
    }

    /**
     * recursively look for a pduSequence object
     * if not overridden - then not a sequence -
     */
    AsnObject findTrapPduv1() {
        return null;
    }

    /**
     * Output ASN header.
     */
    void AsnBuildHeader(OutputStream out, byte t, int length)
            throws IOException {
        int count;

        type = t;
        // Type byte
        out.write(type);

        // Length bytes
        count = getLengthBytes(length);
        headerLength = count + 1;
        contentsLength = length;
        if (debug > 10) {
            System.out.println("AsnBuildHeader(): "
                    + "type = 0x" + SnmpUtilities.toHex(type)
                    + ", headerLength = " + headerLength
                    + ", contentsLength = " + contentsLength);
        }

        if (count > 1) {
            // Write long form prefix byte
            --count;
            byte tmp = (byte) (0x80 | (byte) count);
            out.write(tmp);
        }
        while (count != 0) {
            // Write length bytes
            out.write((byte) ((length >> (--count << 3)) & 0xFF));
        }
    }

    int size() throws EncodingException {
        return 0;
    }

    /**
     * Returns length field size for a packet length.
     * Returns:
     * 1 if length will fit in short form (0x00-0x7f)
     * 2+ if length >= 0x80
     */
    int getLengthBytes(int length) {
        int mask, count;

        if (length < 0x80) {
            // Short form.. 1 byte
            return 1;
        } else {
            // Long form.. prefix byte + length bytes
            mask = 0xFF000000;
            for (count = 4; (length & mask) == 0; count--) {
                mask >>= 8;
            }
            return count + 1;
        }
    }

    int getLengthPacket(InputStream in) throws IOException {
        int length = 0;
        byte mask = (byte) 0x7f;
        byte len = (byte) in.read();

        if ((0x80 & len) != 0) {
            // long form

            int count = (mask & len);
            if (count < 4) {
                byte data[] = new byte[count];
                int n = in.read(data, 0, count);
                if (n != count) {
                    throw new IOException("AsnObject.getLengthPacket(): Not enough data");
                } else {
                    /*
                     * Thanks to Julien Conan (jconan@protego.net)
                     * for improving this code.
                     */
                    DataInputStream dis = new DataInputStream(
                            new ByteArrayInputStream(data));

                    for (n = 0; n < count; n++) {
                        length = (length << 8) + dis.readUnsignedByte();
                    }

                }
            }
        } else {
            // short form
            length = (int) (mask & len);
        }
        return (length);
    }

    int getContentsPos() {
        return startPos + headerLength;
    }

    int getContentsLength() {
        return contentsLength;
    }

    /**
     * Returns the type of this Asn object, such as ASN_INTEGER or
     * IPADDRESS.
     *
     * Note, the name of this method is deceiving; It has
     * nothing (more) to do with a response.
     *
     * @return The AsnObject type.
     * @see #getRespTypeString()
     * @see SnmpConstants
     */
    public byte getRespType() {
        return type;
    }

    /**
     * Returns the object type as string, such as "ASN_INTEGER" or
     * "IPADDRESS".
     *
     * Note, the name of this method is deceiving; It has
     * nothing (more) to do with a response.
     *
     * @return The AsnObject type.
     * @see #getRespType()
     * @see SnmpConstants
     */
    public String getRespTypeString() {
        String str = "";
        switch (type) {
            case ASN_BOOLEAN:
                str = "ASN_BOOLEAN";
                break;
            case ASN_INTEGER:
                str = "ASN_INTEGER";
                break;
            case ASN_BIT_STR:
                str = "ASN_BIT_STR";
                break;
            case ASN_OCTET_STR:
                str = "ASN_OCTET_STR";
                break;
            case ASN_NULL:
                str = "ASN_NULL";
                break;
            case ASN_OBJECT_ID:
                str = "ASN_OBJECT_ID";
                break;
            case ASN_SEQUENCE:
                str = "ASN_SEQUENCE";
                break;
            case ASN_SET:
                str = "ASN_SET";
                break;
            case ASN_UNIVERSAL:
                str = "ASN_UNIVERSAL";
                break;
            case ASN_PRIVATE:
                str = "ASN_PRIVATE";
                break;
            case ASN_CONSTRUCTOR:
                str = "ASN_CONSTRUCTOR";
                break;
            case ASN_EXTENSION_ID:
                str = "ASN_EXTENSION_ID";
                break;
            case IPADDRESS:
                str = "IPADDRESS";
                break;
            case COUNTER:
                str = "COUNTER";
                break;
            case GAUGE:
                str = "GAUGE";
                break;
            case TIMETICKS:
                str = "TIMETICKS";
                break;
            case OPAQUE:
                str = "OPAQUE";
                break;
            case NSAP_ADDRESS:
                str = "NSAP_ADDRESS";
                break;
            case COUNTER64:
                str = "COUNTER64";
                break;
            case OBSOLETED_RFC1442_UINTEGER32:
                str = "OBSOLETED_RFC1442_UINTEGER32 (SMI v1)";
                break;
            case CONS_SEQ:
                str = "CONS_SEQ";
                break;
            case SNMP_VAR_NOSUCHOBJECT:
                str = "SNMP_VAR_NOSUCHOBJECT";
                break;
            case SNMP_VAR_NOSUCHINSTANCE:
                str = "SNMP_VAR_NOSUCHINSTANCE";
                break;
            case SNMP_VAR_ENDOFMIBVIEW:
                str = "SNMP_VAR_ENDOFMIBVIEW";
                break;
            case GET_REQ_MSG:
                str = "GET_REQ_MSG";
                break;
            case GETNEXT_REQ_MSG:
                str = "GETNEXT_REQ_MSG";
                break;
            case SET_REQ_MSG:
                str = "SET_REQ_MSG";
                break;
            case GETBULK_REQ_MSG:
                str = "GETBULK_REQ_MSG";
                break;
            case INFORM_REQ_MSG:
                str = "INFORM_REQ_MSG";
                break;
            case GET_RSP_MSG:
                str = "GET_RSP_MSG";
                break;
            case GET_RPRT_MSG:
                str = "GET_RPRT_MSG";
                break;
            case TRP_REQ_MSG:
                str = "TRP_REQ_MSG";
                break;
            case TRPV2_REQ_MSG:
                str = "TRPV2_REQ_MSG";
                break;
            default:
                str = "0x" + SnmpUtilities.toHex(type);
        }
        return str;
    }
}
