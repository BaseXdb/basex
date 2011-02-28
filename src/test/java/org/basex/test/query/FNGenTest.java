package org.basex.test.query;

import org.basex.core.BaseXException;
import org.basex.query.func.FunDef;
import org.basex.query.util.Err;
import org.junit.Test;

/**
 * This class tests the functions of the <code>FNGen</code> class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNGenTest extends AdvancedQueryTest {
  /** Text file. */
  private static final String TEXT = "etc/xml/stopWords";

  /** Constructor. */
  public FNGenTest() {
    super("fn");
  }

  /**
   * Test method for the fn:unparsed-text() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testUnparsedText() throws BaseXException {
    final String fun = check(FunDef.PARSETXT, String.class, String.class);
    contains(fun + "('" + TEXT + "')", "aboutabove");
    contains(fun + "('" + TEXT + "', 'US-ASCII')", "aboutabove");
    error(fun + "('" + TEXT + "', 'xyz')", Err.WRONGINPUT);
  }

  /**
   * Test method for the fn:parse-xml() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testParseXML() throws BaseXException {
    final String fun = check(FunDef.PARSEXML, String.class, String.class);
    contains(fun + "('<x>a</x>')//text()", "a");
  }

  /**
   * Test method for the fn:serialize() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testSerialize() throws BaseXException {
    final String fun = check(FunDef.SERIALIZE, Object.class, Object.class);
    contains(fun + "(<x/>)", "&lt;x/&gt;");
    contains(fun + "(<x/>, " + serialParams("") + ")", "&lt;x/&gt;");
    contains(fun + "(<x>a</x>, " +
        serialParams("<method>text</method>") + ")", "a");
  }
}
