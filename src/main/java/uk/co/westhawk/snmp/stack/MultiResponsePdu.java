// NAME
//      $RCSfile: MultiResponsePdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.3 $
// CREATED
//      $Date: 2007/10/17 10:44:09 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 2006 by Westhawk Ltd
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
 * This class can receive multiple responses.
 * Typical usage includes sending a single PDU to a multicast /
 * broadcast address so that multiple responses can be received from
 * different sources. 
 *
 * <p>
 * This class sets a single long timeout for the retry, so it sends the 
 * request only once.
 * Opposite to its parent class, this class does not ignore the duplicate
 * responses, and it will timeout by nature.
 * </p>
 *
 * <p>Note:</p>
 * <ul>
 * <li>Please realise that you might choke the stack and your network, when
 * you use this class, even on a small subnet</li>
 * <li>This PDU will eat up transmit and receive resources, until it times out</li>
 * <li>This PDU cannot be used to receive traps</li>
 * <li>Authentication (and privacy) is by definition a unicast activity. 
 * You can find unauthenticated SNMPv3 engines, by broadcasting this PDU 
 * with a SnmpContextv3(Pool) with no authentication.
 * Then you need to continue an authentication/privacy context and a (normal)
 * PDU.<br/>
 * In other words, finding SNMPv3 engines that only support
 * authentication and/or privacy cannot be done via broadcasting.
 * </li>
 * </ul>
 *
 * 
 * <p>
 * Thanks to Josh Bers &lt;jbers@bbn.com&gt;
 * </p>
 * 
 * @since 4_14
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.3 $ $Date: 2007/10/17 10:44:09 $
 */
public class MultiResponsePdu extends Pdu {
    private static final String version_id = "@(#)$Id: MultiResponsePdu.java,v 3.3 2007/10/17 10:44:09 birgita Exp $ Copyright Westhawk Ltd";

    /**
     * Hashtable to hold responses received from agents.
     */
    private Hashtable responses = new Hashtable();

    /**
     * IP address of current response
     */
    private String thisIP = null;

    /**
     * By default create a MultiResponsePdu that will wait for 3
     * seconds for responses to come in from multiple sources. If you
     * want to wait longer set the RetryInterval to a longer first
     * timeout. To make the request more reliable, add more timeouts.
     *
     * @param con The context
     */
    public MultiResponsePdu(SnmpContextBasisFace con) {
        super(con);
        setRetryIntervals(new int[] { 3000 });
    }

    /**
     * Gets the IP address of the host of the most recent response received.
     *
     * @return The sourceAgent value
     */
    public String getSourceAgent() {
        return thisIP;
    }

    /**
     * Gets the number of responses so far received
     * to this request.
     *
     * @return The number of responses
     */
    public int getNumResponses() {
        return responses.size();
    }

    /**
     * Prints out the list of received responses and their source IP
     * addressses. Results will be ommitted if not yet received.
     *
     * @return String representation of this PDU and all its received
     *         responses
     */
    public String toString() {
        // loop over vector of responses and use Pdu.toString(boolean)
        // to print out the received varbinds.
        StringBuffer buffer = new StringBuffer();
        if (!answered) {
            buffer.append(toString(false));
        } else {
            Enumeration ipaddrs = responses.keys();
            String ipaddr = (String) ipaddrs.nextElement();
            // set respVarbinds to each response in turn calling toString(true)
            respVarbinds = (Vector) responses.get(ipaddr);

            buffer.append(toString(true));
            buffer.append(" rhost=").append(ipaddr);
            int i = 2;
            while (ipaddrs.hasMoreElements()) {
                ipaddr = (String) ipaddrs.nextElement();
                respVarbinds = (Vector) responses.get(ipaddr);

                buffer.append("\n\t");
                buffer.append(printVars("respVarbinds" + i, respVarbinds));
                buffer.append(" rhost=").append(ipaddr);
                i++;
            }
        }
        return buffer.toString();
    }

