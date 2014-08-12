package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests standard XQuery functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class FnTest extends AdvancedQueryTest {
  /** Text file. */
  private static final String TEXT = "src/test/resources/input.xml";

  /** Test method. */
  @Test
  public void unparsedText() {
    contains(UNPARSED_TEXT.args(TEXT), "&lt;html");
    contains(UNPARSED_TEXT.args(TEXT, "US-ASCII"), "&lt;html");
    error(UNPARSED_TEXT.args(TEXT, "xyz"), ENCODING_X);
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

  /** Tests for the {@code fn:replate} function. */
  @Test
  public void replace() {
    // tests for issue GH-573:
    query("replace('aaaa bbbbbbbb ddd ','(.{6,15}) ','$1@')", "aaaa bbbbbbbb@ddd ");
    query("replace(' aaa AAA 123','(\\s+\\P{Ll}{3,280}?)','$1@')", " aaa AAA@ 123@");
    error("replace('asdf','a{12,3}','')", REGPAT_X);
  }
}
