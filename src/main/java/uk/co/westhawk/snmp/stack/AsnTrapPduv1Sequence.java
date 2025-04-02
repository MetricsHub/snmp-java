// NAME
//      $RCSfile: AsnTrapPduv1Sequence.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.6 $
// CREATED
//      $Date: 2006/01/17 17:43:54 $
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
 * The AsnTrapPduv1Sequence class knows how a TrapPdu v1 is build, it knows its
 * sequence.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.6 $ $Date: 2006/01/17 17:43:54 $
 */
class AsnTrapPduv1Sequence extends AsnSequence {
    private static final String version_id = "@(#)$Id: AsnTrapPduv1Sequence.java,v 3.6 2006/01/17 17:43:54 birgit Exp $ Copyright Westhawk Ltd";

    AsnTrapPduv1Sequence(InputStream in, int len, int pos) throws IOException {
        super(in, len, pos);
    }

    String getEnterprise() throws DecodingException {
        String ent = "";
        AsnObject obj = getObj(0);
        if (obj instanceof AsnObjectId) {
            AsnObjectId rid = (AsnObjectId) obj;
            ent = rid.getValue();
        } else {
            String msg = "TrapPduv1.Enterprise should be AsnObjectId"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return ent;
    }

    byte[] getIPAddress() throws DecodingException {
        byte[] ip = null;
        AsnObject obj = getObj(1);
        if (obj instanceof AsnOctets) {
            AsnOctets estat = (AsnOctets) obj;
            ip = estat.getBytes();
        } else {
            String msg = "TrapPduv1.IPAddress should be of type AsnOctets"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return ip;
    }

    int getGenericTrap() throws DecodingException {
        int genTrap = -1;
        AsnObject obj = getObj(2);
        if (obj instanceof AsnInteger) {
            AsnInteger estat = (AsnInteger) obj;
            genTrap = estat.getValue();
        } else {
            String msg = "TrapPduv1.GenericTrap should be of type AsnInteger"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return genTrap;
    }

    int getSpecificTrap() throws DecodingException {
        int specTrap = -1;
        AsnObject obj = getObj(3);
        if (obj instanceof AsnInteger) {
            AsnInteger estat = (AsnInteger) obj;
            specTrap = estat.getValue();
        } else {
            String msg = "TrapPduv1.SpecificTrap should be of type AsnInteger"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return specTrap;

    }

    long getTimeTicks() throws DecodingException {
        long ticks = -1;
        AsnObject obj = getObj(4);
        if (obj instanceof AsnUnsInteger) {
            AsnUnsInteger estat = (AsnUnsInteger) obj;
            ticks = estat.getValue();
        } else {
            String msg = "TrapPduv1.TimeTicks should be of type AsnUnsInteger"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return ticks;
    }

    AsnSequence getVarBind() throws DecodingException {
        AsnSequence varb = null;
        AsnObject obj = getObj(5);
        if (obj instanceof AsnSequence) {
            varb = (AsnSequence) obj;
        } else {
            String msg = "TrapPduv1.VarBind should be of type AsnSequence"
                    + " instead of " + obj.getRespTypeString();
            throw new DecodingException(msg);
        }
        return varb;
    }

    /**
     * recursively look for a trapPduv1Sequence object
     * - got one :-)
     */
    AsnObject findTrapPduv1() {
        return this;
    }

}
