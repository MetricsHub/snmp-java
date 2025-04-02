// NAME
//      $RCSfile: OneGetBulkPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.9 $
// CREATED
//      $Date: 2006/01/17 17:43:53 $
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
import java.util.*;

/**
 * <p>
 * The OneGetBulkPdu class performs a getBulkRequest and collects 
 * the response varbinds into a Vector.
 * </p>
 *
 * <p>
 * If no exception occurred whilst receiving the response, the Object to the 
 * update() method of the Observer will be an Vector of
 * varbinds, so they may contains any AsnObject type.
 * If an exception occurred, that exception will be passed as the Object
 * to the update() method.
 * </p>
 *
 * <p>
 * For SNMPv3: The receiver of a request PDU acts as the authoritative engine.
 * </p>
 *
 * @see varbind
 * @see Vector
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.9 $ $Date: 2006/01/17 17:43:53 $
 */
public class OneGetBulkPdu extends GetBulkPdu {
    private static final String version_id = "@(#)$Id: OneGetBulkPdu.java,v 3.9 2006/01/17 17:43:53 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * The GetBulk request is the only request that will return more
     * variables than you've sent.
     */
    Vector vars;

    /**
     * Constructor.
     *
     * @param con The context (v2c or v3) of the PDU
     */
    public OneGetBulkPdu(SnmpContextBasisFace con) {
        super(con);
        vars = new Vector();
    }

    /**
     * Returns a vector with the response varbinds.
     */
    public Vector getVarbinds() {
        return vars;
    }

    /**
     * The value of the request is set. This will be called by
     * Pdu.fillin().
     *
     * @param n     the index of the value
     * @param a_var the value
     * @see Pdu#new_value
     */
    protected void new_value(int n, varbind a_var) {
        if (n == 0) {
            vars = new Vector();
        }
        vars.addElement(a_var);
    }

    /**
     * This method notifies all observers.
     * This will be called by Pdu.fillin().
     * 
     * <p>
     * If no exception occurred whilst receiving the response, the Object to the
     * update() method of the Observer will be an Vector of
     * varbinds, so they may contains any AsnObject type.
     * If an exception occurred, that exception will be passed as the Object
     * to the update() method.
     * </p>
     *
     * @see Vector
     */
    protected void tell_them() {
        notifyObservers(vars);
    }

}
