// NAME
//      $RCSfile: AnnexModemStatusBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.22 $
// CREATED
//      $Date: 2006/03/23 14:54:09 $
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//

/*
 * Copyright (C) 1998 - 2006 by Westhawk Ltd
 *
 * Permission to use, copy, modify, and distribute this software
 * for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that
 * both the copyright notice and this permission notice appear in
 * supporting documentation.
 * This software is provided "as is" without express or implied
 * warranty.
 */

package uk.co.westhawk.snmp.beans;

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
import uk.co.westhawk.snmp.pdu.*;

import javax.swing.tree.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.beans.*;

/**
 * <p>
 * This bean is written for 
 * <a href="http://www.nortelnetworks.com/link/remote_annex_2000">
 * Remote Annex 2000</a>
 * access server.
 * </p>
 *
 * <p>
 * The server comes with the Xylogics specific MIBs, you can
 * find them in the Annex software installation, I couldn't find them on
 * the Web. I did not use them for this bean, however. 
 * </p>
 *
 * <p>
 * This bean uses the 
 * <a href="http://www.ietf.org/rfc/rfc1658.txt">CHARACTER-MIB</a>
 * and 
 * <a href="http://www.ietf.org/rfc/rfc1659.txt">RS-232-MIB</a>.
 * </p>
 *
 * <p>
 * This bean collects information about the modem status,
 * connected to the server. 
 * It will only collect those modems (see charPortOperStatus) that are
 * "up". It will then show if the modem is in use of not (see
 * rs232InSigState).
 * </p>
 *
 * <p>
 * You can get the data via the getModemIndexes() and 
 * getPortStatus(Long index) methods. This way you can visualise
 * the data yourself. 
 * </p>
 *
 * <p>
 * You can also use the Swing JTree to visualise the modems.
 * This bean implements the Swing TreeNode interface. 
 * The user has to set the setDefaultTreeModel, so this class can update all
 * the nodes that are added, removed of changed.
 * If this class is <em>not</em> the root of the JTree, you have to set
 * the (TreeNode) parent of this class.
 * </p>
 *
 * <p>
 * The properties in the parent classes should be set, before calling
 * the action() method. Via a TreeModelEvent the application/applet
 * will be notified. 
 * </p>
 *
 * <p>
 * We only have one Annex server, so I don't know how and if it
 * works with more than 1. Please let us know if it doesn't work.
 * </p>
 *
 * @see SNMPBean#setHost
 * @see SNMPBean#setPort
 * @see SNMPBean#setCommunityName
 * @see SNMPRunBean#setUpdateInterval
 * @see SNMPBean#addPropertyChangeListener
 * @see SNMPBean#action
 * @see GetNextPdu_vec
 * 
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.22 $ $Date: 2006/03/23 14:54:09 $
 */
