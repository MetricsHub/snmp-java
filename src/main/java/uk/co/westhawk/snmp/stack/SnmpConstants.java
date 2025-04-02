// NAME
//      $RCSfile: SnmpConstants.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.10 $
// CREATED
//      $Date: 2006/03/23 14:54:10 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//
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

/*
 * Copyright (C) 2001 - 2006 by Westhawk Ltd
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

/**
 * This interface contains most of the constants used in the stack.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.10 $ $Date: 2006/03/23 14:54:10 $
 */
public interface SnmpConstants {
    static final String version_id = "@(#)$Id: SnmpConstants.java,v 3.10 2006/03/23 14:54:10 birgit Exp $ Copyright Westhawk Ltd";

    public static final byte ASN_BOOLEAN = (byte) (0x01);
    public static final byte ASN_INTEGER = (byte) (0x02);
    public static final byte ASN_BIT_STR = (byte) (0x03);
    public static final byte ASN_OCTET_STR = (byte) (0x04);
    public static final byte ASN_NULL = (byte) (0x05);
    public static final byte ASN_OBJECT_ID = (byte) (0x06);
    public static final byte ASN_SEQUENCE = (byte) (0x10);
    public static final byte ASN_SET = (byte) (0x11);

    public static final byte ASN_UNIVERSAL = (byte) (0x00);
    public static final byte ASN_APPLICATION = (byte) (0x40);
    public static final byte ASN_CONTEXT = (byte) (0x80);
    public static final byte ASN_PRIVATE = (byte) (0xC0);

    public static final byte ASN_PRIMITIVE = (byte) (0x00);
    public static final byte ASN_CONSTRUCTOR = (byte) (0x20);

    public static final byte ASN_LONG_LEN = (byte) (0x80);
    public static final byte ASN_EXTENSION_ID = (byte) (0x1F);
    public static final byte ASN_BIT8 = (byte) (0x80);

    public static final byte INTEGER = (byte) ASN_INTEGER;
    public static final byte STRING = (byte) ASN_OCTET_STR;
    public static final byte OBJID = (byte) ASN_OBJECT_ID;
    public static final byte NULLOBJ = (byte) ASN_NULL;

    /** IPv4 address only */
    // RFC 2578:
    public static final byte IPADDRESS = (byte) (ASN_APPLICATION | 0);
    public static final byte COUNTER = (byte) (ASN_APPLICATION | 1);
    public static final byte GAUGE = (byte) (ASN_APPLICATION | 2);
    public static final byte TIMETICKS = (byte) (ASN_APPLICATION | 3);
    public static final byte OPAQUE = (byte) (ASN_APPLICATION | 4);
    public static final byte COUNTER64 = (byte) (ASN_APPLICATION | 6);

    /**
     * for OSI NSAP addresses (see
     * <a href="http://www.ietf.org/rfc/rfc1442.txt">RFC 1442</a>,
     * is obsoleted by
     * <a href="http://www.ietf.org/rfc/rfc2578.txt">RFC 2578</a>).
     */
    public static final byte NSAP_ADDRESS = (byte) (ASN_APPLICATION | 5);

    /**
     * an unsigned 32-bit quantity (see
     * <a href="http://www.ietf.org/rfc/rfc1902.txt">RFC 1902</a>,
     * is obsoleted by
     * <a href="http://www.ietf.org/rfc/rfc2578.txt">SNMPv2-SMI</a>).
     *
     * <p>
     * Was called UINTEGER32. Renamed it in version 4_13.
     * </p>
     */
    public static final byte OBSOLETED_RFC1442_UINTEGER32 = (byte) (ASN_APPLICATION | 7);

    /**
     * an unsigned 32-bit quantity. This equals GAUGE. (see
     * <a href="http://www.ietf.org/rfc/rfc2578.txt">SNMPv2-SMI</a>).
     */
    public static final byte SMI_V2_UINTEGER32 = GAUGE;

