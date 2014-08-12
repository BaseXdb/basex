package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the duration-equal operator.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpDurationEqual extends QT3TestSet {

  /**
   * Simple test of 'eq' for xs:dayTimeDuration, returning positive. .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") eq xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Testing 'eq' involving xs:dayTimeDuration with two zeroed values. .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration.  .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ3() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.043S\") eq xs:dayTimeDuration(\"P3DT08H34M12.143S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Simple test of 'ne' for xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT08H34M12.143S\") ne xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'ne' for xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ5() {
    final XQuery query = new XQuery(
      "not(xs:dayTimeDuration(\"P3DT08H34M12.143S\") ne xs:dayTimeDuration(\"P3DT08H34M12.143S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * The xs:dayTimeDuration values -PT0S and PT0S are equal. .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ6() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"-PT0S\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The xs:dayTimeDuration values -P2DT5H and P2DT5H are not equal. .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ7() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"-P2DT5H\") ne xs:dayTimeDuration(\"P2DT5H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test eq operator for xs:dayTimeDuration values with large milli second component. .
   */
  @org.junit.Test
  public void kDayTimeDurationEQ8() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P6DT16H34M59.613999S\") eq xs:dayTimeDuration(\"P6DT16H34M59.613999S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:duration, returning positive. .
   */
  @org.junit.Test
  public void kDurationEQ1() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.143S\") eq xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The xs:duration values -P1Y3M4DT08H4M33S and P1Y3M4DT08H4M33S are not equal. .
   */
  @org.junit.Test
  public void kDurationEQ10() {
    final XQuery query = new XQuery(
      "xs:duration(\"-P1Y3M4DT08H4M33S\") ne xs:duration(\"P1Y3M4DT08H4M33S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The 'lt' operator is not available between xs:duration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ11() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.142S\") lt xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  The 'le' operator is not available between xs:duration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ12() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.143S\") le xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  The 'gt' operator is not available between xs:duration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ13() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.144S\") gt xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  The 'ge' operator is not available between xs:duration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ14() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.143S\") ge xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:duration on the left hand. Both values are zero. .
   */
  @org.junit.Test
  public void kDurationEQ15() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT0S\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:duration on the right hand. Both values are zero. .
   */
  @org.junit.Test
  public void kDurationEQ16() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") eq xs:duration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:yearMonthDuration with xs:duration on the right hand. Both values are zero. .
   */
  @org.junit.Test
  public void kDurationEQ17() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0M\") eq xs:duration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:duration on the left hand. Both values are zero. .
   */
  @org.junit.Test
  public void kDurationEQ18() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT0S\") eq xs:yearMonthDuration(\"P0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:yearMonthDuration with xs:dayTimeDuration on the right hand. Both values are zero. .
   */
  @org.junit.Test
  public void kDurationEQ19() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0M\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Testing 'eq' involving xs:duration with two zeroed values. .
   */
  @org.junit.Test
  public void kDurationEQ2() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT0S\") eq xs:duration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:dayTimeDuration on the left hand. Both values are zero. .
   */
  @org.junit.Test
  public void kDurationEQ20() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") eq xs:yearMonthDuration(\"P0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:duration on the left hand. .
   */
  @org.junit.Test
  public void kDurationEQ21() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1DT2H\") eq xs:dayTimeDuration(\"P1DT2H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:duration on the right hand. .
   */
  @org.junit.Test
  public void kDurationEQ22() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P1DT2H\") eq xs:duration(\"P1DT2H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'ne' for xs:dayTimeDuration with xs:duration on the left hand. .
   */
  @org.junit.Test
  public void kDurationEQ23() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1M1DT2H\") ne xs:dayTimeDuration(\"P1DT2H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'ne' for xs:dayTimeDuration with xs:duration on the left hand. .
   */
  @org.junit.Test
  public void kDurationEQ24() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P1DT2H\") ne xs:duration(\"P1M1DT2H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:yearMonthDuration with xs:dayTimeDuration on the left hand. .
   */
  @org.junit.Test
  public void kDurationEQ25() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0M\") eq xs:dayTimeDuration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:yearMonthDuration on the right hand. .
   */
  @org.junit.Test
  public void kDurationEQ26() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"PT0S\") eq xs:yearMonthDuration(\"P0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:yearMonthDuration with xs:duration on the right hand. .
   */
  @org.junit.Test
  public void kDurationEQ27() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1Y12M\") eq xs:duration(\"P1Y12M0D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:dayTimeDuration with xs:duration on the left hand. .
   */
  @org.junit.Test
  public void kDurationEQ28() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y12M0D\") eq xs:yearMonthDuration(\"P1Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test comparing xs:duration and xs:yearMonthDuration stressing value representations are normalized properly. .
   */
  @org.junit.Test
  public void kDurationEQ29() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1M\") ne xs:duration(\"P31D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ3() {
    final XQuery query = new XQuery(
      "not(xs:duration(\"P1999Y10M3DT08H34M12.043S\") eq xs:duration(\"P1999Y10M3DT08H34M12.143S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test comparing xs:duration and xs:yearMonthDuration stressing value representations are normalized properly(with operand order switched). .
   */
  @org.junit.Test
  public void kDurationEQ30() {
    final XQuery query = new XQuery(
      "xs:duration(\"P31D\") ne xs:yearMonthDuration(\"P1M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test comparing xs:dayTimeDuration and xs:yearMonthDuration stressing value representations are normalized properly. .
   */
  @org.junit.Test
  public void kDurationEQ31() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1M\") ne xs:dayTimeDuration(\"P31D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test comparing xs:dayTimeDuration and xs:yearMonthDuration stressing value representations are normalized properly(with operand order switched). .
   */
  @org.junit.Test
  public void kDurationEQ32() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31D\") ne xs:yearMonthDuration(\"P1M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The 'ge' operator is not available between xs:duration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ33() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.143S\") ge xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'gt' operator is not available between xs:duration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ34() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.143S\") gt xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'lt' operator is not available between xs:duration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ35() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.143S\") lt xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'le' operator is not available between xs:duration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ36() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y10M3DT08H34M12.143S\") le xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'ge' operator is not available between xs:duration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ37() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") ge xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  The 'gt' operator is not available between xs:duration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ38() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") gt xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  The 'lt' operator is not available between xs:duration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ39() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") lt xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  Simple test of 'ne' for xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ4() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1999Y01M3DT08H34M12.143S\") ne xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The 'le' operator is not available between xs:duration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ40() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") le xs:duration(\"P1999Y10M3DT08H34M12.143S\")",
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
   *  The 'ge' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ41() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") ge xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'gt' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ42() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") gt xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'lt' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ43() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") lt xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'le' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration. .
   */
  @org.junit.Test
  public void kDurationEQ44() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") le xs:dayTimeDuration(\"P3DT08H34M12.143S\")",
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
   *  The 'ge' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ45() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") ge xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The 'gt' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ46() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") gt xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The 'lt' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ47() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") lt xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The 'le' operator is not available between xs:yearMonthDuration and xs:dayTimeDuration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ48() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3DT08H34M12.143S\") le xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The 'ge' operator is not available between xs:yearMonthDuration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ49() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") ge xs:duration(\"P3DT08H34M12.143S\")",
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
   *  Simple test of 'ne' for xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ5() {
    final XQuery query = new XQuery(
      "not(xs:duration(\"P1999Y10M3DT08H34M12.143S\") ne xs:duration(\"P1999Y10M3DT08H34M12.143S\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The 'gt' operator is not available between xs:yearMonthDuration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ50() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") gt xs:duration(\"P3DT08H34M12.143S\")",
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
   *  The 'lt' operator is not available between xs:yearMonthDuration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ51() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") lt xs:duration(\"P3DT08H34M12.143S\")",
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
   *  The 'le' operator is not available between xs:yearMonthDuration and xs:duration. .
   */
  @org.junit.Test
  public void kDurationEQ52() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") le xs:duration(\"P3DT08H34M12.143S\")",
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
   *  The 'ge' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ53() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") ge xs:duration(\"P3DT08H34M12.143S\")",
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
   *  The 'gt' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ54() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") gt xs:duration(\"P3DT08H34M12.143S\")",
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
   *  The 'lt' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ55() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") lt xs:duration(\"P3DT08H34M12.143S\")",
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
   *  The 'le' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ56() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") le xs:duration(\"P3DT08H34M12.143S\")",
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
   *  The 'ge' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order)(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ57() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3DT08H34M12.143S\") ge xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The 'gt' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order)(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ58() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3DT08H34M12.143S\") gt xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The 'lt' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order)(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ59() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3DT08H34M12.143S\") lt xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The xs:duration values P365D and P1Y are not equal. .
   */
  @org.junit.Test
  public void kDurationEQ6() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y\") ne xs:duration(\"P365D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The 'le' operator is not available between xs:yearMonthDuration and xs:duration(reversed operand order)(reversed operand order). .
   */
  @org.junit.Test
  public void kDurationEQ60() {
    final XQuery query = new XQuery(
      "xs:duration(\"P3DT08H34M12.143S\") le xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  The xs:duration values P12M and P1Y are equal. .
   */
  @org.junit.Test
  public void kDurationEQ7() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y\") eq xs:duration(\"P12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The xs:duration values P1M and P31D are not equal. .
   */
  @org.junit.Test
  public void kDurationEQ8() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1M\") ne xs:duration(\"P31D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The xs:duration values -PT0S and PT0S are equal. .
   */
  @org.junit.Test
  public void kDurationEQ9() {
    final XQuery query = new XQuery(
      "xs:duration(\"-PT0S\") eq xs:duration(\"PT0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:yearMonthDuration, returning positive. .
   */
  @org.junit.Test
  public void kYearMonthDurationEQ1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") eq xs:yearMonthDuration(\"P1999Y10M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Testing 'eq' involving xs:yearMonthDuration with two zeroed values. .
   */
  @org.junit.Test
  public void kYearMonthDurationEQ2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0M\") eq xs:yearMonthDuration(\"P0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'eq' for xs:yearMonthDuration. .
   */
  @org.junit.Test
  public void kYearMonthDurationEQ3() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y\") eq xs:yearMonthDuration(\"P1999Y10M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Simple test of 'ne' for xs:yearMonthDuration..
   */
  @org.junit.Test
  public void kYearMonthDurationEQ4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y01M\") ne xs:yearMonthDuration(\"P1999Y10M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Simple test of 'ne' for xs:yearMonthDuration. .
   */
  @org.junit.Test
  public void kYearMonthDurationEQ5() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y10M\") ne xs:yearMonthDuration(\"P1999Y10M\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  The xs:yearMonthDuration values -P0M and P0M are equal. .
   */
  @org.junit.Test
  public void kYearMonthDurationEQ6() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"-P3Y8M\") ne xs:yearMonthDuration(\"P3Y8M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * The xs:yearMonthDuration values -P3Y8M and +P3Y8M are not equal. .
   */
  @org.junit.Test
  public void kYearMonthDurationEQ7() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"-P3Y8M\") ne xs:yearMonthDuration(\"P3Y8M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of dayTimeDurations .
   */
  @org.junit.Test
  public void cbclDayTimeDurationEqual001() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer, $hours as xs:integer ) { xs:dayTimeDuration(concat('P', $days, 'DT', $hours, 'H')) }; not(local:dayTimeDuration(1, 1) eq xs:dayTimeDuration(\"P0D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of dayTimeDurations .
   */
  @org.junit.Test
  public void cbclDayTimeDurationEqual002() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($days as xs:integer, $hours as xs:integer ) { xs:dayTimeDuration(concat('P', $days, 'DT', $hours, 'H')) }; not(local:dayTimeDuration(1, 1) ne xs:dayTimeDuration(\"P0D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationEqual003() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; exists(local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:true()) eq xs:dayTimeDuration(\"P0D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationEqual004() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:false()) eq xs:dayTimeDuration(\"P0D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationEqual005() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; exists(local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:true()) ne xs:dayTimeDuration(\"P0D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:dayTimeDuration .
   */
  @org.junit.Test
  public void cbclDayTimeDurationEqual006() {
    final XQuery query = new XQuery(
      "declare function local:dayTimeDuration($dayTimeDuration as xs:dayTimeDuration, $null as xs:boolean) { if ($null) then () else $dayTimeDuration }; local:dayTimeDuration(xs:dayTimeDuration(\"P0D\"), fn:false()) ne xs:dayTimeDuration(\"P0D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of dayTimeDurations .
   */
  @org.junit.Test
  public void cbclDurationEqual001() {
    final XQuery query = new XQuery(
      "declare function local:duration($days as xs:integer, $hours as xs:integer) { xs:duration(concat('P', $days, 'DT', $hours, 'H')) }; not(local:duration(1, 1) eq xs:dayTimeDuration(\"P0D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of dayTimeDurations .
   */
  @org.junit.Test
  public void cbclDurationEqual002() {
    final XQuery query = new XQuery(
      "declare function local:duration($days as xs:integer, $hours as xs:integer) { xs:duration(concat('P', $days, 'DT', $hours, 'H')) }; not(local:duration(1, 1) ne xs:dayTimeDuration(\"P0D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:duration .
   */
  @org.junit.Test
  public void cbclDurationEqual003() {
    final XQuery query = new XQuery(
      "declare function local:duration($duration as xs:duration, $null as xs:boolean) { if ($null) then () else $duration }; exists(local:duration(xs:duration(\"P1DT1H\"), fn:true()) eq xs:duration(\"P1DT1H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:duration .
   */
  @org.junit.Test
  public void cbclDurationEqual004() {
    final XQuery query = new XQuery(
      "declare function local:duration($duration as xs:duration, $null as xs:boolean) { if ($null) then () else $duration }; local:duration(xs:duration(\"P1DT1H\"), fn:false()) eq xs:duration(\"P1DT1H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:duration .
   */
  @org.junit.Test
  public void cbclDurationEqual005() {
    final XQuery query = new XQuery(
      "declare function local:duration($duration as xs:duration, $null as xs:boolean) { if ($null) then () else $duration }; exists(local:duration(xs:duration(\"P1DT1H\"), fn:true()) ne xs:duration(\"P1DT1H\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:duration .
   */
  @org.junit.Test
  public void cbclDurationEqual006() {
    final XQuery query = new XQuery(
      "declare function local:duration($duration as xs:duration, $null as xs:boolean) { if ($null) then () else $duration }; local:duration(xs:duration(\"P1DT1H\"), fn:false()) ne xs:duration(\"P1DT1H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test equality of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        not(local:yearMonthDuration(1, 1) eq xs:yearMonthDuration(\"P0Y\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test equality of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationEqual002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        not(local:yearMonthDuration(1, 1) ne xs:yearMonthDuration(\"P0Y\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "yearMonthDuration" and "dayTimeDuration" data types with fn:distinct function given on example. .
   */
  @org.junit.Test
  public void distinctDurationEqual1() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:yearMonthDuration('P0Y'), xs:dayTimeDuration('P0D')))",
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
        assertStringValue(false, "P0M")
      ||
        assertStringValue(false, "PT0S")
      )
    );
  }

  /**
   *  Evaluates The "yearMonthDuration" and "dayTimeDuration" data types with fn:distinct function given on example. .
   */
  @org.junit.Test
  public void distinctDurationEqual2() {
    final XQuery query = new XQuery(
      "fn:distinct-values((xs:yearMonthDuration('P1Y'), xs:dayTimeDuration('P365D')))",
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
        assertStringValue(false, "P1Y P365D")
      ||
        assertStringValue(false, "P365D P1Y")
      )
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args1() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") eq xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args10() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ne xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args11() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") le xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:dayTimeDuration(mid range) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args12() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") le xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:dayTimeDuration(upper bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args13() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") le xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args14() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") le xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args15() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") le xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args16() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ge xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:dayTimeDuration(mid range) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args17() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") ge xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:dayTimeDuration(upper bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args18() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") ge xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args19() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ge xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(mid range) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args2() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") eq xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args20() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ge xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(upper bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args3() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") eq xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args4() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") eq xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(upper bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args5() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") eq xs:dayTimeDuration(\"P31DT23H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args6() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ne xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(mid range) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args7() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P15DT11H59M59S\") ne xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(upper bound) $arg2 = xs:dayTimeDuration(lower bound) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args8() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P31DT23H59M59S\") ne xs:dayTimeDuration(\"P0DT0H0M0S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:dayTimeDuration-equal" operator with the arguments set as follows: $arg1 = xs:dayTimeDuration(lower bound) $arg2 = xs:dayTimeDuration(mid range) .
   */
  @org.junit.Test
  public void opDayTimeDurationEqual2args9() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P0DT0H0M0S\") ne xs:dayTimeDuration(\"P15DT11H59M59S\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator as per example 1 for this operator from Functions and Operators specs. .
   */
  @org.junit.Test
  public void opDurationEqual1() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y\") eq xs:duration(\"P12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P365D" (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual10() {
    final XQuery query = new XQuery(
      "xs:duration(\"P365D\") eq xs:duration(\"P365D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P365D" (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual11() {
    final XQuery query = new XQuery(
      "xs:duration(\"P365D\") ne xs:duration(\"P365D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P36D" and "P39D" (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual12() {
    final XQuery query = new XQuery(
      "xs:duration(\"P36D\") eq xs:duration(\"P39D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P36D" and "P39D" (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual13() {
    final XQuery query = new XQuery(
      "xs:duration(\"P36D\") ne xs:duration(\"P39D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P36D" and used as argument to fn:not (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual14() {
    final XQuery query = new XQuery(
      "fn:not(xs:duration(\"P36D\") eq xs:duration(\"P36D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P36D" and used as argument to fn:not (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual15() {
    final XQuery query = new XQuery(
      "fn:not(xs:duration(\"P36D\") ne xs:duration(\"P36D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P36D" and used as argument to xs:boolean (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual16() {
    final XQuery query = new XQuery(
      "xs:boolean(xs:duration(\"P36D\") eq xs:duration(\"P36D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P36D" and used as argument to xs:boolean (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual17() {
    final XQuery query = new XQuery(
      "xs:boolean(xs:duration(\"P36D\") ne xs:duration(\"P36D\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "and" operator and "fn:true" function (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual18() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") eq xs:duration(\"P36D\")) and fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "and" operator and "fn:true" function (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual19() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") ne xs:duration(\"P36D\")) and fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator as per example 2 for this operator from Functions and Operators specs. .
   */
  @org.junit.Test
  public void opDurationEqual2() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT24H\") eq xs:duration(\"P1D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "or" operator and "fn:true" function (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual20() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") eq xs:duration(\"P36D\")) or fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "or" operator and "fn:true" function (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual21() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") ne xs:duration(\"P36D\")) or fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "and" operator and "fn:false" function (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual22() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") eq xs:duration(\"P36D\")) and fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "and" operator and "fn:false" function (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual23() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") ne xs:duration(\"P36D\")) and fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "or" operator and "fn:false" function (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual24() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") eq xs:duration(\"P36D\")) or fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator as part of boolean expression "or" operator and "fn:false" function (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual25() {
    final XQuery query = new XQuery(
      "(xs:duration(\"P36D\") ne xs:duration(\"P36D\")) or fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator as per example 4 for this operator from the F and O specs. .
   */
  @org.junit.Test
  public void opDurationEqual26() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration('P0Y') eq xs:dayTimeDuration('P0D')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator as per example 5 for this operator from the F and O specs. .
   */
  @org.junit.Test
  public void opDurationEqual27() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration('P1Y') eq xs:dayTimeDuration('P365D')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator as per example 3 for this operator from Functions and Operators specs. .
   */
  @org.junit.Test
  public void opDurationEqual3() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y\") eq xs:duration(\"P365D\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P1Y" (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual4() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y\") eq xs:duration(\"P1Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P1Y" (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual5() {
    final XQuery query = new XQuery(
      "xs:duration(\"P1Y\") ne xs:duration(\"P1Y\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "PT24H" (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual6() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT24H\") eq xs:duration(\"PT24H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "PT24H" (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual7() {
    final XQuery query = new XQuery(
      "xs:duration(\"PT24H\") ne xs:duration(\"PT24H\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P12M" (uses eq operator) .
   */
  @org.junit.Test
  public void opDurationEqual8() {
    final XQuery query = new XQuery(
      "xs:duration(\"P12M\") eq xs:duration(\"P12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of duration-equal operator with both operands set to "P12M" (uses ne operator) .
   */
  @org.junit.Test
  public void opDurationEqual9() {
    final XQuery query = new XQuery(
      "xs:duration(\"P12M\") ne xs:duration(\"P12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") eq xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(upper bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args10() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ne xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args11() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") le xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:yearMonthDuration(mid range) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args12() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") le xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:yearMonthDuration(upper bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args13() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") le xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(mid range) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args14() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") le xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (le) with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(upper bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args15() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") le xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args16() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ge xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:yearMonthDuration(mid range) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args17() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") ge xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:yearMonthDuration(upper bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args18() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") ge xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(mid range) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args19() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ge xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(mid range) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") eq xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator (ge) with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(upper bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args20() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ge xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(upper bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") eq xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(mid range) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") eq xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(upper bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") eq xs:yearMonthDuration(\"P2030Y12M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args6() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ne xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(mid range) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args7() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") ne xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(upper bound) $arg2 = xs:yearMonthDuration(lower bound) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args8() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") ne xs:yearMonthDuration(\"P0Y0M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluates The "op:yearMonthDuration-equal" operator with the arguments set as follows: $arg1 = xs:yearMonthDuration(lower bound) $arg2 = xs:yearMonthDuration(mid range) .
   */
  @org.junit.Test
  public void opYearMonthDurationEqual2args9() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") ne xs:yearMonthDuration(\"P1000Y6M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }
}
