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

import java.security.SecureRandom;

/**
 * The base class for parameters to key generators.
 */
public class KeyGenerationParameters {
    private SecureRandom random;
    private int strength;

    /**
     * initialise the generator with a source of randomness
     * and a strength (in bits).
     *
     * @param random   the random byte source.
     * @param strength the size, in bits, of the keys we want to produce.
     */
    public KeyGenerationParameters(
            SecureRandom random,
            int strength) {
        this.random = random;
        this.strength = strength;
    }

    /**
     * return the random source associated with this
     * generator.
     *
     * @return the generators random source.
     */
    public SecureRandom getRandom() {
        return random;
    }

    /**
     * return the bit strength for keys produced by this generator,
     *
     * @return the strength of the keys this generator produces (in bits).
     */
    public int getStrength() {
        return strength;
    }
}
