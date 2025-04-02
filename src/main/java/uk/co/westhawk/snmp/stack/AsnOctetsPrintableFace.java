// NAME
//      $RCSfile: AsnOctetsPrintableFace.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.5 $
// CREATED
//      $Date: 2006/03/23 14:54:10 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2005 - 2006 by Westhawk Ltd
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
 * This interface contains the isPrintable() method that is used to decided
 * whether or not an AsnOctets with type ASN_OCTET_STR is printable or not.
 * This interface has no effect on the way AsnOctets with type IPADDRESS 
 * or OPAQUE are printed.
 *
 * <p>
 * When the type is ASN_OCTET_STR, the method tries to guess whether
 * or not the string is printable; without the knowledge of the MIB
 * it cannot distinguish between OctetString and any textual
 * conventions, like DisplayString, InternationalDisplayString or DateAndTime.
 * </p>
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.5 $ $Date: 2006/03/23 14:54:10 $
 */
public interface AsnOctetsPrintableFace {
    static final String version_id = "@(#)$Id: AsnOctetsPrintableFace.java,v 3.5 2006/03/23 14:54:10 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * Returns whether or not the AsnOctets' byte array represent a printable
     * string or not.
     *
     * @see AsnOctets#toCalendar()
     * @see AsnOctets#toDisplayString()
     * @see AsnOctets#toHex()
     * @see AsnOctets#toString()
     */
    public boolean isPrintable(byte[] value);

    /**
     * This method provides the implemantation of the
     * InternationalDisplayString text-convention. See
     * <a href="http://www.ietf.org/rfc/rfc2790.txt">HOST-RESOURCES-MIB</a>.
     *
     * <p>
     * "This data type is used to model textual information
     * in some character set. A network management station
     * should use a local algorithm to determine which
     * character set is in use and how it should be
     * displayed. Note that this character set may be
     * encoded with more than one octet per symbol, but will
     * most often be NVT ASCII. When a size clause is
     * specified for an object of this type, the size refers
     * to the length in octets, not the number of symbols."
     * </p>
     */
    public String toInternationalDisplayString(byte[] value);

}
