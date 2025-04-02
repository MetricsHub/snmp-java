// NAME
//      $RCSfile: Pdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.33 $
// CREATED
//      $Date: 2008/12/12 14:55:51 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1995, 1996 by West Consulting BV
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

/*
 * Copyright (C) 1996 - 2006 by Westhawk Ltd
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
import java.io.*;

import uk.co.westhawk.snmp.util.*;

/**
 * This class represents the ASN PDU object, this is the equivalent of
 * a GetRequest PDU.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.33 $ $Date: 2008/12/12 14:55:51 $
 */
public abstract class Pdu extends Observable {
    private static final String version_id = "@(#)$Id: Pdu.java,v 3.33 2008/12/12 14:55:51 tpanton Exp $ Copyright Westhawk Ltd";

    protected Vector reqVarbinds = null;
    protected Vector respVarbinds = null;

    private final static String TIMED_OUT = "Timed out";
    private final static String[] errorStrings = {
            "No error",
            "Value too big error",
            "No such name error",
            "Bad value error",
            "Read only error",
            "General error",
            "No access error",
            "Wrong type error",
            "Wrong length error",
            "Wrong encoding error",
            "Wrong value error",
            "No creation error",
            "Inconsistent value error",
            "Resource unavailable error",
            "Commit failed error",
            "Undo failed error",
            "Authorization error",
            "Not writable error",
            "Inconsistent name error",
    };

    private static int next_id = 1;
    private static final Object NEXT_ID_LOCK = new Object();

    private int retry_intervals[] = { 500, 1000, 2000, 5000, 5000 };

    protected byte[] encodedPacket = null;
    protected SnmpContextBasisFace context;
    protected boolean added = false;
    protected byte msg_type;

    int req_id;
    protected Integer snmpv3MsgId = null;
    protected int errstat;
    protected int errind;

    private Transmitter trans = null;
    private int retries;
    protected boolean answered;
    private boolean got = false;
    private boolean isTimedOut;
    private PduException respException = null;

    /**
     * The value of the response is set. This will be called by
     * Pdu.fillin().
     */
    protected void new_value(int n, varbind res) {
    }

    /**
     * This method notifies all observers.
     * This will be called by Pdu.fillin().
     *
     * <p>
     * The Object to the update() method of the Observer will be a varbind,
     * unless an exception occurred.
     * In the case of an exception, that exception will be passed.
     * So watch out casting!
     * </p>
     */
    protected void tell_them() {
        notifyObservers();
    }

    /**
     * Constructor.
     *
     * @param con The context of the PDU
     * @see SnmpContext
     * @see SnmpContextv2c
     * @see SnmpContextv3
     */
    public Pdu(SnmpContextBasisFace con) {
        context = con;

        // TODO: would not work if we ever were to send response or report!
        // TODO: We would have to set the req_id!

        // Added NEXT_ID_LOCK on suggestion of
        // Victor Kirk <Victor.Kirk@serco.com>
        synchronized (NEXT_ID_LOCK) {
            req_id = next_id++;
        }
        errstat = AsnObject.SNMP_ERR_NOERROR;
        errind = 0x00;
        reqVarbinds = new Vector(1, 1);
        setMsgType(AsnObject.GET_REQ_MSG);
        isTimedOut = false;

        // if it is not set to false, PDUs that do not receive a response
        // will not be filled in properly.
        answered = false;
    }

    /**
     * Returns the context of this PDU.
     * 
     * @return The context
     */
    public SnmpContextBasisFace getContext() {
        return context;
    }

    /**
     * Sets the retry intervals of the PDU. The length of the array
     * corresponds with the number of retries. Each entry in the array
     * is the number of milliseconds of each try.
     *
     * <p>
     * If used, please set before sending!
     * </p>
     *
     * The default is {500, 1000, 2000, 5000, 5000}.
     * It is good practice to make the interval bigger with each retry,
     * if the numbers are the same the chance of collision is higher.
     *
     * @param rinterval The interval in msec of each retry
     */
    public void setRetryIntervals(int rinterval[]) {
        retry_intervals = rinterval;
    }

