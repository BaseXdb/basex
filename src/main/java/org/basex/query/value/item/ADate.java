package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import java.math.*;
import java.util.regex.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for date items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ADate extends Item {
  /** Pattern to convert date to a string. */
  private static final Pattern TOSTRING1 = Pattern.compile("\\.0+(Z|-.*|\\+.*)?$");
  /** Pattern to convert date to a string. */
  private static final Pattern TOSTRING2 = Pattern.compile("(\\.\\d+?)0+(Z|-.*|\\+.*)?$");

    /** Date pattern. */
  static final String ZONE = "((\\+|-)([0-9]{2}):([0-9]{2})|Z)?";
  /** Day per months. */
  static final byte[] DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
  /** Date pattern. */
  private static final Pattern DAT = Pattern.compile(
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
    } catch(final Exception ex) {
      Util.notexpected();
    }
  }

  /**
   * Constructor.
   * @param typ data type
   * @param d date reference
   */
  ADate(final Type typ, final ADate d) {
    super(typ);
    xc = (XMLGregorianCalendar) d.xc.clone();
  }

  /**
   * Constructor.
   * @param typ data type
   * @param d date reference
   * @param e expected format
   * @param ii input info
   * @throws org.basex.query.QueryException query exception
   */
  ADate(final Type typ, final byte[] d, final String e, final InputInfo ii)
      throws QueryException {

    super(typ);
    try {
      xc = df.newXMLGregorianCalendar(Token.string(d).trim());
      if(xc.getHour() == 24) xc.add(df.newDuration(0));
    } catch(final IllegalArgumentException ex) {
      throw dateErr(d, e, ii);
    }
  }

  /**
   * Checks the date format.
   * @param d input format
   * @param e expected format
   * @param ii input info
   * @throws org.basex.query.QueryException query exception
   */
  final void date(final byte[] d, final String e, final InputInfo ii)
      throws QueryException {

    final Matcher mt = DAT.matcher(Token.string(d).trim());
    if(!mt.matches()) dateErr(d, e, ii);
    zone(mt, 5, d, ii);
  }

  /**
   * Checks the time format.
   * @param d input format
   * @param e expected format
   * @param ii input info
   * @throws org.basex.query.QueryException query exception
   */
  final void time(final byte[] d, final String e, final InputInfo ii)
      throws QueryException {

    final Matcher mt = TIM.matcher(Token.string(d).trim());
    if(!mt.matches()) dateErr(d, e, ii);

    final int h = Token.toInt(mt.group(1));
    final int s = Token.toInt(mt.group(3));
    if(s > 59) DATERANGE.thrw(ii, type, d);
    final double ms = mt.group(4) != null ? Double.parseDouble(mt.group(4)) : 0;
    if(h == 24 && ms > 0) dateErr(d, e, ii);
    zone(mt, 6, d, ii);
  }

  /**
   * Evaluates the timezone.
   * @param mt matcher
   * @param p matching position
   * @param val value
   * @param ii input info
   * @throws org.basex.query.QueryException query exception
   */
  static final void zone(final Matcher mt, final int p, final byte[] val,
      final InputInfo ii) throws QueryException {

    if(mt.group(p) == null || mt.group(p).equals("Z")) return;
    final int th = Token.toInt(mt.group(p + 2));
    final int tm = Token.toInt(mt.group(p + 3));
    if(th > 14 || tm > 59 || th == 14 && tm != 0) INVALIDZONE.thrw(ii, val);
  }

  /**
   * Add/subtract the specified duration.
   * @param a duration
   * @param p plus/minus flag
   * @param ii input info
   * @throws org.basex.query.QueryException query exception
   */
  final void calc(final Dur a, final boolean p, final InputInfo ii)
      throws QueryException {

    if(xc.getYear() + a.mon / 12 > 9999) DATERANGE.thrw(ii, type, a.string(ii));
    final Duration dur = a.toJava();
    xc.add(p ? dur : dur.negate());
    if(xc.getYear() == 0) xc.setYear(p ^ dur.getSign() < 0 ? 1 : -1);
  }

  @Override
  public final byte[] string(final InputInfo ii) {
    String str = xc.toXMLFormat();
    str = TOSTRING1.matcher(str).replaceAll("$1");
    str = TOSTRING2.matcher(str).replaceAll("$1$2");
    return Token.token(str);
  }

  @Override
  public final boolean eq(final InputInfo ii, final Item it) throws QueryException {
    final long d1 = days();
    final ADate d = (ADate) (it.type.isDate() ? it : type.cast(it, null, ii));
    final long d2 = d.days();
    return d1 == d2 && seconds().doubleValue() == d.seconds().doubleValue();
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    final long d1 = days();
    final ADate d = (ADate) (it.type.isDate() ? it : type.cast(it, null, ii));
    final long d2 = d.days();
    if(d1 != d2) return (int) (d1 - d2);
    return seconds().subtract(d.seconds()).signum();
  }

  @Override
  public final XMLGregorianCalendar toJava() {
    return xc;
  }

  /**
   * Returns the date in seconds.
   * @return seconds
   */
  final BigDecimal seconds() {
    final int h = xc.getHour() == UNDEF ? 0 : xc.getHour();
    final int m = xc.getMinute() == UNDEF ? 0 : xc.getMinute();
    final int s = xc.getSecond() == UNDEF ? 0 : xc.getSecond();
    final int z = xc.getTimezone() == UNDEF ? 0 : xc.getTimezone();
    BigDecimal bd = xc.getFractionalSecond();
    if(bd == null) bd = BigDecimal.valueOf(0);
    return bd.add(BigDecimal.valueOf(h * 3600 + m * 60 - z * 60 + s));
  }

  /**
   * Returns the number of days since AD.
   * @return days
   */
  final long days() {
    final int y = xc.getYear() == UNDEF ? 0 : xc.getYear();
    final int m = xc.getMonth() == UNDEF ? 0 : xc.getMonth() - 1;
    final int d = xc.getDay() == UNDEF ? 0 : xc.getDay() - 1;
    final long s = days(y, m, d);
    return y > 0 ? s : -s;
  }

  /**
   * Returns days per month, considering leap years.
   * @param y year
   * @param m month
   * @return days
   */
  private static long dpm(final int y, final int m) {
    return DAYS[m] + (m == 1 ? leap(y) : 0);
  }

  /**
   * Adds an offset for a leap year.
   * @param y year
   * @return result of check
   */
  private static int leap(final int y) {
    return y % 4 == 0 && (y % 100 != 0 || y % 400 == 0) ? 1 : 0;
  }

  /**
   * Returns the number of days since AD for the specified years,
   * months and days.
   * @param y year
   * @param m month
   * @param d days
   * @return days
   */
  public static long days(final int y, final int m, final int d) {
    long n = 0;
    final int yy = Math.abs(y);
    for(int i = 0; i < yy; ++i) n += 365 + leap(i);
    for(int i = 0; i < m; ++i) n += dpm(y, i);
    return n + d;
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", string(null));
  }
}
