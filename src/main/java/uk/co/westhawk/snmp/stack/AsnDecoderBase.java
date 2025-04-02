// NAME
//      $RCSfile: AsnDecoderBase.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.3 $
// CREATED
//      $Date: 2007/10/17 10:36:47 $
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
 * This class contains the general methods to decode bytes into a Pdu.
 * We split the original class AsnDecoder into four classes.
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.3 $ $Date: 2007/10/17 10:36:47 $
 */
class AsnDecoderBase extends Object {
    private static final String version_id = "@(#)$Id: AsnDecoderBase.java,v 3.3 2007/10/17 10:36:47 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * Reads the input into an asn sequence.
     */
    AsnSequence getAsnSequence(InputStream in)
            throws IOException, DecodingException {
        AsnSequence asnTopSeq = null;
        AsnSequence dummy = new AsnSequence();
        AsnObject obj = dummy.AsnReadHeader(in);
        if (obj instanceof AsnSequence) {
            asnTopSeq = (AsnSequence) obj;
        } else {
            String msg = "AsnSequence was expected";
            if (obj != null) {
                msg += " instead of " + obj.getRespTypeString();
            } else {
                msg += ", but is null";
            }
            throw new DecodingException(msg);
        }
        return asnTopSeq;
    }

    /**
     * Returns the SNMP version number of the asn sequence.
     */
    int getSNMPVersion(AsnSequence asnTopSeq) throws DecodingException {
        int version = -1;
        AsnObject obj = asnTopSeq.getObj(0);
        if (obj instanceof AsnInteger) {
            AsnInteger v = (AsnInteger) obj;
            version = v.getValue();
        } else {
            String msg = "SNMP version should be of type AsnInteger"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return version;
    }

    /**
     * Returns the SNMP v1 and v2c community of the asn sequence.
     */
    String getCommunity(AsnSequence asnTopSeq) throws DecodingException {
        String comm = "";
        AsnObject obj = asnTopSeq.getObj(1);
        if (obj instanceof AsnOctets) {
            AsnOctets estat = (AsnOctets) obj;
            comm = estat.getValue();
        } else {
            String msg = "community should be of type AsnOctets"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return comm;
    }

    AsnSequence getAsnHeaderData(AsnSequence asnTopSeq) throws DecodingException {
        AsnSequence asnHeaderData = null;
        AsnObject obj = asnTopSeq.getObj(1);
        if (obj instanceof AsnSequence) {
            asnHeaderData = (AsnSequence) obj;
        } else {
            String msg = "asnHeaderData should be of type AsnSequence"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return asnHeaderData;
    }

}
