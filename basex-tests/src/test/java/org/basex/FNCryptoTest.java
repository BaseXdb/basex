package org.basex;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the EXPath Cryptographic module. The tests in basex-test
 * package are only executable after a java keystore has been created.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class FNCryptoTest extends SandboxTest {
  /** User home directory. */
  private static final String KEYSTORE_DIR = System.getProperty("user.home");
  /** Java home directory. */
  private static final String JAVA_HOME = System.getProperty("java.home");
  /** Keytool executable. */
  private static final String KEYTOOL = JAVA_HOME + "/bin/keytool";
  /** Key store file. */
  private static final String KEYSTORE = KEYSTORE_DIR + "/keystore.jks";
  /** Key alias. */
  private static final String ALIAS = "basex";
  /** Key store and key password. */
  private static final String PASS = "password";

  /** Shell command to create java keystore. */
  private static final String[] GENKEY_CMD = { KEYTOOL,
      "-genkey", "-keyalg", "RSA", "-validity", "360", "-alias", ALIAS,
      "-keystore", KEYSTORE, "-storepass", PASS, "-keypass", PASS,
      "-dname", "CN=hans wurst, OU=dev, O=basex, L=konstanz, ST=bw, C=de" };

  /** Digital certificate element. */
  private static final String CT = "<digital-certificate>" +
      "<keystore-type>JKS</keystore-type>" +
      "<keystore-password>" + PASS + "</keystore-password>" +
      "<key-alias>" + ALIAS + "</key-alias>" +
      "<private-key-password>" + PASS + "</private-key-password>" +
      "<keystore-uri>" + KEYSTORE + "</keystore-uri>" +
      "</digital-certificate>";

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test public void validateSignatureWithCertificate() {
    query("crypto:validate-signature(" +
        "crypto:generate-signature(<a/>,'','','','',''," + CT + "))", "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test public void validateSignatureWithXPathAndCertificate() {
    query("crypto:validate-signature(crypto:generate-signature(<a><n/><n/></a>," +
        "'','','','','','/a/n'," + CT + "))", "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test public void validateSignatureFullySpecified() {
    query("crypto:validate-signature(crypto:generate-signature(<a><n/></a>," +
        "'exclusive','SHA512','RSA_SHA1','myPrefix','enveloped','/a/n'," + CT +
        "))", "true");
  }

  /**
   * Creates the database context.
   * @throws Exception error during keystore generation or database exception
   */
  @BeforeAll public static void start() throws Exception {
    new File(KEYSTORE).delete();

    final Process proc = Runtime.getRuntime().exec(GENKEY_CMD);
    Thread.sleep(2000); // give the keytool some time to finish
    if(proc.exitValue() != 0) throw new RuntimeException("Cannot initialize keystore.");

    // turn off pretty printing
    set(MainOptions.SERIALIZER, SerializerMode.NOINDENT.get());
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterAll public static void finish() {
    new File(KEYSTORE).delete();
  }

  /**
   * Runs a query and matches the result against the expected output.
   * @param query query
   * @param expected expected output
   */
  private static void query(final String query, final String expected) {
    query(null, query, expected);
  }

  /**
   * Runs an updating query and matches the result of the second query
   * against the expected output.
   * @param first first query
   * @param second second query
   * @param expected expected output
   */
  private static void query(final String first, final String second, final String expected) {
    if(first != null) query(first);
    final String result = query(second);
    // quotes are replaced by apostrophes to simplify comparison
    assertEquals(expected.replaceAll("\"", "'"), result.replaceAll("\"", "'"));
  }
}