public class AnnexModemStatusBean extends SNMPRunBean
        implements Observer, TreeNode {
    private static final String version_id = "@(#)$Id: AnnexModemStatusBean.java,v 1.22 2006/03/23 14:54:09 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * A unique value for each character port. This perhaps
     * corresponding to the same value of ifIndex when the
     * character port is associated with a hardware port
     * represented by an ifIndex.
     *
     * <p>
     * Syntax: INTEGER
     * </p>
     *
     * See the CHARACTER-MIB.
     */
    public final static String charPortIndex = "1.3.6.1.2.1.19.2.1.1";
    /**
     * An administratively assigned name for the port, typically with
     * some local significance.
     * 
     * <p>
     * Syntax: DisplayString
     * </p>
     *
     * See <a href="http://www.ietf.org/rfc/rfc1658.txt">CHARACTER-MIB</a>.
     */
    public final static String charPortName = "1.3.6.1.2.1.19.2.1.2";

    /**
     * The port's actual, operational state, independent
     * of flow control.
     * <ol>
     * <li>
     * 'up' indicates able to function normally.</li>
     * <li>
     * 'down' indicates inability to function for administrative or
     * operational reasons.</li>
     * <li>
     * 'maintenance' indicates a maintenance mode, exclusive of normal
     * operation, such as running a test.</li>
     * <li>
     * 'absent' indicates that port hardware is not present.</li>
     * <li>
     * 'active' indicates up with a user present (e.g. logged in).</li>
     * </ol>
     *
     * <p>
     * 'up' and 'active' correspond to ifOperStatus (rfc2863.mib) 'up'.
     * 'down' and 'absent' correspond to ifOperStatus 'down'.
     * 'maintenance' corresponds to ifOperStatus 'test'.
     * </p>
     *
     * <p>
     * Syntax: INTEGER
     * </p>
     *
     * See <a href="http://www.ietf.org/rfc/rfc1658.txt">CHARACTER-MIB</a>
     * and <a href="http://www.ietf.org/rfc/rfc2863.txt">IF-MIB-MIB</a>.
     */
    public final static String charPortOperStatus = "1.3.6.1.2.1.19.2.1.7";

    /**
     * The current signal state.
     *
     * <p>
     * Syntax: INTEGER
     * </p>
     *
     * See <a href="http://www.ietf.org/rfc/rfc1659.txt">RS-232-MIB</a>.
     */
    public final static String rs232InSigState = "1.3.6.1.2.1.10.33.5.1.3";

    private final static int NR_PORT_OID = 3;

    /** charPortOperStatus up. */
    public final static int portUP = 1;
    /** charPortOperStatus down. */
    public final static int portDOWN = 2;
    /** charPortOperStatus maintenance. */
    public final static int portMAINTENANCE = 3;
    /** charPortOperStatus absent. */
    public final static int portABSENT = 4;
    /** charPortOperStatus active. */
    public final static int portACTIVE = 5;

    /**
     * One of rs232InSigName values. If the dcd of rs232InSigState is
     * on, that means that there is contact.
     */
    public final static int DCD = 6;

    /** rs232InSigState none. */
    public final static int sigNONE = 1;
    /** rs232InSigState on. */
    public final static int sigON = 2;
    /** rs232InSigState off. */
    public final static int sigOFF = 3;

    public final static String[] sig_state = {
            "unknown",
            "none",
            "in use",
            "not in use"
    };

    private GetNextPdu_vec pduGetNext;
    private Hashtable modemIndexStatusHash;
    private Hashtable modemHash;

    private TreeNode parent;
    private DefaultTreeModel treeModel;

    private boolean isGetNextInFlight;
    private Date lastUpdateDate = null;

    private int deviceType = 0;
    private int openCount = 0;
    private int modemStatus = 0;

    /**
     * The default constructor.
     */
    public AnnexModemStatusBean() {
        modemIndexStatusHash = new Hashtable();
        modemHash = new Hashtable();
    }

    /**
     * The constructor that will set the host and the port no.
     *
     * @param h the hostname
     * @param p the port no
     * @see SNMPBean#setHost
     * @see SNMPBean#setPort
     */
    public AnnexModemStatusBean(String h, int p) {
        this(h, p, null);
    }

    /**
     * The constructor that will set the host, the port no and the local
     * bind address.
     *
     * @param h the hostname
     * @param p the port no
     * @param b the local bind address
     * @see SNMPBean#setHost
     * @see SNMPBean#setPort
     * @see SNMPBean#setBindAddress
     *
     * @since 4_14
     */
    public AnnexModemStatusBean(String h, int p, String b) {
        this();
        setHost(h);
        setPort(p);
        setBindAddress(b);
    }

    /**
     * Returns the date of the moment when this bean was last updated.
     * This might be null when the first time the update was not finished.
     *
     * @return the last update date
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * Returns the indexes (as Integers) of the voice modems that are
     * open. Only the open voice modems of the card are saved.
     * 
     * Use getPortStatus() or getPortStatusString() to get the status
     * of the modem.
     *
     * @see #getPortStatus
     * @see #getPortStatusString
     */
    public Enumeration getModemIndexes() {
        return modemIndexStatusHash.keys();
    }

    /**
     * Returns the number of voice modems in the table.
     */
    public synchronized int getModemCount() {
        return modemIndexStatusHash.size();
    }

    /**
     * Returns the name of the port.
     *
     * @param index The index of the port
     * @return The name
     *
     */
    public String getPortName(Long index) {
        String name = "";
        Object obj = modemHash.get(index);
        if (obj != null) {
            PortInfo pInfo = (PortInfo) obj;
            name = pInfo.getName();
        }
        return name;
    }

    /**
     * Returns the status of the modem as an int.
     *
     * @param index The index of the modem
     * @return The status
     *
     * @see #getModemIndexes
     */
    public int getPortStatus(Long index) {
        int status = 0;
        Object obj = modemIndexStatusHash.get(index);
        if (obj != null) {
            status = ((Integer) obj).intValue();
        }
        return status;
    }

    /**
     * Returns the String representation of the status of the modem.
     *
     * @param index The index of the modem
     * @return The status
     *
     * @see #getModemIndexes
     */
    public String getPortStatusString(Long index) {
        int status = getPortStatus(index);
        String str = sig_state[status];
        return str;
    }

    /**
     * Returns the children of the reciever as an Enumeration.
     */
    public Enumeration children() {
        return modemHash.elements();
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount() {
        int sz = modemHash.size();
        return sz;
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     */
    public TreeNode getChildAt(int childIndex) {
        TreeNode node = null;
        if (childIndex < modemHash.size()) {
            Enumeration e = modemHash.elements();
            for (int i = 0; i <= childIndex; i++) {
                node = (TreeNode) e.nextElement();
            }
        }
        return node;
    }

    /**
     * Returns the index of <code>node</code> in the receivers children.
     * If the receiver does not contain <code>node</code>, -1 will be
     * returned.
     */
    public int getIndex(TreeNode node) {
        int ret = -1;
        if (modemHash.contains(node)) {
            boolean found = false;
            Enumeration e = modemHash.elements();
            while (e.hasMoreElements() && !found) {
                TreeNode n = (TreeNode) e.nextElement();
                found = (n == node);
                ret++;
            }
        }
        return ret;
    }

    /**
     * Returns the parent <code>TreeNode</code> of the receiver.
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Returns true if the receiver allows children.
     */
    public boolean getAllowsChildren() {
        return true;
    }

    /**
     * Returns true if the receiver is a leaf.
     */
    public boolean isLeaf() {
        return false;
    }

    /**
     * This method starts the action of the bean. It will initialises
     * all variables before starting.
     */
    public void action() {
        if (isHostPortReachable()) {
            lastUpdateDate = new Date();
            isGetNextInFlight = false;
            setRunning(true);
        }
    }

    /**
     * Implements the running of the bean.
     *
     * It will send the Pdu, if the previous one is not still in flight.
     * 
     * @see SNMPRunBean#isRunning()
     */
    public void run() {
        while (context != null && isRunning()) {
            if (isGetNextInFlight == false) {
                // start the GetNext loop again
                isGetNextInFlight = true;

                pduGetNext = new GetNextPdu_vec(context, NR_PORT_OID);
                pduGetNext.addObserver(this);

                pduGetNext.addOid(charPortIndex);
                pduGetNext.addOid(charPortName);
                pduGetNext.addOid(charPortOperStatus);
                try {
                    pduGetNext.send();
                } catch (PduException exc) {
                    System.out.println("PduException " + exc.getMessage());
                } catch (IOException exc) {
                    System.out.println("IOException " + exc.getMessage());
                }
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException ix) {
                ;
            }
        }
    }

    /**
     * This method is called when the Pdu response is received. When all
     * answers are received it will fire the property change event.
     *
     */
    public void update(Observable obs, Object ov) {
        boolean loopHasEnded = false;

        if (obs instanceof GetNextPdu_vec) {
            int portIndex, portStatus;
            String portName;

            varbind[] var;
            pduGetNext = (GetNextPdu_vec) obs;

            if (pduGetNext.isTimedOut() == false) {
                if (pduGetNext.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
                    var = (varbind[]) ov;
                    if (var[0].getOid().toString().startsWith(charPortIndex)) {
                        portIndex = ((AsnInteger) var[0].getValue()).getValue();
                        portName = ((AsnOctets) var[1].getValue()).getValue();
                        portStatus = ((AsnInteger) var[2].getValue()).getValue();

                        Long indexInt = new Long(portIndex);
                        if (portStatus == portUP) {
                            // I've found a port that has status up
                            // Store it for later use.

                            PortInfo pStatus = (PortInfo) modemHash.get(indexInt);

                            if (pStatus == null) {
                                pStatus = new PortInfo(portIndex, portName, this);
                                modemHash.put(indexInt, pStatus);
                            }

                            // Now I have to find out its sigState
                            String oid = rs232InSigState + "." + String.valueOf(portIndex)
                                    + "." + String.valueOf(DCD);

                            try {
                                GetPdu pduGet = new GetPdu(context);
                                pduGet.addOid(oid);
                                pduGet.addObserver(this);
                                pduGet.send();
                            } catch (PduException exc) {
                                System.out.println("PduException " + exc.getMessage());
                            } catch (IOException exc) {
                                System.out.println("IOException " + exc.getMessage());
                            }
                        } else {
                            // This port is not up, remove it from our info

                            modemIndexStatusHash.remove(indexInt);
                            modemHash.remove(indexInt);
                        }

                        // ask for the next modem
                        pduGetNext = new GetNextPdu_vec(context, NR_PORT_OID);
                        pduGetNext.addObserver(this);

                        pduGetNext.addOid(var[0].getOid().toString());
                        pduGetNext.addOid(var[1].getOid().toString());
                        pduGetNext.addOid(var[2].getOid().toString());
                        try {
                            pduGetNext.send();
                        } catch (PduException exc) {
                            System.out.println("PduException " + exc.getMessage());
                        } catch (IOException exc) {
                            System.out.println("IOException " + exc.getMessage());
                        }
                    } else {
                        loopHasEnded = true;
                    }
                } else {
                    // Actually the loop is ended when
                    // we have all the responses from pduGet's. Since I
                    // cannot control that, I do it this way.

                    loopHasEnded = true;
                }
            } else {
                modemIndexStatusHash.clear();
                modemHash.clear();
                loopHasEnded = true;
            }
        } else if (obs instanceof GetPdu) {
            GetPdu pduGet = (GetPdu) obs;
            if (pduGet.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
                varbind var = (varbind) ov;

                AsnObjectId oid = var.getOid();
                int sigStatus = ((AsnInteger) var.getValue()).getValue();

                // the index is the one-but-last element in the OID
                int len = oid.getSize();
                long portIndex = oid.getElementAt(len - 2);

                Long indexInt = new Long(portIndex);
                modemIndexStatusHash.put(indexInt,
                        new Integer(sigStatus));

                PortInfo pStatus = (PortInfo) modemHash.get(indexInt);

                if (pStatus != null) {
                    pStatus.setStatus(sigStatus);
                }
            }
        }

        if (loopHasEnded) {
            lastUpdateDate = new Date();
            javax.swing.SwingUtilities.invokeLater(new TreeUpdate());
            isGetNextInFlight = false;
        }

    }

    /**
     * Sets the parent for this TreeNode. If the parent is not set, this
     * class should be the root of the TreeModel.
     */
    public void setParent(TreeNode p) {
        parent = p;
    }

    /**
     * Sets the DefaultTreeModel for this TreeNode. The tree model is used
     * for notifying when any of the nodes were added, removed or changed.
     */
    public void setDefaultTreeModel(DefaultTreeModel model) {
        treeModel = model;
    }

    /**
     * Fire the property event.
     *
     * @see
     *      DefaultTreeModel#nodeStructureChanged
     */
    protected void fireTreeModelChanged() {
        if (treeModel != null) {
            treeModel.nodeStructureChanged(this);
        }
    }

    class PortInfo extends Object implements TreeNode {
        private TreeNode parent;
        private int portIndex, sigStatus;
        private String portName;

        public PortInfo(int ind, String nm, TreeNode par) {
            this(ind, 0, nm, par);
        }

        public PortInfo(int ind, int st, String nm, TreeNode par) {
            portIndex = ind;
            sigStatus = st;
            portName = nm;
            parent = par;
        }

        public int getIndex() {
            return portIndex;
        }

        public int getStatus() {
            return sigStatus;
        }

        public void setStatus(int st) {
            sigStatus = st;
        }

        public String getName() {
            return portName;
        }

        public String toString() {
            return "" + portIndex + " " + portName + " " + sig_state[sigStatus];
        }

        /**
         * Returns the children of the reciever as an Enumeration.
         */
        public Enumeration children() {
            return null;
        }

        /**
         * Returns the number of children <code>TreeNode</code>s the receiver
         * contains.
         */
        public int getChildCount() {
            return 0;
        }

        /**
         * Returns the child <code>TreeNode</code> at index
         * <code>childIndex</code>.
         */
        public TreeNode getChildAt(int childIndex) {
            return null;
        }

        /**
         * Returns the index of <code>node</code> in the receivers children.
         * If the receiver does not contain <code>node</code>, -1 will be
         * returned.
         */
        public int getIndex(TreeNode node) {
            return -1;
        }

        /**
         * Returns the parent <code>TreeNode</code> of the receiver.
         */
        public TreeNode getParent() {
            return parent;
        }

        /**
         * Returns true if the receiver allows children.
         */
        public boolean getAllowsChildren() {
            return true;
        }

        /**
         * Returns true if the receiver is a leaf.
         */
        public boolean isLeaf() {
            return true;
        }

    } // end class PortInfo

    class TreeUpdate implements Runnable {
        public void run() {
            fireTreeModelChanged();
            firePropertyChange("modems", null, null);
        }
    }

}
