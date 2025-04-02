// NAME
//      $RCSfile: BlockPdu.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.13 $
// CREATED
//      $Date: 2006/01/17 17:43:53 $
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
 
package uk.co.westhawk.snmp.pdu;

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
import uk.co.westhawk.snmp.stack.*;
import java.util.*;

/**
 * The BlockPdu class is a wrapper class that will block until it
 * receives the answer. 
 *
 * @see Pdu
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 3.13 $ $Date: 2006/01/17 17:43:53 $
 */
public class BlockPdu extends Object {
    private static final String version_id = "@(#)$Id: BlockPdu.java,v 3.13 2006/01/17 17:43:53 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * The GET PDU type.
     */
    public final static int GET = 0;
    /**
     * The SET PDU type.
     */
    public final static int SET = 1;
    /**
     * The GETNEXT PDU type.
     */
    public final static int GETNEXT = 2;
    /**
     * The GETBULK PDU type. Note, the getBulkRequest was introduced in
     * SNMPv2c. For that reason it should not be sent with SNMPv1.
     */
    public final static int GETBULK = 3;

    protected Vector reqVarbinds;
    private SnmpContextBasisFace context;
    private Pdu pdu;
    private int type = GET;
    private int non_rep = 0;
    private int max_rep = 0;
    private int retry_intervals[] = null;

    /**
     * Constructor.
     *
     * @param con The context of the request
     */
    public BlockPdu(SnmpContextBasisFace con) {
        context = con;
        reqVarbinds = new Vector(1, 1);
    }

    /**
     * Adds an OID to the PDU.
     *
     * @param oid The oid
     * @see Pdu#addOid(String)
     */
    public void addOid(String oid) {
        varbind vb = new varbind(oid);
        addOid(vb);
    }

    /**
     * Adds an OID to the PDU and the value that has to be set.
     * This is only useful for the SET type.
     *
     * @param oid The oid
     * @see SetPdu#addOid(String, AsnObject)
     * @see #SET
     */
    public void addOid(String oid, AsnObject val) {
        varbind vb = new varbind(oid, val);
        addOid(vb);
    }

    /**
     * Adds an OID (object identifier) to the PDU and the value that has
     * to be set.
     * This is only useful for the SET type.
     *
     * @param oid The oid
     * @param val The value
     * @see Pdu#addOid(AsnObjectId, AsnObject)
     * @see varbind
     * @since 4_12
     */
    public void addOid(AsnObjectId oid, AsnObject val) {
        varbind vb = new varbind(oid, val);
        addOid(vb);
    }

