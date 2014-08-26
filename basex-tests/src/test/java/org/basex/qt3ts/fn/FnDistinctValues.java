package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the distinct-values() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnDistinctValues extends QT3TestSet {

  /**
   *  A test whose essence is: `distinct-values()`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc1() {
    final XQuery query = new XQuery(
      "distinct-values()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `deep-equal(distinct-values((1, 2.0, 3, 2)), (1, 2.0, 3))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc10() {
    final XQuery query = new XQuery(
      "deep-equal(distinct-values((1, 2.0, 3, 2)), (1, 2.0, 3))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `exists(distinct-values((1, 2, 3, 1)))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc11() {
    final XQuery query = new XQuery(
      "exists(distinct-values((1, 2, 3, 1)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(empty(distinct-values((1, 1))))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc12() {
    final XQuery query = new XQuery(
      "not(empty(distinct-values((1, 1))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(distinct-values((1, 2, 2, current-time()))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc13() {
    final XQuery query = new XQuery(
      "count(distinct-values((1, 2, 2, current-time()))) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(distinct-values(())) eq 0`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc14() {
    final XQuery query = new XQuery(
      "count(distinct-values(())) eq 0",
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
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  fn:distinct-values() applied on an argument of cardinality exactly-one. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc15() {
    final XQuery query = new XQuery(
      "count(distinct-values(current-time())) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:distinct-values() with a collation argument, although the function does not perform string comparison. For that reason, output is valid as well. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc2() {
    final XQuery query = new XQuery(
      "deep-equal(distinct-values((1, 2, 3), \"http://www.example.com/COLLATION/NOT/SUPPORTED\"), (1, 2, 3))",
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
        assertBoolean(true)
      ||
        error("FOCH0002")
      )
    );
  }

  /**
   *  A test whose essence is: `distinct-values("a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint", "wrong param")`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc3() {
    final XQuery query = new XQuery(
      "distinct-values(\"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\", \"wrong param\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `distinct-values("a string", "http://www.w3.org/2005/xpath-functions/collation/codepoint") eq "a string"`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc4() {
    final XQuery query = new XQuery(
      "distinct-values(\"a string\", \"http://www.w3.org/2005/xpath-functions/collation/codepoint\") eq \"a string\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(distinct-values(()))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc5() {
    final XQuery query = new XQuery(
      "empty(distinct-values(()))",
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
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  A test whose essence is: `deep-equal(distinct-values( ("1", 1, 2, 1, 1, 3, 1, 1, 3, xs:anyURI("example.com/"), xs:anyURI("example.com/"))), ("1", 1, 2, 3, xs:anyURI("example.com/")))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:canon($arg) {\n" +
      "            for $i in \n" +
      "                for $s in $arg \n" +
      "                return string($s) \n" +
      "            order by $i \n" +
      "            return $i \n" +
      "        }; \n" +
      "        deep-equal(\n" +
      "            local:canon(\n" +
      "                distinct-values((\"1\", 1, 2, 1, 1, 3, 1, 1, 3, xs:anyURI(\"example.com/\"), xs:anyURI(\"example.com/\")))), \n" +
      "            local:canon((\"1\", 1, 2, 3, xs:anyURI(\"example.com/\"))))\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `distinct-values((1, 1))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc7() {
    final XQuery query = new XQuery(
      "distinct-values((1, 1))",
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
   *  A test whose essence is: `distinct-values((-3, -3))`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc8() {
    final XQuery query = new XQuery(
      "distinct-values((-3, -3))",
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
        assertEq("-3")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A test whose essence is: `count(distinct-values((1, 2.0, 3, 2))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqDistinctValuesFunc9() {
    final XQuery query = new XQuery(
      "count(distinct-values((1, 2.0, 3, 2))) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Invoke fn:distinct-values() with an invalid collation. .
   */
  @org.junit.Test
  public void k2SeqDistinctValuesFunc1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((\"1\", \"2\", \"3\"), \"http://www.example.com/COLLATION/NOT/SUPPORTED\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0002")
    );
  }

  /**
   *  Test fn:distinct-values on a mixture of numeric types containing several NaN values. .
   */
  @org.junit.Test
  public void cbclDistinctValues001() {
    final XQuery query = new XQuery(
      "count(distinct-values((xs:integer(\"3\"), xs:float(\"3\"), xs:float(\"NaN\"), xs:double(\"3\"), xs:double(\"NaN\"), xs:decimal(\"3\"), xs:float(\"3\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  test fn:distinct-values with a mix of types .
   */
  @org.junit.Test
  public void cbclDistinctValues002() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdeclare function local:create($arg) as xs:anyAtomicType* {\n" +
      "       \t\t( if ($arg castable as xs:untypedAtomic) \n" +
      "       \t\t  \tthen $arg cast as xs:untypedAtomic else () ), \n" +
      "       \t\t( if ($arg castable as xs:dateTime) \n" +
      "       \t\t  \tthen $arg cast as xs:dateTime else () ), \n" +
      "       \t\t( if ($arg castable as xs:date) \n" +
      "       \t\t \tthen $arg cast as xs:date else () ), \n" +
      "       \t\t( if ($arg castable as xs:time) \n" +
      "       \t\t\tthen $arg cast as xs:time else () ), \n" +
      "       \t\t( if ($arg castable as xs:time) \n" +
      "       \t\t\tthen $arg cast as xs:time else () ), \n" +
      "       \t\t( if ($arg castable as xs:duration) \n" +
      "       \t\t\tthen $arg cast as xs:duration else () ), \n" +
      "       \t\t( if ($arg castable as xs:yearMonthDuration) \n" +
      "       \t\t\tthen $arg cast as xs:yearMonthDuration else () ), \n" +
      "       \t\t( if ($arg castable as xs:dayTimeDuration) \n" +
      "       \t\t\tthen $arg cast as xs:dayTimeDuration else () ), \n" +
      "       \t\t( if ($arg castable as xs:float) \n" +
      "       \t\t\tthen $arg cast as xs:float else () ), \n" +
      "       \t\t( if ($arg castable as xs:double) \n" +
      "       \t\t\tthen $arg cast as xs:double else () ), \n" +
      "       \t\t( if ($arg castable as xs:decimal) \n" +
      "       \t\t\tthen $arg cast as xs:decimal else () ), \n" +
      "       \t\t( if ($arg castable as xs:integer) \n" +
      "       \t\t\tthen $arg cast as xs:integer else () ), \n" +
      "       \t\t( if ($arg castable as xs:nonPositiveInteger) \n" +
      "       \t\t\tthen $arg cast as xs:nonPositiveInteger else () ), \n" +
      "       \t\t( if ($arg castable as xs:negativeInteger) \n" +
      "       \t\t\tthen $arg cast as xs:negativeInteger else () ), \n" +
      "       \t\t( if ($arg castable as xs:long) \n" +
      "       \t\t\tthen $arg cast as xs:long else () ), \n" +
      "       \t\t( if ($arg castable as xs:int) \n" +
      "       \t\t\tthen $arg cast as xs:int else () ), \n" +
      "       \t\t( if ($arg castable as xs:short) \n" +
      "       \t\t\tthen $arg cast as xs:short else () ), \n" +
      "       \t\t( if ($arg castable as xs:byte) \n" +
      "       \t\t\tthen $arg cast as xs:byte else () ), \n" +
      "       \t\t( if ($arg castable as xs:byte) \n" +
      "       \t\t\tthen $arg cast as xs:byte else () ), \n" +
      "       \t\t( if ($arg castable as xs:nonNegativeInteger) \n" +
      "       \t\t\tthen $arg cast as xs:nonNegativeInteger else () ), \n" +
      "       \t\t( if ($arg castable as xs:unsignedLong) \n" +
      "       \t\t\tthen $arg cast as xs:unsignedLong else () ), \n" +
      "       \t\t( if ($arg castable as xs:unsignedInt) \n" +
      "       \t\t\tthen $arg cast as xs:unsignedInt else () ), \n" +
      "       \t\t( if ($arg castable as xs:unsignedShort) \n" +
      "       \t\t\tthen $arg cast as xs:unsignedShort else () ), \n" +
      "       \t\t( if ($arg castable as xs:unsignedByte) \n" +
      "       \t\t\tthen $arg cast as xs:unsignedByte else () ), \n" +
      "       \t\t( if ($arg castable as xs:positiveInteger) \n" +
      "       \t\t\tthen $arg cast as xs:positiveInteger else () ), \n" +
      "       \t\t( if ($arg castable as xs:gYearMonth) \n" +
      "       \t\t\tthen $arg cast as xs:gYearMonth else () ), \n" +
      "       \t\t( if ($arg castable as xs:gYear) \n" +
      "       \t\t\tthen $arg cast as xs:gYear else () ), \n" +
      "       \t\t( if ($arg castable as xs:gMonthDay) \n" +
      "       \t\t\tthen $arg cast as xs:gMonthDay else () ), \n" +
      "       \t\t( if ($arg castable as xs:gDay) \n" +
      "       \t\t\tthen $arg cast as xs:gDay else () ), \n" +
      "       \t\t( if ($arg castable as xs:gMonth) \n" +
      "       \t\t\tthen $arg cast as xs:gMonth else () ), \n" +
      "       \t\t( if ($arg castable as xs:string) \n" +
      "       \t\t\tthen $arg cast as xs:string else () ), \n" +
      "       \t\t( if ($arg castable as xs:normalizedString) \n" +
      "       \t\t\tthen $arg cast as xs:normalizedString else () ), \n" +
      "       \t\t( if ($arg castable as xs:token) \n" +
      "       \t\t\tthen $arg cast as xs:token else () ), \n" +
      "       \t\t( if ($arg castable as xs:language) \n" +
      "       \t\t\tthen $arg cast as xs:language else () ), \n" +
      "       \t\t( if ($arg castable as xs:NMTOKEN) \n" +
      "       \t\t\tthen $arg cast as xs:NMTOKEN else () ), \n" +
      "       \t\t( if ($arg castable as xs:Name) \n" +
      "       \t\t\tthen $arg cast as xs:Name else () ), \n" +
      "       \t\t( if ($arg castable as xs:NCName) \n" +
      "       \t\t\tthen $arg cast as xs:NCName else () ), \n" +
      "       \t\t( if ($arg castable as xs:ID) \n" +
      "       \t\t\tthen $arg cast as xs:ID else () ), \n" +
      "       \t\t( if ($arg castable as xs:IDREF) \n" +
      "       \t\t\tthen $arg cast as xs:IDREF else () ), \n" +
      "       \t\t( if ($arg castable as xs:ENTITY) \n" +
      "       \t\t\tthen $arg cast as xs:ENTITY else () ), \n" +
      "       \t\t( if ($arg castable as xs:boolean) \n" +
      "       \t\t\tthen $arg cast as xs:boolean else () ), \n" +
      "       \t\t( if ($arg castable as xs:base64Binary) \n" +
      "       \t\t\tthen $arg cast as xs:base64Binary else () ), \n" +
      "       \t\t( if ($arg castable as xs:hexBinary) \n" +
      "       \t\t\tthen $arg cast as xs:hexBinary else () ), \n" +
      "       \t\t( if ($arg castable as xs:QName) \n" +
      "       \t\t\tthen $arg cast as xs:QName else () ) \n" +
      "       \t}; \n" +
      "       \tfor $value in fn:distinct-values( ( local:create(0), local:create(-1), local:create(1), local:create(3.141),\n" +
      "       \t \tlocal:create(3.141e0), local:create(3.333), local:create(3.141e0), local:create(3.333e2),\n" +
      "       \t  \tlocal:create('NaN'), local:create('zero'), local:create('false'), local:create('true'),\n" +
      "       \t   \tlocal:create('http://www.example.com/'), local:create('2008-06-01'), local:create('1972-06-01Z'), \n" +
      "       \t   \tlocal:create('2008-06-01T12:00:00'), local:create('1972-06-01T12:00:00+01:00'), \n" +
      "       \t   \tlocal:create('00:00:00'), local:create('12:00:00'), local:create('2008'), local:create('1972Z'), \n" +
      "       \t   \tlocal:create('--06'), local:create('--12Z'), local:create('2008-06'), local:create('1972-12Z'), \n" +
      "       \t   \tlocal:create('--06-01'), local:create('--12-15Z'), local:create('---01'), local:create('---15Z'), \n" +
      "       \t   \tlocal:create('P20Y15M'), local:create('P10Y15M'), local:create('-P2DT15H0M0S'), \n" +
      "       \t   \tlocal:create('-P1DT15H0M0S'), local:create(fn:QName(\"http://www.example.com/example\", \"person\")), \n" +
      "       \t   \tlocal:create(fn:QName(\"http://www.example.com/example\", \"ht:person\")), local:create('-P2DT15H0M0S'), \n" +
      "       \t   \tlocal:create('FFFEFDFC'), local:create('aGVsbG8=') )) \n" +
      "       \t order by string($value) return $value",
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
        assertStringValue(false, "---01 ---01 ---15Z ---15Z --06 --06 --06-01 --06-01 --12-15Z --12-15Z --12Z --12Z -1 -1 -P1DT15H -P1DT15H0M0S -P2DT15H -P2DT15H0M0S 0 0 00:00:00 00:00:00 1 1 12:00:00 12:00:00 1972-06-01T12:00:00+01:00 1972-06-01T12:00:00+01:00 1972-06-01Z 1972-06-01Z 1972-12Z 1972-12Z 1972Z 1972Z 2008 2008 2008 2008 2008 2008-06 2008-06 2008-06-01 2008-06-01 2008-06-01T12:00:00 2008-06-01T12:00:00 3 3.141 3.141 3.141 3.333 3.333 3.333 333 333.3 333.3 333.3 FFFEFDFC FFFEFDFC FFFEFDFC NaN NaN P10Y15M P11Y3M P20Y15M P21Y3M aGVsbG8= aGVsbG8= false false ht:person ht:person http://www.example.com/ person true true true zero zero")
      ||
        assertStringValue(false, "---01 ---01 ---15Z ---15Z --06 --06 --06-01 --06-01 --12-15Z --12-15Z --12Z --12Z -1 -1 -P1DT15H -P1DT15H0M0S -P2DT15H -P2DT15H0M0S 0 0 00:00:00 00:00:00 1 1 12:00:00 12:00:00 1972-06-01T12:00:00+01:00 1972-06-01T12:00:00+01:00 1972-06-01Z 1972-06-01Z 1972-12Z 1972-12Z 1972Z 1972Z 2008 2008 2008 2008 2008 2008-06 2008-06 2008-06-01 2008-06-01 2008-06-01T12:00:00 2008-06-01T12:00:00 3 3.141 3.141 3.141 3.333 3.333 3.333 333 333.3 333.3 333.3 FFFEFDFC FFFEFDFC FFFEFDFC NaN NaN P10Y15M P11Y3M P20Y15M P21Y3M aGVsbG8= aGVsbG8= false false ht:person http://www.example.com/ person person true true true zero zero")
      ||
        assertStringValue(false, "---01 ---01 ---15Z ---15Z --06 --06 --06-01 --06-01 --12-15Z --12-15Z --12Z --12Z -1 -1 -P1DT15H -P1DT15H0M0S -P2DT15H -P2DT15H0M0S 0 0 00:00:00 00:00:00 1 1 12:00:00 12:00:00 1972-06-01T12:00:00+01:00 1972-06-01T12:00:00+01:00 1972-06-01Z 1972-06-01Z 1972-12Z 1972-12Z 1972Z 1972Z 2008 2008 2008 2008 2008 2008-06 2008-06 2008-06-01 2008-06-01 2008-06-01T12:00:00 2008-06-01T12:00:00 3 3.141 3.141 3.141 3.333 3.333 3.333 333 333.3 333.3 333.3 FFFEFDFC FFFEFDFC FFFEFDFC FFFEFDFC NaN NaN NaN P10Y15M P10Y15M P11Y3M P20Y15M P20Y15M P21Y3M aGVsbG8= aGVsbG8= false false false ht:person http://www.example.com/ person person true true true true zero zero zero")
      )
    );
  }

  /**
   *  test fn:distinct-values with a mix of types .
   */
  @org.junit.Test
  public void cbclDistinctValues002b() {
    final XQuery query = new XQuery(
      "declare function local:create($arg) as xs:anyAtomicType* { ( if ($arg castable as xs:untypedAtomic) then $arg cast as xs:untypedAtomic else () ), ( if ($arg castable as xs:dateTime) then $arg cast as xs:dateTime else () ), ( if ($arg castable as xs:date) then $arg cast as xs:date else () ), ( if ($arg castable as xs:time) then $arg cast as xs:time else () ), ( if ($arg castable as xs:time) then $arg cast as xs:time else () ), ( if ($arg castable as xs:duration) then $arg cast as xs:duration else () ), ( if ($arg castable as xs:yearMonthDuration) then $arg cast as xs:yearMonthDuration else () ), ( if ($arg castable as xs:dayTimeDuration) then $arg cast as xs:dayTimeDuration else () ), ( if ($arg castable as xs:float) then $arg cast as xs:float else () ), ( if ($arg castable as xs:double) then $arg cast as xs:double else () ), ( if ($arg castable as xs:decimal) then $arg cast as xs:decimal else () ), ( if ($arg castable as xs:integer) then $arg cast as xs:integer else () ), ( if ($arg castable as xs:nonPositiveInteger) then $arg cast as xs:nonPositiveInteger else () ), ( if ($arg castable as xs:negativeInteger) then $arg cast as xs:negativeInteger else () ), ( if ($arg castable as xs:long) then $arg cast as xs:long else () ), ( if ($arg castable as xs:int) then $arg cast as xs:int else () ), ( if ($arg castable as xs:short) then $arg cast as xs:short else () ), ( if ($arg castable as xs:byte) then $arg cast as xs:byte else () ), ( if ($arg castable as xs:byte) then $arg cast as xs:byte else () ), ( if ($arg castable as xs:nonNegativeInteger) then $arg cast as xs:nonNegativeInteger else () ), ( if ($arg castable as xs:unsignedLong) then $arg cast as xs:unsignedLong else () ), ( if ($arg castable as xs:unsignedInt) then $arg cast as xs:unsignedInt else () ), ( if ($arg castable as xs:unsignedShort) then $arg cast as xs:unsignedShort else () ), ( if ($arg castable as xs:unsignedByte) then $arg cast as xs:unsignedByte else () ), ( if ($arg castable as xs:positiveInteger) then $arg cast as xs:positiveInteger else () ), ( if ($arg castable as xs:gYearMonth) then $arg cast as xs:gYearMonth else () ), ( if ($arg castable as xs:gYear) then $arg cast as xs:gYear else () ), ( if ($arg castable as xs:gMonthDay) then $arg cast as xs:gMonthDay else () ), ( if ($arg castable as xs:gDay) then $arg cast as xs:gDay else () ), ( if ($arg castable as xs:gMonth) then $arg cast as xs:gMonth else () ), ( if ($arg castable as xs:string) then $arg cast as xs:string else () ), ( if ($arg castable as xs:normalizedString) then $arg cast as xs:normalizedString else () ), ( if ($arg castable as xs:token) then $arg cast as xs:token else () ), ( if ($arg castable as xs:language) then $arg cast as xs:language else () ), ( if ($arg castable as xs:NMTOKEN) then $arg cast as xs:NMTOKEN else () ), ( if ($arg castable as xs:Name) then $arg cast as xs:Name else () ), ( if ($arg castable as xs:NCName) then $arg cast as xs:NCName else () ), ( if ($arg castable as xs:ID) then $arg cast as xs:ID else () ), ( if ($arg castable as xs:IDREF) then $arg cast as xs:IDREF else () ), ( if ($arg castable as xs:ENTITY) then $arg cast as xs:ENTITY else () ), ( if ($arg castable as xs:boolean) then $arg cast as xs:boolean else () ), ( if ($arg castable as xs:base64Binary) then $arg cast as xs:base64Binary else () ), ( if ($arg castable as xs:hexBinary) then $arg cast as xs:hexBinary else () ), ( if ($arg castable as xs:QName) then $arg cast as xs:QName else () ) }; for $value in fn:distinct-values( ( local:create(0), local:create(-1), local:create(1), local:create(3.141), local:create(3.141e0), local:create(3.333), local:create(3.141e0), local:create(3.333e2), local:create('NaN'), local:create('zero'), local:create('false'), local:create('true'), local:create('http://www.example.com/'), local:create('2008-06-01'), local:create('1972-06-01Z'), local:create('2008-06-01T12:00:00'), local:create('1972-06-01T12:00:00+01:00'), local:create('00:00:00'), local:create('12:00:00'), local:create('2008'), local:create('1972Z'), local:create('--06'), local:create('--12Z'), local:create('2008-06'), local:create('1972-12Z'), local:create('--06-01'), local:create('--12-15Z'), local:create('---01'), local:create('---15Z'), local:create('P20Y15M'), local:create('P10Y15M'), local:create('-P2DT15H0M0S'), local:create('-P1DT15H0M0S'), local:create(fn:QName(\"http://www.example.com/example\", \"person\")), local:create(fn:QName(\"http://www.example.com/example\", \"ht:person\")), local:create('-P2DT15H0M0S'), local:create('FFFEFDFC'), local:create('aGVsbG8=') )) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "---01 ---01 ---15Z ---15Z --06 --06 --06-01 --06-01 --12-15Z --12-15Z --12Z --12Z -1 -1 -P1DT15H -P1DT15H0M0S -P2DT15H -P2DT15H0M0S 0 0 00:00:00 00:00:00 1 1 12:00:00 12:00:00 1972-06-01T12:00:00+01:00 1972-06-01T12:00:00+01:00 1972-06-01Z 1972-06-01Z 1972-12Z 1972-12Z 1972Z 1972Z 2008 2008 2008 2008 2008 2008-06 2008-06 2008-06-01 2008-06-01 2008-06-01T12:00:00 2008-06-01T12:00:00 3 3.141 3.141 3.141 3.333 3.333 3.333 333 333.3 333.3 333.3 FFFEFDFC FFFEFDFC FFFEFDFC FFFEFDFC NaN NaN NaN P10Y15M P10Y15M P11Y3M P20Y15M P20Y15M P21Y3M aGVsbG8= aGVsbG8= false false false ht:person http://www.example.com/ person person true true true true zero zero zero")
    );
  }

  /**
   *  Test with static context dependant values .
   */
  @org.junit.Test
  public void cbclDistinctValues003() {
    final XQuery query = new XQuery(
      "distinct-values(\n" +
      "              (xs:dateTime(\"2008-01-01T13:00:00\"),\n" +
      "               adjust-dateTime-to-timezone(xs:dateTime(\"2008-01-01T13:00:00\"))))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertCount(1)
    );
  }

  /**
   *  test fn:distinct-values with xs:date .
   */
  @org.junit.Test
  public void cbclDistinctValues004() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:date('2008-06-01'), xs:date('2008-06-01'), xs:date('2012-06-01'), xs:date('1918-11-11Z'), xs:date('1972-06-01Z'), xs:date('1972-06-01Z') )) order by $value return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1918-11-11Z 1972-06-01Z 2008-06-01 2012-06-01")
    );
  }

  /**
   *  test fn:distinct-values with xs:dateTime .
   */
  @org.junit.Test
  public void cbclDistinctValues005() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:dateTime('2008-06-01T12:00:00'), xs:dateTime('2008-06-01T12:00:00'), xs:dateTime('2008-06-01T00:00:00'), xs:dateTime('2008-06-02T00:00:00'), xs:dateTime('1918-11-11T11:00:00Z'), xs:dateTime('1972-06-01T13:00:00Z'), xs:dateTime('1972-06-01T13:00:00Z') )) order by $value return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1918-11-11T11:00:00Z 1972-06-01T13:00:00Z 2008-06-01T00:00:00 2008-06-01T12:00:00 2008-06-02T00:00:00")
    );
  }

  /**
   *  test fn:distinct-values with xs:time .
   */
  @org.junit.Test
  public void cbclDistinctValues006() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:time('12:00:00'), xs:time('11:00:00'), xs:time('12:00:00'))) order by $value return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "11:00:00 12:00:00")
    );
  }

  /**
   *  test fn:distinct-values with xs:time .
   */
  @org.junit.Test
  public void cbclDistinctValues007() {
    final XQuery query = new XQuery(
      "for $value at $p in ( for $time in fn:distinct-values(( xs:time('12:00:00'), xs:time('12:00:00'), xs:time('20:00:00'), xs:time('01:00:00+12:00'), xs:time('02:00:00+13:00'))) order by $time return $time ) return adjust-time-to-timezone($value, (xs:dayTimeDuration(\"PT0S\")[$p]))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "13:00:00Z 12:00:00 20:00:00")
    );
  }

  /**
   *  test fn:distinct-values with xs:hexBinary .
   */
  @org.junit.Test
  public void cbclDistinctValues008() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:hexBinary('FFFF'), xs:hexBinary('FFFF'), xs:hexBinary('FFFE'), xs:hexBinary('FF'))) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "FF FFFE FFFF")
    );
  }

  /**
   *  test fn:distinct-values with xs:base64Binary .
   */
  @org.junit.Test
  public void cbclDistinctValues009() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:base64Binary('aGVsbG8NCg=='), xs:base64Binary('aGVsbG8NCg=='), xs:base64Binary('aGFsbG8NCg=='), xs:base64Binary('aGkNCg=='))) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "aGFsbG8NCg== aGVsbG8NCg== aGkNCg==")
    );
  }

  /**
   *  test fn:distinct-values with xs:untypedAtomic .
   */
  @org.junit.Test
  public void cbclDistinctValues010() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:untypedAtomic('a'), xs:untypedAtomic('a'), xs:untypedAtomic('b'), xs:untypedAtomic(''))) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " a b")
    );
  }

  /**
   *  test fn:distinct-values with xs:string .
   */
  @org.junit.Test
  public void cbclDistinctValues011() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:string('a'), xs:string('a'), xs:string('b'), xs:string(''))) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, " a b")
    );
  }

  /**
   *  test fn:distinct-values with xs:gYear .
   */
  @org.junit.Test
  public void cbclDistinctValues012() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:gYear('2008'), xs:gYear('2008'), xs:gYear('1972'))) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1972 2008")
    );
  }

  /**
   *  test fn:distinct-values with durations .
   */
  @org.junit.Test
  public void cbclDistinctValues013() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:dayTimeDuration('P0D'), xs:yearMonthDuration('P0Y'), xs:duration('P0Y'), xs:duration('P0Y'), xs:yearMonthDuration('P0Y'), xs:dayTimeDuration('P0D'), xs:dayTimeDuration('P1D'), xs:yearMonthDuration('P1Y'), xs:duration('P1Y'))) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P1D P1Y PT0S")
    );
  }

  /**
   *  test fn:distinct-values with xs:gMnnthDay .
   */
  @org.junit.Test
  public void cbclDistinctValues014() {
    final XQuery query = new XQuery(
      "for $value in fn:distinct-values(( xs:gMonthDay('--06-16'), xs:gMonthDay('--06-16'), xs:gMonthDay('--12-15'))) order by string($value) return $value",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "--06-16 --12-15")
    );
  }

  /**
   *  Tests distinct values with unknown, but unneeded collation .
   */
  @org.junit.Test
  public void cbclDistinctValues016() {
    final XQuery query = new XQuery(
      "for $x in 65 to 75 return distinct-values(1 to 10,codepoints-to-string($x to $x+10))",
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
        assertStringValue(false, "1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10")
      ||
        error("FOCH0002")
      )
    );
  }

  /**
   *  Test Bugzilla #5183, [FO] Effect of type promotion in fn:distinct-values .
   */
  @org.junit.Test
  public void fnDistinctValues1() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $input := (xs:float('1.0'), xs:decimal('1.0000000000100000000001'), \n" +
      "                       xs:double( '1.00000000001'), xs:float('2.0'), \n" +
      "                       xs:decimal('2.0000000000100000000001'), xs:double( '2.00000000001')), \n" +
      "            $distinct := distinct-values($input) \n" +
      "        return ( (every $n in $input satisfies $n = $distinct) and \n" +
      "        (every $bool in (for $d1 at $p in $distinct, $d2 in $distinct [position() > $p] return $d1 eq $d2) satisfies not($bool)) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * distinct-values() on a somewhat larger set of values.
   */
  @org.junit.Test
  public void fnDistinctValues2() {
    final XQuery query = new XQuery(
      "distinct-values((1 to 300, 100 to 400, 29, 145, 20 to 50, for $x in (30 to 40) return xs:string($x), \"foo\", \"bar\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("1 to 400, \"30\", \"31\", \"32\", \"33\", \"34\", \"35\", \"36\", \"37\", \"38\", \"39\", \"40\", \"foo\", \"bar\"")
    );
  }

  /**
   *  arg: sequence of integer & decimal .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs001() {
    final XQuery query = new XQuery(
      "fn:distinct-values((1, 2))",
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
        assertStringValue(false, "2 1")
      ||
        assertStringValue(false, "1 2")
      )
    );
  }

  /**
   *  arg: sequence of integer .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs002() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( 1, (1), ((1)) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  arg: sequence of integer & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs003() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( 1, 1.0e0))",
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
        assertCount(1)
      &&
        assertEq("1")
      &&
        (
          assertType("xs:integer")
        ||
          assertType("xs:double")
        )
      )
    );
  }

  /**
   *  arg: sequence of integer .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs004() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( xs:integer(1), 1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  arg: sequence of integer & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs005() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( 0e0, -0, 0, 1 ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("1, 0")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs006() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( \"cat\", 'CAT' ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("\"cat\", \"CAT\"")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs007() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( xs:string(\"hello\"), \"hello\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "hello")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs008() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( xs:string(\"\"), \"\", ''))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "")
    );
  }

  /**
   *  arg: sequence of integer,decimal,boolean,string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs009() {
    final XQuery query = new XQuery(
      "fn:distinct-values((1, true(), true(), ()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("1, true()")
    );
  }

  /**
   *  arg: sequence of decimal .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs010() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), xs:decimal('1.2000000000000001')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("1.2000000000000001, 1.2")
    );
  }

  /**
   *  arg: sequence of decimal & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs011() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), '1.2'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("\"1.2\", 1.2")
    );
  }

  /**
   *  arg: sequence of decimal & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs012() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), xs:float('1.2')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.2")
    );
  }

  /**
   *  arg: sequence of decimal & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs013() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal('1.2'), xs:double('1.2')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.2")
    );
  }

  /**
   *  arg: sequence of float & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs014() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), 'NaN'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN NaN")
    );
  }

  /**
   *  arg: sequence of float & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs015() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('INF'), 'INF'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF INF")
    );
  }

  /**
   *  arg: sequence of float & string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs016() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('-INF'), '-INF'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF -INF")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs017() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('INF'), xs:float('INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs018() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('-INF'), xs:float('INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("xs:float('-INF'), xs:float('INF')")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs019() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), xs:float('NaN')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of float & float .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs020() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), xs:float('NaN')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of float & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs021() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('NaN'), xs:double('NaN')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of float & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs022() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('INF'), xs:double('INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "INF")
    );
  }

  /**
   *  arg: sequence of float & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs023() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float('-INF'), xs:double('-INF')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-INF")
    );
  }

  /**
   *  arg: sequence of double & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs024() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double('-INF'), xs:double('INF')))",
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
        assertStringValue(false, "INF -INF")
      ||
        assertStringValue(false, "-INF INF")
      )
    );
  }

  /**
   *  arg: sequence of double & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs025() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double('NaN'), xs:double('NaN')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of double & double .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs026() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double('NaN'), xs:double('NaN')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs027() {
    final XQuery query = new XQuery(
      "fn:distinct-values((\"NaN\", \"-NaN\"))",
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
        assertStringValue(false, "NaN -NaN")
      ||
        assertStringValue(false, "-NaN NaN")
      )
    );
  }

  /**
   *  arg: sequence of string .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs028() {
    final XQuery query = new XQuery(
      "fn:distinct-values((\"-INF\", \"INF\"))",
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
        assertStringValue(false, "INF -INF")
      ||
        assertStringValue(false, "-INF INF")
      )
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs029() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:boolean('true'), true()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs030() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:boolean('true'), xs:boolean('1')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs031() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:boolean('false'), xs:boolean('0')))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  arg: sequence of boolean .
   */
  @org.junit.Test
  public void fnDistinctValuesMixedArgs032() {
    final XQuery query = new XQuery(
      "fn:distinct-values(( true(), false(), () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertPermutation("true(), false()")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdbl1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesdbl1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdbl1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:double(\"1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdec1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesdec1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesdec1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesflt1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"-3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesflt1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesflt1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:float(\"3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("xs:float(\"3.4028235E38\")")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesint1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesint1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesint1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesintg1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesintg1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesintg1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValueslng1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValueslng1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValueslng1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnint1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesnint1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnint1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnni1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesnni1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnni1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnpi1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesnpi1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesnpi1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuespint1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuespint1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuespint1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:positiveInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuessht1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuessht1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuessht1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesulng1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesulng1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesulng1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesusht1args1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnDistinctValuesusht1args2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "distinct-values" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnDistinctValuesusht1args3() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }
}
