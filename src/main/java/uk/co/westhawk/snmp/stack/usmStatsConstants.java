// NAME
//      $RCSfile: usmStatsConstants.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.7 $
// CREATED
//      $Date: 2006/03/23 14:54:10 $
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

/**
 * This interface contains the OIDs for the usmStats variables.
 * These variables are returned with the SNMPv3 usm security model when
 * an error occurs.
 *
 * They are part of the 
 * <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a> mib.
 *
 * @see SnmpContextv3
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.7 $ $Date: 2006/03/23 14:54:10 $
 */
public interface usmStatsConstants {
    static final String version_id = "@(#)$Id: usmStatsConstants.java,v 3.7 2006/03/23 14:54:10 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * The total number of packets received by the SNMP engine which
     * were dropped because they requested a securityLevel that was
     * unknown to the SNMP engine or otherwise unavailable.
     */
    public final static String usmStatsUnsupportedSecLevels = "1.3.6.1.6.3.15.1.1.1";

    /**
     * The total number of packets received by the SNMP engine which were
     * dropped because they appeared outside of the authoritative SNMP
     * engine's window.
     */
    public final static String usmStatsNotInTimeWindows = "1.3.6.1.6.3.15.1.1.2";

    /**
     * The total number of packets received by the SNMP engine which
     * were dropped because they referenced a user that was not known to
     * the SNMP engine.
     */
    public final static String usmStatsUnknownUserNames = "1.3.6.1.6.3.15.1.1.3";

    /**
     * The total number of packets received by the SNMP engine which
     * were dropped because they referenced an snmpEngineID that was not
     * known to the SNMP engine.
     */
    public final static String usmStatsUnknownEngineIDs = "1.3.6.1.6.3.15.1.1.4";

    /**
     * The total number of packets received by the SNMP engine which
     * were dropped because they didn't contain the expected digest
     * value.
     */
    public final static String usmStatsWrongDigests = "1.3.6.1.6.3.15.1.1.5";

    /**
     * The total number of packets received by the SNMP engine which
     * were dropped because they could not be decrypted.
     */
    public final static String usmStatsDecryptionErrors = "1.3.6.1.6.3.15.1.1.6";

    /**
     * The array with all the usmStats dotted OIDs in it.
     */
    public final static String[] usmStatsOids = {
            usmStatsUnsupportedSecLevels,
            usmStatsNotInTimeWindows,
            usmStatsUnknownUserNames,
            usmStatsUnknownEngineIDs,
            usmStatsWrongDigests,
            usmStatsDecryptionErrors
    };

    /**
     * The array with all the usmStats verbal OIDs in it.
     */
    public final static String[] usmStatsStrings = {
            "usmStatsUnsupportedSecLevels",
            "usmStatsNotInTimeWindows",
            "usmStatsUnknownUserNames",
            "usmStatsUnknownEngineIDs",
            "usmStatsWrongDigests",
            "usmStatsDecryptionErrors"
    };

}

