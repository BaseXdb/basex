package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the format-time() function transferred from XSLT 2.0 to XPath 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFormatTime extends QT3TestSet {

  /**
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002a() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H01]:[m01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002b() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002c() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H01]:[m01]:[s01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002d() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]:[s]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002e() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]:[s1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002f() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]:[s01]:[f001]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002g() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]:[s].[f,1-1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002h() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]:[s].[f1,1-1]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002i() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]:[s].[f01]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: basic numeric formats.
   */
  @org.junit.Test
  public void formatTime002j() {
    final XQuery query = new XQuery(
      "format-time($t,\"[H]:[m]:[s].[f001]\")",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * Test format-time: 12-hour clock.
   */
  @org.junit.Test
  public void formatTime004() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 24 return\n" +
      "        format-time($t + xs:dayTimeDuration('PT1H')*$i, '[h].[m]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         10.15 11.15 12.15 1.15 2.15 3.15 4.15 5.15 6.15 7.15 8.15 9.15 10.15 11.15 \n         12.15 1.15 2.15 3.15 4.15 5.15 6.15 7.15 8.15 9.15\n         ")
    );
  }

  /**
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013n() {
    final XQuery query = new XQuery(
      "format-time($t, '[m,3]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "015")
    );
  }

  /**
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013p() {
    final XQuery query = new XQuery(
      "format-time($t, '[f,4-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013q() {
    final XQuery query = new XQuery(
      "format-time($t, '[f,1-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013r() {
    final XQuery query = new XQuery(
      "format-time($t, '[f,2-5]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013s() {
    final XQuery query = new XQuery(
      "format-time($t, '[f,2-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013t() {
    final XQuery query = new XQuery(
      "format-time($t, '[f,1-*]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013u() {
    final XQuery query = new XQuery(
      "format-time($t, '[f,*-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * test format-time(): effect of width specifiers .
   */
  @org.junit.Test
  public void formatTime013v() {
    final XQuery query = new XQuery(
      "format-time($t, '[f,3]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456')", ctx).value());
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
   * test format-time(): timezones in +nn:nn notation.
   */
  @org.junit.Test
  public void formatTime014() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-time(adjust-time-to-timezone(\n" +
      "               $t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][Z]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n             0715-14:00; 0745-13:30; 0815-13:00; 0845-12:30; 0915-12:00; 0945-11:30; 1015-11:00; 1045-10:30; 1115-10:00; \n             1145-09:30; 1215-09:00; 1245-08:30; 0115-08:00; 0145-07:30; 0215-07:00; 0245-06:30; 0315-06:00; 0345-05:30; \n             0415-05:00; 0445-04:30; 0515-04:00; 0545-03:30; 0615-03:00; 0645-02:30; 0715-02:00; 0745-01:30; 0815-01:00; \n             0845-00:30; 0915+00:00; 0945+00:30; 1015+01:00; 1045+01:30; 1115+02:00; 1145+02:30; 1215+03:00; 1245+03:30;\n             0115+04:00; 0145+04:30; 0215+05:00; 0245+05:30; 0315+06:00; 0345+06:30; 0415+07:00; 0445+07:30; 0515+08:00; \n             0545+08:30; 0615+09:00; 0645+09:30; 0715+10:00; 0745+10:30; 0815+11:00; 0845+11:30; 0915+12:00; 0945+12:30; \n             1015+13:00; 1045+13:30; 1115+14:00\n         ")
    );
  }

  /**
   * test format-time(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatTime015() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-time(adjust-time-to-timezone(\n" +
      "                        $t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            0715GMT-14:00; 0745GMT-13:30; 0815GMT-13:00; 0845GMT-12:30; 0915GMT-12:00; 0945GMT-11:30; 1015GMT-11:00; 1045GMT-10:30;\n            1115GMT-10:00; 1145GMT-09:30; 1215GMT-09:00; 1245GMT-08:30; 0115GMT-08:00; 0145GMT-07:30; 0215GMT-07:00; 0245GMT-06:30;\n            0315GMT-06:00; 0345GMT-05:30; 0415GMT-05:00; 0445GMT-04:30; 0515GMT-04:00; 0545GMT-03:30; 0615GMT-03:00; 0645GMT-02:30;\n            0715GMT-02:00; 0745GMT-01:30; 0815GMT-01:00; 0845GMT-00:30; 0915GMT+00:00; 0945GMT+00:30; 1015GMT+01:00; 1045GMT+01:30; 1115GMT+02:00;\n            1145GMT+02:30; 1215GMT+03:00; 1245GMT+03:30; 0115GMT+04:00; 0145GMT+04:30; 0215GMT+05:00; 0245GMT+05:30; 0315GMT+06:00;\n            0345GMT+06:30; 0415GMT+07:00; 0445GMT+07:30; 0515GMT+08:00; 0545GMT+08:30; 0615GMT+09:00; 0645GMT+09:30; 0715GMT+10:00;\n            0745GMT+10:30; 0815GMT+11:00; 0845GMT+11:30; 0915GMT+12:00; 0945GMT+12:30; 1015GMT+13:00; 1045GMT+13:30;\n            1115GMT+14:00\n         ")
    );
  }

  /**
   * test format-time(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatTime016() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-time(adjust-time-to-timezone(\n" +
      "                        $t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z,6-6]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n             0715GMT-14:00; 0745GMT-13:30; 0815GMT-13:00; 0845GMT-12:30; 0915GMT-12:00; 0945GMT-11:30; 1015GMT-11:00; 1045GMT-10:30;\n            1115GMT-10:00; 1145GMT-09:30; 1215GMT-09:00; 1245GMT-08:30; 0115GMT-08:00; 0145GMT-07:30; 0215GMT-07:00; 0245GMT-06:30;\n            0315GMT-06:00; 0345GMT-05:30; 0415GMT-05:00; 0445GMT-04:30; 0515GMT-04:00; 0545GMT-03:30; 0615GMT-03:00; 0645GMT-02:30;\n            0715GMT-02:00; 0745GMT-01:30; 0815GMT-01:00; 0845GMT-00:30; 0915GMT+00:00; 0945GMT+00:30; 1015GMT+01:00; 1045GMT+01:30; 1115GMT+02:00;\n            1145GMT+02:30; 1215GMT+03:00; 1245GMT+03:30; 0115GMT+04:00; 0145GMT+04:30; 0215GMT+05:00; 0245GMT+05:30; 0315GMT+06:00;\n            0345GMT+06:30; 0415GMT+07:00; 0445GMT+07:30; 0515GMT+08:00; 0545GMT+08:30; 0615GMT+09:00; 0645GMT+09:30; 0715GMT+10:00;\n            0745GMT+10:30; 0815GMT+11:00; 0845GMT+11:30; 0915GMT+12:00; 0945GMT+12:30; 1015GMT+13:00; 1045GMT+13:30;\n            1115GMT+14:00\n        ")
    );
  }

  /**
   * test format-time(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatTime017() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-time(adjust-time-to-timezone($t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z,5-6]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n             0715GMT-14:00; 0745GMT-13:30; 0815GMT-13:00; 0845GMT-12:30; 0915GMT-12:00; 0945GMT-11:30; 1015GMT-11:00; 1045GMT-10:30;\n            1115GMT-10:00; 1145GMT-09:30; 1215GMT-09:00; 1245GMT-08:30; 0115GMT-08:00; 0145GMT-07:30; 0215GMT-07:00; 0245GMT-06:30;\n            0315GMT-06:00; 0345GMT-05:30; 0415GMT-05:00; 0445GMT-04:30; 0515GMT-04:00; 0545GMT-03:30; 0615GMT-03:00; 0645GMT-02:30;\n            0715GMT-02:00; 0745GMT-01:30; 0815GMT-01:00; 0845GMT-00:30; 0915GMT+00:00; 0945GMT+00:30; 1015GMT+01:00; 1045GMT+01:30; 1115GMT+02:00;\n            1145GMT+02:30; 1215GMT+03:00; 1245GMT+03:30; 0115GMT+04:00; 0145GMT+04:30; 0215GMT+05:00; 0245GMT+05:30; 0315GMT+06:00;\n            0345GMT+06:30; 0415GMT+07:00; 0445GMT+07:30; 0515GMT+08:00; 0545GMT+08:30; 0615GMT+09:00; 0645GMT+09:30; 0715GMT+10:00;\n            0745GMT+10:30; 0815GMT+11:00; 0845GMT+11:30; 0915GMT+12:00; 0945GMT+12:30; 1015GMT+13:00; 1045GMT+13:30;\n            1115GMT+14:00\n         ")
    );
  }

  /**
   * test format-time(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatTime018() {
    final XQuery query = new XQuery(
      "string-join(\n" +
      "               for $z in -28 to +28\n" +
      "               return format-time(adjust-time-to-timezone($t, $z*xs:dayTimeDuration('PT30M')), '[h01][m01][z,2-6]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:time('09:15:06.456Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n              0715GMT-14:00; 0745GMT-13:30; 0815GMT-13:00; 0845GMT-12:30; 0915GMT-12:00; 0945GMT-11:30; 1015GMT-11:00; 1045GMT-10:30;\n            1115GMT-10:00; 1145GMT-09:30; 1215GMT-09:00; 1245GMT-08:30; 0115GMT-08:00; 0145GMT-07:30; 0215GMT-07:00; 0245GMT-06:30;\n            0315GMT-06:00; 0345GMT-05:30; 0415GMT-05:00; 0445GMT-04:30; 0515GMT-04:00; 0545GMT-03:30; 0615GMT-03:00; 0645GMT-02:30;\n            0715GMT-02:00; 0745GMT-01:30; 0815GMT-01:00; 0845GMT-00:30; 0915GMT+00:00; 0945GMT+00:30; 1015GMT+01:00; 1045GMT+01:30; 1115GMT+02:00;\n            1145GMT+02:30; 1215GMT+03:00; 1245GMT+03:30; 0115GMT+04:00; 0145GMT+04:30; 0215GMT+05:00; 0245GMT+05:30; 0315GMT+06:00;\n            0345GMT+06:30; 0415GMT+07:00; 0445GMT+07:30; 0515GMT+08:00; 0545GMT+08:30; 0615GMT+09:00; 0645GMT+09:30; 0715GMT+10:00;\n            0745GMT+10:30; 0815GMT+11:00; 0845GMT+11:30; 0915GMT+12:00; 0945GMT+12:30; 1015GMT+13:00; 1045GMT+13:30;\n            1115GMT+14:00\n         ")
    );
  }

  /**
   * Error FOFD1340 syntax of picture is incorrect.
   */
  @org.junit.Test
  public void formatTime1340err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[y]', 'en', (), ())",
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
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime809err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[Y]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime810err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[M]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime811err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[d]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime812err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[d]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime813err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[F]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime814err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[W]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime815err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[w]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1350 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime816err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[E]', 'en', (), ())",
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
        error("XTDE1350")
      ||
        error("FOFD1350")
      )
    );
  }

  /**
   * Error XTDE1340 component in picture string not available in value.
   */
  @org.junit.Test
  public void formatTime817err() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[bla]', 'en', (), ())",
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
   * wrong arg input to format-time().
   */
  @org.junit.Test
  public void formatTimeInptEr1() {
    final XQuery query = new XQuery(
      "format-time('abc', '[bla]', 'en', (), ())",
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
   * wrong number of args to format-time().
   */
  @org.junit.Test
  public void formatTimeInptEr2() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[bla]', 'en', (), (), 6)",
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
   * wrong number of args to format-time().
   */
  @org.junit.Test
  public void formatTimeInptEr3() {
    final XQuery query = new XQuery(
      "format-time(current-time(), '[bla]', 'en', (), (), 6)",
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
}
