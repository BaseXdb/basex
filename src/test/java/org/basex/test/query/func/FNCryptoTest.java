package org.basex.test.query.func;

import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests the functions of the EXPath Cryptographic module.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FNCryptoTest extends AdvancedQueryTest {

  /** Test database name. */
  private static final String DB = Util.name(FNCryptoTest.class);

  /**
   * Test method for crypto:encrypt and crypto:decrypt with symmetric keys.
   */
  @Test
  public void encryptionSym1() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    //DES/CBC/PKCS5Padding
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','aaabbbaa','DES/CBC/PKCS5Padding')" +
        "return c:decrypt($e,'symmetric','aaabbbaa','DES/CBC/PKCS5Padding')", msg);
  }
  
  /**
   * Test method for crypto:encrypt and crypto:decrypt with symmetric keys.
   */
  @Test
  public void encryptionSym2() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    //DES/CBC/PKCS5Padding
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','abababababababab','AES/CBC/PKCS5Padding')" +
        "return c:decrypt($e,'symmetric','abababababababab','AES/CBC/PKCS5Padding')", msg);
  }
  
  /**
   * Test method for crypto:encrypt and crypto:decrypt with asymmetric keys.
   */
  @Test
  public void encryptionAsym1() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','asymmetric','aaabbbaa','DSA')" +
        "return c:decrypt($e,'asymmetric','aaabbbaa','DSA')", msg);
  }
  
  @Test
  public void hmac1() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','hmacMd5', 'hex')",
        "\"4E4748E62B463521F6775FBF921234B5\"");
  }

  @Test
  public void generateSignatureSyntax() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','')");
  }
  
  @Test
  public void generateSignatureSyntax2() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','','')");
  }
  
  @Test
  public void generateSignatureSyntax3() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','',<a/>)");
  }
  
  @Test
  public void generateSignatureSyntax4() throws Exception {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','','','',<a/>)");
  }
  
  @Test
  public void generateSignatureSyntax5() throws Exception {
    // TODO general syntax test equiv. FNDbTest.java check()
    Assert.fail();
  }
  
  @Test
  public void generateSignature1() throws Exception {
    new CreateDB(DB, "<n/>").execute(CONTEXT);
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(/n,'','','','','')",
        "");
  }
  
  @Test
  public void generateSignature2() throws Exception {
    final String certificate = 
        "<digital-certificate>" +
        "<keystore-type>JKS</keystore-type>" +
        "<keystore-password>password</keystore-password>" +
        "<key-alias>basexselfsigned</key-alias>" +
        "<private-key-password>password</private-key-password>" +
        "<keystore-uri>/Users/lukas/keystore.jks</keystore-uri>" +
        "</digital-certificate>";
    
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(<a/>,'','','','',''," + certificate + ")",
        "");
  }

  @Test
  public void validateSignature() throws Exception {
    new CreateDB(DB, "<n/>").execute(CONTEXT);
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:validate-signature(c:generate-signature(/n,'','','','',''))",
        "true");
  }
  
  /**
   * Deletes the test db.
   * @throws Exception exception
   */
  @AfterClass
  public static void end() throws Exception {
    new DropDB(DB).execute(CONTEXT);
    CONTEXT.close();
  }
}