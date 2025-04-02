// NAME
//      $RCSfile: DialogChannelStatusBean.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 1.18 $
// CREATED
//      $Date: 2006/02/02 15:49:39 $
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
 * author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
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
 * This bean is written for a Dialogic card.
 *
 * The Dialogic MIBs come with their 
 * <a
 * href="http://support.dialogic.com/Download/boardwatch/index.htm">BoardWatch
 * package</a>. You will have to fill in a form before downloading it.
 *
 * <p>
 * This bean collects information about the voice channel status of the
 * Dialogic card. It will only collect those channels that are open.
 * </p>
 *
 * <p>
 * You can get the data via the getChannelIndexes() and 
 * getChannelStatus(Integer index) methods. This way you can visualise
 * the data yourself. 
 * </p>
 *
 * <p>
 * You can also use the Swing JTree to visualise the channels.
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
 * We only have one (1) Dialogics card, so I don't know how and if it
 * works with more than 1 card. Please let us know if it doesn't work.
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
 * @version $Revision: 1.18 $ $Date: 2006/02/02 15:49:39 $
 *
 */
public class DialogChannelStatusBean extends SNMPRunBean
        implements Observer, TreeNode {
    private static final String version_id = "@(#)$Id: DialogChannelStatusBean.java,v 1.18 2006/02/02 15:49:39 birgit Exp $ Copyright Westhawk Ltd";

    /**
     * A unique value for each R4 device contained by the
     * host. The value for each R4 device must remain
     * constant at least from one re-initialization of the
     * agent to the next re-initialization.
     *
     * See the dlgr4dev.mib.
     */
    public final static String dlgR4DeviceIndex = "1.3.6.1.4.1.3028.2.1.1.2.1.1.1";

    /**
     * Release 4 Device Name. This is the name the application will
     * use when opening the device (e.g. dxxxB1C1, dtiB1T1, msiB1C1)
     * See the dlgr4dev.mib.
     */
    public final static String dlgR4DeviceName = "1.3.6.1.4.1.3028.2.1.1.2.1.1.2";

    /**
     * An indication of the type of the device.
     * <ol>
     * <li>
     * other - some other type of device unknown to the agent.</li>
     * <li>
     * voice - A voice channel device. An entry exits in the
     * dlgR4VoiceTable for this device.</li>
     * <li>
     * dti - A DTI timeslot device. An entry exits in the
     * dlgR4DTITable for this device.</li>
     * <li>
     * isdn - An ISDN B-Channel device. An entry exits in the
     * dlgR4ISDNTable for this device.</li>
     * <li>
     * msi - An MSI station set device. An entry exits in the
     * dlgR4MSITable for this device.</li>
     * </ol>
     * 
     * See the dlgr4dev.mib.
     */
    public final static String dlgR4DeviceType = "1.3.6.1.4.1.3028.2.1.1.2.1.1.3";

    /**
     * An indication of how many instances of this device is currently
     * opened.
     * See the dlgr4dev.mib.
     */
    public final static String dlgR4DeviceOpenCount = "1.3.6.1.4.1.3028.2.1.1.2.1.1.5";

    /**
     * Indicates current activity status on the (voice) channel.
     * See the dlgr4dev.mib.
     */
    public final static String dlgR4VoiceChannelStatus = "1.3.6.1.4.1.3028.2.1.1.2.2.1.2";

    private final static int NR_OID = 5;

    // dlgR4DeviceType values
    public final static int unknown = 1;
    public final static int voice = 2;
    public final static int dti = 3;
    public final static int isdn = 4;
    public final static int msi = 5;

    // dlgR4VoiceChannelStatus values
    public final static int idle = 1;
    public final static int playing = 2;
    public final static int recording = 3;
    public final static int gettingDigits = 5;
    public final static int blocked = 16;

    public final static int dialing = 4;
    public final static int playTone = 6;
    public final static int sendingFax = 8;
    public final static int receivingFax = 9;
    public final static int betweenFAXPages = 10;
    public final static int hookState = 11;
    public final static int winking = 12;
    public final static int callProgess = 13;
    public final static int gettingR2MF = 14;

    public final static String vch_status[] = {
            "",
            "Channel not active",
            "Playing Audio Data",
            "Recording Audio Data",
            "Dialing Digits",
            "Collecting DTMF digits",
            "Playing a tone",
            "",
            "Sending a FAX (VFX boards)",
            "Receiving a FAX (VFX boards)",
            "Between FAX pages (VFX boards)",
            "Changing hook status to onhook or offhook",
            "Performing a wink",
            "Performing Call progress analysis",
            "Retrieving R2MF digits",
            "",
            "Blocked"
    };

    private GetNextPdu_vec pdu;
    private Hashtable channelIndexStatusHash;
    private Hashtable channelHash;

    private TreeNode parent;
    private DefaultTreeModel treeModel;

    private boolean isGetNextInFlight;
    private Date lastUpdateDate = null;

    private int deviceType = 0;
    private String deviceName = "";
    private int openCount = 0;
    private int channelStatus = 0;

    /**
     * The default constructor.
     */
    public DialogChannelStatusBean() {
        channelIndexStatusHash = new Hashtable();
        channelHash = new Hashtable();
    }

    /**
     * The constructor that will set the host and the port no.
     *
     * @param h the hostname
     * @param p the port no
     * @see SNMPBean#setHost
     * @see SNMPBean#setPort
     */
    public DialogChannelStatusBean(String h, int p) {
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
    public DialogChannelStatusBean(String h, int p, String b) {
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
     * Returns the indexes (as Integers) of the voice channels that are
     * open. Only the open voice channels of the card are saved.
     * 
     * Use getChannelStatus() or getChannelStatusString() to get the status
     * of the channel.
     *
     * @see #getChannelStatus
     * @see #getChannelStatusString
     */
    public Enumeration getChannelIndexes() {
        return channelIndexStatusHash.keys();
    }

    /**
     * Returns the number of voice channels in the table.
     */
    public synchronized int getChannelCount() {
        return channelIndexStatusHash.size();
    }

    /**
     * Returns the name of the channel.
     *
     * @param index The index of the channel
     * @return The name
     *
     */
    public String getChannelName(Integer index) {
        String name = "";
        Object obj = channelHash.get(index);
        if (obj != null) {
            ChannelStatus chSt = (ChannelStatus) obj;
            name = chSt.getName();
        }
        return name;
    }

    /**
     * Returns the status of the channel as an int.
     *
     * @param index The index of the channel
     * @return The status
     *
     * @see #getChannelIndexes
     */
    public int getChannelStatus(Integer index) {
        int status = 0;
        Object obj = channelIndexStatusHash.get(index);
        if (obj != null) {
            status = ((Integer) obj).intValue();
        }
        return status;
    }

    /**
     * Returns the String representation of the status of the channel.
     *
     * @param index The index of the channel
     * @return The status
     *
     * @see #getChannelIndexes
     */
    public String getChannelStatusString(Integer index) {
        int status = getChannelStatus(index);
        String str = vch_status[status];
        return str;
    }

    /**
     * Returns the children of the reciever as an Enumeration.
     */
    public Enumeration children() {
        return channelHash.elements();
    }

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount() {
        int sz = channelHash.size();
        return sz;
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     */
    public TreeNode getChildAt(int childIndex) {
        TreeNode node = null;
        if (childIndex < channelHash.size()) {
            Enumeration e = channelHash.elements();
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
        if (channelHash.contains(node)) {
            boolean found = false;
            Enumeration e = channelHash.elements();
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

                pdu = new GetNextPdu_vec(context, NR_OID);
                pdu.addObserver(this);

                pdu.addOid(dlgR4DeviceIndex);
                pdu.addOid(dlgR4DeviceName);
                pdu.addOid(dlgR4DeviceType);
                pdu.addOid(dlgR4DeviceOpenCount);
                pdu.addOid(dlgR4VoiceChannelStatus);
                try {
                    pdu.send();
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
        boolean hasEnded = false;
        int index;
        varbind[] var;

        pdu = (GetNextPdu_vec) obs;

        if (pdu.isTimedOut() == false) {
            if (pdu.getErrorStatus() == AsnObject.SNMP_ERR_NOERROR) {
                var = (varbind[]) ov;
                if (var[0].getOid().toString().startsWith(dlgR4DeviceIndex)) {
                    index = ((AsnInteger) var[0].getValue()).getValue();
                    deviceName = ((AsnOctets) var[1].getValue()).getValue();
                    deviceType = ((AsnInteger) var[2].getValue()).getValue();
                    openCount = ((AsnInteger) var[3].getValue()).getValue();
                    channelStatus = ((AsnInteger) var[4].getValue()).getValue();

                    if (deviceType == voice) {
                        Integer indexInt = new Integer(index);
                        if (openCount == 0) {
                            channelIndexStatusHash.remove(indexInt);
                            channelHash.remove(indexInt);
                        } else {
                            channelIndexStatusHash.put(indexInt,
                                    new Integer(channelStatus));

                            ChannelStatus cStatus = (ChannelStatus) channelHash.get(indexInt);

                            if (cStatus == null) {
                                cStatus = new ChannelStatus(index, channelStatus,
                                        deviceName, this);
                                channelHash.put(indexInt, cStatus);
                            } else {
                                cStatus.setStatus(channelStatus);
                            }
                        }
                    }

                    // ask for the next channel
                    pdu = new GetNextPdu_vec(context, NR_OID);
                    pdu.addObserver(this);

                    pdu.addOid(var[0].getOid().toString());
                    pdu.addOid(var[1].getOid().toString());
                    pdu.addOid(var[2].getOid().toString());
                    pdu.addOid(var[3].getOid().toString());
                    pdu.addOid(var[4].getOid().toString());
                    try {
                        pdu.send();
                    } catch (PduException exc) {
                        System.out.println("PduException " + exc.getMessage());
                    } catch (IOException exc) {
                        System.out.println("IOException " + exc.getMessage());
                    }
                } else {
                    hasEnded = true;
                }
            } else {
                hasEnded = true;
            }
        } else {
            channelIndexStatusHash.clear();
            channelHash.clear();
            hasEnded = true;
        }

        if (hasEnded) {
            // the GetNext loop has ended
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

    class ChannelStatus extends Object implements TreeNode {
        private TreeNode parent;
        private int index, status;
        private String name;

        public ChannelStatus(int ind, int st, String nm, TreeNode par) {
            index = ind;
            status = st;
            name = nm;
            parent = par;
        }

        public int getIndex() {
            return index;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int st) {
            status = st;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return "" + index + " " + name + " " + vch_status[status];
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

    } // end class ChannelStatus

    class TreeUpdate implements Runnable {
        public void run() {
            fireTreeModelChanged();
            firePropertyChange("modems", null, null);
        }
    }

}
