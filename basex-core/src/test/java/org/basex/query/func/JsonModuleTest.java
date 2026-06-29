package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the JSON Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void doc() {
    final Function func = _JSON_DOC;
    query(func.args(" ()"), "");
    query(func.args(" []"), "");
    query(func.args(" <_/>/text()"), "");

    final String path = "src/test/resources/example.json";
    query(func.args(path) + "//name ! string()", "Smith");
    query(func.args(path, " { 'format': 'xquery' }") + "?name", "Smith");
  }

  /** Test method. */
  @Test public void parseXml() {
    // default output
    parse(" ()", "", "");
    parse(" []", "", "");
    parse("[]", "", "<json type=\"array\"/>");
    parse("{}", "", "<json type=\"object\"/>");
    parse("{ \"\\t\" : 0 }", "",
        "<json type=\"object\"><_0009 type=\"number\">0</_0009></json>");
    parse("{ \"a\" :0 }", "", "<json type=\"object\"><a type=\"number\">0</a></json>");
    parse("{ \"\" : 0 }", "", "<json type=\"object\"><_ type=\"number\">0</_></json>");
    parse("{ \"\" : 0.0e0 }", "", "...<_ type=\"number\">0.0e0</_>");
    parse("{ \"\" : null }", "", "...<_ type=\"null\"/>");
    parse("{ \"\" : true }", "", "...<_ type=\"boolean\">true</_>");
    parse("{ \"\" : {} }", "", "... type=\"object\"><_ type=\"object\"/>");
    parse("{ \"\" : [] }", "", "... type=\"object\"><_ type=\"array\"/>");
    parse("{ \"A\" : 0, \"B\": 1 }", "",
        "... type=\"object\"><A type=\"number\">0</A><B type=\"number\">1</B>");
    parse("{ \"O\" : [ 1 ] }", "", "...<O type=\"array\"><_ type=\"number\">1</_></O>");
    parse("{ \"A\" : [ 0, 1 ] }", "",
        "...<A type=\"array\"><_ type=\"number\">0</_><_ type=\"number\">1</_>");
    parse("{ \"\" : 0.0 }", "", "...>0.0<");

    // merging data types
    parse("[]", "'merge': true()", "<json arrays=\"json\"/>");
    parse("{}", "'merge': true()", "<json objects=\"json\"/>");
    parse("{ \"\\t\" : 0 }", "'merge': true()",
        "<json objects=\"json\" numbers=\"_0009\"><_0009>0</_0009></json>");
    parse("{ \"a\" :0 }", "'merge': true()",
        "<json objects=\"json\" numbers=\"a\"><a>0</a></json>");
    parse("{ \"\" : 0 }", "'merge': true()",
        "<json objects=\"json\" numbers=\"_\"><_>0</_></json>");
    parse("{ \"\" : 0.0e0 }", "'merge': true()", "...<_>0.0e0</_>");
    parse("{ \"\" : null }", "'merge': true()", "...<_/>");
    parse("{ \"\" : true }", "'merge': true()", "...<_>true</_>");
    parse("{ \"\" : {} }", "'merge': true()", "... objects=\"json _\"><_/>");
    parse("{ \"\" : [] }", "'merge': true()", "... objects=\"json\" arrays=\"_\"><_/>");
    parse("{ \"A\" : 0, \"B\": 1 }", "'merge': true()",
        "... objects=\"json\" numbers=\"A B\"><A>0</A><B>1</B>");
    parse("{ \"O\" : [ 1 ] }", "'merge': true()",
        "... objects=\"json\" arrays=\"O\" numbers=\"_\"><O><_>1</_></O>");
    parse("{ \"A\" : [ 0, 1 ] }", "'merge': true()",
        "... objects=\"json\" arrays=\"A\" numbers=\"_\"><A><_>0</_><_>1</_>");

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
    parseError("{ \"a\" : 0, }", "'liberal': false()");
  }

  /** Duplicate keys, 'use-first' policy: the suppressed value and its nested content is dropped. */
  @Test public void parseDuplicates() {
    final String first = "<json type=\"object\"><x type=\"number\">1</x></json>";
    final String opt = "'duplicates': 'use-first'";
    // scalar duplicate
    parse("{ \"x\": 1, \"x\": 2 }", opt, first);
    // object duplicate (and its nested pairs) must not leak through
    parse("{ \"x\": 1, \"x\": { \"y\": 2 } }", opt, first);
    // array duplicate (and its items) must not leak through
    parse("{ \"x\": 1, \"x\": [ 1, 2 ] }", opt, first);
    // deeply nested duplicate value
    parse("{ \"x\": 1, \"x\": { \"y\": { \"z\": 2 } } }", opt, first);
    // duplicate within the suppressed value
    parse("{ \"x\": 1, \"x\": { \"y\": 1, \"y\": 2 } }", opt, first);
    // a valid container sibling after a suppressed duplicate is still emitted
    parse("{ \"x\": 1, \"x\": { \"y\": 2 }, \"z\": { \"w\": 3 } }", opt,
        "<json type=\"object\"><x type=\"number\">1</x><z type=\"object\">" +
        "<w type=\"number\">3</w></z></json>");

    // same suppression for the 'attributes' format
    final String attsOpt = opt + ", 'format': 'attributes'";
    final String attsFirst =
        "<json type=\"object\"><pair name=\"x\" type=\"number\">1</pair></json>";
    parse("{ \"x\": 1, \"x\": { \"y\": 2 } }", attsOpt, attsFirst);
    parse("{ \"x\": 1, \"x\": [ 1, 2 ] }", attsOpt, attsFirst);

    // same suppression for the 'basic' (W3 XML) format, as used by fn:json-to-xml
    final String basicOpt = opt + ", 'format': 'basic'";
    final String basicFirst = "<map xmlns=\"http://www.w3.org/2005/xpath-functions\">" +
        "<number key=\"x\">1</number></map>";
    parse("{ \"x\": 1, \"x\": { \"y\": 2 } }", basicOpt, basicFirst);
    parse("{ \"x\": 1, \"x\": [ 1, 2 ] }", basicOpt, basicFirst);
  }

  /** Coverage for every value of the 'duplicates' option and its per-format defaults. */
  @Test public void parseDuplicatesPolicies() {
    final Function func = _JSON_PARSE;
    final String in = "{ \"x\": 1, \"x\": 2 }";

    // tree formats (direct): reject errors, use-first keeps the first, use-last is unsupported,
    // retain keeps both; the default for non-W3 formats is 'use-first'
    final String one = "<json type=\"object\"><x type=\"number\">1</x></json>";
    final String two =
        "<json type=\"object\"><x type=\"number\">1</x><x type=\"number\">2</x></json>";
    error(func.args(in, " { 'duplicates': 'reject' }"), JSON_PARSE_X);
    query(func.args(in, " { 'duplicates': 'use-first' }"), one);
    error(func.args(in, " { 'duplicates': 'use-last' }"), JSON_OPTIONS_X);
    query(func.args(in, " { 'duplicates': 'retain' }"), two);
    query(func.args(in), one);

    // xquery format (maps): reject errors, use-first/use-last select a value, retain is unsupported
    error(func.args(in, " { 'format': 'xquery', 'duplicates': 'reject' }"), JSON_PARSE_X);
    query(func.args(in, " { 'format': 'xquery', 'duplicates': 'use-first' }"), "{\"x\":1}");
    query(func.args(in, " { 'format': 'xquery', 'duplicates': 'use-last' }"), "{\"x\":2}");
    error(func.args(in, " { 'format': 'xquery', 'duplicates': 'retain' }"), JSON_OPTIONS_X);

    // W3 XML format (basic): the default is 'retain' (both kept), use-last is unsupported
    query(func.args(in, " { 'format': 'basic' }"), "<map xmlns=\"http://www.w3.org/2005/" +
        "xpath-functions\"><number key=\"x\">1</number><number key=\"x\">2</number></map>");
    error(func.args(in, " { 'format': 'basic', 'duplicates': 'use-last' }"), JSON_OPTIONS_X);

    // jsonml rejects duplicate object keys regardless of the chosen policy
    error(func.args("[ \"a\", { \"b\": \"1\", \"b\": \"2\" } ]",
        " { 'format': 'jsonml', 'duplicates': 'retain' }"), JSON_PARSE_X);

    // the wrapped json:* error messages must stay intact (no truncation to '1' or 'duplicates')
    final String dup = "{\"x\":1,\"x\":2}";
    query("try { " + func.args(dup, " { 'duplicates': 'reject' }") +
        " } catch * { $err:description }", "(1:11): Key \"x\" occurs more than once.");
    query("try { " + func.args(dup, " { 'duplicates': 'use-last' }") +
        " } catch * { $err:description }",
        "'duplicates':'use-last' is not supported by the target format.");
  }

  /** Test method. */
  @Test public void parseJsonML() {
    parse("[ \"a\" ]", "'format': 'jsonml'", "<a/>");
    parse("[ \"a\", \"A\" ]", "'format': 'jsonml'", "<a>A</a>");
    parse("[ \"a\", { \"b\": \"c\" } ]", "'format': 'jsonml'", "<a b=\"c\"/>");
    parse("[ \"a\", { \"b\": \"\", \"c\": \"\" } ]", "'format': 'jsonml'", "<a b=\"\" c=\"\"/>");

    // duplicate attribute
    parseError("[ \"a\", { \"b\": \"\", \"b\": \"\" } ]", "'format': 'jsonml'");
  }

  /** Test method. */
  @Test public void parseXQuery() {
    final Function func = _JSON_PARSE;
    // queries
    String options = " { 'format': 'xquery' }";
    query(func.args("{}", options), "{}");
    query(func.args("{\"A\":1}", options), "{\"A\":1}");
    query(func.args("{\"\":null}", options), "{\"\":()}");

    query(func.args("[]", options), "[]");
    query(func.args("[\"A\"]", options), "[\"A\"]");
    query(func.args("[1,true]", options), "[1,true()]");

    query(func.args("1", options), 1);
    query(func.args("\"f\"", options), "f");
    query(func.args("false", options), false);
    query(func.args("null", options), "");

    query(func.args("1234567890123456789012345678901234567890", options), "1.2345678901234568e+39");
    query(func.args("1234567890123456789012345678901234567890" +
        ".123456789012345678901234567890123456789", options), "1.2345678901234568e+39");
    query(func.args("1234567890123456789012345678901234567890" +
        "e1234567890123456789012345678901234567890", options), "INF");
    query(func.args("0E1", options), 0);
    query(func.args("0E-1", options), 0);
    query(func.args("0E+1", options), 0);
    query(func.args("-0E+1", options), "-0");
    query(func.args("0E00", options), 0);
    query(func.args("123e-123", options), "1.23e-121");
    query(func.args("123.4e-123", options), "1.234e-121");
    query(func.args("123.456E0001", options), "1234.56");
    query(func.args("-123.456E0001", options), "-1234.56");
    query(func.args("[ -123.456E0001, 0 ]", options), "[-1234.56,0]");

    options = " { 'format': 'xquery', 'number-format': 'decimal' }";
    String input = "1234567890123456789012345678901234567890";
    query(func.args(input, options), input);
    input = "1234567890123456789012345678901234567890.123456789012345678901234567890123456789";
    query(func.args(input, options), input);
  }

  /** Tests the configuration argument of {@code json:parse(...)}. */
  @Test public void parseConfig() {
    final Function func = _JSON_PARSE;
    // queries
    query(func.args("[\"A\",{\"B\":\"C\"}]",
        " { 'format': 'jsonml' }"),
        "<A B=\"C\"/>");
    query("array:size(" + func.args("[\"A\",{\"B\":\"C\"}]",
        " { 'format': 'xquery' }") + ')',
        2);
    query(func.args("\"\\t\\u000A\"",
        " { 'format': 'xquery', 'escape': true(), 'liberal': true() }"),
        "\\t\\n");
    query("string-to-codepoints(" + func.args("\"\\t\\u000A\"",
        " { 'format': 'xquery', 'escape': false(), 'liberal': true() }") + ')',
        "9\n10");

    error(func.args("42", " { 'spec': 'garbage' }"), INVALIDOPTION_X);

    // escape cannot be combined with a fallback function; the json:options message must pass
    // through the wrapping intact (regression for the JSON_OPTIONS_X template)
    final String escFb = " { 'escape': true(), 'fallback': fn($s) { $s } }";
    error(func.args("\"x\"", escFb), JSON_OPTIONS_X);
    query("try { " + func.args("\"x\"", escFb) + " } catch * { $err:description }",
        "Escape cannot be combined with fallback function.");
  }

  /** Test method. */
  @Test public void serialize() {
    serial("<json type='object'/>", "", "{}");
    serial("<json objects='json'/>", "", "{}");
    serial("<json type='array'/>", "", "[]");
    serial("<json arrays='json'/>", "", "[]");
    serial("<json type='number'>1</json>", "", 1);
    serial("<json type='array'><_ type='null'/></json>", "", "[null]");
    serial("<json type='array'><_ type='string'/></json>", "", "[\"\"]");
    serial("<json type='array'><_ type='string'>x</_></json>", "", "[\"x\"]");
    serial("<json type='array'><_ type='number'>1</_></json>", "", "[1]");
    serial("<json numbers=\"_\" type='array'><_>1</_></json>", "", "[1]");

    serialError("<json type='o'/>", ""); // invalid type
    serialError("<json type='array'><_ type='number'/></json>", ""); // value needed
    serialError("<json type='array'><_ type='boolean'/></json>", ""); // value needed
    serialError("<json type='array'><_ type='null'>x</_></json>", ""); // no value
  }

  /** Bidirectional tests. */
  @Test public void serializeParse() {
    query("json:serialize(<x xmlns='X'>{ json:parse('{}') }</x>/*)", "{}");
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private static void parse(final String input, final String options, final Object expected) {
    query(input, options, expected, _JSON_PARSE);
  }

  /**
   * Runs the specified query.
   * @param input query input
   * @param options options
   * @param expected expected result
   */
  private static void serial(final String input, final String options, final Object expected) {
    query(' ' + input, options, expected, _JSON_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param expected expected result
   * @param function function
   */
  private static void query(final String input, final String options, final Object expected,
      final Function function) {

    final String query = options.isEmpty() ? function.args(input) :
      function.args(input, " { " + options + " }");
    final String exp = expected.toString();
    if(exp.startsWith("...")) {
      contains(query, exp.substring(3));
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
    error(' ' + input, options, _JSON_SERIALIZE);
  }

  /**
   * Tests a query which yields an error.
   * @param input query input
   * @param options options
   * @param function function
   */
  private static void error(final String input, final String options, final Function function) {
    final String query = function.args(input, " { " + options + " }");
    error(query, INVALIDOPTION_X, JSON_PARSE_X, PARSE_JSON_X_X_X, JSON_SERIALIZE_X);
  }
}
