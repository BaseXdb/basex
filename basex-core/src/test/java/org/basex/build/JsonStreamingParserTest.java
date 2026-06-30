package org.basex.build;

import static org.basex.build.json.JsonOptions.JsonFormat.*;
import static org.basex.query.QueryError.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.build.json.*;
import org.basex.build.json.JsonParser;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JsonStreamingParser}: JSON-to-XML via direct builder events
 * (the streaming path, active for format=(DIRECT, ATTRIBUTES) with merge=false,
 * as well as for format=(W3_XML, BASIC).
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class JsonStreamingParserTest extends SandboxTest {
  /** JSON test resource. */
  private static final String FILE = "src/test/resources/example.json";

  /**
   * Object with string, nested object, and boolean values.
   * @throws Exception exception
   */
  @Test public void exampleFile() throws Exception {
    final XNode result = parse(new IOFile(FILE), opts(DIRECT));
    query(result, "//name/data()",             "Smith");
    query(result, "//city/data()",             "New York");
    query(result, "//postalCode/data()",       "10021");
    query(result, "//postalCode/@type/data()", "number");
    query(result, "//active/data()",           "true");
    query(result, "//active/@type/data()",     "boolean");
    query(result, "//name/@type/data()",       "");  // string type omitted by default
  }

  /**
   * Top-level array with mixed-type items.
   * @throws Exception exception
   */
  @Test public void topLevelArray() throws Exception {
    final XNode result = parse("[1, \"two\", true, null]", opts(DIRECT));
    query(result, "/json/@type/data()",  "array");
    query(result, "//_ [1]/data()",      "1");
    query(result, "//_[1]/@type/data()", "number");
    query(result, "//_[2]/data()",       "two");
    query(result, "//_[2]/@type/data()", "");       // string type omitted
    query(result, "//_[3]/@type/data()", "boolean");
    query(result, "//_[4]/@type/data()", "null");
    query(result, "//_[4]/data()",       "");       // null has no text content
  }

  /**
   * Nested objects and arrays.
   * @throws Exception exception
   */
  @Test public void nested() throws Exception {
    final XNode result = parse("{\"a\":{\"b\":[1,2]}}", opts(DIRECT));
    query(result, "/json/@type/data()", "object");
    query(result, "//a/@type/data()",   "object");
    query(result, "//b/@type/data()",   "array");
    query(result, "count(//b/_)",       "2");
    query(result, "//b/_[1]/data()",    "1");
    query(result, "//b/_[2]/data()",    "2");
  }

  /**
   * Duplicate key with scalar value: USE_FIRST must suppress the second occurrence.
   * @throws Exception exception
   */
  @Test public void duplicateKeyScalar() throws Exception {
    final XNode result = parse("{\"x\":1,\"x\":2}", opts(DIRECT));
    query(result, "count(//x)", "1");
    query(result, "//x/data()", "1");
  }

  /**
   * Duplicate key where the suppressed value is a nested object (tests skip-depth tracking).
   * @throws Exception exception
   */
  @Test public void duplicateKeyObject() throws Exception {
    final XNode result = parse("{\"x\":1,\"x\":{\"y\":2}}", opts(DIRECT));
    query(result, "count(//x)", "1");
    query(result, "//x/data()", "1");
  }

  /**
   * strings=true makes string type explicit.
   * @throws Exception exception
   */
  @Test public void stringsOption() throws Exception {
    final JsonParserOptions jopts = new JsonParserOptions();
    jopts.set(JsonOptions.STRINGS, true);
    final XNode result = parse("\"hello\"", jopts);
    query(result, "/json/@type/data()", "string");
  }

  /**
   * Invalid JSON syntax is rejected.
   * @throws Exception exception
   */
  @Test public void invalidSyntax() throws Exception {
    error("{invalid}", opts(DIRECT), PARSE_JSON_X_X_X);
  }

  /**
   * Truncated JSON is rejected.
   * @throws Exception exception
   */
  @Test public void truncated() throws Exception {
    error("{\"a\":", opts(DIRECT), PARSE_JSON_X_X_X);
  }

  /**
   * Empty input is rejected.
   * @throws Exception exception
   */
  @Test public void emptyInput() throws Exception {
    error("", opts(DIRECT), PARSE_JSON_X_X_X);
  }

  /**
   * W3_XML: object with string, nested object, number and boolean fields.
   * @throws Exception exception
   */
  @Test public void w3XmlObject() throws Exception {
    final XNode result = parse(new IOFile(FILE), opts(W3_XML));
    query(result, "name(/*)",                               "map");  // not fn:map
    query(result, "//fn:string[@key='name']/data()",       "Smith");
    query(result, "//fn:string[@key='city']/data()",       "New York");
    query(result, "//fn:number[@key='postalCode']/data()", "10021");
    query(result, "//fn:boolean[@key='active']/data()",    "true");
  }

  /**
   * W3_XML: top-level array with mixed-type items (no key attributes on items).
   * @throws Exception exception
   */
  @Test public void w3XmlArray() throws Exception {
    final XNode result = parse("[1, \"two\", true, null]", opts(W3_XML));
    query(result, "local-name(/*)",              "array");
    query(result, "/fn:array/fn:number/data()",  "1");
    query(result, "/fn:array/fn:string/data()",  "two");
    query(result, "/fn:array/fn:boolean/data()", "true");
    query(result, "/fn:array/fn:null/data()",    "");
  }

  /**
   * W3_XML: nested objects and arrays.
   * @throws Exception exception
   */
  @Test public void w3XmlNested() throws Exception {
    final XNode result = parse("{\"a\":{\"b\":[1,2]}}", opts(W3_XML));
    query(result, "//fn:map[@key='a']/fn:array[@key='b']/fn:number[1]/data()", "1");
    query(result, "//fn:map[@key='a']/fn:array[@key='b']/fn:number[2]/data()", "2");
  }

  /**
   * W3_XML: duplicate keys are retained (W3C default — both occurrences kept).
   * @throws Exception exception
   */
  @Test public void w3XmlDuplicateKey() throws Exception {
    final XNode result = parse("{\"x\":1,\"x\":2}", opts(W3_XML));
    query(result, "count(//fn:number[@key='x'])",    "2");
    query(result, "//fn:number[@key='x'][1]/data()", "1");
    query(result, "//fn:number[@key='x'][2]/data()", "2");
  }

  /**
   * BASIC is a deprecated synonym for W3_XML and must use the streaming path.
   * @throws Exception exception
   */
  @Test public void basicFormat() throws Exception {
    final XNode result = parse(new IOFile(FILE), opts(BASIC));
    query(result, "local-name(/*)", "map");
  }

  /**
   * ATTRIBUTES: object with string, nested object, number and boolean fields.
   * @throws Exception exception
   */
  @Test public void attsExampleFile() throws Exception {
    final XNode result = parse(new IOFile(FILE), opts(ATTRIBUTES));
    query(result, "/json/@type/data()",                   "object");
    query(result, "//pair[@name='name']/data()",          "Smith");
    query(result, "//pair[@name='city']/data()",          "New York");
    query(result, "//pair[@name='postalCode']/@type/data()", "number");
    query(result, "//pair[@name='active']/@type/data()",  "boolean");
    query(result, "//pair[@name='name']/@type/data()",    "");  // string type omitted by default
  }

  /**
   * ATTRIBUTES: top-level array with mixed-type items.
   * @throws Exception exception
   */
  @Test public void attsTopLevelArray() throws Exception {
    final XNode result = parse("[1, \"two\", true, null]", opts(ATTRIBUTES));
    query(result, "/json/@type/data()",   "array");
    query(result, "//item[1]/data()",     "1");
    query(result, "//item[1]/@type/data()", "number");
    query(result, "//item[2]/data()",     "two");
    query(result, "//item[2]/@type/data()", "");      // string type omitted
    query(result, "//item[3]/@type/data()", "boolean");
    query(result, "//item[4]/@type/data()", "null");
  }

  /**
   * ATTRIBUTES: nested objects and arrays.
   * @throws Exception exception
   */
  @Test public void attsNested() throws Exception {
    final XNode result = parse("{\"a\":{\"b\":[1,2]}}", opts(ATTRIBUTES));
    query(result, "//pair[@name='a']/@type/data()",         "object");
    query(result, "//pair[@name='b']/@type/data()",         "array");
    query(result, "count(//pair[@name='b']/item)",          "2");
    query(result, "//pair[@name='b']/item[1]/data()",       "1");
    query(result, "//pair[@name='b']/item[2]/data()",       "2");
  }

  /**
   * ATTRIBUTES: duplicate key with scalar value: USE_FIRST must suppress the second occurrence.
   * @throws Exception exception
   */
  @Test public void attsDuplicateKey() throws Exception {
    final XNode result = parse("{\"x\":1,\"x\":2}", opts(ATTRIBUTES));
    query(result, "count(//pair[@name='x'])", "1");
    query(result, "//pair[@name='x']/data()", "1");
  }

  /**
   * merge=true falls back to the non-streaming path.
   * @throws Exception exception
   */
  @Test public void directMergeOption() throws Exception {
    final JsonParserOptions jopts = new JsonParserOptions();
    jopts.set(JsonOptions.MERGE, true);
    context.options.set(MainOptions.JSONPARSER, jopts);
    final SingleParser sp = JsonStreamingParser.get(new IOFile(FILE), context.options);
    assertInstanceOf(JsonParser.class, sp);
    final XNode result = new DBNode(MemBuilder.build(sp), 0);
    query(result, "//name/data()", "Smith");
  }

  /**
   * merge=true falls back to the non-streaming path.
   * @throws Exception exception
   */
  @Test public void attsMergeOption() throws Exception {
    final JsonParserOptions jopts = new JsonParserOptions();
    jopts.set(JsonOptions.FORMAT, ATTRIBUTES);
    jopts.set(JsonOptions.MERGE, true);
    context.options.set(MainOptions.JSONPARSER, jopts);
    final SingleParser sp = JsonStreamingParser.get(new IOFile(FILE), context.options);
    assertInstanceOf(JsonParser.class, sp);
    final XNode result = new DBNode(MemBuilder.build(sp), 0);
    query(result, "//pair[@name='name']/data()", "Smith");
  }

  // ==========================================================================================
  // HELPERS
  // ==========================================================================================

  /**
   * Parses JSON with both the streaming and non-streaming parsers, asserts they produce
   * equivalent results, and returns the resulting document node.
   * @param source JSON source
   * @param jopts JSON parser options
   * @return document node (streaming result)
   * @throws Exception exception
   */
  private static XNode parse(final IO source, final JsonParserOptions jopts) throws Exception {
    context.options.set(MainOptions.JSONPARSER, jopts);

    // streaming parse — verify streaming parser is selected
    final SingleParser sp = JsonStreamingParser.get(source, context.options);
    assertInstanceOf(JsonStreamingParser.class, sp);
    final XNode streamResult = new DBNode(MemBuilder.build(sp), 0);

    // non-streaming parse — verify non-streaming converter is selected
    final XNode nonStreamResult = (XNode) JsonConverter.get(jopts).convert(source);
    assertNotNull(nonStreamResult);

    // verify both parsers produce equivalent output (deep-equal ignores namespace prefix)
    try(QueryProcessor qp = new QueryProcessor(
        "declare variable $a external; declare variable $b external; deep-equal($a,$b)", context)) {
      qp.variable("a", streamResult).variable("b", nonStreamResult);
      assertEquals("true", qp.value().serialize().toString());
    }

    return streamResult;
  }

  /**
   * Wraps JSON in an {@link IOContent} and delegates to {@link #parse(IO, JsonParserOptions)}.
   * @param json JSON content
   * @param jopts JSON parser options
   * @return document node
   * @throws Exception exception
   */
  private static XNode parse(final String json, final JsonParserOptions jopts) throws Exception {
    return parse(new IOContent(json), jopts);
  }

  /**
   * Evaluates an XQuery expression with the given document node as context.
   * @param ctx context document node
   * @param expr XQuery expression
   * @param expected expected result
   * @throws Exception exception
   */
  private static void query(final XNode ctx, final String expr, final String expected)
      throws Exception {
    try(QueryProcessor qp = new QueryProcessor(expr, context)) {
      qp.context(ctx);
      compare(expr, qp.value().serialize().toString(), expected, null);
    }
  }

  /**
   * Returns JSON parser options for the given format.
   * @param fmt JSON format
   * @return options
   */
  private static JsonParserOptions opts(final JsonOptions.JsonFormat fmt) {
    final JsonParserOptions jopts = new JsonParserOptions();
    jopts.set(JsonOptions.FORMAT, fmt);
    return jopts;
  }

  /**
   * Asserts that the streaming parser rejects the given JSON with the expected error.
   * @param json JSON content
   * @param jopts JSON parser options
   * @param error expected error
   * @throws Exception exception
   */
  private static void error(final String json, final JsonParserOptions jopts,
      final QueryError error) throws Exception {
    context.options.set(MainOptions.JSONPARSER, jopts);
    final SingleParser sp = JsonStreamingParser.get(new IOContent(json), context.options);
    assertInstanceOf(JsonStreamingParser.class, sp);
    try {
      MemBuilder.build(sp);
      fail("Expected parse error for: " + json);
    } catch(final QueryIOException ex) {
      assertEquals(error, ex.getCause().error());
    }
  }
}
