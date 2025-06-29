package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the HTML module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HtmlModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void doc() {
    final Function func = _HTML_DOC;
    query(func.args(" ()"), "");
    query(func.args(" []"), "");
    query(func.args(" <_/>/text()"), "");

    final String path = "src/test/resources/input.html";
    query(func.args(path) + "//body ! name()", "body");
    query(func.args(path, " { 'nons': false() }") + "//*:body ! name()", "body");
    query(func.args(path, " { 'method': 'nu' }") + "//Q{http://www.w3.org/1999/xhtml}body ! name()",
        "body");
  }

  /** Test method. */
  @Test public void parse() {
    final Function func = _HTML_PARSE;
    query(func.args(" ()"), "");
    query(func.args(" []"), "");

    // check if the function returns an HTML root node
    query("exists(" + func.args("&lt;html/&gt;") + "/*:html)", true);
    // check if the function returns <html/>
    query(func.args("&lt;html/&gt;", " { 'nons': true() }"), "<html/>");
    query(func.args("&lt;html/&gt;", " { 'method': 'nu' }"),
        "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head/><body/></html>");
  }

  /** Test method. */
  @Test public void parser() {
    final Function func = _HTML_PARSER;
    // check if function returns a string
    query(func.args() + " instance of xs:string", true);
  }
}
