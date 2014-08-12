package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the XQuery fn:put() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public class FNPutTest extends AdvancedQueryTest {
  /** Test output URI is correctly resolved. */
  @Test
  public void resolveUri() {
    final String output = sandbox().merge("test.xml").url();
    query(PUT.args("<a/>", output));
  }
}
