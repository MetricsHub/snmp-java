// NAME
//      $RCSfile: TrapPduv2.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.11 $
// CREATED
//      $Date: 2006/03/23 14:54:10 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

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
 * This class represents the ASN SNMP v2c (and higher) Trap PDU object.
 * This PDU is not supported in SNMPv1.
 *
 * <p>
 * See <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>;<br/>
 * The variable bindings list contains the following pairs of object
 * names and values:
   <ul>
      <li>sysUpTime.0 
          (<a href="http://www.ietf.org/rfc/rfc3418.txt">SNMPv2-MIB</a>)
      </li>
      <li>snmpTrapOID.0: part of the trap group in the SNMPv2 MIB
          (<a href="http://www.ietf.org/rfc/rfc3418.txt">SNMPv2-MIB</a>)
      </li>
      <li>If the OBJECTS clause is present in the corresponding
          invocation of the macro NOTIFICATION-TYPE, then each corresponding
          variable and its value are copied to the variable-bindings field.
      </li>
      <li>Additional variables may be included at the option of the agent.
      </li>
   </ul>
 *
 * <p>
 * For SNMPv3: The sender of a trap PDU acts as the authoritative engine.
 * </p>
 *
 * @see TrapPduv1
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.11 $ $Date: 2006/03/23 14:54:10 $
 */
public class TrapPduv2 extends Pdu {
    private static final String version_id = "@(#)$Id: TrapPduv2.java,v 3.11 2006/03/23 14:54:10 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Constructor.
     *
     * @param con The context (v2c or v3) of the PDU
     * @throws IllegalArgumentException if the context version is
     *                                  SNMPv1
     */
    public TrapPduv2(SnmpContextBasisFace con) {
        super(con);
        setMsgType(AsnObject.TRPV2_REQ_MSG);

        if (con.getVersion() == SnmpConstants.SNMP_VERSION_1) {
            throw new IllegalArgumentException("A TrapPduv2"
                    + " can only be sent with an SNMPv2c or SNMPv3 context."
                    + " NOT with an SNMPv1 context!");
        }
    }

    /**
     * The trap PDU does not get a response back. So it should be sent once.
     *
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
     * @since 4_14
     */
    protected boolean isExpectingResponse() {
        return false;
    }

}
