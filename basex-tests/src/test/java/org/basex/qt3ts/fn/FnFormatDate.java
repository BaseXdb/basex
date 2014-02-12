package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the format-date() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFormatDate extends QT3TestSet {

  /**
   * test format-date: basic numeric formats.
   */
  @org.junit.Test
  public void formatDate001a() {
    final XQuery query = new XQuery(
      "format-date($d,\"[Y]-[M01]-[D]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
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
   * test format-date: basic numeric formats.
   */
  @org.junit.Test
  public void formatDate001b() {
    final XQuery query = new XQuery(
      "format-date($d,\"[M]-[D]-[Y]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
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
   * test format-date: basic numeric formats.
   */
  @org.junit.Test
  public void formatDate001c() {
    final XQuery query = new XQuery(
      "format-date($d,\"[D]-[M]-[Y]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
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
   * test format-date: basic numeric formats.
   */
  @org.junit.Test
  public void formatDate001d() {
    final XQuery query = new XQuery(
      "format-date($d,\"[D1] [MI] [Y]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
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
   * test format-date: basic numeric formats.
   */
  @org.junit.Test
  public void formatDate001e() {
    final XQuery query = new XQuery(
      "format-date($d,\"[[[Y]-[M01]-[D01]]]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
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
   * test format-date: basic numeric formats.
   */
  @org.junit.Test
  public void formatDate001f() {
    final XQuery query = new XQuery(
      "format-date($d,\"[[[Y0001]-[M01]-[D01]]]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
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
   * test format-date: basic numeric formats.
   */
  @org.junit.Test
  public void formatDate001g() {
    final XQuery query = new XQuery(
      "format-date($d,\"([Y01]-[M01]-[D01])\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
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
   * Test format-date: upper-case roman numerals for year.
   */
  @org.junit.Test
  public void formatDate005() {
    final XQuery query = new XQuery(
      " string-join( for $i in 1 to 100 return format-date($t + xs:yearMonthDuration('P1Y')*$i,\n" +
      "         '[YI]'), ' ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1950-01-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "MCMLI MCMLII MCMLIII MCMLIV MCMLV MCMLVI MCMLVII MCMLVIII\n            MCMLIX MCMLX MCMLXI MCMLXII MCMLXIII MCMLXIV MCMLXV MCMLXVI MCMLXVII MCMLXVIII\n            MCMLXIX MCMLXX MCMLXXI MCMLXXII MCMLXXIII MCMLXXIV MCMLXXV MCMLXXVI MCMLXXVII\n            MCMLXXVIII MCMLXXIX MCMLXXX MCMLXXXI MCMLXXXII MCMLXXXIII MCMLXXXIV MCMLXXXV\n            MCMLXXXVI MCMLXXXVII MCMLXXXVIII MCMLXXXIX MCMXC MCMXCI MCMXCII MCMXCIII\n            MCMXCIV MCMXCV MCMXCVI MCMXCVII MCMXCVIII MCMXCIX MM MMI MMII MMIII MMIV MMV\n            MMVI MMVII MMVIII MMIX MMX MMXI MMXII MMXIII MMXIV MMXV MMXVI MMXVII\n            MMXVIII MMXIX MMXX MMXXI MMXXII MMXXIII MMXXIV MMXXV MMXXVI MMXXVII MMXXVIII\n            MMXXIX MMXXX MMXXXI MMXXXII MMXXXIII MMXXXIV MMXXXV MMXXXVI MMXXXVII MMXXXVIII\n            MMXXXIX MMXL MMXLI MMXLII MMXLIII MMXLIV MMXLV MMXLVI MMXLVII MMXLVIII MMXLIX\n            MML")
    );
  }

  /**
   * Test format-date: lower-case roman numerals for year (width specifier ignored).
   */
  @org.junit.Test
  public void formatDate006() {
    final XQuery query = new XQuery(
      " string-join( for $i in 1 to 100 return format-date($t +\n" +
      "         xs:yearMonthDuration('P17Y')*$i, '[Yi,4-4]'), ' ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0800-01-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "dcccxvii dcccxxxiv dcccli dccclxviii dccclxxxv cmii cmxix\n            cmxxxvi cmliii cmlxx cmlxxxvii miv mxxi mxxxviii mlv mlxxii mlxxxix mcvi\n            mcxxiii mcxl mclvii mclxxiv mcxci mccviii mccxxv mccxlii mcclix mcclxxvi\n            mccxciii mcccx mcccxxvii mcccxliv mccclxi mccclxxviii mcccxcv mcdxii mcdxxix\n            mcdxlvi mcdlxiii mcdlxxx mcdxcvii mdxiv mdxxxi mdxlviii mdlxv mdlxxxii mdxcix\n            mdcxvi mdcxxxiii mdcl mdclxvii mdclxxxiv mdcci mdccxviii mdccxxxv mdcclii\n            mdcclxix mdcclxxxvi mdccciii mdcccxx mdcccxxxvii mdcccliv mdccclxxi\n            mdccclxxxviii mcmv mcmxxii mcmxxxix mcmlvi mcmlxxiii mcmxc mmvii mmxxiv mmxli\n            mmlviii mmlxxv mmxcii mmcix mmcxxvi mmcxliii mmclx mmclxxvii mmcxciv mmccxi\n            mmccxxviii mmccxlv mmcclxii mmcclxxix mmccxcvi mmcccxiii mmcccxxx mmcccxlvii\n            mmccclxiv mmccclxxxi mmcccxcviii mmcdxv mmcdxxxii mmcdxlix mmcdlxvi mmcdlxxxiii\n            mmd")
    );
  }

  /**
   * Test format-date: numeric formats using Thai digits.
   */
  @org.junit.Test
  public void formatDate007a() {
    final XQuery query = new XQuery(
      "format-date($d,\"[Y๐๐๐๑]-[M๐๑]-[D๑]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "๒๐๐๓-๐๙-๗")
    );
  }

  /**
   * Test format-date: numeric formats using Thai digits.
   */
  @org.junit.Test
  public void formatDate007b() {
    final XQuery query = new XQuery(
      "format-date($d,\"[M๑]-[D๑]-[Y๐๐๐๑]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "๙-๗-๒๐๐๓")
    );
  }

  /**
   * Test format-date: numeric formats using Thai digits.
   */
  @org.junit.Test
  public void formatDate007c() {
    final XQuery query = new XQuery(
      "format-date($d,\"([Y๐๑]-[M๐๑]-[D๐๑])\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(๐๓-๐๙-๐๗)")
    );
  }

  /**
   * Test format-date: numeric formats using Osmanya (non-BMP) digits.
   */
  @org.junit.Test
  public void formatDate008a() {
    final XQuery query = new XQuery(
      "format-date($d,\"[Y𐒠𐒠𐒠𐒡]-[M𐒠𐒡]-[D𐒡]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "𐒢𐒠𐒠𐒣-𐒠𐒩-𐒧")
    );
  }

  /**
   * Test format-date: numeric formats using Osmanya (non-BMP) digits.
   */
  @org.junit.Test
  public void formatDate008b() {
    final XQuery query = new XQuery(
      "format-date($d,\"[M𐒡]-[D𐒡]-[Y𐒠𐒠𐒠𐒡]\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "𐒩-𐒧-𐒢𐒠𐒠𐒣")
    );
  }

  /**
   * Test format-date: numeric formats using Osmanya (non-BMP) digits.
   */
  @org.junit.Test
  public void formatDate008c() {
    final XQuery query = new XQuery(
      "format-date($d,\"([Y𐒠𐒡]-[M𐒠𐒡]-[D𐒠𐒡])\")",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-09-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "(𐒠𐒣-𐒠𐒩-𐒠𐒧)")
    );
  }

  /**
   * Test format-date: test format-date: ISO week numbers Specifically, in the ISO calendar the days of the week are numbered from 1 (Monday) to 7 (Sunday), and week 1 in any calendar year is the week (from Monday to Sunday) that includes the first Thursday of that year..
   */
  @org.junit.Test
  public void formatDate009() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 48, $d in $t + xs:yearMonthDuration('P1M')*$i \n" +
      "        return concat(\"[\", $d, \":\", format-date($d, '[W]', (), 'ISO', ()), \"]\")\n" +
      "      ",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('2004-04-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            [2004-05-01:18] [2004-06-01:23] [2004-07-01:27] [2004-08-01:31] [2004-09-01:36] [2004-10-01:40] [2004-11-01:45] \n            [2004-12-01:49] [2005-01-01:53] [2005-02-01:5] [2005-03-01:9] [2005-04-01:13] [2005-05-01:17] [2005-06-01:22] \n            [2005-07-01:26] [2005-08-01:31] [2005-09-01:35] [2005-10-01:39] [2005-11-01:44] [2005-13-01:48] [2006-01-01:52] \n            [2006-02-01:5] [2006-03-01:9] [2006-04-01:13] [2006-05-01:18] [2006-06-01:22] [2006-07-01:26] [2006-08-01:31] \n            [2006-09-01:35] [2006-10-01:39] [2006-11-01:44] [2006-12-01:48] [2007-01-01:1] [2007-02-01:5] [2007-03-01:9] \n            [2007-04-01:13] [2007-05-01:18] [2007-06-01:22] [2007-07-01:26] [2007-08-01:31] [2007-09-01:35] [2007-10-01:40] \n            [2007-11-01:44] [2007-12-01:48] [2008-01-01:1] [2008-02-01:5] [2008-03-01:9] [2008-04-01:14]\n         ")
    );
  }

  /**
   * test format-date: ISO day within week Specifically, in the ISO calendar the days of the week are numbered from 1 (Monday) to 7 (Sunday), and week 1 in any calendar year is the week (from Monday to Sunday) that includes the first Thursday of that year..
   */
  @org.junit.Test
  public void formatDate010() {
    final XQuery query = new XQuery(
      "for $i in 1 to 48, $d in $t + xs:yearMonthDuration('P1M')*$i \n" +
      "            return concat(\"[\", $d, \":\", format-date($d, '[F01]', (), 'ISO', ()), ']')\n" +
      "      ",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('2003-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            [2004-01-01:04] [2004-02-01:07] [2004-03-01:01] [2004-04-01:04] [2004-05-01:06] [2004-06-01:02] \n            [2004-07-01:04] [2004-08-01:07] [2004-09-01:03] [2004-10-01:05] [2004-11-01:01] [2004-12-01:03] \n            [2005-01-01:06] [2005-02-01:02] [2005-03-01:02] [2005-04-01:05] [2005-05-01:07] [2005-06-01:03] \n            [2005-07-01:05] [2005-08-01:01] [2005-09-01:04] [2005-10-01:06] [2005-11-01:02] [2005-13-01:04] \n            [2006-01-01:07] [2006-02-01:03] [2006-03-01:03] [2006-04-01:06] [2006-05-01:01] [2006-06-01:04] \n            [2006-07-01:06] [2006-08-01:02] [2006-09-01:05] [2006-10-01:07] [2006-11-01:03] [2006-12-01:05] \n            [2007-01-01:01] [2007-02-01:04] [2007-03-01:04] [2007-04-01:07] [2007-05-01:02] [2007-06-01:05] \n            [2007-07-01:07] [2007-08-01:03] [2007-09-01:06] [2007-10-01:01] [2007-11-01:04] [2007-12-01:06]             \n         ")
    );
  }

  /**
   * test format-date: ISO week number within month --> Specifically, in the ISO calendar the days of the week are numbered from 1 (Monday) to 7 (Sunday), and week 1 in any calendar year is the week (from Monday to Sunday) that includes the first Thursday of that year..
   */
  @org.junit.Test
  public void formatDate011() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 48, $d in $t + xs:dayTimeDuration('P3D')*$i \n" +
      "        return concat(\"[\", $d, \":\", format-date($d, '[w]', (), 'ISO', ()), ']')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('2005-13-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            [2005-13-04:1] [2005-13-07:2] [2005-13-10:2] [2005-13-13:3] [2005-13-16:3] [2005-13-19:4] [2005-13-22:4] \n            [2005-13-25:4] [2005-13-28:5] [2005-13-31:5] [2006-01-03:1] [2006-01-06:1] [2006-01-09:2] [2006-01-12:2] \n            [2006-01-15:2] [2006-01-18:3] [2006-01-21:3] [2006-01-24:4] [2006-01-27:4] [2006-01-30:5] [2006-02-02:1] \n            [2006-02-05:1] [2006-02-08:2] [2006-02-11:2] [2006-02-14:3] [2006-02-17:3] [2006-02-20:4] [2006-02-23:4] \n            [2006-02-26:4] [2006-03-01:1] [2006-03-04:1] [2006-03-07:2] [2006-03-10:2] [2006-03-13:3] [2006-03-16:3] \n            [2006-03-19:3] [2006-03-22:4] [2006-03-25:4] [2006-03-28:5] [2006-03-31:5] [2006-04-03:1] [2006-04-06:1] \n            [2006-04-09:1] [2006-04-12:2] [2006-04-15:2] [2006-04-18:3] [2006-04-21:3] [2006-04-24:4]            \n         ")
    );
  }

  /**
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013L() {
    final XQuery query = new XQuery(
      "format-date($t, '[M,1-*]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013a() {
    final XQuery query = new XQuery(
      "format-date($t, '[Y,4-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013b() {
    final XQuery query = new XQuery(
      "format-date($t, '[Y,3-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013c() {
    final XQuery query = new XQuery(
      "format-date($t, '[Y,2-5]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013d() {
    final XQuery query = new XQuery(
      "format-date($t, '[Y,2-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013e() {
    final XQuery query = new XQuery(
      "format-date($t, '[Y,2-*]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013f() {
    final XQuery query = new XQuery(
      "format-date($t, '[Y,*-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013g() {
    final XQuery query = new XQuery(
      "format-date($t, '[Y,3]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013h() {
    final XQuery query = new XQuery(
      "format-date($t, '[M,4-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013i() {
    final XQuery query = new XQuery(
      "format-date($t, '[M,1-4]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013j() {
    final XQuery query = new XQuery(
      "format-date($t, '[M,2-5]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013k() {
    final XQuery query = new XQuery(
      "format-date($t, '[M,2-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013m() {
    final XQuery query = new XQuery(
      "format-date($t, '[M,*-2]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): effect of width specifiers.
   */
  @org.junit.Test
  public void formatDate013n() {
    final XQuery query = new XQuery(
      "format-date($t, '[M,3]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('0985-03-01')", ctx).value());
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
   * test format-date(): timezones in +nn:nn notation.
   */
  @org.junit.Test
  public void formatDate014() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[Z]'), ' ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "-14:00 -13:30 -13:00 -12:30 -12:00 -11:30 -11:00 -10:30 -10:00\n            -09:30 -09:00 -08:30 -08:00 -07:30 -07:00 -06:30 -06:00 -05:30 -05:00 -04:30\n            -04:00 -03:30 -03:00 -02:30 -02:00 -01:30 -01:00 -00:30 +00:00 +00:30 +01:00\n            +01:30 +02:00 +02:30 +03:00 +03:30 +04:00 +04:30 +05:00 +05:30 +06:00 +06:30\n            +07:00 +07:30 +08:00 +08:30 +09:00 +09:30 +10:00 +10:30 +11:00 +11:30 +12:00\n            +12:30 +13:00 +13:30 +14:00")
    );
  }

  /**
   * test format-date(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatDate015() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[z0]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "GMT-14; GMT-13:30; GMT-13; GMT-12:30; GMT-12; GMT-11:30; GMT-11;\n            GMT-10:30; GMT-10; GMT-9:30; GMT-9; GMT-8:30; GMT-8; GMT-7:30; GMT-7; GMT-6:30; GMT-6;\n            GMT-5:30; GMT-5; GMT-4:30; GMT-4; GMT-3:30; GMT-3; GMT-2:30; GMT-2; GMT-1:30; GMT-1;\n            GMT-0:30; GMT+0; GMT+0:30; GMT+1; GMT+1:30; GMT+2; GMT+2:30; GMT+3; GMT+3:30; GMT+4;\n            GMT+4:30; GMT+5; GMT+5:30; GMT+6; GMT+6:30; GMT+7; GMT+7:30; GMT+8; GMT+8:30; GMT+9;\n            GMT+9:30; GMT+10; GMT+10:30; GMT+11; GMT+11:30; GMT+12; GMT+12:30; GMT+13; GMT+13:30;\n            GMT+14")
    );
  }

  /**
   * test format-date(): timezones in GMT+x notation.
   */
  @org.junit.Test
  public void formatDate016() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[z]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         \tGMT-14:00; GMT-13:30; GMT-13:00; GMT-12:30; GMT-12:00; GMT-11:30; GMT-11:00;\n\t\t\tGMT-10:30; GMT-10:00; GMT-09:30; GMT-09:00; GMT-08:30; GMT-08:00; GMT-07:30;\n\t\t\tGMT-07:00; GMT-06:30; GMT-06:00; GMT-05:30; GMT-05:00; GMT-04:30; GMT-04:00;\n\t\t\tGMT-03:30; GMT-03:00; GMT-02:30; GMT-02:00; GMT-01:30; GMT-01:00; GMT-00:30;\n\t\t\tGMT+00:00; GMT+00:30; GMT+01:00; GMT+01:30; GMT+02:00; GMT+02:30; GMT+03:00;\n\t\t\tGMT+03:30; GMT+04:00; GMT+04:30; GMT+05:00; GMT+05:30; GMT+06:00; GMT+06:30;\n\t\t\tGMT+07:00; GMT+07:30; GMT+08:00; GMT+08:30; GMT+09:00; GMT+09:30; GMT+10:00;\n\t\t\tGMT+10:30; GMT+11:00; GMT+11:30; GMT+12:00; GMT+12:30; GMT+13:00; GMT+13:30;\n\t\t\tGMT+14:00\n\t\t ")
    );
  }

  /**
   * test format-date(): timezones in military notation.
   */
  @org.junit.Test
  public void formatDate017() {
    final XQuery query = new XQuery(
      "string-join( for $z in -12 to +12 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT1H')), '[ZZ]'), ' ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         \tY X W V U T S R Q P O N Z A B C D E F G H I K L M\n\t\t ")
    );
  }

  /**
   * test format-date(): timezone-less date in military notation.
   */
  @org.junit.Test
  public void formatDate018() {
    final XQuery query = new XQuery(
      "format-date(xs:date('1987-12-13'), '[ZZ]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "J")
    );
  }

  /**
   * test format-date(): non-integral timezone in military notation.
   */
  @org.junit.Test
  public void formatDate019() {
    final XQuery query = new XQuery(
      "format-date(xs:date('1987-12-13+05:30'), '[ZZ]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "+05:30")
    );
  }

  /**
   * test format-date(): extreme timezone in military notation.
   */
  @org.junit.Test
  public void formatDate020() {
    final XQuery query = new XQuery(
      "format-date(xs:date('1987-12-13+13:00'), '[ZZ]')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "+13:00")
    );
  }

  /**
   * test format-date(): timezones with an alternate separator.
   */
  @org.junit.Test
  public void formatDate021() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[z00~00]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         \tGMT-14~00; GMT-13~30; GMT-13~00; GMT-12~30; GMT-12~00; GMT-11~30; GMT-11~00; GMT-10~30; \n         \tGMT-10~00; GMT-09~30; GMT-09~00; GMT-08~30; GMT-08~00; GMT-07~30; GMT-07~00; GMT-06~30; \n         \tGMT-06~00; GMT-05~30; GMT-05~00; GMT-04~30; GMT-04~00; GMT-03~30; GMT-03~00; GMT-02~30; \n         \tGMT-02~00; GMT-01~30; GMT-01~00; GMT-00~30; GMT+00~00; GMT+00~30; GMT+01~00; GMT+01~30; \n         \tGMT+02~00; GMT+02~30; GMT+03~00; GMT+03~30; GMT+04~00; GMT+04~30; GMT+05~00; GMT+05~30; \n         \tGMT+06~00; GMT+06~30; GMT+07~00; GMT+07~30; GMT+08~00; GMT+08~30; GMT+09~00; GMT+09~30; \n         \tGMT+10~00; GMT+10~30; GMT+11~00; GMT+11~30; GMT+12~00; GMT+12~30; GMT+13~00; GMT+13~30; \n         \tGMT+14~00\n\t\t ")
    );
  }

  /**
   * test format-date(): timezones with non-ASCII digits.
   */
  @org.junit.Test
  public void formatDate022() {
    final XQuery query = new XQuery(
      "format-date(xs:date('2012-05-18+05:30'), '[Z٠٠:٠٠]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "+٠٥:٣٠")
    );
  }

  /**
   * test format-date(): timezones with three-digit format.
   */
  @org.junit.Test
  public void formatDate023() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[Z0:01]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         \t-14:00; -13:30; -13:00; -12:30; -12:00; -11:30; -11:00; -10:30; -10:00; -9:30; -9:00;\n         \t-8:30; -8:00; -7:30; -7:00; -6:30; -6:00; -5:30; -5:00; -4:30; -4:00; -3:30; -3:00;\n         \t-2:30; -2:00; -1:30; -1:00; -0:30; +0:00; +0:30; +1:00; +1:30; +2:00; +2:30; +3:00;\n         \t+3:30; +4:00; +4:30; +5:00; +5:30; +6:00; +6:30; +7:00; +7:30; +8:00; +8:30; +9:00;\n         \t+9:30; +10:00; +10:30; +11:00; +11:30; +12:00; +12:30; +13:00; +13:30; +14:00\n\t\t ")
    );
  }

  /**
   * test format-date(): timezones with three-digit format, no separator.
   */
  @org.junit.Test
  public void formatDate024() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[Z999]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         \t-1400; -1330; -1300; -1230; -1200; -1130; -1100; -1030; -1000; -930; -900; -830; \n         \t-800; -730; -700; -630; -600; -530; -500; -430; -400; -330; -300; -230; -200; \n         \t-130; -100; -030; +000; +030; +100; +130; +200; +230; +300; +330; +400; +430; \n         \t+500; +530; +600; +630; +700; +730; +800; +830; +900; +930; +1000; +1030; +1100; \n         \t+1130; +1200; +1230; +1300; +1330; +1400\n\t\t ")
    );
  }

  /**
   * test format-date(): timezones with two-digit format, no separator.
   */
  @org.junit.Test
  public void formatDate025() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[Z99]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         \t-14; -13:30; -13; -12:30; -12; -11:30; -11; -10:30; -10; -09:30; -09; -08:30; -08;\n         \t-07:30; -07; -06:30; -06; -05:30; -05; -04:30; -04; -03:30; -03; -02:30; -02; -01:30;\n         \t-01; -00:30; +00; +00:30; +01; +01:30; +02; +02:30; +03; +03:30; +04; +04:30; +05;\n         \t+05:30; +06; +06:30; +07; +07:30; +08; +08:30; +09; +09:30; +10; +10:30; +11; +11:30;\n         \t+12; +12:30; +13; +13:30; +14\n\t\t ")
    );
  }

  /**
   * test format-date(): timezones with one-digit format, no separator, t modifier.
   */
  @org.junit.Test
  public void formatDate026() {
    final XQuery query = new XQuery(
      "string-join( for $z in -28 to +28 return format-date(adjust-date-to-timezone($t,\n" +
      "         $z*xs:dayTimeDuration('PT30M')), '[Z0t]'), '; ')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n         \t-14; -13:30; -13; -12:30; -12; -11:30; -11; -10:30; -10; -9:30; -9; -8:30; -8;\n         \t-7:30; -7; -6:30; -6; -5:30; -5; -4:30; -4; -3:30; -3; -2:30; -2; -1:30; -1; -0:30;\n         \tZ; +0:30; +1; +1:30; +2; +2:30; +3; +3:30; +4; +4:30; +5; +5:30; +6; +6:30; +7;\n         \t+7:30; +8; +8:30; +9; +9:30; +10; +10:30; +11; +11:30; +12; +12:30; +13;\n         \t+13:30; +14\n\t\t ")
    );
  }

  /**
   * test format-date(): timezones with astral plane digits.
   */
  @org.junit.Test
  public void formatDate027() {
    final XQuery query = new XQuery(
      "format-date(xs:date('2012-05-18+05:30'), '[Z𐒡:𐒠𐒡]')",
      ctx);
    try {
      query.bind("t", new XQuery("xs:date('1985-03-01Z')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "+𐒥:𐒣𐒠")
    );
  }

  /**
   * Error FOFD1340 syntax of picture is incorrect.
   */
  @org.junit.Test
  public void formatDate1340err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[yY]', 'en', (), ())",
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
  public void formatDate801err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[bla]', 'en', (), ())",
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
  public void formatDate802err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[H]', 'en', (), ())",
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
  public void formatDate803err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[h]', 'en', (), ())",
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
  public void formatDate804err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[P]', 'en', (), ())",
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
  public void formatDate805err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[m]', 'en', (), ())",
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
  public void formatDate806err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[s]', 'en', (), ())",
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
  public void formatDate807err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[f]', 'en', (), ())",
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
   * Error XTDE1340 incorrect picture string.
   */
  @org.junit.Test
  public void formatDate808err() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[bla]', 'en', (), ())",
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
   * English month names.
   */
  @org.junit.Test
  public void formatDateEn101() {
    final XQuery query = new XQuery(
      "for $i in 1 to 12 return let $d2 := $d + xs:yearMonthDuration('P1M')*$i return\n" +
      "         format-date($d2, '[MN]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            JANUARY FEBRUARY MARCH APRIL MAY JUNE JULY AUGUST SEPTEMBER OCTOBER NOVEMBER DECEMBER\n         ")
    );
  }

  /**
   * English month names.
   */
  @org.junit.Test
  public void formatDateEn102() {
    final XQuery query = new XQuery(
      "for $i in 1 to 12 return let $d2 := $d + xs:yearMonthDuration('P1M')*$i return\n" +
      "         format-date($d2, '[Mn]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            january february march april may june july august september october november december\n         ")
    );
  }

  /**
   * English month names.
   */
  @org.junit.Test
  public void formatDateEn103() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 12 \n" +
      "        return let $d2 := $d + xs:yearMonthDuration('P1M')*$i \n" +
      "        return format-date($d2, '[MNn]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            January February March April May June July August September October November December\n         ")
    );
  }

  /**
   * English month names.
   */
  @org.junit.Test
  public void formatDateEn104() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 12 \n" +
      "        return let $d2 := $d + xs:yearMonthDuration('P1M')*$i \n" +
      "        return format-date($d2, '[MN,3-3]', 'en', (), ()) \n" +
      "      ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "JAN FEB MAR APR MAY JUN JUL AUG SEP OCT NOV DEC")
    );
  }

  /**
   * English month names.
   */
  @org.junit.Test
  public void formatDateEn105() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 12 \n" +
      "        return let $d2 := $d + xs:yearMonthDuration('P1M')*$i \n" +
      "        return format-date($d2, '[Mn,3-3]', 'en', (), ()) \n" +
      "      ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "jan feb mar apr may jun jul aug sep oct nov dec")
    );
  }

  /**
   * English month names.
   */
  @org.junit.Test
  public void formatDateEn106() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $i in 1 to 12 \n" +
      "        return let $d2 := $d + xs:yearMonthDuration('P1M')*$i \n" +
      "        return format-date($d2, '[MNn,3-3]', 'en', (), ()) \n" +
      "      ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec")
    );
  }

  /**
   * English weekday names.
   */
  @org.junit.Test
  public void formatDateEn111() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return\n" +
      "         format-date($d2, '[FN]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n             MONDAY TUESDAY WEDNESDAY THURSDAY FRIDAY SATURDAY SUNDAY\n         ")
    );
  }

  /**
   * English weekday names.
   */
  @org.junit.Test
  public void formatDateEn112() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return\n" +
      "         format-date($d2, '[Fn]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n             monday tuesday wednesday thursday friday saturday sunday\n         ")
    );
  }

  /**
   * English weekday names.
   */
  @org.junit.Test
  public void formatDateEn113() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return\n" +
      "         format-date($d2, '[FNn]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            Monday Tuesday Wednesday Thursday Friday Saturday Sunday\n         ")
    );
  }

  /**
   * English weekday names.
   */
  @org.junit.Test
  public void formatDateEn114() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return\n" +
      "         format-date($d2, '[FN,3-3]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "MON TUE WED THU FRI SAT SUN")
    );
  }

  /**
   * English weekday names.
   */
  @org.junit.Test
  public void formatDateEn115() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return\n" +
      "         format-date($d2, '[Fn,3-3]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "mon tue wed thu fri sat sun")
    );
  }

  /**
   * English weekday names.
   */
  @org.junit.Test
  public void formatDateEn116() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return\n" +
      "         format-date($d2, '[FNn,3-3]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "Mon Tue Wed Thu Fri Sat Sun")
    );
  }

  /**
   * English weekday names, abbreviated, variable length.
   */
  @org.junit.Test
  public void formatDateEn117() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return let $abb :=\n" +
      "         format-date($d2, '[FNn,3-4]', 'en', (), ()) return let $expected := ('Mon', 'Tues', 'Weds',\n" +
      "         'Thur', 'Fri', 'Sat', 'Sun') return (substring($abb, 1, 3), starts-with($expected[$i],\n" +
      "         $abb) and string-length($abb) le 4 and string-length($abb) ge 3) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            Mon true Tue true Wed true Thu true Fri true Sat true Sun true\n         ")
    );
  }

  /**
   * English weekday names, abbreviated, variable length.
   */
  @org.junit.Test
  public void formatDateEn118() {
    final XQuery query = new XQuery(
      "for $i in 1 to 7 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return let $abb :=\n" +
      "         format-date($d2, '[FNn,3-5]', 'en', (), ()) return let $expected := ('Mon', 'Tues', 'Weds',\n" +
      "         'Thurs', 'Fri', 'Sat', 'Sun') return (substring($abb, 1, 3), starts-with($expected[$i],\n" +
      "         $abb) and string-length($abb) le 5 and string-length($abb) ge 3) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-07')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            Mon true Tue true Wed true Thu true Fri true Sat true Sun true\n         ")
    );
  }

  /**
   * English ordinal numbers 1-31.
   */
  @org.junit.Test
  public void formatDateEn121() {
    final XQuery query = new XQuery(
      "for $i in 0 to 30 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i return\n" +
      "         format-date($d2, '[D1o]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('2003-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            1st 2nd 3rd 4th 5th 6th 7th 8th 9th 10th 11th 12th 13th 14th 15th\n            16th 17th 18th 19th 20th 21st 22nd 23rd 24th 25th 26th 27th 28th 29th 30th\n            31st\n         ")
    );
  }

  /**
   * English ordinal numbers 1990-2020.
   */
  @org.junit.Test
  public void formatDateEn122() {
    final XQuery query = new XQuery(
      "for $i in 0 to 30 return let $d2 := $d + xs:yearMonthDuration('P1Y')*$i return\n" +
      "         format-date($d2, '[Y1o]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            1990th 1991st 1992nd 1993rd 1994th 1995th 1996th 1997th 1998th 1999th\n            2000th 2001st 2002nd 2003rd 2004th 2005th 2006th 2007th 2008th 2009th 2010th 2011th\n            2012th 2013th 2014th 2015th 2016th 2017th 2018th 2019th 2020th\n         ")
    );
  }

  /**
   * English cardinal words 1-31 upper case.
   */
  @org.junit.Test
  public void formatDateEn123() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i\n" +
      "         return format-date($d2, '[DW]', 'en', (), ()), ' ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            ONE TWO THREE FOUR FIVE SIX SEVEN EIGHT NINE TEN ELEVEN\n            TWELVE THIRTEEN FOURTEEN FIFTEEN SIXTEEN SEVENTEEN EIGHTEEN NINETEEN TWENTY\n            TWENTY ONE TWENTY TWO TWENTY THREE TWENTY FOUR TWENTY FIVE TWENTY SIX TWENTY\n            SEVEN TWENTY EIGHT TWENTY NINE THIRTY THIRTY ONE\n         ")
    );
  }

  /**
   * English cardinal words 1-31 lower case.
   */
  @org.junit.Test
  public void formatDateEn124() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i\n" +
      "         return format-date($d2, '[Dw]', 'en', (), ()), ' ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            one two three four five six seven eight nine ten eleven\n            twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty\n            twenty one twenty two twenty three twenty four twenty five twenty six twenty\n            seven twenty eight twenty nine thirty thirty one\n         ")
    );
  }

  /**
   * English cardinal words 1-31 title case.
   */
  @org.junit.Test
  public void formatDateEn125() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i\n" +
      "         return format-date($d2, '[DWw]', 'en', (), ()), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            One; Two; Three; Four; Five; Six; Seven; Eight; Nine; Ten; Eleven;\n            Twelve; Thirteen; Fourteen; Fifteen; Sixteen; Seventeen; Eighteen; Nineteen; Twenty;\n            Twenty One; Twenty Two; Twenty Three; Twenty Four; Twenty Five; Twenty Six; Twenty\n            Seven; Twenty Eight; Twenty Nine; Thirty; Thirty One\n        ")
    );
  }

  /**
   * English cardinal words 1990-2020 upper case.
   */
  @org.junit.Test
  public void formatDateEn126() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:yearMonthDuration('P1Y')*$i\n" +
      "         return format-date($d2, '[YW]', 'en', (), ()), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            ONE THOUSAND NINE HUNDRED AND NINETY; ONE THOUSAND NINE HUNDRED AND\n            NINETY ONE; ONE THOUSAND NINE HUNDRED AND NINETY TWO; ONE THOUSAND NINE HUNDRED AND\n            NINETY THREE; ONE THOUSAND NINE HUNDRED AND NINETY FOUR; ONE THOUSAND NINE HUNDRED AND\n            NINETY FIVE; ONE THOUSAND NINE HUNDRED AND NINETY SIX; ONE THOUSAND NINE HUNDRED AND\n            NINETY SEVEN; ONE THOUSAND NINE HUNDRED AND NINETY EIGHT; ONE THOUSAND NINE HUNDRED AND\n            NINETY NINE; TWO THOUSAND; TWO THOUSAND AND ONE; TWO THOUSAND AND TWO; TWO THOUSAND AND\n            THREE; TWO THOUSAND AND FOUR; TWO THOUSAND AND FIVE; TWO THOUSAND AND SIX; TWO THOUSAND\n            AND SEVEN; TWO THOUSAND AND EIGHT; TWO THOUSAND AND NINE; TWO THOUSAND AND TEN; TWO\n            THOUSAND AND ELEVEN; TWO THOUSAND AND TWELVE; TWO THOUSAND AND THIRTEEN; TWO THOUSAND\n            AND FOURTEEN; TWO THOUSAND AND FIFTEEN; TWO THOUSAND AND SIXTEEN; TWO THOUSAND AND\n            SEVENTEEN; TWO THOUSAND AND EIGHTEEN; TWO THOUSAND AND NINETEEN; TWO THOUSAND AND\n            TWENTY\n         ")
    );
  }

  /**
   * English cardinal words 1990-2020 lowerr case.
   */
  @org.junit.Test
  public void formatDateEn127() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:yearMonthDuration('P1Y')*$i\n" +
      "         return format-date($d2, '[Yw]', 'en', (), ()), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            one thousand nine hundred and ninety; one thousand nine hundred and\n            ninety one; one thousand nine hundred and ninety two; one thousand nine hundred and\n            ninety three; one thousand nine hundred and ninety four; one thousand nine hundred and\n            ninety five; one thousand nine hundred and ninety six; one thousand nine hundred and\n            ninety seven; one thousand nine hundred and ninety eight; one thousand nine hundred and\n            ninety nine; two thousand; two thousand and one; two thousand and two; two thousand and\n            three; two thousand and four; two thousand and five; two thousand and six; two thousand\n            and seven; two thousand and eight; two thousand and nine; two thousand and ten; two\n            thousand and eleven; two thousand and twelve; two thousand and thirteen; two thousand\n            and fourteen; two thousand and fifteen; two thousand and sixteen; two thousand and\n            seventeen; two thousand and eighteen; two thousand and nineteen; two thousand and\n            twenty\n         ")
    );
  }

  /**
   * English cardinal words 1990-2020 title case.
   */
  @org.junit.Test
  public void formatDateEn128() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:yearMonthDuration('P1Y')*$i\n" +
      "         return format-date($d2, '[YWw]', 'en', (), ()), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            One Thousand Nine Hundred and Ninety; One Thousand Nine Hundred and\n            Ninety One; One Thousand Nine Hundred and Ninety Two; One Thousand Nine Hundred and\n            Ninety Three; One Thousand Nine Hundred and Ninety Four; One Thousand Nine Hundred and\n            Ninety Five; One Thousand Nine Hundred and Ninety Six; One Thousand Nine Hundred and\n            Ninety Seven; One Thousand Nine Hundred and Ninety Eight; One Thousand Nine Hundred and\n            Ninety Nine; Two Thousand; Two Thousand and One; Two Thousand and Two; Two Thousand and\n            Three; Two Thousand and Four; Two Thousand and Five; Two Thousand and Six; Two Thousand\n            and Seven; Two Thousand and Eight; Two Thousand and Nine; Two Thousand and Ten; Two\n            Thousand and Eleven; Two Thousand and Twelve; Two Thousand and Thirteen; Two Thousand\n            and Fourteen; Two Thousand and Fifteen; Two Thousand and Sixteen; Two Thousand and\n            Seventeen; Two Thousand and Eighteen; Two Thousand and Nineteen; Two Thousand and\n            Twenty\n         ")
    );
  }

  /**
   * English ordinal words 1-31 upper case.
   */
  @org.junit.Test
  public void formatDateEn129() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i\n" +
      "         return translate(format-date($d2, '[DWo]', 'en', (), ()), '- ', ''), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            FIRST; SECOND; THIRD; FOURTH; FIFTH; SIXTH; SEVENTH; EIGHTH; NINTH;\n            TENTH; ELEVENTH; TWELFTH; THIRTEENTH; FOURTEENTH; FIFTEENTH; SIXTEENTH; SEVENTEENTH;\n            EIGHTEENTH; NINETEENTH; TWENTIETH; TWENTYFIRST; TWENTYSECOND; TWENTYTHIRD; TWENTYFOURTH;\n            TWENTYFIFTH; TWENTYSIXTH; TWENTYSEVENTH; TWENTYEIGHTH; TWENTYNINTH; THIRTIETH;\n            THIRTYFIRST\n         ")
    );
  }

  /**
   * English ordinal words 1-31 lower case.
   */
  @org.junit.Test
  public void formatDateEn130() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i\n" +
      "         return translate(format-date($d2, '[Dwo]', 'en', (), ()), '- ', ''), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            first; second; third; fourth; fifth; sixth; seventh; eighth; ninth;\n            tenth; eleventh; twelfth; thirteenth; fourteenth; fifteenth; sixteenth; seventeenth;\n            eighteenth; nineteenth; twentieth; twentyfirst; twentysecond; twentythird; twentyfourth;\n            twentyfifth; twentysixth; twentyseventh; twentyeighth; twentyninth; thirtieth;\n            thirtyfirst\n         ")
    );
  }

  /**
   * English ordinal words 1-31 title case.
   */
  @org.junit.Test
  public void formatDateEn131() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:dayTimeDuration('P1D')*$i\n" +
      "         return translate(format-date($d2, '[DWwo]', 'en', (), ()), '- ', ''), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "\n            First; Second; Third; Fourth; Fifth; Sixth; Seventh; Eighth; Ninth;\n            Tenth; Eleventh; Twelfth; Thirteenth; Fourteenth; Fifteenth; Sixteenth; Seventeenth;\n            Eighteenth; Nineteenth; Twentieth; TwentyFirst; TwentySecond; TwentyThird; TwentyFourth;\n            TwentyFifth; TwentySixth; TwentySeventh; TwentyEighth; TwentyNinth; Thirtieth;\n            ThirtyFirst\n         ")
    );
  }

  /**
   * English ordinal words 1990-2020 upper case.
   */
  @org.junit.Test
  public void formatDateEn132() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:yearMonthDuration('P1Y')*$i\n" +
      "         return translate(format-date($d2, '[YWo]', 'en', (), ()), '- ', ''), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "ONETHOUSANDNINEHUNDREDANDNINETIETH;\n            ONETHOUSANDNINEHUNDREDANDNINETYFIRST; ONETHOUSANDNINEHUNDREDANDNINETYSECOND;\n            ONETHOUSANDNINEHUNDREDANDNINETYTHIRD; ONETHOUSANDNINEHUNDREDANDNINETYFOURTH;\n            ONETHOUSANDNINEHUNDREDANDNINETYFIFTH; ONETHOUSANDNINEHUNDREDANDNINETYSIXTH;\n            ONETHOUSANDNINEHUNDREDANDNINETYSEVENTH; ONETHOUSANDNINEHUNDREDANDNINETYEIGHTH;\n            ONETHOUSANDNINEHUNDREDANDNINETYNINTH; TWOTHOUSANDTH; TWOTHOUSANDANDFIRST;\n            TWOTHOUSANDANDSECOND; TWOTHOUSANDANDTHIRD; TWOTHOUSANDANDFOURTH; TWOTHOUSANDANDFIFTH;\n            TWOTHOUSANDANDSIXTH; TWOTHOUSANDANDSEVENTH; TWOTHOUSANDANDEIGHTH; TWOTHOUSANDANDNINTH;\n            TWOTHOUSANDANDTENTH; TWOTHOUSANDANDELEVENTH; TWOTHOUSANDANDTWELFTH;\n            TWOTHOUSANDANDTHIRTEENTH; TWOTHOUSANDANDFOURTEENTH; TWOTHOUSANDANDFIFTEENTH;\n            TWOTHOUSANDANDSIXTEENTH; TWOTHOUSANDANDSEVENTEENTH; TWOTHOUSANDANDEIGHTEENTH;\n            TWOTHOUSANDANDNINETEENTH; TWOTHOUSANDANDTWENTIETH")
    );
  }

  /**
   * English ordinal words 1990-2020 lower case.
   */
  @org.junit.Test
  public void formatDateEn133() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:yearMonthDuration('P1Y')*$i\n" +
      "         return translate(format-date($d2, '[Ywo]', 'en', (), ()), '- ', ''), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "onethousandninehundredandninetieth;\n            onethousandninehundredandninetyfirst; onethousandninehundredandninetysecond;\n            onethousandninehundredandninetythird; onethousandninehundredandninetyfourth;\n            onethousandninehundredandninetyfifth; onethousandninehundredandninetysixth;\n            onethousandninehundredandninetyseventh; onethousandninehundredandninetyeighth;\n            onethousandninehundredandninetyninth; twothousandth; twothousandandfirst;\n            twothousandandsecond; twothousandandthird; twothousandandfourth; twothousandandfifth;\n            twothousandandsixth; twothousandandseventh; twothousandandeighth; twothousandandninth;\n            twothousandandtenth; twothousandandeleventh; twothousandandtwelfth;\n            twothousandandthirteenth; twothousandandfourteenth; twothousandandfifteenth;\n            twothousandandsixteenth; twothousandandseventeenth; twothousandandeighteenth;\n            twothousandandnineteenth; twothousandandtwentieth")
    );
  }

  /**
   * English ordinal words 1990-2020 title case.
   */
  @org.junit.Test
  public void formatDateEn134() {
    final XQuery query = new XQuery(
      " string-join( for $i in 0 to 30 return let $d2 := $d + xs:yearMonthDuration('P1Y')*$i\n" +
      "         return translate(format-date($d2, '[YWwo]', 'en', (), ()), '- ', ''), '; ') ",
      ctx);
    try {
      query.bind("d", new XQuery("xs:date('1990-12-01')", ctx).value());
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(true, "OneThousandNineHundredandNinetieth;\n            OneThousandNineHundredandNinetyFirst; OneThousandNineHundredandNinetySecond;\n            OneThousandNineHundredandNinetyThird; OneThousandNineHundredandNinetyFourth;\n            OneThousandNineHundredandNinetyFifth; OneThousandNineHundredandNinetySixth;\n            OneThousandNineHundredandNinetySeventh; OneThousandNineHundredandNinetyEighth;\n            OneThousandNineHundredandNinetyNinth; TwoThousandth; TwoThousandandFirst;\n            TwoThousandandSecond; TwoThousandandThird; TwoThousandandFourth; TwoThousandandFifth;\n            TwoThousandandSixth; TwoThousandandSeventh; TwoThousandandEighth; TwoThousandandNinth;\n            TwoThousandandTenth; TwoThousandandEleventh; TwoThousandandTwelfth;\n            TwoThousandandThirteenth; TwoThousandandFourteenth; TwoThousandandFifteenth;\n            TwoThousandandSixteenth; TwoThousandandSeventeenth; TwoThousandandEighteenth;\n            TwoThousandandNineteenth; TwoThousandandTwentieth")
    );
  }

  /**
   * English BC/AD.
   */
  @org.junit.Test
  public void formatDateEn141() {
    final XQuery query = new XQuery(
      " format-date($d1, '[Y][EN]', 'en', (), ()), format-date($d2, '[Y][EN]', 'en', (), ()) ",
      ctx);
    try {
      query.bind("d1", new XQuery("xs:date('1990-12-01')", ctx).value());
      query.bind("d2", new XQuery("xs:date('-0055-12-01')", ctx).value());
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
.
   */
  @org.junit.Test
  public void formatDateEn152() {
    final XQuery query = new XQuery(
      "format-date($b, '[M01]', 'en', 'CB', ())",
      ctx);
    try {
      query.bind("b", new XQuery("xs:date('2006-03-01')", ctx).value());
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
  public void formatDateInptEr1() {
    final XQuery query = new XQuery(
      "format-date('abc', '[bla]', 'en', (), ())",
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
  public void formatDateInptEr2() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[bla]', 'en', (), (), 6)",
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
   * wrong number of args to format-date().
   */
  @org.junit.Test
  public void formatDateInptEr3() {
    final XQuery query = new XQuery(
      "format-date(current-date(), '[bla]', 'en', (), 5)",
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
