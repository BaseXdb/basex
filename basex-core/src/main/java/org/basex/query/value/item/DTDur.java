package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.math.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * DayTime Duration item ({@code xs:dayTimeDuration}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DTDur extends Dur {
  /** DayTime pattern. */
  private static final Pattern DUR = Pattern.compile(
      "(-?)P(" + DP + "D)?(T(" + DP + "H)?(" + DP + "M)?((\\d+(\\.\\d+)?)S)?)?");

  /**
   * Constructor.
   * @param dur duration item
   */
  public DTDur(final Dur dur) {
    super(AtomType.DTD);
    sec = dur.sec == null ? BigDecimal.ZERO : dur.sec;
  }

  /**
   * Timezone constructor.
   * @param hours hours
   * @param minutes minutes
   */
  public DTDur(final long hours, final long minutes) {
    super(AtomType.DTD);
    sec = BigDecimal.valueOf(hours).multiply(BD60).add(BigDecimal.valueOf(minutes)).multiply(BD60);
  }

  /**
   * Timezone constructor.
   * @param ms milliseconds
   */
  public DTDur(final long ms) {
    super(AtomType.DTD);
    sec = BigDecimal.valueOf(ms).divide(BD1000);
  }

  /**
   * Constructor.
   * @param dur duration item
   * @param add duration to be added/subtracted
   * @param plus plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public DTDur(final DTDur dur, final DTDur add, final boolean plus, final InputInfo ii)
      throws QueryException {

    this(dur);
    sec = plus ? sec.add(add.sec) : sec.subtract(add.sec);
    final double d = sec.doubleValue();
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECDURRANGE_X.get(ii, d);
  }

  /**
   * Constructor.
   * @param dur duration item
   * @param factor factor
   * @param mult multiplication flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public DTDur(final Dur dur, final double factor, final boolean mult, final InputInfo ii)
      throws QueryException {

    this(dur);
    if(Double.isNaN(factor)) throw DATECALC_X_X.get(ii, description(), factor);
    if(mult ? Double.isInfinite(factor) : factor == 0) throw DATEZERO_X_X.get(ii, type, factor);
    if(mult ? factor == 0 : Double.isInfinite(factor)) {
      sec = BigDecimal.ZERO;
    } else {
      BigDecimal d = BigDecimal.valueOf(factor);
      try {
        sec = mult ? sec.multiply(d) : sec.divide(d);
      } catch(final ArithmeticException ex) {
        // catching cases in which a computation yields no exact result; eg:
        // xs:dayTimeDuration("P1D") div xs:double("-1.7976931348623157E308")
        d = BigDecimal.valueOf(1 / factor);
        sec = mult ? sec.divide(d) : sec.multiply(d);
      }
    }
    if(Math.abs(sec.doubleValue()) < 1E-13) sec = BigDecimal.ZERO;
  }

  /**
   * Constructor.
   * @param dat date item
   * @param sub date to be subtracted
   * @param ii input info
   * @throws QueryException query exception
   */
  public DTDur(final ADate dat, final ADate sub, final InputInfo ii) throws QueryException {
    super(AtomType.DTD);
    sec = dat.days().subtract(sub.days()).multiply(DAYSECONDS).add(
        dat.seconds().subtract(sub.seconds()));
    final double d = sec.doubleValue();
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECRANGE_X.get(ii, d);
  }

  /**
   * Constructor.
   * @param value value
   * @param ii input info
   * @throws QueryException query exception
   */
  public DTDur(final byte[] value, final InputInfo ii) throws QueryException {
    super(AtomType.DTD);

    final String val = Token.string(value).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T")) throw dateError(value, XDTD, ii);
    dayTime(value, mt, 2, ii);
  }

  /**
   * Returns the date and time.
   * @return year
   */
  public BigDecimal dtd() {
    return sec;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    final int ss = sec.signum();
    if(ss < 0) tb.add('-');
    tb.add('P');
    if(day() != 0) { tb.addLong(Math.abs(day())); tb.add('D'); }
    time(tb);
    if(ss == 0) tb.add("T0S");
    return tb.finish();
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    if(it.type == type) return sec.subtract(((ADateDur) it).sec).signum();
    throw diffError(ii, it, this);
  }
}
