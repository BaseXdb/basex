package org.basex.test;

import org.basex.query.*;
import org.basex.query.util.json.*;
import org.basex.query.util.json.JsonParser.*;
import org.basex.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

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
    error("", Spec.ECMA_262);
    error(" \t\r\n", Spec.ECMA_262);
  }

  /**
   * Tests for parsing numbers.
   * @throws QueryException query exception
   */
  @Test public void numberTest() throws QueryException {
    parse("0", Spec.ECMA_262);
    parse("1", Spec.ECMA_262);
    parse("-1", Spec.ECMA_262);
    parse("10", Spec.ECMA_262);
    parse("1234567890123456789012345678901234567890", Spec.ECMA_262);
    parse("0.5", Spec.ECMA_262);
    parse("0.01", Spec.ECMA_262);
    parse("-0.01", Spec.ECMA_262);
    parse("1234567890123456789012345678901234567890" +
        ".1234567890123456789012345678901234567890", Spec.ECMA_262);
    parse("0E1", Spec.ECMA_262);
    parse("0E-1", Spec.ECMA_262);
    parse("0E+1", Spec.ECMA_262);
    parse("-0E+1", Spec.ECMA_262);
    parse("0E00", Spec.ECMA_262);
    parse("1234567890123456789012345678901234567890" +
        "e1234567890123456789012345678901234567890", Spec.ECMA_262);
    parse("123e-123", Spec.ECMA_262);
    parse("123.4e-123", Spec.ECMA_262);
    parse("123.456E0001", Spec.ECMA_262);
    parse("-123.456E0001", Spec.ECMA_262);
    parse("[ -123.456E0001, 0 ]", Spec.ECMA_262);

    error("01", Spec.ECMA_262);
    error("-", Spec.ECMA_262);
    error("-\u00e4", Spec.ECMA_262);
    error("0.", Spec.ECMA_262);
    error("0.\u00e4", Spec.ECMA_262);
    error("1e", Spec.ECMA_262);
    error("1e+", Spec.ECMA_262);
    error("1e+\u00e4", Spec.ECMA_262);
    error("1e+0\u00e4", Spec.ECMA_262);
  }

  /**
   * Tests for paring strings.
   * @throws QueryException query exception
   */
  @Test public void stringTest() throws QueryException {
    parse("\"\"", Spec.ECMA_262);
    parse("\"test\"", Spec.ECMA_262);
    parse("\"\u00e4\"", Spec.ECMA_262);
    parse("\"\uD834\uDD1E\"", Spec.ECMA_262);
    parse("\"\uD834\"", Spec.ECMA_262);
    parse("\"\uD853\uDF5C\"", Spec.ECMA_262);
    parse("\"\uD853\uFFFF\"", Spec.ECMA_262);
    parse("\"\uFFFF\"", Spec.ECMA_262);
    parse("\"\uD853a\"", Spec.ECMA_262);
    parse("\"\\n\"", Spec.ECMA_262);
    parse("\"\\b\\f\\t\\r\\n\"", Spec.ECMA_262);
    unescape("\"\\b\\f\\t\\r\\n\"", "\"\\\\b\\\\f\\\\t\\\\r\\\\n\"");
    parse("\"\\u0000\\u001F\"", Spec.ECMA_262);
    parse("\"\\\"\\\\\"", Spec.ECMA_262);
    parse("\"\\u000a\"", "\"\\n\"", Spec.ECMA_262);
    parse("\"\\u000A\"", "\"\\n\"", Spec.ECMA_262);
    parse("\"\n\"", "\"\\n\"", Spec.LIBERAL);
    unescape("\"\\uD853\\uDF5C\"", "\"\\\\uD853\\\\uDF5C\"");
    unescape("\"\\uD853asdf\"", "\"\\\\uD853asdf\"");
    unescape("\"\\uD853\"", "\"\\\\uD853\"");
    unescape("\"\uD853\\t\"", "\"\uD853\\\\t\"");
    unescape("\"\uD853\\uD853\\t\"", "\"\uD853\\\\uD853\\\\t\"");

    error("\"\\u0A", Spec.ECMA_262);
    error("\"\\uXX0A\"", Spec.ECMA_262);
    error("\"\\u0 00\"", Spec.ECMA_262);
    error("\"\\u0:00\"", Spec.ECMA_262);
    error("\"\\u0_00\"", Spec.ECMA_262);
    error("\"\\u0~00\"", Spec.ECMA_262);
    error("\"test", Spec.ECMA_262);
    error("\"\uD800", Spec.ECMA_262);
    error("\"\n\"", Spec.ECMA_262);
  }

  /**
   * Tests for parsing arrays.
   * @throws QueryException query exception
   */
  @Test public void arrayTest() throws QueryException {
    parse("[ ]", Spec.RFC4627);
    parse("[]", "[ ]", Spec.RFC4627);
    parse("[[[[[[42], {}]]]]]", "[ [ [ [ [ [ 42 ], { } ] ] ] ] ]", Spec.RFC4627);
    parse("[ 1, 2, 3, 4, 5, 6, 7, 8 ]", Spec.RFC4627);
    parse("[1,2,3,]", "[ 1, 2, 3 ]", Spec.LIBERAL);

    error("[1,2,3,]", Spec.RFC4627);
    error("[,42]", Spec.RFC4627);
    error("[1, ", Spec.RFC4627);
  }

  /** Tests for the restrictions in RFC 4627. */
  @Test public void rfc4627() {
    error("\"test\"", Spec.RFC4627);
    error("123", Spec.RFC4627);
    error("true", Spec.RFC4627);
    error("null", Spec.RFC4627);
  }

  /**
   * Tests for parsing objects.
   * @throws QueryException query exception
   */
  @Test public void objectTest() throws QueryException {
    parse("{ }", Spec.RFC4627);
    parse("{ \"\": 42 }", Spec.RFC4627);
    parse("{ a : 42, b: 23 }", "{ \"a\": 42, \"b\": 23 }", Spec.LIBERAL);
    parse("{ \"a\": 1, \"b\": 2, }", "{ \"a\": 1, \"b\": 2 }", Spec.LIBERAL);

    error("{ a : 42 }", Spec.RFC4627);
    error("{ \"a\": 42, b: 23 }", Spec.RFC4627);
  }

  /**
   * Tests for parsing literals.
   * @throws QueryException query exception
   */
  @Test public void literals() throws QueryException {
    parse("true", Spec.ECMA_262);
    parse("false", Spec.ECMA_262);
    parse("null", Spec.ECMA_262);
    parse("true", Spec.LIBERAL);
    parse("false", Spec.LIBERAL);
    parse("null", Spec.LIBERAL);

    error("true123", Spec.LIBERAL);
  }

  /**
   * Tests for parsing constructors.
   * @throws QueryException query exception
   */
  @Test public void constructorTest() throws QueryException {
    parse("new foo()", Spec.LIBERAL);
    parse("new Test(1, { })", Spec.LIBERAL);
    parse("new Foo([ { \"a\": new Test(1, { }) } ])", Spec.LIBERAL);

    error("new ", Spec.LIBERAL);
    error("newt", Spec.LIBERAL);
  }

  /**
   * Tests if the given JSON string is rejected by the parser using the given spec.
   * @param json JSON string
   * @param spec specification
   */
  private void error(final String json, final Spec spec) {
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
  private void parse(final String json, final Spec spec) throws QueryException {
    parse(json, json, spec);
  }

  /**
   * Checks if the given JSON string is correct and produces the given output.
   * @param json JSON string
   * @param exp expected output
   * @param spec specification
   * @throws QueryException parse error
   */
  private void parse(final String json, final String exp, final Spec spec)
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
    JsonStringConverter.print(json, Spec.ECMA_262, false, tb);
    assertEquals(exp, tb.toString());
  }
}
