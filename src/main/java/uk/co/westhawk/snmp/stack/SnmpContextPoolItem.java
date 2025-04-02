// NAME
//      $RCSfile: SnmpContextPoolItem.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.4 $
// CREATED
//      $Date: 2006/01/17 17:33:04 $
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

import java.util.*;
import uk.co.westhawk.snmp.event.*;

/**
 * This class contains one context and one reference counter.
 * The reference counter
 * maintains how many objects reference this context. 
 * It is a helper class for the context pools, to improve the
 * synchronisation. 
 *
 * @see SnmpContextPool
 * @see SnmpContextv2cPool
 * @see SnmpContextv3Pool
 * @since 4_12
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.4 $ $Date: 2006/01/17 17:33:04 $
 */
class SnmpContextPoolItem {
    private static final String version_id = "@(#)$Id: SnmpContextPoolItem.java,v 3.4 2006/01/17 17:33:04 birgit Exp $ Copyright Westhawk Ltd";

    private SnmpContextBasisFace context = null;
    private int counter = 0;

    /**
     * Constructor.
     *
     * @param con The context
     */
    SnmpContextPoolItem(SnmpContextBasisFace con) {
        context = con;
        counter = 0;
    }

    SnmpContextBasisFace getContext() {
        return context;
    }

    int getCounter() {
        return counter;
    }

    void setCounter(int i) {
        counter = i;
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return The string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("SnmpContextPoolItem[");
        buffer.append("context=").append(context.toString());
        buffer.append(", counter=").append(counter);
        buffer.append("]");
        return buffer.toString();
    }

}
