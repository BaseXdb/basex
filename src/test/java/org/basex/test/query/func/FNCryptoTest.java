package org.basex.test.query.func;

import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the functions of the EXPath Cryptographic module.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public class FNCryptoTest extends AdvancedQueryTest {

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

  // ADDITIONAL TESTS

  /*declare namespace c = 'http://expath.org/ns/crypto';
  c:generate-signature(/n,'','','','','')*/
}
