// NAME
//      $RCSfile: DefaultTrapContext.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.12 $
// CREATED
//      $Date: 2009/03/05 13:12:50 $
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

import uk.co.westhawk.snmp.event.*;
import uk.co.westhawk.snmp.net.*;
import uk.co.westhawk.snmp.util.*;


/**
 * The DefaultTrapContext class will enable this stack to receive traps.
 * Only one (1) instance of the DefaultTrapContext can exist. The
 * context will only start receiving (or listen for) traps when there is
 * at least one listener registered. Two kind of listeners can be added; 
 * the normal and unhandled trap listeners.
 * The normal trap listeners are added via the
 * <code>addTrapListener()</code> method, 
 * the unhandled trap listeners are added via the 
 * <code>addUnhandledTrapListener()</code>.
 *
 * <p>
 * Use one of the <code>getInstance()</code> methods to get the instance and add a trap
 * listener. This class will fire undecoded trap events, i.e. the raw
 * data is sent and no attempt is made to decode the data into a pdu.
 * </p>
 *
 * <p>
 * The SnmpContext classes provide functionality for decoded trap
 * events. These classes will register themselves to the
 * DefaultTrapContext object and only pass the event on if it matches
 * their configuration.
 * </p>
 *
 * <p>
 * <a id=note></a>
 * Note that because only one instance of this class
 * can exist, the first call of <code>getInstance()</code> will define 
 * the settings
 * (i.e. port number and socket type) for the lifetime of the stack. All
 * the subsequent calls of <code>getInstance()</code> will return the existing
 * instance, irrespective of the arguments.
 * </p>
 *
 * <p>
 * On UNIX and Linux operating systems the default port where trap are 
 * sent (i.e. <em>162</em>) can only be opened as root.
 * </p>
 *
 * <p>
 * Note, this class is now deprecated. We are (very) slowly trying to 
 * move to a more general way of receiving packets and adding agent
 * functionality. ListeningContext and ListeningContextPool allow the
 * stack to listen to more than one port. 
 * </p>
 *
 * @deprecated  As of 4_14, replaced by {@link ListeningContext} and
 * {@link ListeningContextPool}
 *
 * @see AbstractSnmpContext#addTrapListener
 * @see ListeningContext
 * @see ListeningContextPool
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.12 $ $Date: 2009/03/05 13:12:50 $
 */
public class DefaultTrapContext extends ListeningContext {
    private static final String version_id = "@(#)$Id: DefaultTrapContext.java,v 3.12 2009/03/05 13:12:50 birgita Exp $ Copyright Westhawk Ltd";

    private static DefaultTrapContext current = null;

    /**
     * Constructor.
     * The Standard socket type will be used.
     *
     * @param port The local port where traps are received
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     */
    protected DefaultTrapContext(int port) throws IOException {
        this(port, SnmpContextBasisFace.STANDARD_SOCKET);
    }

    /**
     * Constructor.
     *
     * The typeSocket will indicate which type of socket to use. This way
     * different handlers can be provided.
     * It should be either STANDARD_SOCKET, TCP_SOCKET or a
     * fully qualified classname.
     *
     * @param port        The local port where traps are received
     * @param typeSocketA The type of socket to use.
     *
     * @see SnmpContextBasisFace#STANDARD_SOCKET
     * @see SnmpContextBasisFace#TCP_SOCKET
     */
    protected DefaultTrapContext(int port, String typeSocketA)
            throws IOException {
        super(port, typeSocketA);
    }

    /**
     * Returns the instance of DefaultTrapContext. It will create the
     * instance if it didn't exists.
     * See <a href=#note>the note</a> above.
     */
    public static synchronized DefaultTrapContext getInstance(int port)
            throws IOException {
        if (current == null) {
            current = new DefaultTrapContext(port);
        }
        return current;
    }

    /**
     * Returns the instance of DefaultTrapContext. It will create the
     * instance if it didn't exists.
     * See <a href=#note>the note</a> above.
     */
    public static synchronized DefaultTrapContext getInstance(int port, String typeSocketA)
            throws IOException {
        if (current == null) {
            current = new DefaultTrapContext(port, typeSocketA);
        }
        return current;
    }

}
