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

class OfflineSnmpClientTest {

	private OfflineSnmpClient client;

	@BeforeEach
	void setUp() throws IOException {
		final Path tempDir = Files.createTempDirectory("snmpWalkDir");

		// -------- File 1: out.walk (original structure) --------
		final Path file1 = tempDir.resolve("out.walk");
		final String dump1 =
				"1.3.6.1.2.1.2.2.1.1.1\tASN_INTEGER\t1\n" +
						"1.3.6.1.2.1.2.2.1.1.6\tASN_INTEGER\t6\n" +
						"1.3.6.1.2.1.2.2.1.2.1\tASN_OCTET_STR\tlo\n" +
						"1.3.6.1.2.1.2.2.1.2.6\tASN_OCTET_STR\tbond0\n";
		Files.write(file1, dump1.getBytes(StandardCharsets.UTF_8));

		// -------- File 2: extra.walk (table layout) --------
		final Path file2 = tempDir.resolve("extra.walk");
		final String dump2 =
				// row 1
				"1.3.6.1.4.1.1.1.1.1\tASN_OCTET_STR\tmyDevice\n" +
						"1.3.6.1.4.1.1.1.2.1\tASN_INTEGER\t7\n" +
						// row 2
						"1.3.6.1.4.1.1.1.1.2\tASN_OCTET_STR\teth0\n" +
						"1.3.6.1.4.1.1.1.2.2\tASN_INTEGER\t99\n";
		Files.write(file2, dump2.getBytes(StandardCharsets.UTF_8));

		// -------- File 3: another.walk (table layout) --------
		final Path file3 = tempDir.resolve("another.walk");
		final String dump3 =
				// row 1
				"1.3.6.1.4.1.1.2.1.1\tASN_INTEGER\t42\n" +
						"1.3.6.1.4.1.1.2.2.1\tASN_OCTET_STR\tanswer\n" +
						// row 2
						"1.3.6.1.4.1.1.2.1.2\tASN_INTEGER\t123\n" +
						"1.3.6.1.4.1.1.2.2.2\tASN_OCTET_STR\tsecond\n";
		Files.write(file3, dump3.getBytes(StandardCharsets.UTF_8));

		client = new OfflineSnmpClient(tempDir);

		file1.toFile().deleteOnExit();
		file2.toFile().deleteOnExit();
		file3.toFile().deleteOnExit();
		tempDir.toFile().deleteOnExit();
	}

	// --- Tests for File 1 (out.walk) ---
	@Test
	void testGetExistingOid() throws Exception {
		assertEquals("lo", client.get("1.3.6.1.2.1.2.2.1.2.1"));
	}

	@Test
	void testGetNonExistingOid() {
		final Exception exception = assertThrows(Exception.class,
				() -> client.get("1.3.6.1.999.1"));
		assertEquals("(no-such-oid)", exception.getMessage());
	}

	@Test
	void testGetNextFromFirstFile() {
		final String next = client.getNext("1.3.6.1.2.1.2.2.1.1.1");
		assertTrue(next.startsWith("1.3.6.1.2.1.2.2.1.1.6"));
		assertTrue(next.contains("ASN_INTEGER"));
		assertTrue(next.endsWith("6"));
	}

	@Test
	void testWalkFromFirstFile() {
		final String walk = client.walk("1.3.6.1.2.1.2.2.1.1");
		assertTrue(walk.contains("1.3.6.1.2.1.2.2.1.1.1\tASN_INTEGER\t1"));
		assertTrue(walk.contains("1.3.6.1.2.1.2.2.1.1.6\tASN_INTEGER\t6"));
		assertFalse(walk.contains("bond0")); // bond0 is under .2, not .1
	}

	@Test
	void testTableFromFirstFile() {
		final List<List<String>> rows = client.table("1.3.6.1.2.1.2.2.1", new String[]{"1", "2", "ID"});
		assertEquals(2, rows.size());

		final List<String> row1 = rows.get(0);
		assertEquals("1", row1.get(2));
		assertEquals("1", row1.get(0));
		assertEquals("lo", row1.get(1));

		final List<String> row2 = rows.get(1);
		assertEquals("6", row2.get(2));
		assertEquals("6", row2.get(0));
		assertEquals("bond0", row2.get(1));
	}

