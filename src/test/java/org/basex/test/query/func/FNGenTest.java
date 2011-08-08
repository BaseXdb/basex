package org.basex.test.query.func;

import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the functions of the <code>FNGen</code> class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNGenTest extends AdvancedQueryTest {
  /** Text file. */
  private static final String TEXT = "etc/test/input.xml";

  /**
   * Test method for the fn:unparsed-text() function.
   * @throws QueryException database exception
   */
  @Test
  public void fnUnparsedText() throws QueryException {
    final String fun = check(Function.PARSETXT);
    contains(fun + "('" + TEXT + "')", "?&gt;&lt;html");
    contains(fun + "('" + TEXT + "', 'US-ASCII')", "?&gt;&lt;html");
    error(fun + "('" + TEXT + "', 'xyz')", Err.WRONGINPUT);
  }

  /**
   * Test method for the fn:parse-xml() function.
   * @throws QueryException database exception
   */
  @Test
  public void fnParseXML() throws QueryException {
    final String fun = check(Function.PARSEXML);
    contains(fun + "('<x>a</x>')//text()", "a");
  }

  /**
   * Test method for the fn:serialize() function.
   * @throws QueryException database exception
   */
  @Test
  public void fnSerialize() throws QueryException {
    final String fun = check(Function.SERIALIZE);
    contains(fun + "(<x/>)", "&lt;x/&gt;");
    contains(fun + "(<x/>, " + serialParams("") + ")", "&lt;x/&gt;");
    contains(fun + "(<x>a</x>, " +
        serialParams("<method>text</method>") + ")", "a");
  }
}
