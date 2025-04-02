// NAME
//      $RCSfile: AsnPrimitive.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.10 $
// CREATED
//      $Date: 2006/01/17 17:43:54 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2000 - 2006 by Westhawk Ltd
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

import java.io.*;
import java.util.*;

/**
 * This class represents the Exception values for SNMP v2c, v3:
 * SNMP_VAR_NOSUCHOBJECT, SNMP_VAR_NOSUCHINSTANCE, SNMP_VAR_ENDOFMIBVIEW
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.10 $ $Date: 2006/01/17 17:43:54 $
 * @see #SNMP_VAR_NOSUCHOBJECT
 * @see #SNMP_VAR_NOSUCHINSTANCE
 * @see #SNMP_VAR_ENDOFMIBVIEW
 */
public class AsnPrimitive extends AsnObject {
    private static final String version_id = "@(#)$Id: AsnPrimitive.java,v 3.10 2006/01/17 17:43:54 birgit Exp $ Copyright Westhawk Ltd";

    private byte type;

    /**
     * Default Constructor.
     *
     * @param t The primitive type
     * @see #SNMP_VAR_NOSUCHOBJECT
     * @see #SNMP_VAR_NOSUCHINSTANCE
     * @see #SNMP_VAR_ENDOFMIBVIEW
     */
    public AsnPrimitive(byte t) {
        type = t;
    }

    /**
     * Returns the string representation of the AsnPrimitive.
     *
     * @return The string of the AsnPrimitive
     */
    public String toString() {
        String str = "AsnPrimitive ";
        if (type == SNMP_VAR_NOSUCHOBJECT) {
            str = "No such object";
        } else if (type == SNMP_VAR_NOSUCHINSTANCE) {
            str = "No such instance";
        } else if (type == SNMP_VAR_ENDOFMIBVIEW) {
            str = "End of MIB view";
        }
        return str;
    }

    void write(OutputStream out, int pos) throws IOException {
        AsnBuildHeader(out, type, 0);
    }

    /**
     * Compares this object to the specified object. The result is
     * <code>true</code> if and only if the argument is not
     * <code>null</code> and is an <code>AsnPrimitive</code> object that
     * contains the same <code>type</code> as this object.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof AsnPrimitive) {
            return type == ((AsnPrimitive) obj).type;
        }
        return false;
    }

    /**
     * Returns a hash code for this <code>AsnPrimitive</code>.
     *
     * @return a hash code value for this object, equal to the
     *         type represented by this
     *         <code>AsnPrimitive</code> object.
     */
    public int hashCode() {
        return (int) type;
    }
}