    /**
     * Value for the <code>GenericTrap</code> field in SNMPv1 Trap.
     */
    public static final byte SNMP_TRAP_COLDSTART = (byte) (0x0);
    /**
     * Value for the <code>GenericTrap</code> field in SNMPv1 Trap.
     */
    public static final byte SNMP_TRAP_WARMSTART = (byte) (0x1);
    /**
     * Value for the <code>GenericTrap</code> field in SNMPv1 Trap.
     */
    public static final byte SNMP_TRAP_LINKDOWN = (byte) (0x2);
    /**
     * Value for the <code>GenericTrap</code> field in SNMPv1 Trap.
     */
    public static final byte SNMP_TRAP_LINKUP = (byte) (0x3);
    /**
     * Value for the <code>GenericTrap</code> field in SNMPv1 Trap.
     */
    public static final byte SNMP_TRAP_AUTHFAIL = (byte) (0x4);
    /**
     * Value for the <code>GenericTrap</code> field in SNMPv1 Trap.
     */
    public static final byte SNMP_TRAP_EGPNEIGHBORLOSS = (byte) (0x5);
    /**
     * Value for the <code>GenericTrap</code> field in SNMPv1 Trap.
     */
    public static final byte SNMP_TRAP_ENTERPRISESPECIFIC = (byte) (0x6);

    /**
     * Indicated the agent does not implement the object referred to by
     * this object identifier.
     * Used in varbind list in response to SNMPv2c, SNMPv3 getRequest.
     */
    public static final byte SNMP_VAR_NOSUCHOBJECT = (byte) (ASN_CONTEXT | ASN_PRIMITIVE | 0x0);
    /**
     * Indicated that this object does not exists for this operation.
     * Used in varbind list in response to SNMPv2c, SNMPv3 getRequest.
     */
    public static final byte SNMP_VAR_NOSUCHINSTANCE = (byte) (ASN_CONTEXT | ASN_PRIMITIVE | 0x1);
    /**
     * Indicated an attempt to reference an object identifier that is
     * beyond the end of the MIB at the agent.
     * Used in varbind list in response to SNMPv2c, SNMPv3 getRequest or
     * getBulkRequest.
     */
    public static final byte SNMP_VAR_ENDOFMIBVIEW = (byte) (ASN_CONTEXT | ASN_PRIMITIVE | 0x2);

    /**
     * No error in the PDU response.
     * Used in SNMPv1, SNMPv2c, SNMPv3.
     */
    public static final byte SNMP_ERR_NOERROR = (byte) (0x00);
    /**
     * The size of the generated response PDU would exceed a local
     * limitation.
     * Used in SNMPv1, SNMPv2c, SNMPv3.
     */
    public static final byte SNMP_ERR_TOOBIG = (byte) (0x01);
    /**
     * The object is not available or the object's name does not exactly
     * match.
     * Only used in SNMPv1.
     */
    public static final byte SNMP_ERR_NOSUCHNAME = (byte) (0x02);
    /**
     * The object does not manifest a type, length, and value
     * that is consistent with that required for the variable.
     * Only used in response to SNMPv1 SetRequest.
     */
    public static final byte SNMP_ERR_BADVALUE = (byte) (0x03);
    /**
     * The object can not be set, only read.
     * Only used in response to SNMPv1 SetRequest.
     */
    public static final byte SNMP_ERR_READONLY = (byte) (0x04);
    /**
     * The value of an object cannot be retrieved for reasons
     * not covered by any of other errors.
     * Used in SNMPv1, SNMPv2c, SNMPv3.
     */
    public static final byte SNMP_ERR_GENERR = (byte) (0x05);

