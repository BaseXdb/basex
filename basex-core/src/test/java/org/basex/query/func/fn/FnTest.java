package org.basex.query.func.fn;

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

  /** Tests for the {@code fn:replace} function. */
  @Test
  public void replace() {
    // tests for issue GH-573:
    query("replace('aaaa bbbbbbbb ddd ','(.{6,15}) ','$1@')", "aaaa bbbbbbbb@ddd ");
    query("replace(' aaa AAA 123','(\\s+\\P{Ll}{3,280}?)','$1@')", " aaa AAA@ 123@");
    error("replace('asdf','a{12,3}','')", REGPAT_X);
  }

  /** Tests for the {@code fn:sum} function. */
  @Test
  public void sum() {
    query("sum(1)", "1");
    query("sum(1 to 10)", "55");
    query("sum(1 to 3037000499)", "4611686016981624750");
    query("sum(1 to 3037000500)", "4611686020018625250");
    query("sum(1 to 4294967295)", "9223372034707292160");
  }

  /** Tests for the {@code fn:parse-ietf-date} function. */
  @Test
  public void parseIetfDate() {
    query("fn:parse-ietf-date('Wed, 06 Jun 1994 07:29:35 GMT')", "1994-06-06T07:29:35Z");
    query("fn:parse-ietf-date('Wed, 6 Jun 94 07:29:35 GMT')", "1994-06-06T07:29:35Z");
    query("fn:parse-ietf-date('Wed Jun 06 11:54:45 EST 0090')", "0090-06-06T11:54:45-05:00");
    query("fn:parse-ietf-date('Sunday, 06-Nov-94 08:49:37 GMT')", "1994-11-06T08:49:37Z");
    query("fn:parse-ietf-date('Wed, 6 Jun 94 07:29:35 +0500')", "1994-06-06T07:29:35+05:00");
    query("fn:parse-ietf-date(' 1 Nov 1234 05:06:07.89 gmt')", "1234-11-01T05:06:07.89Z");

    query("fn:parse-ietf-date(' 01-feb-3456 07:08:09 GMT')", "3456-02-01T07:08:09Z");
    query("fn:parse-ietf-date(' 01-FEB-3456 07:08:09 GMT')", "3456-02-01T07:08:09Z");
    query("fn:parse-ietf-date('Wed, 06 Jun 94 07:29:35 +0000 (GMT)')", "1994-06-06T07:29:35Z");
    query("fn:parse-ietf-date('Wed, 06 Jun 94 07:29:35')", "1994-06-06T07:29:35Z");

    String s = "Wed, Jan-01 07:29:35 GMT 19";
    query("fn:parse-ietf-date('" + s + "')", "1919-01-01T07:29:35Z");
    for(int i = s.length(); --i >= 0;) {
      error("fn:parse-ietf-date('" + s.substring(0, i) + "')", IETF_PARSE_X_X_X);
    }

    s = "Wed, 06 Jun 1994 07:29";
    query("fn:parse-ietf-date('" + s + "')", "1994-06-06T07:29:00Z");
    for(int i = s.length(); --i >= 0;) {
      error("fn:parse-ietf-date('" + s.substring(0, i) + "')", IETF_PARSE_X_X_X);
    }
    error("fn:parse-ietf-date('" + s + "X')", IETF_PARSE_X_X_X);

    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0000 (')", IETF_PARSE_X_X_X);
    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0000 (GT)')", IETF_PARSE_X_X_X);
    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0000 (GMT')", IETF_PARSE_X_X_X);

    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35. GMT')", IETF_PARSE_X_X_X);
    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35 0500')", IETF_PARSE_X_X_X);
    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35 +5')", IETF_PARSE_X_X_X);
    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35 -050')", IETF_PARSE_X_X_X);
    error("fn:parse-ietf-date('Wed, 99 Jun 94 07:29:35 +0500')", IETF_INIT_X);
  }
}
