package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.func.java.*;
import org.basex.util.*;
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
    query("Q{java:Integer}new('123')()", 123);
    query("Q{java:Integer}new\u00b7String('456')()", 456);
    query("Q{java:Integer}new\u00b7int(xs:int(789))()", 789);

    query("declare namespace Random = 'java:java.util.Random'; Random:nextInt(Random:new())");
    query("declare namespace List = 'java:org.basex.util.list.StringList'; List:new()");

    // implicitly cast xs:integer to int
    query("Q{java:StringBuilder}new(1)()", "");

    error("declare namespace random = 'java:java.util.random'; random:new()", JAVACLASS_X);
    error("Q{java:java.util.rndm}new()", JAVACLASS_X);
    error("Q{java:java.util.random}new()", JAVACLASS_X);
    error("Q{java:Integer}new\u00b7int('abc')", JAVAARGS_X_X_X);
  }

  /** Tests namespace rewritings. */
  @Test public void rewriteURI() {
    query("Q{java.lang.integer}new('123')()", 123);
    query("Q{java.lang.Integer}new\u00b7int(xs:int(456))()", 456);
    query("declare namespace Random = 'java.util.random'; Random:nextInt(Random:new())");
    query("declare namespace List = 'http://basex.org/util/list/string-list'; List:new()");
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
    query("declare namespace Point = 'java:java.awt.Point'; " +
        "Point:new() => Point:x()", 0);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test public void staticMethod() {
    query("Q{java.lang.Math}sqrt(xs:double(9.0))", 3);
    query("Q{java.lang.Math}sqrt\u00b7double(xs:double(9.0))", 3);
    error("Q{java:org.basex.query.func.JavaFunctionExample}error()", JAVAEXEC_X_X_X);

    // sequence types
    query("Q{java:org.basex.util.Strings}eqic('1', (('1', '2')))", true);
    query("Q{java:org.basex.util.Strings}eqic('1', (1 to 2) ! string())", true);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test public void method() {
    query("declare namespace Rectangle = 'java.awt.Rectangle'; " +
        "Rectangle:new(xs:int(2), xs:int(2)) => Rectangle:contains(xs:int(1), xs:int(1))", true);

    query("declare namespace Properties = 'java.util.Properties'; Properties:new()",
        "java:java.util.Properties#0");
    query("declare namespace Properties = 'java.util.Properties'; Properties:new()()",
        "map {\n}");
  }

  /** Tests importing a Java class. */
  @Test public void importClass() {
    query("import module namespace Set = 'java.util.HashSet'; " +
        "let $a := (Set:add('a'), Set:add('b')) return Set:size()", 2);
    query("import module namespace Set = 'java.util.HashSet'; " +
        "let $a := (Set:add(128), Set:add(128)) return Set:size()", 1);
    query("import module namespace Set = 'java.util.HashSet'; " +
        "let $a := Set:add\u00b7java.lang.Object(128) return Set:size()", 1);

    // use class with capital and lower case
    query("import module namespace String = 'http://lang.java/String'; " +
        "String:length()", 0);
    query("import module namespace String = 'http://lang.java/string'; " +
        "String:length()", 0);

    // handle {@link Jav} type
    query("declare namespace Set = 'java.util.HashSet';" +
        "Set:add(Set:new(), Q{java.awt.Point}new())", true);
  }

  /** Tests importing a query module. */
  @Test public void importQueryModule() {
    // address class extending QueryModule
    query("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest';" +
        "qm:fast(0)", "Apple");
    query("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest';" +
        "qm:convenient(xs:int(1))", "Banana");
    query("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest';" +
        "qm:functionNS()", "http://www.w3.org/2005/xpath-functions");
    query("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest';" +
        "qm:faculty(1 to 5)", "120");
  }

  /** Expression argument. */
  @Test public void expr() {
    query("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest';" +
        "qm:ignore((1 to 10)[. > 1])", "");
  }

  /** Tests importing a Java class and throwing errors. */
  @Test public void importError() {
    // handle {@link Jav} type
    error("declare namespace string = 'java.lang.String'; " +
        "string:concat(string:new(), Q{java.awt.Point}new())", JAVAARGS_X_X_X);
    error("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest'; " +
        "qm:xyz()", JAVAMEMBER_X);
    error("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest'; " +
        "qm:fast()", FUNCARITY_X_X_X);

    query("declare namespace qm = 'java:org.basex.query.func.QueryModuleTest'; " +
        "try{ qm:error(qm:new()) } catch * { $err:code }", "basex:error");
    query("import module namespace qm = 'java:org.basex.query.func.QueryModuleTest'; " +
        "try { qm:error() } catch * { $err:code }", "basex:error");
  }

  /** Tests ambiguous signatures. */
  @Test public void ambiguous() {
    query("Q{StringBuilder}new() => Q{StringBuilder}append(1) => string()", 1);
    query("Q{StringBuilder}new() => Q{StringBuilder}append·int(1) => string()", 1);
    query("Q{StringBuilder}new() => Q{StringBuilder}append(xs:int(1)) => string()", 1);
    query("Q{StringBuilder}new() => Q{StringBuilder}append('x') => string()", "x");

    error("Q{StringBuilder}new() => Q{StringBuilder}append(xs:byte(1))", JAVAMULTIPLE_X_X);
    error("Q{java:org.basex.query.func.JavaFunctionExample}new(true())", JAVAMULTIPLE_X_X);

    error("import module namespace StringBuilder = 'java:java.lang.StringBuilder'; " +
        "StringBuilder:append('x')", JAVAMULTIPLE_X_X);
  }

  /** Pass on empty sequences. */
  @Test public void emptyDecl() {
    query("declare namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:string(n:new(), ())", "");
    query("declare namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:ambiguous1(n:new(), ())", "");

    error("declare namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:bool(n:new(), ())", JAVAARGS_X_X_X);
    error("declare namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:ambiguous2(n:new(), ())", JAVAMULTIPLE_X_X);
  }

  /** Pass on empty sequences. */
  @Test public void emptyImport() {
    query("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:string(())", "");

    error("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:bool(())", JAVAARGS_X_X_X);
    error("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:ambiguous1(())", JAVAMULTIPLE_X_X);
    error("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:ambiguous2(())", JAVAMULTIPLE_X_X);
  }

  /** Address invisible code. */
  @Test public void notVisible() {
    error("declare namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:var(n:new())", WHICHFUNC_X);
    error("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:var()", JAVAMEMBER_X);
  }

  /** Error cases. */
  @Test public void errors() {
    error("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:null-array()", JAVANULL);
  }

  /** Instance errors. */
  @Test public void instance() {
    error("Q{String}toString(())", JAVANOINSTANCE_X_X);
    error("Q{String}toString(123)", JAVANOINSTANCE_X_X);
  }

  /** Process arrays. */
  @Test public void array() {
    query("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:strings(array { })", "");
    query("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:strings(array { '1' })", 1);
    query("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:longs(array { 1 })", 1);
    query("import module namespace n = 'org.basex.query.func.JavaFunctionExample'; " +
        "n:chars() => codepoints-to-string()", "ab");
  }

  /** Character parameters. */
  @Test public void gh2018() {
    query("'A' " +
        "=> string-to-codepoints()" +
        "=> xs:unsignedShort()" +
        "=> Q{java:java.lang.Character}isUpperCase()",
        true);

    query("Q{java:java.lang.Character}isUpperCase(xs:int(65))", true);
    query("Q{java:java.lang.Character}isUpperCase(xs:unsignedShort(65))", true);
    error("Q{java:java.lang.Character}isUpperCase(65)", JAVAMULTIPLE_X_X);
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
    error("Q{java:java.lang.String}XYZ()", JAVAMEMBER_X);

  }

  /** Pass on Java items to functions. */
  @Test public void funcItem() {
    final String ns = "declare namespace File = 'java:java.io.File'; ";
    query(ns + "declare function db:f($x) { $x }; db:f(File:new('x'))", "java:java.io.File#0");
    query(ns + "declare function db:f($x) { $x }; db:f(File:new('x')) => File:toString()", "x");

    query(ns + "declare function db:f($x) { $x }; db:f#1(File:new('x'))", "java:java.io.File#0");
    query(ns + "declare function db:f($x) { $x }; db:f#1(File:new('x')) => File:toString()", "x");

    query(ns + "function($x) { $x }(File:new('x'))", "java:java.io.File#0");
    query(ns + "function($x) { $x }(File:new('x')) => File:toString()", "x");
  }

  /** URI test. */
  @Test public void uri() {
    final String ns = "declare namespace URI = 'java.net.URI'; ";
    query(ns + "URI:get-host(URI:new('http://a'))", "a");
    query(ns + "URI:get-path(URI:new('http://a/b'))", "/b");
  }

  /** Return Java items. */
  @Test public void data() {
    final String ns = "declare namespace List = 'java:java.util.ArrayList'; ";
    query(ns + "List:new()", "java:java.util.ArrayList#0");
    query(ns + "List:new()()", "");
  }

  /** Retrieve function items as Java objects. */
  @Test public void toJava() {
    query("import module namespace Set = 'java:java.util.HashSet'; Set:add(true#0)", "true");
  }

  /** Test method. */
  @Test public void fromJava() {
    query("import module namespace jfe = 'org.basex.query.func.JavaFunctionExample'; "
        + "jfe:data() ! (if(. instance of function(*)) then .() else .)",
        "a\nb\nc\nmap {\n\"d\": \"e\"\n}\nf");

    query("import module namespace jfe = 'org.basex.query.func.JavaFunctionExample'; "
        + "let $c := jfe:data() ! (if(. instance of function(*)) then .() else .) "
        + "return ($c[1], $c[2], $c[3], map:keys($c[4]), $c[4]?*, $c[5])",
        "a\nb\nc\nd\ne\nf");

    query("declare namespace Random = 'java:java.util.Random';" +
        "starts-with(Random:new(xs:long(0))(), 'java.util.Random@')", true);
  }

  /** Empty sequences. */
  @Test public void emptySequence() {
    error("Q{java.lang.String}new·java.lang.String(())", JAVAEXEC_X_X_X);
  }

  /** Array arguments. */
  @Test public void arrays() {
    final String ns = "declare namespace String = 'java:java.lang.String'; ";
    query(ns + "String:new(array { xs:unsignedShort(33) })()", "!");
    query(ns + "String:new·char...(array { xs:unsignedShort(33) })()", "!");
    query(ns + "String:new·char...(array { (65 to 69) ! xs:unsignedShort(.) })()", "ABCDE");
    query(ns + "String:new·char...·int·int(" +
        "  array { (65 to 69) ! xs:unsignedShort(.) }, xs:int(1), xs:int(3)" +
        ")()",
        "BCD");
  }

  /** Wrap options. */
  @Test public void wrap() {
    String pattern = "declare namespace S = 'java:String'; " +
        "let $s := (# db:wrapjava % #) { S:new('12') => S:charAt(xs:int(0)) } return $s%";
    query(Util.info(pattern, "all", "()"), 1);
    query(Util.info(pattern, "instance", "()"), 12);
    query(Util.info(pattern, "some", ""), 1);
    query(Util.info(pattern, "none", ""), 1);
    query(Util.info(pattern, "void", ""), "");

    pattern = "declare namespace SB = 'java:StringBuilder'; " +
        "let $s := (# db:wrapjava % #) { SB:new() => SB:append·String('12') } return $s%";
    query(Util.info(pattern, "all", "()"), 12);
    query(Util.info(pattern, "instance", "()"), 12);
    query(Util.info(pattern, "some", "()"), 12);
    query(Util.info(pattern, "none", ""), 12);
    query(Util.info(pattern, "void", ""), "");

    pattern = "declare namespace SB = 'java:String'; " +
        "let $s := (# db:wrapjava % #) { SB:valueOf·long(12) } return $s%";
    query(Util.info(pattern, "all", "()"), 12);
    query(Util.info(pattern, "instance", ""), 12);
    query(Util.info(pattern, "some", ""), 12);
    query(Util.info(pattern, "none", ""), 12);
    query(Util.info(pattern, "void", ""), "");

    pattern = "declare namespace Math = 'java:java.lang.Math'; " +
        "let $s := (# db:wrapjava % #) { Math:PI() } return $s% instance of xs:double";
    query(Util.info(pattern, "all", "()"), true);
    query(Util.info(pattern, "instance", ""), true);
    query(Util.info(pattern, "some", ""), true);
    query(Util.info(pattern, "none", ""), true);
    query(Util.info(pattern, "void", ""), false);

    pattern = "import module namespace jfe = 'org.basex.query.func.JavaFunctionExample'; " +
        "let $s := (# db:wrapjava % #) { jfe:bool(true()) } return $s%";
    query(Util.info(pattern, "all", "()"), true);
    query(Util.info(pattern, "instance", " instance of function(*)"), true);
    query(Util.info(pattern, "some", ""), true);
    query(Util.info(pattern, "none", ""), true);
    query(Util.info(pattern, "void", ""), "");
  }

  /** Test. */
  @Test public void camelCase() {
    assertEquals("", JavaCall.camelCase(""));
    assertEquals("a", JavaCall.camelCase("a"));
    assertEquals("aB", JavaCall.camelCase("a-b"));
    assertEquals("aBC", JavaCall.camelCase("a-b--c"));
    assertEquals("a.bC", JavaCall.camelCase("a.b-c"));
    assertEquals("a/b.cD", JavaCall.camelCase("a/b.c-D"));
  }

  /** Test. */
  @Test public void className() {
    assertEquals("", JavaCall.uriToClasspath(""));
    assertEquals("A", JavaCall.uriToClasspath("a"));
    assertEquals(".", JavaCall.uriToClasspath("."));
    assertEquals(".A", JavaCall.uriToClasspath(".a"));
    assertEquals(".Ab", JavaCall.uriToClasspath(".ab"));
    assertEquals("String", JavaCall.uriToClasspath("string"));
    assertEquals("java.lang.String", JavaCall.uriToClasspath("java.lang.string"));
    assertEquals("java.lang.String", JavaCall.uriToClasspath("java.lang.string"));
    assertEquals("java.lang.String", JavaCall.uriToClasspath("java/lang/string"));
    assertEquals("org.basex.modules.MD", JavaCall.uriToClasspath("org.basex.modules.m-d"));
    assertEquals("a.BC", JavaCall.uriToClasspath("a/-b-c"));
  }

  /** Test. */
  @Test public void uri2Path() {
    assertEquals("a", JavaCall.uri2path("a"));
    assertEquals("a", JavaCall.uri2path("/a"));
    assertEquals("a/b", JavaCall.uri2path("a/b"));
    assertEquals("a-c", JavaCall.uri2path("a-c"));
    assertEquals("A", JavaCall.uri2path("%41"));
    assertEquals("a/b", JavaCall.uri2path("a///b"));
    assertEquals("a/index", JavaCall.uri2path("a/"));
    assertEquals("index", JavaCall.uri2path("/"));
    assertEquals("index", JavaCall.uri2path(""));

    assertEquals("org/index", JavaCall.uri2path("http://org"));
    assertEquals("org/index", JavaCall.uri2path("http://org/"));
    assertEquals("org/basex/m/hello/World", JavaCall.uri2path("http://basex.org/m/hello/World"));
    assertEquals("com/example/www/index", JavaCall.uri2path("http://www.example.com"));
    assertEquals("a/b/c", JavaCall.uri2path("a:b:c"));
    assertEquals("A/A", JavaCall.uri2path("http://%41/%41"));

    assertEquals("-gg", JavaCall.uri2path("%gg"));
    assertEquals("-", JavaCall.uri2path(";"));
    assertEquals("http-/-gg", JavaCall.uri2path("http://%gg"));

    assertEquals("a/b/c", JavaCall.uri2path("a:b:c"));
  }
}
