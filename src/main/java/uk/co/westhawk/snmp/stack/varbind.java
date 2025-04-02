// NAME
//      $RCSfile: varbind.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.10 $
// CREATED
//      $Date: 2007/10/17 10:47:47 $
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
 * <p>
 * This class represents the variable bindings to a PDU.
 * A variable consists of a name (an AsnObjectId) and a value (an
 * AsnObject)
 * </p>
 *
 * <p>
 * The varbind is usually passed to the Observers of the PDU when
 * notifying them. 
 * </p>
 *
 * @see Pdu#addOid(varbind)
 * @see Pdu#addOid(String, AsnObject)
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.10 $ $Date: 2007/10/17 10:47:47 $
 */
public class varbind extends Object {
    private static final String version_id = "@(#)$Id: varbind.java,v 3.10 2007/10/17 10:47:47 birgita Exp $ Copyright Westhawk Ltd";

    private AsnObjectId name;
    private AsnObject value;

    /**
     * Constructor.
     * It will clone the varbind given as parameter.
     *
     * @param var The varbind
     */
    public varbind(varbind var) {
        name = var.name;
        value = var.value;
    }

    /**
     * Constructor.
     * The name will be set to the Oid, the value will be set to
     * AsnNull. This is usually used in Get or GetNext requests.
     *
     * @param Oid The oid
     * @see AsnNull
     */
    public varbind(String Oid) {
        this(new AsnObjectId(Oid), new AsnNull());
    }

    /**
     * Constructor.
     * The name will be set to the Oid, the value will be set to
     * AsnNull. This is usually used in Get or GetNext requests.
     *
     * @param Oid The oid
     * @see AsnNull
     */
    public varbind(AsnObjectId Oid) {
        this(Oid, new AsnNull());
    }

    /**
     * Constructor.
     * The name and value will be set.
     * This is usually used in Set requests.
     *
     * @param Oid The oid
     * @param val The value for the varbind
     */
    public varbind(String Oid, AsnObject val) {
        this(new AsnObjectId(Oid), val);
    }

    /**
     * Constructor.
     * The name and value will be set.
     * This is usually used in Set requests.
     *
     * @param Oid The oid
     * @param val The value for the varbind
     * @since 4_12
     */
    public varbind(AsnObjectId Oid, AsnObject val) {
        name = Oid;
        value = val;
    }

    varbind(AsnSequence vb)
            throws IllegalArgumentException {
        Object obj = vb.getObj(0);
        if (obj instanceof AsnObjectId) {
            name = (AsnObjectId) obj;
            value = vb.getObj(1);
        } else {
            String msg = "First object should be AsnObjectId, but is ";
            if (obj != null) {
                msg += obj.getClass().getName();
            } else {
                msg += "null";
            }
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Returns the oid, this is the name of the varbind.
     *
     * @return the name as an AsnObjectId
     */
    public AsnObjectId getOid() {
        return name;
    }

    /**
     * Returns the value of the varbind.
     *
     * @return the value as AsnObject
     */
    public AsnObject getValue() {
        return value;
    }

    Object setValue(AsnSequence vb)
            throws IllegalArgumentException {
        varbind tmp = new varbind(vb);
        name = tmp.name;
        value = tmp.value;
        return value;
    }

    /**
     * Returns the string representation of the varbind.
     *
     * @return The string of the varbind
     */
    public String toString() {
        return (name.toString() + ": " + value.toString());
    }
}
