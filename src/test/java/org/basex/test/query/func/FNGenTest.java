package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the <code>FNGen</code> class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNGenTest extends AdvancedQueryTest {
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";

  /**
   * Test method for the fn:unparsed-text() function.
   */
  @Test
  public void fnUnparsedText() {
    check(UNPARSED_TEXT);
    contains(UNPARSED_TEXT.args(TEXT), "&lt;html");
    contains(UNPARSED_TEXT.args(TEXT, "US-ASCII"), "&lt;html");
    error(UNPARSED_TEXT.args(TEXT, "xyz"), Err.WHICHENC);
  }

  /**
   * Test method for the fn:parse-xml() function.
   */
  @Test
  public void fnParseXML() {
    check(PARSE_XML);
    contains(PARSE_XML.args("\"<x>a</x>\"") + "//text()", "a");
  }

  /**
   * Test method for the fn:serialize() function.
   */
  @Test
  public void fnSerialize() {
    check(SERIALIZE);
    contains(SERIALIZE.args("<x/>"), "&lt;x/&gt;");
    contains(SERIALIZE.args("<x/>", serialParams("")), "&lt;x/&gt;");
    contains(SERIALIZE.args("<x>a</x>", serialParams("<method value='text'/>")), "a");
  }
}
