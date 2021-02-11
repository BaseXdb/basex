package org.basex.io.parse.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link JsonParser} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class JsonParserTest {
  /** Tests if the empty JSON string is rejected. */
  @Test public void emptyQuery() {
    error("", false);
    error(" \t\r\n", false);
  }

  /**
   * Tests for parsing numbers.
   * @throws Exception exception
   */
  @Test public void numberTest() throws Exception {
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
   * @throws Exception exception
   */
  @Test public void stringTest() throws Exception {
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
    parse("\"\uD834\"", "\"\uFFFD\"", false);
    parse("\"\uFFFF\"", "\"\uFFFD\"", false);
    parse("\"\\b\\f\\t\\r\\n\"", "\"\uFFFD\uFFFD\\t\\r\\n\"", false);
    parse("\"\\u0000\\u001F\"", "\"\uFFFD\uFFFD\"", false);
    parse("\"\uD853\uFFFF\"", "\"\uFFFD\uFFFD\"", false);
    parse("\"\uD853a\"", "\"\uFFFDa\"", false);

    escape("\"\\u0008\\u000c\\u0009\\u000d\\u000a\"", "\"\\\\b\\\\f\\\\t\\\\r\\\\n\"");
    escape("\"\\b\\f\\t\\r\\n\"", "\"\\\\b\\\\f\\\\t\\\\r\\\\n\"");
    // Unicode in JSON notation
    //escape("\"\\uD853\\uDF5C\"", "\"\\\\uD853\\\\uDF5C\"");
    escape("\"\\uD853asdf\"", "\"\\\\uD853asdf\"");
    escape("\"\\uD853\"", "\"\\\\uD853\"");

    // Unicode in Java notation
    escape("\"\u00E4\\t\"", "\"\u00E4\\\\t\"");
    escape("\"\u00E4\\u00E4\\t\"", "\"\u00E4\u00E4\\\\t\"");

    error("\"\\u0A", false);
    error("\"\\uXX0A\"", false);
    error("\"\\u0 00\"", false);
    error("\"\\u0:00\"", false);
    error("\"\\u0_00\"", false);
    error("\"\\u0~00\"", false);
    error("\"test", false);
    error("\"\uD800", false);
    error("\"\n\"", false);
  }

  /**
   * Tests for parsing arrays.
   * @throws Exception exception
   */
  @Test public void arrayTest() throws Exception {
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
   * @throws Exception exception
   */
  @Test public void objectTest() throws Exception {
    parse("{ }", false);
    parse("{ \"\": 42 }", false);
    parse("{ a : 42, b: 23 }", "{ \"a\": 42, \"b\": 23 }", true);
    parse("{ \"a\": 1, \"b\": 2, }", "{ \"a\": 1, \"b\": 2 }", true);

    error("{ a : 42 }", false);
    error("{ \"a\": 42, b: 23 }", false);
  }

  /**
   * Tests for parsing literals.
   * @throws Exception exception
   */
  @Test public void literals() throws Exception {
    parse("true", false);
    parse("false", false);
    parse("null", false);

    parse("true", true);
    parse("false", true);
    parse("null", true);

    error("true123", true);
  }

  /**
   * Tests if the given JSON string is rejected by the parser using the given spec.
   * @param json JSON string
   * @param liberal liberal parsing
   */
  private static void error(final String json, final boolean liberal) {
    try {
      parse(json, liberal);
      fail("Should have failed: '" + json + '\'');
    } catch(final Exception qe) {
      // expected error
    }
  }

  /**
   * Checks if the given JSON string is correct and is reproduced by the parser.
   * @param json JSON string
   * @param liberal liberal parsing
   * @throws Exception exception
   */
  private static void parse(final String json, final boolean liberal) throws Exception {
    parse(json, json, liberal);
  }

  /**
   * Checks if the given JSON string is correct and produces the given output.
   * @param json JSON string
   * @param exp expected output
   * @param liberal liberal parsing
   * @throws Exception exception
   */
  private static void parse(final String json, final String exp, final boolean liberal)
      throws Exception {
    assertEquals(exp, JsonStringConverter.toString(json, liberal, false));
  }

  /**
   * Checks if the given JSON string is correct and produces the given output with escaping
   * activated.
   * @param json JSON string
   * @param exp expected output
   * @throws Exception exception
   */
  private static void escape(final String json, final String exp) throws Exception {
    assertEquals(exp, JsonStringConverter.toString(json, false, true));
  }
}
