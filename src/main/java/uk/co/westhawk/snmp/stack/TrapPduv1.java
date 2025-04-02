// NAME
//      $RCSfile: TrapPduv1.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.14 $
// CREATED
//      $Date: 2007/10/17 10:44:09 $
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
import java.util.*;
import java.io.*;
import java.net.*;

import uk.co.westhawk.snmp.util.*;

/**
 * This class represents the ASN SNMPv1 Trap PDU object. 
 * See <a href="http://www.ietf.org/rfc/rfc1157.txt">RFC1157-SNMP</a>.
 * 
 * <p>
 * Note that the SNMPv1 Trap PDU is the only PDU with a different PDU
 * format. It has additional fields like <code>enterprise</code>,
 * <code>ipAddress</code>, <code>genericTrap</code>,
 * <code>specificTrap</code> and <code>timeTicks</code>.
 * </p>
 *
 * @see TrapPduv2
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.14 $ $Date: 2007/10/17 10:44:09 $
 */
public class TrapPduv1 extends Pdu {
    private static final String version_id = "@(#)$Id: TrapPduv1.java,v 3.14 2007/10/17 10:44:09 birgita Exp $ Copyright Westhawk Ltd";

    private String enterprise;
    private byte[] IpAddress;
    private int generic_trap;
    private int specific_trap;
    private long timeTicks;

    private final static String[] genericTrapStrings = {
            "Cold Start",
            "Warm Start",
            "Link Down",
            "Link Up",
            "Authentication Failure",
            "EGP Neighbor Loss",
            "Enterprise Specific",
    };

    /**
     * Constructor.
     *
     * @param con The context (v1) of the TrapPduv1
     * @see SnmpContext
     */
    public TrapPduv1(SnmpContext con) {
        super(con);
        setMsgType(AsnObject.TRP_REQ_MSG);

        generic_trap = AsnObject.SNMP_TRAP_WARMSTART;
    }

    /**
     * Constructor.
     *
     * @param con The context pool (v1) of the TrapPduv1
     * @see SnmpContext
     */
    public TrapPduv1(SnmpContextPool con) {
        super(con);
        setMsgType(AsnObject.TRP_REQ_MSG);

        generic_trap = AsnObject.SNMP_TRAP_WARMSTART;
    }

    /**
     * Sets the type of object generating the trap. This parameter is based on
     * the sysObjectId.
     */
    public void setEnterprise(String var) {
        enterprise = var;
    }

    /**
     * Returns the type of object generating the trap.
     */
    public String getEnterprise() {
        return enterprise;
    }

    /**
     * Sets the IP Address of the object generating the trap.
     */
    public void setIpAddress(byte[] var) {
        IpAddress = var;
    }

    /**
     * Returns the IP Address of the object generating the trap.
     */
    public byte[] getIpAddress() {
        return IpAddress;
    }

    /**
     * Sets the generic trap type. By default the warmStart is sent.
     *
     * @see SnmpConstants#SNMP_TRAP_COLDSTART
     * @see SnmpConstants#SNMP_TRAP_WARMSTART
     * @see SnmpConstants#SNMP_TRAP_LINKDOWN
     * @see SnmpConstants#SNMP_TRAP_LINKUP
     * @see SnmpConstants#SNMP_TRAP_AUTHFAIL
     * @see SnmpConstants#SNMP_TRAP_EGPNEIGHBORLOSS
     * @see SnmpConstants#SNMP_TRAP_ENTERPRISESPECIFIC
     */
    public void setGenericTrap(int var) {
        generic_trap = var;
    }

    /**
     * Returns the generic trap type.
     */
    public int getGenericTrap() {
        return generic_trap;
    }

    /**
     * Returns the string representation of the generic trap.
     *
     * @return the generic trap string
     * @see #getGenericTrap
     */
    public String getGenericTrapString() {
        String trapStr = "";
        if (generic_trap > -1 && generic_trap < genericTrapStrings.length) {
            trapStr = genericTrapStrings[generic_trap];
        }
        return trapStr;
    }

    /**
     * Sets the specific trap code.
     */
    public void setSpecificTrap(int var) {
        specific_trap = var;
    }

