package org.basex.io.parse.json;

import static org.basex.build.JsonOptions.JsonSpec.*;
import static org.junit.Assert.*;

import org.basex.build.JsonOptions.JsonSpec;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for the {@link JsonParser} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class JsonParserTest {
  /** Token builder for output checks. */
  private final TokenBuilder tb = new TokenBuilder();

  /** Tests if the empty JSON string is rejected. */
  public void emptyQuery() {
    error("", ECMA_262);
    error(" \t\r\n", ECMA_262);
  }

  /**
   * Tests for parsing numbers.
   * @throws QueryIOException query I/O exception
   */
  @Test public void numberTest() throws QueryIOException {
    parse("0", ECMA_262);
    parse("1", ECMA_262);
    parse("-1", ECMA_262);
    parse("10", ECMA_262);
    parse("1234567890123456789012345678901234567890", ECMA_262);
    parse("0.5", ECMA_262);
    parse("0.01", ECMA_262);
    parse("-0.01", ECMA_262);
    parse("1234567890123456789012345678901234567890" +
        ".1234567890123456789012345678901234567890", ECMA_262);
    parse("0E1", ECMA_262);
    parse("0E-1", ECMA_262);
    parse("0E+1", ECMA_262);
    parse("-0E+1", ECMA_262);
    parse("0E00", ECMA_262);
    parse("1234567890123456789012345678901234567890" +
        "e1234567890123456789012345678901234567890", ECMA_262);
    parse("123e-123", ECMA_262);
    parse("123.4e-123", ECMA_262);
    parse("123.456E0001", ECMA_262);
    parse("-123.456E0001", ECMA_262);
    parse("[ -123.456E0001, 0 ]", ECMA_262);

    error("01", ECMA_262);
    error("-", ECMA_262);
    error("-\u00e4", ECMA_262);
    error("0.", ECMA_262);
    error("0.\u00e4", ECMA_262);
    error("1e", ECMA_262);
    error("1e+", ECMA_262);
    error("1e+\u00e4", ECMA_262);
    error("1e+0\u00e4", ECMA_262);
  }

  /**
   * Tests for paring strings.
   * @throws QueryIOException query I/O exception
   */
  @Test public void stringTest() throws QueryIOException {
    parse("\"\"", ECMA_262);
    parse("\"test\"", ECMA_262);
    parse("\"\u00e4\"", ECMA_262);
    parse("\"\uD834\uDD1E\"", ECMA_262);
    parse("\"\uD834\"", ECMA_262);
    parse("\"\uD853\uDF5C\"", ECMA_262);
    parse("\"\uD853\uFFFF\"", ECMA_262);
    parse("\"\uFFFF\"", ECMA_262);
    parse("\"\uD853a\"", ECMA_262);
    parse("\"\\n\"", ECMA_262);
    parse("\"\\b\\f\\t\\r\\n\"", ECMA_262);
    parse("\"\\u0000\\u001F\"", ECMA_262);
    parse("\"\\\"\\\\\"", ECMA_262);
    parse("\"\\u000a\"", "\"\\n\"", ECMA_262);
    parse("\"\\u000A\"", "\"\\n\"", ECMA_262);
    parse("\"\n\"", "\"\\n\"", LIBERAL);

    unescape("\"\\b\\f\\t\\r\\n\"", "\"\\\\b\\\\f\\\\t\\\\r\\\\n\"");
    unescape("\"\\uD853\\uDF5C\"", "\"\\\\uD853\\\\uDF5C\"");
    unescape("\"\\uD853asdf\"", "\"\\\\uD853asdf\"");
    unescape("\"\\uD853\"", "\"\\\\uD853\"");
    unescape("\"\uD853\\t\"", "\"\uD853\\\\t\"");
    unescape("\"\uD853\\uD853\\t\"", "\"\uD853\\\\uD853\\\\t\"");

    error("\"\\u0A", ECMA_262);
    error("\"\\uXX0A\"", ECMA_262);
    error("\"\\u0 00\"", ECMA_262);
    error("\"\\u0:00\"", ECMA_262);
    error("\"\\u0_00\"", ECMA_262);
    error("\"\\u0~00\"", ECMA_262);
    error("\"test", ECMA_262);
    error("\"\uD800", ECMA_262);
    error("\"\n\"", ECMA_262);
  }

  /**
   * Tests for parsing arrays.
   * @throws QueryIOException query I/O exception
   */
  @Test public void arrayTest() throws QueryIOException {
    parse("[ ]", RFC4627);
    parse("[]", "[ ]", RFC4627);
    parse("[[[[[[42], {}]]]]]", "[ [ [ [ [ [ 42 ], { } ] ] ] ] ]", RFC4627);
    parse("[ 1, 2, 3, 4, 5, 6, 7, 8 ]", RFC4627);
    parse("[1,2,3,]", "[ 1, 2, 3 ]", LIBERAL);

    error("[1,2,3,]", RFC4627);
    error("[,42]", RFC4627);
    error("[1,", RFC4627);
  }

  /** Tests for the restrictions in RFC 4627. */
  @Test public void rfc4627() {
    error("\"test\"", RFC4627);
    error("123", RFC4627);
    error("true", RFC4627);
    error("null", RFC4627);
  }

  /**
   * Tests for parsing objects.
   * @throws QueryIOException query I/O exception
   */
  @Test public void objectTest() throws QueryIOException {
    parse("{ }", RFC4627);
    parse("{ \"\": 42 }", RFC4627);
    parse("{ a : 42, b: 23 }", "{ \"a\": 42, \"b\": 23 }", LIBERAL);
    parse("{ \"a\": 1, \"b\": 2, }", "{ \"a\": 1, \"b\": 2 }", LIBERAL);

    error("{ a : 42 }", RFC4627);
    error("{ \"a\": 42, b: 23 }", RFC4627);
  }

  /**
   * Tests for parsing literals.
   * @throws QueryIOException query I/O exception
   */
  @Test public void literals() throws QueryIOException {
    parse("true", ECMA_262);
    parse("false", ECMA_262);
    parse("null", ECMA_262);
    parse("true", LIBERAL);
    parse("false", LIBERAL);
    parse("null", LIBERAL);

    error("true123", LIBERAL);
  }

  /**
   * Tests for parsing constructors.
   * @throws QueryIOException query I/O exception
   */
  @Test public void constructorTest() throws QueryIOException {
    parse("new foo()", LIBERAL);
    parse("new Test(1, { })", LIBERAL);
    parse("new Foo([ { \"a\": new Test(1, { }) } ])", LIBERAL);

    error("new" , LIBERAL);
    error("newt", LIBERAL);
  }

  /**
   * Tests if the given JSON string is rejected by the parser using the given spec.
   * @param json JSON string
   * @param spec specification
   */
  private void error(final String json, final JsonSpec spec) {
    try {
      parse(json, spec);
      fail("Should have failed: '" + json + '\'');
    } catch(final QueryIOException qe) {
      // expected error
    }
  }

  /**
   * Checks if the given JSON string is correct and is reproduced by the parser.
   * @param json JSON string
   * @param spec specification
   * @throws QueryIOException parse error
   */
  private void parse(final String json, final JsonSpec spec) throws QueryIOException {
    parse(json, json, spec);
  }

  /**
   * Checks if the given JSON string is correct and produces the given output.
   * @param json JSON string
   * @param exp expected output
   * @param spec specification
   * @throws QueryIOException parse error
   */
  private void parse(final String json, final String exp, final JsonSpec spec)
      throws QueryIOException {
    tb.reset();
    JsonStringConverter.print(json, spec, true, tb);
    assertEquals(exp, tb.toString());
  }

  /**
   * Checks if the given JSON string is correct and produces the given output with the
   * {@code ECMA_262} spec and unescaping deactivated.
   * @param json JSON string
   * @param exp expected output
   * @throws QueryIOException parse error
   */
  private void unescape(final String json, final String exp) throws QueryIOException {
    tb.reset();
    JsonStringConverter.print(json, ECMA_262, false, tb);
    assertEquals(exp, tb.toString());
  }
}