    public int[] getRetryIntervals() {
        return retry_intervals;
    }

    /**
     * Sends the PDU.
     * Note that all properties of the context have to be set before this
     * point.
     */
    public boolean send() throws IOException, PduException {
        return send(errstat, errind);
    }

    /**
     * Sends the PDU. This method accommodates the GetBulk request.
     *
     * @param error_status The value of the error_status field.
     * @param error_index  The value of the error_index field.
     * @see #send()
     */
    protected boolean send(int error_status, int error_index)
            throws IOException, PduException {
        if (added == false) {
            // Moved this statement from the constructor because it
            // conflicts with the way the SnmpContextXPool works.
            added = context.addPdu(this);
        }
        Enumeration vbs = reqVarbinds.elements();
        encodedPacket = context.encodePacket(msg_type, req_id, error_status,
                error_index, vbs, snmpv3MsgId);
        addToTrans();
        return added;
    }

    /**
     * Adds the PDU to its transmitter. The transmitter is the thread that
     * will be sent the PDU and then waits for the answer.
     *
     * @see #send
     */
    protected void addToTrans() {
        if (added && (trans != null)) {
            // thanks to Ian Dowse <iedowse@iedowse.com> for synchronisation
            synchronized (trans) {
                trans.setPdu(this);
                trans.stand();
            }
        }
    }

    /**
     * Sends the actual packet and updates the retries.
     *
     * @see AbstractSnmpContext#sendPacket(byte[] p)
     */
    protected boolean sendme() {
        context.sendPacket(encodedPacket);
        retries++;

        if (AsnObject.debug > 6) {
            System.out.println(getClass().getName() + ".sendme(): Sent Pdu reqId=" + req_id + ", retries " + retries);
        }
        return (retries > 3);
    }

    /**
     * Sends the PDU.
     * For backwards compatibility only. Please use send() instead.
     * The community name will be passed to the SnmpContext. If using
     * SnmpContextv3, the community name will be ignored.
     *
     * @param com The community name of the PDU in SNMPv1 and SNMPv2c.
     * @deprecated Community name has moved to SnmpContext. Use
     *             {@link SnmpContext#setCommunity(String)}.
     *
     * @see SnmpContext#setCommunity
     * @see #send
     */
    public boolean send(String com) throws IOException, PduException {
        if (context instanceof SnmpContext) {
            ((SnmpContext) context).setCommunity(com);
        }
        return send();
    }

    /**
     * Adds an OID (object identifier) to the PDU. The OID indicates which
     * MIB variable we request for or which MIB variable should be set.
     *
     * @param oid The oid
     * @see #addOid(varbind)
     * @see varbind
     */
    public void addOid(String oid) {
        varbind vb = new varbind(oid);
        addOid(vb);
    }

    /**
     * Adds an OID (object identifier) to the PDU. The OID indicates which
     * MIB variable we request for or which MIB variable should be set.
     *
     * @param oid The oid
     * @see #addOid(varbind)
     * @see varbind
     * @since 4_12
     */
    public void addOid(AsnObjectId oid) {
        varbind vb = new varbind(oid);
        addOid(vb);
    }

    /**
     * Adds an OID (object identifier) to the PDU and the value that has
     * to be set. This method has moved from SetPdu to this class in version 4_12.
     *
     * @param oid The oid
     * @param val The value
     * @see Pdu#addOid
     * @see varbind
     * @since 4_12
     */
    public void addOid(String oid, AsnObject val) {
        varbind vb = new varbind(oid, val);
        addOid(vb);
    }

    /**
     * Adds an OID (object identifier) to the PDU and the value that has
     * to be set.
     *
     * <p>
     * Thanks to Eli Bishop (eli@graphesthesia.com) for the suggestion.
     * </p>
     *
     * @param oid The oid
     * @param val The value
     * @see Pdu#addOid
     * @see varbind
     * @since 4_12
     */
    public void addOid(AsnObjectId oid, AsnObject val) {
        varbind vb = new varbind(oid, val);
        addOid(vb);
    }

