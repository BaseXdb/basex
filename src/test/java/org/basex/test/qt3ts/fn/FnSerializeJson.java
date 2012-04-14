package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * 
 *       Tests the serialize-json function.
 *     .
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnSerializeJson extends QT3TestSet {

  /**
   * Empty object.
   */
  @org.junit.Test
  public void fnSerializeJson001() {
    final XQuery query = new XQuery(
      "serialize-json(map{})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("translate($result,' \t\n\r', '') = '{}'")
    );
  }

  /**
   * Empty object.
   */
  @org.junit.Test
  public void fnSerializeJson002() {
    final XQuery query = new XQuery(
      "serialize-json((), map{\"spec\":=\"ECMA-262\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("translate($result,' \t\n\r', '') = 'null'")
    );
  }

  /**
   * Empty object.
   */
  @org.junit.Test
  public void fnSerializeJson003() {
    final XQuery query = new XQuery(
      "serialize-json(12.5, map{\"spec\":=\"ECMA-262\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("number($result) = 12.5")
    );
  }

  /**
   * Boolean value.
   */
  @org.junit.Test
  public void fnSerializeJson004() {
    final XQuery query = new XQuery(
      "normalize-space(serialize-json(true(), map{\"spec\":=\"ECMA-262\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true")
    );
  }

  /**
   * Boolean value.
   */
  @org.junit.Test
  public void fnSerializeJson005() {
    final XQuery query = new XQuery(
      "normalize-space(serialize-json(false(), map{\"spec\":=\"ECMA-262\"}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "false")
    );
  }

  /**
   * Singleton object.
   */
  @org.junit.Test
  public void fnSerializeJson006() {
    final XQuery query = new XQuery(
      "serialize-json(map{'abc':=23})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertQuery("translate($result,' \t\n\r', '') = '{\"abc\":23}'")
    );
  }

  /**
   * Two-entry object.
   */
  @org.junit.Test
  public void fnSerializeJson007() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(map{'abc':=23, 'xyz':=49}, map{\"spec\":=\"RFC4627\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("starts-with($result, '{')")
      &&
        assertQuery("contains($result, '\"abc\":23')")
      &&
        assertQuery("contains($result, '\"xyz\":49')")
      &&
        assertQuery("ends-with($result, '}')")
      &&
        assertType("xs:string")
      &&
        assertCount(1)
      &&
        assertQuery("parse-json($result)(\"abc\") = 23")
      )
    );
  }

  /**
   * JSON array.
   */
  @org.junit.Test
  public void fnSerializeJson008() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(parse-json('[1, 2, 3, \"four\", true, false, null]'), map{\"spec\":=\"RFC4627\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[1,2,3,\"four\",true,false,null]")
    );
  }

  /**
   * Serialize XDM sequence.
   */
  @org.junit.Test
  public void fnSerializeJson009() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((1, 2, 3, \"four\", true(), false()), map{\"spec\":=\"RFC4627\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[1,2,3,\"four\",true,false]")
    );
  }

  /**
   * Serialize nested arrays.
   */
  @org.junit.Test
  public void fnSerializeJson010() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(parse-json('[[1, 2], [3, 4], [5, 6], [7], [], [null]]'))\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[[1,2],[3,4],[5,6],[7],[],[null]]")
    );
  }

  /**
   * Map containing a sequence.
   */
  @org.junit.Test
  public void fnSerializeJson011() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(map{\"abc\":=(1 to 10)}, map{\"spec\":=\"RFC4627\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "{\"abc\":[1,2,3,4,5,6,7,8,9,10]}")
    );
  }

  /**
   * Sequence of maps.
   */
  @org.junit.Test
  public void fnSerializeJson012() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((map{\"abc\":=1},map{\"def\":=2},map{\"ghi\":=3}))\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[{\"abc\":1},{\"def\":2},{\"ghi\":3}]")
    );
  }

  /**
   * Nested maps.
   */
  @org.junit.Test
  public void fnSerializeJson013() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((map{\"abc\":=map{\"abc\":=1}},map{\"def\":=map{\"def\":=2}},map{\"ghi\":=map{\"ghi\":=3}}), map{\"spec\":=\"RFC4627\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[{\"abc\":{\"abc\":1}},{\"def\":{\"def\":2}},{\"ghi\":{\"ghi\":3}}]")
    );
  }

  /**
   * Non-BMP string.
   */
  @org.junit.Test
  public void fnSerializeJson014() {
    final XQuery query = new XQuery(
      "let $r := serialize-json('𝄞', map{\"spec\":=\"ECMA-262\"})\n" +
      "            return translate(normalize-space($r), 'abcdef', 'ABCDEF')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "\"\\uD834\\uDD1E\"")
    );
  }

  /**
   * String with newline.
   */
  @org.junit.Test
  public void fnSerializeJson015() {
    final XQuery query = new XQuery(
      "let $r := serialize-json('\n" +
      "', map{\"spec\":=\"ECMA-262\"})\n" +
      "            return translate(normalize-space($r), 'abcdef', 'ABCDEF')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertStringValue(false, "\"\\n\"")
      ||
        assertStringValue(false, "\"\\u0010\"")
      )
    );
  }

  /**
   * Nested maps with indent="no" - require no optional space.
   */
  @org.junit.Test
  public void fnSerializeJson016() {
    final XQuery query = new XQuery(
      "serialize-json((map{\"abc\":=map{\"abc\":=1}},map{\"def\":=map{\"def\":=2}},map{\"ghi\":=map{\"ghi\":=3}}),\n" +
      "        map{\"indent\":=false()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[{\"abc\":{\"abc\":1}},{\"def\":{\"def\":2}},{\"ghi\":{\"ghi\":3}}]")
    );
  }

  /**
   * Nested maps with indent="yes" - (we can't check that it's actually indented).
   */
  @org.junit.Test
  public void fnSerializeJson017() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((map{\"abc\":=map{\"abc\":=1}},map{\"def\":=map{\"def\":=2}},map{\"ghi\":=map{\"ghi\":=3}}),\n" +
      "              map{\"indent\":=true(), \"spec\":=\"RFC4627\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[{\"abc\":{\"abc\":1}},{\"def\":{\"def\":2}},{\"ghi\":{\"ghi\":3}}]")
    );
  }

  /**
   * Decimal number.
   */
  @org.junit.Test
  public void fnSerializeJson018() {
    final XQuery query = new XQuery(
      "parse-json(serialize-json(12.34, map{\"spec\":=\"ECMA-262\"}), map{\"spec\":=\"ECMA-262\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result eq xs:double('12.34')")
      &&
        assertQuery("$result instance of xs:double")
      )
    );
  }

  /**
   * Exponential number.
   */
  @org.junit.Test
  public void fnSerializeJson019() {
    final XQuery query = new XQuery(
      "parse-json(serialize-json(12.34e-30, map{\"spec\":=\"ECMA-262\"}), map{\"spec\":=\"ECMA-262\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result eq 12.34e-30")
      &&
        assertQuery("$result instance of xs:double")
      )
    );
  }

  /**
   * String with escaping.
   */
  @org.junit.Test
  public void fnSerializeJson020() {
    final XQuery query = new XQuery(
      "serialize-json(\"abc\"\"def\", map{\"spec\":=\"ECMA-262\",\"escape\":=true()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result eq '\"abc\\\"def\"'")
      &&
        assertQuery("$result instance of xs:string")
      )
    );
  }

  /**
   * String with no escaping.
   */
  @org.junit.Test
  public void fnSerializeJson021() {
    final XQuery query = new XQuery(
      "serialize-json(\"abc\\\\def\", map{\"spec\":=\"ECMA-262\",\"escape\":=false()})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertQuery("$result eq '\"abc\\\\def\"'")
      &&
        assertQuery("$result instance of xs:string")
      )
    );
  }

  /**
   * NaN, INF, -INF.
   */
  @org.junit.Test
  public void fnSerializeJson022() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((number('NaN'), number('INF'), number('-INF')))\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[\"NaN\",\"INF\",\"-INF\"]")
    );
  }

  /**
   * untypedAtomic input.
   */
  @org.junit.Test
  public void fnSerializeJson023() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((0,0,xs:untypedAtomic(\"abcd\")))\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[0,0,\"abcd\"]")
    );
  }

  /**
   * sparse array.
   */
  @org.junit.Test
  public void fnSerializeJson024() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(map{1:=\"a\",2:=\"b\",4:=\"d\",10:=\"j\",7:=\"g\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[\"a\",\"b\",null,\"d\",null,null,\"g\",null,null,\"j\"]")
    );
  }

  /**
   * Fallback converts date to string.
   */
  @org.junit.Test
  public void fnSerializeJson100() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((0,0,xs:date('2011-04-06')), map{\"fallback\":=function($v){string($v)}})\n" +
      "        return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[0,0,\"2011-04-06\"]")
    );
  }

  /**
   * Fallback converts date to string.
   */
  @org.junit.Test
  public void fnSerializeJson101() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(map{\"a\":=xs:date('2011-04-06')}, map{\"fallback\":=function($v){string($v)}})\n" +
      "        return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "{\"a\":\"2011-04-06\"}")
    );
  }

  /**
   * Fallback converts nodes to lexical XML.
   */
  @org.junit.Test
  public void fnSerializeJson102() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(map{\"a\":=doc($uri)}, map{\"fallback\":=function($v){serialize($v)}})\n" +
      "        return translate($r,' \t\n" +
      "\r', '')",
      ctx);
    query.addDocument("http://www.w3.org/fots/serialize-json/doc001.xml", file("fn/serialize-json/doc001.xml"));
    query.bind("uri", new XQuery("'http://www.w3.org/fots/serialize-json/doc001.xml'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "{\"a\":\"<?xmlversion=\\\"1.0\\\"encoding=\\\"UTF-8\\\"?><a>text</a>\"}")
    );
  }

  /**
   * Fallback applied to a map.
   */
  @org.junit.Test
  public void fnSerializeJson103() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((0,0,map{true():=\"gogogo\"}), map{\"fallback\":=function($v){if ($v instance of map(xs:boolean, item()*)) then \"a-boolean-map\" else $v}})\n" +
      "        return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[0,0,\"a-boolean-map\"]")
    );
  }

  /**
   * Recursive fallback.
   */
  @org.junit.Test
  public void fnSerializeJson104() {
    final XQuery query = new XQuery(
      "let $r := serialize-json((0,0,map{false():=map{false():=\"ok\"}}), \n" +
      "           map{\"fallback\":=function($v){\n" +
      "                 if ($v instance of map(xs:boolean, item()*)) \n" +
      "                 then map:new(for $k in map:keys($v) return map:entry(string($k), map:get($v, $k)))  \n" +
      "                 else $v}})\n" +
      "        return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "[0,0,{\"false\":{\"false\":\"ok\"}}]")
    );
  }

  /**
   * Top level not a map, using RFC spec.
   */
  @org.junit.Test
  public void fnSerializeJson901() {
    final XQuery query = new XQuery(
      "serialize-json(\"abcd\", map{\"spec\":=\"RFC4627\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Top level not a map, using RFC spec.
   */
  @org.junit.Test
  public void fnSerializeJson902() {
    final XQuery query = new XQuery(
      "serialize-json(true(), map{\"spec\":=\"RFC4627\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Top level not a map, using RFC spec.
   */
  @org.junit.Test
  public void fnSerializeJson903() {
    final XQuery query = new XQuery(
      "serialize-json((), map{\"spec\":=\"RFC4627\"})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Cannot serialize a date value.
   */
  @org.junit.Test
  public void fnSerializeJson904() {
    final XQuery query = new XQuery(
      "serialize-json((0,0,xs:date('2011-04-06')))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Cannot serialize a URI value.
   */
  @org.junit.Test
  public void fnSerializeJson905() {
    final XQuery query = new XQuery(
      "serialize-json(map{\"uri\":=xs:anyURI('http://www.w3.org/')})",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Bad indent option.
   */
  @org.junit.Test
  public void fnSerializeJson906() {
    final XQuery query = new XQuery(
      "serialize-json((1,2,3),map:entry(\"indent\",23))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Bad indent option.
   */
  @org.junit.Test
  public void fnSerializeJson907() {
    final XQuery query = new XQuery(
      "serialize-json((1,2,3),map:entry(\"indent\",\"true\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Bad indent option.
   */
  @org.junit.Test
  public void fnSerializeJson908() {
    final XQuery query = new XQuery(
      "serialize-json((1,2,3),map:entry(\"indent\",(true(),false())))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * sparse array with negative keys.
   */
  @org.junit.Test
  public void fnSerializeJson909() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(map{1:=\"a\",2:=\"b\",4:=\"d\",10:=\"j\",7:=\"g\",-1:=\"$$$\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * sparse array with zero key.
   */
  @org.junit.Test
  public void fnSerializeJson910() {
    final XQuery query = new XQuery(
      "let $r := serialize-json(map{1:=\"a\",2:=\"b\",4:=\"d\",10:=\"j\",7:=\"g\",0:=\"$$$\"})\n" +
      "            return translate($r,' \t\n" +
      "\r', '')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }

  /**
   * Bad escape option.
   */
  @org.junit.Test
  public void fnSerializeJson911() {
    final XQuery query = new XQuery(
      "serialize-json((1,2,3),map:entry(\"escape\",map{}))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOJS0002")
    );
  }
}
