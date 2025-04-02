// NAME
//      $RCSfile: InformPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.5 $
// CREATED
//      $Date: 2006/11/29 16:23:33 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2002 - 2006 by Westhawk Ltd
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
 * This class represents the SNMP Inform Request Pdu. This request has
 * been added in SNMPv2c, hence is not supported by SNMPv1 agents.
 *
 * <p>
 * Inform Requests
 * are sent between managers. It is an acknowlegded trap since
 * the receiving end should send a Response Pdu as reply. 
 * The varbind list has the same elements as the TrapPduv2.<br/>
 * See <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>
 * </p>
 *
 * <p>
 * Note this PDU should be sent to port 162 (the default trap port) by
 * default. You will have to create a SnmpContext with the 
 * ListeningContextFace.DEFAULT_TRAP_PORT as parameter!
 * </p>
 *
 * <p>
 * For SNMPv3: The sender of an inform PDU acts as the authoritative engine.
 * </p>
 *
 * @see TrapPduv2
 * @see ListeningContextFace#DEFAULT_TRAP_PORT
 * @since 4_12
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.5 $ $Date: 2006/11/29 16:23:33 $
 */
public class InformPdu extends Pdu {
    private static final String version_id = "@(#)$Id: InformPdu.java,v 3.5 2006/11/29 16:23:33 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Constructor.
     *
     * @param con The context (v2c or v3) of the Pdu
     */
    public InformPdu(SnmpContextBasisFace con) {
        super(con);
        setMsgType(AsnObject.INFORM_REQ_MSG);
    }

}
