package org.basex.query.func;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the Java bindings.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class JavaFunctionTest extends AdvancedQueryTest {
  /** Tests calling some Java constructors from XQuery. */
  @Test
  public void constr() {
    query("Q{java:java.lang.Integer}new('123')", 123);
    query("declare namespace rand='java:java.util.Random'; rand:nextInt(rand:new())");
    query("declare namespace ctx='java:org.basex.core.Context'; ctx:new()");
  }

  /** Tests calling some Java static fields from XQuery. */
  @Test
  public void staticField() {
    query("Q{java:java.lang.Math}PI()", Math.PI);
    query("Q{java:org.basex.util.Prop}gui()");
  }

  /** Tests calling some Java object fields from XQuery. */
  @Test
  public void field() {
    query("declare namespace point='java:java.awt.Point'; point:x(point:new())", 0);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test
  public void staticMethod() {
    query("Q{java.lang.Math}sqrt(xs:double(9.0))", 3);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test
  public void method() {
    query("declare namespace rect = 'java.awt.Rectangle';" +
        "rect:contains(rect:new(xs:int(2), xs:int(2)), xs:int(1), xs:int(1))", true);
    query("declare namespace p = 'java.util.Properties'; p:new()", "{}");
  }

  /** Tests importing a Java class. */
  @Test
  public void importClass() {
    query("import module namespace set='java.util.HashSet';" +
        "let $a := (set:add('a'), set:add('b')) return set:size()", "2");
    query("import module namespace set='java.util.HashSet';" +
        "let $a := (set:add(128), set:add(128)) return set:size()", "1");

    // use class with capital and lower case
    query("import module namespace string='http://lang.java/String'; string:length()", "0");
    query("import module namespace string='http://lang.java/string'; string:length()", "0");

    // handle {@link Jav} type
    query("declare namespace set = 'java.util.HashSet';" +
        "set:add(set:new(), Q{java.awt.Point}new())", "true");
  }

  /** Tests importing a query module. */
  @Test
  public void importQueryModule() {
    // address class extending QueryModule
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:fast(0)", "Apple");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:convenient(xs:int(1))", "Banana");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "qm:functionNS()", "http://www.w3.org/2005/xpath-functions");
  }

  /** Tests importing a Java class and throwing errors. */
  @Test
  public void importError() {
    // handle {@link Jav} type
    error("declare namespace string = 'java.lang.String';" +
        "string:concat(string:new(), Q{java.awt.Point}new())", JAVAMETHOD_X_X);

    query("declare namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "try{qm:error(qm:new())} catch * {local-name-from-QName($err:code)}", "BASX0000");
    query("import module namespace qm='java:org.basex.query.func.QueryModuleTest';" +
        "try { qm:error() } catch * { local-name-from-QName($err:code) }", "BASX0000");
  }

  /** Tests ambiguous function signatures. */
  @Test
  public void ambiguousSignature() {
    error("import module namespace n='java:java.lang.StringBuilder'; n:append('x')",
        JAVAAMBIG_X);
  }

  /** Ensure that items cannot be cast to Java. */
  @Test
  public void javaCast() {
    error("xs:java('x')", FUNCUNKNOWN_X);
    error("java('x')", FUNCUNKNOWN_X);
    error("'x' cast as xs:java", TYPE30_X);
    error("'x' cast as java", TYPE30_X);
  }

  /** Pass on Java items to functions. */
  @Test
  public void funcItem() {
    query("function($x) { $x }(Q{java:java.io.File}new('x'))", "x");
    query("declare function db:f($x) { $x }; db:f#1(Q{java:java.io.File}new('x'))", "x");
  }

  /** URI test. */
  @Test
  public void uri() {
    query("declare namespace uri = 'java.net.URI'; uri:get-host(uri:new('http://a'))", "a");
    query("declare namespace uri = 'java.net.URI'; uri:get-path(uri:new('http://a/b'))", "/b");
  }

  /** Atomizing Java items. */
  @Test
  public void data() {
    query("data(Q{java:java.lang.Object}new()) instance of xs:anyAtomicType", "true");
  }
}
