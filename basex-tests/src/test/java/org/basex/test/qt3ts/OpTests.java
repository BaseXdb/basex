package org.basex.test.qt3ts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.test.qt3ts.op.*;

/**
 * Test suite for the "op" test group.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  OpAddDayTimeDurationToDate.class,
  OpAddDayTimeDurationToDateTime.class,
  OpAddDayTimeDurationToTime.class,
  OpAddDayTimeDurations.class,
  OpAddYearMonthDurationToDate.class,
  OpAddYearMonthDurationToDateTime.class,
  OpAddYearMonthDurations.class,
  OpAnyURIEqual.class,
  OpAnyURIGreaterThan.class,
  OpAnyURILessThan.class,
  OpBang.class,
  OpBase64BinaryEqual.class,
  OpBooleanEqual.class,
  OpBooleanGreaterThan.class,
  OpBooleanLessThan.class,
  OpConcat.class,
  OpConcatenate.class,
  OpDateEqual.class,
  OpDateGreaterThan.class,
  OpDateLessThan.class,
  OpDateTimeEqual.class,
  OpDateTimeGreaterThan.class,
  OpDateTimeLessThan.class,
  OpDayTimeDurationGreaterThan.class,
  OpDayTimeDurationLessThan.class,
  OpDivideDayTimeDuration.class,
  OpDivideDayTimeDurationByDayTimeDuration.class,
  OpDivideYearMonthDuration.class,
  OpDivideYearMonthDurationByYearMonthDuration.class,
  OpDurationEqual.class,
  OpExcept.class,
  OpGDayEqual.class,
  OpGMonthDayEqual.class,
  OpGMonthEqual.class,
  OpGYearEqual.class,
  OpGYearMonthEqual.class,
  OpHexBinaryEqual.class,
  OpIntersect.class,
  OpIsSameNode.class,
  OpMultiplyDayTimeDuration.class,
  OpMultiplyYearMonthDuration.class,
  OpNodeAfter.class,
  OpNodeBefore.class,
  OpNumericAdd.class,
  OpNumericDivide.class,
  OpNumericEqual.class,
  OpNumericGreaterThan.class,
  OpNumericIntegerDivide.class,
  OpNumericLessThan.class,
  OpNumericMod.class,
  OpNumericMultiply.class,
  OpNumericSubtract.class,
  OpNumericUnaryMinus.class,
  OpNumericUnaryPlus.class,
  OpQNameEqual.class,
  OpStringEqual.class,
  OpStringGreaterThan.class,
  OpStringLessThan.class,
  OpSubtractDateTimes.class,
  OpSubtractDates.class,
  OpSubtractDayTimeDurationFromDate.class,
  OpSubtractDayTimeDurationFromDateTime.class,
  OpSubtractDayTimeDurationFromTime.class,
  OpSubtractDayTimeDurations.class,
  OpSubtractTimes.class,
  OpSubtractYearMonthDurationFromDate.class,
  OpSubtractYearMonthDurationFromDateTime.class,
  OpSubtractYearMonthDurations.class,
  OpTimeEqual.class,
  OpTimeGreaterThan.class,
  OpTimeLessThan.class,
  OpTo.class,
  OpUnion.class,
  OpYearMonthDurationGreaterThan.class,
  OpYearMonthDurationLessThan.class
})
public class OpTests { }
