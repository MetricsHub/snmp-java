# SNMP Java Client

The SNMP Java client enables you to run SNMP operations, including:

- SNMP Client initialization
- Execution of single requests (`Get` and `GetNext`), as well as multiple request functionalities (`Walk` and `Table`)
- Request execution on remote devices, supporting SNMP v1, v2c, or v3 implementations

# How to run the SNMP Client inside Java

Add SNMP in the list of dependencies in your [Maven **pom.xml**](https://maven.apache.org/pom.html):

```
<dependencies>
	<!-- [...] -->
	<dependency>
		<groupId>${project.groupId}</groupId>
		<artifactId>${project.artifactId}</artifactId>
		<version>${project.version}</version>
	</dependency>
</dependencies>
```

Invoke the SNMP Client:

```
	public static void main(String[] args) throws Exception {
		final String hostname = "my-hostname";
		final int port = 161;
		final int version = SnmpClient.SNMP_V2C;
		final int[] retryIntervals = { 500, 1000, 2000 };
		final String community = "my-community";

		// SNMP v3 settings
		final String authType = null; // E.g. SnmpClient.SNMP_AUTH_MD5;
		final String authUsername = null; // E.g. "my-username";
		final String authPassword = null; // E.g. "my-auth-password";
		final String privacyType = null; // E.g. SnmpClient.SNMP_PRIVACY_AES;
		final String privacyPassword = null; // E.g. "my-privacy-password";
		final String contextName = null; // E.g. "my-context-name";
		final byte[] contextID = {};

		// Initialize the SNMP Client
		final SnmpClient snmpClient = new SnmpClient(
			hostname,
			port,
			version,
			retryIntervals,
			community,
			authType,
			authUsername,
			authPassword,
			privacyType,
			privacyPassword,
			contextName,
			contextID
		);

		// MIB 2 DOD OID
		final String oid = "1.3.6";

		// Perform a GetNext operation on the specified MIB 2 OID
		final String result = snmpClient.getNext(oid);

		System.out.println("SNMP GetNext Result: " + result);
	}
```
