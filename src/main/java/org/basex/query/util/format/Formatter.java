package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Abstract class for formatting data in different languages.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Formatter extends FormatUtil {
  /** Default language: English. */
  private static final String EN = "en";
  /** Formatter instances. */
  private static final HashMap<String, Formatter> MAP =
    new HashMap<String, Formatter>();

  // initialize hash map with English formatter as default
  static { MAP.put(EN, new FormatterEN()); }

  /**
   * Returns a formatter for the specified language.
   * @param ln language
   * @return formatter instance
   */
  public static Formatter get(final String ln) {
    // check if formatter has already been created
    Formatter form = MAP.get(ln);
    if(form == null) {
      final String clz = Util.name(Formatter.class) +
          ln.toUpperCase(Locale.ENGLISH);
      form = (Formatter) Reflect.get(Reflect.find(clz));
      // instantiation not successful: return default formatter
      if(form == null) form = MAP.get(EN);
    }
    return form;
  }

  /**
   * Returns a word representation for the specified number.
   * @param n number to be formatted
   * @param ord ordinal suffix
   * @return token
   */
  protected abstract byte[] word(final long n, final byte[] ord);

  /**
   * Returns an ordinal representation for the specified number.
   * @param n number to be formatted
   * @param ord ordinal suffix
   * @return ordinal
   */
  protected abstract byte[] ordinal(final long n, final byte[] ord);

  /**
   * Returns the specified month (0-11).
   * @param n number to be formatted
   * @param min minimum length
   * @param max maximum length
   * @return month
   */
  protected abstract byte[] month(final int n, final int min, final int max);

  /**
   * Returns the specified day of the week (0-6, Sunday-Saturday).
   * @param n number to be formatted
   * @param min minimum length
   * @param max maximum length
   * @return day of week
   */
  protected abstract byte[] day(final int n, final int min, final int max);

  /**
   * Returns the am/pm marker.
   * @param am am flag
   * @return am/pm marker
   */
  protected abstract byte[] ampm(final boolean am);

  /**
   * Returns the calendar.
   * @return calendar
   */
  protected abstract byte[] calendar();

  /**
   * Returns the era.
   * @param year year
   * @return era
   */
  protected abstract byte[] era(final long year);

  /**
   * Formats the specified date.
   * @param date date to be formatted
   * @param pic picture
   * @param cal calendar
   * @param plc place
   * @param ii input info
   * @return formatted string
   * @throws QueryException query exception
   */
  public final byte[] formatDate(final ADate date, final byte[] pic, final byte[] cal,
      final byte[] plc, final InputInfo ii) throws QueryException {

    // [CG] XQuery/Formatter: currently, calendars and places are ignored
    if(cal != null || plc != null);

    final TokenBuilder tb = new TokenBuilder();
    final DateParser dp = new DateParser(ii, pic);
    while(dp.more()) {
      final int ch = dp.literal();
      if(ch != -1) {
        // print literal
        tb.add(ch);
      } else {
        // retrieve variable marker
        final byte[] marker = dp.marker();
        if(marker.length == 0) PICDATE.thrw(ii, pic);

        // parse component specifier
        final int compSpec = ch(marker, 0);
        byte[] pres = ONE;
        boolean max = false;
        BigDecimal frac = null;
        long num = 0;

        final boolean dat = date.type == AtomType.DAT;
        final boolean tim = date.type == AtomType.TIM;
        boolean err = false;
        switch(compSpec) {
          case 'Y':
            num = Math.abs(date.yea());
            max = true;
            err = tim;
            break;
          case 'M':
            num = date.mon();
            err = tim;
            break;
          case 'D':
            num = date.day();
            err = tim;
            break;
          case 'd':
            num = ADate.days(0, (int) date.mon(), (int) date.day() - 1).longValue();
            err = tim;
            break;
          case 'F':
            num = date.toJava().toGregorianCalendar().get(Calendar.DAY_OF_WEEK) - 1;
            if(num == 0) num = 7;
            pres = new byte[] { 'n' };
            err = tim;
            break;
          case 'W':
            num = date.toJava().toGregorianCalendar().get(Calendar.WEEK_OF_YEAR);
            err = tim;
            break;
          case 'w':
            num = date.toJava().toGregorianCalendar().get(Calendar.WEEK_OF_MONTH);
            err = tim;
            break;
          case 'H':
            num = date.hou();
            err = dat;
            break;
          case 'h':
            num = date.hou() % 12;
            if(num == 0) num = 12;
            err = dat;
            break;
          case 'P':
            num = date.hou() / 12;
            pres = new byte[] { 'n' };
            err = dat;
            break;
          case 'm':
            num = date.min();
            pres = token("01");
            err = dat;
            break;
          case 's':
            num = date.sec().intValue();
            pres = token("01");
            err = dat;
            break;
          case 'f':
            frac = date.sec().remainder(BigDecimal.ONE);
            err = dat;
            break;
          case 'Z':
          case 'z':
            num = date.zon();
            pres = num == Short.MAX_VALUE ? null : token("01:01");
            break;
          case 'C':
            pres = new byte[] { 'n' };
            break;
          case 'E':
            num = date.yea();
            pres = new byte[] { 'n' };
            err = tim;
            break;
          default:
            INVCOMPSPEC.thrw(ii, marker);
        }
        if(err) PICINVCOMP.thrw(ii, marker, date.type);
        if(pres == null) continue;

        // parse presentation modifier(s) and width modifier
        final DateFormat fp = new DateFormat(substring(marker, 1), pres, ii);
        if(max) {
          // limit maximum length of numeric output
          int mx = 0;
          for(int s = 0; s < fp.primary.length; s += cl(fp.primary, s)) mx++;
          if(mx > 1) fp.max = mx;
        }

        if(fp.first == 'n') {
          byte[] in = EMPTY;
          if(compSpec == 'M') {
            in = month((int) num - 1, fp.min, fp.max);
          } else if(compSpec == 'F') {
            in = day((int) num - 1, fp.min, fp.max);
          } else if(compSpec == 'P') {
            in = ampm(num == 0);
          } else if(compSpec == 'C') {
            in = calendar();
          } else if(compSpec == 'E') {
            in = era((int) num);
          }
          if(fp.cs == Case.LOWER) in = lc(in);
          if(fp.cs == Case.UPPER) in = uc(in);
          tb.add(in);
        } else {
          if(frac != null && !frac.equals(BigDecimal.ZERO)) {
            String s = frac.toString().replace("0.", "");
            final int sl = s.length();
            if(fp.min > sl) {
              s = frac(frac, fp.min);
            } else if(fp.max < sl) {
              s = frac(frac, fp.max);
            } else {
              final int fl = fp.primary.length;
              if(fl != 1 && fl != sl) s = frac(frac, fl);
            }
            num = Token.toLong(s);
          }
          tb.add(formatInt(num, fp));
        }
      }
    }
    return tb.finish();
  }

  /**
   * Returns the fractional part of a decimal number.
   * @param num number
   * @param len length of fractional part
   * @return string representation
   */
  private String frac(final BigDecimal num, final int len) {
    final String s = num.setScale(len, BigDecimal.ROUND_HALF_UP).toString();
    final int d = s.indexOf('.');
    return d == -1 ? s : s.substring(d + 1);
  }

  /**
   * Returns a formatted integer.
   * @param num integer to be formatted
   * @param fp format parser
   * @return string representation
   */
  public final byte[] formatInt(final long num, final FormatParser fp) {
    // prepend minus sign to negative values
    long n = num;
    final boolean sign = n < 0;
    if(sign) n = -n;

    final TokenBuilder tb = new TokenBuilder();
    final int ch = fp.first;

    if(ch == 'w') {
      tb.add(word(n, fp.ordinal));
    } else if(ch == KANJI[1]) {
      japanese(tb, n);
    } else if(ch == 'i') {
      roman(tb, n);
    } else if(ch == '\u2460' || ch == '\u2474' || ch == '\u2488') {
      if(num < 1 || num > 20) tb.addLong(num);
      else tb.add((int) (ch + num - 1));
    } else {
      final String seq = sequence(ch);
      if(seq != null) alpha(tb, num, seq);
      else tb.add(number(n, fp, zeroes(ch)));
    }

    // finalize formatted string
    byte[] in = tb.finish();
    if(fp.cs == Case.LOWER) in = lc(in);
    if(fp.cs == Case.UPPER) in = uc(in);
    return sign ? concat(new byte[] { '-' }, in) : in;
  }

  /**
   * Returns a character sequence based on the specified alphabet.
   * @param tb token builder
   * @param n number to be formatted
   * @param a alphabet
   */
  private static void alpha(final TokenBuilder tb, final long n, final String a) {
    final int al = a.length();
    if(n > al) alpha(tb, (n - 1) / al, a);
    if(n > 0) tb.add(a.charAt((int) ((n - 1) % al)));
    else tb.add(ZERO);
  }

  /**
   * Adds a Roman character sequence.
   * @param tb token builder
   * @param n number to be formatted
   */
  private static void roman(final TokenBuilder tb, final long n) {
    if(n > 0 && n < 4000) {
      final int v = (int) n;
      tb.add(ROMANM[v / 1000]);
      tb.add(ROMANC[v / 100 % 10]);
      tb.add(ROMANX[v / 10 % 10]);
      tb.add(ROMANI[v % 10]);
    } else {
      tb.addLong(n);
    }
  }

  /**
   * Adds a Japanese character sequence.
   * @param tb token builder
   * @param n number to be formatted
   */
  private static void japanese(final TokenBuilder tb, final long n) {
    if(n == 0) {
      tb.add(KANJI[0]);
    } else {
      jp(tb, n, false);
    }
  }

  /**
   * Recursively adds a Japanese character sequence.
   * @param tb token builder
   * @param n number to be formatted
   * @param i initial call
   */
  private static void jp(final TokenBuilder tb, final long n, final boolean i) {
    if(n == 0) {
    } else if(n <= 9) {
      if(n != 1 || !i) tb.add(KANJI[(int) n]);
    } else if(n == 10) {
      tb.add(KANJI[10]);
    } else if(n <= 99) {
      jp(tb, n, 10, 10);
    } else if(n <= 999) {
      jp(tb, n, 100, 11);
    } else if(n <= 9999) {
      jp(tb, n, 1000, 12);
    } else if(n <= 99999999) {
      jp(tb, n, 10000, 13);
    } else if(n <= 999999999999L) {
      jp(tb, n, 100000000, 14);
    } else if(n <= 9999999999999999L) {
      jp(tb, n, 1000000000000L, 15);
    } else {
      tb.addLong(n);
    }
  }

  /**
   * Recursively adds a Japanese character sequence.
   * @param tb token builder
   * @param n number to be formatted
   * @param f factor
   * @param o kanji offset
   */
  private static void jp(final TokenBuilder tb, final long n, final long f,
      final int o) {
    jp(tb, n / f, true);
    tb.add(KANJI[o]);
    jp(tb, n % f, false);
  }

  /**
   * Creates a number character sequence.
   * @param num number to be formatted
   * @param fp format parser
   * @param z zero digit
   * @return number character sequence
   */
  private byte[] number(final long num, final FormatParser fp, final int z) {
    // cache characters of presentation modifier
    final IntList pr = new TokenParser(fp.primary).toList();

    // check for a regular separator pattern
    int rp = -1;
    boolean reg = false;
    for(int p = pr.size() - 1; p >= 0; --p) {
      final int ch = pr.get(p);
      if(ch == '#' || ch >= z && ch <= z + 9) continue;
      if(rp == -1) rp = pr.size() - p;
      reg = (pr.size() - p) % rp == 0;
    }
    final int rc = reg ? pr.get(pr.size() - rp) : 0;
    if(!reg) rp = Integer.MAX_VALUE;

    // build string representation in a reverse order
    final IntList cache = new IntList();
    final byte[] n = token(num);
    int b = n.length - 1, p = pr.size() - 1;

    // add numbers and separators
    int mn = fp.min;
    int mx = fp.max;
    while((--mn >= 0 || b >= 0 || p >= 0) && --mx >= 0) {
      final boolean sep = cache.size() % rp == rp - 1;
      if(p >= 0) {
        final int c = pr.get(p--);
        if(b >= 0) {
          if(c == '#' && sep) cache.add(rc);
          cache.add(c == '#' || c >= z && c <= z + 9 ? n[b--] - '0' + z : c);
        } else {
          // add remaining modifiers
          if(c == '#') break;
          cache.add(c >= z && c <= z + 9 ? z : c);
        }
      } else if(b >= 0) {
        // add remaining numbers
        if(sep) cache.add(rc);
        cache.add(n[b--] - '0' + z);
      } else {
        // add minimum numbers
        cache.add(z);
      }
    }

    // reverse result and add ordinal suffix
    final TokenBuilder tb = new TokenBuilder();
    for(int c = cache.size() - 1; c >= 0; --c) tb.add(cache.get(c));
    return tb.add(ordinal(num, fp.ordinal)).finish();
  }
}
