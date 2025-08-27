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
 * {@link OfflineSnmpFileClient} (file‑backed) can implement.
 * <p>
 * All operations return plain {@link String}s – no {@code Optional}s – so that
 * the API mirrors {@code SnmpClient}'s existing contract and remains
 * straightforward for scripting / CLI use‑cases.
 * <p>
 * <strong>Return formats</strong>
 * <ul>
 *   <li><b>get</b> – the value of the OID, as produced by the underlying client.</li>
 *   <li><b>getNext</b> – a TAB‑separated triple “OID&nbsp;TYPE&nbsp;VALUE”
 *       (same convention as {@code SnmpClient#getNext}).</li>
 *   <li><b>walk</b> – a multi‑line string, one “OID&nbsp;TYPE&nbsp;VALUE” per line.</li>
 *   <li><b>table</b> – a multi‑line string; each row is the selected columns
 *       joined by semicolons.</li>
 * </ul>
 */
public interface ISnmpClient {

	/** SNMP <em>GET</em>. */
	String get(String oid) throws Exception;

	/** SNMP <em>GETNEXT</em>. */
	String getNext(String oid) throws Exception;


	/**
	 * Reads an SNMP table.
	 *
	 * @param rootOID       root OID of the table (e.g. …7.1)
	 * @param selectColumns numeric column indexes or "ID" for the row index
	 * @return rows serialized with semicolons (one row per line)
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

	String walk(String oid) throws Exception;
}
