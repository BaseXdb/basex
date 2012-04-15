package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the DefaultNamespaceDecl production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDefaultNamespaceDecl extends QT3TestSet {

  /**
   *  A 'declare default element namespace' expression containing many comments, using apostrophes for the URILiteral. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog1() {
    final XQuery query = new XQuery(
      "declare(:..:)default(:..:)element(:..:)namespace(:..:)'http://example.com/'(:..:);(:..:)1(:..:)eq(:..:)1(:..:)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  ':=' cannot be used in a 'declare namespace' declaration. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog10() {
    final XQuery query = new XQuery(
      "declare default element namespace := \"http://example.com/\";1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  ':=' cannot be used in a 'declare namespace' declaration. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog11() {
    final XQuery query = new XQuery(
      "declare default function namespace := \"http://example.com/\";1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  A 'declare default element namespace' expression containing many comments, using quotes for the URILiteral. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog2() {
    final XQuery query = new XQuery(
      "declare(:..:)default(:..:)element(:..:)namespace(:..:)\"http://example.com/\"(:..:);(:..:)1(:..:)eq(:..:)1(:..:)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A 'declare default function namespace' expression containing many comments, using apostrophes for the URILiteral. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog3() {
    final XQuery query = new XQuery(
      "declare(:..:)default(:..:)function(:..:)namespace(:..:)'http://example.com/'(:..:);(:..:)1(:..:)eq(:..:)1(:..:)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A 'declare default function namespace' expression containing many comments, using quotes for the URILiteral. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog4() {
    final XQuery query = new XQuery(
      "declare(:..:)default(:..:)function(:..:)namespace(:..:)\"http://example.com/\"(:..:);(:..:)1(:..:)eq(:..:)1(:..:)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  No function named boolean is available in the namespace 'example.com' set via 'declare default function namespace'. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog5() {
    final XQuery query = new XQuery(
      "declare(:..:)default(:..:)function(:..:)namespace(:..:)\"http://example.com/\"(:..:);(:..:)boolean(1)(:..:)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  'declare function namespace' is a syntactically invalid declaration. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog6() {
    final XQuery query = new XQuery(
      "declare function namespace \"http://example.com/\";1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  'declare function namespace' is a syntactically invalid declaration. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog7() {
    final XQuery query = new XQuery(
      "declare element namespace \"http://example.com/\";1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  '=' cannot be used in a 'declare namespace' declaration. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog8() {
    final XQuery query = new XQuery(
      "declare default element namespace = \"http://example.com/\";1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  '=' cannot be used in a 'declare namespace' declaration. .
   */
  @org.junit.Test
  public void kDefaultNamespaceProlog9() {
    final XQuery query = new XQuery(
      "declare default function namespace = \"http://example.com/\";1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  In direct element constructors, the declared default element namespace is respected. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog1() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/defelementns\"; namespace-uri(<foo/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/defelementns")
    );
  }

  /**
   *  Two attributes that have identical expanded names, but indifferent lexical names. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog10() {
    final XQuery query = new XQuery(
      "declare namespace a = \"http://www.example.com/\"; <e xmlns:b=\"http://www.example.com/\" a:localName=\"1\" b:localName=\"2\" />",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0040")
    );
  }

  /**
   *  Ensure the default namespaces are correctly set. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog11() {
    final XQuery query = new XQuery(
      "<a xmlns=\"http://www.w3.org/2001/XMLSchema\"> {1 cast as byte} <b xmlns=\"http://www.w3.org/1999/XSL/Transform\"> {count(1)} </b> {2 cast as byte} </a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a xmlns=\"http://www.w3.org/2001/XMLSchema\">1<b xmlns=\"http://www.w3.org/1999/XSL/Transform\">1</b>2</a>", false)
    );
  }

  /**
   *  Ensure that default namespaces override each other properly. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog12() {
    final XQuery query = new XQuery(
      "<a xmlns=\"http://www.w3.org/2001/XMLSchema\"> {1 cast as byte} <b xmlns=\"http://www.w3.org/1999/XSL/Transform\"> {count(1), 2 cast as byte} </b> {2 cast as byte} </a>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0051")
    );
  }

  /**
   *  Variables cannot occur before namespace declarations. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog13() {
    final XQuery query = new XQuery(
      "declare variable $variable := 1; declare default element namespace \"http://example.com\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Variables cannot occur before namespace declarations. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog14() {
    final XQuery query = new XQuery(
      "declare variable $variable := 1; declare default element namespace \"http://example.com\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Functions cannot occur before namespace declarations. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog15() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { 1 }; declare default element namespace \"http://example.com\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Options cannot occur before namespace declarations. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog16() {
    final XQuery query = new XQuery(
      "declare option local:myOption \"foo\"; declare default element namespace \"http://example.com\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Ensure that the right namespace binding is picked up. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog17() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare default element namespace \"http://example.com/\"; \n" +
      "         for $test as attribute(integer, xs:anyAtomicType) in (<e integer=\"1\"/>/@integer) \n" +
      "         return data($test)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Ensure the 'default' keyword is parsed correctly. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog18() {
    final XQuery query = new XQuery(
      "default eq default",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  'xmlns' declarations on direct element constructors override the declared default element namespace. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog2() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/defelementns\"; namespace-uri(<foo xmlns=\"http://example.com/overriden\"/>)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "http://example.com/overriden")
    );
  }

  /**
   *  Syntax error in the keywords. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog3() {
    final XQuery query = new XQuery(
      "default declare default element namespace b = \"http://www.example.com/\"; empty(<e xmlns=\"http://www.example.com/\"><d xmlns=\"\"><b/></d></e>/b:d/b:b)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Syntax error in the keywords(#2). .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog4() {
    final XQuery query = new XQuery(
      "declare default element namespace b = \"http://www.example.com/\"; empty(<e xmlns=\"http://www.example.com/\"><d xmlns=\"\"><b/></d></e>/b:d/b:b)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XPST0003")
      ||
        error("XPST0081")
      )
    );
  }

  /**
   *  Syntax error in the keywords(#3). .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog5() {
    final XQuery query = new XQuery(
      "declare default element namespace b = \"http://www.example.com/\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Ensure the default element namespace is properly handled with default namespace attribute declarations. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog6() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/\"; namespace-uri-from-QName(xs:QName(\"localName\")), <e xmlns=\"\"> { \" | \", namespace-uri-from-QName(xs:QName(\"localName\")) } </e>, namespace-uri-from-QName(xs:QName(\"localName\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("http://example.com/<e> |  </e>http://example.com/", false)
    );
  }

  /**
   *  Use two default namespace declarations on the same element. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog7() {
    final XQuery query = new XQuery(
      "<e xmlns=\"\" xmlns=\"\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0071")
    );
  }

  /**
   *  Use two default namespace declarations on the same element, but with different namespace URIs. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog8() {
    final XQuery query = new XQuery(
      "<e xmlns=\"http://www.example.com/1\" xmlns=\"http://www.example.com/2\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0071")
    );
  }

  /**
   *  Use two namespace declarations on the same element, but with different namespace URIs. .
   */
  @org.junit.Test
  public void k2DefaultNamespaceProlog9() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://www.example.com/1\" xmlns:p=\"http://www.example.com/2\"/>",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0071")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace001() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://www..oracle.com/xquery/test\"; declare function price ($b as element()) as element()* { $b/price }; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://www..oracle.com/xquery/test\"; \n" +
      "        declare function foo($n as xs:integer) { <tr> {$n} </tr> }; \n" +
      "        foo(4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<tr>4</tr>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://www..oracle.com/xquery/test\"; \n" +
      "        declare function price ($i as element()) as element()? { $i/price }; \n" +
      "        for $j in /bib/book return price($j)\n" +
      "      ",
      ctx);
    query.context(node(file("docs/bib.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<price>65.95</price><price>65.95</price><price>39.95</price><price>129.95</price>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://example.org\"; \n" +
      "        declare function summary($emps as element(employee)*) as element(dept)* { \n" +
      "            for $d in fn:distinct-values($emps/deptno) \n" +
      "            let $e := $emps[deptno = $d] \n" +
      "            return <dept> <deptno>{$d}</deptno> <headcount> {fn:count($e)} </headcount> <payroll> {fn:sum($e/salary)} </payroll> </dept> \n" +
      "        }; \n" +
      "        summary(//employee[location = \"Denver\"])",
      ctx);
    query.context(node(file("op/union/acme_corp.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<dept><deptno>1</deptno><headcount>2</headcount><payroll>130000</payroll></dept><dept><deptno>2</deptno><headcount>1</headcount><payroll>80000</payroll></dept>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace005() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function mysum($i as xs:integer, $j as xs:integer) { let $j := $i + $j return $j }; \n" +
      "      \tdeclare function invoke_mysum() { let $s := 1 for $d in (1,2,3,4,5) let $s := mysum($s, $d) return $s }; \n" +
      "      \tinvoke_mysum()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2 3 4 5 6")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace006() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function mysum($i as xs:integer, $j as xs:integer) { let $j := $i + $j return $j }; \n" +
      "      \tdeclare function invoke_mysum($st) { for $d in (1,2,3,4,5) let $st := mysum($d, $st) return $st }; \n" +
      "      \tinvoke_mysum(0)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace007() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare variable $a := 1; \n" +
      "      \tdeclare function foo($a as xs:integer) { if ($a > 100) then $a else let $a := $a + 1 return foo($a) }; \n" +
      "      \tfoo($a)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("101")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace008() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function fact($n as xs:integer) as xs:integer { if ($n < 2) then 1 else $n * fact($n - 1) }; \n" +
      "      \tdeclare variable $ten := fact(10); \n" +
      "      \t<table> { for $i in 1 to 10 return <tr> <td>10!/{$i}! = {$ten div fact($i)}</td> </tr> } </table>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<table><tr><td>10!/1! = 3628800</td></tr><tr><td>10!/2! = 1814400</td></tr><tr><td>10!/3! = 604800</td></tr><tr><td>10!/4! = 151200</td></tr><tr><td>10!/5! = 30240</td></tr><tr><td>10!/6! = 5040</td></tr><tr><td>10!/7! = 720</td></tr><tr><td>10!/8! = 90</td></tr><tr><td>10!/9! = 10</td></tr><tr><td>10!/10! = 1</td></tr></table>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace009() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function fact($n as xs:integer) as xs:integer { if ($n < 2) then 1 else $n * fact(($n)-1) }; \n" +
      "      \t<table> { for $i in 1 to 10 return <tr> <td>{$i}! = {fact($i)}</td> </tr> } </table>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<table><tr><td>1! = 1</td></tr><tr><td>2! = 2</td></tr><tr><td>3! = 6</td></tr><tr><td>4! = 24</td></tr><tr><td>5! = 120</td></tr><tr><td>6! = 720</td></tr><tr><td>7! = 5040</td></tr><tr><td>8! = 40320</td></tr><tr><td>9! = 362880</td></tr><tr><td>10! = 3628800</td></tr></table>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace010() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://example.org\"; \n" +
      "        declare function prnt($n as xs:integer,$n2 as xs:string, $n3 as xs:date, $n4 as xs:long, $n5 as xs:string, $n6 as xs:decimal) {\n" +
      "             if ($n < 2) then 1 else fn:concat($n, \" \",$n2,\" \",$n3,\" \",$n4,\" \",$n5,\" \",$n6) \n" +
      "        }; \n" +
      "        <table> { <td>Value is = {prnt(4,xs:string(\"hello\"),xs:date(\"2005-02-22\"), xs:long(5),xs:string(\"well\"),xs:decimal(1.2))}</td> } </table>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<table><td>Value is = 4 hello 2005-02-22 5 well 1.2</td></table>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace011() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function fn1 ($n as xs:integer) as xs:integer { fn2($n) }; \n" +
      "      \tdeclare function fn2 ($n as xs:integer) as xs:integer { if ($n = 1) then 1 else $n + fn1($n - 1) }; \n" +
      "      \tfn1(4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace012() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function fn1 ($n as xs:integer) as xs:integer { fn2($n) }; \n" +
      "      \tdeclare function fn2 ($n as xs:integer) as xs:integer { if ($n = 1) then 1 else $n + fn1($n - 1) }; \n" +
      "      \tfn1(4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace013() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function foo2($i as xs:string) as xs:string {foo($i)}; \n" +
      "      \tdeclare function foo($i as xs:string) as xs:string {$i}; \n" +
      "      \tfoo2(\"abc\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "abc")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace014() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function odd($x as xs:integer) as xs:boolean {if ($x = 0) then fn:false() else even($x - 1)}; \n" +
      "      \tdeclare function even($x as xs:integer) as xs:boolean {if ($x = 0) then fn:true() else odd($x - 1)}; \n" +
      "      \teven(4)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace015() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function odd($x as xs:integer) as xs:boolean {if ($x = 0) then fn:false() else even($x - 1)}; \n" +
      "      \tdeclare function even($x as xs:integer) as xs:boolean {if ($x = 0) then fn:true() else odd($x - 1)}; \n" +
      "      \teven(3)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace016() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://example.org\"; \n" +
      "        declare function title($a_book as element()) as element()* { for $i in $a_book return $i/title }; \n" +
      "        /bib/book/(title(.))",
      ctx);
    query.context(node(file("op/union/bib2.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title><title>Data on the Web</title><title>The Economics of Technology and Content for Digital TV</title>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace017() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://example.org\"; \n" +
      "        declare default element namespace \"http://www.example.com/filesystem\"; \n" +
      "        declare variable $v as xs:integer := 100; \n" +
      "        declare function udf1 ($CUSTNO as xs:integer) { <empty> {$CUSTNO*$v} </empty> }; \n" +
      "        udf1(10)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<empty xmlns=\"http://www.example.com/filesystem\">1000</empty>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace018() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://example.org\"; \n" +
      "        declare default element namespace \"http://www.example.com/filesystem\"; \n" +
      "        declare function udf1 () { <empty> {10*10} </empty> }; \n" +
      "        udf1 ()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<empty xmlns=\"http://www.example.com/filesystem\">100</empty>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace019() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://example.org\"; \n" +
      "        declare default element namespace \"http://www.example.com/def\"; \n" +
      "        declare namespace test=\"http://www.example.com/test\"; \n" +
      "        declare namespace test2=\"http://www.example.com/test2\"; \n" +
      "        declare function test:udf1() { <empty> {10*10} </empty> }; \n" +
      "        declare function test2:udf1() { <empty/> }; \n" +
      "        <A> {test:udf1()} {test2:udf1()} </A>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<A xmlns=\"http://www.example.com/def\"><empty>100</empty><empty/></A>", false)
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace020() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare default function namespace \"http://example.org\"; \n" +
      "      \tdeclare function price () as xs:integer+ { 100 }; \n" +
      "      \tdeclare function price ($z as xs:integer) as xs:integer+ { $z }; \n" +
      "      \tdeclare function price ($x as xs:integer, $y as xs:integer) as xs:integer+ { $x, $y }; \n" +
      "      \tdeclare function price ($x as xs:integer, $y as xs:integer, $z as xs:integer) as xs:integer+ { $x+$y+$z }; \n" +
      "      \tprice(), price(1), price(2,3), price(4,5,6)\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "100 1 2 3 15")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace021() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http:/www.oracle.com/xquery\"; \n" +
      "        declare variable $x := 7.5; \n" +
      "        $x + 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "9.5")
    );
  }

  /**
   *  Demonstrates Default namespace declaration, facilitates the use of unprefixed QNames .
   */
  @org.junit.Test
  public void defaultNamespace022() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.example.com/test\"; \n" +
      "        <test/>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<test xmlns=\"http://www.example.com/test\"/>", false)
    );
  }

  /**
   *  Evaluation of the of a query prolog with two default namespace declarations. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://example.org/names\"; \n" +
      "        declare default element namespace \"http://someexample.org/names\"; \n" +
      "        \"abc\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0066")
    );
  }

  /**
   *  Evaluation of the of a query prolog with two default namespace declarations. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://example.org/names\"; \n" +
      "        declare default function namespace \"http://someexample.org/names\"; \n" +
      "        \"abc\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0066")
    );
  }

  /**
   * Invalid use of XML namespace as default. See bug 14930. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr3() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.w3.org/XML/1998/namespace\";\n" +
      "        <a/>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   * Invalid use of XML namespace as default. See bug 14930. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr4() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://www.w3.org/XML/1998/namespace\";\n" +
      "        declare function go() {3};\n" +
      "        go()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   * Invalid use of XMLNS namespace as default. See bug 14930. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http://www.w3.org/2000/xmlns/\";\n" +
      "        <a/>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   * Invalid use of XMLNS namespace as default. See bug 14930. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http://www.w3.org/2000/xmlns/\";\n" +
      "        declare function go() {3};\n" +
      "        go()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   * Invalid use of XMLNS namespace as default, with escaping. See bug 14930. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr7() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default element namespace \"http&#x3a;//www.w3.org/2000/xmlns/\";\n" +
      "        <a/>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   * Invalid use of XMLNS namespace as default, with escaping. See bug 14930. .
   */
  @org.junit.Test
  public void defaultnamespacedeclerr8() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare default function namespace \"http&#x3a;//www.w3.org/2000/xmlns/\";\n" +
      "        declare function go() {3};\n" +
      "        go()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }
}
