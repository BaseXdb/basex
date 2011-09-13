package org.basex.test.query.func;

import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.AfterClass;
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
   * Test method for crypto:encrypt and crypto:decrypt.
   */
  @Test
  public void encryption1() {
    final String msg = "messagemessagemessagemessagemessagemessagemessage";

    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "let $e := c:encrypt('" + msg + "','symmetric','aaabbbaa','')" +
        "return c:decrypt($e,'symmetric','aaabbbaa','')", msg);
  }

  /**
   * Test method for crypto:encrypt and crypto:decrypt.
   */
  @Test
  public void hmac1() {
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:hmac('message','key','hmacMd5', 'hex')",
        "\"4E4748E62B463521F6775FBF921234B5\"");
  }

  /**
   * Test method for crypto:encrypt and crypto:decrypt.
   */
  @Test
  public void generatesignature() throws Exception {
    new CreateDB(DB, "<n/>").execute(CONTEXT);
    query("declare namespace c = 'http://expath.org/ns/crypto';" +
        "c:generate-signature(/n,'','','','','')",
        "");
  }

  /**
   * Test method for crypto:encrypt and crypto:decrypt.
   */
  @Test
  public void validatesignature() throws Exception {
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