package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Abstract class for formatting data in different languages.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Formatter extends FormatUtil {
  /** Calendar pattern. */
  private static final Pattern CALENDAR = Pattern.compile("(Q\\{([^}]*)\\})?([^}]+)");
  /** Military timezones. */
  private static final byte[] MIL = token("YXWVUTSRQPONZABCDEFGHIKLM");
  /** Token: Nn. */
  private static final byte[] NN = { 'N', 'n' };
  /** Allowed calendars. */
  private static final byte[][] CALENDARS = tokens(
    "ISO", "AD", "AH", "AME", "AM", "AP", "AS", "BE", "CB", "CE", "CL", "CS", "EE", "FE",
    "JE", "KE", "KY", "ME", "MS", "NS", "OS", "RS", "SE", "SH", "SS", "TE", "VE", "VS");

  /** Default language: English. */
  private static final byte[] EN = token("en");
  /** Formatter instances. */
  private static final TokenObjMap<Formatter> MAP = new TokenObjMap<Formatter>();

  // initialize hash map with English formatter as default
  static {
    MAP.put(EN, new FormatterEN());
    MAP.put(token("de"), new FormatterDE());
  }

  /**
   * Returns a formatter for the specified language.
   * @param ln language
   * @return formatter instance
   */
  public static Formatter get(final byte[] ln) {
    // check if formatter has already been created
    Formatter form = MAP.get(ln);
    if(form == null) {
      final String clz = Util.className(Formatter.class) + string(uc(ln));
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
   * @param lng language
   * @param pic picture
   * @param cal calendar
   * @param plc place
   * @param ii input info
   * @return formatted string
   * @throws QueryException query exception
   */
  public final byte[] formatDate(final ADate date, final byte[] lng, final byte[] pic,
      final byte[] cal, final byte[] plc, final InputInfo ii) throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    if(lng.length != 0 && MAP.get(lng) == null) tb.add("[Language: en]");
    boolean iso = false;
    if(cal.length != 0) {
      final Matcher m = CALENDAR.matcher(string(cal));
      if(!m.matches()) CALQNAME.thrw(ii, cal);
      final QNm qnm = new QNm(m.group(3), m.group(1) == null ||
          m.group(2).isEmpty() ? null : m.group(2));
      if(!qnm.hasURI()) {
        int c = -1;
        final byte[] ln = qnm.local();
        final int cl = CALENDARS.length;
        while(++c < cl && !eq(CALENDARS[c], ln));
        if(c == cl) CALWHICH.thrw(ii, cal);
        if(c > 1) tb.add("[Calendar: AD]");
        iso = c == 0;
      }
    }
    if(plc.length != 0) tb.add("[Place: ]");

    final DateParser dp = new DateParser(ii, pic);
    while(dp.more()) {
      final int ch = dp.literal();
      if(ch == -1) {
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
            final long y = date.yea();
            for(int m = (int) date.mon() - 1; --m >= 0;) num += ADate.dpm(y, m);
            num += date.day();
            err = tim;
            break;
          case 'F':
            num = date.toJava().toGregorianCalendar().get(Calendar.DAY_OF_WEEK) - 1;
            if(num == 0) num = 7;
            pres = NN;
            err = tim;
            break;
          case 'W':
            num = date.toJava().toGregorianCalendar().get(Calendar.WEEK_OF_YEAR);
            err = tim;
            break;
          case 'w':
            num = date.toJava().toGregorianCalendar().get(Calendar.WEEK_OF_MONTH);
            if(iso && num == 0) num = 5;
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
            pres = NN;
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
            num = frac.movePointRight(3).intValue();
            err = dat;
            break;
          case 'Z':
          case 'z':
            num = date.zon();
            pres = token("01:01");
            break;
          case 'C':
            pres = NN;
            break;
          case 'E':
            num = date.yea();
            pres = NN;
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

        if(compSpec == 'z' || compSpec == 'Z') {
          // output timezone
          tb.add(formatZone((int) num, fp, marker));
        } else if(fp.first == 'n') {
          // output name representation
          byte[] in = null;
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
          if(in != null) {
            if(fp.cs == Case.LOWER) in = lc(in);
            if(fp.cs == Case.UPPER) in = uc(in);
            tb.add(in);
          } else {
            // fallback representation
            fp.first = '0';
            fp.primary = ONE;
            tb.add(formatInt(num, fp));
          }
        } else {
          // output fractional component
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
            num = toLong(s);
          }
          tb.add(formatInt(num, fp));
        }
      } else {
        // print literal
        tb.add(ch);
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
  private static String frac(final BigDecimal num, final int len) {
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
      roman(tb, n, fp.min);
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
   * Returns a formatted timezone.
   * @param num integer to be formatted
   * @param fp format parser
   * @param marker marker
   * @return string representation
   * @throws QueryException query exception
   */
  final byte[] formatZone(final int num, final FormatParser fp,
                          final byte[] marker) throws QueryException {

    final boolean uc = ch(marker, 0) == 'Z';
    final boolean mil = uc && ch(marker, 1) == 'Z';

    // ignore values without timezone. exception: military timezone
    if(num == Short.MAX_VALUE) return mil ? new byte[] { 'J' } : EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    if(!mil || !addMilZone(num, tb)) {
      if(!uc) tb.add("GMT");

      final boolean minus = num < 0;
      if(fp.trad && num == 0) {
        tb.add('Z');
      } else {
        tb.add(minus ? '-' : '+');

        final TokenParser tp = new TokenParser(fp.primary);
        final int c1 = tp.next(), c2 = tp.next(), c3 = tp.next(), c4 = tp.next();
        final int z1 = zeroes(c1), z2 = zeroes(c2), z3 = zeroes(c3), z4 = zeroes(c4);
        if(z1 == -1) {
          tb.add(addZone(num, 0, new TokenBuilder("00"))).add(':');
          tb.add(addZone(num, 1, new TokenBuilder("00")));
        } else if(z2 == -1) {
          tb.add(addZone(num, 0, new TokenBuilder().add(c1)));
          if(c2 == -1) {
            if(num % 60 != 0) tb.add(':').add(addZone(num, 1, new TokenBuilder("00")));
          } else {
            final TokenBuilder t = new TokenBuilder().add(z3 == -1 ? '0' : z3);
            if(z3 != -1 && z4 != -1) t.add(z4);
            tb.add(c2).add(addZone(num, 1, t));
          }
        } else if(z3 == -1) {
          tb.add(addZone(num, 0, new TokenBuilder().add(c1).add(c2)));
          if(c3 == -1) {
            if(num % 60 != 0) tb.add(':').add(addZone(num, 1, new TokenBuilder("00")));
          } else {
            final int c5 = tp.next(), z5 = zeroes(c5);
            final TokenBuilder t = new TokenBuilder().add(z4 == -1 ? '0' : z4);
            if(z4 != -1 && z5 != -1) t.add(z5);
            tb.add(c3).add(addZone(num % 60, 1, t));
          }
        } else if(z4 == -1) {
          tb.add(addZone(num, 0, new TokenBuilder().add(c1)));
          tb.add(addZone(num, 1, new TokenBuilder().add(c2).add(c3)));
        } else {
          tb.add(addZone(num, 0, new TokenBuilder().add(c1).add(c2)));
          tb.add(addZone(num, 1, new TokenBuilder().add(c3).add(c4)));
        }
      }
    }
    return tb.finish();
  }

  /**
   * Returns a timezone component.
   * @param num number to be formatted
   * @param c counter
   * @param format presentation format
   * @return timezone component
   * @throws QueryException query exception
   */
  private byte[] addZone(final int num, final int c, final TokenBuilder format)
      throws QueryException {

    int n = c == 0 ? num / 60 : num % 60;
    if(num < 0) n = -n;
    return number(n, new IntFormat(format.finish(), null), zeroes(format.cp(0)));
  }

  /**
   * Adds a military timezone component to the specified token builder.
   * @param num number to be formatted
   * @param tb token builder
   * @return {@code true} if timezone was added
   */
  private static boolean addMilZone(final int num, final TokenBuilder tb) {
    final int n = num / 60;
    if(num % 60 != 0 || n < -12 || n > 12) return false;
    tb.add(MIL[n + 12]);
    return true;
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
   * @param min minimum width
   */
  private static void roman(final TokenBuilder tb, final long n, final int min) {
    final int s = tb.size();
    if(n > 0 && n < 4000) {
      final int v = (int) n;
      tb.add(ROMANM[v / 1000]);
      tb.add(ROMANC[v / 100 % 10]);
      tb.add(ROMANX[v / 10 % 10]);
      tb.add(ROMANI[v % 10]);
    } else {
      tb.addLong(n);
    }
    while(tb.size() - s < min) tb.add(' ');
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
