package org.basex.query;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.basex.io.serial.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the serializers.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SerializerTest extends SandboxTest {
  /** Test: method=xml. */
  @Test public void xml() {
    query(SerializerOptions.METHOD.arg("xml") + "<html/>", "<html/>");
  }

  /** Test: method=xhtml. */
  @Test public void xhtml() {
    final String option = SerializerOptions.METHOD.arg("xhtml");
    query(option + "<html/>", "<html></html>");
    final String[] empties = { "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "basefont", "frame", "isindex", "param" };
    for(final String e : empties) {
      query(option + "<html xmlns='http://www.w3.org/1999/xhtml'><" + e + "/></html>",
          "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<" + e + " />\n</html>");
    }
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
    final String option = SerializerOptions.METHOD.arg("html");
    query(option + "<html/>", "<html></html>");
    final String[] empties = { "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "basefont", "frame", "isindex", "param" };
    for(final String e : empties) query(option + '<' + e + "/>", '<' + e + '>');

    query(option + "<html><script>&lt;</script></html>",
        "<html>\n<script><</script>\n</html>");
    query(option + "<html><style>{ serialize(<a/>) }</style></html>",
        "<html>\n<style><a/></style>\n</html>");
    query(option + "<a b='&lt;'/>", "<a b=\"<\"></a>");
    error(option + "<a>&#x90;</a>", SERILL_X);

    query(option + "<option selected='selected'/>", "<option selected></option>");

    query(option + "<?x y?>", "<?x y>");
    error(option + "<?x > ?>", SERPI);
  }

  /** Test: method=html, version=5.0. */
  @Test public void version50() {
    final String option = SerializerOptions.METHOD.arg("html") +
        SerializerOptions.VERSION.arg("5.0");
    query(option + "<html/>", "<!DOCTYPE html>\n<html></html>");
    final String[] empties = { "area", "base", "br", "col", "command", "embed", "hr",
        "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr" };
    for(final String e : empties) {
      query(option + '<' + e + "/>", "<!DOCTYPE html>\n<" + e + '>');
    }
    query(option + "<a>&#x90;</a>", "<!DOCTYPE html>\n<a>&#x90;</a>");
    query(option + "<html/>", "<!DOCTYPE html>\n<html></html>");
  }

  /** Test: method=text. */
  @Test public void text() {
    final String option = SerializerOptions.METHOD.arg("text");
    query(option + "1,2", "1 2");
    query(option + "<a>1</a>", 1);
    query(option + "1,<a>2</a>,3", 123);
    query(option + SerializerOptions.USE_CHARACTER_MAPS.arg(";=,,") + "'1;2'", "1,2");
  }

  /** Test: item-separator. */
  @Test public void itemSeparator() {
    query(SerializerOptions.ITEM_SEPARATOR.arg("-") + "1,2", "1-2");
    query(SerializerOptions.ITEM_SEPARATOR.arg("") + "1,2", 12);
    query(SerializerOptions.ITEM_SEPARATOR.arg("ABC") + "1 to 3", "1ABC2ABC3");

    query(SerializerOptions.ITEM_SEPARATOR.arg("&#xa;") + "<a/>,<b/>", "<a/>\n<b/>");
    query(SerializerOptions.ITEM_SEPARATOR.arg("&#xa;") +
        SerializerOptions.METHOD.arg("text") + "1,2", "1\n2");
  }

  /** Test: xml:space='preserve'. */
  @Test public void preserve() {
    query("<a xml:space='preserve'>T<b/></a>", "<a xml:space=\"preserve\">T<b/></a>");
    query("<a xml:space='default'>T<b/></a>", "<a xml:space=\"default\">T<b/>\n</a>");
    query("<a xml:space='x'>T<b/></a>", "<a xml:space=\"x\">T<b/>\n</a>");

    String option = SerializerOptions.INDENT.arg("yes");
    query(option + "<a xml:space='preserve'>T<b/></a>", "<a xml:space=\"preserve\">T<b/></a>");
    query(option + "<a xml:space='default'>T<b/></a>", "<a xml:space=\"default\">T<b/>\n</a>");
    query(option + "<a xml:space='x'>T<b/></a>", "<a xml:space=\"x\">T<b/>\n</a>");

    option = SerializerOptions.INDENT.arg("no");
    query(option + "<a xml:space='preserve'>T<b/></a>", "<a xml:space=\"preserve\">T<b/></a>");
    query(option + "<a xml:space='default'>T<b/></a>", "<a xml:space=\"default\">T<b/></a>");
    query(option + "<a xml:space='x'>T<b/></a>", "<a xml:space=\"x\">T<b/></a>");
  }
}
