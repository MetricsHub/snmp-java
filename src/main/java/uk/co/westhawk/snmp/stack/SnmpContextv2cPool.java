// NAME
//      $RCSfile: SnmpContextv2cPool.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.15 $
// CREATED
//      $Date: 2009/03/05 13:27:41 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2000 - 2006 by Westhawk Ltd
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

/**
 * This class contains the pool of SNMP v2c contexts.
 * It extends the SnmpContextPool and is similar in every way, except it
 * uses a pool of SnmpContextv2c.
 *
 * <p>
 * Thanks to Seon Lee (slee@virtc.com) for reporting thread safety
 * problems.
 * </p>
 *
 * @see SnmpContextv2c
 * @see SnmpContextPool
 * @see SnmpContextv3Pool
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.15 $ $Date: 2009/03/05 13:27:41 $
 */
public class SnmpContextv2cPool extends SnmpContextPool
        implements SnmpContextv2cFace {
    private static final String version_id = "@(#)$Id: SnmpContextv2cPool.java,v 3.15 2009/03/05 13:27:41 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * Constructor.
     *
     * @param host The host to which the PDU will be sent
     * @param port The port where the SNMP server will be
     * @see SnmpContextv2c#SnmpContextv2c(String, int)
     */
    public SnmpContextv2cPool(String host, int port) throws java.io.IOException {
        super(host, port, STANDARD_SOCKET);
    }

    /**
     * Constructor.
     * Parameter typeSocket should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host       The host to which the PDU will be sent
     * @param port       The port where the SNMP server will be
     * @param typeSocket The type of socket to use.
     *
     * @see SnmpContextv2c#SnmpContextv2c(String, int, String)
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    public SnmpContextv2cPool(String host, int port, String typeSocket)
            throws java.io.IOException {
        super(host, port, typeSocket);
    }

    /**
     * Constructor.
     * Parameter typeSocket should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host       The host to which the PDU will be sent
     * @param port       The port where the SNMP server will be
     * @param comm       The community name.
     * @param typeSocket The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     *
     * @since 4_14
     */
    public SnmpContextv2cPool(String host, int port, String comm, String typeSocket)
            throws java.io.IOException {
        super(host, port, comm, null, typeSocket);
    }

    /**
     * Constructor.
     * Parameter typeSocket should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param host        The host to which the PDU will be sent
     * @param port        The port where the SNMP server will be
     * @param comm        The community name.
     * @param bindAddress The local address the server will bind to
     * @param typeSocket  The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     *
     * @since 4_14
     */
    public SnmpContextv2cPool(String host, int port, String comm, String bindAddress, String typeSocket)
            throws java.io.IOException {
        super(host, port, comm, bindAddress, typeSocket);
    }

    public int getVersion() {
        return SnmpConstants.SNMP_VERSION_2c;
    }

    /**
     * Returns a v2c context from the pool.
     * The pre-existing context (if there is any) is destroyed.
     * This methods checks for an existing context that matches all our
     * properties. If such a context does not exist, a new one is created and
     * added to the pool.
     *
     * This method actually returns a SnmpContextv2c, although it doesn't
     * look like it.
     *
     * @return A context (v2c) from the pool
     * @see #getHashKey
     * @see SnmpContext
     * @see SnmpContextv2c
     */
    protected SnmpContext getMatchingContext() throws java.io.IOException {
        SnmpContextPoolItem item = null;
        SnmpContextv2c newContext = null;
        String hashKey = getHashKey();

        destroy();
        synchronized (contextPool) {
            int count = 0;
            if (contextPool.containsKey(hashKey)) {
                item = (SnmpContextPoolItem) contextPool.get(hashKey);
                newContext = (SnmpContextv2c) item.getContext();
                count = item.getCounter();
            } else {
                newContext = new SnmpContextv2c(hostname, hostPort, bindAddr, socketType);
                newContext.setCommunity(community);
                item = new SnmpContextPoolItem(newContext);
                contextPool.put(hashKey, item);
            }
            count++;
            item.setCounter(count);
        }
        return newContext;
    }

    /**
     * This method is not supported. It will throw a CloneNotSupportedException.
     *
     * @since 4_14
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

}
