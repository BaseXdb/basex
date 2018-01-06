package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.math.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * DayTime Duration item ({@code xs:dayTimeDuration}).
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class DTDur extends Dur {
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
   * @param sec seconds
   */
  public DTDur(final BigDecimal sec) {
    super(AtomType.DTD);
    this.sec = sec;
  }

  /**
   * Constructor.
   * @param dur duration item
   * @param add duration to be added/subtracted
   * @param plus plus/minus flag
   * @param info input info
   * @throws QueryException query exception
   */
  public DTDur(final DTDur dur, final DTDur add, final boolean plus, final InputInfo info)
      throws QueryException {

    this(dur);
    sec = plus ? sec.add(add.sec) : sec.subtract(add.sec);
    final double d = sec.doubleValue();
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECDURRANGE_X.get(info, d);
  }

  /**
   * Constructor.
   * @param dur duration item
   * @param factor factor
   * @param mult multiplication flag
   * @param info input info
   * @throws QueryException query exception
   */
  public DTDur(final Dur dur, final double factor, final boolean mult, final InputInfo info)
      throws QueryException {

    this(dur);
    if(Double.isNaN(factor)) throw DATECALC_X_X.get(info, description(), factor);
    if(mult ? Double.isInfinite(factor) : factor == 0) throw DATEZERO_X_X.get(info, type, factor);
    if(mult ? factor == 0 : Double.isInfinite(factor)) {
      sec = BigDecimal.ZERO;
    } else {
      BigDecimal d = BigDecimal.valueOf(factor);
      try {
        sec = mult ? sec.multiply(d) : sec.divide(d, MathContext.DECIMAL64);
      } catch(final ArithmeticException ex) {
        Util.debug(ex);
        // catch cases in which a computation yields no exact result; eg:
        // xs:dayTimeDuration("P1D") div xs:double("-1.7976931348623157E308")
        d = BigDecimal.valueOf(1 / factor);
        sec = mult ? sec.divide(d, MathContext.DECIMAL64) : sec.multiply(d);
      }
    }
    if(Math.abs(sec.doubleValue()) < 1.0E-13) sec = BigDecimal.ZERO;
  }

  /**
   * Constructor.
   * @param date date item
   * @param sub date to be subtracted
   * @param info input info
   * @throws QueryException query exception
   */
  public DTDur(final ADate date, final ADate sub, final InputInfo info) throws QueryException {
    super(AtomType.DTD);
    sec = date.days().subtract(sub.days()).multiply(DAYSECONDS).add(
        date.seconds().subtract(sub.seconds()));
    final double d = sec.doubleValue();
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECRANGE_X.get(info, d);
  }

  /**
   * Constructor.
   * @param value value
   * @param info input info
   * @throws QueryException query exception
   */
  public DTDur(final byte[] value, final InputInfo info) throws QueryException {
    super(AtomType.DTD);

    final String val = Token.string(value).trim();
    final Matcher mt = DTD.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T")) throw dateError(value, XDTD, info);
    dayTime(value, mt, 2, info);
  }

  /**
   * Returns the date and time.
   * @return year
   */
  public BigDecimal dtd() {
    return sec;
  }

  @Override
  public byte[] string(final InputInfo info) {
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
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    if(item.type == type) return sec.subtract(((ADateDur) item).sec).signum();
    throw diffError(item, this, info);
  }

  /**
   * Returns a dayTimeDuration item for the specified milliseconds.
   * @param ms milliseconds
   * @return dateTime instance
   */
  public static DTDur get(final long ms) {
    return new DTDur(BigDecimal.valueOf(ms).divide(BD1000, MathContext.DECIMAL64));
  }
}