    /**
     * Adds an OID (object identifier) to the PDU.
     *
     * @param var The varbind
     * @see #addOid(String)
     */
    public void addOid(varbind var) {
        reqVarbinds.addElement(var);
    }

    /**
     * Returns a copy of the varbinds used to build the request.
     *
     * @return the request varbinds of this PDU.
     */
    public varbind[] getRequestVarbinds() {
        int sz = reqVarbinds.size();
        varbind[] arr = new varbind[sz];
        reqVarbinds.copyInto(arr);
        return arr;
    }

    /**
     * Returns a copy of the varbinds received in the response.
     * If there was no response (yet), null will be returned.
     *
     * @return the response varbinds of this PDU.
     * @exception PduException An agent or decoding exception occurred
     *                         whilst receiving the response.
     *
     * @see #getErrorStatus
     * @see #notifyObservers
     */
    public varbind[] getResponseVarbinds() throws PduException {
        if (respException != null) {
            throw respException;
        }

        varbind[] arr = null;
        if (respVarbinds != null) {
            int sz = respVarbinds.size();
            arr = new varbind[sz];
            respVarbinds.copyInto(arr);
        }
        return arr;
    }

    private void dump(Vector v, varbind[] array) {
        int sz = v.size();
        System.out.println("Vector: ");
        for (int i = 0; i < sz; i++) {
            System.out.println("\t" + v.elementAt(i));
        }
        System.out.println("Array: ");
        for (int i = 0; i < array.length; i++) {
            System.out.println("\t" + array[i]);
        }
        System.out.println("--");
    }

    void setResponseException(PduException exc) {
        if (AsnObject.debug > 6) {
            System.out.println(getClass().getName() + ".setResponseException(): reqId=" + req_id + exc.getMessage());
        }
        respException = exc;
    }

    /**
     * Returns the request id of the PDU.
     *
     * @return The ID
     */
    public int getReqId() {
        return req_id;
    }

    /**
     * Returns the error index.
     * The error index indicates which of the OIDs went wrong.
     *
     * @return the error index
     * @see #getErrorStatus
     */
    public int getErrorIndex() {
        return errind;
    }

    /**
     * Returns the error status as indicated by the error-status field in
     * the reponse PDU. The error index will indicated which OID caused the
     * error.
     * In case of a decoding exception the error status
     * will be set to one of the decoding errors:
     * <ul>
     * <li>
     * <code>SnmpConstants.SNMP_ERR_DECODING_EXC</code>.
     * </li>
     * <li>
     * <code>SnmpConstants.SNMP_ERR_DECODINGASN_EXC</code>.
     * </li>
     * <li>
     * <code>SnmpConstants.SNMP_ERR_DECODINGPKTLNGTH_EXC</code>.
     * </li>
     * </ul>
     *
     * <p>
     * The actual exception will be passed to your
     * <code>update(Observable ob, Object arg)</code>
     * method via the the parameter
     * <code>arg</code>.
     * </p>
     *
     * @return the error status
     * @see #notifyObservers
     * @see #getResponseVarbinds
     * @see SnmpConstants#SNMP_ERR_NOERROR
     * @see SnmpConstants#SNMP_ERR_DECODING_EXC
     * @see SnmpConstants#SNMP_ERR_DECODINGASN_EXC
     * @see SnmpConstants#SNMP_ERR_DECODINGPKTLNGTH_EXC
     * @see #getErrorStatusString
     * @see #getErrorIndex
     */
    public int getErrorStatus() {
        return errstat;
    }

    /**
     * Returns the string representation of the error status.
     *
     * @return the error string
     * @see #getErrorStatus
     */
    public String getErrorStatusString() {
        String errString = "";
        if (errstat >= 0) {
            if (errstat < errorStrings.length) {
                errString = errorStrings[errstat];

                if (errstat == AsnObject.SNMP_ERR_GENERR
                        &&
                        isTimedOut() == true) {
                    errString = TIMED_OUT;
                }
            } else {
                // they much be one of the DECODING*_EXC
                if (respException != null) {
                    errString = respException.getMessage();
                } else {
                    errString = "Decoding Exception";
                }
            }
        }
        return errString;
    }

