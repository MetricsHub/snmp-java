// NAME
//      $RCSfile: AsnPduSequence.java,v $
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

import java.io.*;
import java.util.*;

/**
 * The AsnPduSequence class know how a Pdu v1 and v2c is build, it knows its
 * sequence.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.10 $ $Date: 2006/01/17 17:43:54 $
 */
class AsnPduSequence extends AsnSequence {
    private static final String version_id = "@(#)$Id: AsnPduSequence.java,v 3.10 2006/01/17 17:43:54 birgit Exp $ Copyright Westhawk Ltd";

    boolean snmpv3Discovery = false;

    AsnPduSequence(InputStream in, int len, int pos) throws IOException {
        super(in, len, pos);
    }

    int getReqId() {
        AsnInteger rid = (AsnInteger) getObj(0);
        return (rid.getValue());
    }

    int getWhatError() {
        AsnInteger estat = (AsnInteger) getObj(1);
        return (estat.getValue());
    }

    int getWhereError() {
        AsnInteger estat = (AsnInteger) getObj(2);
        return (estat.getValue());
    }

    AsnSequence getVarBind() {
        return (AsnSequence) getObj(3);
    }

    /**
     * recursively look for a pduSequence object
     * - got one :-)
     */
    AsnObject findPdu() {
        return this;
    }

    int getValue() {
        AsnSequence varBind = (AsnSequence) getObj(3);
        AsnSequence varPair = (AsnSequence) varBind.getObj(0);
        AsnInteger val = (AsnInteger) varPair.getObj(1);
        int value = val.getValue();

        return value;
    }

    boolean hadError() {
        return (SNMP_ERR_NOERROR != getWhatError());
    }

    boolean isSnmpv3Discovery() {
        return snmpv3Discovery;
    }

    void setSnmpv3Discovery(boolean b) {
        snmpv3Discovery = b;
    }
}
