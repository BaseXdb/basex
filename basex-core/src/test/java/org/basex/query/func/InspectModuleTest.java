package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Inspection Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InspectModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void context() {
    final Function func = _INSPECT_CONTEXT;
    // queries
    final String query = query("declare function local:x() { 1 }; " + func.args());
    query(query + "/name()", "context");
    query("count(" + query + "/function)", 1);
    query(query + "/function/@name/string()", "local:x");
  }

  /** Test method. */
  @Test public void function() {
    final Function func = _INSPECT_FUNCTION;
    // queries
    String query = query(func.args(" true#0"));
    query(query + "/@name/data()", true);
    query(query + "/@uri/data()", "http://www.w3.org/2005/xpath-functions");
    query(query + "/return/@type/data()", "xs:boolean");
    query(query + "/return/@occurrence/data()", "");

    query = query(func.args(" map { }"));
    query(query + "/@name/data()", "");
    query(query + "/@uri/data()", "");
    query(query + "/argument/@type/data()", "xs:anyAtomicType");
    query(query + "/return/@type/data()", "item()");
    query(query + "/return/@occurrence/data()", "*");

    query = query(func.args(" function($a as xs:int) as xs:integer { $a + 1 }"));
    query(query + "/@name/data()", "");
    query(query + "/@uri/data()", "");
    query(query + "/argument/@name/data()", "");
    query(query + "/argument/@type/data()", "xs:int");
    query(query + "/return/@type/data()", "xs:integer");
    query(query + "/return/@occurrence/data()", "");

    query = query("declare %private function Q{U}f($v as xs:int) as xs:integer { $v };" +
        func.args(" Q{U}f#1"));
    query(query + "/@name/data()", "f");
    query(query + "/@uri/data()", "U");
    query(query + "/argument/@name/data()", "v");
    query(query + "/argument/@type/data()", "xs:int");
    query(query + "/annotation/@name/data()", "private");
    query(query + "/annotation/@uri/data()", "http://www.w3.org/2012/xquery");
    query(query + "/return/@type/data()", "xs:int");
    query(query + "/return/@occurrence/data()", "");

    // unknown annotation
    query("declare namespace pref='uri';" +
        func.args(" %pref:x function() {()}") + "/annotation/@name/data()", "pref:x");
    query("declare namespace pref='uri';" +
        func.args(" %pref:x function() {()}") + "/annotation/@uri/data()", "uri");
  }

  /** Test method. */
  @Test public void functionAnnotations() {
    final Function func = _INSPECT_FUNCTION_ANNOTATIONS;
    // queries
    query(func.args(" true#0"), "map {\n}");
    query(func.args(" %local:x function() { }") +
        "=> " + _MAP_CONTAINS.args(" xs:QName('local:x')"), true);
    query(func.args(" %Q{uri}name('a','b') function() {}") +
        " (QName('uri','name'))", "a\nb");
    query(_MAP_SIZE.args(func.args(" %basex:inline %basex:lazy function() {}")), 2);
  }

  /** Test method. */
  @Test public void functions() {
    final Function func = _INSPECT_FUNCTIONS;
    // queries
    final String url = "src/test/resources/hello.xqm";
    query("declare function local:x() { 1 }; " + COUNT.args(func.args()), 1);
    query("declare function local:x() { 2 }; " + func.args() + "()", 2);
    query("import module namespace hello='world' at '" + url + "';" +
        func.args() + "[last()] instance of function(*)", true);

    query("for $f in " + func.args(url)
        + "where local-name-from-QName(function-name($f)) = 'world' "
        + "return $f()", "hello world");

    // ensure that closures will be compiled (GH-1194)
    query(func.args(url)
        + "[function-name(.) = QName('world','closure')]()", 1);
    query("import module namespace hello='world' at '" + url + "';"
        + func.args() + "[function-name(.) = xs:QName('hello:closure')]()", 1);
  }

  /** Test method. */
  @Test public void module() {
    final Function func = _INSPECT_MODULE;
    // queries
    error(func.args("src/test/resources/non-existent.xqm"), WHICHRES_X);

    final String module = "src/test/resources/hello.xqm";
    final String result = query(func.args(module)).replace("{", "{{").replace("}", "}}");

    final String var1 = query(result + "/variable[@name = 'hello:lazy']");
    query(var1 + "/@uri/data()", "world");
    query(var1 + "/@external/data()", false);
    query(var1 + "/annotation/@name/data()", "basex:lazy");
    query(var1 + "/annotation/@uri/data()", "http://basex.org");

    final String var2 = query(result + "/variable[@name = 'hello:ext']");
    query(var2 + "/@external/data()", true);

    final String query1 = query(result + "/function[@name = 'hello:world']");
    query(query1 + "/@uri/data()", "world");
    query(query1 + "/@external/data()", false);
    query(query1 + "/annotation/@name/data()", "public");
    query(query1 + "/annotation/@uri/data()", "http://www.w3.org/2012/xquery");
    query(query1 + "/return/@type/data()", "xs:string");
    query(query1 + "/return/@occurrence/data()", "");

    final String query2 = query(result + "/function[@name = 'hello:internal']").
        replace("{", "{{").replace("}", "}}");
    query(query2 + "/@uri/data()", "world");
    query(query2 + "/annotation/@name/data()[ends-with(., 'ignored')]", "ignored");
    query(query2 + "/annotation/@uri/data()[. = 'ns']", "ns");

    final String query3 = query(result + "/function[@name = 'hello:ext']");
    query(query3 + "/@external/data()", true);
  }

  /** Test method. */
  @Test public void staticContext() {
    final Function func = _INSPECT_STATIC_CONTEXT;
    // queries
    query(func.args(" ()", "namespaces") + "?xml", "http://www.w3.org/XML/1998/namespace");
    query("starts-with(" + func.args(" ()", "base-uri") + ", 'file:')", true);
    query(func.args(" ()", "element-namespace"), "");
    query(func.args(" ()", "function-namespace"), "http://www.w3.org/2005/xpath-functions");
    query(func.args(" ()", "collation"),
        "http://www.w3.org/2005/xpath-functions/collation/codepoint");
    query(func.args(" ()", "boundary-space"), "strip");
    query(func.args(" ()", "ordering"), "ordered");
    query(func.args(" ()", "construction"), "preserve");
    query(func.args(" ()", "default-order-empty"), "least");
    query("declare boundary-space preserve;" + func.args(" ()", "boundary-space"), "preserve");
    query(func.args(" ()", "copy-namespaces"), "preserve\ninherit");
    query(func.args(" ()", "decimal-formats") + "('')('percent')", "%");

    // check different function types
    query(func.args(" true#0", "boundary-space"), "strip");
    query(func.args(" function(){}", "boundary-space"), "strip");
    query(func.args(" function($a){}(?)", "boundary-space"), "strip");

    // errors
    error(func.args(" ()", "unknown"), INSPECT_UNKNOWN_X);
  }

  /** Test method. */
  @Test public void type() {
    final Function func = _INSPECT_TYPE;
    // queries
    query(func.args(" ()"), "empty-sequence()");
    query(func.args(1), "xs:integer");
    query(func.args(" 1 to 2"), "xs:integer+");
    query(func.args(" <_/>"), "element(_)");
    query(func.args(" map { 'a': (1, 2)[. = 1] }"), "map(xs:string, xs:integer*)");
    query(func.args(" map { 'a': 'b' }"), "map(xs:string, xs:string)");
    query(func.args(" array { 1, <a/> }"), "array(item())");
    query(func.args(" array { 1, 2 }"), "array(xs:integer)");
    query(func.args(" function() { 1 }"), "function() as xs:integer");
  }

  /** Test method. */
  @Test public void xqdoc() {
    final Function func = _INSPECT_XQDOC;
    // queries
    error(func.args("src/test/resources/non-existent.xqm"), WHICHRES_X);

    // validate against xqDoc schema
    final String result = query(func.args("src/test/resources/hello.xqm")).
        replace("{", "{{").replace("}", "}}");
    query(_VALIDATE_XSD.args(' ' + result, "src/test/resources/xqdoc.xsd"));
  }
}