    /**
     * Returns the specific trap code.
     */
    public int getSpecificTrap() {
        return specific_trap;
    }

    /**
     * Sets the sysUpTime of the agent.
     *
     * <p>
     * <a href="http://www.ietf.org/rfc/rfc1155.txt">RFC1155-SMI</a>: TimeTicks:
     * <br/>
     * This application-wide type represents a non-negative integer which
     * counts the time in hundredths of a second since some epoch. When
     * object types are defined in the MIB which use this ASN.1 type, the
     * description of the object type identifies the reference epoch.
     * </p>
     */
    public void setTimeTicks(long var) {
        timeTicks = var;
    }

    /**
     * Returns the sysUpTime of the agent.
     *
     * @see #setTimeTicks
     */
    public long getTimeTicks() {
        return timeTicks;
    }

    /**
     * Sends the TrapPduv1. Since the Trap v1 PDU has a different format
     * (sigh), a different encoding message has to be called.
     *
     * Note that all properties of the context have to be set before this
     * point.
     */
    public boolean send() throws IOException, PduException {
        if (added == false) {
            // Moved this statement from the constructor because it
            // conflicts with the way the SnmpContextXPool works.
            added = context.addPdu(this);
        }
        Enumeration vbs = reqVarbinds.elements();
        encodedPacket = ((SnmpContext) context).encodePacket(msg_type, enterprise,
                IpAddress, generic_trap, specific_trap, timeTicks, vbs);
        addToTrans();
        return added;
    }

    /**
     * The trap PDU does not get a response back. So it should be sent once.
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
        String iPAddressStr = "null";
        if (IpAddress != null) {
            iPAddressStr = (new AsnOctets(IpAddress)).toIpAddress();
        }
        StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append("[");
        buffer.append("context=").append(context);
        buffer.append(", reqId=").append(getReqId());
        buffer.append(", msgType=0x").append(SnmpUtilities.toHex(msg_type));
        buffer.append(", enterprise=").append(enterprise);
        buffer.append(", IpAddress=").append(iPAddressStr);
        buffer.append(", generic_trap=").append(getGenericTrapString());
        buffer.append(", specific_trap=").append(specific_trap);
        buffer.append(", timeTicks=").append(timeTicks);

        buffer.append(", reqVarbinds=");
        if (reqVarbinds != null) {
            int sz = reqVarbinds.size();
            buffer.append("[");
            for (int i = 0; i < sz; i++) {
                varbind var = (varbind) reqVarbinds.elementAt(i);
                buffer.append(var.toString()).append(", ");
            }
            buffer.append("]");
        } else {
            buffer.append("null");
        }

        buffer.append(", respVarbinds=");
        if (respVarbinds != null) {
            int sz = respVarbinds.size();
            buffer.append("[");
            for (int i = 0; i < sz; i++) {
                varbind var = (varbind) respVarbinds.elementAt(i);
                buffer.append(var.toString()).append(", ");
            }
            buffer.append("]");
        } else {
            buffer.append("null");
        }

        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Fill in the received trap.
     * 
     * @see Pdu#getResponseVarbinds()
     */
    void fillin(AsnTrapPduv1Sequence seq) {
        if (seq != null) {
            try {
                AsnSequence varBind = seq.getVarBind();
                int size = varBind.getObjCount();
                respVarbinds = new Vector(size, 1);
                for (int n = 0; n < size; n++) {
                    Object obj = varBind.getObj(n);
                    if (obj instanceof AsnSequence) {
                        AsnSequence varSeq = (AsnSequence) obj;
                        try {
                            varbind vb = new varbind(varSeq);
                            respVarbinds.addElement(vb);
                        } catch (IllegalArgumentException exc) {
                        }
                    }
                }

                setEnterprise(seq.getEnterprise());
                setIpAddress(seq.getIPAddress());
                setGenericTrap(seq.getGenericTrap());
                setSpecificTrap(seq.getSpecificTrap());
                setTimeTicks(seq.getTimeTicks());
            } catch (DecodingException exc) {
                setErrorStatus(AsnObject.SNMP_ERR_DECODINGASN_EXC, exc);
            }
        }
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
