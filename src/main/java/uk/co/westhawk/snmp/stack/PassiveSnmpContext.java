// NAME
//      $RCSfile: PassiveSnmpContext.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.10 $
// CREATED
//      $Date: 2009/03/05 13:12:50 $
// COPYRIGHT
//      ERG Group Ltd
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

/**
 * This class contains the SNMP v1 context that is needed by a Pdu to
 * send a SNMP v1 request in environments where thread creation is
 * unwanted.
 *
 * <p>
 * This extends SnmpContext so that it does not create any
 * threads to send PDUs. It must be used with the
 * PDU class PassiveTrapPduv1. The original purpose of the
 * Passive classes is to allow the stack to be used in environments where
 * thread creation is unwanted, eg database JVMs such as Oracle JServer.
 * See <a href="http://www.ietf.org/rfc/rfc3416.txt">SNMPv2-PDU</a>.
 * </p>
 *
 * <p>
 * See 
 * <a
 * href="../../../../../uk/co/westhawk/nothread/trap/package-summary.html">notes</a>
 * on how to send traps in an Oracle JServer environment.
 * </p>
 *
 * @see uk.co.westhawk.snmp.pdu.PassiveTrapPduv1
 * @since 4_12
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.10 $ $Date: 2009/03/05 13:12:50 $
 */
public class PassiveSnmpContext extends SnmpContext {
    private static final String version_id = "@(#)$Id: PassiveSnmpContext.java,v 3.10 2009/03/05 13:12:50 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * Constructor.
     *
     * @param host The host to which the Pdu will be sent
     * @param port The port where the SNMP server will be
     * @see SnmpContext#SnmpContext(String, int)
     */
    public PassiveSnmpContext(String host, int port)
            throws java.io.IOException {
        super(host, port);
    }

    /**
     * Constructor.
     * Parameter typeSocketA should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the Pdu will be sent
     * @param port        The port where the SNMP server will be
     * @param typeSocketA The type of socket to use.
     *
     * @see SnmpContext#SnmpContext(String, int, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    public PassiveSnmpContext(String host, int port, String typeSocketA)
            throws java.io.IOException {
        super(host, port, typeSocketA);
    }

    /**
     * Constructor.
     *
     * If bindAddress is null, then the system will pick up a valid local
     * address to bind the socket.
     *
     * The typeSocketA will indicate which type of socket to use. This way
     * different handlers can be provided.
     * It should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the Pdu will be sent
     * @param port        The port where the SNMP server will be
     * @param bindAddress The local address the server will bind to
     * @param typeSocketA The type of socket to use.
     *
     * @exception java.io.IOException Thrown when the socket cannot be
     *                                created.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     * @since 4_14
     */
    protected PassiveSnmpContext(String host, int port, String bindAddress, String typeSocketA)
            throws java.io.IOException {
        super(host, port, bindAddress, typeSocketA);
    }

    /**
     * Overrides the AbstractSnmpContext.activate() to do nothing.
     * This prevents the creation of threads in the base class.
     *
     * @see AbstractSnmpContext#activate()
     */
    protected void activate() {
        // do nothing
    }

}
