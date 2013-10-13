package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CastableExpr production.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCastableExpr extends QT3TestSet {

  /**
   * Try xs:untypedAtomic(INF) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs001() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"INF\") castable as xs:untypedAtomic",
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
   * Try xs:untypedAtomic(0.0E0) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs002() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0.0E0\") castable as xs:untypedAtomic",
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
   * Try xs:untypedAtomic(true) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs003() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:untypedAtomic",
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
   * Try xs:untypedAtomic(INF) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs004() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"INF\") castable as xs:string",
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
   * Try xs:untypedAtomic(0.0E0) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs005() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0.0E0\") castable as xs:string",
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
   * Try xs:untypedAtomic(true) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs006() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:string",
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
   * Try xs:untypedAtomic(-0.0E0) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs007() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-0.0E0\") castable as xs:float",
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
   * Try xs:untypedAtomic(NaN) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs008() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"NaN\") castable as xs:float",
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
   * Try xs:untypedAtomic(1e-5) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs009() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1e-5\") castable as xs:float",
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
   * Try xs:untypedAtomic(-10000000) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs010() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-10000000\") castable as xs:float",
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
   * Try xs:untypedAtomic(true) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs011() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:float",
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
   * Try xs:untypedAtomic(-0.0E0) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs012() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-0.0E0\") castable as xs:double",
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
   * Try xs:untypedAtomic(NaN) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs013() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"NaN\") castable as xs:double",
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
   * Try xs:untypedAtomic(1e-5) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs014() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1e-5\") castable as xs:double",
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
   * Try xs:untypedAtomic(-10000000) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs015() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-10000000\") castable as xs:double",
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
   * Try xs:untypedAtomic(true) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs016() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:double",
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
   * Try xs:untypedAtomic(-0.0E0) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs017() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-0.0E0\") castable as xs:decimal",
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
   * Try xs:untypedAtomic(NaN) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs018() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"NaN\") castable as xs:decimal",
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
   * Try xs:untypedAtomic(1e-5) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs019() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1e-5\") castable as xs:decimal",
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
   * Try xs:untypedAtomic(5.5432) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs020() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"5.5432\") castable as xs:decimal",
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
   * Try xs:untypedAtomic(true) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs021() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:decimal",
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
   * Try xs:untypedAtomic(-0.0E0) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs022() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-0.0E0\") castable as xs:integer",
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
   * Try xs:untypedAtomic(NaN) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs023() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"NaN\") castable as xs:integer",
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
   * Try xs:untypedAtomic(1e-5) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs024() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1e-5\") castable as xs:integer",
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
   * Try xs:untypedAtomic(-1.1234) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs025() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-1.1234\") castable as xs:integer",
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
   * Try xs:untypedAtomic(true) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs026() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:integer",
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
   * Try xs:untypedAtomic(P1Y2M3DT10H30M23S) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs027() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"P1Y2M3DT10H30M23S\") castable as xs:duration",
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
   * Try xs:untypedAtomic(-P1Y1M1DT1H1M1.123S) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs028() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-P1Y1M1DT1H1M1.123S\") castable as xs:duration",
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
   * Try xs:untypedAtomic(true) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs029() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:duration",
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
   * Try xs:untypedAtomic(-P1Y1M1DT1H1M1.123S) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs030() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-P1Y1M1DT1H1M1.123S\") castable as xs:yearMonthDuration",
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
   * Try xs:untypedAtomic(P24M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs031() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"P24M\") castable as xs:yearMonthDuration",
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
   * Try xs:untypedAtomic(-P21M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs032() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-P21M\") castable as xs:yearMonthDuration",
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
   * Try xs:untypedAtomic(true) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs033() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:yearMonthDuration",
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
   * Try xs:untypedAtomic(-P1Y1M1DT1H1M1.123S) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs034() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-P1Y1M1DT1H1M1.123S\") castable as xs:dayTimeDuration",
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
   * Try xs:untypedAtomic(P3DT10H30M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs035() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"P3DT10H30M\") castable as xs:dayTimeDuration",
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
   * Try xs:untypedAtomic(-PT100M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs036() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-PT100M\") castable as xs:dayTimeDuration",
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
   * Try xs:untypedAtomic(true) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs037() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:dayTimeDuration",
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
   * Try xs:untypedAtomic(1999-05-31T13:20:00) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs038() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1999-05-31T13:20:00\") castable as xs:dateTime",
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
   * Try xs:untypedAtomic(-1999-05-31T13:20:00+14:00) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs039() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-1999-05-31T13:20:00+14:00\") castable as xs:dateTime",
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
   * Try xs:untypedAtomic(2000-01-16T00:00:00Z) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs040() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"2000-01-16T00:00:00Z\") castable as xs:dateTime",
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
   * Try xs:untypedAtomic(true) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs041() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:dateTime",
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
   * Try xs:untypedAtomic(13:20:00-05:00) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs042() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"13:20:00-05:00\") castable as xs:time",
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
   * Try xs:untypedAtomic(13:20:02.123) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs043() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"13:20:02.123\") castable as xs:time",
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
   * Try xs:untypedAtomic(13:20:00Z) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs044() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"13:20:00Z\") castable as xs:time",
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
   * Try xs:untypedAtomic(true) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs045() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:time",
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
   * Try xs:untypedAtomic(1999-05-31) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs046() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1999-05-31\") castable as xs:date",
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
   * Try xs:untypedAtomic(-0012-12-03-05:00) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs047() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-0012-12-03-05:00\") castable as xs:date",
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
   * Try xs:untypedAtomic(1999-05-31Z) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs048() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1999-05-31Z\") castable as xs:date",
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
   * Try xs:untypedAtomic(true) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs049() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:date",
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
   * Try xs:untypedAtomic(1999-05) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs050() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1999-05\") castable as xs:gYearMonth",
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
   * Try xs:untypedAtomic(-0012-12-05:00) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs051() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-0012-12-05:00\") castable as xs:gYearMonth",
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
   * Try xs:untypedAtomic(1999-05Z) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs052() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1999-05Z\") castable as xs:gYearMonth",
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
   * Try xs:untypedAtomic(true) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs053() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:gYearMonth",
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
   * Try xs:untypedAtomic(1999) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs054() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1999\") castable as xs:gYear",
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
   * Try xs:untypedAtomic(-0012-05:00) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs055() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"-0012-05:00\") castable as xs:gYear",
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
   * Try xs:untypedAtomic(1999Z) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs056() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"1999Z\") castable as xs:gYear",
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
   * Try xs:untypedAtomic(true) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs057() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:gYear",
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
   * Try xs:untypedAtomic(--05-31) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs058() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"--05-31\") castable as xs:gMonthDay",
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
   * Try xs:untypedAtomic(--05-31+14:00) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs059() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"--05-31+14:00\") castable as xs:gMonthDay",
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
   * Try xs:untypedAtomic(--05-31Z) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs060() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"--05-31Z\") castable as xs:gMonthDay",
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
   * Try xs:untypedAtomic(true) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs061() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:gMonthDay",
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
   * Try xs:untypedAtomic(---31) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs062() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"---31\") castable as xs:gDay",
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
   * Try xs:untypedAtomic(---03-05:00) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs063() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"---03-05:00\") castable as xs:gDay",
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
   * Try xs:untypedAtomic(---31Z) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs064() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"---31Z\") castable as xs:gDay",
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
   * Try xs:untypedAtomic(true) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs065() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:gDay",
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
   * Try xs:untypedAtomic(--05) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs066() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"--05\") castable as xs:gMonth",
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
   * Try xs:untypedAtomic(--12-05:00) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs067() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"--12-05:00\") castable as xs:gMonth",
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
   * Try xs:untypedAtomic(--05Z) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs068() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"--05Z\") castable as xs:gMonth",
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
   * Try xs:untypedAtomic(true) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs069() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:gMonth",
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
   * Try xs:untypedAtomic(0.0) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs070() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0.0\") castable as xs:boolean",
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
   * Try xs:untypedAtomic(0) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs071() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0\") castable as xs:boolean",
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
   * Try xs:untypedAtomic(true) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs072() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:boolean",
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
   * Try xs:untypedAtomic(true) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs073() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:base64Binary",
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
   * Try xs:untypedAtomic(00000000) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs074() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"00000000\") castable as xs:base64Binary",
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
   * Try xs:untypedAtomic(D74D35D35D35) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs075() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"D74D35D35D35\") castable as xs:base64Binary",
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
   * Try xs:untypedAtomic(true) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs076() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:hexBinary",
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
   * Try xs:untypedAtomic(010010101) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs077() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"010010101\") castable as xs:hexBinary",
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
   * Try xs:untypedAtomic(0fb7) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs078() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"0fb7\") castable as xs:hexBinary",
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
   * Try xs:untypedAtomic(D74D35D35D35) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs079() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"D74D35D35D35\") castable as xs:hexBinary",
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
   * Try xs:untypedAtomic(-0012-05:00) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs080() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"http://www.example.com/~b%C3%A9b%C3%A9\") castable as xs:anyURI",
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
   * Try xs:untypedAtomic(true) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs081() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"true\") castable as xs:anyURI",
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
   * Try xs:untypedAtomic(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs082() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:anyURI",
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
   * Try xs:string(INF) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs083() {
    final XQuery query = new XQuery(
      "xs:string(\"INF\") castable as xs:untypedAtomic",
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
   * Try xs:string(0.0E0) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs084() {
    final XQuery query = new XQuery(
      "xs:string(\"0.0E0\") castable as xs:untypedAtomic",
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
   * Try xs:string(true) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs085() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:untypedAtomic",
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
   * Try xs:string(INF) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs086() {
    final XQuery query = new XQuery(
      "xs:string(\"INF\") castable as xs:string",
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
   * Try xs:string(0.0E0) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs087() {
    final XQuery query = new XQuery(
      "xs:string(\"0.0E0\") castable as xs:string",
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
   * Try xs:string(true) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs088() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:string",
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
   * Try xs:string(-0.0E0) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs089() {
    final XQuery query = new XQuery(
      "xs:string(\"-0.0E0\") castable as xs:float",
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
   * Try xs:string(NaN) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs090() {
    final XQuery query = new XQuery(
      "xs:string(\"NaN\") castable as xs:float",
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
   * Try xs:string(1e-5) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs091() {
    final XQuery query = new XQuery(
      "xs:string(\"1e-5\") castable as xs:float",
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
   * Try xs:string(-10000000) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs092() {
    final XQuery query = new XQuery(
      "xs:string(\"-10000000\") castable as xs:float",
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
   * Try xs:string(true) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs093() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:float",
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
   * Try xs:string(-0.0E0) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs094() {
    final XQuery query = new XQuery(
      "xs:string(\"-0.0E0\") castable as xs:double",
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
   * Try xs:string(NaN) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs095() {
    final XQuery query = new XQuery(
      "xs:string(\"NaN\") castable as xs:double",
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
   * Try xs:string(1e-5) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs096() {
    final XQuery query = new XQuery(
      "xs:string(\"1e-5\") castable as xs:double",
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
   * Try xs:string(-10000000) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs097() {
    final XQuery query = new XQuery(
      "xs:string(\"-10000000\") castable as xs:double",
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
   * Try xs:string(true) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs098() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:double",
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
   * Try xs:string(-0.0E0) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs099() {
    final XQuery query = new XQuery(
      "xs:string(\"-0.0E0\") castable as xs:decimal",
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
   * Try xs:string(NaN) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs100() {
    final XQuery query = new XQuery(
      "xs:string(\"NaN\") castable as xs:decimal",
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
   * Try xs:string(1e-5) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs101() {
    final XQuery query = new XQuery(
      "xs:string(\"1e-5\") castable as xs:decimal",
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
   * Try xs:string(5.5432) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs102() {
    final XQuery query = new XQuery(
      "xs:string(\"5.5432\") castable as xs:decimal",
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
   * Try xs:string(true) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs103() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:decimal",
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
   * Try xs:string(-0.0E0) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs104() {
    final XQuery query = new XQuery(
      "xs:string(\"-0.0E0\") castable as xs:integer",
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
   * Try xs:string(NaN) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs105() {
    final XQuery query = new XQuery(
      "xs:string(\"NaN\") castable as xs:integer",
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
   * Try xs:string(1e-5) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs106() {
    final XQuery query = new XQuery(
      "xs:string(\"1e-5\") castable as xs:integer",
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
   * Try xs:string(-1.1234) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs107() {
    final XQuery query = new XQuery(
      "xs:string(\"-1.1234\") castable as xs:integer",
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
   * Try xs:string(true) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs108() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:integer",
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
   * Try xs:string(P1Y2M3DT10H30M23S) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs109() {
    final XQuery query = new XQuery(
      "xs:string(\"P1Y2M3DT10H30M23S\") castable as xs:duration",
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
   * Try xs:string(-P1Y1M1DT1H1M1.123S) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs110() {
    final XQuery query = new XQuery(
      "xs:string(\"-P1Y1M1DT1H1M1.123S\") castable as xs:duration",
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
   * Try xs:string(true) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs111() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:duration",
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
   * Try xs:string(-P1Y1M1DT1H1M1.123S) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs112() {
    final XQuery query = new XQuery(
      "xs:string(\"-P1Y1M1DT1H1M1.123S\") castable as xs:yearMonthDuration",
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
   * Try xs:string(P24M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs113() {
    final XQuery query = new XQuery(
      "xs:string(\"P24M\") castable as xs:yearMonthDuration",
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
   * Try xs:string(-P21M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs114() {
    final XQuery query = new XQuery(
      "xs:string(\"-P21M\") castable as xs:yearMonthDuration",
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
   * Try xs:string(true) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs115() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:yearMonthDuration",
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
   * Try xs:string(-P1Y1M1DT1H1M1.123S) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs116() {
    final XQuery query = new XQuery(
      "xs:string(\"-P1Y1M1DT1H1M1.123S\") castable as xs:dayTimeDuration",
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
   * Try xs:string(P3DT10H30M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs117() {
    final XQuery query = new XQuery(
      "xs:string(\"P3DT10H30M\") castable as xs:dayTimeDuration",
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
   * Try xs:string(-PT100M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs118() {
    final XQuery query = new XQuery(
      "xs:string(\"-PT100M\") castable as xs:dayTimeDuration",
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
   * Try xs:string(true) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs119() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:dayTimeDuration",
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
   * Try xs:string(1999-05-31T13:20:00) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs120() {
    final XQuery query = new XQuery(
      "xs:string(\"1999-05-31T13:20:00\") castable as xs:dateTime",
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
   * Try xs:string(-1999-05-31T13:20:00+14:00) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs121() {
    final XQuery query = new XQuery(
      "xs:string(\"-1999-05-31T13:20:00+14:00\") castable as xs:dateTime",
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
   * Try xs:string(2000-01-16T00:00:00Z) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs122() {
    final XQuery query = new XQuery(
      "xs:string(\"2000-01-16T00:00:00Z\") castable as xs:dateTime",
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
   * Try xs:string(true) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs123() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:dateTime",
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
   * Try xs:string(13:20:00-05:00) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs124() {
    final XQuery query = new XQuery(
      "xs:string(\"13:20:00-05:00\") castable as xs:time",
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
   * Try xs:string(13:20:02.123) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs125() {
    final XQuery query = new XQuery(
      "xs:string(\"13:20:02.123\") castable as xs:time",
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
   * Try xs:string(13:20:00Z) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs126() {
    final XQuery query = new XQuery(
      "xs:string(\"13:20:00Z\") castable as xs:time",
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
   * Try xs:string(true) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs127() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:time",
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
   * Try xs:string(1999-05-31) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs128() {
    final XQuery query = new XQuery(
      "xs:string(\"1999-05-31\") castable as xs:date",
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
   * Try xs:string(-0012-12-03-05:00) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs129() {
    final XQuery query = new XQuery(
      "xs:string(\"-0012-12-03-05:00\") castable as xs:date",
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
   * Try xs:string(1999-05-31Z) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs130() {
    final XQuery query = new XQuery(
      "xs:string(\"1999-05-31Z\") castable as xs:date",
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
   * Try xs:string(true) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs131() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:date",
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
   * Try xs:string(1999-05) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs132() {
    final XQuery query = new XQuery(
      "xs:string(\"1999-05\") castable as xs:gYearMonth",
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
   * Try xs:string(-0012-12-05:00) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs133() {
    final XQuery query = new XQuery(
      "xs:string(\"-0012-12-05:00\") castable as xs:gYearMonth",
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
   * Try xs:string(1999-05Z) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs134() {
    final XQuery query = new XQuery(
      "xs:string(\"1999-05Z\") castable as xs:gYearMonth",
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
   * Try xs:string(true) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs135() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:gYearMonth",
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
   * Try xs:string(1999) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs136() {
    final XQuery query = new XQuery(
      "xs:string(\"1999\") castable as xs:gYear",
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
   * Try xs:string(-0012-05:00) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs137() {
    final XQuery query = new XQuery(
      "xs:string(\"-0012-05:00\") castable as xs:gYear",
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
   * Try xs:string(1999Z) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs138() {
    final XQuery query = new XQuery(
      "xs:string(\"1999Z\") castable as xs:gYear",
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
   * Try xs:string(true) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs139() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:gYear",
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
   * Try xs:string(--05-31) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs140() {
    final XQuery query = new XQuery(
      "xs:string(\"--05-31\") castable as xs:gMonthDay",
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
   * Try xs:string(--05-31+14:00) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs141() {
    final XQuery query = new XQuery(
      "xs:string(\"--05-31+14:00\") castable as xs:gMonthDay",
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
   * Try xs:string(--05-31Z) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs142() {
    final XQuery query = new XQuery(
      "xs:string(\"--05-31Z\") castable as xs:gMonthDay",
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
   * Try xs:string(true) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs143() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:gMonthDay",
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
   * Try xs:string(---31) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs144() {
    final XQuery query = new XQuery(
      "xs:string(\"---31\") castable as xs:gDay",
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
   * Try xs:string(---03-05:00) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs145() {
    final XQuery query = new XQuery(
      "xs:string(\"---03-05:00\") castable as xs:gDay",
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
   * Try xs:string(---31Z) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs146() {
    final XQuery query = new XQuery(
      "xs:string(\"---31Z\") castable as xs:gDay",
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
   * Try xs:string(true) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs147() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:gDay",
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
   * Try xs:string(--05) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs148() {
    final XQuery query = new XQuery(
      "xs:string(\"--05\") castable as xs:gMonth",
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
   * Try xs:string(--12-05:00) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs149() {
    final XQuery query = new XQuery(
      "xs:string(\"--12-05:00\") castable as xs:gMonth",
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
   * Try xs:string(--05Z) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs150() {
    final XQuery query = new XQuery(
      "xs:string(\"--05Z\") castable as xs:gMonth",
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
   * Try xs:string(true) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs151() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:gMonth",
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
   * Try xs:string(0.0) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs152() {
    final XQuery query = new XQuery(
      "xs:string(\"0.0\") castable as xs:boolean",
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
   * Try xs:string(0) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs153() {
    final XQuery query = new XQuery(
      "xs:string(\"0\") castable as xs:boolean",
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
   * Try xs:string(true) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs154() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:boolean",
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
   * Try xs:string(true) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs155() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:base64Binary",
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
   * Try xs:string(00000000) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs156() {
    final XQuery query = new XQuery(
      "xs:string(\"00000000\") castable as xs:base64Binary",
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
   * Try xs:string(D74D35D35D35) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs157() {
    final XQuery query = new XQuery(
      "xs:string(\"D74D35D35D35\") castable as xs:base64Binary",
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
   * Try xs:string(true) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs158() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:hexBinary",
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
   * Try xs:string(010010101) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs159() {
    final XQuery query = new XQuery(
      "xs:string(\"010010101\") castable as xs:hexBinary",
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
   * Try xs:string(0fb7) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs160() {
    final XQuery query = new XQuery(
      "xs:string(\"0fb7\") castable as xs:hexBinary",
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
   * Try xs:string(D74D35D35D35) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs161() {
    final XQuery query = new XQuery(
      "xs:string(\"D74D35D35D35\") castable as xs:hexBinary",
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
   * Try xs:string(-0012-05:00) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs162() {
    final XQuery query = new XQuery(
      "xs:string(\"http://www.example.com/~b%C3%A9b%C3%A9\") castable as xs:anyURI",
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
   * Try xs:string(true) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs163() {
    final XQuery query = new XQuery(
      "xs:string(\"true\") castable as xs:anyURI",
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
   * Try xs:string(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs164() {
    final XQuery query = new XQuery(
      "xs:string(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:anyURI",
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
   * Try xs:float(1e5) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs165() {
    final XQuery query = new XQuery(
      "xs:float(\"1e5\") castable as xs:untypedAtomic",
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
   * Try xs:float(-INF) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs166() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") castable as xs:untypedAtomic",
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
   * Try xs:float(-0.0E0) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs167() {
    final XQuery query = new XQuery(
      "xs:float(\"-0.0E0\") castable as xs:string",
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
   * Try xs:float(NaN) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs168() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") castable as xs:string",
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
   * Try xs:float(5.4321E-100) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs169() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:float",
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
   * Try xs:float(-1.75e-3) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs170() {
    final XQuery query = new XQuery(
      "xs:float(\"-1.75e-3\") castable as xs:float",
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
   * Try xs:float(INF) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs171() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") castable as xs:float",
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
   * Try xs:float(-0.0E0) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs172() {
    final XQuery query = new XQuery(
      "xs:float(\"-0.0E0\") castable as xs:double",
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
   * Try xs:float(-INF) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs173() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") castable as xs:double",
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
   * Try xs:float(-0.0E0) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs174() {
    final XQuery query = new XQuery(
      "xs:float(\"-0.0E0\") castable as xs:decimal",
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
   * Try xs:float(5.4321E-100) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs175() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:decimal",
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
   * Try xs:float(1e5) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs176() {
    final XQuery query = new XQuery(
      "xs:float(\"1e5\") castable as xs:decimal",
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
   * Try xs:float(-1.75e-3) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs177() {
    final XQuery query = new XQuery(
      "xs:float(\"-1.75e-3\") castable as xs:decimal",
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
   * Try xs:float(-0.0E0) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs178() {
    final XQuery query = new XQuery(
      "xs:float(\"-0.0E0\") castable as xs:integer",
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
   * Try xs:float(1e5) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs179() {
    final XQuery query = new XQuery(
      "xs:float(\"1e5\") castable as xs:integer",
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
   * Try xs:float(INF) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs180() {
    final XQuery query = new XQuery(
      "xs:float(\"INF\") castable as xs:integer",
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
   * Try xs:float(-1.75e-3) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs181() {
    final XQuery query = new XQuery(
      "xs:float(\"-1.75e-3\") castable as xs:duration",
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
   * Try xs:float(5.4321E-100) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs182() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:yearMonthDuration",
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
   * Try xs:float(5.4321E-100) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs183() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:dayTimeDuration",
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
   * Try xs:float(5.4321E-100) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs184() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:dateTime",
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
   * Try xs:float(5.4321E-100) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs185() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:time",
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
   * Try xs:float(5.4321E-100) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs186() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:date",
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
   * Try xs:float(5.4321E-100) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs187() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:gYearMonth",
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
   * Try xs:float(5.4321E-100) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs188() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:gYear",
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
   * Try xs:float(5.4321E-100) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs189() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:gMonthDay",
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
   * Try xs:float(5.4321E-100) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs190() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:gDay",
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
   * Try xs:float(5.4321E-100) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs191() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:gMonth",
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
   * Try xs:float(-0.0E0) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs192() {
    final XQuery query = new XQuery(
      "xs:float(\"-0.0E0\") castable as xs:boolean",
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
   * Try xs:float(1e5) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs193() {
    final XQuery query = new XQuery(
      "xs:float(\"1e5\") castable as xs:boolean",
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
   * Try xs:float(-INF) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs194() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") castable as xs:boolean",
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
   * Try xs:float(NaN) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs195() {
    final XQuery query = new XQuery(
      "xs:float(\"NaN\") castable as xs:boolean",
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
   * Try xs:float(5.4321E-100) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs196() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:base64Binary",
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
   * Try xs:float(5.4321E-100) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs197() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:hexBinary",
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
   * Try xs:float(5.4321E-100) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs198() {
    final XQuery query = new XQuery(
      "xs:float(\"5.4321E-100\") castable as xs:anyURI",
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
   * Try xs:double(1e5) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs199() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:untypedAtomic",
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
   * Try xs:double(INF) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs200() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") castable as xs:untypedAtomic",
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
   * Try xs:double(1e8) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs201() {
    final XQuery query = new XQuery(
      "xs:double(\"1e8\") castable as xs:string",
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
   * Try xs:double(INF) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs202() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") castable as xs:string",
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
   * Try xs:double(-0.0E0) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs203() {
    final XQuery query = new XQuery(
      "xs:double(\"-0.0E0\") castable as xs:float",
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
   * Try xs:double(5.4321E-1001) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs204() {
    final XQuery query = new XQuery(
      "xs:double(\"5.4321E-1001\") castable as xs:float",
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
   * Try xs:double(1e5) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs205() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:float",
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
   * Try xs:double(-1.75e-3) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs206() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.75e-3\") castable as xs:float",
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
   * Try xs:double(NaN) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs207() {
    final XQuery query = new XQuery(
      "xs:double(\"NaN\") castable as xs:float",
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
   * Try xs:double(1e5) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs208() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:double",
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
   * Try xs:double(INF) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs209() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") castable as xs:double",
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
   * Try xs:double(-0.0E0) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs210() {
    final XQuery query = new XQuery(
      "xs:double(\"-0.0E0\") castable as xs:decimal",
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
   * Try xs:double(5.4321E-1001) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs211() {
    final XQuery query = new XQuery(
      "xs:double(\"5.4321E-1001\") castable as xs:decimal",
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
   * Try xs:double(-1.75e-3) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs212() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.75e-3\") castable as xs:decimal",
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
   * Try xs:double(INF) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs213() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") castable as xs:decimal",
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
   * Try xs:double(-0.0E0) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs214() {
    final XQuery query = new XQuery(
      "xs:double(\"-0.0E0\") castable as xs:integer",
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
   * Try xs:double(1e5) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs215() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:integer",
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
   * Try xs:double(-1.75e-3) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs216() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.75e-3\") castable as xs:integer",
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
   * Try xs:double(INF) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs217() {
    final XQuery query = new XQuery(
      "xs:double(\"INF\") castable as xs:integer",
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
   * Try xs:double(NaN) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs218() {
    final XQuery query = new XQuery(
      "xs:double(\"NaN\") castable as xs:integer",
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
   * Try xs:double(1e5) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs219() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:duration",
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
   * Try xs:double(1e5) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs220() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:yearMonthDuration",
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
   * Try xs:double(1e5) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs221() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:dayTimeDuration",
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
   * Try xs:double(1e5) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs222() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:dateTime",
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
   * Try xs:double(1e5) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs223() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:time",
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
   * Try xs:double(1e5) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs224() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:date",
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
   * Try xs:double(1e5) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs225() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:gYearMonth",
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
   * Try xs:double(1e5) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs226() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:gYear",
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
   * Try xs:double(1e5) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs227() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:gMonthDay",
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
   * Try xs:double(1e5) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs228() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:gDay",
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
   * Try xs:double(1e5) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs229() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:gMonth",
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
   * Try xs:double(1e5) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs230() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:boolean",
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
   * Try xs:double(1e5) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs231() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:base64Binary",
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
   * Try xs:double(1e5) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs232() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:hexBinary",
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
   * Try xs:double(1e5) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs233() {
    final XQuery query = new XQuery(
      "xs:double(\"1e5\") castable as xs:anyURI",
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
   * Try xs:decimal(-1.1234) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs234() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:untypedAtomic",
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
   * Try xs:decimal(-1.1234) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs235() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:string",
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
   * Try xs:decimal(-1.1234) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs236() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:float",
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
   * Try xs:decimal(-1.1234) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs237() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:double",
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
   * Try xs:decimal(-1.1234) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs238() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:decimal",
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
   * Try xs:decimal(-1.1234) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs239() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:integer",
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
   * Try xs:decimal(5.5432) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs240() {
    final XQuery query = new XQuery(
      "xs:decimal(\"5.5432\") castable as xs:integer",
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
   * Try xs:decimal(-1.1234) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs241() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:duration",
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
   * Try xs:decimal(-1.1234) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs242() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:yearMonthDuration",
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
   * Try xs:decimal(-1.1234) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs243() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:dayTimeDuration",
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
   * Try xs:decimal(-1.1234) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs244() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:dateTime",
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
   * Try xs:decimal(-1.1234) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs245() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:time",
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
   * Try xs:decimal(-1.1234) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs246() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:date",
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
   * Try xs:decimal(-1.1234) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs247() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:gYearMonth",
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
   * Try xs:decimal(-1.1234) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs248() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:gYear",
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
   * Try xs:decimal(-1.1234) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs249() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:gMonthDay",
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
   * Try xs:decimal(-1.1234) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs250() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:gDay",
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
   * Try xs:decimal(-1.1234) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs251() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:gMonth",
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
   * Try xs:decimal(-1.1234) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs252() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:boolean",
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
   * Try xs:decimal(-1.1234) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs253() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:base64Binary",
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
   * Try xs:decimal(-1.1234) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs254() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:hexBinary",
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
   * Try xs:decimal(-1.1234) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs255() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-1.1234\") castable as xs:anyURI",
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
   * Try xs:integer(1) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs256() {
    final XQuery query = new XQuery(
      "xs:integer(\"1\") castable as xs:untypedAtomic",
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
   * Try xs:integer(-100) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs257() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:string",
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
   * Try xs:integer(-100) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs258() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:float",
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
   * Try xs:integer(-100) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs259() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:double",
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
   * Try xs:integer(-100) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs260() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:decimal",
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
   * Try xs:integer(-100) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs261() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:integer",
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
   * Try xs:integer(-100) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs262() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:duration",
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
   * Try xs:integer(-100) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs263() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:yearMonthDuration",
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
   * Try xs:integer(-100) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs264() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:dayTimeDuration",
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
   * Try xs:integer(-100) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs265() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:dateTime",
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
   * Try xs:integer(-100) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs266() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:time",
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
   * Try xs:integer(-100) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs267() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:date",
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
   * Try xs:integer(-100) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs268() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:gYearMonth",
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
   * Try xs:integer(-100) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs269() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:gYear",
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
   * Try xs:integer(-100) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs270() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:gMonthDay",
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
   * Try xs:integer(-100) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs271() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:gDay",
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
   * Try xs:integer(-100) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs272() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:gMonth",
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
   * Try xs:integer(-100) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs273() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:boolean",
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
   * Try xs:integer(-100) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs274() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:base64Binary",
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
   * Try xs:integer(-100) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs275() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:hexBinary",
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
   * Try xs:integer(-100) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs276() {
    final XQuery query = new XQuery(
      "xs:integer(\"-100\") castable as xs:anyURI",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs277() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:untypedAtomic",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs278() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:string",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs279() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:float",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs280() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:double",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs281() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:decimal",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs282() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:integer",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs283() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:duration",
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
   * Try xs:duration(PT10H) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs284() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT10H\") castable as xs:duration",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs285() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:yearMonthDuration",
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
   * Try xs:duration(PT10H) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs286() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT10H\") castable as xs:yearMonthDuration",
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
   * Try xs:duration(P24M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs287() {
    final XQuery query = new XQuery(
      "xs:duration(\"P24M\") castable as xs:yearMonthDuration",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs288() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:dayTimeDuration",
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
   * Try xs:duration(PT10H) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs289() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT10H\") castable as xs:dayTimeDuration",
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
   * Try xs:duration(P24M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs290() {
    final XQuery query = new XQuery(
      "xs:duration(\"P24M\") castable as xs:dayTimeDuration",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs291() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:dateTime",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs292() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:time",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs293() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:date",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs294() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:gYearMonth",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs295() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:gYear",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs296() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:gMonthDay",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs297() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:gDay",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs298() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:gMonth",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs299() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:boolean",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs300() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:base64Binary",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs301() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:hexBinary",
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
   * Try xs:duration(P1Y2M3DT10H30M23S) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs302() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y2M3DT10H30M23S\") castable as xs:anyURI",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs303() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:untypedAtomic",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs304() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:string",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs305() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:float",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs306() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:double",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs307() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:decimal",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs308() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:integer",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs309() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:duration",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs310() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:yearMonthDuration",
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
   * Try xs:yearMonthDuration(-P21M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs311() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"-P21M\") castable as xs:yearMonthDuration",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs312() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:dayTimeDuration",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs313() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:dateTime",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs314() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:time",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs315() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:date",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs316() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:gYearMonth",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs317() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:gYear",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs318() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:gMonthDay",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs319() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:gDay",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs320() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:gMonth",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs321() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:boolean",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs322() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:base64Binary",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs323() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:hexBinary",
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
   * Try xs:yearMonthDuration(P1Y2M) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs324() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y2M\") castable as xs:anyURI",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs325() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:untypedAtomic",
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
   * Try xs:dayTimeDuration(-PT100M) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs326() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"-PT100M\") castable as xs:untypedAtomic",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs327() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:string",
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
   * Try xs:dayTimeDuration(-PT100M) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs328() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"-PT100M\") castable as xs:string",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs329() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:float",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs330() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:double",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs331() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:decimal",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs332() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:integer",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs333() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:duration",
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
   * Try xs:dayTimeDuration(PT24H) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs334() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT24H\") castable as xs:duration",
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
   * Try xs:dayTimeDuration(-PT100M) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs335() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"-PT100M\") castable as xs:duration",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs336() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:yearMonthDuration",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs337() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:dayTimeDuration",
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
   * Try xs:dayTimeDuration(P14D) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs338() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P14D\") castable as xs:dayTimeDuration",
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
   * Try xs:dayTimeDuration(-PT100M) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs339() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"-PT100M\") castable as xs:dayTimeDuration",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs340() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:dateTime",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs341() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:time",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs342() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:date",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs343() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:gYearMonth",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs344() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:gYear",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs345() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:gMonthDay",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs346() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:gDay",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs347() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:gMonth",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs348() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:boolean",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs349() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:base64Binary",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs350() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:hexBinary",
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
   * Try xs:dayTimeDuration(P3DT10H30M) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs351() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT10H30M\") castable as xs:anyURI",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs352() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:untypedAtomic",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs353() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:untypedAtomic",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs354() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:string",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs355() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:string",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs356() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:float",
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
   * Try xs:dateTime(2000-01-16T00:00:00Z) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs357() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2000-01-16T00:00:00Z\") castable as xs:float",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs358() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:decimal",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs359() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:integer",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs360() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:duration",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs361() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:yearMonthDuration",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs362() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:dayTimeDuration",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs363() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:dateTime",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs364() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:time",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs365() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:time",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs366() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:date",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs367() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:date",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs368() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:gYearMonth",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs369() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:gYearMonth",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs370() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:gYear",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs371() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:gYear",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs372() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:gMonthDay",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs373() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:gMonthDay",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs374() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:gDay",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs375() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:gDay",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs376() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:gMonth",
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
   * Try xs:dateTime(-1999-05-31T13:20:00+14:00) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs377() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-1999-05-31T13:20:00+14:00\") castable as xs:gMonth",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs378() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:boolean",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs379() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:base64Binary",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs380() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:hexBinary",
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
   * Try xs:dateTime(1999-05-31T13:20:00) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs381() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-05-31T13:20:00\") castable as xs:anyURI",
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
   * Try xs:time(13:20:00-05:00) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs382() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:untypedAtomic",
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
   * Try xs:time(13:20:00-05:00) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs383() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:string",
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
   * Try xs:time(13:20:00-05:00) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs384() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:float",
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
   * Try xs:time(13:20:00-05:00) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs385() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:double",
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
   * Try xs:time(13:20:00-05:00) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs386() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:decimal",
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
   * Try xs:time(13:20:00-05:00) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs387() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:integer",
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
   * Try xs:time(13:20:00-05:00) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs388() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:duration",
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
   * Try xs:time(13:20:00-05:00) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs389() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:yearMonthDuration",
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
   * Try xs:time(13:20:00-05:00) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs390() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:dayTimeDuration",
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
   * Try xs:time(13:20:00-05:00) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs391() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:dateTime",
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
   * Try xs:time(13:20:00-05:00) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs392() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:time",
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
   * Try xs:time(13:20:02.123) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs393() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:02.123\") castable as xs:time",
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
   * Try xs:time(13:20:00Z) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs394() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00Z\") castable as xs:time",
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
   * Try xs:time(13:20:00-05:00) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs395() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:date",
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
   * Try xs:time(13:20:00-05:00) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs396() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:gYearMonth",
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
   * Try xs:time(13:20:00-05:00) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs397() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:gYear",
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
   * Try xs:time(13:20:00-05:00) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs398() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:gMonthDay",
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
   * Try xs:time(13:20:00-05:00) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs399() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:gDay",
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
   * Try xs:time(13:20:00-05:00) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs400() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:gMonth",
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
   * Try xs:time(13:20:00-05:00) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs401() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:boolean",
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
   * Try xs:time(13:20:00-05:00) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs402() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:base64Binary",
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
   * Try xs:time(13:20:00-05:00) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs403() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:hexBinary",
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
   * Try xs:time(13:20:00-05:00) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs404() {
    final XQuery query = new XQuery(
      "xs:time(\"13:20:00-05:00\") castable as xs:anyURI",
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
   * Try xs:date(1999-05-31) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs405() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:untypedAtomic",
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
   * Try xs:date(-0012-12-03-05:00) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs406() {
    final XQuery query = new XQuery(
      "xs:date(\"-0012-12-03-05:00\") castable as xs:untypedAtomic",
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
   * Try xs:date(1999-05-31) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs407() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:string",
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
   * Try xs:date(-0012-12-03-05:00) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs408() {
    final XQuery query = new XQuery(
      "xs:date(\"-0012-12-03-05:00\") castable as xs:string",
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
   * Try xs:date(1999-05-31) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs409() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:float",
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
   * Try xs:date(1999-05-31) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs410() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:double",
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
   * Try xs:date(1999-05-31Z) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs411() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31Z\") castable as xs:double",
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
   * Try xs:date(1999-05-31) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs412() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:integer",
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
   * Try xs:date(1999-05-31) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs413() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:duration",
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
   * Try xs:date(1999-05-31) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs414() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:yearMonthDuration",
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
   * Try xs:date(1999-05-31) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs415() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:dayTimeDuration",
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
   * Try xs:date(1999-05-31) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs416() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:dateTime",
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
   * Try xs:date(1999-05-31) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs417() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:time",
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
   * Try xs:date(1999-05-31) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs418() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:date",
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
   * Try xs:date(-0012-12-03-05:00) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs419() {
    final XQuery query = new XQuery(
      "xs:date(\"-0012-12-03-05:00\") castable as xs:date",
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
   * Try xs:date(1999-05-31Z) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs420() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31Z\") castable as xs:date",
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
   * Try xs:date(1999-05-31) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs421() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:gYearMonth",
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
   * Try xs:date(1999-05-31) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs422() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:gYear",
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
   * Try xs:date(-0012-12-03-05:00) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs423() {
    final XQuery query = new XQuery(
      "xs:date(\"-0012-12-03-05:00\") castable as xs:gYear",
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
   * Try xs:date(1999-05-31Z) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs424() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31Z\") castable as xs:gYear",
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
   * Try xs:date(1999-05-31) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs425() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:gMonthDay",
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
   * Try xs:date(-0012-12-03-05:00) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs426() {
    final XQuery query = new XQuery(
      "xs:date(\"-0012-12-03-05:00\") castable as xs:gMonthDay",
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
   * Try xs:date(1999-05-31Z) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs427() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31Z\") castable as xs:gMonthDay",
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
   * Try xs:date(1999-05-31Z) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs428() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31Z\") castable as xs:gDay",
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
   * Try xs:date(-0012-12-03-05:00) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs429() {
    final XQuery query = new XQuery(
      "xs:date(\"-0012-12-03-05:00\") castable as xs:gMonth",
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
   * Try xs:date(1999-05-31Z) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs430() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31Z\") castable as xs:gMonth",
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
   * Try xs:date(1999-05-31) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs431() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:boolean",
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
   * Try xs:date(1999-05-31) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs432() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:base64Binary",
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
   * Try xs:date(1999-05-31) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs433() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:hexBinary",
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
   * Try xs:date(1999-05-31) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs434() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-05-31\") castable as xs:anyURI",
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
   * Try xs:gYearMonth(1999-05) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs435() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05\") castable as xs:untypedAtomic",
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
   * Try xs:gYearMonth(1999-05) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs436() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05\") castable as xs:string",
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
   * Try xs:gYearMonth(1999-05) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs437() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05\") castable as xs:float",
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
   * Try xs:gYearMonth(1999-05) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs438() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05\") castable as xs:double",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs439() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:double",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs440() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:decimal",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs441() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:integer",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs442() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:duration",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs443() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:yearMonthDuration",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs444() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:dayTimeDuration",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs445() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:dateTime",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs446() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:time",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs447() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:date",
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
   * Try xs:gYearMonth(1999-05) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs448() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05\") castable as xs:gYearMonth",
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
   * Try xs:gYearMonth(-0012-12-05:00) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs449() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"-0012-12-05:00\") castable as xs:gYearMonth",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs450() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:gYearMonth",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs451() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:gYear",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs452() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:gMonthDay",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs453() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:gDay",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs454() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:gMonth",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs455() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:boolean",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs456() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:base64Binary",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs457() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:hexBinary",
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
   * Try xs:gYearMonth(1999-05Z) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs458() {
    final XQuery query = new XQuery(
      "xs:gYearMonth(\"1999-05Z\") castable as xs:anyURI",
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
   * Try xs:gYear(1999) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs459() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:untypedAtomic",
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
   * Try xs:gYear(1999) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs460() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:string",
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
   * Try xs:gYear(1999) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs461() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:float",
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
   * Try xs:gYear(1999) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs462() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:double",
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
   * Try xs:gYear(1999) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs463() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:decimal",
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
   * Try xs:gYear(1999) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs464() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:integer",
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
   * Try xs:gYear(1999) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs465() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:duration",
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
   * Try xs:gYear(1999) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs466() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:yearMonthDuration",
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
   * Try xs:gYear(1999) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs467() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:dateTime",
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
   * Try xs:gYear(1999) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs468() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:time",
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
   * Try xs:gYear(1999) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs469() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:date",
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
   * Try xs:gYear(1999) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs470() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:gYearMonth",
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
   * Try xs:gYear(1999) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs471() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:gYear",
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
   * Try xs:gYear(-0012-05:00) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs472() {
    final XQuery query = new XQuery(
      "xs:gYear(\"-0012-05:00\") castable as xs:gYear",
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
   * Try xs:gYear(1999Z) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs473() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999Z\") castable as xs:gYear",
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
   * Try xs:gYear(1999) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs474() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:gMonthDay",
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
   * Try xs:gYear(1999) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs475() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:gDay",
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
   * Try xs:gYear(1999) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs476() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:gMonth",
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
   * Try xs:gYear(1999) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs477() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:boolean",
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
   * Try xs:gYear(1999) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs478() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:base64Binary",
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
   * Try xs:gYear(1999) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs479() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:hexBinary",
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
   * Try xs:gYear(1999) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs480() {
    final XQuery query = new XQuery(
      "xs:gYear(\"1999\") castable as xs:anyURI",
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
   * Try xs:gMonthDay(--05-31) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs481() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:untypedAtomic",
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
   * Try xs:gMonthDay(--05-31) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs482() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:string",
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
   * Try xs:gMonthDay(--05-31) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs483() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:float",
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
   * Try xs:gMonthDay(--05-31) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs484() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:double",
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
   * Try xs:gMonthDay(--05-31) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs485() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:decimal",
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
   * Try xs:gMonthDay(--05-31) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs486() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:integer",
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
   * Try xs:gMonthDay(--05-31) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs487() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:duration",
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
   * Try xs:gMonthDay(--05-31) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs488() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:yearMonthDuration",
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
   * Try xs:gMonthDay(--05-31) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs489() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:dayTimeDuration",
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
   * Try xs:gMonthDay(--05-31) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs490() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:dateTime",
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
   * Try xs:gMonthDay(--05-31) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs491() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:time",
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
   * Try xs:gMonthDay(--05-31) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs492() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:date",
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
   * Try xs:gMonthDay(--05-31) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs493() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:gYearMonth",
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
   * Try xs:gMonthDay(--05-31) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs494() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:gYear",
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
   * Try xs:gMonthDay(--05-31) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs495() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:gMonthDay",
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
   * Try xs:gMonthDay(--12-03-05:00) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs496() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--12-03-05:00\") castable as xs:gMonthDay",
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
   * Try xs:gMonthDay(--05-31Z) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs497() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31Z\") castable as xs:gMonthDay",
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
   * Try xs:gMonthDay(--05-31) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs498() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:gDay",
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
   * Try xs:gMonthDay(--05-31) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs499() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:gMonth",
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
   * Try xs:gMonthDay(--05-31) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs500() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:boolean",
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
   * Try xs:gMonthDay(--05-31) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs501() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:base64Binary",
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
   * Try xs:gMonthDay(--05-31) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs502() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:hexBinary",
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
   * Try xs:gMonthDay(--05-31) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs503() {
    final XQuery query = new XQuery(
      "xs:gMonthDay(\"--05-31\") castable as xs:anyURI",
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
   * Try xs:gDay(---31) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs504() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:untypedAtomic",
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
   * Try xs:gDay(---31) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs505() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:string",
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
   * Try xs:gDay(---31) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs506() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:float",
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
   * Try xs:gDay(---31) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs507() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:double",
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
   * Try xs:gDay(---31) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs508() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:decimal",
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
   * Try xs:gDay(---31) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs509() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:integer",
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
   * Try xs:gDay(---31) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs510() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:duration",
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
   * Try xs:gDay(---31) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs511() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:yearMonthDuration",
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
   * Try xs:gDay(---31) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs512() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:dayTimeDuration",
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
   * Try xs:gDay(---31) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs513() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:dateTime",
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
   * Try xs:gDay(---31) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs514() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:time",
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
   * Try xs:gDay(---31) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs515() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:date",
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
   * Try xs:gDay(---31) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs516() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:gYearMonth",
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
   * Try xs:gDay(---31) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs517() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:gYear",
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
   * Try xs:gDay(---31) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs518() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:gMonthDay",
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
   * Try xs:gDay(---31) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs519() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:gDay",
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
   * Try xs:gDay(---03-05:00) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs520() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---03-05:00\") castable as xs:gDay",
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
   * Try xs:gDay(---31Z) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs521() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31Z\") castable as xs:gDay",
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
   * Try xs:gDay(---31) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs522() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:gMonth",
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
   * Try xs:gDay(---31) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs523() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:boolean",
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
   * Try xs:gDay(---31) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs524() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:base64Binary",
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
   * Try xs:gDay(---31) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs525() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:hexBinary",
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
   * Try xs:gDay(---31) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs526() {
    final XQuery query = new XQuery(
      "xs:gDay(\"---31\") castable as xs:anyURI",
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
   * Try xs:gMonth(--05) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs527() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:untypedAtomic",
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
   * Try xs:gMonth(--05) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs528() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:string",
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
   * Try xs:gMonth(--05) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs529() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:float",
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
   * Try xs:gMonth(--05) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs530() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:double",
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
   * Try xs:gMonth(--05) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs531() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:decimal",
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
   * Try xs:gMonth(--05) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs532() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:integer",
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
   * Try xs:gMonth(--05) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs533() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:duration",
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
   * Try xs:gMonth(--05) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs534() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:yearMonthDuration",
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
   * Try xs:gMonth(--05) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs535() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:dayTimeDuration",
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
   * Try xs:gMonth(--05) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs536() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:dateTime",
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
   * Try xs:gMonth(--05) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs537() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:time",
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
   * Try xs:gMonth(--05) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs538() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:date",
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
   * Try xs:gMonth(--05) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs539() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:gYearMonth",
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
   * Try xs:gMonth(--05) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs540() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:gYear",
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
   * Try xs:gMonth(--05) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs541() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:gMonthDay",
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
   * Try xs:gMonth(--05) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs542() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:gDay",
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
   * Try xs:gMonth(--05) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs543() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:gMonth",
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
   * Try xs:gMonth(--12-05:00) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs544() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--12-05:00\") castable as xs:gMonth",
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
   * Try xs:gMonth(--05Z) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs545() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05Z\") castable as xs:gMonth",
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
   * Try xs:gMonth(--05) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs546() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:boolean",
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
   * Try xs:gMonth(--05) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs547() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:base64Binary",
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
   * Try xs:gMonth(--05) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs548() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:hexBinary",
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
   * Try xs:gMonth(--05) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs549() {
    final XQuery query = new XQuery(
      "xs:gMonth(\"--05\") castable as xs:anyURI",
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
   * Try xs:boolean(true) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs550() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:untypedAtomic",
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
   * Try xs:boolean(true) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs551() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:string",
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
   * Try xs:boolean(true) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs552() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:float",
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
   * Try xs:boolean(false) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs553() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") castable as xs:float",
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
   * Try xs:boolean(true) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs554() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:double",
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
   * Try xs:boolean(false) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs555() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") castable as xs:double",
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
   * Try xs:boolean(true) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs556() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:decimal",
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
   * Try xs:boolean(false) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs557() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") castable as xs:decimal",
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
   * Try xs:boolean(true) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs558() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:integer",
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
   * Try xs:boolean(false) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs559() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") castable as xs:integer",
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
   * Try xs:boolean(true) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs560() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:duration",
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
   * Try xs:boolean(true) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs561() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:yearMonthDuration",
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
   * Try xs:boolean(true) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs562() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:dayTimeDuration",
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
   * Try xs:boolean(true) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs563() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:dateTime",
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
   * Try xs:boolean(true) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs564() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:time",
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
   * Try xs:boolean(true) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs565() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:date",
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
   * Try xs:boolean(true) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs566() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:gYearMonth",
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
   * Try xs:boolean(true) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs567() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:gYear",
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
   * Try xs:boolean(true) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs568() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:gMonthDay",
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
   * Try xs:boolean(true) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs569() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:gDay",
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
   * Try xs:boolean(true) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs570() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:gMonth",
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
   * Try xs:boolean(true) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs571() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:boolean",
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
   * Try xs:boolean(false) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs572() {
    final XQuery query = new XQuery(
      "xs:boolean(\"false\") castable as xs:boolean",
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
   * Try xs:boolean(true) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs573() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:base64Binary",
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
   * Try xs:boolean(true) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs574() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:hexBinary",
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
   * Try xs:boolean(true) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs575() {
    final XQuery query = new XQuery(
      "xs:boolean(\"true\") castable as xs:anyURI",
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
   * Try xs:base64Binary(01001010) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs576() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"01001010\") castable as xs:untypedAtomic",
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
   * Try xs:base64Binary(0FB7) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs577() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"0FB7\") castable as xs:untypedAtomic",
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
   * Try xs:base64Binary(01001010) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs578() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"01001010\") castable as xs:string",
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
   * Try xs:base64Binary(0FB7) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs579() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"0FB7\") castable as xs:string",
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
   * Try xs:base64Binary(10010101) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs580() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:float",
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
   * Try xs:base64Binary(10010101) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs581() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:double",
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
   * Try xs:base64Binary(10010101) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs582() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:decimal",
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
   * Try xs:base64Binary(10010101) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs583() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:integer",
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
   * Try xs:base64Binary(10010101) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs584() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:duration",
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
   * Try xs:base64Binary(10010101) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs585() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:yearMonthDuration",
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
   * Try xs:base64Binary(10010101) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs586() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:dayTimeDuration",
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
   * Try xs:base64Binary(10010101) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs587() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:dateTime",
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
   * Try xs:base64Binary(10010101) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs588() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:time",
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
   * Try xs:base64Binary(10010101) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs589() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:date",
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
   * Try xs:base64Binary(10010101) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs590() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:gYearMonth",
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
   * Try xs:base64Binary(10010101) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs591() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:gYear",
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
   * Try xs:base64Binary(10010101) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs592() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:gMonthDay",
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
   * Try xs:base64Binary(10010101) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs593() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:gDay",
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
   * Try xs:base64Binary(10010101) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs594() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:gMonth",
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
   * Try xs:base64Binary(10010101) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs595() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:boolean",
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
   * Try xs:base64Binary(01001010) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs596() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"01001010\") castable as xs:base64Binary",
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
   * Try xs:base64Binary(0FB7) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs597() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"0FB7\") castable as xs:base64Binary",
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
   * Try xs:base64Binary(00000000) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs598() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"00000000\") castable as xs:hexBinary",
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
   * Try xs:base64Binary(10010101) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs599() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:hexBinary",
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
   * Try xs:base64Binary(aA+zZ/09) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs600() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"aA+zZ/09\") castable as xs:hexBinary",
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
   * Try xs:base64Binary(0FB7) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs601() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"0FB7\") castable as xs:hexBinary",
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
   * Try xs:base64Binary(10010101) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs602() {
    final XQuery query = new XQuery(
      "xs:base64Binary(\"10010101\") castable as xs:anyURI",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs603() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:untypedAtomic",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs604() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:string",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs605() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:float",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs606() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:double",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs607() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:decimal",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs608() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:integer",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs609() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:duration",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs610() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:yearMonthDuration",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs611() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:dayTimeDuration",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs612() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:dateTime",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs613() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:time",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs614() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:date",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs615() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:gYearMonth",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs616() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:gYear",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs617() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:gMonthDay",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs618() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:gDay",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs619() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:gMonth",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs620() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:boolean",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs621() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:base64Binary",
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
   * Try xs:hexBinary(0fb7) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs622() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"0fb7\") castable as xs:base64Binary",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs623() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:hexBinary",
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
   * Try xs:hexBinary(d74d35d35d35) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs624() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"d74d35d35d35\") castable as xs:hexBinary",
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
   * Try xs:hexBinary(D74D35D35D35) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs625() {
    final XQuery query = new XQuery(
      "xs:hexBinary(\"D74D35D35D35\") castable as xs:anyURI",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:untypedAtomic .
   */
  @org.junit.Test
  public void castableAs626() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:untypedAtomic",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:string .
   */
  @org.junit.Test
  public void castableAs627() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:string",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:float .
   */
  @org.junit.Test
  public void castableAs628() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:float",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:double .
   */
  @org.junit.Test
  public void castableAs629() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:double",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:decimal .
   */
  @org.junit.Test
  public void castableAs630() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:decimal",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:integer .
   */
  @org.junit.Test
  public void castableAs631() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:integer",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:duration .
   */
  @org.junit.Test
  public void castableAs632() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:duration",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:yearMonthDuration .
   */
  @org.junit.Test
  public void castableAs633() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:yearMonthDuration",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:dayTimeDuration .
   */
  @org.junit.Test
  public void castableAs634() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:dayTimeDuration",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:dateTime .
   */
  @org.junit.Test
  public void castableAs635() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:dateTime",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:time .
   */
  @org.junit.Test
  public void castableAs636() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:time",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:date .
   */
  @org.junit.Test
  public void castableAs637() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:date",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:gYearMonth .
   */
  @org.junit.Test
  public void castableAs638() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:gYearMonth",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:gYear .
   */
  @org.junit.Test
  public void castableAs639() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:gYear",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:gMonthDay .
   */
  @org.junit.Test
  public void castableAs640() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:gMonthDay",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:gDay .
   */
  @org.junit.Test
  public void castableAs641() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:gDay",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:gMonth .
   */
  @org.junit.Test
  public void castableAs642() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:gMonth",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:boolean .
   */
  @org.junit.Test
  public void castableAs643() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:boolean",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:base64Binary .
   */
  @org.junit.Test
  public void castableAs644() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:base64Binary",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:hexBinary .
   */
  @org.junit.Test
  public void castableAs645() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:hexBinary",
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
   * Try xs:anyURI(http://www.ietf.org/rfc/rfc2396.txt) castable as xs:anyURI .
   */
  @org.junit.Test
  public void castableAs646() {
    final XQuery query = new XQuery(
      "xs:anyURI(\"http://www.ietf.org/rfc/rfc2396.txt\") castable as xs:anyURI",
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
   * Try string literal castable as xs:QName .
   */
  @org.junit.Test
  public void castableAs647() {
    final XQuery query = new XQuery(
      "\"ABC\" castable as xs:QName",
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
   * Try variable castable as xs:QName .
   */
  @org.junit.Test
  public void castableAs648() {
    xquery10();
    final XQuery query = new XQuery(
      "for $var in \"ABC\" return $var castable as xs:QName",
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
   * Try variable castable as xs:QName .
   */
  @org.junit.Test
  public void castableAs648a() {
    final XQuery query = new XQuery(
      "let $var := \"ABC\" return $var castable as xs:QName",
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
   * Reordering the predicates should not cause the expression to raise an error .
   */
  @org.junit.Test
  public void castableAs649() {
    final XQuery query = new XQuery(
      "count(//employee[salary castable as xs:integer][xs:integer(salary) gt 65000])",
      ctx);
    try {
      query.context(node(file("op/union/acme_corp.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   * The expression should not raise a dynamic error .
   */
  @org.junit.Test
  public void castableAs650() {
    final XQuery query = new XQuery(
      "count(//employee[if (salary castable as xs:integer) then xs:integer(salary) gt 65000 else false()])",
      ctx);
    try {
      query.context(node(file("op/union/acme_corp.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  '+' nor '?' is allowed as a cardinality specifier in 'castable as'. .
   */
  @org.junit.Test
  public void kSeqExprCastable1() {
    final XQuery query = new XQuery(
      "'string' castable as xs:string*",
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
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable10() {
    final XQuery query = new XQuery(
      "\"notation is abstract\" castable as xs:NOTATION",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0080")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable11() {
    final XQuery query = new XQuery(
      "() castable as xs:NOTATION",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0080")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable12() {
    xquery10();
    final XQuery query = new XQuery(
      "(xs:double(1), xs:double(2), xs:double(3)) castable as xs:double*",
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
        error("XQST0052")
      )
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable12a() {
    final XQuery query = new XQuery(
      "(xs:double(1), xs:double(2), xs:double(3)) castable as xs:double*",
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
        error("XPST0051")
      )
    );
  }

  /**
   *  An invalid type for 'castable as' is specified, leading to a syntax error. .
   */
  @org.junit.Test
  public void kSeqExprCastable13() {
    xquery10();
    final XQuery query = new XQuery(
      "'string' castable as item()",
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
        error("XPST0051")
      )
    );
  }

  /**
   *  An invalid type for 'castable as' is specified, leading to a syntax error. .
   */
  @org.junit.Test
  public void kSeqExprCastable13a() {
    final XQuery query = new XQuery(
      "'string' castable as item()",
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
        error("XQST0052")
      )
    );
  }

  /**
   *  An invalid type for 'castable as' is specified, leading to a syntax error. .
   */
  @org.junit.Test
  public void kSeqExprCastable14() {
    final XQuery query = new XQuery(
      "'string' castable as node()",
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
        error("XPST0051")
      )
    );
  }

  /**
   *  An invalid type for 'castable as' is specified, leading to a syntax error. .
   */
  @org.junit.Test
  public void kSeqExprCastable15() {
    final XQuery query = new XQuery(
      "'string' castable as attribute()",
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
        error("XPST0051")
      )
    );
  }

  /**
   *  An invalid type for 'castable as' is specified, leading to a syntax error. .
   */
  @org.junit.Test
  public void kSeqExprCastable16() {
    final XQuery query = new XQuery(
      "'string' castable as empty-sequence()",
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
        error("XPST0051")
      )
    );
  }

  /**
   *  A test whose essence is: `not(QName("", "lname") castable as xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable17() {
    final XQuery query = new XQuery(
      "not(QName(\"\", \"lname\") castable as xs:integer)",
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
   *  When casting to xs:QName the source value can be a xs:QName value. .
   */
  @org.junit.Test
  public void kSeqExprCastable18() {
    final XQuery query = new XQuery(
      "QName(\"\", \"lname\") castable as xs:QName",
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
   *  Casting an xs:string to xs:QName works. .
   */
  @org.junit.Test
  public void kSeqExprCastable19() {
    final XQuery query = new XQuery(
      "\"ncname\" castable as xs:QName",
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
   *  '+' nor '?' is allowed as a cardinality specifier in 'castable as'. .
   */
  @org.junit.Test
  public void kSeqExprCastable2() {
    final XQuery query = new XQuery(
      "'string' castable as xs:string+",
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
   *  Casting an empty sequence to xs:QName does not work. .
   */
  @org.junit.Test
  public void kSeqExprCastable20() {
    final XQuery query = new XQuery(
      "not(() castable as xs:QName)",
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
   *  Casting an empty sequence to xs:QName? works. .
   */
  @org.junit.Test
  public void kSeqExprCastable21() {
    final XQuery query = new XQuery(
      "() castable as xs:QName?",
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
   *  Casting an xs:untypedAtomic to xs:QName does not work. .
   */
  @org.junit.Test
  public void kSeqExprCastable22() {
    final XQuery query = new XQuery(
      "not(xs:untypedAtomic(\"ncname\") castable as xs:QName)",
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
        assertBoolean(false)
      )
    );
  }

  /**
   *  A test whose essence is: `not(("one", "two") castable as xs:string?)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable23() {
    final XQuery query = new XQuery(
      "not((\"one\", \"two\") castable as xs:string?)",
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
   *  A test whose essence is: `not('string' castable as xs:boolean)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable24() {
    final XQuery query = new XQuery(
      "not('string' castable as xs:boolean)",
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
   *  A test whose essence is: `'true' castable as xs:boolean`. .
   */
  @org.junit.Test
  public void kSeqExprCastable25() {
    final XQuery query = new XQuery(
      "'true' castable as xs:boolean",
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
   *  A test whose essence is: `xs:float(1) castable as xs:float`. .
   */
  @org.junit.Test
  public void kSeqExprCastable26() {
    final XQuery query = new XQuery(
      "xs:float(1) castable as xs:float",
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
   *  A test whose essence is: `xs:float(1) castable as xs:untypedAtomic`. .
   */
  @org.junit.Test
  public void kSeqExprCastable27() {
    final XQuery query = new XQuery(
      "xs:float(1) castable as xs:untypedAtomic",
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
   *  A test whose essence is: `xs:float(1) castable as xs:string`. .
   */
  @org.junit.Test
  public void kSeqExprCastable28() {
    final XQuery query = new XQuery(
      "xs:float(1) castable as xs:string",
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
   *  A test whose essence is: `not(xs:anyURI("example.com/") castable as xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable29() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"example.com/\") castable as xs:integer)",
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
   *  '+' nor '?' is allowed as a cardinality specifier in 'castable as'. .
   */
  @org.junit.Test
  public void kSeqExprCastable3() {
    final XQuery query = new XQuery(
      "(\"one\", \"two\") castable as xs:string+",
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
   *  A test whose essence is: `not("three" castable as xs:float)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable30() {
    final XQuery query = new XQuery(
      "not(\"three\" castable as xs:float)",
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
   *  A test whose essence is: `not("three" castable as xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable31() {
    final XQuery query = new XQuery(
      "not(\"three\" castable as xs:integer)",
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
   *  A test whose essence is: `not("three" castable as xs:decimal)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable32() {
    final XQuery query = new XQuery(
      "not(\"three\" castable as xs:decimal)",
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
   *  A test whose essence is: `not("three" castable as xs:double)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable33() {
    final XQuery query = new XQuery(
      "not(\"three\" castable as xs:double)",
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
   *  A test whose essence is: `not((1, 2, 3) castable as xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable34() {
    final XQuery query = new XQuery(
      "not((1, 2, 3) castable as xs:integer)",
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
   *  A test whose essence is: `not(() castable as xs:integer)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable35() {
    final XQuery query = new XQuery(
      "not(() castable as xs:integer)",
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
   *  A test whose essence is: `() castable as xs:integer?`. .
   */
  @org.junit.Test
  public void kSeqExprCastable36() {
    final XQuery query = new XQuery(
      "() castable as xs:integer?",
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
   *  A test whose essence is: `(1) castable as xs:integer?`. .
   */
  @org.junit.Test
  public void kSeqExprCastable37() {
    final XQuery query = new XQuery(
      "(1) castable as xs:integer?",
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
   *  A test whose essence is: `not(("one", "two") castable as xs:string?)`. .
   */
  @org.junit.Test
  public void kSeqExprCastable38() {
    final XQuery query = new XQuery(
      "not((\"one\", \"two\") castable as xs:string?)",
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
   *  '+' nor '?' is allowed as a cardinality specifier in 'castable as'. .
   */
  @org.junit.Test
  public void kSeqExprCastable4() {
    xquery10();
    final XQuery query = new XQuery(
      "'string' castable as xs:anyType*",
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
        error("XPST0051")
      )
    );
  }

  /**
   *  '+' nor '?' is allowed as a cardinality specifier in 'castable as'. .
   */
  @org.junit.Test
  public void kSeqExprCastable4a() {
    final XQuery query = new XQuery(
      "'string' castable as xs:anyType*",
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
        error("XQST0052")
      )
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable5() {
    xquery10();
    final XQuery query = new XQuery(
      "'string' castable as xs:anySimpleType",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0051")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable5a() {
    final XQuery query = new XQuery(
      "'string' castable as xs:anySimpleType",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0080")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable6() {
    xquery10();
    final XQuery query = new XQuery(
      "'string' castable as xs:untyped",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0051")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable6a() {
    final XQuery query = new XQuery(
      "'string' castable as xs:untyped",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0052")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable7() {
    final XQuery query = new XQuery(
      "'string' castable as xs:anyAtomicType",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0080")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable8() {
    final XQuery query = new XQuery(
      "\"notation is abstract\" castable as xs:NOTATION?",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0080")
    );
  }

  /**
   *  An invalid type for 'castable as' is specified. .
   */
  @org.junit.Test
  public void kSeqExprCastable9() {
    final XQuery query = new XQuery(
      "() castable as xs:NOTATION?",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0080")
    );
  }

  /**
   *  Testing castability to xs:QName where the cardinality is wrong. .
   */
  @org.junit.Test
  public void k2SeqExprCastable1() {
    final XQuery query = new XQuery(
      "(QName(\"http://example.com/ANamespace\", \"ncname\"), QName(\"http://example.com/ANamespace\", \"ncname2\"), QName(\"http://example.com/ANamespace\", \"ncname3\")) castable as xs:QName",
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
   *  Testing castability to xs:QName where the cardinality is wrong(#2). .
   */
  @org.junit.Test
  public void k2SeqExprCastable2() {
    final XQuery query = new XQuery(
      "(QName(\"http://example.com/ANamespace\", \"ncname\"), QName(\"http://example.com/ANamespace\", \"ncname2\"), QName(\"http://example.com/ANamespace\", \"ncname3\")) castable as xs:QName?",
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
   *  Combine operator div with castable as. .
   */
  @org.junit.Test
  public void k2SeqExprCastable3() {
    final XQuery query = new XQuery(
      "(1 div 0) castable as xs:string",
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
        error("FOAR0001")
      )
    );
  }

  /**
   *  Combine operator div with castable as(#2). .
   */
  @org.junit.Test
  public void k2SeqExprCastable4() {
    final XQuery query = new XQuery(
      "concat(\"2007-01-3\", 1 div 0) castable as xs:date",
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
   *  Check casting an xs:positiveInteger value to xs:double. .
   */
  @org.junit.Test
  public void k2SeqExprCastable5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52\") castable as xs:double",
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
   *  Check that an invalid xs:anyURI gets flagged as not being castable. .
   */
  @org.junit.Test
  public void k2SeqExprCastable6() {
    final XQuery query = new XQuery(
      "\"%\" castable as xs:anyURI",
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
   *  Check that an invalid xs:anyURI gets flagged as not being castable. .
   */
  @org.junit.Test
  public void k2SeqExprCastable7() {
    final XQuery query = new XQuery(
      "xs:untypedAtomic(\"%\") castable as xs:anyURI",
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
   *  Check castability of an invalid xs:anyURI. .
   */
  @org.junit.Test
  public void k2SeqExprCastable8() {
    final XQuery query = new XQuery(
      "(\"http:\\\\invalid>URI\\someURI\") castable as xs:anyURI",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableByte001() {
    final XQuery query = new XQuery(
      "128 castable as xs:byte",
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
   *  test castable to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableByte002() {
    final XQuery query = new XQuery(
      "-129 castable as xs:byte",
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
   *  test castable to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableByte003() {
    final XQuery query = new XQuery(
      "\"128\" castable as xs:byte",
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
   *  test castable to xs:byte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableByte004() {
    final XQuery query = new XQuery(
      "\"-129\" castable as xs:byte",
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
   *  test castable to xs:date with overflow .
   */
  @org.junit.Test
  public void cbclCastableDate001() {
    final XQuery query = new XQuery(
      "\"-25252734927766555-06-06\" castable as xs:date",
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
   *  test castable to xs:date with overflow .
   */
  @org.junit.Test
  public void cbclCastableDate002() {
    final XQuery query = new XQuery(
      "\"25252734927766555-07-29\" castable as xs:date",
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
   *  test castable to xs:dateTime with overflow .
   */
  @org.junit.Test
  public void cbclCastableDateTime001() {
    final XQuery query = new XQuery(
      "\"-25252734927766555-06-06T00:00:00Z\" castable as xs:dateTime",
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
   *  test castable to xs:dateTime with overflow .
   */
  @org.junit.Test
  public void cbclCastableDateTime002() {
    final XQuery query = new XQuery(
      "\"25252734927766555-07-29T00:00:00Z\" castable as xs:dateTime",
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
   *  test castable to xs:dayTimeDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastableDayTimeDuration001() {
    final XQuery query = new XQuery(
      "\"P11768614336404564651D\" castable as xs:dayTimeDuration",
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
   *  test castable to xs:dayTimeDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastableDayTimeDuration002() {
    final XQuery query = new XQuery(
      "\"-P11768614336404564651D\" castable as xs:duration",
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
   *  test castability of xs:float('NaN') as xs:decimal .
   */
  @org.junit.Test
  public void cbclCastableDecimal001() {
    final XQuery query = new XQuery(
      "xs:float('NaN') castable as xs:decimal",
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
   *  test castability of xs:float('-INF') as xs:decimal .
   */
  @org.junit.Test
  public void cbclCastableDecimal002() {
    final XQuery query = new XQuery(
      "xs:float('-INF') castable as xs:decimal",
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
   *  test castability of xs:float('INF') as xs:decimal .
   */
  @org.junit.Test
  public void cbclCastableDecimal003() {
    final XQuery query = new XQuery(
      "xs:float('INF') castable as xs:decimal",
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
   *  test castability of xs:double('NaN') as xs:double .
   */
  @org.junit.Test
  public void cbclCastableDecimal004() {
    final XQuery query = new XQuery(
      "xs:double('NaN') castable as xs:decimal",
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
   *  test castability of xs:double('-INF') as xs:decimal .
   */
  @org.junit.Test
  public void cbclCastableDecimal005() {
    final XQuery query = new XQuery(
      "xs:double('-INF') castable as xs:decimal",
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
   *  test castability of xs:double('INF') as xs:decimal .
   */
  @org.junit.Test
  public void cbclCastableDecimal006() {
    final XQuery query = new XQuery(
      "xs:double('INF') castable as xs:decimal",
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
   *  test castable as xs:decimal of large double value .
   */
  @org.junit.Test
  public void cbclCastableDecimal007() {
    final XQuery query = new XQuery(
      "1.7976931348623157E+308 castable as xs:decimal",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable as xs:decimal of large negative double value .
   */
  @org.junit.Test
  public void cbclCastableDecimal008() {
    final XQuery query = new XQuery(
      "-1.7976931348623157E+308 castable as xs:decimal",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable as xs:decimal of large float value .
   */
  @org.junit.Test
  public void cbclCastableDecimal009() {
    final XQuery query = new XQuery(
      "xs:float('3.402823e38') castable as xs:decimal",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable to xs:decimal of large negative float value .
   */
  @org.junit.Test
  public void cbclCastableDecimal010() {
    final XQuery query = new XQuery(
      "xs:float('-3.402823e38') castable as xs:decimal",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable to xs:duration with overflow .
   */
  @org.junit.Test
  public void cbclCastableDuration001() {
    final XQuery query = new XQuery(
      "\"-P768614336404564651Y\" castable as xs:duration",
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
   *  test castable to xs:duration with overflow .
   */
  @org.junit.Test
  public void cbclCastableDuration002() {
    final XQuery query = new XQuery(
      "\"P768614336404564651Y\" castable as xs:duration",
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
   *  test cast to xs:gYear with overflow .
   */
  @org.junit.Test
  public void cbclCastableGYear001() {
    final XQuery query = new XQuery(
      "\"99999999999999999999999999999\" castable as xs:gYear",
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
   *  test casting 0000 to xs:gYear .
   */
  @org.junit.Test
  public void cbclCastableGYear002() {
    final XQuery query = new XQuery(
      "\"0000\" castable as xs:gYear",
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
   *  test casting -0000 to xs:gYear .
   */
  @org.junit.Test
  public void cbclCastableGYear003() {
    final XQuery query = new XQuery(
      "\"-0000\" castable as xs:gYear",
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
   *  test cast to xs:gYearMonth with overflow .
   */
  @org.junit.Test
  public void cbclCastableGYearMonth001() {
    final XQuery query = new XQuery(
      "\"99999999999999999999999999999-01\" castable as xs:gYearMonth",
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
   *  test cast to xs:gYearMonth with overflow .
   */
  @org.junit.Test
  public void cbclCastableGYearMonth002() {
    final XQuery query = new XQuery(
      "\"99999999999999999999999999999-XX\" castable as xs:gYearMonth",
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
   *  test cast year 0000 xs:gYearMonth .
   */
  @org.junit.Test
  public void cbclCastableGYearMonth003() {
    final XQuery query = new XQuery(
      "\"0000-05\" castable as xs:gYearMonth",
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
   *  test cast year 0000 xs:gYearMonth .
   */
  @org.junit.Test
  public void cbclCastableGYearMonth004() {
    final XQuery query = new XQuery(
      "\"-0000-05\" castable as xs:gYearMonth",
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
   *  test castable to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableInt001() {
    final XQuery query = new XQuery(
      "2147483648 castable as xs:int",
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
   *  test castable to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableInt002() {
    final XQuery query = new XQuery(
      "-2147483649 castable as xs:int",
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
   *  test castable to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableInt003() {
    final XQuery query = new XQuery(
      "\"2147483648\" castable as xs:int",
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
   *  test castable to xs:int of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableInt004() {
    final XQuery query = new XQuery(
      "\"-2147483649\" castable as xs:int",
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
   *  test castable as xs:integer of large double value .
   */
  @org.junit.Test
  public void cbclCastableInteger001() {
    final XQuery query = new XQuery(
      "1.7976931348623157E+308 castable as xs:integer",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable as xs:integer of large negative double value .
   */
  @org.junit.Test
  public void cbclCastableInteger002() {
    final XQuery query = new XQuery(
      "-1.7976931348623157E+308 castable as xs:integer",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable as xs:integer of large float value .
   */
  @org.junit.Test
  public void cbclCastableInteger003() {
    final XQuery query = new XQuery(
      "xs:float('3.402823e38') castable as xs:integer",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable to xs:integer of large negative float value .
   */
  @org.junit.Test
  public void cbclCastableInteger004() {
    final XQuery query = new XQuery(
      "xs:float('-3.402823e38') castable as xs:integer",
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
        assertBoolean(false)
      ||
        assertBoolean(true)
      )
    );
  }

  /**
   *  test castable to xs:language .
   */
  @org.junit.Test
  public void cbclCastableLanguage001() {
    final XQuery query = new XQuery(
      "xs:language('en-gb') castable as xs:language",
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
   *  test castable to xs:language from a type which can only fail .
   */
  @org.junit.Test
  public void cbclCastableLanguage002() {
    final XQuery query = new XQuery(
      "1.0 castable as xs:language",
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
   *  test castable to xs:language .
   */
  @org.junit.Test
  public void cbclCastableLanguage003() {
    final XQuery query = new XQuery(
      "'en-gb' castable as xs:language",
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
   *  test castable to xs:language .
   */
  @org.junit.Test
  public void cbclCastableLanguage004() {
    final XQuery query = new XQuery(
      "\"gobbledygook\" castable as xs:language",
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
   *  test castable to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableLong001() {
    final XQuery query = new XQuery(
      "9223372036854775808 castable as xs:long",
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
   *  test castable to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableLong002() {
    final XQuery query = new XQuery(
      "-9223372036854775809 castable as xs:long",
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
   *  test castable to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableLong003() {
    final XQuery query = new XQuery(
      "\"9223372036854775808\" castable as xs:long",
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
   *  test castable to xs:long of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableLong004() {
    final XQuery query = new XQuery(
      "\"-9223372036854775809\" castable as xs:long",
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
   *  test castable as xs:Name from a subtype of xs:Name .
   */
  @org.junit.Test
  public void cbclCastableName001() {
    final XQuery query = new XQuery(
      "xs:NCName('NCName') castable as xs:Name",
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
   *  test castable as xs:Name from a type which will always fail .
   */
  @org.junit.Test
  public void cbclCastableName002() {
    final XQuery query = new XQuery(
      "fn:current-time() castable as xs:Name",
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
   *  test castable as xs:Name .
   */
  @org.junit.Test
  public void cbclCastableName003() {
    final XQuery query = new XQuery(
      "'NCName' castable as xs:Name",
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
   *  test castable as xs:Name .
   */
  @org.junit.Test
  public void cbclCastableName004() {
    final XQuery query = new XQuery(
      "'N A M E' castable as xs:Name",
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
   *  test castable as xs:NCName from a subtype of xs:NCName .
   */
  @org.junit.Test
  public void cbclCastableNcname001() {
    final XQuery query = new XQuery(
      "xs:ID('id') castable as xs:NCName",
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
   *  test castable as xs:NCName from a type which will always fail .
   */
  @org.junit.Test
  public void cbclCastableNcname002() {
    final XQuery query = new XQuery(
      "fn:current-time() castable as xs:NCName",
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
   *  test castable as xs:NCName .
   */
  @org.junit.Test
  public void cbclCastableNcname003() {
    final XQuery query = new XQuery(
      "'NCName' castable as xs:NCName",
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
   *  test castable as xs:NCName .
   */
  @org.junit.Test
  public void cbclCastableNcname004() {
    final XQuery query = new XQuery(
      "'NC:Name' castable as xs:NCName",
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
   *  test castable to xs:negativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableNegativeInteger001() {
    final XQuery query = new XQuery(
      "0 castable as xs:negativeInteger",
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
   *  test castable to xs:negativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableNegativeInteger002() {
    final XQuery query = new XQuery(
      "\"0\" castable as xs:negativeInteger",
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
   *  test castable as xs:NMTOKEN from a subtype of xs:NMTOKEN .
   */
  @org.junit.Test
  public void cbclCastableNmtoken001() {
    final XQuery query = new XQuery(
      "xs:NMTOKEN('NMTOKEN') castable as xs:NMTOKEN",
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
   *  test cast to xs:NMTOKEN .
   */
  @org.junit.Test
  public void cbclCastableNmtoken002() {
    final XQuery query = new XQuery(
      "\n" +
      "        \"&#xD;&#xA;&#x9; foobar &#xA;&#xD;&#x9;\" castable as xs:NMTOKEN",
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
   *  test castable to xs:nonNegativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableNonNegativeInteger001() {
    final XQuery query = new XQuery(
      "-1 castable as xs:nonNegativeInteger",
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
   *  test castable to xs:nonNegativeInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableNonNegativeInteger002() {
    final XQuery query = new XQuery(
      "\"-1\" castable as xs:nonNegativeInteger",
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
   *  Ensure that "-00" is a valid lexical value for xs:nonNegativeInteger .
   */
  @org.junit.Test
  public void cbclCastableNonNegativeInteger003() {
    final XQuery query = new XQuery(
      "\"-00\" castable as xs:nonNegativeInteger",
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
   *  test castable to xs:nonPositiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableNonPositiveInteger001() {
    final XQuery query = new XQuery(
      "1 castable as xs:nonPositiveInteger",
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
   *  test castable to xs:nonPositiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableNonPositiveInteger002() {
    final XQuery query = new XQuery(
      "\"1\" castable as xs:nonPositiveInteger",
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
   *  Ensure that "+00" is a valid lexical value for xs:nonPositiveInteger .
   */
  @org.junit.Test
  public void cbclCastableNonPositiveInteger003() {
    final XQuery query = new XQuery(
      "\"+00\" castable as xs:nonPositiveInteger",
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
   *  test castable to xs:positiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastablePositiveInteger001() {
    final XQuery query = new XQuery(
      "0 castable as xs:positiveInteger",
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
   *  test castable to xs:positiveInteger of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastablePositiveInteger002() {
    final XQuery query = new XQuery(
      "\"0\" castable as xs:positiveInteger",
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
   *  test castable to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableShort001() {
    final XQuery query = new XQuery(
      "32768 castable as xs:short",
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
   *  test castable to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableShort002() {
    final XQuery query = new XQuery(
      "-32769 castable as xs:short",
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
   *  test castable to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableShort003() {
    final XQuery query = new XQuery(
      "\"32769\" castable as xs:short",
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
   *  test castable to xs:short of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableShort004() {
    final XQuery query = new XQuery(
      "\"-32769\" castable as xs:short",
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
   *  test castable to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedByte001() {
    final XQuery query = new XQuery(
      "256 castable as xs:unsignedByte",
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
   *  test castable to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedByte002() {
    final XQuery query = new XQuery(
      "-1 castable as xs:unsignedByte",
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
   *  test castable to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedByte003() {
    final XQuery query = new XQuery(
      "\"256\" castable as xs:unsignedByte",
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
   *  test castable to xs:unsignedByte of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedByte004() {
    final XQuery query = new XQuery(
      "\"-1\" castable as xs:unsignedByte",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedByte .
   */
  @org.junit.Test
  public void cbclCastableUnsignedByte005() {
    final XQuery query = new XQuery(
      "\"-00\" castable as xs:unsignedByte",
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
   *  test castable to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedInt001() {
    final XQuery query = new XQuery(
      "4294967296 castable as xs:unsignedInt",
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
   *  test castable to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedInt002() {
    final XQuery query = new XQuery(
      "-1 castable as xs:unsignedInt",
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
   *  test castable to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedInt003() {
    final XQuery query = new XQuery(
      "\"4294967296\" castable as xs:unsignedInt",
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
   *  test castable to xs:unsignedInt of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedInt004() {
    final XQuery query = new XQuery(
      "\"-1\" castable as xs:unsignedInt",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedInt .
   */
  @org.junit.Test
  public void cbclCastableUnsignedInt005() {
    final XQuery query = new XQuery(
      "\"-00\" castable as xs:unsignedInt",
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
   *  test castable to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedLong001() {
    final XQuery query = new XQuery(
      "18446744073709551616 castable as xs:unsignedLong",
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
   *  test castable to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedLong002() {
    final XQuery query = new XQuery(
      "-1 castable as xs:unsignedLong",
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
   *  test castable to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedLong003() {
    final XQuery query = new XQuery(
      "\"18446744073709551616\" castable as xs:unsignedLong",
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
   *  test castable to xs:unsignedLong of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedLong004() {
    final XQuery query = new XQuery(
      "\"-1\" castable as xs:unsignedLong",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedLong .
   */
  @org.junit.Test
  public void cbclCastableUnsignedLong005() {
    final XQuery query = new XQuery(
      "\"-00\" castable as xs:unsignedLong",
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
   *  test castable to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedShort001() {
    final XQuery query = new XQuery(
      "65536 castable as xs:unsignedShort",
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
   *  test castable to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedShort002() {
    final XQuery query = new XQuery(
      "-1 castable as xs:unsignedShort",
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
   *  test castable to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedShort003() {
    final XQuery query = new XQuery(
      "\"65536\" castable as xs:unsignedShort",
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
   *  test castable to xs:unsignedShort of out-of-range value .
   */
  @org.junit.Test
  public void cbclCastableUnsignedShort004() {
    final XQuery query = new XQuery(
      "\"-1\" castable as xs:unsignedShort",
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
   *  Ensure that "-00" is a valid lexical value for xs:unsignedShort .
   */
  @org.junit.Test
  public void cbclCastableUnsignedShort005() {
    final XQuery query = new XQuery(
      "\"-00\" castable as xs:unsignedShort",
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
   *  test castable to xs:yearMonthDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastableYearMonthDuration001() {
    final XQuery query = new XQuery(
      "\"-P768614336404564651Y\" castable as xs:yearMonthDuration",
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
   *  test castable to xs:yearMonthDuration with overflow .
   */
  @org.junit.Test
  public void cbclCastableYearMonthDuration002() {
    final XQuery query = new XQuery(
      "\"P768614336404564651Y\" castable as xs:yearMonthDuration",
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
}
