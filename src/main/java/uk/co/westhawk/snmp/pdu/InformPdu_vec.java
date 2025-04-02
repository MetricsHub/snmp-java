// NAME
//      $RCSfile: InformPdu_vec.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.5 $
// CREATED
//      $Date: 2006/11/29 16:12:50 $
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
import java.lang.*;

/**
 * <p>
 * The InformPdu_vec class will inform a manager about a number of 
 * objects (OIDs), based on the Inform request.
 * </p>
 *
 * <p>
 * Specify with <code>addOid()</code> the OIDs that should be informed with this
 * InformPdu request. No more than <code>count</code> (see constructor) 
 * should be added.
 * Add an Observer to the InformPdu with <code>addObserver()</code>, and 
 * send the InformPdu with <code>send()</code>.
 * </p>
 *
 * <p>
 * Note, this PDU should be sent to port 162 (the default trap port) by
 * default. You will have to create a SnmpContext with the
 * ListeningContextFace.DEFAULT_TRAP_PORT as parameter!
 * </p>
 *
 * <p>
 * Inform Requests
 * are sent between managers. It is a kind of 'acknowlegded' trap since
 * the receiving end should send a Response Pdu as reply.
 * The varbind list has the same elements as the TrapPduv2.
 * </p>
 *
 * @see Pdu#addOid
 * @see Pdu#send
 * @see varbind
 * @see ListeningContextFace#DEFAULT_TRAP_PORT
 * @since 4_12
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.5 $ $Date: 2006/11/29 16:12:50 $
 */
public class InformPdu_vec extends InformPdu {
    private static final String version_id = "@(#)$Id: InformPdu_vec.java,v 3.5 2006/11/29 16:12:50 birgit Exp $ Copyright Westhawk Ltd";

    varbind[] value;

    /**
     * Constructor.
     *
     * @param con   The context of the request
     * @param count The number of OIDs to be get
     */
    public InformPdu_vec(SnmpContextBasisFace con, int count) {
        super(con);
        value = new varbind[count];
    }

    /**
     * The value of the request is set. This will be called by
     * InformPdu.fillin().
     *
     * @param n   the index of the value
     * @param var the value
     * @see Pdu#new_value
     */
    protected void new_value(int n, varbind var) {
        if (n < value.length) {
            value[n] = var;
        }
    }

    /**
     * This method notifies all observers.
     * This will be called by InformPdu.fillin().
     * 
     * <p>
     * If no exception occurred whilst receiving the response, the Object to the
     * update() method of the Observer will be an array of
     * varbinds, so they may contains any AsnObject type.
     * If an exception occurred, that exception will be passed as the Object
     * to the update() method.
     * </p>
     */
    protected void tell_them() {
        notifyObservers(value);
    }

}
