package org.basex.query;

import static org.basex.query.QueryError.*;

import java.util.*;

import static org.basex.io.serial.SerializerOptions.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the serializers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SerializerTest extends SandboxTest {
  /** Test: method=xml. */
  @Test public void xml() {
    query(METHOD.arg("xml") + "<html/>", "<html/>");
    query(METHOD.arg("xml") + INDENT_ATTRIBUTES.arg("yes") + "<x a='1' b='2' c='3'/>",
        "<x a=\"1\"\n"
        + "   b=\"2\"\n"
        + "   c=\"3\"/>");
  }

  /** Test: method=xhtml. */
  @Test public void xhtml() {
    final String option = METHOD.arg("xhtml");
    query(option + "<html/>", "<html></html>");
    final String[] empties = { "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "basefont", "frame", "isindex", "param" };
    for(final String e : empties) {
      query(option + "<html xmlns='http://www.w3.org/1999/xhtml'><" + e + "/></html>",
          "<html xmlns=\"http://www.w3.org/1999/xhtml\"><" + e + " /></html>");
    }
    query(option + INDENT.arg("yes")
        + "<html xmlns='http://www.w3.org/1999/xhtml'><body><pre><u>test</u></pre></body></html>",
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
        + "  <body>\n"
        + "    <pre><u>test</u></pre>\n"
        + "  </body>\n"
        + "</html>");
    query(option + INDENT.arg("yes") + HTML_VERSION.arg("5.0")
        + "<html><body><PRE><u>test</u></PRE></body></html>",
        "<!DOCTYPE html>\n"
        + "<html>\n"
        + "  <body>\n"
        + "    <PRE><u>test</u></PRE>\n"
        + "  </body>\n"
        + "</html>");
    query(option + INDENT.arg("yes") + HTML_VERSION.arg("5.0")
        + "<html><body><a name='x'>x</a><p><hr/></p><a><hr/></a><br/></body></html>",
        "<!DOCTYPE html>\n"
        + "<html>\n"
        + "  <body><a name=\"x\">x</a><p>\n"
        + "      <hr/>\n"
        + "    </p><a>\n"
        + "      <hr/>\n"
        + "    </a><br/></body>\n"
        + "</html>");
    query(option + MEDIA_TYPE.arg("application/xhtml+xml")
        + INDENT.arg("yes")
        + INDENT_ATTRIBUTES.arg("yes")
        + "<html xmlns='http://www.w3.org/1999/xhtml' xmlns:svg='http://www.w3.org/2000/svg'>"
        + "<head/><body><div id='a' name='b' class='c' style='width: 42px'/></body></html>",
        "<html xmlns=\"http://www.w3.org/1999/xhtml\"\n"
        + "      xmlns:svg=\"http://www.w3.org/2000/svg\">\n"
        + "  <head>\n"
        + "    <meta http-equiv=\"Content-Type\"\n"
        + "          content=\"application/xhtml+xml; charset=UTF-8\"/>\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <div id=\"a\"\n"
        + "         name=\"b\"\n"
        + "         class=\"c\"\n"
        + "         style=\"width: 42px\"></div>\n"
        + "  </body>\n"
        + "</html>");
    // URI escaping enabled: href and name will be percent-encoded
    query(option
        + "<html xmlns='http://www.w3.org/1999/xhtml'><body><a href='\u8f49\u7fa9.html'"
        + " name='\u8f49\u7fa9'>Link</a></body></html>",
        "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body><a href=\"%E8%BD%89%E7%BE%A9.html\""
        + " name=\"%E8%BD%89%E7%BE%A9\">Link</a></body></html>");
    // URI escaping disabled: raw Unicode is preserved
    query(option + ESCAPE_URI_ATTRIBUTES.arg("no")
        + "<html xmlns='http://www.w3.org/1999/xhtml'><body><a href='\u672a\u8f49\u7fa9.html'"
        + " name='\u672a\u8f49\u7fa9'>Link</a></body></html>",
        "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body><a href=\"\u672a\u8f49\u7fa9.html\""
        + " name=\"\u672a\u8f49\u7fa9\">Link</a></body></html>");
  }

  /** method=xhtml, meta element. */
  @Test public void gh1933() {
    final String query1 = "let $string := serialize("
        + "<head><meta http-equiv='Content-Type'/></head>";
    final String query2 = ", { 'method': 'xhtml' })"
        + "return count(analyze-string($string, '<meta ')//fn:match)";

    query(query1 + query2, 1);
    query(query1 + "update {}" + query2, 1);
  }

  /** Test: method=html. */
  @Test public void html() {
    final String option = METHOD.arg("html");
    query(option + "<html/>", "<!DOCTYPE HTML><html></html>");
    final String[] empties = { "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "basefont", "frame", "isindex", "param" };
    for(final String e : empties) {
      query(option + '<' + e + "/>", '<' + e + '>');
    }

    query(option + "<html><script>&lt;</script></html>",
        "<!DOCTYPE HTML><html><script><</script></html>");
    query(option + "<html><style>{ serialize(<a/>) }</style></html>",
        "<!DOCTYPE HTML><html><style><a/></style></html>");
    query(option + "<a b='&lt;'/>", "<a b=\"<\"></a>");

    query(option + "<a>&#x90;</a>", "<a>&#x90;</a>");
    error(option + HTML_VERSION.arg("4.01") + "<a>&#x90;</a>", SERILL_X);

    query(option + "<option selected='selected'/>", "<option selected></option>");

    query(option + "<?x y?>", "<?x y>");
    error(option + "<?x > ?>", SERPI);

    query(option + INDENT.arg("yes")
        + "<html><body><PRE><u>test</u></PRE></body></html>",
        "<!DOCTYPE HTML>\n"
        + "<html>\n"
        + "  <body>\n"
        + "    <PRE><u>test</u></PRE>\n"
        + "  </body>\n"
        + "</html>");
    query(option + INDENT.arg("yes") + HTML_VERSION.arg("5.0")
        + "<html><body><PRE><u>test</u></PRE></body></html>",
        "<!DOCTYPE HTML>\n"
        + "<html>\n"
        + "  <body>\n"
        + "    <PRE><u>test</u></PRE>\n"
        + "  </body>\n"
        + "</html>");
    query(option + INDENT.arg("yes") + HTML_VERSION.arg("5.0")
        + "<html><body><p><b>x</b><ul><li>1</li><li>2</li></ul></p><br/></body></html>",
        "<!DOCTYPE HTML>\n"
        + "<html>\n"
        + "  <body>\n"
        + "    <p><b>x</b><ul>\n"
        + "        <li>1</li>\n"
        + "        <li>2</li>\n"
        + "      </ul>\n"
        + "    </p><br></body>\n"
        + "</html>");
    query(option + INDENT_ATTRIBUTES.arg("yes") + INDENT.arg("yes")
        + "<html><body onload='alert(\"loaded\")' style='background: black'/></html>",
        "<!DOCTYPE HTML>\n"
        + "<html>\n"
        + "  <body onload=\"alert(&quot;loaded&quot;)\"\n"
        + "        style=\"background: black\"></body>\n"
        + "</html>");
  }

  /** Test: method=html, version=5.0. */
  @Test public void version50() {
    final String option = METHOD.arg("html") + VERSION.arg("5.0");
    query(option + "<html/>", "<!DOCTYPE HTML><html></html>");
    final String[] empties = { "area", "base", "br", "col", "command", "embed", "hr",
        "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr" };
    for(final String e : empties) {
      query(option + '<' + e + "/>", "<" + e + '>');
    }
    query(option + "<a>&#x90;</a>", "<a>&#x90;</a>");
    query(option + "<html/>", "<!DOCTYPE HTML><html></html>");
  }

  /** Test: method=text. */
  @Test public void text() {
    final String option = METHOD.arg("text");
    query(option + "1, 2", "1 2");
    query(option + "<a>1</a>", 1);
    query(option + "1, <a>2</a>, 3", 123);
    query(option + USE_CHARACTER_MAPS.arg(";=,,") + "'1;2'", "1,2");
  }

  /** Test: item-separator. */
  @Test public void itemSeparator() {
    query(ITEM_SEPARATOR.arg("-") + "1, 2", "1-2");
    query(ITEM_SEPARATOR.arg("") + "1, 2", 12);
    query(ITEM_SEPARATOR.arg("ABC") + "1 to 3", "1ABC2ABC3");

    query(ITEM_SEPARATOR.arg("&#xa;") + "<a/>, <b/>", "<a/>\n<b/>");
    query(ITEM_SEPARATOR.arg("&#xa;") + METHOD.arg("text") + "1, 2", "1\n2");

    // GH-2163: XQuery, serialization item-separator option
    Map.of("xml", "", "text", "", "adaptive", "[]").forEach((key, value) ->
      query(METHOD.arg(key) + ITEM_SEPARATOR.arg("|") + "[]", value));
    Map.of("xml", 1, "text", 1, "adaptive", "[1]").forEach((key, value) ->
      query(METHOD.arg(key) + ITEM_SEPARATOR.arg("|") + "[1]", value));
    Map.of("xml", "", "text", "", "adaptive", "[]|[]").forEach((key, value) ->
      query(METHOD.arg(key) + ITEM_SEPARATOR.arg("|") + "[], []", value));
    Map.of("xml", "1|2", "text", "1|2", "adaptive", "[1]|[2]").forEach((key, value) ->
      query(METHOD.arg(key) + ITEM_SEPARATOR.arg("|") + "[ 1 ], [ 2 ]", value));
    Map.of("xml", "1|2|3", "text", "1|2|3", "adaptive", "[1,2]|[3]").forEach((key, value) ->
      query(METHOD.arg(key) + ITEM_SEPARATOR.arg("|") + "[ 1, 2 ], [ 3 ]", value));
    Map.of("xml", "1|2", "text", "1|2", "adaptive", "1|[[2]]|[]").forEach((key, value) ->
      query(METHOD.arg(key) + ITEM_SEPARATOR.arg("|") + "1, [[ 2 ]], []", value));
    Map.of("xml", "1|2|3", "text", "1|2|3", "adaptive", "1|[2,3]").forEach((key, value) ->
      query(METHOD.arg(key) + ITEM_SEPARATOR.arg("|") + "1, [ 2, 3 ]", value));
  }

  /** Test: xml:space='preserve'. */
  @Test public void preserve() {
    query("<a xml:space='preserve'>T<b/></a>", "<a xml:space=\"preserve\">T<b/></a>");
    query("<a xml:space='default'>T<b/></a>", "<a xml:space=\"default\">T<b/></a>");
    query("<a xml:space='x'>T<b/></a>", "<a xml:space=\"x\">T<b/></a>");

    String option = INDENT.arg("yes");
    query(option + "<a xml:space='preserve'>T<b/></a>", "<a xml:space=\"preserve\">T<b/></a>");
    query(option + "<a xml:space='default'>T<b/></a>", "<a xml:space=\"default\">T<b/>\n</a>");
    query(option + "<a xml:space='x'>T<b/></a>", "<a xml:space=\"x\">T<b/>\n</a>");

    option = INDENT.arg("no");
    query(option + "<a xml:space='preserve'>T<b/></a>", "<a xml:space=\"preserve\">T<b/></a>");
    query(option + "<a xml:space='default'>T<b/></a>", "<a xml:space=\"default\">T<b/></a>");
    query(option + "<a xml:space='x'>T<b/></a>", "<a xml:space=\"x\">T<b/></a>");
  }

  /** Test: method=json, json-lines=on. */
  @Test public void jsonLines() {
    final String option = METHOD.arg("json") + JSON_LINES.arg("on");
    query(option + "()", "");
    query(option + "1, 2", "1\n2");
    query(option + "[1], { '2': 3 }", "[1]\n{\"2\":3}");

    query(option + INDENT.arg("yes") + "[1], { '2': 3 }", "[ 1 ]\n{ \"2\": 3 }");
  }

  /** Test: method=adaptive. */
  @Test public void adaptive() {
    final String option = METHOD.arg("adaptive");
    query(option + "()", "");
    query(option + "1", 1);
    query(option + "1.0", 1);
    query(option + "1e0", "1.0e0");
    query(option + "1234567890e0", "1.23456789e9");
    query(option + "xs:double('NaN')", "NaN");
    query(option + "xs:double('INF')", "INF");
    query(option + "xs:byte(1)", 1);
    query(option + "false()", "false()");
    query(option + "'A'", "\"A\"");
    query(option + "xs:anyURI('A')", "\"A\"");
    query(option + "xs:untypedAtomic('A')", "\"A\"");
    query(option + "xs:QName('xml:a')", "#Q{http://www.w3.org/XML/1998/namespace}a");
    query(option + "xs:dayTimeDuration('P1D')", "xs:duration(\"P1D\")");
    query(option + "<xml><a>B</a></xml>", "<xml><a>B</a></xml>");
    query(option + "true#0", "fn:true#0");
    query(option + "fn() {}", "(anonymous-function)#0");
    query(option + "xs:float(1)", "xs:float(\"1\")");

    query(option + "[]", "[]");
    query(option + "[ 1 ]", "[1]");
    query(option + "[ 1.0 ]", "[1]");
    query(option + "[ 1e0 ]", "[1.0e0]");
    query(option + "[ 1234567890e0 ]", "[1.23456789e9]");
    query(option + "[ xs:double('NaN') ]", "[NaN]");
    query(option + "[ xs:double('INF') ]", "[INF]");
    query(option + "[ xs:byte(1) ]", "[1]");
    query(option + "[ false() ]", "[false()]");
    query(option + "[ 'A' ]", "[\"A\"]");
    query(option + "[ xs:anyURI('A') ]", "[\"A\"]");
    query(option + "[ xs:untypedAtomic('A') ]", "[\"A\"]");
    query(option + "[ xs:QName('xml:a') ]", "[#Q{http://www.w3.org/XML/1998/namespace}a]");
    query(option + "[ xs:dayTimeDuration('P1D') ]", "[xs:duration(\"P1D\")]");
    query(option + "[ <xml><a>B</a></xml> ]", "[<xml><a>B</a></xml>]");
    query(option + "[ true#0 ]", "[fn:true#0]");
    query(option + "[ fn() {} ]", "[(anonymous-function)#0]");
    query(option + "[ xs:float(1) ]", "[xs:float(\"1\")]");

    query(option + "{ 1: (), 2: 3, 4: (5, 6) }", "{1:(),2:3,4:(5,6)}");
  }

  /** Test: method=basex. */
  @Test public void basex() {
    query("()", "");
    query("1", 1);
    query("1.0", 1);
    query("1e0", 1);
    query("1234567890e0", 1234567890);
    query("xs:double('NaN')", "NaN");
    query("xs:double('INF')", "INF");
    query("xs:byte(1)", 1);
    query("false()", "false");
    query("'A'", "A");
    query("xs:anyURI('A')", "A");
    query("xs:untypedAtomic('A')", "A");
    query("xs:QName('xml:a')", "#xml:a");
    query("xs:dayTimeDuration('P1D')", "P1D");
    query("<xml><a>B</a></xml>", "<xml><a>B</a></xml>");
    query("true#0", "fn:true#0");
    query("fn() {}", "fn() as empty-sequence() { () }");
    query("xs:float(1)", 1);

    query("[]", "[]");
    query("[ 1 ]", "[1]");
    query("[ 1.0 ]", "[1]");
    query("[ 1e0 ]", "[1]");
    query("[ 1234567890e0 ]", "[1234567890]");
    query("[ xs:double('NaN') ]", "[NaN]");
    query("[ xs:double('INF') ]", "[INF]");
    query("[ xs:byte(1) ]", "[1]");
    query("[ false() ]", "[false()]");
    query("[ 'A' ]", "[\"A\"]");
    query("[ xs:anyURI('A') ]", "[\"A\"]");
    query("[ xs:untypedAtomic('A') ]", "[\"A\"]");
    query("[ xs:QName('xml:a') ]", "[#xml:a]");
    query("[ xs:dayTimeDuration('P1D') ]", "[\"P1D\"]");
    query("[ <xml><a>B</a></xml> ]", "[<xml><a>B</a></xml>]");
    query("[ true#0 ]", "[fn:true#0]");
    query("[ fn() {} ]", "[fn() as empty-sequence() { () }]");
    query("[ xs:float(1) ]", "[\"1\"]"); // should be revised for 'adaptive' method

    query("{ 1: (), 2: 3, 4: (5, 6) }", "{1:(),2:3,4:(5,6)}");
  }

  /** HTML Serialization: escaping entities in multiple script elements. */
  @Test public void gh2575() {
    final String option = METHOD.arg("html");
    contains(option + "<html><script>&amp;&amp;</script></html>", ">&&<");

    contains(option + "<html><style>123</style><script>&amp;&amp;</script></html>", ">&&<");
    contains(option + "<html><style>&amp;&amp;</style><script>123</script></html>", ">&&<");
    contains(option + "<html><head><style/></head><script>&amp;&amp;</script></html>", ">&&<");

    contains(option + "<html><style/><script>&amp;&amp;</script></html>", ">&&<");
    contains(option + "<html><style/><body><script>&amp;&amp;</script></body></html>", ">&&<");
  }

  /** HTML5 indentation. */
  @Test public void indentHTML5() {
    final String option = METHOD.arg("html") + INDENT.arg("on");
    query(option + "<html/>",
        "<!DOCTYPE HTML>\n<html></html>");
    query(option + "<html><head/></html>",
        "<!DOCTYPE HTML>\n<html>\n  <head>\n    <meta charset=\"UTF-8\">\n  </head>\n</html>");
    query(option + "<html><script/></html>",
        "<!DOCTYPE HTML>\n<html>\n  <script></script>\n</html>");
    query(option + "<html><meta/></html>",
        "<!DOCTYPE HTML>\n<html>\n  <meta>\n</html>");
    query(option + "<html><meta/><meta/></html>",
        "<!DOCTYPE HTML>\n<html>\n  <meta>\n  <meta>\n</html>");
  }

  /** Canonical serialization. */
  @Test public void canonical() {
    final String option = METHOD.arg("xml") + CANONICAL.arg("yes");
    // relative namespace URIs are not allowed
    error(option + "<x xmlns='relative'/>", SERCANONURI);
    // document must only have one element root node
    error(option + "document { <x/>, <x/> }", SERCANONROOTS_X);
    // document must only have one element root node (use 'update {}': create database node)
    error(option + "document { <x/>, <x/> } update {}", SERCANONROOTS_X);
  }
}
