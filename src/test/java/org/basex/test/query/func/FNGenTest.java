package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;
import static org.basex.util.Token.*;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.Test;

/**
 * This class tests the functions of the <code>FNGen</code> class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNGenTest extends AdvancedQueryTest {
  /** Test database name. */
  private static final String NAME = Util.name(FNGenTest.class);
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";

  /**
   * Test method for the fn:unparsed-text() function.
   * @throws Exception exception
   */
  @Test
  public void fnUnparsed3Text() throws Exception {
    check(UNPARSED_TEXT);
    contains(UNPARSED_TEXT.args(TEXT), "?&gt;&lt;html");
    contains(UNPARSED_TEXT.args(TEXT, "US-ASCII"), "?&gt;&lt;html");
    final IOFile io = new IOFile(Prop.TMP, NAME);
    io.write(token("A\r\nB"));
    query(STRING_LENGTH.args(UNPARSED_TEXT.args(io.path())), 3);
    io.write(token("A\nB"));
    query(STRING_LENGTH.args(UNPARSED_TEXT.args(io.path())), 3);
    io.write(token("A\rB"));
    query(STRING_LENGTH.args(UNPARSED_TEXT.args(io.path())), 3);
    io.write(token("A\r\nB\rC\nD"));
    query(_UTIL_TO_BYTES.args(UNPARSED_TEXT.args(io.path())), "65 10 66 10 67 10 68");
    assertTrue(io.delete());
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
