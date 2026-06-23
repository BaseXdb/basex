package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.value.item.Dec.*;

import java.io.*;
import java.math.*;
import java.util.regex.*;

import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * DayTime Duration item ({@code xs:dayTimeDuration}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DTDur extends Dur {
  /**
   * Constructor.
   * @param dur duration item
   */
  public DTDur(final Dur dur) {
    super(BasicType.DAY_TIME_DURATION);
    seconds = dur.seconds == null ? BigDecimal.ZERO : dur.seconds;
  }

  /**
   * Constructor.
   * @param hours hours
   * @param minutes minutes
   */
  public DTDur(final long hours, final long minutes) {
    super(BasicType.DAY_TIME_DURATION);
    seconds = BigDecimal.valueOf(hours).multiply(BD_60).add(BigDecimal.valueOf(minutes)).
        multiply(BD_60);
  }

  /**
   * Constructor.
   * @param seconds seconds
   */
  public DTDur(final BigDecimal seconds) {
    super(BasicType.DAY_TIME_DURATION);
    this.seconds = seconds;
  }

  /**
   * Constructor.
   * @param value value
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DTDur(final byte[] value, final InputInfo info) throws QueryException {
    super(BasicType.DAY_TIME_DURATION);

    final String val = Token.string(value).trim();
    final Matcher mt = DTD.matcher(val);
    if(!mt.matches() || Strings.endsWith(val, 'P') || Strings.endsWith(val, 'T'))
      throw dateError(value, XDTD, info);
    dayTime(value, mt, 2, info);
  }

  /**
   * Constructor for adding two durations.
   * @param dur duration item
   * @param add duration to be added/subtracted
   * @param plus plus/minus flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DTDur(final DTDur dur, final DTDur add, final boolean plus, final InputInfo info)
      throws QueryException {
    this(dur);
    seconds = plus ? seconds.add(add.seconds) : seconds.subtract(add.seconds);
    final double d = seconds.doubleValue();
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECDURRANGE_X.get(info, d);
  }

  /**
   * Constructor for multiplying or dividing a duration by a number.
   * @param dur duration item
   * @param factor factor
   * @param mult multiplication/division flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DTDur(final Dur dur, final double factor, final boolean mult, final InputInfo info)
      throws QueryException {
    this(dur);
    checkFactor(factor, mult, type, info);
    seconds = scaleSeconds(seconds, factor, mult);
  }

  /**
   * Constructor for subtracting two date/time items.
   * @param date date item
   * @param sub date/time to be subtracted
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DTDur(final ADate date, final ADate sub, final InputInfo info) throws QueryException {
    super(BasicType.DAY_TIME_DURATION);
    seconds = date.toSeconds().subtract(sub.toSeconds());
    final double d = seconds.doubleValue();
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECRANGE_X.get(info, d);
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    out.writeToken(string(null));
  }

  /**
   * Returns the date and time.
   * @return year
   */
  public BigDecimal dtd() {
    return seconds;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    final int ss = seconds.signum();
    if(ss < 0) tb.add('-');
    tb.add('P');
    final long d = day();
    if(d != 0) { tb.addLong(Math.abs(d)); tb.add('D'); }
    time(tb);
    if(ss == 0) tb.add("T0S");
    return tb.finish();
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo ii) throws QueryException {
    return item.type == type ? seconds.subtract(((Dur) item).seconds).signum() :
      super.compare(item, coll, transitive, ii);
  }

  /**
   * Returns a dayTimeDuration item for the specified milliseconds.
   * @param ms milliseconds
   * @return dateTime instance
   */
  public static DTDur get(final long ms) {
    return new DTDur(BigDecimal.valueOf(ms).divide(BD_1000, MathContext.DECIMAL64));
  }
}
