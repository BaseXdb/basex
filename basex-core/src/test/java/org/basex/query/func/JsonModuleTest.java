package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the JSON Module.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class JsonModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void parseXml() {
    // default output
    parse("[]", "", "<json type=\"array\"/>");
    parse("{}", "", "<json type=\"object\"/>");
    parse("{ } ", "", "<json type=\"object\"/>");
    parse("{ \"\\t\" : 0 }", "",
        "<json type=\"object\">\n<_0009 type=\"number\">0</_0009>\n</json>");
    parse("{ \"a\" :0 }", "", "<json type=\"object\">\n<a type=\"number\">0</a>\n</json>");
    parse("{ \"\" : 0 }", "", "<json type=\"object\">\n<_ type=\"number\">0</_>\n</json>");
    parse("{ \"\" : 0.0e0 }", "", "...<_ type=\"number\">0.0e0</_>");
    parse("{ \"\" : null }", "", "...<_ type=\"null\"/>");
    parse("{ \"\" : true }", "", "...<_ type=\"boolean\">true</_>");
    parse("{ \"\" : {} }", "", "... type=\"object\">\n<_ type=\"object\"/>");
    parse("{ \"\" : [] }", "", "... type=\"object\">\n<_ type=\"array\"/>");
    parse("{ \"\" : 0, \"\": 1 }", "",
        "... type=\"object\">\n<_ type=\"number\">0</_>\n<_ type=\"number\">1</_>");
    parse("{ \"O\" : [ 1 ] }", "", "...<O type=\"array\">\n<_ type=\"number\">1</_>\n</O>");
    parse("{ \"A\" : [ 0,1 ] }", "",
        "...<A type=\"array\">\n<_ type=\"number\">0</_>\n<_ type=\"number\">1</_>");
    parse("{ \"\" : 0.0 }", "", "...0.0");

    // merging data types
    parse("[]", "'merge':true()", "<json arrays=\"json\"/>");
    parse("{}", "'merge':true()", "<json objects=\"json\"/>");
    parse("{ } ", "'merge':true()", "<json objects=\"json\"/>");
    parse("{ \"\\t\" : 0 }", "'merge':true()",
        "<json objects=\"json\" numbers=\"_0009\">\n<_0009>0</_0009>\n</json>");
    parse("{ \"a\" :0 }", "'merge':true()",
        "<json objects=\"json\" numbers=\"a\">\n<a>0</a>\n</json>");
    parse("{ \"\" : 0 }", "'merge':true()",
        "<json objects=\"json\" numbers=\"_\">\n<_>0</_>\n</json>");
    parse("{ \"\" : 0.0e0 }", "'merge':true()", "...<_>0.0e0</_>");
    parse("{ \"\" : null }", "'merge':true()", "...<_/>");
    parse("{ \"\" : true }", "'merge':true()", "...<_>true</_>");
    parse("{ \"\" : {} }", "'merge':true()", "... objects=\"json _\">\n<_/>");
    parse("{ \"\" : [] }", "'merge':true()", "... objects=\"json\" arrays=\"_\">\n<_/>");
    parse("{ \"\" : 0, \"\": 1 }", "'merge':true()",
        "... objects=\"json\" numbers=\"_\">\n<_>0</_>\n<_>1</_>");
    parse("{ \"O\" : [ 1 ] }", "'merge':true()",
        "... objects=\"json\" arrays=\"O\" numbers=\"_\">\n<O>\n<_>1</_>\n</O>");
    parse("{ \"A\" : [ 0,1 ] }", "'merge':true()",
        "... objects=\"json\" arrays=\"A\" numbers=\"_\">\n<A>\n<_>0</_>\n<_>1</_>");

    // errors
    parseError("", "");
    parseError("{", "");
    parseError("{ \"", "");
    parseError("{ \"\" : 00 }", "");
    parseError("{ \"\" : 0. }", "");
    parseError("{ \"\\c\" : 0 }", "");
    parseError("{ \"\" : 0e }", "");
    parseError("{ \"\" : 0.1. }", "");
    parseError("{ \"\" : 0.1e }", "");
    parseError("{ \"a\" : 0 }}", "");
    parseError("{ \"a\" : 0, }", "'liberal':false()");
  }

  /** Test method. */
  @Test
  public void parseMap() {
    final String map = " map { 'format':'map' }";
    query(_MAP_SERIALIZE.args(_JSON_PARSE.args("{}", map)), "{\n}");
    query(_MAP_SERIALIZE.args(_JSON_PARSE.args("{\"A\":1}", map)), "{\n\"A\": 1\n}");
    query(_MAP_SERIALIZE.args(_JSON_PARSE.args("{\"\":null}", map)), "{\n\"\": ()\n}");

    query(_ARRAY_SERIALIZE.args(_JSON_PARSE.args("[]", map)), "[]");
    query(_ARRAY_SERIALIZE.args(_JSON_PARSE.args("[\"A\"]", map)), "[\"A\"]");
    query(_ARRAY_SERIALIZE.args(_JSON_PARSE.args("[1,true]", map)), "[1, true()]");

    query(_JSON_PARSE.args("1", map), "1");
    query(_JSON_PARSE.args("\"\"\"f\"\"\"", map), "f");
    query(_JSON_PARSE.args("false", map), "false");
    query(_JSON_PARSE.args("null", map), "");
  }

  /** Test method. */
  @Test
  public void serialize() {
    serial("<json type='object'/>", "", "{\n}");
    serial("<json objects='json'/>", "", "{\n}");
    serial("<json type='array'/>", "", "[\n]");
    serial("<json arrays='json'/>", "", "[\n]");
    serial("<json type='number'>1</json>", "", "1");
    serial("<json type='array'><_ type='null'/></json>", "", "[\nnull\n]");
    serial("<json type='array'><_ type='string'/></json>", "", "[\n\"\"\n]");
    serial("<json type='array'><_ type='string'>x</_></json>", "", "[\n\"x\"\n]");
    serial("<json type='array'><_ type='number'>1</_></json>", "", "[\n1\n]");
    serial("<json numbers=\"_\" type='array'><_>1</_></json>", "", "[\n1\n]");

    serialError("<json type='o'/>", ""); // invalid type
    serialError("<json type='array'><_ type='number'/></json>", ""); // value needed
    serialError("<json type='array'><_ type='boolean'/></json>", ""); // value needed
    serialError("<json type='array'><_ type='null'>x</_></json>", ""); // no value
  }

  /** Test method with namespaces. */
  @Test public void ns() {
    query("json:serialize(<x xmlns='X'>{ json:parse('{}') }</x>/*)", "{\n}");
  }

  /** Tests the configuration argument of {@code json:parse(...)}. */
  @Test public void config() {
    query("json:parse('[\"foo\",{\"test\":\"asdf\"}]', map {'format':'jsonml'})",
        "<foo test=\"asdf\"/>");
    query("array:size(json:parse('[\"foo\",{\"test\":\"asdf\"}]', map {'format':'map'}))",
        "2");
    query("json:parse('\"\\t\\u000A\"', map {'format':'map','unescape':false(),'liberal':true()})",
        "\\t\\u000A");
    query("string-to-codepoints(json:parse('\"\\t\\u000A\"'," +
        "  map {'format':'map','unescape':true(),'liberal':true()}))", "9\n10");
    error("json:parse('42', map {'spec':'garbage'})", INVALIDOPT_X);
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private static void parse(final String input, final String options, final String expected) {
    query(input, options, expected, _JSON_PARSE);
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private static void serial(final String input, final String options, final String expected) {
    query(input, options, expected, _JSON_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param expected expected result
   * @param function function
   */
  private static void query(final String input, final String options, final String expected,
      final Function function) {

    final String query = options.isEmpty() ? function.args(input) :
      function.args(input, " map {" + options + '}');
    if(expected.startsWith("...")) {
      contains(query, expected.substring(3));
    } else {
      query(query, expected);
    }
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   */
  private static void parseError(final String input, final String options) {
    error(input, options, _JSON_PARSE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   */
  private static void serialError(final String input, final String options) {
    error(input, options, _JSON_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param function function
   */
  private static void error(final String input, final String options, final Function function) {
    final String query = options.isEmpty() ? function.args(input) :
      function.args(input, " map {" + options + '}');
    error(query, INVALIDOPT_X, BXJS_PARSE_X_X_X, BXJS_SERIAL_X);
  }
}
