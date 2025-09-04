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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * An offline SNMP client that reads OID values from a snmp WALK dump file and executes
 * SNMP operations (GET, GET_NEXT, TABLE, WALK) against those OIDs.
 */
public final class OfflineSnmpFileClient implements ISnmpClient {

	/** Container for Snmp object's type and value. */
	private static final class SnmpValue {
		final String type;
		final String value;
		SnmpValue(String type, String value) {
			this.type = type;
			this.value = value;
		}
	}

	// Maps the OIDs to their corresponding SnmpValue (type + value).
	private final NavigableMap<String, SnmpValue> oidValues = new TreeMap<>();

	/**
	 * Constructs an SNMP client that reads OID values from a file.
	 *
	 * @param file the path to the file containing OID values in UTF-16 encoding.
	 * @throws IOException if an I/O error occurs while reading the file.
	 */
	public OfflineSnmpFileClient(Path file) throws IOException {
		try (final BufferedReader br =
					 Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.contains("\t")) continue;
				final String[] parts = line.split("\\t", 3);
				if (parts.length < 3) {
					continue;
				}
				final String oid   = stripDot(parts[0]);
				final String type  = parts[1];
				final String value = parts[2];
				oidValues.put(oid, new SnmpValue(type, value));
			}
		}
	}

	@Override
	public String get(String oid) throws Exception {
		final String key = stripDot(oid);
		final SnmpValue val = oidValues.get(key);
		if (val == null) {
			throw new Exception("(no-such-oid)");
		}
		return val.value;
	}

	@Override
	public String getNext(String oid) {
		final Map.Entry<String, SnmpValue> next = oidValues.higherEntry(stripDot(oid));
		if (next == null) {
			return "(end-of-mib-view)";
		}
		final String nextOid = next.getKey();
		final SnmpValue snmpVal = next.getValue();
		return nextOid + "\t" + snmpVal.type + "\t" + snmpVal.value;
	}

	@Override
	public List<List<String>> table(String rootOID, String[] selectColumns) {
		if (rootOID == null || rootOID.length() < 3) {
			throw new IllegalArgumentException("Invalid SNMP Table OID: " + rootOID);
		}
		if (selectColumns == null || selectColumns.length < 1) {
			throw new IllegalArgumentException("Invalid SNMP Table columns");
		}

		final String base = stripDot(rootOID);

		// find first column under base
		final Map.Entry<String, SnmpValue> firstEntry = oidValues.higherEntry(base);
		if (firstEntry == null || !firstEntry.getKey().startsWith(base + ".")) {
			return new ArrayList<>();  // empty table
		}

		final String firstKey = firstEntry.getKey(); // e.g. base + ".1.<idx>"
		final int dotAfter = firstKey.indexOf('.', base.length() + 1);
		if (dotAfter < 0) return new ArrayList<>();

		final String firstColOid = firstKey.substring(0, dotAfter); // e.g. "…7.1"
		final int colOidLen = firstColOid.length();

		// collect row IDs
		final List<String> ids = new ArrayList<>();
		String cursor = firstColOid;
		while (true) {
			final Map.Entry<String, SnmpValue> next = oidValues.higherEntry(cursor);
			if (next == null) break;
			final String key = next.getKey();
			if (!key.startsWith(firstColOid + ".")) {
				break;
			}
			final String id = key.substring(colOidLen + 1);
			ids.add(id);
			cursor = key;
			if (ids.size() > 10000) {
				break;
			}
		}

		// build result rows
		final List<List<String>> rows = new ArrayList<>(ids.size());
		for (String id : ids) {
			final List<String> row = new ArrayList<>(selectColumns.length);
			for (String col : selectColumns) {
				if ("ID".equals(col)) {
					row.add(id);
				} else {
					String lookupOid = base + "." + col + "." + id;
					try {
						row.add(get(lookupOid));
					} catch (Exception e) {
						row.add("");
					}
				}
			}
			rows.add(row);
		}
		return rows;
	}

	@Override
	public String walk(String oid) {
		final String base = stripDot(oid);
		final StringBuilder walkResult = new StringBuilder();

		// Find the first OID that is >= base
		Map.Entry<String, SnmpValue> entry = oidValues.ceilingEntry(base);
		if (entry == null) {
			return ""; // Nothing at or after base
		}

		while (entry != null) {
			final String nextOid = entry.getKey();
			if (!nextOid.startsWith(base + ".") && !nextOid.equals(base)) {
				break; // left the subtree
			}

			final SnmpValue snmpValue = entry.getValue();
			walkResult.append(nextOid)
					.append("\t")
					.append(snmpValue.type)
					.append("\t")
					.append(snmpValue.value)
					.append("\n");

			entry = oidValues.higherEntry(nextOid);
		}

		return walkResult.toString();
	}
}