package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery functions prefixed with "validate".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNValidateTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String DIR = "src/test/resources/";
  /** Test file. */
  private static final String FILE = DIR + "input.xml";

  /**
   * Test method for the validate:xsd() function.
   */
  @Test
  public void validateXSD() {
    check(_VALIDATE_XSD);
    error(_VALIDATE_XSD.args("unknown"), Err.WHICHRES);
    error(_VALIDATE_XSD.args(FILE, "unknown.xsd"), Err.WHICHRES);
    error(_VALIDATE_XSD.args(FILE), Err.VALFAIL);
    // [MS] to be added: tests with internal/external XSD
  }

  /**
   * Test method for the validate:dtd() function.
   */
  @Test
  public void validateDTD() {
    check(_VALIDATE_DTD);
    error(_VALIDATE_DTD.args("unknown"), Err.WHICHRES);
    error(_VALIDATE_DTD.args(FILE, "unknown.dtd"), Err.WHICHRES);
    error(_VALIDATE_DTD.args(FILE), Err.VALFAIL);
    // [MS] to be added: tests with internal/external DTD
  }
}
