package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Abstract class for formatting data in different languages.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Formatter extends FormatUtil {
  /** Military timezones. */
  private static final byte[] MIL = token("YXWVUTSRQPONZABCDEFGHIKLM");
  /** Token: n. */
  private static final byte[] N = { 'n' };
  /** Allowed calendars. */
  private static final byte[][] CALENDARS = tokens(
    "ISO", "AD", "AH", "AME", "AM", "AP", "AS", "BE", "CB", "CE", "CL", "CS", "EE", "FE",
    "JE", "KE", "KY", "ME", "MS", "NS", "OS", "RS", "SE", "SH", "SS", "TE", "VE", "VS");

  /** Default language: English. */
  public static final byte[] EN = token("en");
  /** Formatter instances. */
  private static final TokenObjMap<Formatter> MAP = new TokenObjMap<>();

  // initialize hash map with English formatter as default
  static {
    MAP.put(EN, new FormatterEN());
    MAP.put(token("de"), new FormatterDE());
  }

  /**
   * Returns a formatter for the specified language.
   * @param language language
   * @return formatter instance
   */
  public static Formatter get(final byte[] language) {
    final Formatter form = MAP.get(language);
    return form != null ? form : MAP.get(EN);
  }

  /**
   * Returns a word representation for the specified number.
   * @param n number to be formatted
   * @param ord ordinal suffix
   * @return token
   */
  protected abstract byte[] word(long n, byte[] ord);

  /**
   * Returns an ordinal representation for the specified number.
   * @param n number to be formatted
   * @param ord ordinal suffix
   * @return ordinal
   */
  protected abstract byte[] ordinal(long n, byte[] ord);

  /**
   * Returns the specified month (0-11).
   * @param n number to be formatted
   * @param min minimum length
   * @param max maximum length
   * @return month
   */
  protected abstract byte[] month(int n, int min, int max);

  /**
   * Returns the specified day of the week (0-6, Sunday-Saturday).
   * @param n number to be formatted
   * @param min minimum length
   * @param max maximum length
   * @return day of week
   */
  protected abstract byte[] day(int n, int min, int max);

  /**
   * Returns the am/pm marker.
   * @param am am flag
   * @return am/pm marker
   */
  protected abstract byte[] ampm(boolean am);

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
  protected abstract byte[] era(long year);

  /**
   * Formats the specified date.
   * @param date date to be formatted
   * @param language language
   * @param picture picture
   * @param calendar calendar (can be {@code null})
   * @param place place
   * @param sc static context
   * @param ii input info
   * @return formatted string
   * @throws QueryException query exception
   */
  public final byte[] formatDate(final ADate date, final byte[] language, final byte[] picture,
      final byte[] calendar, final byte[] place, final StaticContext sc, final InputInfo ii)
      throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    if(language.length != 0 && MAP.get(language) == null) tb.add("[Language: en]");
    if(calendar != null) {
      final QNm qnm;
      try {
        qnm = QNm.resolve(trim(calendar), sc);
      } catch(final QueryException ex) {
        throw CALWHICH_X.get(ii, calendar);
      }
      if(qnm.uri().length == 0) {
        int c = -1;
        final byte[] ln = qnm.local();
        final int cl = CALENDARS.length;
        while(++c < cl && !eq(CALENDARS[c], ln));
        if(c == cl) throw CALWHICH_X.get(ii, calendar);
        if(c > 1) tb.add("[Calendar: AD]");
      }
    }
    if(place.length != 0) tb.add("[Place: ]");

    final DateParser dp = new DateParser(ii, picture);
    while(dp.more()) {
      final int ch = dp.literal();
      if(ch == -1) {
        // retrieve variable marker
        final byte[] marker = dp.marker();
        if(marker.length == 0) throw PICDATE_X.get(ii, picture);

        // parse component specifier
        final int compSpec = ch(marker, 0);
        byte[] pres = ONE;
        boolean max = false;
        BigDecimal frac = null;
        long num = 0;

        final boolean dat = date.type == AtomType.DATE, tim = date.type == AtomType.TIME;
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
            pres = N;
            err = tim;
            break;
          case 'W':
            num = date.toJava().toGregorianCalendar().get(Calendar.WEEK_OF_YEAR);
            err = tim;
            break;
          case 'w':
            num = date.toJava().toGregorianCalendar().get(Calendar.WEEK_OF_MONTH);
            // first week of month: fix value, according to ISO 8601
            if(num == 0) num = new Dtm(new Dtm(date), new DTDur(date.day() * 24, 0), false, ii).
                toJava().toGregorianCalendar().get(Calendar.WEEK_OF_MONTH);
            err = tim;
            break;
          case 'H':
            num = date.hour();
            err = dat;
            break;
          case 'h':
            num = date.hour() % 12;
            if(num == 0) num = 12;
            err = dat;
            break;
          case 'P':
            num = date.hour() / 12;
            pres = N;
            err = dat;
            break;
          case 'm':
            num = date.minute();
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
            num = date.tz();
            pres = token("01:01");
            break;
          case 'C':
            pres = N;
            break;
          case 'E':
            num = date.yea();
            pres = N;
            err = tim;
            break;
          default:
            throw INVCOMPSPEC_X.get(ii, marker);
        }
        if(err) throw PICINVCOMP_X_X.get(ii, marker, date.type);
        if(pres == null) continue;

        // parse presentation modifier(s) and width modifier
        final DateFormat fp = new DateFormat(substring(marker, 1), pres, ii);
        if(max && fp.max == Integer.MAX_VALUE) {
          // limit maximum length of numeric output
          int mx = 0;
          final int fl = fp.primary.length;
          for(int s = 0; s < fl; s += cl(fp.primary, s)) mx++;
          if(mx > 1) fp.max = mx;
        }

        if(compSpec == 'z' || compSpec == 'Z') {
          // output timezone
          tb.add(formatZone((int) num, fp, marker));
        } else if(fp.first == 'n') {
          // output name representation
          byte[] in = null;
          switch(compSpec) {
            case 'M': in = month((int) num - 1, fp.min, fp.max); break;
            case 'F': in = day((int) num - 1, fp.min, fp.max); break;
            case 'P': in = ampm(num == 0); break;
            case 'C': in = calendar(); break;
            case 'E': in = era((int) num); break;
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
        } else if(frac != null) {
          String s = frac.toString().replace("0.", "").replaceAll("0+$", "");
          if(frac.compareTo(BigDecimal.ZERO) != 0) {
            final int sl = s.length();
            if(fp.min > sl) {
              s = frac(frac, fp.min);
            } else if(fp.max < sl) {
              s = frac(frac, fp.max);
            } else {
              final int fl = length(fp.primary);
              if(fl != 1 && fl != sl) s = frac(frac, fl);
            }
          }
          tb.add(number(token(s), fp, fp.first));
        } else {
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
    final String s = num.setScale(len, RoundingMode.DOWN).toString();
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
      else tb.add(number(n, fp, ch));
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
  private byte[] formatZone(final int num, final FormatParser fp, final byte[] marker)
      throws QueryException {

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
          tb.add(addZone(num, 0, new TokenBuilder().add("00"))).add(':');
          tb.add(addZone(num, 1, new TokenBuilder().add("00")));
        } else if(z2 == -1) {
          tb.add(addZone(num, 0, new TokenBuilder().add(c1)));
          if(c2 == -1) {
            if(num % 60 != 0) tb.add(':').add(addZone(num, 1, new TokenBuilder().add("00")));
          } else {
            final TokenBuilder t = new TokenBuilder().add(z3 == -1 ? '0' : z3);
            if(z3 != -1 && z4 != -1) t.add(z4);
            tb.add(c2).add(addZone(num, 1, t));
          }
        } else if(z3 == -1) {
          tb.add(addZone(num, 0, new TokenBuilder().add(c1).add(c2)));
          if(c3 == -1) {
            if(num % 60 != 0) tb.add(':').add(addZone(num, 1, new TokenBuilder().add("00")));
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
    return number(n, new IntFormat(format.toArray(), null), format.cp(0));
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
    final int sz = tb.size();
    if(n > 0 && n < 4000) {
      final int v = (int) n;
      tb.add(ROMANM[v / 1000]);
      tb.add(ROMANC[v / 100 % 10]);
      tb.add(ROMANX[v / 10 % 10]);
      tb.add(ROMANI[v % 10]);
    } else {
      tb.addLong(n);
    }
    while(tb.size() - sz < min) tb.add(' ');
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
   * @param first first digit
   * @return number character sequence
   */
  private byte[] number(final long num, final FormatParser fp, final int first) {
    final byte[] n = number(token(num), fp, first);
    return concat(n, ordinal(num, fp.ordinal));
  }

  /**
   * Creates a number character sequence.
   * @param num number to be formatted
   * @param fp format parser
   * @param first first digit
   * @return number character sequence
   */
  private static byte[] number(final byte[] num, final FormatParser fp, final int first) {
    final int zero = zeroes(first);

    // cache characters of presentation modifier
    final int[] mod = new TokenParser(fp.primary).toArray();
    final int modSize = mod.length;
    int modStart = 0;
    while(modStart < modSize && mod[modStart] == '#') modStart++;

    // try to find regular separator pattern
    int sepPos = -1, sepChar = -1, digitPos = 0;
    boolean regSep = false;
    for(int mp = modSize - 1; mp >= modStart; --mp) {
      final int ch = mod[mp];
      if(ch >= zero && ch <= zero + 9) {
        digitPos = mp;
        continue;
      }
      if(ch == '#') continue;
      if(sepPos == -1) {
        sepPos = modSize - mp;
        sepChar = ch;
        regSep = true;
      } else if(regSep) {
        regSep = (modSize - mp) % sepPos == 0 && ch == sepChar;
      }
    }
    if(!regSep) sepPos = Integer.MAX_VALUE;

    // cache characters in reverse order
    final IntList reverse = new IntList();
    int inPos = num.length - 1, modPos = modSize - 1;

    // add numbers and separators
    int min = fp.min, max = fp.max;
    while((--min >= 0 || inPos >= 0 || modPos >= modStart) && --max >= 0) {
      final boolean sep = reverse.size() % sepPos == sepPos - 1;
      int ch;
      if(modPos >= modStart) {
        ch = mod[modPos--];
        if(inPos >= 0) {
          if(ch == '#' && sep) reverse.add(sepChar);
          if(ch == '#' || ch >= zero && ch <= zero + 9) ch = num[inPos--] - '0' + zero;
        } else {
          // add remaining modifiers
          if(ch == '#') break;
          if(ch >= zero && ch <= zero + 9) ch = zero;
          if(regSep && modPos + 1 < digitPos) break;
        }
      } else if(inPos >= 0) {
        // add remaining numbers
        if(sep) reverse.add(sepChar);
        ch = num[inPos--] - '0' + zero;
      } else {
        // add minimum number of digits
        ch = zero;
      }
      reverse.add(ch);
    }
    while(min-- >= 0) reverse.add(zero);

    // reverse result and add ordinal suffix
    final TokenBuilder result = new TokenBuilder();
    for(int rs = reverse.size() - 1; rs >= 0; --rs) result.add(reverse.get(rs));
    return result.finish();
  }
}
