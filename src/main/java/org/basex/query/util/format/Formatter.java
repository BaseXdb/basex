package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;

import javax.xml.datatype.*;

import org.basex.query.*;
import org.basex.query.value.item.ADate;
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
  /** Language code: English. */
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
  protected abstract byte[] era(final int year);

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
      final int ch = dp.next();
      if(ch != 0) {
        // print literal
        tb.add(ch);
      } else {
        byte[] p = dp.marker();
        if(p.length == 0) PICDATE.thrw(ii, pic);
        final int spec = ch(p, 0);
        p = substring(p, cl(p, 0));
        byte[] pres = ONE;
        boolean max = false;
        long num = 0;

        final boolean dat = date.type == AtomType.DAT;
        final boolean tim = date.type == AtomType.TIM;
        final XMLGregorianCalendar gc = date.xc;
        boolean err = false;
        switch(spec) {
          case 'Y':
            num = Math.abs(gc.getYear());
            max = true;
            err = tim;
            break;
          case 'M':
            num = gc.getMonth();
            err = tim;
            break;
          case 'D':
            num = gc.getDay();
            err = tim;
            break;
          case 'd':
            num = ADate.days(0, gc.getMonth(), gc.getDay());
            err = tim;
            break;
          case 'F':
            num = gc.toGregorianCalendar().get(Calendar.DAY_OF_WEEK) - 1;
            pres = new byte[] { 'n' };
            err = tim;
            break;
          case 'W':
            num = gc.toGregorianCalendar().get(Calendar.WEEK_OF_YEAR);
            err = tim;
            break;
          case 'w':
            num = gc.toGregorianCalendar().get(Calendar.WEEK_OF_MONTH);
            err = tim;
            break;
          case 'H':
            num = gc.getHour();
            err = dat;
            break;
          case 'h':
            num = gc.getHour() % 12;
            if(num == 0) num = 12;
            err = dat;
            break;
          case 'P':
            num = gc.getHour() / 12;
            pres = new byte[] { 'n' };
            err = dat;
            break;
          case 'm':
            num = gc.getMinute();
            pres = token("01");
            err = dat;
            break;
          case 's':
            num = gc.getSecond();
            pres = token("01");
            err = dat;
            break;
          case 'f':
            num = gc.getMillisecond();
            err = dat;
            break;
          case 'Z':
          case 'z':
            num = gc.getTimezone();
            pres = token("01:01");
            break;
          case 'C':
            pres = new byte[] { 'n' };
            break;
          case 'E':
            num = gc.getYear();
            pres = new byte[] { 'n' };
            break;
          default:
            err = true;
            break;
        }
        if(err) PICCOMP.thrw(ii, pic);

        final FormatParser fp = new FormatParser(p, pres, ii);
        if(max) {
          // limit maximum length of numeric output
          int mx = 0;
          for(int s = 0; s < fp.primary.length; s += cl(fp.primary, s)) mx++;
          if(mx > 1) fp.max = mx;
        }

        if(fp.digit == 'n') {
          byte[] in = EMPTY;
          if(spec == 'M') {
            in = month((int) num - 1, fp.min, fp.max);
          } else if(spec == 'F') {
            in = day((int) num, fp.min, fp.max);
          } else if(spec == 'P') {
            in = ampm(num == 0);
          } else if(spec == 'C') {
            in = calendar();
          } else if(spec == 'E') {
            in = era((int) num);
          }
          if(fp.cs == Case.LOWER) in = lc(in);
          if(fp.cs == Case.UPPER) in = uc(in);
          tb.add(in);
        } else {
          tb.add(formatInt(num, fp));
        }
      }
    }
    return tb.finish();
  }

  /**
   * Returns a formatted integer.
   * @param num integer to be formatted
   * @param fp format parser
   * @return string representation
   */
  public final byte[] formatInt(final long num, final FormatParser fp) {
    // choose sign
    long n = num;
    final boolean sign = n < 0;
    if(sign) n = -n;

    final TokenBuilder tb = new TokenBuilder();
    final int ch = fp.digit;
    final boolean single = fp.primary.length == cl(fp.primary, 0);

    if(ch == 'w') {
      tb.add(word(n, fp.ordinal));
    } else if(ch == KANJI[1]) {
      japanese(tb, n);
    } else if(single && ch == 'i') {
      roman(tb, n);
    } else if(ch >= '\u2460' && ch <= '\u249b') {
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
  private static void alpha(final TokenBuilder tb, final long n,
      final String a) {

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
    final IntList pr = new IntList(fp.primary.length);
    for(int p = 0; p < fp.primary.length; p += cl(fp.primary, p)) {
      pr.add(cp(fp.primary, p));
    }

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
