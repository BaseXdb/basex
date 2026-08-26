package org.basex;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the EXPath Cryptographic module. A Java keystore is generated
 * in the sandbox before the tests are run.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class FNCryptoTest extends SandboxTest {
  /** Keytool executable. */
  private static final String KEYTOOL = System.getProperty("java.home") + "/bin/keytool";
  /** Key store file. */
  private static final IOFile KEYSTORE = new IOFile(sandbox(), "keystore.jks");
  /** Key alias. */
  private static final String ALIAS = "basex";
  /** Key store and key password. */
  private static final String PASS = "password";
  /** Timeout for the keystore generation (seconds). */
  private static final int TIMEOUT = 60;

  /** Digital certificate element. */
  private static final String CT = "<digital-certificate>" +
      "<keystore-type>JKS</keystore-type>" +
      "<keystore-password>" + PASS + "</keystore-password>" +
      "<key-alias>" + ALIAS + "</key-alias>" +
      "<private-key-password>" + PASS + "</private-key-password>" +
      "<keystore-uri>" + KEYSTORE.path() + "</keystore-uri>" +
      "</digital-certificate>";

  /**
   * Creates the keystore.
   * @throws Exception error during keystore generation
   */
  @BeforeAll public static void start() throws Exception {
    KEYSTORE.delete();
    final ProcessBuilder pb = new ProcessBuilder(KEYTOOL,
        "-genkey", "-keyalg", "RSA", "-validity", "360", "-alias", ALIAS,
        "-keystore", KEYSTORE.path(), "-storepass", PASS, "-keypass", PASS,
        "-dname", "CN=basex, OU=dev, O=basex, L=konstanz, ST=bw, C=de");
    final Process proc = pb.redirectErrorStream(true).start();
    if(!proc.waitFor(TIMEOUT, TimeUnit.SECONDS)) {
      proc.destroyForcibly();
      fail("Keystore generation timed out.");
    }
    assertEquals(0, proc.exitValue(), "Cannot initialize keystore.");
    assertTrue(KEYSTORE.exists(), "Keystore was not created: " + KEYSTORE);
  }

  /**
   * Removes the keystore.
   */
  @AfterAll public static void finish() {
    KEYSTORE.delete();
  }

  /** Validates a signature that was created with the default algorithms. */
  @Test public void defaultAlgorithms() {
    query("crypto:validate-signature(" +
        "crypto:generate-signature(<a/>,'','','','',''," + CT + "))", true);
  }

  /** Validates a signature that only covers the nodes selected by an XPath expression. */
  @Test public void xpath() {
    query("crypto:validate-signature(crypto:generate-signature(<a><n/><n/></a>," +
        "'','','','','','/a/n'," + CT + "))", true);
  }

  /** Validates a signature for which every parameter is specified. */
  @Test public void fullySpecified() {
    query("crypto:validate-signature(crypto:generate-signature(<a><n/></a>," +
        "'exclusive','SHA512','RSA_SHA512','myPrefix','enveloped','/a/n'," + CT +
        "))", true);
  }

  /**
   * Validates signatures for all signature algorithms that a signature can be validated with
   * (SHA-1 is rejected by the secure validation of the JDK).
   */
  @Test public void signatureAlgorithms() {
    for(final String algorithm : new String[] { "rsa_sha256", "rsa_sha512" }) {
      query("crypto:validate-signature(crypto:generate-signature(<a/>," +
          "'','','" + algorithm + "','','',''," + CT + "))", true);
    }
  }

  /**
   * Validates signatures for all digest algorithms that a signature can be validated with
   * (SHA-1 is rejected by the secure validation of the JDK).
   */
  @Test public void digestAlgorithms() {
    for(final String algorithm : new String[] { "sha256", "sha512" }) {
      query("crypto:validate-signature(crypto:generate-signature(<a/>," +
          "'','" + algorithm + "','','','',''," + CT + "))", true);
    }
  }
}
