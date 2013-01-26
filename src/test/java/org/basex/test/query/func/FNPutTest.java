package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the XQuery fn:put() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public class FNPutTest extends AdvancedQueryTest {

  /** Test output URI is correctly resolved. */
  @Test
  public void resolveUri() {
    String output = sandbox().merge("test.xml").url();
    query(PUT.args("<a/>", output));
  }
}
