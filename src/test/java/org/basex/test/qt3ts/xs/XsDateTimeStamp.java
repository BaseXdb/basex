package org.basex.test.qt3ts.xs;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the xs:dateTimeStamp values.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class XsDateTimeStamp extends QT3TestSet {

  /**
   * Make sure the value returned by current-dateTime() includes a timezone.
   */
  @org.junit.Test
  public void xsDateTimeStamp1() {
    final XQuery query = new XQuery(
      "exists(fn:timezone-from-dateTime(fn:current-dateTime()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("true()")
    );
  }

  /**
   * Make sure xs:dateTimeStamp() has a timezone.
   */
  @org.junit.Test
  public void xsDateTimeStamp2() {
    final XQuery query = new XQuery(
      "exists(fn:timezone-from-dateTime(xs:dateTimeStamp(\"2011-07-28T12:34:56-08:00\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("true()")
    );
  }

  /**
   * Make sure an attempt to create an xs:dateTimeStamp() w/o a timezone fails.
   */
  @org.junit.Test
  public void xsDateTimeStamp3() {
    final XQuery query = new XQuery(
      "xs:dateTimeStamp(\"2011-07-28T12:34:56\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0001")
    );
  }
}
