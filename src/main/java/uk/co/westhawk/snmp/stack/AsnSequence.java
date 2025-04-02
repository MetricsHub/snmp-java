// NAME
//      $RCSfile: AsnSequence.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision: 3.15 $
// CREATED
//      $Date: 2006/11/29 16:35:29 $
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
 * original version by hargrave@dellgate.us.dell.com (Jordan Hargrave)
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

import java.io.*;
import java.util.*;


/**
 * This class represents the ASN.1 sequence class.
 * This class contains a Sequence of AsnObjects.
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 3.15 $ $Date: 2006/11/29 16:35:29 $
 */
class AsnSequence extends AsnObject {
    private static final String version_id = "@(#)$Id: AsnSequence.java,v 3.15 2006/11/29 16:35:29 birgit Exp $ Copyright Westhawk Ltd";

    private Vector children;

    /**
     * Constructors.
     */
    AsnSequence() {
        this(CONS_SEQ);
    }

    /**
     * Constructors.
     */
    AsnSequence(byte oddtype) {
        type = oddtype;
        children = new Vector(1, 1);
    }

    /**
     * Constructors.
     * 
     * @param pos The position of the first child
     */
    AsnSequence(InputStream in, int len, int pos) throws IOException {
        this();
        if (debug > 10) {
            System.out.println("AsnSequence(): Length = " + len
                    + ", Pos = " + pos);
        }
        AsnObject a = null;
        while (true) {
            a = AsnReadHeader(in, pos);
            if (a != null) {
                pos += (a.headerLength + a.contentsLength);
                add(a);
            } else {
                break; // all done
            }
        }
    }

    /**
     * Returns the string representation of the AsnSequence.
     *
     * @return The string of the AsnSequence
     */
    public String toString() {
        return "";
    }

    /**
     * Adds a child to the sequence.
     */
    AsnObject add(AsnObject child) {
        if (child.isCorrect == true) {
            children.addElement(child);
        }
        return child;
    }

    /**
     * Replaces one child by the other.
     * This is used by v3 to insert the security / auth fingerprint.
     */
    AsnObject replaceChild(AsnObject oldChild, AsnObject newChild) {
        AsnObject ret = oldChild;
        int at = children.indexOf(oldChild);
        if (at > -1) {
            children.setElementAt(newChild, at);
            ret = newChild;
        }
        return ret;
    }

    /**
     * Returns the total size of the children.
     */
    int size() throws EncodingException {
        Enumeration childList = children.elements();
        int cnt, sz = 0;

        while (childList.hasMoreElements()) {
            // Get size of child
            cnt = ((AsnObject) childList.nextElement()).size();

            // Add space for type byte & length encoding
            cnt += (1 + getLengthBytes(cnt));
            sz += cnt;
        }
        return sz;
    }

    void write(OutputStream out)
            throws IOException, EncodingException {
        write(out, 0);
    }

    void write(OutputStream out, int pos)
            throws IOException, EncodingException {
        // Output header
        int length = size();
        startPos = pos;
        AsnBuildHeader(out, type, length);
        if (debug > 10) {
            System.out.println("\tAsnSequence.write(): begin, startPos = "
                    + startPos);
        }

        // Output children
        pos += headerLength;
        Enumeration childList = children.elements();
        while (childList.hasMoreElements()) {
            AsnObject child = ((AsnObject) childList.nextElement());
            child.write(out, pos);
            child.startPos = pos;
            pos += child.headerLength + child.contentsLength;
        }

        if (debug > 10) {
            System.out.println("\tAsnSequence.write(): end");
        }
    }

    /**
     * recursively look for a pduSequence object.
     */
    AsnObject findPdu() {
        AsnObject res = null;

        Enumeration childList = children.elements();
        while (childList.hasMoreElements()) {
            AsnObject child = ((AsnObject) childList.nextElement());
            res = child.findPdu();
            if (res != null) {
                break;
            }
        }

        if (this.isCorrect == false && res != null) {
            res.isCorrect = false;
        }
        return res;
    }

    /**
     * recursively look for a trapPduv1Sequence object.
     */
    AsnObject findTrapPduv1() {
        AsnObject res = null;

        Enumeration childList = children.elements();
        while (childList.hasMoreElements()) {
            AsnObject child = ((AsnObject) childList.nextElement());
            res = child.findTrapPduv1();
            if (res != null) {
                break;
            }
        }
        if (this.isCorrect == false && res != null) {
            res.isCorrect = false;
        }
        return res;
    }

    AsnObject getObj(int offset) {
        AsnObject res = new AsnNull();
        try {
            res = (AsnObject) children.elementAt(offset);
        } catch (ArrayIndexOutOfBoundsException exc) {
        }
        return res;
    }

    int getObjCount() {
        return children.size();
    }

}
