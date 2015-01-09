package org.basex.io.parse.json;

import static org.junit.Assert.*;

import org.basex.query.*;
import org.junit.*;

/**
 * Tests for the {@link JsonParser} class.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class JsonParserTest {
  /** Tests if the empty JSON string is rejected. */
  public void emptyQuery() {
    error("", false);
    error(" \t\r\n", false);
  }

  /**
   * Tests for parsing numbers.
   * @throws QueryIOException query I/O exception
   */
  @Test public void numberTest() throws QueryIOException {
    parse("0", false);
    parse("1", false);
    parse("-1", false);
    parse("10", false);
    parse("1234567890123456789012345678901234567890", false);
    parse("0.5", false);
    parse("0.01", false);
    parse("-0.01", false);
    parse("1234567890123456789012345678901234567890" +
        ".1234567890123456789012345678901234567890", false);
    parse("0E1", false);
    parse("0E-1", false);
    parse("0E+1", false);
    parse("-0E+1", false);
    parse("0E00", false);
    parse("1234567890123456789012345678901234567890" +
        "e1234567890123456789012345678901234567890", false);
    parse("123e-123", false);
    parse("123.4e-123", false);
    parse("123.456E0001", false);
    parse("-123.456E0001", false);
    parse("[ -123.456E0001, 0 ]", false);

    error("01", false);
    error("-", false);
    error("-\u00e4", false);
    error("0.", false);
    error("0.\u00e4", false);
    error("1e", false);
    error("1e+", false);
    error("1e+\u00e4", false);
    error("1e+0\u00e4", false);
  }

  /**
   * Tests for paring strings.
   * @throws QueryIOException query I/O exception
   */
  @Test public void stringTest() throws QueryIOException {
    parse("\"\"", false);
    parse("\"test\"", false);
    parse("\"\u00e4\"", false);
    parse("\"\uD834\uDD1E\"", false);
    parse("\"\uD853\uDF5C\"", false);
    parse("\"\\n\"", false);
    parse("\"\\\"\\\\\"", false);
    parse("\"\\u000a\"", "\"\\n\"", false);
    parse("\"\\u000A\"", "\"\\n\"", false);
    parse("\"\n\"", "\"\\n\"", true);

    unescape("\"\\b\\f\\t\\r\\n\"", "\"\\\\b\\\\f\\\\t\\\\r\\\\n\"");
    // Unicode in JSON notation
    unescape("\"\\uD853\\uDF5C\"", "\"\\\\uD853\\\\uDF5C\"");
    unescape("\"\\uD853asdf\"", "\"\\\\uD853asdf\"");
    unescape("\"\\uD853\"", "\"\\\\uD853\"");
    // Unicode in Java notation
    unescape("\"\u00E4\\t\"", "\"\u00E4\\\\t\"");
    unescape("\"\u00E4\\u00E4\\t\"", "\"\u00E4\\\\u00E4\\\\t\"");

    error("\"\\u0A", false);
    error("\"\\uXX0A\"", false);
    error("\"\\u0 00\"", false);
    error("\"\\u0:00\"", false);
    error("\"\\u0_00\"", false);
    error("\"\\u0~00\"", false);
    error("\"test", false);
    error("\"\uD800", false);
    error("\"\n\"", false);

    error("\"\uD834\"", false);
    error("\"\uD853\uFFFF\"", false);
    error("\"\uFFFF\"", false);
    error("\"\uD853a\"", false);
    error("\"\\b\\f\\t\\r\\n\"", false);
    error("\"\\u0000\\u001F\"", false);
  }

  /**
   * Tests for parsing arrays.
   * @throws QueryIOException query I/O exception
   */
  @Test public void arrayTest() throws QueryIOException {
    parse("[ ]", false);
    parse("[]", "[ ]", false);
    parse("[[[[[[42], {}]]]]]", "[ [ [ [ [ [ 42 ], { } ] ] ] ] ]", false);
    parse("[ 1, 2, 3, 4, 5, 6, 7, 8 ]", false);
    parse("[1,2,3,]", "[ 1, 2, 3 ]", true);

    error("[1,2,3,]", false);
    error("[,42]", false);
    error("[1,", false);
  }

  /**
   * Tests for parsing objects.
   * @throws QueryIOException query I/O exception
   */
  @Test public void objectTest() throws QueryIOException {
    parse("{ }", false);
    parse("{ \"\": 42 }", false);
    parse("{ a : 42, b: 23 }", "{ \"a\": 42, \"b\": 23 }", true);
    parse("{ \"a\": 1, \"b\": 2, }", "{ \"a\": 1, \"b\": 2 }", true);

    error("{ a : 42 }", false);
    error("{ \"a\": 42, b: 23 }", false);
  }

  /**
   * Tests for parsing literals.
   * @throws QueryIOException query I/O exception
   */
  @Test public void literals() throws QueryIOException {
    parse("true", false);
    parse("false", false);
    parse("null", false);

    parse("true", true);
    parse("false", true);
    parse("null", true);

    error("true123", true);
  }

  /**
   * Tests for parsing constructors.
   * @throws QueryIOException query I/O exception
   */
  @Test public void constructorTest() throws QueryIOException {
    parse("new foo()", true);
    parse("new Test(1, { })", true);
    parse("new Foo([ { \"a\": new Test(1, { }) } ])", true);

    error("new" , true);
    error("newt", true);
  }

  /**
   * Tests if the given JSON string is rejected by the parser using the given spec.
   * @param json JSON string
   * @param liberal liberal parsing
   */
  private void error(final String json, final boolean liberal) {
    try {
      parse(json, liberal);
      fail("Should have failed: '" + json + '\'');
    } catch(final QueryIOException qe) {
      // expected error
    }
  }

  /**
   * Checks if the given JSON string is correct and is reproduced by the parser.
   * @param json JSON string
   * @param liberal liberal parsing
   * @throws QueryIOException parse error
   */
  private void parse(final String json, final boolean liberal) throws QueryIOException {
    parse(json, json, liberal);
  }

  /**
   * Checks if the given JSON string is correct and produces the given output.
   * @param json JSON string
   * @param exp expected output
   * @param liberal liberal parsing
   * @throws QueryIOException parse error
   */
  private void parse(final String json, final String exp, final boolean liberal)
      throws QueryIOException {
    assertEquals(exp, JsonStringConverter.toString(json, liberal, true));
  }

  /**
   * Checks if the given JSON string is correct and produces the given output with unescaping
   * deactivated.
   * @param json JSON string
   * @param exp expected output
   * @throws QueryIOException parse error
   */
  private void unescape(final String json, final String exp) throws QueryIOException {
    assertEquals(exp, JsonStringConverter.toString(json, false, false));
  }
}
