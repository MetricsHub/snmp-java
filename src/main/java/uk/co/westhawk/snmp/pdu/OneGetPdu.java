// NAME
//      $RCSfile: OneGetPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.13 $
// CREATED
//      $Date: 2006/01/17 17:49:53 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1996 - 1998 by Westhawk Ltd (www.westhawk.nl)
 * Copyright (C) 1998 - 2006 by Westhawk Ltd 
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
 * The OneGetPdu class will ask for one (1) object (OID), based on
 * the Get request.
 * </p>
 *
 * <p>
 * Unless an exception occurred the Object to the update() method of the
 * Observer will be a varbind, so any AsnObject type can be returned.
 * In the case of an exception, that exception will be passed.
 * </p>
 *
 * <p>
 * For SNMPv3: The receiver of a request PDU acts as the authoritative engine.
 * </p>
 *
 * @see varbind
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.13 $ $Date: 2006/01/17 17:49:53 $
 */
public class OneGetPdu extends GetPdu {
    private static final String version_id = "@(#)$Id: OneGetPdu.java,v 3.13 2006/01/17 17:49:53 birgit Exp $ Copyright Westhawk Ltd";

    varbind var;

    /**
     * Constructor.
     *
     * @param con The context of the request
     */
    public OneGetPdu(SnmpContextBasisFace con) {
        super(con);
    }

    /**
     * Constructor that will send the request immediately. No Observer
     * is set.
     *
     * @param con the SnmpContextBasisFace
     * @param oid the oid
     */
    public OneGetPdu(SnmpContextBasisFace con, String oid)
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
    public OneGetPdu(SnmpContextBasisFace con, String oid, Observer o)
            throws PduException, java.io.IOException {
        super(con);
        if (o != null) {
            addObserver(o);
        }
        addOid(oid);
        send();
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
            var = a_var;
        }
    }

    /**
     * This method notifies all observers.
     * This will be called by Pdu.fillin().
     * 
     * <p>
     * Unless an exception occurred the Object to the update() method of the
     * Observer will be a varbind, so any AsnObject type can be returned.
     * In the case of an exception, that exception will be passed.
     * </p>
     */
    protected void tell_them() {
        notifyObservers(var);
    }

}