    /**
     * Returns whether or not this PDU is timed out, i.e. it did not get
     * a response.
     * Its errorStatus will be set to AsnObject.SNMP_ERR_GENERR.
     *
     * <p>
     * Note that a SNMP agent can respond with an errorStatus of
     * AsnObject.SNMP_ERR_GENERR as well, so getting a
     * AsnObject.SNMP_ERR_GENERR does not necessarily mean that the request
     * is timed out!
     * </p>
     *
     * @return true is the PDU was timed out
     * @see #getErrorStatus
     * @see SnmpConstants#SNMP_ERR_GENERR
     */
    public boolean isTimedOut() {
        return isTimedOut;
    }

    /**
     * This method will wait until the answer is received, instead of
     * continue with other stuff.
     */
    public boolean waitForSelf() {
        // Add an extra second to the waiting. This gives the PDU a chance
        // to handle the timeout correctly before this thread wakes up.
        long del = 1000;
        for (int i = 0; i < retry_intervals.length; i++) {
            del += retry_intervals[i];
        }
        boolean res = waitForSelf(del);

        if (AsnObject.debug > 6) {
            System.out.println(getClass().getName() + ".waitForSelf(): reqId=" + req_id + " " + res);
        }

        // Should I??
        if (!answered) {
            handleNoAnswer();
        }

        return res;
    }

    /**
     * Returns the string representation of the PDU.
     *
     * @return The string of the PDU
     */
    public String toString() {
        return toString(false);
    }

    /**
     * Returns the string representation of the PDU with or without the
     * response varbinds.
     *
     * @param withRespVars Include the response varbinds or not
     * @return The string of the PDU
     */
    protected String toString(boolean withRespVars) {
        StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append("[");
        buffer.append("context=").append(context);
        buffer.append(", reqId=").append(req_id);
        buffer.append(", msgType=0x").append(SnmpUtilities.toHex(msg_type));

        buffer.append(", ");
        buffer.append(printVars("reqVarbinds", reqVarbinds));

        if (withRespVars == true) {
            buffer.append(", ");
            buffer.append(printVars("respVarbinds", respVarbinds));
        }

        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Returns the string representation of the varbinds of the PDU.
     *
     * @see #toString(boolean)
     * @since 4_14
     */
    protected StringBuffer printVars(String title, Vector vars) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(title).append("=");
        if (vars != null) {
            int sz = vars.size();
            buffer.append("[");
            for (int i = 0; i < sz; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                varbind var = (varbind) vars.elementAt(i);
                buffer.append(var.toString());
            }
            buffer.append("]");
        } else {
            buffer.append("null");
        }
        return buffer;
    }

    synchronized boolean waitForSelf(long delay) {
        if (!got) {
            try {
                wait(delay);
            } catch (InterruptedException ix) {
                ;
            }
        }
        return answered;
    }

    void transmit() {
        transmit(true);
    }

    void transmit(boolean withRetries) {
        if (withRetries == true) {
            int n = 0;
            answered = false;

            while ((!context.isDestroyed()) && (!answered) && (n < retry_intervals.length)) {
                sendme();

                try {
                    Thread.sleep(retry_intervals[n]);
                } catch (InterruptedException e) {
                }
                n++;
            }

            if (!answered) {
                handleNoAnswer();
            }
        } else {
            // just send it once. this will only happen if we are in a trap
            // or response PDU.
            sendme();
            answered = true;
        }

        if (!context.removePdu(req_id)) {
            if (AsnObject.debug > 6) {
                System.out.println(getClass().getName() + ".transmit(): Failed to remove reqId " + req_id);
            }
        }
    }

    void setTrans(Transmitter t) {
        trans = t;
    }

