package org.basex.query.item;

import static org.basex.query.QueryText.*;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.BaseX;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Date container.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Date extends Item {
  /** Date pattern. */
  protected static final String ZONE = "((\\+|-)([0-9]{2}):([0-9]{2})|Z)?";
  /** Day per months. */
  protected static final byte[] DAYS = {
    31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
  /** Date pattern. */
  protected static final Pattern DAT = Pattern.compile(
      "(-?)([0-9]{4})-([0-9]{2})-([0-9]{2})" + ZONE);
  /** Time pattern. */
  private static final Pattern TIM = Pattern.compile(
      "([0-9]{2}):([0-9]{2}):([0-9]{2})(\\.([0-9]+))?" + ZONE);
  /** Data factory. */
  public static DatatypeFactory df;
  /** Calendar instance. */
  public XMLGregorianCalendar xc;
  
  static {
    try {
      df = DatatypeFactory.newInstance();
    } catch(final Exception e) {
      BaseX.notexpected();
    }
  }

  /**
   * Constructor.
   * @param typ data type
   * @param d date reference
   */
  protected Date(final Type typ, final Date d) {
    super(typ);
    xc = (XMLGregorianCalendar) d.xc.clone();
  }

  /**
   * Constructor.
   * @param typ data type
   * @param d date reference
   * @param e expected format
   * @throws QueryException evaluation exception
   */
  protected Date(final Type typ, final byte[] d, final String e)
      throws QueryException {
    super(typ);
    try {
      xc = df.newXMLGregorianCalendar(Token.string(d).trim());
      if(xc.getHour() == 24) xc.add(df.newDuration(0));
    } catch(final IllegalArgumentException ex) {
      Err.date(type, e);
    }
  }

  /**
   * Checks the date format.
   * @param d input format
   * @param e expected format
   * @throws QueryException query exception
   */
  protected final void date(final byte[] d, final String e)
      throws QueryException {

    final Matcher mt = DAT.matcher(Token.string(d).trim());
    if(!mt.matches()) Err.date(type, e);
    zone(mt, 5, d);
  }

  /**
   * Checks the time format.
   * @param d input format
   * @param e expected format
   * @throws QueryException query exception
   */
  protected final void time(final byte[] d, final String e)
      throws QueryException {

    final Matcher mt = TIM.matcher(Token.string(d).trim());
    if(!mt.matches()) Err.date(type, e);
    
    final int h = Token.toInt(mt.group(1));
    final int s = Token.toInt(mt.group(3));
    if(s > 59) Err.range(type, d);
    final double ms = mt.group(4) != null ? Double.parseDouble(mt.group(4)) : 0;
    if(h == 24 && ms > 0) Err.date(type, e);
    zone(mt, 6, d);
  }

  /**
   * Evaluates the timezone.
   * @param mt matcher
   * @param p matching position
   * @param val value
   * @throws QueryException evaluation exception
   */
  protected final void zone(final Matcher mt, final int p, final byte[] val)
    throws QueryException {

    if(mt.group(p) == null || mt.group(p).equals("Z")) return;
    final int th = Token.toInt(mt.group(p + 2));
    final int tm = Token.toInt(mt.group(p + 3));
    if(th > 14 || tm > 59 || th == 14 && tm != 0) Err.or(INVALIDZONE, val);
  }

  /**
   * Add/subtract the specified duration.
   * @param a duration
   * @param p plus/minus flag
   * @throws QueryException evaluation exception
   */
  protected final void calc(final Dur a, final boolean p)
      throws QueryException {

    if(xc.getYear() + a.mon / 12 > 9999) Err.range(type, a.str());
    xc.add(p ? a.java() : a.java().negate());
  }

  @Override
  public final byte[] str() {
    String str = xc.toXMLFormat();
    str = str.replaceAll("\\.0+(Z|-.*|\\+.*)?$", "$1");
    str = str.replaceAll("(\\.\\d+?)0+(Z|-.*|\\+.*)?$", "$1$2");
    return Token.token(str);
  }

  @Override
  public final boolean eq(final Item it) {
    final long d1 = days();
    final long d2 = ((Date) it).days();
    return d1 == d2 && seconds().doubleValue() ==
      (((Date) it).seconds()).doubleValue();
    //return d1 == d2 && seconds().equals(((Date) it).seconds());
  }

  @Override
  @SuppressWarnings("unused")
  public int diff(final Item it) throws QueryException {
    final long d1 = days();
    final long d2 = ((Date) it).days();
    if(d1 != d2) return (int) (d1 - d2);
    return seconds().subtract(((Date) it).seconds()).signum();
  }

  @Override
  public final String toString() {
    return new TokenBuilder().add('"').add(str()).add('"').toString();
  }

  @Override
  public final Object java() {
    return xc;
  }
  
  /**
   * Returns the date in seconds.
   * @return seconds
   */
  public BigDecimal seconds() {
    final int h = xc.getHour() == UNDEF ? 0 : xc.getHour();
    final int m = xc.getMinute() == UNDEF ? 0 : xc.getMinute();
    final int s = xc.getSecond() == UNDEF ? 0 : xc.getSecond();
    final int z = xc.getTimezone() == UNDEF ? 0 : xc.getTimezone();
    BigDecimal bd = xc.getFractionalSecond();
    if(bd == null) bd = BigDecimal.valueOf(0);
    return bd.add(BigDecimal.valueOf(h * 3600 + m * 60 - z * 60 + s));
  }
  
  /**
   * Returns the days.
   * @return seconds
   */
  public long days() {
    final int y = xc.getYear() == UNDEF ? 0 : xc.getYear();
    final int m = xc.getMonth() == UNDEF ? 0 : xc.getMonth() - 1;
    final int d = xc.getDay() == UNDEF ? 0 : xc.getDay() - 1;
    long s = days(y, m, d);
    return y > 0 ? s : -s;
  }
  
  /***
   * Returns days per month, considering leap years.
   * @param y year
   * @param m month
   * @return days
   */
  private static long dpm(final int y, final int m) {
    return DAYS[m] + (m == 1 && leap(y) ? 1 : 0);
  }
  
  /***
   * Checks if the specified year is a leap year.
   * @param y year
   * @return result of check
   */
  private static boolean leap(final int y) {
    return y % 4 == 0 && (y % 100 != 0 || y % 400 == 0);
  }

  /***
   * Returns the nth day of the year.
   * @param y year
   * @param m month
   * @param d days
   * @return days
   */
  private static long days(final int y, final int m, final int d) {
    long n = 0;
    for(int i = 0; i < Math.abs(y); i++) n += 365 + (leap(i) ? 1 : 0);
    for(int i = 0; i < m; i++) n += dpm(y, i);
    return n + d;
  }
}
