package org.basex.query;

import static org.basex.query.QueryError.*;

import java.util.*;

import static org.basex.io.serial.SerializerOptions.*;

import org.basex.*;
import org.basex.io.serial.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the serializers.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class SerializerTest extends SandboxTest {
  /** Test: method=xml. */
  @Test public void xml() {
    query(METHOD.arg("xml") + "<html/>", "<html/>");
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
    query(option + SerializerOptions.INDENT.arg("yes")
        + "<html xmlns='http://www.w3.org/1999/xhtml'><body><pre><u>test</u></pre></body></html>",
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
        + "<body>\n"
        + "<pre><u>test</u></pre>\n"
        + "</body>\n"
        + "</html>");
  }

  /** method=xhtml, meta element. */
  @Test public void gh1933() {
    final String query1 = "let $string := serialize("
        + "<head><meta http-equiv='Content-Type'/></head>";
    final String query2 = ", map { 'method': 'xhtml' })"
        + "return count(analyze-string($string, '<meta ')//fn:match)";

    query(query1 + query2, 1);
    query(query1 + "update {}" + query2, 1);
  }

  /** Test: method=html. */
  @Test public void html() {
    final String option = METHOD.arg("html");
    query(option + "<html/>", "<html></html>");
    final String[] empties = { "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "basefont", "frame", "isindex", "param" };
    for(final String e : empties) query(option + '<' + e + "/>", '<' + e + '>');

    query(option + "<html><script>&lt;</script></html>",
        "<html><script><</script></html>");
    query(option + "<html><style>{ serialize(<a/>) }</style></html>",
        "<html><style><a/></style></html>");
    query(option + "<a b='&lt;'/>", "<a b=\"<\"></a>");
    error(option + "<a>&#x90;</a>", SERILL_X);

    query(option + "<option selected='selected'/>", "<option selected></option>");

    query(option + "<?x y?>", "<?x y>");
    error(option + "<?x > ?>", SERPI);

    query(option + SerializerOptions.INDENT.arg("yes")
        + "<html><body><PRE><u>test</u></PRE></body></html>",
        "<html>\n"
        + "<body>\n"
        + "<PRE><u>test</u></PRE>\n"
        + "</body>\n"
        + "</html>");
  }

  /** Test: method=html, version=5.0. */
  @Test public void version50() {
    final String option = METHOD.arg("html") + VERSION.arg("5.0");
    query(option + "<html/>", "<!DOCTYPE html><html></html>");
    final String[] empties = { "area", "base", "br", "col", "command", "embed", "hr",
        "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr" };
    for(final String e : empties) {
      query(option + '<' + e + "/>", "<!DOCTYPE html><" + e + '>');
    }
    query(option + "<a>&#x90;</a>", "<!DOCTYPE html><a>&#x90;</a>");
    query(option + "<html/>", "<!DOCTYPE html><html></html>");
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
}
