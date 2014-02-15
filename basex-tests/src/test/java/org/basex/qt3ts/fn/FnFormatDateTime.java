package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the format-dateTime() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFormatDateTime extends QT3TestSet {

  /**
   * test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime001a() {
    final XQuery query = new XQuery(
      "format-dateTime($d,\"[Y]-[M01]-[D]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:dateTime('2003-09-07T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2003-09-7")
    );
  }

  /**
   * test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime001b() {
    final XQuery query = new XQuery(
      "format-dateTime($d,\"[M]-[D]-[Y]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:dateTime('2003-09-07T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9-7-2003")
    );
  }

  /**
   * test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime001c() {
    final XQuery query = new XQuery(
      "format-dateTime($d,\"[D]-[M]-[Y]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:dateTime('2003-09-07T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7-9-2003")
    );
  }

  /**
   * test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime001d() {
    final XQuery query = new XQuery(
      "format-dateTime($d,\"[D1] [MI] [Y]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:dateTime('2003-09-07T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7 IX 2003")
    );
  }

  /**
   * test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime001e() {
    final XQuery query = new XQuery(
      "format-dateTime($d,\"[[[Y]-[M01]-[D01]]]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:dateTime('2003-09-07T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "[2003-09-07]")
    );
  }

  /**
   * test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime001f() {
    final XQuery query = new XQuery(
      "format-dateTime($d,\"[[[Y0001]-[M01]-[D01]]]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:dateTime('2003-09-07T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "[2003-09-07]")
    );
  }

  /**
   * test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime001g() {
    final XQuery query = new XQuery(
      "format-dateTime($d,\"([Y01]-[M01]-[D01])\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:dateTime('2003-09-07T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(03-09-07)")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002a() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H01]:[m01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "09:15")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002b() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002c() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H01]:[m01]:[s01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "09:15:06")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002d() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002e() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:6")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002f() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s01]:[f001]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06:456")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002g() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f,1-1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.5")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002h() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f1,1-1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.5")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002i() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.46")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime002j() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f001]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.456")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003L() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:6")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003a() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[Y]-[M01]-[D]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2003-09-7")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003b() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[M]-[D]-[Y]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9-7-2003")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003c() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[D]-[M]-[Y]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7-9-2003")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003d() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[D1] [MI] [Y]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "7 IX 2003")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003e() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[[[Y]-[M01]-[D01]]]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "[2003-09-07]")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003f() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[[[Y0001]-[M01]-[D01]]]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "[2003-09-07]")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003g() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"([Y01]-[M01]-[D01])\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(03-09-07)")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003h() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H01]:[m01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "09:15")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003i() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003j() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H01]:[m01]:[s01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "09:15:06")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003k() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003m() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f,1-1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.5")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003n() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f1,1-1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.5")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003p() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.46")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003q() {
    final XQuery query = new XQuery(
      "format-dateTime($t,\"[H]:[m]:[s].[f001]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "9:15:06.456")
    );
  }

  /**
   * Test format-dateTime: basic numeric formats.
   */
  @org.junit.Test
  public void formatDateTime003r() {
    final XQuery query = new XQuery(
      "format-dateTime($t,'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01].[f001]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2003-09-07T09:15:06.456")
    );
  }

  /**
   * Test format-dateTime: 12-hour clock.
   */
  @org.junit.Test
  public void formatDateTime004() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 24 return\n" +
      "        format-dateTime($t + xs:dayTimeDuration('PT1H')*$i, '[h].[m]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-09-07T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "10.15 11.15 12.15 1.15 2.15 3.15 4.15 5.15 6.15 7.15 8.15 9.15 10.15 11.15 12.15 \n         1.15 2.15 3.15 4.15 5.15 6.15 7.15 8.15 9.15")
    );
  }

  /**
   * Test format-dateTime: upper-case roman numerals for year.
   */
  @org.junit.Test
  public void formatDateTime005() {
    final XQuery query = new XQuery(
      "\n" +
      "        string-join(\n" +
      "          for $i in 1 to 100 return\n" +
      "          format-dateTime($t + xs:yearMonthDuration('P1Y')*$i, '[YI]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('1950-01-01T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            MCMLI; MCMLII; MCMLIII; MCMLIV; MCMLV; MCMLVI; MCMLVII; MCMLVIII; MCMLIX; MCMLX;             MCMLXI; MCMLXII; MCMLXIII; MCMLXIV; MCMLXV; MCMLXVI; MCMLXVII; MCMLXVIII;             MCMLXIX; MCMLXX; MCMLXXI; MCMLXXII; MCMLXXIII; MCMLXXIV; MCMLXXV; MCMLXXVI;             MCMLXXVII; MCMLXXVIII; MCMLXXIX; MCMLXXX; MCMLXXXI; MCMLXXXII; MCMLXXXIII;             MCMLXXXIV; MCMLXXXV; MCMLXXXVI; MCMLXXXVII; MCMLXXXVIII; MCMLXXXIX; MCMXC;             MCMXCI; MCMXCII; MCMXCIII; MCMXCIV; MCMXCV; MCMXCVI; MCMXCVII; MCMXCVIII;             MCMXCIX; MM; MMI; MMII; MMIII; MMIV; MMV; MMVI; MMVII; MMVIII; MMIX; MMX; MMXI; MMXII;             MMXIII; MMXIV; MMXV; MMXVI; MMXVII; MMXVIII; MMXIX; MMXX; MMXXI; MMXXII; MMXXIII; MMXXIV; MMXXV;             MMXXVI; MMXXVII; MMXXVIII; MMXXIX; MMXXX; MMXXXI; MMXXXII; MMXXXIII; MMXXXIV; MMXXXV;             MMXXXVI; MMXXXVII; MMXXXVIII; MMXXXIX; MMXL; MMXLI; MMXLII; MMXLIII; MMXLIV; MMXLV; MMXLVI;             MMXLVII; MMXLVIII; MMXLIX; MML\n        ")
    );
  }

  /**
   * Test format-dateTime: lower-case roman numerals for year (width specifier ignored).
   */
  @org.junit.Test
  public void formatDateTime006() {
    final XQuery query = new XQuery(
      "\n" +
      "        string-join(\n" +
      "          for $i in 1 to 100 return\n" +
      "          format-dateTime($t + xs:yearMonthDuration('P17Y')*$i, '[Yi,4-4]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0800-01-01T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            dcccxvii; dcccxxxiv; dcccli; dccclxviii; dccclxxxv; cmii; cmxix; cmxxxvi; cmliii;             cmlxx; cmlxxxvii; miv; mxxi; mxxxviii; mlv; mlxxii; mlxxxix; mcvi; mcxxiii; mcxl; mclvii;             mclxxiv; mcxci; mccviii; mccxxv; mccxlii; mcclix; mcclxxvi; mccxciii; mcccx; mcccxxvii; mcccxliv;             mccclxi; mccclxxviii; mcccxcv; mcdxii; mcdxxix; mcdxlvi; mcdlxiii; mcdlxxx; mcdxcvii; mdxiv;             mdxxxi; mdxlviii; mdlxv; mdlxxxii; mdxcix; mdcxvi; mdcxxxiii; mdcl; mdclxvii; mdclxxxiv; mdcci;             mdccxviii; mdccxxxv; mdcclii; mdcclxix; mdcclxxxvi; mdccciii; mdcccxx; mdcccxxxvii; mdcccliv;             mdccclxxi; mdccclxxxviii; mcmv; mcmxxii; mcmxxxix; mcmlvi; mcmlxxiii; mcmxc; mmvii; mmxxiv;             mmxli; mmlviii; mmlxxv; mmxcii; mmcix; mmcxxvi; mmcxliii; mmclx; mmclxxvii; mmcxciv; mmccxi;             mmccxxviii; mmccxlv; mmcclxii; mmcclxxix; mmccxcvi; mmcccxiii; mmcccxxx; mmcccxlvii; mmccclxiv;             mmccclxxxi; mmcccxcviii; mmcdxv; mmcdxxxii; mmcdxlix; mmcdlxvi; mmcdlxxxiii; mmd\n        ")
    );
  }

  /**
   * Test format-dateTime: test format-dateTime: ISO week numbers Specifically, in the ISO calendar the days of the week are numbered from 1 (Monday) to 7 (Sunday), and week 1 in any calendar year is the week (from Monday to Sunday) that includes the first Thursday of that year. .
   */
  @org.junit.Test
  public void formatDateTime009() {
    final XQuery query = new XQuery(
      "for $i in 1 to 48,\n" +
      "                $d in $t + xs:yearMonthDuration('P1M')*$i\n" +
      "            return concat(\"[\", $d, \": \", format-dateTime($d, '[W]', (), 'ISO', ()), \"]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-12-01T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            [2004-01-01T12:00:00: 1] [2004-02-01T12:00:00: 5] [2004-03-01T12:00:00: 10] [2004-04-01T12:00:00: 14] \n            [2004-05-01T12:00:00: 18] [2004-06-01T12:00:00: 23] [2004-07-01T12:00:00: 27] [2004-08-01T12:00:00: 31] \n            [2004-09-01T12:00:00: 36] [2004-10-01T12:00:00: 40] [2004-11-01T12:00:00: 45] [2004-12-01T12:00:00: 49] \n            [2005-01-01T12:00:00: 53] [2005-02-01T12:00:00: 5] [2005-03-01T12:00:00: 9] [2005-04-01T12:00:00: 13] \n            [2005-05-01T12:00:00: 17] [2005-06-01T12:00:00: 22] [2005-07-01T12:00:00: 26] [2005-08-01T12:00:00: 31] \n            [2005-09-01T12:00:00: 35] [2005-10-01T12:00:00: 39] [2005-11-01T12:00:00: 44] [2005-14-01T12:00:00: 48] \n            [2006-01-01T12:00:00: 52] [2006-02-01T12:00:00: 5] [2006-03-01T12:00:00: 9] [2006-04-01T12:00:00: 13] \n            [2006-05-01T12:00:00: 18] [2006-06-01T12:00:00: 22] [2006-07-01T12:00:00: 26] [2006-08-01T12:00:00: 31] \n            [2006-09-01T12:00:00: 35] [2006-10-01T12:00:00: 39] [2006-11-01T12:00:00: 44] [2006-12-01T12:00:00: 48] \n            [2007-01-01T12:00:00: 1] [2007-02-01T12:00:00: 5] [2007-03-01T12:00:00: 9] [2007-04-01T12:00:00: 13] \n            [2007-05-01T12:00:00: 18] [2007-06-01T12:00:00: 22] [2007-07-01T12:00:00: 26] [2007-08-01T12:00:00: 31] \n            [2007-09-01T12:00:00: 35] [2007-10-01T12:00:00: 40] [2007-11-01T12:00:00: 44] [2007-12-01T12:00:00: 48]\n        ")
    );
  }

  /**
   * test format-dateTime: ISO day within week Specifically, in the ISO calendar the days of the week are numbered from 1 (Monday) to 7 (Sunday), and week 1 in any calendar year is the week (from Monday to Sunday) that includes the first Thursday of that year. .
   */
  @org.junit.Test
  public void formatDateTime010() {
    final XQuery query = new XQuery(
      "for $i in 1 to 48,\n" +
      "                $d in $t + xs:yearMonthDuration('P1M')*$i\n" +
      "            return concat(\"[\", $d, \": \", format-dateTime($d, '[F01]', (), 'ISO', ()))",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2003-12-01T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            [2004-01-01T12:00:00: 04 [2004-02-01T12:00:00: 07 [2004-03-01T12:00:00: 01 [2004-04-01T12:00:00: 04 \n            [2004-05-01T12:00:00: 06 [2004-06-01T12:00:00: 02 [2004-07-01T12:00:00: 04 [2004-08-01T12:00:00: 07 \n            [2004-09-01T12:00:00: 03 [2004-10-01T12:00:00: 05 [2004-11-01T12:00:00: 01 [2004-12-01T12:00:00: 03 \n            [2005-01-01T12:00:00: 06 [2005-02-01T12:00:00: 02 [2005-03-01T12:00:00: 02 [2005-04-01T12:00:00: 05 \n            [2005-05-01T12:00:00: 07 [2005-06-01T12:00:00: 03 [2005-07-01T12:00:00: 05 [2005-08-01T12:00:00: 01 \n            [2005-09-01T12:00:00: 04 [2005-10-01T12:00:00: 06 [2005-11-01T12:00:00: 02 [2005-14-01T12:00:00: 04 \n            [2006-01-01T12:00:00: 07 [2006-02-01T12:00:00: 03 [2006-03-01T12:00:00: 03 [2006-04-01T12:00:00: 06 \n            [2006-05-01T12:00:00: 01 [2006-06-01T12:00:00: 04 [2006-07-01T12:00:00: 06 [2006-08-01T12:00:00: 02 \n            [2006-09-01T12:00:00: 05 [2006-10-01T12:00:00: 07 [2006-11-01T12:00:00: 03 [2006-12-01T12:00:00: 05 \n            [2007-01-01T12:00:00: 01 [2007-02-01T12:00:00: 04 [2007-03-01T12:00:00: 04 [2007-04-01T12:00:00: 07 \n            [2007-05-01T12:00:00: 02 [2007-06-01T12:00:00: 05 [2007-07-01T12:00:00: 07 [2007-08-01T12:00:00: 03 \n            [2007-09-01T12:00:00: 06 [2007-10-01T12:00:00: 01 [2007-11-01T12:00:00: 04 [2007-12-01T12:00:00: 06\n         ")
    );
  }

  /**
   * test format-dateTime: ISO week number within month --> Specifically, in the ISO calendar the days of the week are numbered from 1 (Monday) to 7 (Sunday), and week 1 in any calendar year is the week (from Monday to Sunday) that includes the first Thursday of that year. .
   */
  @org.junit.Test
  public void formatDateTime011() {
    final XQuery query = new XQuery(
      "for $i in 1 to 48,\n" +
      "                $d in $t + xs:yearMonthDuration('P1M')*$i\n" +
      "            return concat(\"[\", $d, \": \", format-dateTime($d, '[w]', (), 'ISO', ()))",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2005-14-01T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            [2006-01-01T12:00:00: 0 [2006-02-01T12:00:00: 1 [2006-03-01T12:00:00: 1 [2006-04-01T12:00:00: 0 \n            [2006-05-01T12:00:00: 1 [2006-06-01T12:00:00: 1 [2006-07-01T12:00:00: 0 [2006-08-01T12:00:00: 1 \n            [2006-09-01T12:00:00: 0 [2006-10-01T12:00:00: 0 [2006-11-01T12:00:00: 1 [2006-12-01T12:00:00: 0 \n            [2007-01-01T12:00:00: 1 [2007-02-01T12:00:00: 1 [2007-03-01T12:00:00: 1 [2007-04-01T12:00:00: 0 \n            [2007-05-01T12:00:00: 1 [2007-06-01T12:00:00: 0 [2007-07-01T12:00:00: 0 [2007-08-01T12:00:00: 1 \n            [2007-09-01T12:00:00: 0 [2007-10-01T12:00:00: 1 [2007-11-01T12:00:00: 1 [2007-12-01T12:00:00: 0 \n            [2008-01-01T12:00:00: 1 [2008-02-01T12:00:00: 0 [2008-03-01T12:00:00: 0 [2008-04-01T12:00:00: 1 \n            [2008-05-01T12:00:00: 1 [2008-06-01T12:00:00: 0 [2008-07-01T12:00:00: 1 [2008-08-01T12:00:00: 0 \n            [2008-09-01T12:00:00: 1 [2008-10-01T12:00:00: 1 [2008-11-01T12:00:00: 0 [2008-12-01T12:00:00: 1 \n            [2009-01-01T12:00:00: 1 [2009-02-01T12:00:00: 0 [2009-03-01T12:00:00: 0 [2009-04-01T12:00:00: 1 \n            [2009-05-01T12:00:00: 0 [2009-06-01T12:00:00: 1 [2009-07-01T12:00:00: 1 [2009-08-01T12:00:00: 0 \n            [2009-09-01T12:00:00: 1 [2009-10-01T12:00:00: 1 [2009-11-01T12:00:00: 0 [2009-12-01T12:00:00: 1\n         ")
    );
  }

  /**
   * test format-dateTime(): a,b,c... numbering sequence (not actually useful but allowed by the spec... .
   */
  @org.junit.Test
  public void formatDateTime012() {
    final XQuery query = new XQuery(
      "for $i in 1 to 60 return\n" +
      "            format-dateTime($t + xs:dayTimeDuration('PT61S')*$i, '[mA].[sa]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('2011-07-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            P.g Q.h R.i S.j T.k U.l V.m W.n X.o Y.p Z.q AA.r AB.s AC.t AD.u AE.v AF.w AG.x AH.y AI.z AJ.aa \n            AK.ab AL.ac AM.ad AN.ae AO.af AP.ag AQ.ah AR.ai AS.aj AT.ak AU.al AV.am AW.an AX.ao AY.ap AZ.aq \n            BA.ar BB.as BC.at BD.au BE.av BF.aw BG.ax \n            0.ay A.az B.ba C.bb D.bc E.bd F.be G.bf H.bg J.0 K.a L.b M.c N.d O.e P.f\n         ")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013L() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[M,1-*]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013a() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[Y,4-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0985")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013b() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[Y,3-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "985")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013c() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[Y,2-5]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "985")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013d() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[Y,2-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "85")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013e() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[Y,2-*]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "985")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013f() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[Y,*-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "985")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013g() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[Y,3]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "985")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013h() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[M,4-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0003")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013i() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[M,1-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013j() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[M,2-5]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "03")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013k() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[M,2-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "03")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013m() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[M,*-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013n() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[M,3]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "003")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013p() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[f,4-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4560")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013q() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[f,1-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "456")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013r() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[f,2-5]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "456")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013s() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[f,2-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "46")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013t() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[f,1-*]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "456")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013u() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[f,*-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "46")
    );
  }

  /**
   * test format-dateTime(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatDateTime013v() {
    final XQuery query = new XQuery(
      "format-dateTime($t, '[f,3]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "456")
    );
  }

  /**
   * test format-dateTime(): timezones in +nn:nn notation.
   */
  @org.junit.Test
  public void formatDateTime014() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-dateTime(adjust-dateTime-to-timezone(\n" +
      "                   $t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][Z]'), '; ')\n" +
      "      ",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            0715-14:00; 0745-13:30; 0815-13:00; 0845-12:30; 0915-12:00; 0945-11:30; 1015-11:00; 1045-10:30; 1115-10:00; \n            1145-09:30; 1215-09:00; 1245-08:30; 0115-08:00; 0145-07:30; 0215-07:00; 0245-06:30; 0315-06:00; 0345-05:30;\n            0415-05:00; 0445-04:30; 0515-04:00; 0545-03:30; 0615-03:00; 0645-02:30; 0715-02:00; 0745-01:30; 0815-01:00;\n            0845-00:30; 0915+00:00; 0945+00:30; 1015+01:00; 1045+01:30; 1115+02:00; 1145+02:30; 1215+03:00; 1245+03:30;\n            0115+04:00; 0145+04:30; 0215+05:00; 0245+05:30; 0315+06:00; 0345+06:30; 0415+07:00; 0445+07:30; 0515+08:00;\n            0545+08:30; 0615+09:00; 0645+09:30; 0715+10:00; 0745+10:30; 0815+11:00; 0845+11:30; 0915+12:00; 0945+12:30;\n            1015+13:00; 1045+13:30; 1115+14:00\n         ")
    );
  }

  /**
   * test format-dateTime(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatDateTime015() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-dateTime(adjust-dateTime-to-timezone(\n" +
      "                          $t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z0]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            0715GMT-14; 0745GMT-13:30; 0815GMT-13; 0845GMT-12:30; 0915GMT-12; 0945GMT-11:30; 1015GMT-11; 1045GMT-10:30;\n            1115GMT-10; 1145GMT-9:30; 1215GMT-9; 1245GMT-8:30; 0115GMT-8; 0145GMT-7:30; 0215GMT-7; 0245GMT-6:30; 0315GMT-6;\n            0345GMT-5:30; 0415GMT-5; 0445GMT-4:30; 0515GMT-4; 0545GMT-3:30; 0615GMT-3; 0645GMT-2:30; 0715GMT-2; 0745GMT-1:30;\n            0815GMT-1; 0845GMT-0:30; 0915GMT+0; 0945GMT+0:30; 1015GMT+1; 1045GMT+1:30; 1115GMT+2; 1145GMT+2:30; 1215GMT+3; \n            1245GMT+3:30; 0115GMT+4; 0145GMT+4:30; 0215GMT+5; 0245GMT+5:30; 0315GMT+6; 0345GMT+6:30; 0415GMT+7; 0445GMT+7:30;             \n            0515GMT+8; 0545GMT+8:30; 0615GMT+9; 0645GMT+9:30; 0715GMT+10; 0745GMT+10:30; 0815GMT+11; 0845GMT+11:30; 0915GMT+12;             \n            0945GMT+12:30; 1015GMT+13; 1045GMT+13:30; 1115GMT+14\n         ")
    );
  }

  /**
   * test format-dateTime(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatDateTime016() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-dateTime(adjust-dateTime-to-timezone(\n" +
      "               $t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z00:00]'), '; ')\n" +
      "      ",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            0715GMT-14:00; 0745GMT-13:30; 0815GMT-13:00; 0845GMT-12:30; 0915GMT-12:00; 0945GMT-11:30; 1015GMT-11:00;            \n             1045GMT-10:30; 1115GMT-10:00; 1145GMT-09:30; 1215GMT-09:00; 1245GMT-08:30; 0115GMT-08:00; 0145GMT-07:30;             \n             0215GMT-07:00; 0245GMT-06:30; 0315GMT-06:00; 0345GMT-05:30; 0415GMT-05:00; 0445GMT-04:30; 0515GMT-04:00;             \n             0545GMT-03:30; 0615GMT-03:00; 0645GMT-02:30; 0715GMT-02:00; 0745GMT-01:30; 0815GMT-01:00; 0845GMT-00:30;             \n             0915GMT+00:00; 0945GMT+00:30; 1015GMT+01:00; 1045GMT+01:30; 1115GMT+02:00; 1145GMT+02:30; 1215GMT+03:00; 1245GMT+03:30;             \n             0115GMT+04:00; 0145GMT+04:30; 0215GMT+05:00; 0245GMT+05:30; 0315GMT+06:00; 0345GMT+06:30; 0415GMT+07:00;             \n             0445GMT+07:30; 0515GMT+08:00; 0545GMT+08:30; 0615GMT+09:00; 0645GMT+09:30; 0715GMT+10:00; 0745GMT+10:30;             \n             0815GMT+11:00; 0845GMT+11:30; 0915GMT+12:00; 0945GMT+12:30; 1015GMT+13:00; 1045GMT+13:30; 1115GMT+14:00\n        ")
    );
  }

  /**
   * test format-dateTime(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatDateTime017() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-dateTime(adjust-dateTime-to-timezone($t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z00]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n             0715GMT-14; 0745GMT-13:30; 0815GMT-13; 0845GMT-12:30; 0915GMT-12; 0945GMT-11:30; 1015GMT-11; 1045GMT-10:30; \n             1115GMT-10; 1145GMT-09:30; 1215GMT-09; 1245GMT-08:30; 0115GMT-08; 0145GMT-07:30; 0215GMT-07; 0245GMT-06:30;\n             0315GMT-06; 0345GMT-05:30; 0415GMT-05; 0445GMT-04:30; 0515GMT-04; 0545GMT-03:30; 0615GMT-03; 0645GMT-02:30;\n             0715GMT-02; 0745GMT-01:30; 0815GMT-01; 0845GMT-00:30; 0915GMT+00; 0945GMT+00:30; 1015GMT+01; 1045GMT+01:30; 1115GMT+02;\n             1145GMT+02:30; 1215GMT+03; 1245GMT+03:30; 0115GMT+04; 0145GMT+04:30; 0215GMT+05; 0245GMT+05:30; 0315GMT+06;\n             0345GMT+06:30; 0415GMT+07; 0445GMT+07:30; 0515GMT+08; 0545GMT+08:30; 0615GMT+09; 0645GMT+09:30; 0715GMT+10;\n             0745GMT+10:30; 0815GMT+11; 0845GMT+11:30; 0915GMT+12; 0945GMT+12:30; 1015GMT+13; 1045GMT+13:30; 1115GMT+14\n         ")
    );
  }

  /**
   * test format-dateTime(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatDateTime018() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-dateTime(adjust-dateTime-to-timezone($t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z00]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:dateTime('0985-03-01T09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            0715GMT-14; 0745GMT-13:30; 0815GMT-13; 0845GMT-12:30; 0915GMT-12; 0945GMT-11:30; 1015GMT-11; 1045GMT-10:30;\n            1115GMT-10; 1145GMT-09:30; 1215GMT-09; 1245GMT-08:30; 0115GMT-08; 0145GMT-07:30; 0215GMT-07; 0245GMT-06:30; 0315GMT-06;\n            0345GMT-05:30; 0415GMT-05; 0445GMT-04:30; 0515GMT-04; 0545GMT-03:30; 0615GMT-03; 0645GMT-02:30; 0715GMT-02; 0745GMT-01:30;\n            0815GMT-01; 0845GMT-00:30; 0915GMT+00; 0945GMT+00:30; 1015GMT+01; 1045GMT+01:30; 1115GMT+02; 1145GMT+02:30; 1215GMT+03;\n            1245GMT+03:30; 0115GMT+04; 0145GMT+04:30; 0215GMT+05; 0245GMT+05:30; 0315GMT+06; 0345GMT+06:30; 0415GMT+07; 0445GMT+07:30;\n            0515GMT+08; 0545GMT+08:30; 0615GMT+09; 0645GMT+09:30; 0715GMT+10; 0745GMT+10:30; 0815GMT+11; 0845GMT+11:30; 0915GMT+12;\n            0945GMT+12:30; 1015GMT+13; 1045GMT+13:30; 1115GMT+14\n         ")
    );
  }

  /**
   * Error FOFD1340 syntax of picture is incorrect.
   */
  @org.junit.Test
  public void formatDateTime1340err() {
    final XQuery query = new XQuery(
      "format-dateTime(current-dateTime(), '[yY]', 'en', (), ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOFD1340")
    );
  }

  /**
   * Error XTDE1340 incorrect picture string.
   */
  @org.junit.Test
  public void formatDateTime801err() {
    final XQuery query = new XQuery(
      "format-dateTime(current-dateTime(), '[bla]', 'en', (), ())",
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
        error("XTDE1340")
      ||
        error("FOFD1340")
      )
    );
  }

  /**
   * English BC/AD.
   */
  @org.junit.Test
  public void formatDateTimeEn141() {
    final XQuery query = new XQuery(
      "\n" +
      "        format-dateTime($d1, '[Y][EN]', 'en', (), ()),\n" +
      "        format-dateTime($d2, '[Y][EN]', 'en', (), ())\n" +
      "      ",
      ctx);
    try {
      query.bind("d1", new XQuery("xs:dateTime('1990-12-01T12:00:00')", ctx).value());
      query.bind("d2", new XQuery("xs:dateTime('-0055-12-01T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertDeepEq("\"1990AD\", \"55BC\"")
      ||
        assertDeepEq("\"1990CE\", \"55BCE\"")
      ||
        assertDeepEq("\"1990A.D.\", \"55B.C.\"")
      ||
        assertDeepEq("\"1990C.E.\", \"55B.C.E.\"")
      )
    );
  }

  /**
   * English AM/PM.
   */
  @org.junit.Test
  public void formatDateTimeEn142() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 0 to 23 return\n" +
      "        let $t := $b + xs:dayTimeDuration('PT1H')*$i return\n" +
      "        translate(format-dateTime($t, '[h]~[m][P]', 'en', (), ()), '.- ', '')\n" +
      "      ",
      ctx);
    try {
      query.bind("b", new XQuery("xs:dateTime('2011-07-01T00:10:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         12~10am 1~10am 2~10am 3~10am 4~10am 5~10am 6~10am 7~10am 8~10am 9~10am 10~10am 11~10am 12~10pm \n         1~10pm 2~10pm 3~10pm 4~10pm 5~10pm 6~10pm 7~10pm 8~10pm 9~10pm 10~10pm 11~10pm\n         ")
    );
  }

  /**
   * English noon/midnight. The US convention seems to be noon=AM, midnight=PM, 
   *       and as no-one except the US uses the 12-hour clock these days, we'll go with that..
   */
  @org.junit.Test
  public void formatDateTimeEn143() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 0 to 1 return\n" +
      "        let $t := $b + xs:dayTimeDuration('PT12H')*$i return\n" +
      "        translate(format-dateTime($t, '[h]~[m][P]', 'en', (), ()), '.- ', '')\n" +
      "      ",
      ctx);
    try {
      query.bind("b", new XQuery("xs:dateTime('2011-07-01T00:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "12~00am 12~00pm")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void formatDateTimeEn152() {
    final XQuery query = new XQuery(
      "format-dateTime($b, '[M01]', 'en', 'CB', ())",
      ctx);
    try {
      query.bind("b", new XQuery("xs:dateTime('2006-03-01T12:00:00')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "[Calendar: AD]03")
    );
  }

  /**
   * wrong arg input to format-date().
   */
  @org.junit.Test
  public void formatDateTimeInptEr1() {
    final XQuery query = new XQuery(
      "format-dateTime('abc', '[bla]', 'en', (), ())",
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
   * wrong number of args to format-date().
   */
  @org.junit.Test
  public void formatDateTimeInptEr2() {
    final XQuery query = new XQuery(
      "format-dateTime(current-dateTime(), '[bla]', 'en', (), (), 6)",
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
   * wrong type of args to format-date().
   */
  @org.junit.Test
  public void formatDateTimeInptEr3() {
    final XQuery query = new XQuery(
      "format-dateTime(current-dateTime(), '[bla]', 'en', (), 5)",
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
}