    /**
     * Lets the observers know which source we received a response from.
     */
    protected void tell_them() {
        String sender = thisIP;
        // if timed out then we are done waiting for replies.
        if (isTimedOut()) {
            sender = null;
        } else {
            // record this response for posterity
            responses.put(sender, respVarbinds);
        }

        // tell all interested parties
        notifyObservers(sender);

        // free up space for next result
        respVarbinds = null;
        thisIP = null;
    }

    /**
     * Fills in the received response.
     *
     * Now override fillin to fetch source ip address and not set answered
     * you can get multiple responses by setting the timeout period long
     * do this in the constructor.
     *
     * @param seq Description of Parameter
     * @see Pdu#getResponseVarbinds()
     */
    void fillin(AsnPduSequence seq) {
        // this will be set to true (eventually) in handleNoAnswer()
        if (answered) {
            if (AsnObject.debug > 6) {
                System.out.println(getClass().getName() + ".fillin(): "
                        + "Got a second answer to request " + getReqId());
            }
            return;
        }

        // check that we haven't already heard from this host before:
        thisIP = getContext().getReceivedFromHostAddress();
        if (responses.containsKey(thisIP)) {
            if (AsnObject.debug > 6) {
                System.out.println(getClass().getName() + ".fillin(): "
                        + "Got a second answer from " + thisIP
                        + " to request " + getReqId());
            }
            return;
        }

        // fillin(null) can be called in case of a Decoding exception
        if (seq != null) {
            if (seq.isCorrect == true) {
                int n = -1;
                try {
                    // Fill in the request id
                    this.req_id = seq.getReqId();
                    setErrorStatus(seq.getWhatError());
                    setErrorIndex(seq.getWhereError());

                    // The varbinds from the response/report are set in a
                    // new Vector.
                    AsnSequence varBind = seq.getVarBind();
                    int size = varBind.getObjCount();
                    respVarbinds = new Vector(size, 1);
                    for (n = 0; n < size; n++) {
                        Object obj = varBind.getObj(n);
                        if (obj instanceof AsnSequence) {
                            AsnSequence varSeq = (AsnSequence) obj;
                            try {
                                varbind vb = new varbind(varSeq);
                                respVarbinds.addElement(vb);
                                new_value(n, vb);
                            } catch (IllegalArgumentException exc) {
                            }
                        }
                    }

                    // At this point, I don't know whether I received a
                    // response and should fill in only the respVarbind or
                    // whether I received a request (via ListeningContext)
                    // and I should fill in the reqVarbinds.
                    // So when reqVarbinds is empty, I clone the
                    // respVarbinds.
                    if (reqVarbinds.isEmpty()) {
                        reqVarbinds = (Vector) respVarbinds.clone();
                    }
                } catch (Exception e) {
                    // it happens that an agent does not encode the varbind
                    // list properly. Since we try do decode as much as
                    // possible there may be wrong elements in this list.

                    DecodingException exc = new DecodingException(
                            "Incorrect varbind list, element " + n);
                    setErrorStatus(AsnObject.SNMP_ERR_DECODINGASN_EXC, exc);
                }
            } else {
                // we couldn't read the whole message
                // see AsnObject.AsnReadHeader, isCorrect

                DecodingException exc = new DecodingException(
                        "Incorrect packet. No of bytes received less than packet length.");
                setErrorStatus(AsnObject.SNMP_ERR_DECODINGPKTLNGTH_EXC, exc);
            }
        }

        // always do 'setChanged', even if there are no varbinds.
        setChanged();
        tell_them();
        clearChanged();

        // don't want to tell trans to stop since this will remove
        // the PDU from the context. Instead depend on timeouts to
        // free up the transmitter and remove PDU from context.
        /*
         * synchronized(this)
         * {
         * got = true;
         * answered = true;
         * notify(); // see also handleNoAnswer()
         * if (trans != null)
         * {
         * // free up the transmitter, since
         * // we are happy with the answer.
         * // trans may be null if we are receiving a trap.
         * trans.interruptMe();
         * }
         * }
         */
    }

}
