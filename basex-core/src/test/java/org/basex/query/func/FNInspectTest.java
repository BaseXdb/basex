package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Inspection Module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNInspectTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void function() {
    String func = query(_INSPECT_FUNCTION.args(" true#0"));
    query(func + "/@name/data()", "true");
    query(func + "/@uri/data()", "http://www.w3.org/2005/xpath-functions");
    query(func + "/return/@type/data()", "xs:boolean");
    query(func + "/return/@occurrence/data()", "");

    func = query(_INSPECT_FUNCTION.args(" { }"));
    query(func + "/@name/data()", "");
    query(func + "/@uri/data()", "");
    query(func + "/argument/@type/data()", "xs:anyAtomicType");
    query(func + "/return/@type/data()", "item()");
    query(func + "/return/@occurrence/data()", "*");

    func = query(_INSPECT_FUNCTION.args(" function($a as xs:string) as item() { $a }"));
    query(func + "/@name/data()", "");
    query(func + "/@uri/data()", "");
    query(func + "/argument/@name/data()", "");
    query(func + "/argument/@type/data()", "xs:string");
    query(func + "/return/@type/data()", "item()");
    query(func + "/return/@occurrence/data()", "");

    func = query("declare %private function Q{U}f($v as xs:int) as xs:integer {$v};" +
        _INSPECT_FUNCTION.args(" Q{U}f#1"));
    query(func + "/@name/data()", "f");
    query(func + "/@uri/data()", "U");
    query(func + "/argument/@name/data()", "v");
    query(func + "/argument/@type/data()", "xs:int");
    query(func + "/annotation/@name/data()", "private");
    query(func + "/annotation/@uri/data()", "http://www.w3.org/2012/xquery");
    query(func + "/return/@type/data()", "xs:integer");
    query(func + "/return/@occurrence/data()", "");
  }

  /** Test method. */
  @Test
  public void module() {
    error(_INSPECT_MODULE.args("src/test/resources/non-existent.xqm"), Err.WHICHRES);

    final String module = "src/test/resources/hello.xqm";
    final String result = query(_INSPECT_MODULE.args(module));
    final String var = query(result + "/variable[@name = 'hello:lazy']");
    query(var + "/@uri/data()", "world");
    query(var + "/annotation/@name/data()", "basex:lazy");
    query(var + "/annotation/@uri/data()", "http://basex.org");

    final String func = query(result + "/function[@name = 'hello:world']");
    query(func + "/@uri/data()", "world");
    query(func + "/annotation/@name/data()", "public");
    query(func + "/annotation/@uri/data()", "http://www.w3.org/2012/xquery");
    query(func + "/return/@type/data()", "xs:string");
    query(func + "/return/@occurrence/data()", "");
  }

  /** Test method. */
  @Test
  public void xqdoc() {
    error(_INSPECT_XQDOC.args("src/test/resources/non-existent.xqm"), Err.WHICHRES);

    // validate against xqDoc schema
    final String result = query(_INSPECT_XQDOC.args("src/test/resources/hello.xqm"));
    query(_VALIDATE_XSD.args(result, "src/test/resources/xqdoc.xsd"));
  }

  /** Test method. */
  @Test
  public void context() {
    final String func = query("declare function local:x() { 1 }; " +
        _INSPECT_CONTEXT.args());
    query(func + "/name()", "context");
    query(COUNT.args(func + "/function"), "1");
    query(func + "/function/@name/string()", "local:x");
  }

  /** Test method. */
  @Test
  public void contextFunctions() {
    query("declare function local:x() { 1 }; " +
        COUNT.args(_INSPECT_FUNCTIONS.args()), "1");
    query("declare function local:x() { 2 }; " +
        _INSPECT_FUNCTIONS.args() + "()", "2");
  }
}
