package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the HTML module.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class HtmlModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void parser() {
    final Function func = _HTML_PARSER;
    // check if function returns a string
    query(func.args() + " instance of xs:string", true);
  }

  /** Test method. */
  @Test
  public void parse() {
    final Function func = _HTML_PARSE;
    query(func.args(" ()"), "");
    query(func.args(" []"), "");

    // check if the function returns a HTML root node
    query("exists(" + func.args("&lt;html/&gt;") + "/*:html)", true);
    // check if the function returns <html/>
    query(func.args("&lt;html/&gt;", " map {'nons': true()}"), "<html/>");
  }
}
