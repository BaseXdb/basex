package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the HTML module.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class HtmlModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void parser() {
    // check if function returns a string
    query(_HTML_PARSER.args() + " instance of xs:string", "true");
  }

  /** Test method. */
  @Test
  public void parse() {
    // check if the function returns a HTML root node
    query(EXISTS.args(_HTML_PARSE.args("&lt;html/&gt;") + "/*:html"), "true");
    // check if the function returns <html/>
    query(_HTML_PARSE.args("&lt;html/&gt;", " map {'nons': true()}"), "<html/>");
  }
}