	// --- Tests for File 2 (extra.walk) ---
	@Test
	void testGetFromSecondFile() throws Exception {
		assertEquals("myDevice", client.get("1.3.6.1.4.1.1.1.1.1"));
		assertEquals("7", client.get("1.3.6.1.4.1.1.1.2.1"));
		assertEquals("eth0", client.get("1.3.6.1.4.1.1.1.1.2"));
		assertEquals("99", client.get("1.3.6.1.4.1.1.1.2.2"));
	}

	@Test
	void testGetNextFromSecondFile() {
		final String next = client.getNext("1.3.6.1.4.1.1.1.1.1"); // myDevice
		assertTrue(next.endsWith("eth0")); // because lex order goes to 1.1.2
	}

	@Test
	void testWalkFromSecondFile() {
		final String walk = client.walk("1.3.6.1.4.1.1.1");
		assertTrue(walk.contains("1.3.6.1.4.1.1.1.1.1\tASN_OCTET_STR\tmyDevice"));
		assertTrue(walk.contains("1.3.6.1.4.1.1.1.2.1\tASN_INTEGER\t7"));
		assertTrue(walk.contains("1.3.6.1.4.1.1.1.1.2\tASN_OCTET_STR\teth0"));
		assertTrue(walk.contains("1.3.6.1.4.1.1.1.2.2\tASN_INTEGER\t99"));
	}

	@Test
	void testTableFromSecondFile() {
		final List<List<String>> rows = client.table("1.3.6.1.4.1.1.1", new String[]{"1", "2", "ID"});
		assertEquals(2, rows.size());

		final List<String> row1 = rows.get(0);
		assertEquals("myDevice", row1.get(0));
		assertEquals("7", row1.get(1));
		assertEquals("1", row1.get(2));

		final List<String> row2 = rows.get(1);
		assertEquals("eth0", row2.get(0));
		assertEquals("99", row2.get(1));
		assertEquals("2", row2.get(2));
	}

	// --- Tests for File 3 (another.walk) ---
	@Test
	void testGetFromThirdFile() throws Exception {
		assertEquals("42", client.get("1.3.6.1.4.1.1.2.1.1"));
		assertEquals("answer", client.get("1.3.6.1.4.1.1.2.2.1"));
		assertEquals("123", client.get("1.3.6.1.4.1.1.2.1.2"));
		assertEquals("second", client.get("1.3.6.1.4.1.1.2.2.2"));
	}

	@Test
	void testWalkFromThirdFile() {
		final String walk = client.walk("1.3.6.1.4.1.1.2");
		assertTrue(walk.contains("1.3.6.1.4.1.1.2.1.1\tASN_INTEGER\t42"));
		assertTrue(walk.contains("1.3.6.1.4.1.1.2.2.1\tASN_OCTET_STR\tanswer"));
		assertTrue(walk.contains("1.3.6.1.4.1.1.2.1.2\tASN_INTEGER\t123"));
		assertTrue(walk.contains("1.3.6.1.4.1.1.2.2.2\tASN_OCTET_STR\tsecond"));
	}

	@Test
	void testGetNextFromThirdFile() {
		final String next = client.getNext("1.3.6.1.4.1.1.2.1.1"); // 42
		assertTrue(next.endsWith("123")); // because lex order goes to 1.2
	}

	@Test
	void testTableFromThirdFile() {
		final List<List<String>> rows = client.table("1.3.6.1.4.1.1.2", new String[]{"1", "2", "ID"});
		assertEquals(2, rows.size());

		final List<String> row1 = rows.get(0);
		assertEquals("42", row1.get(0));
		assertEquals("answer", row1.get(1));
		assertEquals("1", row1.get(2));

		final List<String> row2 = rows.get(1);
		assertEquals("123", row2.get(0));
		assertEquals("second", row2.get(1));
		assertEquals("2", row2.get(2));
	}
}
