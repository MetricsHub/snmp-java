// NAME
//      $RCSfile: UsmAgent.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.8 $
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
 * This interface provides the SNMPv3 USM (User-Based Security Model)
 * authoritative details.
 *
 * <p>
 * When the stack is used as authoritative SNMP engine it has to send
 * its Engine ID and clock (i.e. Engine Boots and Engine Time) with each 
 * message. 
 * The engine who sends a Response, a Trapv2 or a Report is
 * authoritative.
 * </p>
 *
 * <p>
 * Since this stack has no means in providing this information, this
 * interface has to be implemented by the user.
 * </p>
 *
 * <p>
 * See <a href="http://www.ietf.org/rfc/rfc3414.txt">SNMP-USER-BASED-SM-MIB</a>.
 * </p>
 * @see SnmpContextv3#setUsmAgent
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.8 $ $Date: 2006/03/23 14:54:10 $
 */
public interface UsmAgent {
    static final String version_id = "@(#)$Id: UsmAgent.java,v 3.8 2006/03/23 14:54:10 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * This name ("_myusmagent") is used to create an entry in the TimeWindow
     * for the stack to act as authoritative engine.
     */
    public String MYFAKEHOSTNAME = "_myusmagent";

    /**
     * Returns the authoritative SNMP Engine ID.
     * It uniquely and unambiguously identifies the SNMP engine, within an
     * administrative domain.
     *
     * <p>
     * The Engine ID is the (case insensitive) string representation of a
     * hexadecimal number, without any prefix, for example
     * <b>010000a1d41e4946</b>.
     * </p>
     * 
     * @see uk.co.westhawk.snmp.util.SnmpUtilities#toBytes(String)
     */
    public String getSnmpEngineId();

    /**
     * Returns the authoritative Engine Boots.
     * It is a count of the number of times the SNMP engine has
     * re-booted/re-initialized since snmpEngineID was last configured.
     */
    public int getSnmpEngineBoots();

    /**
     * Returns the authoritative Engine Time.
     * It is the number of seconds since the snmpEngineBoots counter was
     * last incremented.
     */
    public int getSnmpEngineTime();

    /**
     * Returns the value of the usmStatsUnknownEngineIDs counter.
     * The stack needs this when responding to a discovery request.
     */
    public long getUsmStatsUnknownEngineIDs();

    /**
     * Returns the value of the usmStatsNotInTimeWindows counter.
     * The stack needs this when responding to a discovery request.
     */
    public long getUsmStatsNotInTimeWindows();

    /**
     * Sets the current snmp context.
     */
    public void setSnmpContext(SnmpContextv3Basis context);
}
