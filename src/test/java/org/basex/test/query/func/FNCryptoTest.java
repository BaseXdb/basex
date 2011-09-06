package org.basex.test.query.func;

import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;
import static org.basex.util.Token.*;

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
}
