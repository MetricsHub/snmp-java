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

import uk.co.westhawk.snmp.pdu.BlockPdu;
import uk.co.westhawk.snmp.stack.AsnObject;
import uk.co.westhawk.snmp.stack.AsnObjectId;
import uk.co.westhawk.snmp.stack.AsnOctets;
import uk.co.westhawk.snmp.stack.PduException;
import uk.co.westhawk.snmp.stack.SnmpConstants;
import uk.co.westhawk.snmp.stack.SnmpContext;
import uk.co.westhawk.snmp.stack.SnmpContextv2c;
import uk.co.westhawk.snmp.stack.SnmpContextv3;
import uk.co.westhawk.snmp.stack.SnmpContextv3Face;
import uk.co.westhawk.snmp.stack.varbind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnmpClient {

	// Default SNMP port number
	public static final int SNMP_PORT = 161;

	// SNMP v1, v2c, v3
	public static final int SNMP_V1 = 1;
	public static final int SNMP_V2C = 2;
	public static final int SNMP_V3 = 3;
	public static final String SNMP_AUTH_MD5 = "MD5";
	public static final String SNMP_AUTH_SHA = "SHA";
	public static final String SNMP_AUTH_SHA256 = "SHA256";
	public static final String SNMP_AUTH_SHA512 = "SHA512";
	public static final String SNMP_AUTH_SHA224 = "SHA224";
	public static final String SNMP_AUTH_SHA384 = "SHA384";
	public static final String SNMP_PRIVACY_DES = "DES";
	public static final String SNMP_PRIVACY_AES = "AES";
	public static final String SNMP_NONE = "None";

	private SnmpContext contextv1 = null;
	private SnmpContextv2c contextv2c = null;
	private SnmpContextv3 contextv3 = null;
	private BlockPdu pdu;
	private String host;
	private int port;
	private String community;
	private int snmpVersion;
	private String authUsername;
	private String authType;
	private String authPassword;
	private String privacyType;
	private String privacyPassword;
	private int[] retryIntervals;
	private String contextName;
	private byte[] contextEngineID;
	public static final String SOCKET_TYPE = "Standard";

	/**
	 * Creates an SNMPClient instance, which connects to the specified SNMP agent
	 * with the specified credentials
	 * (depending on the version of SNMP)
	 * 
	 * @param host            The hostname/IP address of the SNMP agent we're
	 *                        querying
	 * @param port            The port of the SNMP agent (should be 161)
	 * @param version         The version of SNMP to use (1, 2 or 3)
	 * @param retryIntervals  Timeout in milliseconds after which the elementary
	 *                        operations will be retried
	 * @param community       <i>(SNMP v1 and v2 only)</i> The SNMP community
	 * @param authType        <i>(SNMP v3 only)</i> The authentication method:
	 *                        "MD5", "SHA" or ""
	 * @param authUsername    <i>(SNMP v3 only)</i> The username
	 * @param authPassword    <i>(SNMP v3 only)</i> The password (in clear)
	 * @param privacyType     <i>(SNMP v3 only)</i> The encryption type: "DES",
	 *                        "AES" or ""
	 * @param privacyPassword <i>(SNMP v3 only)</i> The encryption password
	 * @param contextName     <i>(SNMP v3 only)</i> The context name
	 * @param contextID       <i>(SNMP v3 only)</i> The context ID (??)
	 * @throws IllegalArgumentException when specified authType, privType are
	 *                                  invalid
	 * @throws IllegalStateException    when the specified properties lead to
	 *                                  something that cannot work (i.e. privacy
	 *                                  without authentication)
	 * @throws IOException              when cannot initialize the SNMP context
	 */
	public SnmpClient(String host, int port, int version, int[] retryIntervals,
			String community,
			String authType, String authUsername, String authPassword,
			String privacyType, String privacyPassword,
			String contextName, byte[] contextID) throws IOException {
		// First, validate the inputs
		validate(version, authType, privacyType);

		// Sets the attributes of the class instance
		this.host = host;
		this.port = port;
		this.snmpVersion = version;
		this.retryIntervals = retryIntervals;
		this.community = community;
		this.authType = authType;
		this.authUsername = authUsername;
		this.authPassword = authPassword;
		this.privacyType = privacyType;
		this.privacyPassword = privacyPassword;
		this.contextName = contextName;
		this.contextEngineID = contextID;

		// Properly create the SNMP context, based on these properties
		initialize();
	}

	/**
	 * Validate the specified inputs. Throws an IllegalArgumentException if needed.
	 * 
	 * @param version     Specified version of the SNMP protocol
	 * @param authType    Specified authType
	 * @param privacyType Specified privacyType
	 * @throws IllegalArgumentException when invalid inputs are specified
	 */
	private void validate(int version, String authType, String privacyType) {

		// In case of SNMP v3, check the authType and privType (if not empty)
		if (version == SNMP_V3) {
			if (authType != null) {
				if (!authType.isEmpty()) {
					if (!authType.equals(SNMP_AUTH_MD5) && !authType.equals(SNMP_AUTH_SHA)
							&& !authType.equals(SNMP_AUTH_SHA256) && !authType.equals(SNMP_AUTH_SHA512)
							&& !authType.equals(SNMP_AUTH_SHA224) && !authType.equals(SNMP_AUTH_SHA384)) {
						throw new IllegalArgumentException("Invalid authentication method '" + authType +
								"' (must be either '" + SNMP_AUTH_MD5
								+ "' or '" + SNMP_AUTH_SHA
								+ "' or '" + SNMP_AUTH_SHA256
								+ "' or '" + SNMP_AUTH_SHA512
								+ "' or '" + SNMP_AUTH_SHA224
								+ "' or '" + SNMP_AUTH_SHA384
								+ "' or empty)");
					}
				}
			}

			if (privacyType != null) {
				if (!privacyType.isEmpty()) {
					if (!privacyType.equals(SNMP_PRIVACY_DES) && !privacyType.equals(SNMP_PRIVACY_AES)) {
						throw new IllegalArgumentException(
								"Invalid privacy method '" + privacyType + "' (must be either '" + SNMP_PRIVACY_DES
										+ "' or '" + SNMP_PRIVACY_AES + "' or empty)");
					}
				}
			}
		}
	}

	/**
	 * Initialize the SNMPClient
	 * <p>
	 * Creates the context to connect to the SNMP agent. Required before any actual
	 * operation is performed.
	 * 
	 * @throws IOException           when cannot create the SNMP context
	 * @throws IllegalStateException when there is an inconsistency in the
	 *                               properties that prevent us from moving forward
	 */
	private void initialize() throws IOException {

		// SNMP v2c
		if (snmpVersion == SNMP_V2C) {
			contextv2c = new SnmpContextv2c(host, port, null, SOCKET_TYPE);
			contextv2c.setCommunity(community);
		}

		// SNMP v3
		else if (snmpVersion == SNMP_V3) {
			int authProtocolCode = 0;
			int privacyProtocolCode = 0;
			boolean authenticate = false;
			boolean privacy = false;

			// Some sanity check with the "context"
			if (contextEngineID == null) {
				contextEngineID = new byte[0];
			}
			if (contextName == null) {
				contextName = "";
			}

			// Verify the username
			if (authUsername == null) {
				authUsername = "";
			}

			// Verify and translate the authentication type
			if (authType == null || authUsername == null || authPassword == null) {
				authenticate = false;
				authProtocolCode = SnmpContextv3Face.NO_AUTH_PROTOCOL;
				authPassword = "";
			} else if (authType.isEmpty() || authUsername.isEmpty() || authPassword.isEmpty()) {
				authenticate = false;
				authProtocolCode = SnmpContextv3Face.NO_AUTH_PROTOCOL;
				authPassword = "";
			} else if (authType.equals(SNMP_AUTH_MD5)) {
				authenticate = true;
				authProtocolCode = SnmpContextv3Face.MD5_PROTOCOL;
			} else if (authType.equals(SNMP_AUTH_SHA)) {
				authenticate = true;
				authProtocolCode = SnmpContextv3Face.SHA1_PROTOCOL;
			} else if (authType.equals(SNMP_AUTH_SHA256)) {
				authenticate = true;
				authProtocolCode = SnmpContextv3Face.SHA256_PROTOCOL;
			} else if (authType.equals(SNMP_AUTH_SHA512)) {
				authenticate = true;
				authProtocolCode = SnmpContextv3Face.SHA512_PROTOCOL;
			} else if (authType.equals(SNMP_AUTH_SHA224)) {
				authenticate = true;
				authProtocolCode = SnmpContextv3Face.SHA224_PROTOCOL;

			} else if (authType.equals(SNMP_AUTH_SHA384)) {
				authenticate = true;
				authProtocolCode = SnmpContextv3Face.SHA384_PROTOCOL;
			}

			// Verify the privacy thing
			if (privacyType == null || privacyPassword == null) {
				privacy = false;
			} else if (privacyType.isEmpty() || privacyPassword.isEmpty()) {
				privacy = false;
			} else if (privacyType.equals(SNMP_PRIVACY_DES)) {
				privacy = true;
				privacyProtocolCode = SnmpContextv3Face.DES_ENCRYPT;
			} else if (privacyType.equals(SNMP_PRIVACY_AES)) {
				privacy = true;
				privacyProtocolCode = SnmpContextv3Face.AES_ENCRYPT;
			}

			// Privacy with no authentication is impossible
			if (privacy && !authenticate) {
				throw new IllegalStateException("Authentication is required for privacy to be enforced");
			}

			// Create the context
			contextv3 = new SnmpContextv3(host, port, SOCKET_TYPE);
			contextv3.setContextEngineId(contextEngineID);
			contextv3.setContextName(contextName);
			contextv3.setUserName(authUsername);
			contextv3.setUseAuthentication(authenticate);
			if (authenticate) {
				contextv3.setUserAuthenticationPassword(authPassword);
				contextv3.setAuthenticationProtocol(authProtocolCode);
				contextv3.setUsePrivacy(privacy);
				if (privacy) {
					contextv3.setPrivacyProtocol(privacyProtocolCode);
					contextv3.setUserPrivacyPassword(privacyPassword);
				}
			}
		}

		// SNMP v1 (default)
		else {
			contextv1 = new SnmpContext(host, port, SOCKET_TYPE);
			contextv1.setCommunity(community);
		}

		// Small thing: set the prefix for hex values (default is "0x" but we're setting
		// it to empty)
		AsnOctets.setHexPrefix("");

		// AsnObject.setDebug(15);

	}

	/**
	 * Releases the resources associated to this instance
	 * (or so at least we believe...)
	 */
	public void freeResources() {
		if (contextv1 != null) {
			contextv1.destroy();
			contextv1 = null;
		}
		if (contextv2c != null) {
			contextv2c.destroy();
			contextv2c = null;
		}
		if (contextv3 != null) {
			contextv3.destroy();
			contextv3 = null;
		}

		if (pdu != null) {
			pdu = null;
		}
	}

	/**
	 * Create the PDU and sets the timeout
	 * <p>
	 * Note: This method has been created just to avoid duplicate code in the get,
	 * getNext and walk functions
	 */
	private void createPdu() {
		// Create the PDU based on the proper context
		if (snmpVersion == SNMP_V2C) {
			pdu = new BlockPdu(contextv2c);
		} else if (snmpVersion == SNMP_V3) {
			pdu = new BlockPdu(contextv3);
		} else {
			pdu = new BlockPdu(contextv1);
		}

		// Set the timeout
		if (retryIntervals != null) {
			pdu.setRetryIntervals(retryIntervals);
		}
	}

	/**
	 * Perform a GET operation on the specified OID
	 * 
	 * @param oid OID on which to perform a GET operation
	 * @return Value of the specified OID
	 * @throws Exception in case of any problem
	 */
	public String get(String oid) throws Exception {
		createPdu();
		pdu.setPduType(BlockPdu.GET);
		pdu.addOid(oid);
		return sendRequest().value;
	}

	/**
	 * Perform a GET operation on the specified OID and return the details of the
	 * result (including the type of the value)
	 * 
	 * @param oid OID on which to perform a GET operation
	 * @return A string in the form of the OID, "string" and the value, separated by
	 *         tabs (\t)
	 * @throws Exception in case of any problem
	 */
	public String getWithDetails(String oid) throws Exception {
		createPdu();
		pdu.setPduType(BlockPdu.GET);
		pdu.addOid(oid);
		SnmpResult result = sendRequest();
		return result.oid + "\t" + result.type + "\t" + result.value;
	}

	/**
	 * Perform a GETNEXT operation on the specified OID
	 * 
	 * @param oid OID on which to perform a GETNEXT operation
	 * @return A string in the form of the OID, "string" and the value, separated by
	 *         tabs (\t)
	 * @throws Exception in case of any problem
	 */
	public String getNext(String oid) throws Exception {
		createPdu();
		pdu.setPduType(BlockPdu.GETNEXT);
		pdu.addOid(oid);
		SnmpResult result = sendRequest();
		return result.oid + "\t" + result.type + "\t" + result.value;
	}

	/**
	 * Perform a WALK, i.e. a series of GETNEXT operations until we fall off the
	 * tree
	 * 
	 * @param oid Root OID of the tree
	 * @return Result of the WALK operation, as a long String. Each pair of
	 *         oid/value is separated with a linefeed (at least, for now!)
	 * @throws Exception
	 * @throws IllegalArgumentException for bad specified OIDs
	 */
	public String walk(String oid) throws Exception {

		StringBuilder walkResult = new StringBuilder();
		String currentOID;
		SnmpResult getNextResult;

		// Sanity check?
		if (oid == null) {
			throw new IllegalArgumentException("Invalid SNMP Walk OID: null");
		}
		if (oid.length() < 3) {
			throw new IllegalArgumentException("Invalid SNMP Walk OID: \"" + oid + "\"");
		}

		// Now, something special:
		// In the walk loop below, we will catch any exception and break out of the loop
		// if anything happens. At that point, we simply return what we have, i.e. just
		// as if everything was okay. Doing so, we fail to report authentication
		// problems.
		// So, the code using this will think it's just okay, even though the QA team
		// intentionally put bad credentials to verify the error message... See
		// MATSYA-464.
		//
		// So, we're going to first run a getNext() for nothing, just so that
		// this call will throw the proper exception in case of credentials problems.
		getNext(oid);

		currentOID = oid;
		do {
			createPdu();
			pdu.setPduType(BlockPdu.GETNEXT);
			pdu.addOid(currentOID);
			try {
				getNextResult = sendRequest();
			} catch (Exception e) {
				// Something wrong? Get out of the loop and return what we have
				break;
			}

			currentOID = getNextResult.oid;
			if (!currentOID.startsWith(oid)) {
				// We're off the tree, so get out of the loop
				break;
			}

			// Append the result
			walkResult.append(currentOID + "\t" + getNextResult.type + "\t" + getNextResult.value + "\n");

		} while (walkResult.length() < 10 * 1048576); // 10 MB is the limit for the result of our WALK operation. Should
														// be enough.

		// Remove the trailing \n (if any)
		int resultLength = walkResult.length();
		if (resultLength > 0) {
			return walkResult.substring(0, resultLength - 1);
		}

		// If nothing, return an empty string
		return "";
	}

	/**
	 * Read the content of an SNMP table
	 * 
	 * @param rootOID           Root OID of the SNMP table
	 * @param selectColumnArray Array of numbers specifying the column numbers of
	 *                          the array to be read. Use "ID" for the row number.
	 * @return A semicolon-separated list of values
	 * @throws IllegalArgumentException when the specified arguments are wrong
	 * @throws Exception                when the underlying SNMP API throws one
	 */
	public List<List<String>> table(String rootOID, String[] selectColumnArray) throws Exception {

		// Sanity check
		if (rootOID == null) {
			throw new IllegalArgumentException("Invalid SNMP Table OID: null");
		}
		if (rootOID.length() < 3) {
			throw new IllegalArgumentException("Invalid SNMP Table OID: \"" + rootOID + "\"");
		}
		if (selectColumnArray == null) {
			throw new IllegalArgumentException("Invalid SNMP Table column numbers: null");
		}
		if (selectColumnArray.length < 1) {
			throw new IllegalArgumentException("Invalid SNMP Table column numbers: none");
		}

		// First of all, retrieve the list of IDs in the table
		// To do so, we need to see what is the first column number available (it may
		// not be 1)
		createPdu();
		pdu.setPduType(BlockPdu.GETNEXT);
		pdu.addOid(rootOID);
		String firstValueOid = sendRequest().oid;
		if (firstValueOid.isEmpty() || !firstValueOid.startsWith(rootOID)) {
			// Empty table
			return new ArrayList<>();
		}

		int tempIndex = firstValueOid.indexOf(".", rootOID.length() + 2);
		if (tempIndex < 0) {
			// Weird case, there is no "." after the rootOID in the OID of the first value
			// we successfully got in the table
			return new ArrayList<>();
		}
		String firstColumnOid = firstValueOid.substring(0, tempIndex);
		int firstColumnOidLength = firstColumnOid.length();

		// Now, find the list of row IDs in this column. We're going to do something
		// like a walk, except we don't care about the values. Just the OIDs.
		ArrayList<String> IDArray = new ArrayList<String>(0);
		String currentOID = firstColumnOid;
		SnmpResult getNextResult;
		do {
			// Get next until we get out of the tree
			createPdu();
			pdu.setPduType(BlockPdu.GETNEXT);
			pdu.addOid(currentOID);
			getNextResult = sendRequest();

			currentOID = getNextResult.oid;

			// Outside? Exit!
			if (!currentOID.startsWith(firstColumnOid)) {
				break;
			}

			// Add the right part of the OID in the list of IDs (the part to the right of
			// the column OID)
			IDArray.add(currentOID.substring(firstColumnOidLength + 1));

		} while (IDArray.size() < 10000); // Not more than 10000 lines, please...

		// And finally, build the result table
		List<List<String>> tableResult = new ArrayList<>();
		for (String ID : IDArray) {
			// For each row...
			List<String> row = new ArrayList<>();
			for (String column : selectColumnArray) {
				// For each column...

				// If the column has to provide the ID of the row
				if (column.equals("ID")) {
					row.add(ID);
				} else {
					// Keep going, even in case of a failure
					try {
						row.add(get(rootOID + "." + column + "." + ID));
					} catch (Exception e) {
						row.add("");
					}
				}
			}
			tableResult.add(row);
		}

		// Return the result
		return tableResult;
	}

	/**
	 * Sends the SNMP request and perform some minor interpretation of the result
	 * 
	 * @return Result of the query in the form of a couple {oid;value} (SnmpResult)
	 * @throws PduException when an error happens at the SNMP layer
	 * @throws IOException  when an error occurs at the network layer
	 * @throws Exception    when we cannot get the value of the specified OID,
	 *                      because it does not exist
	 *                      <p>
	 *                      <li>In case of no such OID, an exception is thrown.
	 *                      <li>In case of empty value, the result will have the oid
	 *                      and an empty value.
	 */
	private SnmpResult sendRequest() throws PduException, IOException, Exception {

		// Declarations
		SnmpResult result = new SnmpResult();

		// Send the SNMP request
		varbind var = pdu.getResponseVariableBinding();

		// Retrieve the OID and value of the response (a varbind)
		AsnObjectId oid = var.getOid();
		AsnObject value = var.getValue();

		// No such OID? Throw an exception (this needs to be caught gracefully by other
		// functions)
		byte valueType = value.getRespType();
		if (valueType == SnmpConstants.SNMP_VAR_NOSUCHOBJECT ||
				valueType == SnmpConstants.SNMP_VAR_NOSUCHINSTANCE ||
				valueType == SnmpConstants.SNMP_VAR_ENDOFMIBVIEW) {
			throw new Exception(value.getRespTypeString());
		}

		// Empty?
		else if (valueType == SnmpConstants.ASN_NULL) {
			result.oid = oid.toString();
			result.type = "null";
		}

		// ASN_OCTET_STRING? (special case, because it may need to be displayed as an
		// hexadecimal string)
		// Normally, the API should take care of that, but this is not the case for
		// 0x00:00:00:00 values, which are displayed as empty values
		else if (valueType == SnmpConstants.ASN_OCTET_STR) {

			result.oid = oid.toString();
			result.type = "ASN_OCTET_STR";

			// Map the value to the specific AsnOctets sub-class
			AsnOctets octetStringValue = (AsnOctets) value;

			// First, convert the value as a string, using toString().
			// AsnOctets.toString() will convert the value to a normal string (with ASCII
			// chars)
			// or to the hexadecimal version of it, like 0x00:30:31:32 when the "normal"
			// string is
			// not printable
			String octetStringValuetoString = octetStringValue.toString();

			// Then forcibly convert the value to its hexadecimal representation, but
			// replace ':' with blank spaces, to match with what PATROL does
			String octetStringValuetoHex = octetStringValue.toHex().replace(':', ' ');

			// So, if the toString() and the toHex() value have the same length, it means
			// that toString() is actually returning
			// the hexadecimal representation (yeah, there is no other way to retrieve that,
			// the SNMP API does not tell us
			// whether the value is printable or not)
			// If the SNMP API judges that the value is not printable, then we will use the
			// toHex() value, with ':' replaced with blank spaces.
			// But there is another case: if the original value is just a series of 0x00
			// (nul) chars, the SNMP API converts that to an
			// empty string, while we need to actually display 00 00 00 00...
			// That's what we're doing in the code below

			// If octetStringValuetoString is empty while the original value's length was
			// greater than 0, then we'll need to convert it to hexadecimal
			if (octetStringValuetoString.isEmpty() && octetStringValue.getBytes().length > 0) {
				result.value = octetStringValuetoHex;
			} else if (octetStringValuetoString.length() == octetStringValuetoHex.length()) {
				result.value = octetStringValuetoHex;
			} else {
				result.value = octetStringValuetoString;
			}
		}

		// Sets the result object
		else {
			result.oid = oid.toString();
			result.type = value.getRespTypeString();
			result.value = value.toString();
		}

		return result;

	} // end of sendRequest

}// end of class - SNMPClient
