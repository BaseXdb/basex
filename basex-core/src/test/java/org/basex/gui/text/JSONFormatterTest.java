package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the code {@link Formatter}, driven by the JSON syntax.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JSONFormatterTest {
  /** Indentation of a single level. */
  private static final byte[] SPACES = Token.token("  ");
  /** Line margin. */
  private static final int MARGIN = 80;

  /** Objects and arrays are indented by their nesting depth. */
  @Test public void indent() {
    format("{\n\"a\": 1,\n\"b\": [\n1,\n2\n]\n}",
        "{\n  \"a\": 1,\n  \"b\": [\n    1,\n    2\n  ]\n}");
    // wrong indentation is corrected
    format("[\n      1\n]", "[\n  1\n]");
    // the bracket of an object that spans several lines starts a new line
    format("{\n \"a\":       1,\n    \"b\":\"c\"}", "{\n  \"a\": 1,\n  \"b\": \"c\"\n}");
    // compact data is left alone
    format("{ \"a\": [ 1, 2 ] }", "{ \"a\": [ 1, 2 ] }");
  }

  /** If an object or array is broken, all its values are placed on separate lines. */
  @Test public void lists() {
    format("{\"a\": 1,\n\"b\": 2, \"c\": 3}", "{\n  \"a\": 1,\n  \"b\": 2,\n  \"c\": 3\n}");
    // values that fit into a line are left alone, even in a broken object
    format("{\n\"a\": {\"b\": 1, \"c\": 2}\n}", "{\n  \"a\": {\"b\": 1, \"c\": 2}\n}");
  }

  /** Whitespace is collapsed, separators are followed by a single space. */
  @Test public void whitespace() {
    format("{\"a\" :       1}", "{\"a\": 1}");
    format("{\"b\":{\"name\":\"Anon\"}}", "{\"b\": {\"name\": \"Anon\"}}");
    format("[1 ,2]", "[1, 2]");
    // empty brackets are collapsed
    format("{\"a\": { }, \"b\": [ ]}", "{\"a\": {}, \"b\": []}");
    // whitespace in strings is untouched
    format("[\"a  b\"]", "[\"a  b\"]");
  }

  /** Brackets in strings are no code. */
  @Test public void code() {
    format("[\"[\"]", "[\"[\"]");
    // escaped quotes do not close a string
    format("[\"\\\"[\"]", "[\"\\\"[\"]");
  }

  /** Objects and arrays that exceed the line margin are wrapped. */
  @Test public void wrap() {
    final String string = "\"" + "a".repeat(80) + "\"";
    format("[" + string + "]", "[\n  " + string + "\n]");
    format("[1, 2]", "[1, 2]");
    // the values of a wrapped array are placed on separate lines
    format("[" + string + ", 1, 2]", "[\n  " + string + ",\n  1,\n  2\n]");
  }

  /**
   * Compares formatted JSON with the expected result, and checks that formatting is idempotent.
   * @param json JSON string
   * @param expected expected result
   */
  private static void format(final String json, final String expected) {
    final Syntax syntax = new SyntaxJSON();
    final byte[] formatted = syntax.format(Token.token(json), SPACES, MARGIN);
    assertEquals(expected, Token.string(formatted), json);
    assertEquals(expected, Token.string(syntax.format(formatted, SPACES, MARGIN)),
      "reformatted: " + json);
  }
}
