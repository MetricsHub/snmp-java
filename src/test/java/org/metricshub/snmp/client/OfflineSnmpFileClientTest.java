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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfflineSnmpFileClientTest {

	private OfflineSnmpFileClient client;

	@BeforeEach
	void setUp() throws IOException {
		// Create a temporary snmpWalk dump file
		final Path tempFile = Files.createTempFile("snmpWalk", ".txt");
		final String dump =
				"1.3.6.1.2.1.2.2.1.1.1\tASN_INTEGER\t1\n" +
						"1.3.6.1.2.1.2.2.1.1.6\tASN_INTEGER\t6\n" +
						"1.3.6.1.2.1.2.2.1.2.1\tASN_OCTET_STR\tlo\n" +
						"1.3.6.1.2.1.2.2.1.2.6\tASN_OCTET_STR\tbond0\n";
		Files.write(tempFile, dump.getBytes(StandardCharsets.UTF_8));


		client = new OfflineSnmpFileClient(tempFile);

		// cleanup on exit
		tempFile.toFile().deleteOnExit();
	}

	@Test
	void testGetExistingOid() throws Exception {
		final String value = client.get("1.3.6.1.2.1.2.2.1.2.1");
		assertEquals("lo", value);
	}

	@Test
	void testGetNonExistingOid() {
		final Exception exception = assertThrows(Exception.class,
				() -> client.get("1.3.6.1.999.1"));
		assertEquals("(no-such-oid)", exception.getMessage());
	}

	@Test
	void testGetNext() {
		final String next = client.getNext("1.3.6.1.2.1.2.2.1.1.1");
		assertTrue(next.startsWith("1.3.6.1.2.1.2.2.1.1.6"));
		assertTrue(next.contains("ASN_INTEGER"));
		assertTrue(next.endsWith("6"));
	}

	@Test
	void testWalk() {
		final String walk = client.walk("1.3.6.1.2.1.2.2.1.1");
		assertTrue(walk.contains("1.3.6.1.2.1.2.2.1.1.1\tASN_INTEGER\t1"));
		assertTrue(walk.contains("1.3.6.1.2.1.2.2.1.1.6\tASN_INTEGER\t6"));
		assertFalse(walk.contains("bond0")); // bond0 is under .2, not .1
	}

	@Test
	void testTable() {
		// The OIDs have structure like ...2.2.1.<col>.<row>
		final List<List<String>> rows = client.table("1.3.6.1.2.1.2.2.1", new String[]{"1", "2", "ID"});
		assertEquals(2, rows.size());

		// First row: ID=1
		final List<String> row1 = rows.get(0);
		assertEquals("1", row1.get(2)); // ID
		assertEquals("1", row1.get(0)); // column 1 value
		assertEquals("lo", row1.get(1)); // column 2 value

		// Second row: ID=6
		final List<String> row2 = rows.get(1);
		assertEquals("6", row2.get(2)); // ID
		assertEquals("6", row2.get(0));
		assertEquals("bond0", row2.get(1));
	}
}
