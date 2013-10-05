package org.basex.test;

import static org.junit.Assert.*;

import org.basex.build.JsonProp.*;
import org.basex.query.*;
import org.basex.query.util.json.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for the {@link JsonParser} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class JsonParserTest {
  /** Token builder for output checks. */
  private final TokenBuilder tb = new TokenBuilder();

  /** Tests if the empty JSON string is rejected. */
  public void emptyQuery() {
    error("", JsonSpec.ECMA_262);
    error(" \t\r\n", JsonSpec.ECMA_262);
  }

  /**
   * Tests for parsing numbers.
   * @throws QueryException query exception
   */
  @Test public void numberTest() throws QueryException {
    parse("0", JsonSpec.ECMA_262);
    parse("1", JsonSpec.ECMA_262);
    parse("-1", JsonSpec.ECMA_262);
    parse("10", JsonSpec.ECMA_262);
    parse("1234567890123456789012345678901234567890", JsonSpec.ECMA_262);
    parse("0.5", JsonSpec.ECMA_262);
    parse("0.01", JsonSpec.ECMA_262);
    parse("-0.01", JsonSpec.ECMA_262);
    parse("1234567890123456789012345678901234567890" +
        ".1234567890123456789012345678901234567890", JsonSpec.ECMA_262);
    parse("0E1", JsonSpec.ECMA_262);
    parse("0E-1", JsonSpec.ECMA_262);
    parse("0E+1", JsonSpec.ECMA_262);
    parse("-0E+1", JsonSpec.ECMA_262);
    parse("0E00", JsonSpec.ECMA_262);
    parse("1234567890123456789012345678901234567890" +
        "e1234567890123456789012345678901234567890", JsonSpec.ECMA_262);
    parse("123e-123", JsonSpec.ECMA_262);
    parse("123.4e-123", JsonSpec.ECMA_262);
    parse("123.456E0001", JsonSpec.ECMA_262);
    parse("-123.456E0001", JsonSpec.ECMA_262);
    parse("[ -123.456E0001, 0 ]", JsonSpec.ECMA_262);

    error("01", JsonSpec.ECMA_262);
    error("-", JsonSpec.ECMA_262);
    error("-\u00e4", JsonSpec.ECMA_262);
    error("0.", JsonSpec.ECMA_262);
    error("0.\u00e4", JsonSpec.ECMA_262);
    error("1e", JsonSpec.ECMA_262);
    error("1e+", JsonSpec.ECMA_262);
    error("1e+\u00e4", JsonSpec.ECMA_262);
    error("1e+0\u00e4", JsonSpec.ECMA_262);
  }

  /**
   * Tests for paring strings.
   * @throws QueryException query exception
   */
  @Test public void stringTest() throws QueryException {
    parse("\"\"", JsonSpec.ECMA_262);
    parse("\"test\"", JsonSpec.ECMA_262);
    parse("\"\u00e4\"", JsonSpec.ECMA_262);
    parse("\"\uD834\uDD1E\"", JsonSpec.ECMA_262);
    parse("\"\uD834\"", JsonSpec.ECMA_262);
    parse("\"\uD853\uDF5C\"", JsonSpec.ECMA_262);
    parse("\"\uD853\uFFFF\"", JsonSpec.ECMA_262);
    parse("\"\uFFFF\"", JsonSpec.ECMA_262);
    parse("\"\uD853a\"", JsonSpec.ECMA_262);
    parse("\"\\n\"", JsonSpec.ECMA_262);
    parse("\"\\b\\f\\t\\r\\n\"", JsonSpec.ECMA_262);
    unescape("\"\\b\\f\\t\\r\\n\"", "\"\\\\b\\\\f\\\\t\\\\r\\\\n\"");
    parse("\"\\u0000\\u001F\"", JsonSpec.ECMA_262);
    parse("\"\\\"\\\\\"", JsonSpec.ECMA_262);
    parse("\"\\u000a\"", "\"\\n\"", JsonSpec.ECMA_262);
    parse("\"\\u000A\"", "\"\\n\"", JsonSpec.ECMA_262);
    parse("\"\n\"", "\"\\n\"", JsonSpec.LIBERAL);
    unescape("\"\\uD853\\uDF5C\"", "\"\\\\uD853\\\\uDF5C\"");
    unescape("\"\\uD853asdf\"", "\"\\\\uD853asdf\"");
    unescape("\"\\uD853\"", "\"\\\\uD853\"");
    unescape("\"\uD853\\t\"", "\"\uD853\\\\t\"");
    unescape("\"\uD853\\uD853\\t\"", "\"\uD853\\\\uD853\\\\t\"");

    error("\"\\u0A", JsonSpec.ECMA_262);
    error("\"\\uXX0A\"", JsonSpec.ECMA_262);
    error("\"\\u0 00\"", JsonSpec.ECMA_262);
    error("\"\\u0:00\"", JsonSpec.ECMA_262);
    error("\"\\u0_00\"", JsonSpec.ECMA_262);
    error("\"\\u0~00\"", JsonSpec.ECMA_262);
    error("\"test", JsonSpec.ECMA_262);
    error("\"\uD800", JsonSpec.ECMA_262);
    error("\"\n\"", JsonSpec.ECMA_262);
  }

  /**
   * Tests for parsing arrays.
   * @throws QueryException query exception
   */
  @Test public void arrayTest() throws QueryException {
    parse("[ ]", JsonSpec.RFC4627);
    parse("[]", "[ ]", JsonSpec.RFC4627);
    parse("[[[[[[42], {}]]]]]", "[ [ [ [ [ [ 42 ], { } ] ] ] ] ]", JsonSpec.RFC4627);
    parse("[ 1, 2, 3, 4, 5, 6, 7, 8 ]", JsonSpec.RFC4627);
    parse("[1,2,3,]", "[ 1, 2, 3 ]", JsonSpec.LIBERAL);

    error("[1,2,3,]", JsonSpec.RFC4627);
    error("[,42]", JsonSpec.RFC4627);
    error("[1, ", JsonSpec.RFC4627);
  }

  /** Tests for the restrictions in RFC 4627. */
  @Test public void rfc4627() {
    error("\"test\"", JsonSpec.RFC4627);
    error("123", JsonSpec.RFC4627);
    error("true", JsonSpec.RFC4627);
    error("null", JsonSpec.RFC4627);
  }

  /**
   * Tests for parsing objects.
   * @throws QueryException query exception
   */
  @Test public void objectTest() throws QueryException {
    parse("{ }", JsonSpec.RFC4627);
    parse("{ \"\": 42 }", JsonSpec.RFC4627);
    parse("{ a : 42, b: 23 }", "{ \"a\": 42, \"b\": 23 }", JsonSpec.LIBERAL);
    parse("{ \"a\": 1, \"b\": 2, }", "{ \"a\": 1, \"b\": 2 }", JsonSpec.LIBERAL);

    error("{ a : 42 }", JsonSpec.RFC4627);
    error("{ \"a\": 42, b: 23 }", JsonSpec.RFC4627);
  }

  /**
   * Tests for parsing literals.
   * @throws QueryException query exception
   */
  @Test public void literals() throws QueryException {
    parse("true", JsonSpec.ECMA_262);
    parse("false", JsonSpec.ECMA_262);
    parse("null", JsonSpec.ECMA_262);
    parse("true", JsonSpec.LIBERAL);
    parse("false", JsonSpec.LIBERAL);
    parse("null", JsonSpec.LIBERAL);

    error("true123", JsonSpec.LIBERAL);
  }

  /**
   * Tests for parsing constructors.
   * @throws QueryException query exception
   */
  @Test public void constructorTest() throws QueryException {
    parse("new foo()", JsonSpec.LIBERAL);
    parse("new Test(1, { })", JsonSpec.LIBERAL);
    parse("new Foo([ { \"a\": new Test(1, { }) } ])", JsonSpec.LIBERAL);

    error("new ", JsonSpec.LIBERAL);
    error("newt", JsonSpec.LIBERAL);
  }

  /**
   * Tests if the given JSON string is rejected by the parser using the given spec.
   * @param json JSON string
   * @param spec specification
   */
  private void error(final String json, final JsonSpec spec) {
    try {
      parse(json, spec);
      fail("Should have failed: '" + json + "'");
    } catch(final QueryException qe) {
      // expected error
    }
  }

  /**
   * Checks if the given JSON string is correct and is reproduced by the parser.
   * @param json JSON string
   * @param spec specification
   * @throws QueryException parse error
   */
  private void parse(final String json, final JsonSpec spec) throws QueryException {
    parse(json, json, spec);
  }

  /**
   * Checks if the given JSON string is correct and produces the given output.
   * @param json JSON string
   * @param exp expected output
   * @param spec specification
   * @throws QueryException parse error
   */
  private void parse(final String json, final String exp, final JsonSpec spec)
      throws QueryException {
    tb.reset();
    JsonStringConverter.print(json, spec, true, tb);
    assertEquals(exp, tb.toString());
  }

  /**
   * Checks if the given JSON string is correct and produces the given output with the
   * {@code ECMA_262} spec and unescaping deactivated.
   * @param json JSON string
   * @param exp expected output
   * @throws QueryException parse error
   */
  private void unescape(final String json, final String exp) throws QueryException {
    tb.reset();
    JsonStringConverter.print(json, JsonSpec.ECMA_262, false, tb);
    assertEquals(exp, tb.toString());
  }
}
