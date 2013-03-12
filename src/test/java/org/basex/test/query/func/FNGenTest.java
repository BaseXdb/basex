package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests XQuery functions placed in the {@link FNGen} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNGenTest extends AdvancedQueryTest {
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";

  /** Test method. */
  @Test
  public void unparsedText() {
    contains(UNPARSED_TEXT.args(TEXT), "&lt;html");
    contains(UNPARSED_TEXT.args(TEXT, "US-ASCII"), "&lt;html");
    error(UNPARSED_TEXT.args(TEXT, "xyz"), Err.WHICHENC);
  }

  /** Test method. */
  @Test
  public void parseXML() {
    contains(PARSE_XML.args("\"<x>a</x>\"") + "//text()", "a");
  }

  /** Test method. */
  @Test
  public void serialize() {
    contains(SERIALIZE.args("<x/>"), "&lt;x/&gt;");
    contains(SERIALIZE.args("<x/>", serialParams("")), "&lt;x/&gt;");
    contains(SERIALIZE.args("<x>a</x>", serialParams("<method value='text'/>")), "a");
  }
}
