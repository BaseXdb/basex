package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * DayTime Duration item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DTd extends Dur {
  /** DayTime pattern. */
  private static final Pattern DUR = Pattern.compile("(-?)P(([0-9]+)D)?" +
    "(T(([0-9]+)H)?(([0-9]+)M)?(([0-9]+(\\.[0-9]+)?)?S)?)?");

  /**
   * Constructor.
   * @param d duration item
   */
  DTd(final Dur d) {
    super(Type.DTD);
    sc = d.sc == null ? BigDecimal.valueOf(0) : d.sc;
  }

  /**
   * Timezone constructor.
   * @param shift shift value
   */
  public DTd(final int shift) {
    super(Type.DTD);
    sc = BigDecimal.valueOf(shift * 60L);
  }

  /**
   * Constructor.
   * @param it duration item
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   */
  public DTd(final DTd it, final DTd a, final boolean p) {
    this(it);
    sc = p ? sc.add(a.sc) : sc.subtract(a.sc);
  }

  /**
   * Constructor.
   * @param it duration item
   * @param f factor
   * @param m multiplication flag
   * @throws XQException evaluation exception
   */
  public DTd(final Dur it, final double f, final boolean m) throws XQException {
    this(it);
    if(f != f) Err.or(DATECALC, info(), f);
    if(m ? f == 1 / 0d || f == -1 / 0d : f == 0) Err.or(DATEZERO, info(), f);
    sc = sc.multiply(BigDecimal.valueOf(m ? f : 1 / f));
    if(Math.abs(sc.doubleValue()) < 1E-13) sc = BigDecimal.valueOf(0);
  }

  /**
   * Constructor.
   * @param dat date item
   * @param sub date to be subtracted
   */
  public DTd(final Date dat, final Date sub) {
    super(Type.DTD);
    final long d1 = dat.days();
    final BigDecimal s1 = dat.seconds();
    final long d2 = sub.days();
    final BigDecimal s2 = sub.seconds();
    sc = BigDecimal.valueOf((d1 - d2) * DAYSECONDS).add(s1.subtract(s2));
  }
  
  /**
   * Constructor.
   * @param v value
   * @throws XQException evaluation exception
   */
  DTd(final byte[] v) throws XQException {
    super(Type.DTD);
    
    final String val = Token.string(v).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P") || val.endsWith("T"))
      Err.date(type, XDTD);
    final long d = mt.group(2) != null ? Token.toInt(mt.group(3)) : 0;
    final long h = mt.group(5) != null ? Token.toInt(mt.group(6)) : 0;
    final long n = mt.group(7) != null ? Token.toInt(mt.group(8)) : 0;
    final double s = mt.group(9) != null ?
        Token.toDouble(Token.token(mt.group(10))) : 0;

    sc = BigDecimal.valueOf(d * DAYSECONDS + h * 3600 + n * 60);
    sc = sc.add(BigDecimal.valueOf(s));
    if(mt.group(1).length() != 0) sc = sc.negate();
  }

  /**
   * Returns the date and time.
   * @return year
   */
  public BigDecimal dtd() {
    return sc;
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    if(sc.signum() < 0) tb.add('-');
    tb.add('P');
    if(day() != 0) { tb.add(Math.abs(day())); tb.add('D'); }
    if(sc.remainder(BigDecimal.valueOf(DAYSECONDS)).signum() != 0) {
      tb.add('T');
      if(hou() != 0) { tb.add(Math.abs(hou())); tb.add('H'); }
      if(min() != 0) { tb.add(Math.abs(min())); tb.add('M'); }
      if(sec().signum() != 0) { tb.add(sc()); tb.add('S'); }
    }
    if(sc.signum() == 0) tb.add("T0S");
    return tb.finish();
  }

  @Override
  public int hash() {
    return sc.intValue();
  }

  @Override
  public int diff(final Item it) throws XQException {
    if(it.type != type) Err.cmp(it, this);
    return sc.subtract(((Dur) it).sc).signum();
  }
}
