package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.math.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.*;
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
   * @param d duration item
   */
  public DTDur(final Dur d) {
    super(AtomType.DTD);
    sec = d.sec == null ? BigDecimal.ZERO : d.sec;
  }

  /**
   * Timezone constructor.
   * @param h hours
   * @param m minutes
   */
  public DTDur(final long h, final long m) {
    super(AtomType.DTD);
    sec = BigDecimal.valueOf(h).multiply(BD60).add(BigDecimal.valueOf(m)).multiply(BD60);
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
   * @param it duration item
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public DTDur(final DTDur it, final DTDur a, final boolean p, final InputInfo ii)
      throws QueryException {

    this(it);
    sec = p ? sec.add(a.sec) : sec.subtract(a.sec);
    final double d = sec.doubleValue();
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECDURRANGE.get(ii, d);
  }

  /**
   * Constructor.
   * @param it duration item
   * @param f factor
   * @param m multiplication flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public DTDur(final Dur it, final double f, final boolean m, final InputInfo ii)
      throws QueryException {

    this(it);
    if(Double.isNaN(f)) throw DATECALC.get(ii, description(), f);
    if(m ? Double.isInfinite(f) : f == 0) throw DATEZERO.get(ii, type);
    if(m ? f == 0 : Double.isInfinite(f)) {
      sec = BigDecimal.ZERO;
    } else {
      BigDecimal d = BigDecimal.valueOf(f);
      try {
        sec = m ? sec.multiply(d) : sec.divide(d);
      } catch(final ArithmeticException ex) {
        // catching cases in which a computation yields no exact result; eg:
        // xs:dayTimeDuration("P1D") div xs:double("-1.7976931348623157E308")
        d = BigDecimal.valueOf(1 / f);
        sec = m ? sec.divide(d) : sec.multiply(d);
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
    if(d <= Long.MIN_VALUE || d >= Long.MAX_VALUE) throw SECRANGE.get(ii, d);
  }

  /**
   * Constructor.
   * @param vl value
   * @param ii input info
   * @throws QueryException query exception
   */
  public DTDur(final byte[] vl, final InputInfo ii) throws QueryException {
    super(AtomType.DTD);

    final String val = Token.string(vl).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T")) dateErr(vl, XDTD, ii);
    dayTime(vl, mt, 2, ii);
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
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    if(it.type != type) throw diffError(ii, it, this);
    return sec.subtract(((ADateDur) it).sec).signum();
  }
}
