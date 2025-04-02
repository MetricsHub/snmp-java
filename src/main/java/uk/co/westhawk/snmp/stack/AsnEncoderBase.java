// NAME
//      $RCSfile: AsnEncoderBase.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.2 $
// CREATED
//      $Date: 2006/01/17 17:43:53 $
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
 * original version by hargrave@dellgate.us.dell.com (Jordan Hargrave)
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
import uk.co.westhawk.snmp.util.*;
import java.io.*;
import java.util.*;

/**
 * This class contains the general methods to encode a Pdu into bytes.
 * We split the original class AsnEncoder into four classes.
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.2 $ $Date: 2006/01/17 17:43:53 $
 */
class AsnEncoderBase extends Object {
    private static final String version_id = "@(#)$Id: AsnEncoderBase.java,v 3.2 2006/01/17 17:43:53 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Encode PDU itself packet into bytes.
     * The actual PDU encoding is the same for v1 to v3.
     * Except for Trapv1, but we are not implementing that.
     */
    protected AsnObject EncodePdu(byte msg_type,
            int pduId, int errstat, int errind, Enumeration ve)
            throws IOException {
        AsnObject asnPduObject, asnVBObject;

        // kind of request
        asnPduObject = new AsnSequence(msg_type);
        asnPduObject.add(new AsnInteger(pduId)); // reqid
        asnPduObject.add(new AsnInteger(errstat)); // errstat
        asnPduObject.add(new AsnInteger(errind)); // errindex

        // Create VarbindList sequence
        AsnObject asnVBLObject = asnPduObject.add(new AsnSequence());

        // Add variable bindings
        while (ve.hasMoreElements()) {
            asnVBObject = asnVBLObject.add(new AsnSequence());
            varbind vb = (varbind) ve.nextElement();
            asnVBObject.add(vb.getOid());
            asnVBObject.add(vb.getValue());
        }

        return asnPduObject;
    }

}
