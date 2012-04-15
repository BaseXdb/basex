package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * 
 *       Tests the parse-json function.
 *       
 *       Test coverage of the first argument is reasonably complete. There are few tests yet
 *       for the options argument.
 *    .
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnParseJson extends QT3TestSet {

  /**
   * Empty object.
   */
  @org.junit.Test
  public void fnParseJson001() {
    final XQuery query = new XQuery(
      "parse-json(\"{}\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{}")
    );
  }

  /**
   * Singleton object, numeric value.
   */
  @org.junit.Test
  public void fnParseJson002() {
    final XQuery query = new XQuery(
      "parse-json('{\"abc\":12}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=12e0}")
    );
  }

  /**
   * Singleton object, numeric value.
   */
  @org.junit.Test
  public void fnParseJson003() {
    final XQuery query = new XQuery(
      "parse-json('{\"abc\":12e0}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=12e0}")
    );
  }

  /**
   * Singleton object, negative numeric value.
   */
  @org.junit.Test
  public void fnParseJson004() {
    final XQuery query = new XQuery(
      "parse-json('{\"abc\":-1.2e0}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=-1.2e0}")
    );
  }

  /**
   * Singleton object, boolean value.
   */
  @org.junit.Test
  public void fnParseJson005() {
    final XQuery query = new XQuery(
      "parse-json('{\"abc\":true}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=true()}")
    );
  }

  /**
   * Singleton object, boolean value.
   */
  @org.junit.Test
  public void fnParseJson006() {
    final XQuery query = new XQuery(
      "parse-json('{\"abc\":false}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=false()}")
    );
  }

  /**
   * Singleton object, null value.
   */
  @org.junit.Test
  public void fnParseJson007() {
    final XQuery query = new XQuery(
      "parse-json('{\"abc\":null}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=()}")
    );
  }

  /**
   * Two-entry object.
   */
  @org.junit.Test
  public void fnParseJson008() {
    final XQuery query = new XQuery(
      "parse-json('{\"abc\":true,\"xyz\":false}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=true(),\"xyz\":=false()}")
    );
  }

  /**
   * Two-entry object - same with whitespace.
   */
  @org.junit.Test
  public void fnParseJson009() {
    final XQuery query = new XQuery(
      "parse-json(' { \"abc\" : true , \"xyz\" : false } ')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=true(),\"xyz\":=false()}")
    );
  }

  /**
   * Two-entry object - same with more whitespace.
   */
  @org.junit.Test
  public void fnParseJson010() {
    final XQuery query = new XQuery(
      "parse-json('    {   \"abc\"   :   true    ,\n" +
      "            \"xyz\"   :   false   \n" +
      "            }   ')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("map{\"abc\":=true(),\"xyz\":=false()}")
    );
  }

  /**
   * Empty array.
   */
  @org.junit.Test
  public void fnParseJson011() {
    final XQuery query = new XQuery(
      "parse-json(\"[]\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("map:size($result) eq 0")
      &&
        assertQuery("map:collation($result) eq \"http://saxon.sf.net/json/array-collation\"")
      )
    );
  }

  /**
   * Single-item array.
   */
  @org.junit.Test
  public void fnParseJson012() {
    final XQuery query = new XQuery(
      "parse-json(\"[12345]\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("map:contains($result, 1)")
      &&
        assertQuery("map:get($result, 1) eq 12345")
      )
    );
  }

  /**
   * Single-item array.
   */
  @org.junit.Test
  public void fnParseJson013() {
    final XQuery query = new XQuery(
      "parse-json('[\"abcd\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("map:contains($result, 1)")
      &&
        assertQuery("$result(1) eq \"abcd\"")
      )
    );
  }

  /**
   * Single-item array.
   */
  @org.junit.Test
  public void fnParseJson014() {
    final XQuery query = new XQuery(
      "parse-json(\"[true]\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("map:contains($result, 1)")
      &&
        assertQuery("$result(1) eq true()")
      )
    );
  }

  /**
   * Single-item array.
   */
  @org.junit.Test
  public void fnParseJson015() {
    final XQuery query = new XQuery(
      "parse-json(\"[false]\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("map:contains($result, 1)")
      &&
        assertQuery("$result(1) eq false()")
      )
    );
  }

  /**
   * Single-item array.
   */
  @org.junit.Test
  public void fnParseJson016() {
    final XQuery query = new XQuery(
      "parse-json(\"[null]\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("map:contains($result, 1)")
      &&
        assertQuery("empty($result(1))")
      )
    );
  }

  /**
   * Multi-item array.
   */
  @org.junit.Test
  public void fnParseJson017() {
    final XQuery query = new XQuery(
      "parse-json('[1,2,3, \"abc\", \"def\", true, false, null]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("every $i in 1 to 8 satisfies map:contains($result, $i)")
      &&
        assertQuery("every $i in (0, 9, 10) satisfies not(map:contains($result, $i))")
      &&
        assertQuery("$result(1) eq 1")
      &&
        assertQuery("$result(5) eq \"def\"")
      &&
        assertQuery("$result(7) eq false()")
      )
    );
  }

  /**
   * Multi-item array - same with more whitespace.
   */
  @org.junit.Test
  public void fnParseJson018() {
    final XQuery query = new XQuery(
      "parse-json('\n" +
      "        [   1,     2,  3, \n" +
      "        \"abc\",  \"def\",   true, \n" +
      "        false,  null ]\n" +
      "        ')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(*)")
      &&
        assertCount(1)
      &&
        assertQuery("every $i in 1 to 8 satisfies map:contains($result, $i)")
      &&
        assertQuery("every $i in (0, 9, 10) satisfies not(map:contains($result, $i))")
      &&
        assertQuery("$result(1) eq 1")
      &&
        assertQuery("$result(5) eq \"def\"")
      &&
        assertQuery("$result(7) eq false()")
      )
    );
  }

  /**
   * Nested empty arrays.
   */
  @org.junit.Test
  public void fnParseJson019() {
    final XQuery query = new XQuery(
      "parse-json(\"[[[],[]]]\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertType("map(xs:integer, map(xs:integer, map(*)))")
      &&
        assertCount(1)
      &&
        assertQuery("map:size($result) eq 1")
      &&
        assertQuery("map:size($result(1)) eq 2")
      &&
        assertQuery("map:size($result(1)(2)) eq 0")
      )
    );
  }

  /**
   * Nested non-empty arrays.
   */
  @org.junit.Test
  public void fnParseJson020() {
    final XQuery query = new XQuery(
      "parse-json('[1, 2, [], [1], [1,2], [1,2,3]]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:size($result) = 6")
      &&
        assertQuery("$result(6)(3) = 3")
      )
    );
  }

  /**
   * Array of objects.
   */
  @org.junit.Test
  public void fnParseJson021() {
    final XQuery query = new XQuery(
      "parse-json('[{\"x\":12,\"y\":5}, {\"x\":13,\"y\":6}]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:size($result) = 2")
      &&
        assertQuery("$result(2)(\"y\") = 6")
      )
    );
  }

  /**
   * Object containing arrays.
   */
  @org.junit.Test
  public void fnParseJson022() {
    final XQuery query = new XQuery(
      "parse-json('{\"x\":[12,3], \"y\":[14,9]}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("map:size($result) = 2")
      &&
        assertQuery("$result(\"y\")(2) = 9")
      )
    );
  }

  /**
   * Number formats.
   */
  @org.junit.Test
  public void fnParseJson023() {
    final XQuery query = new XQuery(
      "parse-json('[0.123]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = 0.123e0")
      &&
        assertQuery("$result(1) instance of xs:double")
      )
    );
  }

  /**
   * Number formats.
   */
  @org.junit.Test
  public void fnParseJson024() {
    final XQuery query = new XQuery(
      "parse-json('[-0.123]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = -0.123e0")
      &&
        assertQuery("$result(1) instance of xs:double")
      )
    );
  }

  /**
   * Number formats.
   */
  @org.junit.Test
  public void fnParseJson025() {
    final XQuery query = new XQuery(
      "parse-json('[-0.123e2]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = -0.123e2")
      &&
        assertQuery("$result(1) instance of xs:double")
      )
    );
  }

  /**
   * Number formats.
   */
  @org.junit.Test
  public void fnParseJson026() {
    final XQuery query = new XQuery(
      "parse-json('[-0.123e+2]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = -0.123e+2")
      &&
        assertQuery("$result(1) instance of xs:double")
      )
    );
  }

  /**
   * Number formats.
   */
  @org.junit.Test
  public void fnParseJson027() {
    final XQuery query = new XQuery(
      "parse-json('[-0.123e-2]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = -0.123e-2")
      &&
        assertQuery("$result(1) instance of xs:double")
      )
    );
  }

  /**
   * String formats.
   */
  @org.junit.Test
  public void fnParseJson028() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\\\\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = \"\\\"")
      &&
        assertQuery("$result(1) instance of xs:string")
      )
    );
  }

  /**
   * String formats.
   */
  @org.junit.Test
  public void fnParseJson029() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\\"\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = '\"'")
      &&
        assertQuery("$result(1) instance of xs:string")
      )
    );
  }

  /**
   * String formats.
   */
  @org.junit.Test
  public void fnParseJson030() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\r\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = '\r'")
      &&
        assertQuery("$result(1) instance of xs:string")
      )
    );
  }

  /**
   * String formats.
   */
  @org.junit.Test
  public void fnParseJson031() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\n\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = '\n'")
      &&
        assertQuery("$result(1) instance of xs:string")
      )
    );
  }

  /**
   * String formats.
   */
  @org.junit.Test
  public void fnParseJson032() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\/\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = '/'")
      &&
        assertQuery("$result(1) instance of xs:string")
      )
    );
  }

  /**
   * String formats.
   */
  @org.junit.Test
  public void fnParseJson033() {
    final XQuery query = new XQuery(
      "parse-json('[\"aa\\u0030aa\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = 'aa0aa'")
      &&
        assertQuery("$result(1) instance of xs:string")
      )
    );
  }

  /**
   * String formats - surrogate pair.
   */
  @org.junit.Test
  public void fnParseJson034() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\uD834\\udD1E\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result(1) = '𝄞'")
      &&
        assertQuery("$result(1) instance of xs:string")
      )
    );
  }

  /**
   * String formats: effect of unescape option.
   */
  @org.junit.Test
  public void fnParseJson035() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\r\"]', map{'unescape':=false()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("$result(1) = '\\r'")
    );
  }

  /**
   * String formats: effect of unescape option.
   */
  @org.junit.Test
  public void fnParseJson036() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\r\"]', map{'unescape':=true()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("$result(1) = '\r'")
    );
  }

  /**
   * String formats: effect of unescape option.
   */
  @org.junit.Test
  public void fnParseJson037() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\u0000\"]', map{'unescape':=false()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("$result(1) = '\\u0000'")
    );
  }

  /**
   * Anything allowed at Top level under ECMA spec.
   */
  @org.junit.Test
  public void fnParseJson038() {
    final XQuery query = new XQuery(
      "parse-json('true', map{'spec':='ECMA-262'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Anything allowed at Top level under ECMA spec.
   */
  @org.junit.Test
  public void fnParseJson039() {
    final XQuery query = new XQuery(
      "parse-json('false', map{'spec':='ECMA-262'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Anything allowed at Top level under ECMA spec.
   */
  @org.junit.Test
  public void fnParseJson040() {
    final XQuery query = new XQuery(
      "parse-json('null', map{'spec':='ECMA-262'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEmpty()
    );
  }

  /**
   * Anything allowed at Top level under ECMA spec.
   */
  @org.junit.Test
  public void fnParseJson041() {
    final XQuery query = new XQuery(
      "parse-json('93.7', map{'spec':='ECMA-262'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("93.7e0")
      &&
        assertType("xs:double")
      )
    );
  }

  /**
   * Anything allowed at Top level under ECMA spec.
   */
  @org.junit.Test
  public void fnParseJson042() {
    final XQuery query = new XQuery(
      "parse-json('\"abcd\\n\"', map{'spec':='ECMA-262','unescape':=false()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertEq("'abcd\\n'")
      &&
        assertType("xs:string")
      )
    );
  }

  /**
   * An example JSON file.
   */
  @org.junit.Test
  public void fnParseJson101() {
    final XQuery query = new XQuery(
      "parse-json(unparsed-text('parse-json/data001.json'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertCount(1)
    );
  }

  /**
   * An example JSON file.
   */
  @org.junit.Test
  public void fnParseJson102() {
    final XQuery query = new XQuery(
      "parse-json(unparsed-text('parse-json/data002.json'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertCount(1)
    );
  }

  /**
   * An example JSON file.
   */
  @org.junit.Test
  public void fnParseJson103() {
    final XQuery query = new XQuery(
      "parse-json(unparsed-text('parse-json/data003.json'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertCount(1)
    );
  }

  /**
   * An example JSON file.
   */
  @org.junit.Test
  public void fnParseJson104() {
    final XQuery query = new XQuery(
      "parse-json(unparsed-text('parse-json/data004.json'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertCount(1)
    );
  }

  /**
   * An example JSON file.
   */
  @org.junit.Test
  public void fnParseJson105() {
    final XQuery query = new XQuery(
      "parse-json(unparsed-text('parse-json/data005.json'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertCount(1)
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson901() {
    final XQuery query = new XQuery(
      "parse-json('[-0.123e-2[')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson902() {
    final XQuery query = new XQuery(
      "parse-json('[false')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson903() {
    final XQuery query = new XQuery(
      "parse-json('[falsehood]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson904() {
    final XQuery query = new XQuery(
      "parse-json('[(5)]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson905() {
    final XQuery query = new XQuery(
      "parse-json('[{5}]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson906() {
    final XQuery query = new XQuery(
      "parse-json('[{x:23}]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson907() {
    final XQuery query = new XQuery(
      "parse-json('23,24')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson908() {
    final XQuery query = new XQuery(
      "parse-json('[\"abc]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson909() {
    final XQuery query = new XQuery(
      "parse-json('[1,2,3,]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson910() {
    final XQuery query = new XQuery(
      "parse-json('{\"a\":=13}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson911() {
    final XQuery query = new XQuery(
      "parse-json('{\"a\":13,,\"b\":15}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson912() {
    final XQuery query = new XQuery(
      "parse-json('{\"a\":13')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson913() {
    final XQuery query = new XQuery(
      "parse-json('{\"a\":{\"b\":12}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson914() {
    final XQuery query = new XQuery(
      "parse-json('{\"a\":{\"b\":12}}}')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson915() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson916() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\1\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson917() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\u2\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson918() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\u123u\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error: invalid character under XML 1.0.
   */
  @org.junit.Test
  public void fnParseJson919() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\b\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Syntax error.
   */
  @org.junit.Test
  public void fnParseJson920() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\x20\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * String formats: \s is not allowed by the RFC.
   */
  @org.junit.Test
  public void fnParseJson921() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\s\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * String formats - unpaired surrogate.
   */
  @org.junit.Test
  public void fnParseJson922() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\uD834\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * String formats - surrogate pair.
   */
  @org.junit.Test
  public void fnParseJson923() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\udD1E\"]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * String formats: effect of unescape option: invalid XML character.
   */
  @org.junit.Test
  public void fnParseJson924() {
    final XQuery query = new XQuery(
      "parse-json('[\"\\u0000\"]', map{'unescape':=true()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Anything not allowed at Top level under RFC spec.
   */
  @org.junit.Test
  public void fnParseJson925() {
    final XQuery query = new XQuery(
      "parse-json('true', map{'spec':='RFC4627'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Anything not allowed at Top level under RFC spec.
   */
  @org.junit.Test
  public void fnParseJson926() {
    final XQuery query = new XQuery(
      "parse-json('false', map{'spec':='RFC4627'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Anything not allowed at Top level under RFC spec.
   */
  @org.junit.Test
  public void fnParseJson927() {
    final XQuery query = new XQuery(
      "parse-json('null', map{'spec':='RFC4627'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Anything not allowed at Top level under RFC spec.
   */
  @org.junit.Test
  public void fnParseJson928() {
    final XQuery query = new XQuery(
      "parse-json('93.7', map{'spec':='RFC4627'})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Anything not allowed at Top level under RFC spec.
   */
  @org.junit.Test
  public void fnParseJson929() {
    final XQuery query = new XQuery(
      "parse-json('\"abcd\\n\"', map{'spec':='RFC4627','unescape':=false()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Invalid number.
   */
  @org.junit.Test
  public void fnParseJson930() {
    final XQuery query = new XQuery(
      "parse-json('[.3]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Invalid number.
   */
  @org.junit.Test
  public void fnParseJson931() {
    final XQuery query = new XQuery(
      "parse-json('[01]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Invalid number.
   */
  @org.junit.Test
  public void fnParseJson932() {
    final XQuery query = new XQuery(
      "parse-json('[00.00]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Invalid number.
   */
  @org.junit.Test
  public void fnParseJson933() {
    final XQuery query = new XQuery(
      "parse-json('[+23]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }

  /**
   * Invalid number.
   */
  @org.junit.Test
  public void fnParseJson934() {
    final XQuery query = new XQuery(
      "parse-json('[1.234f0]')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0001")
    );
  }
}
