// NAME
//      $RCSfile: GetBulkPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.17 $
// CREATED
//      $Date: 2006/02/09 14:30:19 $
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
/**
 * This class represents the SNMP GetBulk Pdu. This request has been
 * added in SNMPv2c, hence is not supported by SNMPv1 agents.
 *
 * <p>
 * The request is processed on the agent side in the following way:
 * <pre>
 * // getNext (once) on the non_repeaters
 * for (n=0; n&lt;N; n++)
 * {
 *    getNext(var[n]);
 * }
 * // getNext (max_repetitions times) on the repeaters
 * for (m=1; m&lt;M; m++)
 * {
 *     for (r=1; r&lt;R; r++)
 *     {
 *         getNext(var[N+r]);
 *     }
 * }
 * </pre>
 *
 * <p>
 * Where:<br>
 * <ul>
 * <li><code>L</code> is the number of vars in the request </li>
 * <li><code>N = Max(Min(non_repeaters, L), 0)</code></li>
 * <li><code>M = Max(max_repetitions, 0)</code></li>
 * <li><code>R = L-N</code></li>
 * </ul>
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.17 $ $Date: 2006/02/09 14:30:19 $
 */
public class GetBulkPdu extends Pdu {
    private static final String version_id = "@(#)$Id: GetBulkPdu.java,v 3.17 2006/02/09 14:30:19 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * The value of the non_repeaters.
     */
    protected int non_repeaters = 0;
    /**
     * The value of the max_repetitions.
     */
    protected int max_repetitions = 0;

    /**
     * Constructor.
     * The GetBulkRequest has been added in SNMPv2. Its purpose is to
     * retrieve a large amount of information with one request.
     *
     * @param con The context (v2c or v3) of the Pdu
     */
    public GetBulkPdu(SnmpContextBasisFace con) {
        super(con);
        setMsgType(AsnObject.GETBULK_REQ_MSG);
    }

    /**
     * Sets the non_repeaters. The non_repeaters specifies the number of
     * variables in the varbind list for which a single lexicographic
     * successor is to be returned.
     * If they are not set, the value will be 0 (zero).
     */
    public void setNonRepeaters(int no) {
        // same field as error status in any other PDU.
        non_repeaters = no;
    }

    /**
     * Returns the non_repeaters.
     */
    public int getNonRepeaters() {
        return non_repeaters;
    }

    /**
     * Sets the max_repetitions. The max_repetitions specifies the number of
     * lexicographic successors to be returned for the remaining variables
     * in the varbind list.
     * If they are not set, the value will be 0 (zero).
     */
    public void setMaxRepetitions(int no) {
        // same field as error index in any other PDU.
        max_repetitions = no;
    }

    /**
     * Old method to set the max_repetitions. Was a spelling mistake,
     * it is still in here for backwards compatibility.
     *
     * @deprecated Use {@link #setMaxRepetitions(int no)}.
     * @see #setMaxRepetitions
     */
    public void setMaxRepititions(int no) {
        setMaxRepetitions(no);
    }

    /**
     * Returns the max_repetitions.
     */
    public int getMaxRepetitions() {
        return max_repetitions;
    }

    /**
     * Sends the Pdu.
     *
     * <p>
     * The GetBulk request has the same format as any other request, except
     * that the error_status field is replaced by non_repeaters and the
     * error_index field is replaces by max_repetitions.
     * </p>
     * 
     * @see Pdu#send(int, int)
     */
    public boolean send() throws java.io.IOException, PduException {
        return send(non_repeaters, max_repetitions);
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return The string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(super.toString());
        int l = buffer.length();
        buffer.setLength(l - 1);

        buffer.append(", non_rep=").append(getNonRepeaters());
        buffer.append(", max_rep=").append(getMaxRepetitions());
        buffer.append("]");
        return buffer.toString();
    }

}
