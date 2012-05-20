package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery functions prefixed with "validate".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNValidateTest extends AdvancedQueryTest {
  /**
   * Test method for the validate:xsd() function.
   */
  @Test
  public void validateXSD() {
    check(_VALIDATE_XSD);
  }

  /**
   * Test method for the validate:dtd() function.
   */
  @Test
  public void validateDTD() {
    check(_VALIDATE_DTD);
  }
}
