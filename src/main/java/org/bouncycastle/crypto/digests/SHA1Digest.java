package org.bouncycastle.crypto.digests;

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
 * implementation of SHA-1 as outlined in "Handbook of Applied Cryptography", pages 346 - 349.
 *
 * It is interesting to ponder why the, apart from the extra IV, the other difference here from MD5
 * is the "endienness" of the word processing!
 */
public class SHA1Digest
        extends GeneralDigest {
    private static final int DIGEST_LENGTH = 20;

    private int H1, H2, H3, H4, H5;

    private int[] X = new int[80];
    private int xOff;

    /**
     * Standard constructor
     */
    public SHA1Digest() {
        reset();
    }

    /**
     * Copy constructor. This will copy the state of the provided
     * message digest.
     */
    public SHA1Digest(SHA1Digest t) {
        super(t);

        H1 = t.H1;
        H2 = t.H2;
        H3 = t.H3;
        H4 = t.H4;
        H5 = t.H5;

        System.arraycopy(t.X, 0, X, 0, t.X.length);
        xOff = t.xOff;
    }

    public String getAlgorithmName() {
        return "SHA-1";
    }

    public int getDigestSize() {
        return DIGEST_LENGTH;
    }

    protected void processWord(
            byte[] in,
            int inOff) {
        X[xOff++] = ((in[inOff] & 0xff) << 24) | ((in[inOff + 1] & 0xff) << 16)
                | ((in[inOff + 2] & 0xff) << 8) | ((in[inOff + 3] & 0xff));

        if (xOff == 16) {
            processBlock();
        }
    }

    private void unpackWord(
            int word,
            byte[] out,
            int outOff) {
        out[outOff] = (byte) (word >>> 24);
        out[outOff + 1] = (byte) (word >>> 16);
        out[outOff + 2] = (byte) (word >>> 8);
        out[outOff + 3] = (byte) word;
    }

    protected void processLength(
            long bitLength) {
        if (xOff > 14) {
            processBlock();
        }

        X[14] = (int) (bitLength >>> 32);
        X[15] = (int) (bitLength & 0xffffffff);
    }

    public int doFinal(
            byte[] out,
            int outOff) {
        finish();

        unpackWord(H1, out, outOff);
        unpackWord(H2, out, outOff + 4);
        unpackWord(H3, out, outOff + 8);
        unpackWord(H4, out, outOff + 12);
        unpackWord(H5, out, outOff + 16);

        reset();

        return DIGEST_LENGTH;
    }

    /**
     * reset the chaining variables
     */
    public void reset() {
        super.reset();

        H1 = 0x67452301;
        H2 = 0xefcdab89;
        H3 = 0x98badcfe;
        H4 = 0x10325476;
        H5 = 0xc3d2e1f0;

        xOff = 0;
        for (int i = 0; i != X.length; i++) {
            X[i] = 0;
        }
    }

    //
    // Additive constants
    //
    private static final int Y1 = 0x5a827999;
    private static final int Y2 = 0x6ed9eba1;
    private static final int Y3 = 0x8f1bbcdc;
    private static final int Y4 = 0xca62c1d6;

    private int f(
            int u,
            int v,
            int w) {
        return ((u & v) | ((~u) & w));
    }

    private int h(
            int u,
            int v,
            int w) {
        return (u ^ v ^ w);
    }

    private int g(
            int u,
            int v,
            int w) {
        return ((u & v) | (u & w) | (v & w));
    }

    private int rotateLeft(
            int x,
            int n) {
        return (x << n) | (x >>> (32 - n));
    }

    protected void processBlock() {
        //
        // expand 16 word block into 80 word block.
        //
        for (int i = 16; i <= 79; i++) {
            X[i] = rotateLeft((X[i - 3] ^ X[i - 8] ^ X[i - 14] ^ X[i - 16]), 1);
        }

        //
        // set up working variables.
        //
        int A = H1;
        int B = H2;
        int C = H3;
        int D = H4;
        int E = H5;

        //
        // round 1
        //
        for (int j = 0; j <= 19; j++) {
            int t = rotateLeft(A, 5) + f(B, C, D) + E + X[j] + Y1;

            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = t;
        }

        //
        // round 2
        //
        for (int j = 20; j <= 39; j++) {
            int t = rotateLeft(A, 5) + h(B, C, D) + E + X[j] + Y2;

            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = t;
        }

        //
        // round 3
        //
        for (int j = 40; j <= 59; j++) {
            int t = rotateLeft(A, 5) + g(B, C, D) + E + X[j] + Y3;

            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = t;
        }

        //
        // round 4
        //
        for (int j = 60; j <= 79; j++) {
            int t = rotateLeft(A, 5) + h(B, C, D) + E + X[j] + Y4;

            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = t;
        }

        H1 += A;
        H2 += B;
        H3 += C;
        H4 += D;
        H5 += E;

        //
        // reset the offset and clean out the word buffer.
        //
        xOff = 0;
        for (int i = 0; i != X.length; i++) {
            X[i] = 0;
        }
    }
}
