package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the analyze-string() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnAnalyzeString extends QT3TestSet {

  /**
   *  analyze-string with empty string.
   */
  @org.junit.Test
  public void analyzeString001() {
    final XQuery query = new XQuery(
      "analyze-string(\"\", \"abc\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"/>", true)
    );
  }

  /**
   *  analyze-string with empty sequence .
   */
  @org.junit.Test
  public void analyzeString002() {
    final XQuery query = new XQuery(
      "analyze-string((), \"abc\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"/>", true)
    );
  }

  /**
   *  analyze-string with empty sequence .
   */
  @org.junit.Test
  public void analyzeString002a() {
    final XQuery query = new XQuery(
      "count(analyze-string((), \"abc\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  analyze-string with a mix of matching and non-matching strings.
   */
  @org.junit.Test
  public void analyzeString003() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"a\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:non-match>b</fn:non-match><fn:match>a</fn:match><fn:non-match>n</fn:non-match><fn:match>a</fn:match><fn:non-match>n</fn:non-match><fn:match>a</fn:match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string with a single non-matching string.
   */
  @org.junit.Test
  public void analyzeString004() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"custard\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:non-match>banana</fn:non-match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string with a single matching string.
   */
  @org.junit.Test
  public void analyzeString005() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \".+\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match>banana</fn:match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string with a adjacent matching strings.
   */
  @org.junit.Test
  public void analyzeString006() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"an\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:non-match>b</fn:non-match><fn:match>an</fn:match><fn:match>an</fn:match><fn:non-match>a</fn:non-match></fn:analyze-string-result>", true)
      )
    );
  }

  /**
   *  analyze-string with a single captured group.
   */
  @org.junit.Test
  public void analyzeString007() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"a(n)\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:non-match>b</fn:non-match><fn:match>a<fn:group nr=\"1\">n</fn:group></fn:match><fn:match>a<fn:group nr=\"1\">n</fn:group></fn:match><fn:non-match>a</fn:non-match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string with nested captured groups.
   */
  @org.junit.Test
  public void analyzeString008() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"(a(n?))\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:non-match>b</fn:non-match><fn:match><fn:group nr=\"1\">a<fn:group nr=\"2\">n</fn:group></fn:group></fn:match><fn:match><fn:group nr=\"1\">a<fn:group nr=\"2\">n</fn:group></fn:group></fn:match><fn:match><fn:group nr=\"1\">a<fn:group nr=\"2\"/></fn:group></fn:match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string, groups in alternatives .
   */
  @org.junit.Test
  public void analyzeString009() {
    final XQuery query = new XQuery(
      "analyze-string(\"how now brown cow\", \"(how)|(now)|(brown)|(cow)\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match><fn:group nr=\"1\">how</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"2\">now</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"3\">brown</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"4\">cow</fn:group></fn:match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string, with i flag .
   */
  @org.junit.Test
  public void analyzeString010() {
    final XQuery query = new XQuery(
      "analyze-string(\"how now brown cow\", \"(HOW)|(NOW)|(BROWN)|(COW)\", \"i\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match><fn:group nr=\"1\">how</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"2\">now</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"3\">brown</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"4\">cow</fn:group></fn:match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string, with i and x flag .
   */
  @org.junit.Test
  public void analyzeString011() {
    final XQuery query = new XQuery(
      "analyze-string(\"how now brown cow\", \" (HOW) | (NOW) \n" +
      "| (BROWN) | (COW) \", \"ix\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match><fn:group nr=\"1\">how</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"2\">now</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"3\">brown</fn:group></fn:match><fn:non-match> </fn:non-match><fn:match><fn:group nr=\"4\">cow</fn:group></fn:match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string, with flags set to empty string.
   */
  @org.junit.Test
  public void analyzeString012() {
    final XQuery query = new XQuery(
      "analyze-string(\"how now brown cow\", \"(.*?ow\\s+)+\", \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match>how <fn:group nr=\"1\">now </fn:group></fn:match><fn:non-match>brown cow</fn:non-match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string, with "s" flag.
   */
  @org.junit.Test
  public void analyzeString013() {
    final XQuery query = new XQuery(
      "let $in := \n" +
      "\"Mary had a little lamb,\n" +
      "it's fleece was black as soot,\n" +
      "and everywhere that Mary went,\n" +
      "it put its sooty foot.\"\n" +
      "            return analyze-string($in, \"Mary.*foot\", \"s\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match>Mary had a little lamb,\nit's fleece was black as soot,\nand everywhere that Mary went,\nit put its sooty foot</fn:match><fn:non-match>.</fn:non-match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  analyze-string, multiple lines without "s" flag .
   */
  @org.junit.Test
  public void analyzeString014() {
    final XQuery query = new XQuery(
      "let $in := \n" +
      "\"Mary had a little lamb,\n" +
      "it's fleece was black as soot,\n" +
      "and everywhere that Mary went,\n" +
      "it put its sooty foot.\"\n" +
      "            return analyze-string($in, \".+\", \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match>Mary had a little lamb,</fn:match><fn:non-match>\n</fn:non-match><fn:match>it's fleece was black as soot,</fn:match><fn:non-match>\n</fn:non-match><fn:match>and everywhere that Mary went,</fn:match><fn:non-match>\n</fn:non-match><fn:match>it put its sooty foot.</fn:match></fn:analyze-string-result>", true)
      )
    );
  }

  /**
   *  analyze-string, multiple lines with "m" flag .
   */
  @org.junit.Test
  public void analyzeString015() {
    final XQuery query = new XQuery(
      "let $in := \n" +
      "\"Mary had a little lamb,\n" +
      "it's fleece was black as soot,\n" +
      "and everywhere that Mary went,\n" +
      "it put its sooty foot.\"\n" +
      "            return analyze-string($in, \"^.+$\", \"m\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match>Mary had a little lamb,</fn:match><fn:non-match>\n</fn:non-match><fn:match>it's fleece was black as soot,</fn:match><fn:non-match>\n</fn:non-match><fn:match>and everywhere that Mary went,</fn:match><fn:non-match>\n</fn:non-match><fn:match>it put its sooty foot.</fn:match></fn:analyze-string-result>", true)
      )
    );
  }

  /**
   * 
   *  analyze-string, multiple lines with "m" flag 
   * .
   */
  @org.junit.Test
  public void analyzeString016() {
    final XQuery query = new XQuery(
      "let $in := \n" +
      "\"Mary had a little lamb,\n" +
      "it's fleece was black as soot,\n" +
      "and everywhere that Mary went,\n" +
      "it put its sooty foot.\"\n" +
      "            return analyze-string($in, \"^.+$\", \"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:non-match>Mary had a little lamb,\nit's fleece was black as soot,\nand everywhere that Mary went,\nit put its sooty foot.</fn:non-match></fn:analyze-string-result>", true)
      )
    );
  }

  /**
   *  subtle distinction in the positioning of an empty captured group .
   */
  @org.junit.Test
  public void analyzeString017() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"(b)(x?)\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match><fn:group nr=\"1\">b</fn:group><fn:group nr=\"2\"/></fn:match><fn:non-match>anana</fn:non-match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  subtle distinction in the positioning of an empty captured group.
   */
  @org.junit.Test
  public void analyzeString017a() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"(b(x?))\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match><fn:group nr=\"1\">b<fn:group nr=\"2\"/></fn:group></fn:match><fn:non-match>anana</fn:non-match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  non-capturing group indicated by "(?:...)" .
   */
  @org.junit.Test
  public void analyzeString018() {
    final XQuery query = new XQuery(
      "analyze-string(\"banana\", \"(?:b(an)*a)\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:match>ban<fn:group nr=\"1\">an</fn:group>a</fn:match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  "q" flag suppresses grouping.
   */
  @org.junit.Test
  public void analyzeString019() {
    final XQuery query = new XQuery(
      "analyze-string(\"((banana))\", \"(banana)\", \"q\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<fn:analyze-string-result xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"><fn:non-match>(</fn:non-match><fn:match>(banana)</fn:match><fn:non-match>)</fn:non-match></fn:analyze-string-result>", true)
    );
  }

  /**
   *  test string value of result of analyze-string .
   */
  @org.junit.Test
  public void analyzeString022() {
    final XQuery query = new XQuery(
      "let $result := analyze-string(\"banana\", \"(b)(anana)\") return string($result)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "banana")
    );
  }

  /**
   *  test string value of result of analyze-string .
   */
  @org.junit.Test
  public void analyzeString023() {
    final XQuery query = new XQuery(
      "let $result := analyze-string(\"banana\", \"(b)(anana)\") return string($result/fn:match[1])",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "banana")
    );
  }

  /**
   *  analyze-string, error, bad regex pattern.
   */
  @org.junit.Test
  public void analyzeString901() {
    final XQuery query = new XQuery(
      "analyze-string(\"\", \")-(\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORX0002")
    );
  }

  /**
   *  analyze-string, error, bad flags .
   */
  @org.junit.Test
  public void analyzeString902() {
    final XQuery query = new XQuery(
      "analyze-string(\"abc\", \"abc\", \"w\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORX0001")
    );
  }

  /**
   *  analyze-string, error, pattern matches a zero-length string .
   */
  @org.junit.Test
  public void analyzeString903() {
    final XQuery query = new XQuery(
      "analyze-string(\"abc\", \"a|b|c?\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORX0003")
    );
  }
}