    /**
     * The variable binding's name specifies an existing or
     * non-existent variable to which this request is/would be denied
     * access because it is/would not be in the appropriate MIB view.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_NOACCESS = (byte) (0x06);
    /**
     * The variable binding's value field specifies a type which is
     * inconsistent with that required for all variables which share the
     * same OBJECT IDENTIFIER prefix as the variable binding's name.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_WRONGTYPE = (byte) (0x07);
    /**
     * The variable binding's value field specifies a length which is
     * inconsistent with that required for all variables which share the
     * same OBJECT IDENTIFIER prefix as the variable binding's name.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_WRONGLENGTH = (byte) (0x08);
    /**
     * Otherwise, if the variable binding's value field contains an ASN.1
     * encoding which is inconsistent with that field's ASN.1 tag.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_WRONGENCODING = (byte) (0x09);
    /**
     * The variable binding's value field specifies a value which could
     * under no circumstances be assigned to the variable.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_WRONGVALUE = (byte) (0x0A);
    /**
     * The variable binding's name specifies a variable which does not
     * exist and could not ever be created (even though some variables
     * sharing the same OBJECT IDENTIFIER prefix might under some
     * circumstances be able to be created).
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_NOCREATION = (byte) (0x0B);
    /**
     * The variable binding's value field specifies a value that could
     * under other circumstances be held by the variable, but is presently
     * inconsistent or otherwise unable to be assigned to the variable.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_INCONSISTENTVALUE = (byte) (0x0C);
    /**
     * The assignment of the value specified by the variable binding's
     * value field to the specified variable requires the allocation of a
     * resource which is presently unavailable.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_RESOURCEUNAVAILABLE = (byte) (0x0D);
    /**
     * Any of the assignments failed and all other assignments were
     * undone.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_COMMITFAILED = (byte) (0x0E);
    /**
     * Any of the assignments failed and all other assignments were
     * undone, but is was not possible to undo all the assignments.
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_UNDOFAILED = (byte) (0x0F);
    /**
     * Users is has no access privileges.
     * Used in SNMPv2c, SNMPv3.
     */
    public static final byte SNMP_ERR_AUTHORIZATIONERR = (byte) (0x10);
    /**
     * <ul>
     * <li>
     * There are no variables which share the same OBJECT IDENTIFIER
     * prefix as the variable binding's name, and which are able to be
     * created or modified no matter what new value is specified.
     * </li>
     * <li>
     * The variable binding's name specifies a variable which exists but
     * can not be modified no matter what new value is specified.
     * </li>
     * </ul>
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_NOTWRITABLE = (byte) (0x11);
    /**
     * The variable binding's name specifies a variable which does not
     * exist but can not be created under the present circumstances (even
     * though it could be created under other circumstances).
     * Used in response to SNMPv2c, SNMPv3 SetRequest.
     */
    public static final byte SNMP_ERR_INCONSISTENTNAME = (byte) (0x12);

    /**
     * A general decoding exception occured whilst decoding the response.
     */
    public static final byte SNMP_ERR_DECODING_EXC = (byte) (0x13);
    /**
     * A specific decoding exception occured whilst decoding the response.
     * The response PDU being decoded has a sub length wrong (not overall
     * length necessarily), so the asn sequence can't get created.
     */
    public static final byte SNMP_ERR_DECODINGASN_EXC = (byte) (0x14);
    /**
     * A specific decoding exception occured whilst decoding the response.
     * The received overall response PDU is shorter than the PDU header
     * says it is.
     */
    public static final byte SNMP_ERR_DECODINGPKTLNGTH_EXC = (byte) (0x15);

    /**
     * The version number for SNMPv1.
     */
    public static final byte SNMP_VERSION_1 = (byte) (0x0);
    /**
     * The version number for SNMPv2c.
     */
    public static final byte SNMP_VERSION_2c = (byte) (0x1);
    /**
     * The version number for SNMPv2u.
     */
    // public static final byte SNMP_VERSION_2u =(byte)(0x2);
    /**
     * The version number for SNMPv3.
     */
    public static final byte SNMP_VERSION_3 = (byte) (0x3);

    /**
     * The GetRequest PDU type.
     */
    static final byte GET_REQ_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x0);
    /**
     * The GetNextRequest PDU type.
     */
    static final byte GETNEXT_REQ_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x1);
    /**
     * The Response PDU type.
     */
    static final byte GET_RSP_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x2);
    /**
     * The SetRequest PDU type.
     */
    static final byte SET_REQ_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x3);
    /**
     * The SNMPv1 Trap PDU type.
     */
    static final byte TRP_REQ_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x4);

    /**
     * The GetBulkRequest PDU type.
     */
    static final byte GETBULK_REQ_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x5);
    /**
     * The InformRequest PDU type.
     */
    static final byte INFORM_REQ_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x6);
    /**
     * The SNMPv2 (and SNMPv3) Trap PDU type.
     */
    static final byte TRPV2_REQ_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x7);
    /**
     * The GetReport PDU type.
     */
    static final byte GET_RPRT_MSG = (byte) (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x8);

    static final byte CONS_SEQ = (byte) (ASN_SEQUENCE | ASN_CONSTRUCTOR);

}
