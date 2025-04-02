// NAME
//      $RCSfile: AsnDecoderv2c.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.3 $
// CREATED
//      $Date: 2006/02/09 14:16:36 $
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
 * This class contains the v2c specific methods to decode bytes into a Pdu.
 * We split the original class AsnDecoder into four classes.
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.3 $ $Date: 2006/02/09 14:16:36 $
 */
class AsnDecoderv2c extends AsnDecoderBase {
    private static final String version_id = "@(#)$Id: AsnDecoderv2c.java,v 3.3 2006/02/09 14:16:36 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * This method creates an AsnPduSequence out of the characters of the
     * InputStream for v2c.
     *
     * @see AbstractSnmpContext#run
     * @see SnmpContextv2c#processIncomingResponse
     * @see SnmpContextv2c#processIncomingPdu
     */
    AsnPduSequence DecodeSNMPv2c(InputStream in, String community)
            throws IOException, DecodingException {
        AsnSequence asnTopSeq = getAsnSequence(in);
        int snmpVersion = getSNMPVersion(asnTopSeq);
        if (snmpVersion != SnmpConstants.SNMP_VERSION_2c) {
            String str = SnmpUtilities.getSnmpVersionString(snmpVersion);
            String msg = "Wrong SNMP version: expected SNMPv2c, received "
                    + str;
            throw new DecodingException(msg);
        }
        String comm = getCommunity(asnTopSeq);
        if (comm.equals(community) == false) {
            String msg = "Wrong community: expected "
                    + community + ", received " + comm;
            throw new DecodingException(msg);
        }
        AsnPduSequence Pdu = (AsnPduSequence) asnTopSeq.findPdu();
        return Pdu;
    }

}