    /**
     * Adds an OID (object identifier) to the PDU. The OID indicates which
     * MIB variable we request or which MIB variable should be set.
     *
     * @param oid The oid
     * @see Pdu#addOid(AsnObjectId)
     * @see varbind
     * @since 4_12
     */
    public void addOid(AsnObjectId oid) {
        varbind vb = new varbind(oid);
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
     * Sets the getBulkRequest parameters.
     * This is only useful for the GETBULK type.
     * If these parameters are not set, they both default to zero.
     *
     * @param nr The non repeaters.
     * @param mr The max repetitions.
     * @see GetBulkPdu#setMaxRepetitions(int)
     * @see GetBulkPdu#setNonRepeaters(int)
     * @see #GETBULK
     */
    public void setBulkParameters(int nr, int mr) {
        non_rep = nr;
        max_rep = mr;
    }

    /**
     * Adds a list of OIDs to the PDU.
     *
     * @param oids The OIDs to be added
     */
    public void addOid(String[] oids) {
        for (int i = 0; i < oids.length; i++) {
            varbind vb = new varbind(oids[i]);
            reqVarbinds.addElement(vb);
        }
    }

    /**
     * Sets the type of PDU. The type indicates the kind of request, i.e.
     * the getRequest (the default), the setRequest, the getNextRequest, or
     * the getBulkRequest.
     *
     * @param newType The type of request
     * @see #GET
     * @see #SET
     * @see #GETNEXT
     * @see #GETBULK
     */
    public void setPduType(int newType) {
        type = newType;
    }

    /**
     * Sets the retry intervals of the PDU. This method overwrites the
     * default values in the PDU class.
     *
     * @param retryIntervals The interval in msec of each retry
     * @see Pdu#setRetryIntervals(int[])
     */
    public void setRetryIntervals(int retryIntervals[]) {
        retry_intervals = retryIntervals;
    }

    /**
     * Sends the request and waits (blocks) for the response. Returns the
     * value of the first variable binding in the response. If no response was
     * received, this will be null.
     * 
     * @return The value of the first variable binding in the response
     * @see #sendAndWait()
     */
    public AsnObject getResponseVariable()
            throws PduException, java.io.IOException {
        AsnObject value = null;
        varbind[] vars = sendAndWait();
        if (vars != null && vars.length > 0) {
            value = vars[0].getValue();
        }
        return value;
    }

    /**
     * Sends the request and waits (blocks) for the response. Returns the
     * values of the variable bindings in the response. If no response was
     * received, this will be null.
     * 
     * @return The values of the variable bindings in the response
     * @see #sendAndWait()
     */
    public AsnObject[] getResponseVariables()
            throws PduException, java.io.IOException {
        AsnObject[] values = null;
        varbind[] vars = sendAndWait();
        if (vars != null) {
            values = new AsnObject[vars.length];
            for (int i = 0; i < vars.length; i++) {
                values[i] = vars[i].getValue();
            }
        }
        return values;
    }

    /**
     * Sends the request and waits (blocks) for the response. Returns the
     * first variable binding in the response. If no response was received, this
     * will be null.
     * 
     * @return The first variable binding in the response
     * @see #sendAndWait()
     */
    public varbind getResponseVariableBinding()
            throws PduException, java.io.IOException {
        varbind var = null;
        varbind[] vars = sendAndWait();
        if (vars != null && vars.length > 0) {
            var = vars[0];
        }
        return var;
    }

    /**
     * Sends the request and waits (blocks) for the response. Returns the
     * variable bindings in the response. If no response was received, this
     * will be null.
     * 
     * @return The variable bindings in the response
     * @see #sendAndWait()
     */
    public varbind[] getResponseVariableBindings()
            throws PduException, java.io.IOException {
        varbind[] vars = sendAndWait();
        return vars;
    }

    /**
     * Sends the request and waits (blocks) for the response. This is the core
     * of the blocking getResponseVariableX() methods.
     * 
     * @return The response, can be null if there was a timeout
     * @see Pdu#send()
     * @see Pdu#waitForSelf()
     * @see Pdu#getResponseVarbinds()
     */
    protected synchronized varbind[] sendAndWait()
            throws PduException, java.io.IOException {
        int sz = reqVarbinds.size();
        switch (type) {
            case SET: {
                pdu = new SetPdu_vec(context, sz);
                break;
            }
            case GETNEXT: {
                pdu = new GetNextPdu_vec(context, sz);
                break;
            }
            case GETBULK: {
                pdu = new GetBulkPdu(context);
                ((GetBulkPdu) pdu).setNonRepeaters(non_rep);
                ((GetBulkPdu) pdu).setMaxRepetitions(max_rep);
                break;
            }
            default: {
                pdu = new GetPdu_vec(context, sz);
            }
        }
        for (int i = 0; i < sz; i++) {
            varbind var = (varbind) reqVarbinds.elementAt(i);
            pdu.addOid(var);
        }
        if (retry_intervals != null) {
            pdu.setRetryIntervals(retry_intervals);
        }
        pdu.send();
        pdu.waitForSelf();
        varbind[] vars = pdu.getResponseVarbinds();
        return vars;
    }

    /**
     * Returns the error index of the PDU.
     * This will only have been set after the response is received.
     * Thanks to Maga Hegde (mhegde@zumanetworks.com) to requesting this method.
     *
     * @see Pdu#getErrorIndex()
     * @return The error index
     */
    public int getErrorIndex() {
        return pdu.getErrorIndex();
    }

    /**
     * Returns the error status of the PDU.
     * This will only have been set after the response is received.
     *
     * @see Pdu#getErrorStatus()
     * @return The error status
     */
    public int getErrorStatus() {
        return pdu.getErrorStatus();
    }

    /**
     * Returns the error status string of the PDU.
     * This will only have been set after the response is received.
     *
     * @see Pdu#getErrorStatusString()
     * @return The error status string
     */
    public String getErrorStatusString() {
        return pdu.getErrorStatusString();
    }

}
