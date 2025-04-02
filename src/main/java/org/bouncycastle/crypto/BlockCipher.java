package org.bouncycastle.crypto;

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
 * Block cipher engines are expected to conform to this interface.
 */
public interface BlockCipher {
    /**
     * Initialise the cipher.
     *
     * @param forEncryption if true the cipher is initialised for
     *                      encryption, if false for decryption.
     * @param params        the key and other data required by the cipher.
     * @exception IllegalArgumentException if the params argument is
     *                                     inappropriate.
     */
    public void init(boolean forEncryption, CipherParameters params)
            throws IllegalArgumentException;

    /**
     * Return the name of the algorithm the cipher implements.
     *
     * @return the name of the algorithm the cipher implements.
     */
    public String getAlgorithmName();

    /**
     * Return the block size for this cipher (in bytes).
     *
     * @return the block size for this cipher in bytes.
     */
    public int getBlockSize();

    /**
     * Process one block of input from the array in and write it to
     * the out array.
     *
     * @param in     the array containing the input data.
     * @param inOff  offset into the in array the data starts at.
     * @param out    the array the output data will be copied into.
     * @param outOff the offset into the out array the output will start at.
     * @exception DataLengthException   if there isn't enough data in in, or
     *                                  space in out.
     * @exception IllegalStateException if the cipher isn't initialised.
     * @return the number of bytes processed and produced.
     */
    public int processBlock(byte[] in, int inOff, byte[] out, int outOff)
            throws DataLengthException, IllegalStateException;

    /**
     * Reset the cipher. After resetting the cipher is in the same state
     * as it was after the last init (if there was one).
     */
    public void reset();
}
