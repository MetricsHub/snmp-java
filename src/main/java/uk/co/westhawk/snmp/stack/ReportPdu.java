// NAME
//      $RCSfile: ReportPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.3 $
// CREATED
//      $Date: 2006/01/17 17:59:34 $
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
 * This class represents the ASN SNMP Report PDU object.
 * This class is used when requests are received that try to discover our
 * SNMPv3 timeliness. This will only be the case then the stack acts as
 * authoritative engine.
 *
 * <p>
 * Reports are not used (as far as we know) in normal
 * manager - agent (authoritative - non authoritative) communication.
 * Hence the reason why this stack does not support them in any other
 * way.
 * </p>
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.3 $ $Date: 2006/01/17 17:59:34 $
 */
public class ReportPdu extends Pdu {
    private static final String version_id = "@(#)$Id: ReportPdu.java,v 3.3 2006/01/17 17:59:34 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Constructor.
     * The requestPdu is used to copy the necessary IDs to this PDU.
     *
     * @param con        The context of the PDU
     * @param requestPdu The original Request PDU
     */
    public ReportPdu(SnmpContextBasisFace con, Pdu requestPdu) {
        super(con);
        setMsgType(AsnObject.GET_RPRT_MSG);
        req_id = requestPdu.req_id;
        snmpv3MsgId = requestPdu.snmpv3MsgId;
    }

    /**
     * Sets the error status of this PDU. This indicates that an exception
     * has occurred while processing the original request.
     *
     * @see SnmpConstants#SNMP_ERR_NOERROR
     * @see SnmpConstants#SNMP_ERR_TOOBIG
     * @see SnmpConstants#SNMP_ERR_NOSUCHNAME
     * @see SnmpConstants#SNMP_ERR_BADVALUE
     * @see SnmpConstants#SNMP_ERR_READONLY
     * @see SnmpConstants#SNMP_ERR_GENERR
     * @see SnmpConstants#SNMP_ERR_NOACCESS
     * @see SnmpConstants#SNMP_ERR_WRONGTYPE
     * @see SnmpConstants#SNMP_ERR_WRONGLENGTH
     * @see SnmpConstants#SNMP_ERR_WRONGENCODING
     * @see SnmpConstants#SNMP_ERR_WRONGVALUE
     * @see SnmpConstants#SNMP_ERR_NOCREATION
     * @see SnmpConstants#SNMP_ERR_INCONSISTENTVALUE
     * @see SnmpConstants#SNMP_ERR_RESOURCEUNAVAILABLE
     * @see SnmpConstants#SNMP_ERR_COMMITFAILED
     * @see SnmpConstants#SNMP_ERR_UNDOFAILED
     * @see SnmpConstants#SNMP_ERR_AUTHORIZATIONERR
     * @see SnmpConstants#SNMP_ERR_NOTWRITABLE
     * @see SnmpConstants#SNMP_ERR_INCONSISTENTNAME
     */
    public void getErrorStatus(int errorStatus) {
        errstat = errorStatus;
    }

    /**
     * Sets the error index of this PDU. When the error status is not
     * SNMP_ERR_NOERROR, it indicates the index of the variable in the
     * varbind list that caused the exception.
     */
    public void getErrorIndex(int errorIndex) {
        errind = errorIndex;
    }

    /**
     * The Report PDU does not get a response back. So it should be sent once.
     */
    void transmit() {
        transmit(false);
    }

    /**
     * Returns the string representation of this object.
     *
     * @return The string of the PDU
     */
    public String toString() {
        return super.toString(true);
    }

    /**
     * Has no meaning, since there is not response.
     */
    protected void new_value(int n, varbind res) {
    }

    /**
     * Has no meaning, since there is not response.
     */
    protected void tell_them() {
    }

    /**
     * Returns that this type of PDU is <em>not</em> expecting a response.
     * This method is used in AbstractSnmpContext to help determine whether
     * or not to start a thread that listens for a response when sending this
     * PDU.
     * The default is <em>false</em>.
     *
     * @return true if a response is expected, false if not.
     */
    protected boolean isExpectingResponse() {
        return false;
    }

}
