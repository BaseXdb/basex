package org.basex.test.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the AnnexE operator.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscAnnexE extends QT3TestSet {

  /**
   *  User defined function # 1 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex1() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:if-empty ( $node as node()?, $value as xs:anyAtomicType) as xs:anyAtomicType* { if ($node and $node/child::node()) then fn:data($node) else $value }; let $arg1 := <element1>some data</element1> let $arg2 as xs:anyAtomicType := 1 return eg:if-empty($arg1,$arg2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "some data")
    );
  }

  /**
   *  User defined function # 2 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex2() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:if-absent ( $node as node()?, $value as xs:anyAtomicType) as xs:anyAtomicType* { if ($node) then fn:data($node) else $value }; let $arg1 := <element1>some data</element1> let $arg2 as xs:anyAtomicType := 1 return eg:if-absent($arg1,$arg2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "some data")
    );
  }

  /**
   *  User defined function # 3 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex3() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:value-union ( $arg1 as xs:anyAtomicType*, $arg2 as xs:anyAtomicType*) as xs:anyAtomicType* { fn:distinct-values(($arg1, $arg2)) }; let $arg1 as xs:anyAtomicType := 1 let $arg2 as xs:anyAtomicType := 2 return eg:value-union($arg1,$arg2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  User defined function # 4 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex4() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:value-intersect ( $arg1 as xs:anyAtomicType*, $arg2 as xs:anyAtomicType* ) as xs:anyAtomicType* { fn:distinct-values($arg1[.=$arg2]) }; let $arg1 as xs:anyAtomicType := 1 let $arg2 as xs:anyAtomicType := 1 return eg:value-intersect($arg1,$arg2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  User defined function # 5 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex5() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:value-except ( $arg1 as xs:anyAtomicType*, $arg2 as xs:anyAtomicType*) as xs:anyAtomicType* { fn:distinct-values($arg1[not(.=$arg2)]) }; let $arg1 as xs:anyAtomicType := 1 let $arg2 as xs:anyAtomicType := 2 return eg:value-except($arg1,$arg2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertEq("1")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  User defined function # 6 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex6() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:index-of-node($sequence as node()*, $srch as node()) as xs:integer* { for $n at $i in $sequence where ($n is $srch) return $i }; let $arg1 := (<element1>some data 1</element1>,<element2>some data 2</element2>) let $arg2 := $arg1[2] return eg:index-of-node($arg1, exactly-one($arg2))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   *  User defined function # 7 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex7() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:string-pad ( $padString as xs:string?, $padCount as xs:integer) as xs:string { fn:string-join((for $i in 1 to $padCount return $padString), \"\") }; let $arg1 as xs:string := \"A String\" let $arg2 as xs:integer := 3 return eg:string-pad($arg1,$arg2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A StringA StringA String")
    );
  }

  /**
   *  User defined function # 8 from annex E of F& O Specs. .
   */
  @org.junit.Test
  public void annex8() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:distinct-nodes-stable ($arg as node()*) as node()* { for $a at $apos in $arg let $before_a := fn:subsequence($arg, 1, $apos - 1) where every $ba in $before_a satisfies not($ba is $a) return $a }; let $arg1 := (<element1>some data 1</element1>,<element2>some data 2</element2>) return eg:distinct-nodes-stable($arg1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<element1>some data 1</element1><element2>some data 2</element2>", false)
    );
  }
}
