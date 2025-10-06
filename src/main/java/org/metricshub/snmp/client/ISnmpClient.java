/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * SNMP Java Client
 * ჻჻჻჻჻჻
 * Copyright 2023 MetricsHub
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

package org.metricshub.snmp.client;

import java.util.List;

/**
 * Unified abstraction that both {@link SnmpClient} (live network) and
 * {@link OfflineSnmpClient} (file‑backed) can implement.
 */
public interface ISnmpClient {

	/**
	 * Performs SNMP get action for a single OID.
	 * @param oid A given OID.
	 * @return The corresponding object as a string.
	 * @throws Exception on error (e.g. no such OID).
	 */
	String get(String oid) throws Exception;

	/**
	 * Performs SNMP getNext action for	 a single OID.
	 * @param oid A given OID.
	 * @return The corresponding object as a string.
	 * @throws Exception on error (e.g. no such OID).
	 */
	String getNext(String oid) throws Exception;


	/**
	 * Reads an SNMP table.
	 *
	 * @param rootOID       Root OID of the table (e.g. …7.1)
	 * @param selectColumns Numeric column indexes or "ID" for the row index
	 * @return Rows serialized with semicolons (one row per line)
	 */
	List<List<String>> table(String rootOID, String[] selectColumns) throws Exception;

	/**
	 * Strips the leading dot from an OID string if it exists.
	 *
	 * @param s the OID string to process.
	 * @return the OID string without a leading dot.
	 */
	default String stripDot(String s) {
		return s.startsWith(".") ? s.substring(1) : s;
	}

	/**
	 * Performs SNMP walk action starting from a given OID.
	 * @param oid A given OID.
	 * @return The corresponding object as a string.
	 * @throws Exception on error (e.g. no such OID).
	 */
	String walk(String oid) throws Exception;

	/**
	 * Frees any resources held by the client, such as network connections or file handles or memory space.
	 */
	void freeResources();
}