    /**
     * Returns the message type, this will indicate what kind of request we
     * are dealing with.
     * By default it will be set to the GET_REQ_MSG
     *
     * @return The message type
     */
    public byte getMsgType() {
        return msg_type;
    }

    /**
     * Sets the message type, this will indicate what kind of request we
     * are dealing with.
     * By default it will be set to the GET_REQ_MSG
     *
     * @param type The message type
     */
    protected void setMsgType(byte type) {
        msg_type = type;
    }

    /**
     * Sets the error status, indicating what went wrong.
     *
     * @param err the error status
     * @see #getErrorIndex
     * @see #getErrorStatusString
     * @see #getErrorStatus
     */
    protected void setErrorStatus(int err) {
        errstat = err;
        if (AsnObject.debug > 6) {
            System.out.println(getClass().getName() + ".setErrorStatus(): reqId=" + req_id + " " + errstat);
        }
        if (errstat != AsnObject.SNMP_ERR_NOERROR) {
            setResponseException(new AgentException(getErrorStatusString()));
        }
    }

    /**
     * Sets the error status and the exception, indicating what went wrong.
     *
     * @param err the error status
     * @param exc the PDU Exception that was thrown whilst decoding
     *
     * @see #getErrorIndex
     * @see #getErrorStatusString
     * @see #getErrorStatus
     */
    protected void setErrorStatus(int err, PduException exc) {
        errstat = err;
        setResponseException(exc);
    }

    /**
     * Sets the error index, this indicates which of the OIDs went wrong.
     * 
     * @param ind the error index
     * @see #setErrorStatus(int)
     * @see #getErrorIndex
     */
    protected void setErrorIndex(int ind) {
        errind = ind;
    }

    /**
     * Returns whether or not this type of PDU is expecting some kind of response.
     * This method is used in AbstractSnmpContext to help determine whether
     * or not to start a thread that listens for a response when sending this
     * PDU.
     * The default is <em>true</em>.
     *
     * @return true if a response is expected, false if not.
     * @since 4_14
     */
    protected boolean isExpectingResponse() {
        return true;
    }

    /**
     * This method is called when no answer is received after all
     * retries.
     * The application is notified of this.
     * See also fillin()
     */
    private void handleNoAnswer() {
        if (AsnObject.debug > 6) {
            System.out.println(getClass().getName() + ".handleNoAnswer(): reqId=" + req_id);
        }

        // it's a lie, but it will prevent this method from
        // being called twice
        answered = true;

        isTimedOut = true;
        setErrorStatus(AsnObject.SNMP_ERR_GENERR);
        setErrorIndex(0);

        setChanged();
        tell_them();
        clearChanged();

        synchronized (this) {
            notify();
        }
    }

    /**
     * Fill in the received response.
     * 
     * @see Pdu#getResponseVarbinds()
     *
     */
    void fillin(AsnPduSequence seq) {
        if (answered) {
            if (AsnObject.debug > 6) {
                System.out.println(getClass().getName() + ".fillin(): Got a second answer to reqId " + req_id);
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

        synchronized (this) {
            got = true;
            answered = true;
            notify(); // see also handleNoAnswer()
            if (trans != null) {
                // free up the transmitter, since
                // we are happy with the answer.
                // trans may be null if we are receiving a trap.
                trans.interruptMe();
            }
        }
    }

    /**
     * Notify all observers. If a decoding exception had occurred, the argument
     * will be replaced with the exception.
     *
     * <p>
     * In the case of an exception, the error status
     * will be set to one of the decoding errors (see
     * <code>getErrorStatus</code>)
     * and passed as the parameter
     * <code>arg</code> in the
     * <code>update(Observable obs, Object arg)</code>
     * method.
     * </p>
     *
     * @param arg The argument passed to update, can be a PduException.
     * @see SnmpConstants#SNMP_ERR_DECODING_EXC
     * @see #getErrorStatus
     * @see #getResponseVarbinds
     * @since 4.5
     */
    public void notifyObservers(Object arg) {
        if (respException != null) {
            super.notifyObservers(respException);
        } else {
            super.notifyObservers(arg);
        }
    }

}
