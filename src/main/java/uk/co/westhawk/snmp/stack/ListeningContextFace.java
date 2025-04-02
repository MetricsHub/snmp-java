// NAME
//      $RCSfile: ListeningContextFace.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.6 $
// CREATED
//      $Date: 2006/11/29 16:12:50 $
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
import uk.co.westhawk.snmp.event.*;

/**
 * This interface contains the SNMP listening context methods.
 *
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.6 $ $Date: 2006/11/29 16:12:50 $
 */
public interface ListeningContextFace {
    static final String     version_id =
        "@(#)$Id: ListeningContextFace.java,v 3.6 2006/11/29 16:12:50 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * The default port number where we listen for traps (162).
     */
    public final static int DEFAULT_TRAP_PORT = 162;


/**
 * Returns the port number.
 *
 * @return The port no
 */
public int getPort();

/**
 * Returns the local address the server will bind to
 * When the address is null, the socket accepts connections on
 * any/all local addresses.
 *
 * @return The bind address
 */
public String getBindAddress();

/**
 * Returns the type of socket.
 *
 * @see SnmpContextBasisFace#STANDARD_SOCKET
 * @see SnmpContextBasisFace#TCP_SOCKET
 * @return The type of socket 
 */
public String getTypeSocket();

/**
 * Returns the maximum number of bytes this context will read from the
 * socket. By default this will be set to <code>MSS</code> (i.e. 1300).
 *
 * @see SnmpContextBasisFace#MSS
 * @see #setMaxRecvSize(int)
 * @see AbstractSnmpContext#setMaxRecvSize(int)
 * @return The number 
 */
public int getMaxRecvSize();

/**
 * Sets the maximum number of bytes this context will read from the
 * socket. By default this will be set to <code>MSS</code> (i.e. 1300).
 *
 * @see SnmpContextBasisFace#MSS
 * @see AbstractSnmpContext#getMaxRecvSize()
 * @param no The new size 
 */
public void setMaxRecvSize(int no);

/**
 * Removes the resouces held by this context. 
 * This method will stop the thread listening for packets. 
 */
public void destroy();

/**
 * Adds the specified PDU listener to receive the undecoded PDUs.
 * When a PDU is received the PDU received event is fired to all 
 * listeners, until one of them consumes it.
 *
 * <p>
 * All the SnmpContext objects use this method to listen for PDUs. When
 * a SnmpContext object decodes the PDU succesfully, it will consume
 * it.
 * </p>
 *
 * <p>
 * Only when a listener is added will this context create a listening socket.
 * </p>
 *
 * @param l The listener object
 * @exception java.io.IOException Thrown when creating a listening
 * socket fails
 *
 * @see RawPduReceivedSupport#fireRawPduReceived
 * @see AbstractSnmpContext#addTrapListener
 * @see AbstractSnmpContext#addRequestPduListener
 * @see #addUnhandledRawPduListener(RawPduListener)
 */
public void addRawPduListener(RawPduListener l)
throws java.io.IOException;

/**
 * Removes the specified PDU listener. When there are no more listeners,
 * calls destroy().
 *
 * @param l The listener object
 */
public void removeRawPduListener(RawPduListener l);

/**
 * Adds the specified PDU listener to receive the undecoded PDUs when
 * it was not handled (i.e. not consumed) by any of the PDU listeners in
 * addRawPduListener().
 *
 * <p>
 * Only when a listener is added will this context create a listening socket.
 * </p>
 *
 * @param listener The listener object
 * @exception java.io.IOException Thrown when creating a listening
 * socket fails
 *
 * @see #addRawPduListener(RawPduListener)
 */
public void addUnhandledRawPduListener(RawPduListener listener)
throws java.io.IOException;

/**
 * Removes the specified unhandled PDU listener. When there are no more 
 * listeners, calls destroy().
 *
 * @param listener The listener object
 */
public void removeUnhandledRawPduListener(RawPduListener listener);

}
