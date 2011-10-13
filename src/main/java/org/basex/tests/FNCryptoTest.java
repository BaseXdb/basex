package org.basex.tests;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functions of the EXPath Cryptographic module. The tests
 * in basex-test package are only executable after a java keystore has been
 * created.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FNCryptoTest {
  /** Database context. */
  private static Context context;


/*
shell command to create java keystore (REMOVE LINEBREAK BEFORE C&P!), expected
to be stored in HOME

keytool -genkey -keyalg RSA -alias basex -keystore keystore.jks
 -storepass password -validity 360

What is your first and last name?
[Unknown]:  hans wurst
What is the name of your organizational unit?
  [Unknown]:  dev
What is the name of your organization?
  [Unknown]:  basex
What is the name of your City or Locality?
  [Unknown]:  konstanz
What is the name of your State or Province?
  [Unknown]:  bw
What is the two-letter country code for this unit?
  [Unknown]:  de
 */


  /** Digital certificate element. */
  private static final String CT = "<digital-certificate>" +
      "<keystore-type>JKS</keystore-type>" +
      "<keystore-password>password</keystore-password>" +
      "<key-alias>basex</key-alias>" +
      "<private-key-password>password</private-key-password>" +
      "<keystore-uri>~/keystore.jks</keystore-uri>" +
      "</digital-certificate>";

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithCertificate() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a/>,'','','','',''," + CT
        + "))", "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureWithXPathAndCertificate() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a><n/><n/></a>," +
        "'','','','','','/a/n'," + CT + "))", "true");
  }

  /**
   * Tests whether validate-signature returns true for a certificate created
   * with generate-signature.
   */
  @Test
  public void validateSignatureFullySpecified() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(<a><n/></a>," +
        "'exclusive','SHA512','RSA_SHA1','myPrefix','enveloped','/a/n'," + CT +
        "))", "true");
  }

  /**
  * Test method for crypto:encrypt and crypto:decrypt with asymmetric keys.
  */
  @Test
  public void encryptionAsym1() {
   final String msg = "messagemessagemessagemessagemessagemessagemessage";

   PublicKey puk = null;
   PrivateKey prk = null;
   try {

     final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
     final KeyPair kp = gen.generateKeyPair();
     puk = kp.getPublic();
     prk = kp.getPrivate();

   } catch(NoSuchAlgorithmException e) {
     e.printStackTrace();
   }

   query("declare namespace c = 'http://expath.org/ns/crypto';" +
      "let $e := c:encrypt('" + msg + "','asymmetric','" + prk + "','RSA')" +
       "return c:decrypt($e,'asymmetric','" + puk + "','RSA')", msg);
  }

  /**
   * Creates the database context.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void start() throws BaseXException {
    context = new Context();
    // turn off pretty printing
    new Set(Prop.SERIALIZER, "indent=no").execute(context);
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterClass
  public static void finish() {
    context.close();
  }

  /**
   * Runs a query and matches the result against the expected output.
   * @param query query
   * @param expected expected output
   */
  private void query(final String query, final String expected) {
    query(null, query, expected);
  }

  /**
   * Runs an updating query and matches the result of the second query
   * against the expected output.
   * @param first first query
   * @param second second query
   * @param expected expected output
   */
  private void query(final String first, final String second,
      final String expected) {

    try {
      if(first != null) new XQuery(first).execute(context);
      final String result = new XQuery(second).execute(context);
      // quotes are replaced by apostrophes to simplify comparison
      assertEquals(expected.replaceAll("\\\"", "'"),
          result.replaceAll("\\\"", "'"));
    } catch(final BaseXException ex) {
      fail(Util.message(ex));
    }
  }
}