// NAME
//      $RCSfile: OneIntPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.14 $
// CREATED
//      $Date: 2006/11/29 16:12:50 $
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
 * The OneIntPdu class will ask for one (1) object (oid) of the 
 * AsnInteger type, based on the Get request.
 * </p>
 *
 * <p>
 * Unless an exception occurred the Object to the update() method of the
 * Observer will be an Integer.
 * In the case of an exception, that exception will be passed.
 * </p>
 *
 * <p>
 * For SNMPv3: The receiver of a request PDU acts as the authoritative engine.
 * </p>
 *
 * @see GetPdu_vec
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.14 $ $Date: 2006/11/29 16:12:50 $
 */
public class OneIntPdu extends GetPdu {
    private static final String version_id = "@(#)$Id: OneIntPdu.java,v 3.14 2006/11/29 16:12:50 birgit Exp $ Copyright Westhawk Ltd";

    Integer value;

    /**
     * Constructor.
     *
     * @param con The context of the request
     */
    public OneIntPdu(SnmpContextBasisFace con) {
        super(con);
    }

    /**
     * Constructor that will send the request immediately. No Observer
     * is set.
     *
     * @param con the SnmpContextBasisFace
     * @param oid the oid
     */
    public OneIntPdu(SnmpContextBasisFace con, String oid)
            throws PduException, java.io.IOException {
        this(con, oid, null);
    }

    /**
     * Constructor that will send the request immediately.
     *
     * @param con the SnmpContextBasisFace
     * @param oid the oid
     * @param o   the Observer that will be notified when the answer is received
     */
    public OneIntPdu(SnmpContextBasisFace con, String oid, Observer o)
            throws PduException, java.io.IOException {
        super(con);
        if (o != null) {
            addObserver(o);
        }
        addOid(oid);
        send();
    }

    /**
     * Returns the value (the answer) of this request.
     *
     * @return the value
     */
    public Integer getValue() {
        return value;
    }

    /**
     * The value of the request is set. This will be called by
     * Pdu.fillin().
     *
     * @param n   the index of the value
     * @param res the value
     * @see Pdu#new_value
     */
    protected void new_value(int n, varbind res) {
        AsnObject val = res.getValue();
        if (val instanceof AsnInteger) {
            AsnInteger va = (AsnInteger) res.getValue();
            if (n == 0) {
                value = new Integer(va.getValue());
            }
        } else {
            value = null;
        }
    }

    /**
     * This method notifies all observers.
     * This will be called by Pdu.fillin().
     * 
     * <p>
     * Unless an exception occurred the Object to the update() method of the
     * Observer will be an Integer.
     * In the case of an exception, that exception will be passed.
     * </p>
     */
    protected void tell_them() {
        notifyObservers(value);
    }

}
