package org.basex.qt3ts.misc;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the CombinedErrorCodes operator.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class MiscCombinedErrorCodes extends QT3TestSet {

  /**
   *  check that decimal division by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00011() {
    final XQuery query = new XQuery(
      "1.0 div 0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that integer division by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00012() {
    final XQuery query = new XQuery(
      "1 div 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that double integer division by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00013() {
    final XQuery query = new XQuery(
      "1.0e0 idiv 0.0e0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that float integer division by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00014() {
    final XQuery query = new XQuery(
      "xs:float(1.0e0) idiv xs:float(0.0e0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that decimal integer division by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00015() {
    final XQuery query = new XQuery(
      "1.0 idiv 0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that integer integer division by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00016() {
    final XQuery query = new XQuery(
      "1 div 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that integer mod ivision by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00017() {
    final XQuery query = new XQuery(
      "1 mod 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that decimal mod by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00018() {
    final XQuery query = new XQuery(
      "1.0 mod 0.0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that yearMonthDuration div by zero throws FOAR0001 .
   */
  @org.junit.Test
  public void fOAR00019() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration('P1Y2M') div xs:yearMonthDuration('P0Y0M')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0001")
    );
  }

  /**
   *  check that duration functions throw FOAR0002 .
   */
  @org.junit.Test
  public void fOAR00025() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P5999999999999999999DT00H00M01S') div xs:dayTimeDuration('P0DT00H00M0.000001S')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  check that underflow/overflow throws FOAR0002 .
   */
  @org.junit.Test
  public void fOAR00021() {
    final XQuery query = new XQuery(
      "2e308",
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
        error("FOAR0002")
      ||
        assertStringValue(false, "INF")
      )
    );
  }

  /**
   *  Evaluates The "op:numeric-integer-divide" operator for error condition. .
   */
  @org.junit.Test
  public void fOAR00023() {
    final XQuery query = new XQuery(
      "(0 div 0E0) idiv xs:integer(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  Evaluates The "op:numeric-integer-divide" operator for error condition. .
   */
  @org.junit.Test
  public void fOAR00024() {
    final XQuery query = new XQuery(
      "xs:double('INF') idiv xs:integer(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOAR0002")
    );
  }

  /**
   *  check that values too large for decimal throws FOCA0001 .
   */
  @org.junit.Test
  public void fOCA00011() {
    final XQuery query = new XQuery(
      "xs:decimal(1e308)",
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
        error("FOCA0001")
      ||
        (
          assertType("xs:decimal")
        &&
          assertQuery("string-length(string($result)) = (308, 309)")
        &&
          assertQuery("starts-with(string($result), '1000000000') or starts-with(string($result), '999999999')")
        )
      )
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0001 .
   */
  @org.junit.Test
  public void fOCA00021() {
    final XQuery query = new XQuery(
      "xs:float('INF') cast as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00022() {
    final XQuery query = new XQuery(
      "QName(\"http://www.w3.org/\", \"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00023() {
    final XQuery query = new XQuery(
      "QName(\"\", \"prefix:localName\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00024() {
    final XQuery query = new XQuery(
      "QName(\"http://www.w3.org/\", \"1prefix:localName\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00025() {
    final XQuery query = new XQuery(
      "QName(\"http://www.w3.org/\", \"prefix:2localName\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00026() {
    final XQuery query = new XQuery(
      "QName(\"\", \"2localName\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00027() {
    final XQuery query = new XQuery(
      "resolve-QName(\"2localName\", <localName />)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00028() {
    final XQuery query = new XQuery(
      "resolve-QName(\"1prefix:localName\", <localName />)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that invalid lexical values throw FOCA0002 .
   */
  @org.junit.Test
  public void fOCA00029() {
    final XQuery query = new XQuery(
      "resolve-QName(\"2localName\", <localName />)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0002")
    );
  }

  /**
   *  check that large integers throw FOCA0003 .
   */
  @org.junit.Test
  public void fOCA00031() {
    final XQuery query = new XQuery(
      "xs:integer(xs:double(1e308))",
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
        error("FOCA0003")
      ||
        (
          assertType("xs:integer")
        &&
          assertQuery("string-length(string($result)) = (308, 309)")
        &&
          assertQuery("starts-with(string($result), '1000000000') or starts-with(string($result), '999999999')")
        )
      )
    );
  }

  /**
   *  check that using NaN throws FOCA0005 .
   */
  @org.junit.Test
  public void fOCA00051() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P3DT10H30M') div xs:double('NaN')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   *  check that using NaN throws FOCA0005 .
   */
  @org.junit.Test
  public void fOCA00052() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P3DT10H30M') * xs:double('NaN')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   *  check that using NaN throws FOCA0005 .
   */
  @org.junit.Test
  public void fOCA00053() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P3DT10H30M') div xs:double('NaN')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   *  check that using NaN throws FOCA0005 .
   */
  @org.junit.Test
  public void fOCA00054() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P3DT10H30M') * xs:double('NaN')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCA0005")
    );
  }

  /**
   *  check that invalid codepoints throw FOCH0001 .
   */
  @org.junit.Test
  public void fOCH0001() {
    final XQuery query = new XQuery(
      "codepoints-to-string(0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOCH0001")
    );
  }

  /**
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00021() {
    final XQuery query = new XQuery(
      "compare('a', 'b', 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH000210() {
    final XQuery query = new XQuery(
      "substring-before('a', 'b', 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00022() {
    final XQuery query = new XQuery(
      "deep-equal('a', 'b', 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00023() {
    final XQuery query = new XQuery(
      "distinct-values(('a', 'b'), 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00024() {
    final XQuery query = new XQuery(
      "ends-with('a', 'b', 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00025() {
    final XQuery query = new XQuery(
      "index-of('a', 'b', 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00026() {
    final XQuery query = new XQuery(
      "max(('a', 'b'), 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00027() {
    final XQuery query = new XQuery(
      "min(('a', 'b'), 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00028() {
    final XQuery query = new XQuery(
      "starts-with('a', 'b', 'http://www.cbcl.co.u,/collation')",
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
   *  check that invalid collations throw FOCH0002 .
   */
  @org.junit.Test
  public void fOCH00029() {
    final XQuery query = new XQuery(
      "substring-after('a', 'b', 'http://www.cbcl.co.u,/collation')",
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
   *  check that fn:id throws FOAR0001 .
   */
  @org.junit.Test
  public void fODC00011() {
    final XQuery query = new XQuery(
      "fn:id('id', <a />)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0001")
    );
  }

  /**
   *  check that fn:idref throws FOAR0001 .
   */
  @org.junit.Test
  public void fODC00012() {
    final XQuery query = new XQuery(
      "<a />/fn:idref('id')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0001")
    );
  }

  /**
   *  check that fn:doc throws FOAR0002 .
   */
  @org.junit.Test
  public void fODC00021() {
    final XQuery query = new XQuery(
      "doc('http://www.example.org/notFound.xml')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  check that fn:doc throws FOAR0002 .
   */
  @org.junit.Test
  public void fODC00022() {
    final XQuery query = new XQuery(
      "doc('http://www.example.org/notFound.xml')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  check that fn:doc throws FOAR0002 .
   */
  @org.junit.Test
  public void fODC00023() {
    final XQuery query = new XQuery(
      "doc('http://www.example.org/notFound.xml')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  check that fn:collection() throws FODC0002 .
   */
  @org.junit.Test
  public void fODC00024() {
    final XQuery query = new XQuery(
      "collection()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0002")
    );
  }

  /**
   *  check that fn:collection throws FODC0004 .
   */
  @org.junit.Test
  public void fODC0004() {
    final XQuery query = new XQuery(
      "collection('%gg')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0004")
    );
  }

  /**
   *  check that fn:doc throws FODC0005 .
   */
  @org.junit.Test
  public void fODC00051() {
    final XQuery query = new XQuery(
      "doc('%gg')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODC0005")
    );
  }

  /**
   *  check that fn:doc-available throws FODC0005 .
   */
  @org.junit.Test
  public void fODC00052() {
    final XQuery query = new XQuery(
      "doc-available('%gg')",
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
        error("FODC0005")
      ||
        assertBoolean(false)
      )
    );
  }

  /**
   *  check that date/time functions throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00011() {
    final XQuery query = new XQuery(
      "adjust-dateTime-to-timezone( xs:dateTime(\"25252734927766555-07-28T23:59:59-14:00\"), xs:dayTimeDuration(\"PT14H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT000110() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28-14:00\") - xs:yearMonthDuration(\"-P1Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT000111() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28-14:00\") - xs:date(\"-25252734927766555-07-28-14:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT000112() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"25252734927766555-07-28T23:59:59-14:00\") - xs:dateTime(\"-25252734927766555-07-28T23:59:59-14:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time functions throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00012() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone( xs:date(\"25252734927766555-07-28-14:00\"), xs:dayTimeDuration(\"PT14H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00013() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"25252734927766555-07-28T23:59:59-14:00\") + xs:dayTimeDuration(\"PT14H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00014() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28-14:00\") + xs:dayTimeDuration(\"PT24H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00015() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"25252734927766555-07-28T23:59:59-14:00\") - xs:dayTimeDuration(\"-PT14H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00016() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28-14:00\") - xs:dayTimeDuration(\"-PT24H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00017() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"25252734927766555-07-28T23:59:59-14:00\") + xs:yearMonthDuration(\"P1Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00018() {
    final XQuery query = new XQuery(
      "xs:date(\"25252734927766555-07-28-14:00\") + xs:yearMonthDuration(\"P1Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that date/time operators throw FODT0001 .
   */
  @org.junit.Test
  public void fODT00019() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"25252734927766555-07-28T23:59:59-14:00\") - xs:yearMonthDuration(\"-P1Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00021() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P5999999999999999999DT00H00M01S') + xs:dayTimeDuration('P4999999999999999999DT00H00M01S')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00022() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P5999999999999999999DT00H00M01S') * 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00023() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P5999999999999999999DT00H00M01S') div 0.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00024() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration('P5999999999999999999DT00H00M01S') - xs:dayTimeDuration('-P5999999999999999999DT00H00M01S')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00026() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration('P768614336404564650Y0M') + xs:yearMonthDuration('P768614336404564650Y1M')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00027() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration('P768614336404564650Y0M') * 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00028() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration('P768614336404564650Y0M') div 0.5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that duration functions throw FODT0002 .
   */
  @org.junit.Test
  public void fODT00029() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration('P768614336404564650Y0M') - xs:yearMonthDuration('-P768614336404564650Y0M')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0002")
    );
  }

  /**
   *  check that bad timezones throw FODT0003 .
   */
  @org.junit.Test
  public void fODT00031() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone( xs:date(\"2001-07-28-14:00\"), xs:dayTimeDuration(\"PT15H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0003")
    );
  }

  /**
   *  check that bad timezones throw FODT0003 .
   */
  @org.junit.Test
  public void fODT00032() {
    final XQuery query = new XQuery(
      "adjust-date-to-timezone( xs:date(\"2001-07-28-14:00\"), xs:dayTimeDuration(\"-PT15H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0003")
    );
  }

  /**
   *  check that fn:error() throws FOER0000 .
   */
  @org.junit.Test
  public void fOER0000() {
    final XQuery query = new XQuery(
      "error()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  check that missing prefix throws FONS0004 .
   */
  @org.junit.Test
  public void fONS00041() {
    final XQuery query = new XQuery(
      "resolve-QName('prefix:localName', <element />)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FONS0004")
    );
  }

  /**
   *  check that missing prefix throws FONS0004 .
   */
  @org.junit.Test
  public void fONS00042() {
    final XQuery query = new XQuery(
      "xs:QName('prefix:localName')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FONS0004")
    );
  }

  /**
   *  check that invalid construction throws FORG0001 .
   */
  @org.junit.Test
  public void fORG0001() {
    final XQuery query = new XQuery(
      "xs:integer('INF')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0001")
    );
  }

  /**
   *  check that resolve-uri throws FORG0002 .
   */
  @org.junit.Test
  public void fORG0002() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.w3.org/\"; resolve-uri(\"%gg\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0002")
    );
  }

  /**
   *  check that zero-or-one throws FORG0003 .
   */
  @org.junit.Test
  public void fORG0003() {
    final XQuery query = new XQuery(
      "zero-or-one( (1, 2, 3) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0003")
    );
  }

  /**
   *  check that one-or-more throws FORG0004 .
   */
  @org.junit.Test
  public void fORG0004() {
    final XQuery query = new XQuery(
      "one-or-more( () )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0004")
    );
  }

  /**
   *  check that exactly-one throws FORG0005 .
   */
  @org.junit.Test
  public void fORG0005() {
    final XQuery query = new XQuery(
      "exactly-one( () )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0005")
    );
  }

  /**
   *  check that exactly-one throws FORG0005 .
   */
  @org.junit.Test
  public void fORG000601() {
    final XQuery query = new XQuery(
      "fn:boolean( xs:date('2007-01-01') )",
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
        error("FORG0006")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  check that fn:avg throws FORG0006-2 .
   */
  @org.junit.Test
  public void fORG000602() {
    final XQuery query = new XQuery(
      "fn:avg( (xs:yearMonthDuration('P1Y0M'), 1) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  check that fn:min throws FORG0006-3 .
   */
  @org.junit.Test
  public void fORG000603() {
    final XQuery query = new XQuery(
      "fn:min( (xs:yearMonthDuration('P1Y0M'), 1) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  check that fn:max throws FORG0006-3 .
   */
  @org.junit.Test
  public void fORG000604() {
    final XQuery query = new XQuery(
      "fn:max( (xs:yearMonthDuration('P1Y0M'), 1) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  check that fn:sum throws FORG0006 .
   */
  @org.junit.Test
  public void fORG000605() {
    final XQuery query = new XQuery(
      "fn:sum( (xs:yearMonthDuration('P1Y0M'), 1) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  check that fn:dateTime can throw FORG0008 .
   */
  @org.junit.Test
  public void fORG0008() {
    final XQuery query = new XQuery(
      "dateTime(xs:date('2001-01-01-14:00'), xs:time('01:01:01+14:00'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0008")
    );
  }

  /**
   *  check resolution against a relative URI .
   */
  @org.junit.Test
  public void fORG0009() {
    final XQuery query = new XQuery(
      "resolve-uri('../../', '../../')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0002")
    );
  }

  /**
   *  Schema import binding to no namespace, and no location hint. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes1() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\"; 1 eq 1",
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
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes10() {
    final XQuery query = new XQuery(
      "validate { () }",
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
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes11() {
    final XQuery query = new XQuery(
      "validate lax { 1 }",
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
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes12() {
    final XQuery query = new XQuery(
      "validate strict { 1 }",
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
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes13() {
    final XQuery query = new XQuery(
      "validate lax { }",
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
        error("XPST0003")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes14() {
    final XQuery query = new XQuery(
      "validate strict { }",
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
        error("XPST0003")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes15() {
    final XQuery query = new XQuery(
      "validate { }",
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
        error("XPST0003")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  Schema import binding to no namespace, but has a location hint. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes2() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\"; 1 eq 1",
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
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to no namespace, but has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes3() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2\", \"http://example.com/3\"; 1 eq 1",
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
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to no namespace, but has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes4() {
    final XQuery query = new XQuery(
      "import schema \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2\", \"http://example.com/3\"; 1 eq 1",
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
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to a namespace, and has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes5() {
    final XQuery query = new XQuery(
      "import schema namespace prefix = \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2DOESNOTEXIST\", \"http://example.com/3DOESNOTEXIST\"; 1 eq 1",
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
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  Schema import binding to the default element namespace, and has three location hints. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes6() {
    final XQuery query = new XQuery(
      "import schema default element namespace \"http://example.com/NSNOTRECOGNIZED\" at \"http://example.com/DOESNOTEXIST\", \"http://example.com/2DOESNOTEXIST\", \"http://example.com/3DOESNOTEXIST\"; 1 eq 1",
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
        error("XQST0009")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  ':=' cannot be used to assing namespaces in 'import schema'. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes7() {
    final XQuery query = new XQuery(
      "import schema namespace NCName := \"http://example.com/Dummy\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  A 'validate' expression with an invalid operand. .
   */
  @org.junit.Test
  public void kCombinedErrorCodes9() {
    final XQuery query = new XQuery(
      "validate { 1 }",
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
        error("XQTY0030")
      ||
        error("XQST0075")
      )
    );
  }

  /**
   *  check that unassigned context item is reported correctly .
   */
  @org.junit.Test
  public void xPDY000201() {
    final XQuery query = new XQuery(
      ".",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  check that unassigned context item is reported correctly .
   */
  @org.junit.Test
  public void xPDY000202() {
    final XQuery query = new XQuery(
      "declare variable $variable external; $variable",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  check that treat as errors are reported correctly .
   */
  @org.junit.Test
  public void xPDY0050() {
    final XQuery query = new XQuery(
      "1 treat as node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0050")
    );
  }

  /**
   *  test type checking of attribute names .
   */
  @org.junit.Test
  public void xPTY000401() {
    final XQuery query = new XQuery(
      "attribute { 1 } { 1 }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of element names .
   */
  @org.junit.Test
  public void xPTY000402() {
    final XQuery query = new XQuery(
      "element { 1 } { }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad casts .
   */
  @org.junit.Test
  public void xPTY000403() {
    final XQuery query = new XQuery(
      "() cast as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad casts .
   */
  @org.junit.Test
  public void xPTY000404() {
    final XQuery query = new XQuery(
      "(1, 2) cast as xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad 'as' clauses .
   */
  @org.junit.Test
  public void xPTY000405() {
    final XQuery query = new XQuery(
      "let $x as node() := 1 return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad 'as' clauses .
   */
  @org.junit.Test
  public void xPTY000406() {
    final XQuery query = new XQuery(
      "for $x as node() in (1, 2, 3) return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad document constructions .
   */
  @org.junit.Test
  public void xPTY000407() {
    final XQuery query = new XQuery(
      "document { attribute {'foo'} {} }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad document constructions .
   */
  @org.junit.Test
  public void xPTY000408() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:content($type as xs:integer) { if ($type eq 1) then attribute {'foo'} {} else <foo /> }; document { foo:content(1) }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad order-by comparisons .
   */
  @org.junit.Test
  public void xPTY000409() {
    final XQuery query = new XQuery(
      "for $x in (1, 'hello', xs:date('2007-11-28')) order by $x return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of function calls .
   */
  @org.junit.Test
  public void xPTY000410() {
    final XQuery query = new XQuery(
      "fn:upper-case(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of function calls .
   */
  @org.junit.Test
  public void xPTY000411() {
    final XQuery query = new XQuery(
      "fn:tokenize('foo', () )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of function calls .
   */
  @org.junit.Test
  public void xPTY000412() {
    final XQuery query = new XQuery(
      "fn:error( () )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000413() {
    final XQuery query = new XQuery(
      "\"string\" eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000414() {
    final XQuery query = new XQuery(
      "\"string\" ne 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000415() {
    final XQuery query = new XQuery(
      "\"string\" le 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000416() {
    final XQuery query = new XQuery(
      "\"string\" gt 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000417() {
    final XQuery query = new XQuery(
      "\"string\" ge 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000418() {
    final XQuery query = new XQuery(
      "\"string\" ne 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000419() {
    final XQuery query = new XQuery(
      "\"string\" << 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000420() {
    final XQuery query = new XQuery(
      "\"string\" >> 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000421() {
    final XQuery query = new XQuery(
      "\"string\" is 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000422() {
    final XQuery query = new XQuery(
      "\"string\" div 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000423() {
    final XQuery query = new XQuery(
      "\"string\" idiv 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000424() {
    final XQuery query = new XQuery(
      "\"string\" * 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000425() {
    final XQuery query = new XQuery(
      "\"string\" mod 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000426() {
    final XQuery query = new XQuery(
      "\"string\" - 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test type checking of binary operators .
   */
  @org.junit.Test
  public void xPTY000427() {
    final XQuery query = new XQuery(
      "\"string\" + 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000428() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type as xs:integer) { if ($type eq 1) then xs:date('2007-11-28') else 1.0 }; abs(foo:something(1))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000429() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type as xs:integer) { if ($type eq 1) then xs:date('2007-11-28') else 'foo' }; element { foo:something(1) } { }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000430() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type as xs:integer) { if ($type eq 1) then xs:date('2007-11-28') else 'foo' }; processing-instruction { foo:something(1) } { }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000431() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type as xs:integer) { if ($type eq 1) then xs:date('2007-11-28') else 'foo' }; <e> { attribute { foo:something(1) } { } } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000432() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type as xs:integer) { if ($type eq 1) then ('foo', xs:date('2007-11-28'), 'foo') else 'foo' }; let $x as xs:string* := foo:something(1) return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000433() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type as xs:integer) { if ($type eq 1) then xs:date('2007-11-28') else 'foo' }; for $x as xs:string in foo:something(1) return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000434() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type) as xs:integer { $type }; foo:something('foo')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY000435() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($type as xs:integer) as xs:integer { if ($type eq 1) then xs:date('2007-11-28') else $type }; foo:something(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad unary functions .
   */
  @org.junit.Test
  public void xPTY000437() {
    final XQuery query = new XQuery(
      "-xs:date('2007-11-29')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad unary functions .
   */
  @org.junit.Test
  public void xPTY000438() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($pos as xs:integer) { if ($pos eq 1) then 1 else xs:date('2007-11-29') }; -foo:something(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad unary functions .
   */
  @org.junit.Test
  public void xPTY000439() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($pos as xs:integer) { if ($pos eq 1) then 1 else xs:date('2007-11-29') }; +foo:something(2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad unary functions .
   */
  @org.junit.Test
  public void xPTY000440() {
    final XQuery query = new XQuery(
      "+xs:date('2007-11-29')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad input to fn:boolean. Error differs depending on type checking mode .
   */
  @org.junit.Test
  public void xPTY000441() {
    final XQuery query = new XQuery(
      "fn:boolean( (1, 2) )",
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
        error("XPTY0004")
      ||
        error("FORG0006")
      )
    );
  }

  /**
   *  test bad input to fn:string-length .
   */
  @org.junit.Test
  public void xPTY000442() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:date('2007-11-29'))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad input to fn:string-length .
   */
  @org.junit.Test
  public void xPTY000443() {
    final XQuery query = new XQuery(
      "xs:date(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad input to fn:string-length .
   */
  @org.junit.Test
  public void xPTY000444() {
    final XQuery query = new XQuery(
      "1 cast as xs:date",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test bad casts .
   */
  @org.junit.Test
  public void xPTY000445() {
    xquery10();
    final XQuery query = new XQuery(
      "concat('prefix:', 'localname') cast as xs:QName",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test runtime cardinality checks .
   */
  @org.junit.Test
  public void xPTY000446() {
    final XQuery query = new XQuery(
      "(1, 2) + 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test runtime cardinality checks .
   */
  @org.junit.Test
  public void xPTY000447() {
    final XQuery query = new XQuery(
      "declare variable $a := <e><a/><b/><a/></e>; <a>{$a/a eq 1}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  test runtime cardinality checks .
   */
  @org.junit.Test
  public void xPTY000448() {
    final XQuery query = new XQuery(
      "let $i := (1, 3, 2) order by $i return $i",
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
        assertStringValue(false, "1 3 2")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  check that last step of path expressions are reported correctly .
   */
  @org.junit.Test
  public void xPTY0018() {
    final XQuery query = new XQuery(
      "(<a/>, <b/>)/(if (position() mod 2 = 1) then position() else .)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0018")
    );
  }

  /**
   *  check that bad steps are reported correctly .
   */
  @org.junit.Test
  public void xPTY00191() {
    final XQuery query = new XQuery(
      "<a/>/1/node()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0019")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY00192() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something() { (<a />, 1, <b/>, 2) }; foo:something()/a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0019")
    );
  }

  /**
   *  test bad run-time conversions .
   */
  @org.junit.Test
  public void xPTY00193() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org\"; declare function foo:something($pos as xs:integer) { if ($pos eq 1) then 1 else <a /> }; let $x := <a><b><c/></b><b><c/></b></a> return $x/b/(foo:something(position()))/a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0019")
    );
  }

  /**
   *  check that bad context item types are reported correctly .
   */
  @org.junit.Test
  public void xPTY0020() {
    final XQuery query = new XQuery(
      "<a/>/20[text()]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0020")
    );
  }

  /**
   *  check that attributes clashes are reported correctly .
   */
  @org.junit.Test
  public void xQDY00251() {
    final XQuery query = new XQuery(
      "let $attr1 := attribute attr { 'foo' } return let $attr2 := attribute attr { 'bar' } return <a>{$attr1, $attr2 }</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0025")
    );
  }

  /**
   *  check that attributes clashes are reported correctly .
   */
  @org.junit.Test
  public void xQDY00252() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www/w3.org/\"; let $attr1 := attribute prefix:attr { 'foo' } return let $attr2 := attribute prefix:attr { 'bar' } return <a>{$attr1, $attr2 }</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0025")
    );
  }

  /**
   *  check that attributes clashes are reported correctly .
   */
  @org.junit.Test
  public void xQDY00253() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www/w3.org/\"; let $attr1 := attribute attr { 'foo' } return let $attr2 := attribute attr { 'bar' } return <prefix:a>{$attr1, $attr2 }</prefix:a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0025")
    );
  }

  /**
   *  check that attributes clashes are reported correctly .
   */
  @org.junit.Test
  public void xQDY00254() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www/w3.org/\"; let $attr1 := attribute prefix:attr { 'foo' } return let $attr2 := attribute prefix:attr { 'bar' } return <prefix:a>{$attr1, $attr2 }</prefix:a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0025")
    );
  }

  /**
   *  check that bad processing instruction content is reported correctly .
   */
  @org.junit.Test
  public void xQDY0026() {
    final XQuery query = new XQuery(
      "processing-instruction target { '?>' }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0026")
    );
  }

  /**
   *  check that bad processing instruction targets reported correctly .
   */
  @org.junit.Test
  public void xQDY0041() {
    final XQuery query = new XQuery(
      "<a> { processing-instruction { '1BadName' } { 'content' } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0041")
    );
  }

  /**
   *  check that bad attribute names are reported correctly .
   */
  @org.junit.Test
  public void xQDY00441() {
    final XQuery query = new XQuery(
      "<a> { attribute { 'xmlns' } { 'http://www.w3.org/' } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0044")
    );
  }

  /**
   *  check that bad processing instructions are reported correctly .
   */
  @org.junit.Test
  public void xQDY0064() {
    final XQuery query = new XQuery(
      "<a> { processing-instruction { 'xml' } { 'content' } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0064")
    );
  }

  /**
   *  check that bad comments are reported correctly .
   */
  @org.junit.Test
  public void xQDY0072() {
    final XQuery query = new XQuery(
      "<a> { comment { ' -- ' } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0072")
    );
  }

  /**
   *  check that bad qualified names are reported correctly .
   */
  @org.junit.Test
  public void xQDY00741() {
    final XQuery query = new XQuery(
      "<a> { element { 'prefix:localName' } { } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  check that bad qualified names are reported correctly .
   */
  @org.junit.Test
  public void xQDY00742() {
    final XQuery query = new XQuery(
      "<a> { attribute { 'prefix:localName' } { } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  check that bad qualified names are reported correctly .
   */
  @org.junit.Test
  public void xQDY00743() {
    final XQuery query = new XQuery(
      "<a> { element { '1localName' } { } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  check that bad qualified names are reported correctly .
   */
  @org.junit.Test
  public void xQDY00744() {
    final XQuery query = new XQuery(
      "<a> { attribute { '1localName' } { } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQDY0074")
    );
  }

  /**
   *  check that bad xml:id attributes are reported correctly .
   */
  @org.junit.Test
  public void xQDY0091() {
    final XQuery query = new XQuery(
      "<e xml:id=\" ab c d \"/>",
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
        error("XQDY0091")
      ||
        assertSerialization("<e xml:id=\"ab c d\"/>", false)
      )
    );
  }

  /**
   *  check construction of xml:space attribute with a value other than preserve or default. .
   */
  @org.junit.Test
  public void xQDY0092() {
    final XQuery query = new XQuery(
      "<a xml:space=\"space\"/>",
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
        error("XQDY0092")
      ||
        assertSerialization("<a xml:space=\"space\"/>", false)
      )
    );
  }

  /**
   *  check that bad context item types are reported correctly .
   */
  @org.junit.Test
  public void xQST00221() {
    final XQuery query = new XQuery(
      "<a xmlns=\"{1}\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  check that bad context item types are reported correctly .
   */
  @org.junit.Test
  public void xQST00222() {
    final XQuery query = new XQuery(
      "<a xmlns:prefix=\"{1}\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  check that invalid encodings are reported correctly .
   */
  @org.junit.Test
  public void xQST0031() {
    final XQuery query = new XQuery(
      "xquery version '2.0'; 1+2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0031")
    );
  }

  /**
   *  check that invalid prolog declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0032() {
    final XQuery query = new XQuery(
      "declare base-uri \"http://www.example.org/A\"; declare base-uri \"http://www.example.org/B\"; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0032")
    );
  }

  /**
   *  check that invalid namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0033() {
    final XQuery query = new XQuery(
      "declare namespace cheddar = 'http://www.example.org/cheddar'; declare namespace cheddar = 'http://www.example.org/cheddar'; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0033")
    );
  }

  /**
   *  check that function name clashes are reported correctly .
   */
  @org.junit.Test
  public void xQST0034() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www.example.org/\"; declare function prefix:foo() { 1 }; declare function prefix:foo() { 1 }; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0034")
    );
  }

  /**
   *  check that invalid prolog declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00381() {
    final XQuery query = new XQuery(
      "declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; declare default collation \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0038")
    );
  }

  /**
   *  check that invalid prolog declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00383() {
    final XQuery query = new XQuery(
      "declare default collation \"http://www.example.org/\"; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0038")
    );
  }

  /**
   *  check that bad function parameter declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0039() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www.w3.org/\"; declare function prefix:foo($arg, $arg) { 1 }; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0039")
    );
  }

  /**
   *  check that duplicate attributes are reported correctly .
   */
  @org.junit.Test
  public void xQST0040() {
    final XQuery query = new XQuery(
      "<a attr=\"a\" attr=\"a\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0040")
    );
  }

  /**
   *  check that bad function declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00451() {
    final XQuery query = new XQuery(
      "declare function foo() { 1 }; foo()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0045")
    );
  }

  /**
   *  check that bad function declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00452() {
    final XQuery query = new XQuery(
      "declare function xml:foo() { 1 }; xml:foo()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0045")
    );
  }

  /**
   *  check that bad function declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00453() {
    final XQuery query = new XQuery(
      "declare function xs:foo() { 1 }; xs:foo()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0045")
    );
  }

  /**
   *  check that bad function declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00454() {
    final XQuery query = new XQuery(
      "declare function xsi:foo() { 1 }; xsi:foo()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0045")
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004601() {
    final XQuery query = new XQuery(
      "base-uri(<a xml:base=\"%gg\" />)",
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
        error("XQST0046")
      ||
        error("FORG0001")
      ||
        assertStringValue(false, "%gg")
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004602() {
    final XQuery query = new XQuery(
      "import module \"http://www.example.org/test\"; true()",
      ctx);
    try {
      query.addModule("http://www.example.org/test", file("misc/CombinedErrorCodes/XQST0046_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0046")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004603() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"%gg\"; true()",
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
        error("XQST0046")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004604() {
    final XQuery query = new XQuery(
      "declare default element namespace \"%gg\"; true()",
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
        error("XQST0046")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004605() {
    final XQuery query = new XQuery(
      "declare default function namespace \"%gg\"; fn:true()",
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
        error("XQST0046")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004606() {
    final XQuery query = new XQuery(
      "declare default collation \"%gg\"; fn:true()",
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
        error("XQST0046")
      ||
        error("XQST0038")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004607() {
    final XQuery query = new XQuery(
      "declare base-uri \"%gg\"; true()",
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
        error("XQST0046")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004608() {
    final XQuery query = new XQuery(
      "import schema \"%gg\" at \"http://www.w3.org/\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0046")
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004609() {
    final XQuery query = new XQuery(
      "import schema \"http://www.w3.org/\" at \"%gg\"; 1",
      ctx);
    try {
      query.addModule("http://www.example.org/test", file("misc/CombinedErrorCodes/XQST0046_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0046")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004610() {
    final XQuery query = new XQuery(
      "import module \"%gg\"; true()",
      ctx);
    try {
      query.addModule("http://www.example.org/test", file("misc/CombinedErrorCodes/XQST0046_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0046")
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004611() {
    final XQuery query = new XQuery(
      "import module \"http://www.w3.org/\" at \"%gg\"; 1",
      ctx);
    try {
      query.addModule("http://www.example.org/test", file("misc/CombinedErrorCodes/XQST0046_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XQST0046")
      ||
        error("XQST0059")
      )
    );
  }

  /**
   *  check that invalid collation URIs are reported correctly .
   */
  @org.junit.Test
  public void xQST004612() {
    final XQuery query = new XQuery(
      "for $x in (\"a\", \"a\", \"a\") order by $x collation \"%gg\" return $x",
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
        error("XQST0046")
      ||
        error("XQST0038")
      ||
        assertStringValue(false, "aaa")
      )
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004613() {
    final XQuery query = new XQuery(
      "<a xmlns=\"%gg\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0046")
    );
  }

  /**
   *  check that bad namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST004614() {
    final XQuery query = new XQuery(
      "<a xmlns:foo=\"%gg\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0046")
    );
  }

  /**
   *  check that bad module imports are reported correctly .
   */
  @org.junit.Test
  public void xQST0047() {
    final XQuery query = new XQuery(
      "import module \"http://www.example.org/foo\"; import module \"http://www.example.org/foo\"; 1",
      ctx);
    try {
      query.addModule("http://www.example.org/foo", file("misc/CombinedErrorCodes/XQST0047_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0047")
    );
  }

  /**
   *  check that bad declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0048() {
    final XQuery query = new XQuery(
      "import module namespace foo = \"http://www.example.org/foo\"; 1",
      ctx);
    try {
      query.addModule("http://www.example.org/foo", file("misc/CombinedErrorCodes/XQST0048_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0048")
    );
  }

  /**
   *  check that bad variable declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0049() {
    final XQuery query = new XQuery(
      "declare variable $foo external; declare variable $foo external; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0049")
    );
  }

  /**
   *  check that cyclic variable definitions are reported correctly .
   */
  @org.junit.Test
  public void xQST0054() {
    xquery10();
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://www.example.org/\"; declare variable $a := foo:bar(); declare function foo:bar() { $a + 1 }; $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0054")
    );
  }

  /**
   *  check that cyclic variable definitions are reported correctly .
   */
  @org.junit.Test
  public void xQST0055() {
    final XQuery query = new XQuery(
      "declare copy-namespaces preserve,inherit; declare copy-namespaces preserve,no-inherit; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0055")
    );
  }

  /**
   *  check that bad schema imports are reported correctly .
   */
  @org.junit.Test
  public void xQST0057() {
    final XQuery query = new XQuery(
      "import schema namespace foo = \"\" at \"http://www.w3.org/\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0057")
    );
  }

  /**
   *  check that bad schema imports are reported correctly .
   */
  @org.junit.Test
  public void xQST0058() {
    final XQuery query = new XQuery(
      "import schema namespace foo = \"http://www.w3.org/XQueryTest/testcases\"; import schema namespace bar = \"http://www.w3.org/XQueryTest/testcases\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0058")
    );
  }

  /**
   *  check that missing schema imports are reported correctly .
   */
  @org.junit.Test
  public void xQST00591() {
    final XQuery query = new XQuery(
      "import schema namespace foo = \"http://www.w3.org/\" at \"DoesNotExist.xsd\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0059")
    );
  }

  /**
   *  check that missing module imports are reported correctly .
   */
  @org.junit.Test
  public void xQST00592() {
    final XQuery query = new XQuery(
      "import module namespace foo = \"http://www.w3.org/\" at \"DoesNotExist.xq\"; 1",
      ctx);
    try {
      query.addModule("http://www.example.org", file("misc/CombinedErrorCodes/XQST0059_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0059")
    );
  }

  /**
   *  check that missing schema imports are reported correctly .
   */
  @org.junit.Test
  public void xQST00593() {
    final XQuery query = new XQuery(
      "import schema namespace foo = \"http://www.w3.org/\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0059")
    );
  }

  /**
   *  check that missing module imports are reported correctly .
   */
  @org.junit.Test
  public void xQST00594() {
    final XQuery query = new XQuery(
      "import module namespace foo = \"http://www.w3.org/\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0059")
    );
  }

  /**
   *  Test generating XQST0059 .
   */
  @org.junit.Test
  public void xQST00595() {
    final XQuery query = new XQuery(
      "import module namespace foo = \"http://www.example.org/\"; foo:bar()",
      ctx);
    try {
      query.addModule("http://www.example.org", file("misc/CombinedErrorCodes/XQST0059_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0059")
    );
  }

  /**
   *  Test generating XQST0059 .
   */
  @org.junit.Test
  public void xQST00596() {
    final XQuery query = new XQuery(
      "import schema namespace foo = \"http://www.example.org/\" at \"XQST0059.xsd\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0059")
    );
  }

  /**
   *  check that bad context item types are reported correctly .
   */
  @org.junit.Test
  public void xQST0060() {
    final XQuery query = new XQuery(
      "declare default function namespace \"\"; declare function foo() { 1 }; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0060")
    );
  }

  /**
   *  check that bad context item types are reported correctly .
   */
  @org.junit.Test
  public void xQST0065() {
    final XQuery query = new XQuery(
      "declare ordering unordered; declare ordering ordered; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0065")
    );
  }

  /**
   *  check that repeated element namespace declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00661() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.w3.org/a\"; declare default element namespace \"http://www.w3.org/b\"; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0066")
    );
  }

  /**
   *  check that invalid prolog declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00663() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://www.example.org/\"; declare default function namespace \"http://www.w3.org/2005/xpath-functions/collation/codepoint\"; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0066")
    );
  }

  /**
   *  check that duplicate declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0067() {
    final XQuery query = new XQuery(
      "declare construction strip; declare construction preserve; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0067")
    );
  }

  /**
   *  check that duplicate declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0068() {
    final XQuery query = new XQuery(
      "declare boundary-space strip; declare boundary-space preserve; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0068")
    );
  }

  /**
   *  check that duplicate declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0069() {
    final XQuery query = new XQuery(
      "declare default order empty least; declare default order empty greatest; 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0069")
    );
  }

  /**
   *  check that invalid prolog declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00701() {
    final XQuery query = new XQuery(
      "import schema namespace xml = \"http://www.example.org/\"; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  check that invalid prolog declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00702() {
    final XQuery query = new XQuery(
      "import module namespace xml = \"http://www.example.org/\"; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  check that invalid module declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00703() {
    final XQuery query = new XQuery(
      "import module namespace xml = 'http://www.example.org/'; 1 + 2",
      ctx);
    try {
      query.addModule("http://www.example.org/foo", file("misc/CombinedErrorCodes/XQST0070_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  check that invalid prolog declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00704() {
    final XQuery query = new XQuery(
      "declare namespace xml = \"http://www.example.org/\"; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  check that duplicate namespaces are reported correctly .
   */
  @org.junit.Test
  public void xQST00711() {
    final XQuery query = new XQuery(
      "<a xmlns:prefix=\"http://www.w3.org/\" xmlns:prefix=\"http://www.w3.org/\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0071")
    );
  }

  /**
   *  check that duplicate namespaces are reported correctly .
   */
  @org.junit.Test
  public void xQST00712() {
    final XQuery query = new XQuery(
      "<a xmlns=\"http://www.w3.org/\" xmlns=\"http://www.w3.org/\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0071")
    );
  }

  /**
   *  check that invalid collations are reported correctly .
   */
  @org.junit.Test
  public void xQST0076() {
    final XQuery query = new XQuery(
      "for $x in ('a', 'b', 'c') order by $x collation 'http://www.w3.org/' return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0076")
    );
  }

  /**
   *  check that unrecognised pragma is reported correctly .
   */
  @org.junit.Test
  public void xQST0079() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www.w3.org/\"; (# prefix:pragma #) { }",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0079")
    );
  }

  /**
   *  check that XML 1.1 namespace undeclarations are reported correctly .
   */
  @org.junit.Test
  public void xQST0085() {
    final XQuery query = new XQuery(
      "<element xmlns:foo=\"http://www.w3.org/\"> <element xmlns:foo=\"\" /> </element>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0085")
    );
  }

  /**
   *  check that invalid encodings are reported correctly .
   */
  @org.junit.Test
  public void xQST0087() {
    final XQuery query = new XQuery(
      "xquery version '1.0' encoding '_utf'; 1+2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0087")
    );
  }

  /**
   *  check that invalid module imports are reported correctly .
   */
  @org.junit.Test
  public void xQST00881() {
    final XQuery query = new XQuery(
      "import module namespace cheese = ''; 1 + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0088")
    );
  }

  /**
   *  check that invalid module declarations are reported correctly .
   */
  @org.junit.Test
  public void xQST00882() {
    final XQuery query = new XQuery(
      "import module \"http://www.example.org/test\"; 1",
      ctx);
    try {
      query.addModule("http://www.example.org/test", file("misc/CombinedErrorCodes/XQST0088_lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0088")
    );
  }

  /**
   *  check that bad for/at expressions are reported correctly .
   */
  @org.junit.Test
  public void xQST0089() {
    final XQuery query = new XQuery(
      "for $x at $x in (1, 2, 3) return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0089")
    );
  }

  /**
   *  check that bad character references are reported correctly .
   */
  @org.junit.Test
  public void xQST0090() {
    final XQuery query = new XQuery(
      "<bad-character-reference>&#xa999999999999999a;</bad-character-reference>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0090")
    );
  }

  /**
   *  Test generating XQST0093 .
   */
  @org.junit.Test
  public void xQST0093() {
    xquery10();
    final XQuery query = new XQuery(
      "import module namespace foo=\"http://www.example.org/foo\"; $foo:variable2",
      ctx);
    try {
      query.addModule("http://www.example.org/foo", file("misc/CombinedErrorCodes/XQST0093_lib2.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0093")
    );
  }

  /**
   *  Test generating XQST0093 (no longer an error in XQuery 3.0).
   */
  @org.junit.Test
  public void xQST0093a() {
    final XQuery query = new XQuery(
      "\n" +
      "      \timport module namespace foo=\"http://www.example.org/foo\"; \n" +
      "      \t$foo:variable2\n" +
      "      ",
      ctx);
    try {
      query.addModule("http://www.example.org/foo", file("misc/CombinedErrorCodes/XQST0093_lib2.xq"));
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
   * It is a static error if an inline function expression is annotated as %public..
   */
  @org.junit.Test
  public void xQST01251() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := %public function($arg as xs:integer) as xs:integer \n" +
      "                          { $arg + 1 }\n" +
      "        return $f(1)\n" +
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
      error("XQST0125")
    );
  }

  /**
   * It is a static error if an inline function expression is annotated as %private..
   */
  @org.junit.Test
  public void xQST01252() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $f := %private function($arg as xs:integer) as xs:integer \n" +
      "                           { $arg + 1 }\n" +
      "        return $f(1)\n" +
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
      error("XQST0125")
    );
  }

  /**
   *  check that bad element content is reported correctly .
   */
  @org.junit.Test
  public void xQTY00241() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www.w3.org/\"; <prefix:a> { <b />, attribute prefix:foo { 'bar' } } </prefix:a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQTY0024")
    );
  }

  /**
   *  check that bad element content is reported correctly .
   */
  @org.junit.Test
  public void xQTY00242() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www.w3.org/\"; <prefix:a> { <b />, attribute foo { 'bar' } } </prefix:a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQTY0024")
    );
  }

  /**
   *  check that bad element content is reported correctly .
   */
  @org.junit.Test
  public void xQTY00243() {
    final XQuery query = new XQuery(
      "declare namespace prefix = \"http://www.w3.org/\"; <a> { <b />, attribute prefix:foo { 'bar' } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQTY0024")
    );
  }

  /**
   *  check that bad element content is reported correctly .
   */
  @org.junit.Test
  public void xQTY00244() {
    final XQuery query = new XQuery(
      "<a> { <b />, attribute foo { 'bar' } } </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQTY0024")
    );
  }

  /**
   *  Evaluates simple module import to generate error code if feature not supported. .
   */
  @org.junit.Test
  public void combinedErrors1() {
    final XQuery query = new XQuery(
      "import module namespace defs=\"http://www.w3.org/TestModules/defs\"; \"ABC\"",
      ctx);
    try {
      query.addModule("http://www.w3.org/TestModules/test1", file("misc/CombinedErrorCodes/test1-lib.xq"));
      query.addModule("http://www.w3.org/TestModules/defs", file("misc/CombinedErrorCodes/moduleDefs-lib.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "ABC")
      ||
        error("XQST0016")
      )
    );
  }

  /**
   *  Evaluates simple full axis feature (preceding axis) to generate error code if feature not supported. .
   */
  @org.junit.Test
  public void combinedErrors4() {
    final XQuery query = new XQuery(
      "/works[1]/employee[2]/preceding::employee",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
      ||
        error("XPST0010")
      )
    );
  }
}
