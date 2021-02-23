package org.basex.query.func;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the Java bindings.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class JavaFunctionTest extends SandboxTest {
  /** Tests calling some Java constructors from XQuery. */
  @Test public void constr() {
    // check java: prefix
    query("Q{java:java.lang.Integer}new('123')", 123);
    query("Q{java:java.lang.Integer}new\u00b7java.lang.String('456')", 456);
    query("Q{java:java.lang.Integer}new\u00b7int(xs:int(789))", 789);

    query("declare namespace f='java:java.util.Random'; f:nextInt(f:new())");
    query("declare namespace f='java:org.basex.util.list.StringList'; f:new()");

    error("declare namespace rand='java:java.util.random'; rand:new()", WHICHCLASS_X);
    error("Q{java:java.util.rndm}new()", WHICHCLASS_X);
    error("Q{java:java.util.random}new()", WHICHCLASS_X);
    error("Q{java:java.lang.Integer}new\u00b7int('abc')", JAVAARGS_X_X_X);
  }

  /** Tests namespace rewritings. */
  @Test public void rewriteURI() {
    query("Q{java.lang.integer}new('123')", 123);
    query("Q{java.lang.Integer}new\u00b7int(xs:int(456))", 456);
    query("declare namespace j='java.util.random'; j:nextInt(j:new())");
    query("declare namespace j='http://basex.org/util/list/string-list'; j:new()");
  }

  /** Tests calling some Java static fields from XQuery. */
  @Test public void staticField() {
    query("Q{java:java.lang.Math}PI()", Math.PI);
    query("Q{java:org.basex.util.Prop}gui()", false);

    query("Q{java:org.basex.util.Prop}debug()", false);
    query("Q{org.basex.util.Prop}debug()", false);
    query("Q{http://basex.org/util/Prop}debug()", false);
  }

  /** Tests calling some Java object fields from XQuery. */
  @Test public void field() {
    query("declare namespace point='java:java.awt.Point'; point:new() => point:x()", 0);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test public void staticMethod() {
    query("Q{java.lang.Math}sqrt(xs:double(9.0))", 3);
    query("Q{java.lang.Math}sqrt\u00b7double(xs:double(9.0))", 3);
    error("Q{java:org.basex.query.func.JavaFunctionExample}error()", JAVAEVAL_X_X_X);

    // sequence types
    query("Q{java:org.basex.util.Strings}eqic('1', (('1','2')))", true);
    query("Q{java:org.basex.util.Strings}eqic('1', (1 to 2) ! string())", true);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test public void method() {
    query("declare namespace rect = 'java.awt.Rectangle';" +
        "rect:new(xs:int(2), xs:int(2)) => rect:contains(xs:int(1), xs:int(1))", true);
    query("declare namespace p = 'java.util.Properties'; p:new()", "{}");
  }

  /** Tests importing a Java class. */
  @Test public void importClass() {
    query("import module namespace set='java.util.HashSet';" +
        "let $a := (set:add('a'), set:add('b')) return set:size()", 2);
    query("import module namespace set='java.util.HashSet';" +
        "let $a := (set:add(128), set:add(128)) return set:size()", 1);
    query("import module namespace set='java.util.HashSet';" +
        "let $a := set:add\u00b7java.lang.Object(128) return set:size()", 1);

    // use class with capital and lower case
    query("import module namespace string='http://lang.java/String'; string:length()", 0);
    query("import module namespace string='http://lang.java/string'; string:length()", 0);

    // handle {@link Jav} type
    query("declare namespace set = 'java.util.HashSet';" +
        "set:add(set:new(), Q{java.awt.Point}new())", true);
  }

  /** Tests importing a query module. */
  @Test public void importQueryModule() {
    // address class extending QueryModule
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:fast(0)", "Apple");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:convenient(xs:int(1))", "Banana");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:functionNS()", "http://www.w3.org/2005/xpath-functions");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:ignore((1 to 1000000000)[. = 0])", "");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:faculty(1 to 5)", "120");
  }

  /** Tests importing a Java class and throwing errors. */
  @Test public void importError() {
    // handle {@link Jav} type
    error("declare namespace string = 'java.lang.String';" +
        "string:concat(string:new(), Q{java.awt.Point}new())", JAVAARGS_X_X_X);
    error("import module namespace qm='java:org.basex.query.func.QueryModuleTest'; qm:xyz()",
        WHICHFUNC_X);
    error("import module namespace qm='java:org.basex.query.func.QueryModuleTest'; qm:fast()",
        FUNCARITY_X_X_X);

    query("declare namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "try{ qm:error(qm:new()) } catch * { $err:code }", "basex:error");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "try { qm:error() } catch * { $err:code }", "basex:error");
  }

  /** Tests ambiguous signatures. */
  @Test public void ambiguous() {
    error("Q{java:org.basex.query.func.JavaFunctionExample}new(true())", DYNMULTICONS_X_X);
    error("import module namespace n='java:java.lang.StringBuilder'; n:append('x')",
        JAVAMULTIFUNC_X_X);
    error("declare namespace n='java:java.lang.StringBuilder';n:append(n:new(), 'x')",
        DYNMULTIFUNC_X_X);
  }

  /** Pass on empty sequences. */
  @Test public void empty() {
    query("declare namespace n='org.basex.query.func.JavaFunctionExample'; n:f(n:new(),())", "");
    query("declare namespace n='org.basex.query.func.JavaFunctionExample'; n:a(n:new(),())", "");
    error("declare namespace n='org.basex.query.func.JavaFunctionExample'; n:b(n:new(),())",
        JAVAARGS_X_X_X);
    error("declare namespace n='org.basex.query.func.JavaFunctionExample'; n:g(n:new(),())",
        DYNMULTIFUNC_X_X);

    query("import module namespace n='org.basex.query.func.JavaFunctionExample'; n:f(())", "");
    error("import module namespace n='org.basex.query.func.JavaFunctionExample'; n:a(())",
        JAVAMULTIFUNC_X_X);
    error("import module namespace n='org.basex.query.func.JavaFunctionExample'; n:b(())",
        JAVAARGS_X_X_X);
    error("import module namespace n='org.basex.query.func.JavaFunctionExample'; n:g(())",
        JAVAMULTIFUNC_X_X);
  }

  /** Pass on empty sequences. */
  @Test public void errors() {
    error("declare namespace n='org.basex.query.func.JavaFunctionExample'; n:x(n:new())",
        WHICHFUNC_X);
    error("import module namespace n='org.basex.query.func.JavaFunctionExample'; n:x()",
        WHICHFUNC_X);
  }

  /** Ensure that items cannot be cast to Java. */
  @Test public void javaCast() {
    error("xs:java('x')", WHICHFUNC_X);
    error("java('x')", WHICHFUNC_X);
    error("'x' cast as xs:java", WHICHCAST_X);
    error("'x' cast as java", WHICHCAST_X);
  }

  /** Static check for Java method/variable names. */
  @Test public void javaNameTest() {
    error("rest:XYZ()", WHICHFUNC_X);
    error("Q{java.lang.String}XYZ()", WHICHFUNC_X);
    error("Q{java:java.lang.String}XYZ()", WHICHFUNC_X);

  }

  /** Pass on Java items to functions. */
  @Test public void funcItem() {
    query("function($x) { $x }(Q{java:java.io.File}new('x'))", "x");
    query("declare function db:f($x) { $x }; db:f#1(Q{java:java.io.File}new('x'))", "x");
  }

  /** URI test. */
  @Test public void uri() {
    query("declare namespace uri = 'java.net.URI'; uri:get-host(uri:new('http://a'))", "a");
    query("declare namespace uri = 'java.net.URI'; uri:get-path(uri:new('http://a/b'))", "/b");
  }

  /** Return Java items. */
  @Test public void data() {
    query("Q{java:java.util.ArrayList}new()", "[]");
  }

  /** Retrieve function items as Java objects. */
  @Test public void toJava() {
    query("import module namespace set = 'java:java.util.HashSet'; set:add(true#0)", "true");
  }

  /** Empty sequences. */
  @Test public void emptySequence() {
    error("Q{java.lang.String}new·java.lang.String(())", JAVAEVAL_X_X_X);
  }
}
