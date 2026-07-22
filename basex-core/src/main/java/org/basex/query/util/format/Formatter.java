package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.time.*;
import java.time.temporal.*;
import java.time.zone.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.util.format.FormatParser.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Abstract class for formatting data in different languages.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Formatter extends FormatUtil {
  /** Military timezones. */
  private static final byte[] MIL = token("YXWVUTSRQPONZABCDEFGHIKLM");
  /** Token: n. */
  private static final byte[] N = cpToken('n');
  /** Token: 1. */
  private static final byte[] ONE = cpToken('1');
  /** Presentation modifier: two digits. */
  private static final byte[] TWO_DIGITS = token("01");
  /** Presentation modifier: timezone. */
  private static final byte[] ZONE_DIGITS = token("01:01");
  /** Default digit pattern of a timezone component. */
  private static final byte[] ZONE_PATTERN = token("00");
  /** Components that are not available in times. */
  private static final byte[] DATE_COMPONENTS = token("YMDdFWwE");
  /** Components that are not available in dates. */
  private static final byte[] TIME_COMPONENTS = token("HhPmsf");
  /** Japanese factors (kanji offset: 10 + array index). */
  private static final long[] JAPANESE = { 10, 100, 1000, 10000, 100000000L, 1000000000000L,
    10000000000000000L };
  /** Allowed calendars. */
  private static final byte[][] CALENDARS = tokens(
    "ISO", "AD", "AH", "AME", "AM", "AP", "AS", "BE", "CB", "CE", "CL", "CS", "EE", "FE",
    "JE", "KE", "KY", "ME", "MS", "NS", "OS", "RS", "SE", "SH", "SS", "TE", "VE", "VS");

  /** Default language: English. */
  public static final byte[] EN = token("en");
  /** Formatter instances. */
  private static final TokenObjectMap<Formatter> MAP = new TokenObjectMap<>();

  // initialize hash map with English formatter as default
  static {
    MAP.put(EN, new FormatterEN());
    MAP.put(token("de"), new FormatterDE());
    MAP.put(token("fr"), new FormatterFR());
  }

  /**
   * Returns a formatter for the specified language.
   * @param languageTag language tag
   * @return formatter instance
   */
  public static Formatter get(final byte[] languageTag) {
    if(Prop.ICU) return IcuFormatter.get(languageTag);
    final Formatter form = getInternal(languageTag);
    return form != null ? form : getInternal(EN);
  }

  /**
   * Returns an internal formatter for the specified language.
   * @param languageTag language tag
   * @return formatter instance, or {@code null} if not implemented
   */
  protected static Formatter getInternal(final byte[] languageTag) {
    final int i = indexOf(languageTag, '-');
    return i < 0 ? MAP.get(languageTag) : MAP.get(substring(languageTag, 0, i));
  }

  /**
   * Checks whether a formatter is available for the specified language.
   * @param languageTag language tag
   * @return true if the language is supported
   */
  public static boolean available(final byte[] languageTag) {
    return Prop.ICU ? IcuFormatter.available(languageTag) : getInternal(languageTag) != null;
  }

  /**
   * Returns a word representation for the specified number.
   * @param n number to be formatted
   * @param numType numeral type
   * @param suffix suffix
   * @return token
   */
  protected abstract byte[] word(long n, NumeralType numType, byte[] suffix);

  /**
   * Returns a suffix for the representation of the specified number.
   * @param n number to be formatted
   * @param numType numeral type
   * @return ordinal
   */
  protected abstract byte[] suffix(long n, NumeralType numType);

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
   * @param dt date to be formatted
   * @param languageTag language tag
   * @param picture picture
   * @param calendar calendar (can be {@code null})
   * @param place place
   * @param info input info (can be {@code null})
   * @return formatted string
   * @throws QueryException query exception
   */
  public final byte[] formatDate(final ADate dt, final byte[] languageTag, final byte[] picture,
      final byte[] calendar, final byte[] place, final InputInfo info) throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    if(languageTag.length != 0 && !available(languageTag)) tb.add("[Language: en]");
    if(calendar != null && !supported(calendar, info)) tb.add("[Calendar: AD]");

    // adopt IANA timezone, and remember its name (standard time for xs:time, otherwise DST-aware)
    ADate date = dt;
    byte[] zone = null;
    final ZoneId id = zoneId(place);
    if(id != null) {
      final ZoneRules rules = id.getRules();
      final boolean time = dt.type == BasicType.TIME;
      final Instant instant = time ? Instant.now() :
        dt.toLocalDateTime().atZone(zoneId(dt)).toInstant();
      final ZoneOffset offset = time ? rules.getStandardOffset(instant) : rules.getOffset(instant);
      date = dt.timeZone(DTDur.get(offset.getTotalSeconds() * 1000L), false, info);
      final Locale locale = languageTag.length == 0 ? Locale.ENGLISH :
        Locale.forLanguageTag(string(languageTag));
      zone = token(TimeZone.getTimeZone(id).getDisplayName(
        !time && rules.isDaylightSavings(instant), TimeZone.SHORT, locale));
    }

    final DateParser dp = new DateParser(info, picture);
    while(dp.more()) {
      final int ch = dp.literal();
      if(ch != -1) {
        // print literal
        tb.add(ch);
      } else {
        // retrieve and format variable marker
        final byte[] marker = dp.marker();
        if(marker.length == 0) throw PICDATE_X.get(info, picture);
        tb.add(component(marker, date, zone, info));
      }
    }
    return tb.finish();
  }

  /**
   * Formats a variable marker of a date picture.
   * @param marker variable marker
   * @param date date
   * @param zone timezone name (can be {@code null})
   * @param info input info (can be {@code null})
   * @return formatted component
   * @throws QueryException query exception
   */
  private byte[] component(final byte[] marker, final ADate date, final byte[] zone,
      final InputInfo info) throws QueryException {

    // reject components that are not available in the supplied value
    final int comp = ch(marker, 0);
    final Type type = date.type;
    final byte[] rejected = type == BasicType.DATE ? TIME_COMPONENTS :
      type == BasicType.TIME ? DATE_COMPONENTS : EMPTY;
    if(indexOf(rejected, comp) != -1) throw PICINVCOMP_X_X_X.get(info, marker, type, date);

    // evaluate component, choose default presentation modifier
    byte[] pres = ONE;
    BigDecimal frac = null;
    long num = 0;
    switch(comp) {
      case 'Y' -> num = Math.abs(date.yea());
      case 'M' -> num = date.mon();
      case 'D' -> num = date.day();
      case 'd' -> {
        final long year = date.yea();
        for(int m = (int) date.mon(); --m >= 1;) num += ADate.daysOfMonth(year, m);
        num += date.day();
      }
      case 'F' -> {
        num = date.toLocalDate().getDayOfWeek().getValue();
        pres = N;
      }
      case 'W' -> num = date.toLocalDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
      case 'w' -> {
        final TemporalField wom = WeekFields.ISO.weekOfMonth();
        final LocalDate ld = date.toLocalDate();
        num = ld.get(wom);
        // first week of month: use last week of previous month, according to ISO 8601
        if(num == 0) num = ld.minusDays(ld.getDayOfMonth()).get(wom);
      }
      case 'H' -> num = date.hour();
      case 'h' -> {
        num = date.hour() % 12;
        if(num == 0) num = 12;
      }
      case 'P' -> {
        num = date.hour() / 12;
        pres = N;
      }
      case 'm' -> {
        num = date.minute();
        pres = TWO_DIGITS;
      }
      case 's' -> {
        num = date.seconds().intValue();
        pres = TWO_DIGITS;
      }
      case 'f' -> frac = date.seconds().remainder(BigDecimal.ONE);
      case 'Z', 'z' -> {
        num = date.tz();
        pres = ZONE_DIGITS;
      }
      case 'C' -> pres = N;
      case 'E' -> {
        num = date.yea();
        pres = N;
      }
      default -> throw INVCOMPSPEC_X.get(info, marker);
    }

    // parse presentation modifier(s) and width modifier
    final DateFormat fp = new DateFormat(substring(marker, 1), pres, frac != null, info);
    if(comp == 'Y') num = year(num, fp);

    if(comp == 'Z' || comp == 'z') {
      // output timezone (as name if requested via 'N' and a place is known)
      final byte[] tz = fp.first == 'n' && zone != null ? zone : formatZone((int) num, fp, marker);
      // pad to minimum width; the representation is never shortened
      return tz.length == 0 ? tz : format(tz, fp.min, Integer.MAX_VALUE);
    }
    if(fp.first == 'n') {
      // output name representation
      byte[] name = switch(comp) {
        case 'M' -> month((int) num - 1, fp.min, fp.max);
        case 'F' -> day((int) num - 1, fp.min, fp.max);
        case 'P' -> ampm(num == 0);
        case 'C' -> calendar();
        case 'E' -> era((int) num);
        default -> null;
      };
      if(name != null) {
        if(fp.cs == Case.LOWER) name = lc(name);
        else if(fp.cs == Case.UPPER) name = uc(name);
        return name;
      }
      // fallback representation
      fp.first = '0';
      fp.primary = ONE;
    } else if(frac != null) {
      return formatFrac(frac, fp);
    }
    // integer-valued component: minimum width is enforced, maximum width is ignored
    fp.max = Integer.MAX_VALUE;
    return formatInt(num, fp);
  }

  /**
   * Checks if the specified calendar is supported.
   * @param calendar calendar
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean supported(final byte[] calendar, final InputInfo info)
      throws QueryException {

    final QNm qnm;
    try {
      qnm = QNm.parse(trim(calendar), null, info.sc());
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw CALWHICH_X.get(info, calendar);
    }
    if(qnm.uri().length != 0) return true;

    final byte[] local = qnm.local();
    int c = -1;
    final int cl = CALENDARS.length;
    while(++c < cl && !eq(CALENDARS[c], local));
    if(c == cl) throw CALWHICH_X.get(info, calendar);
    // only ISO and AD are supported
    return c <= 1;
  }

  /**
   * Reduces a year to the number of digits requested by the format
   * (see: Formatting the Year Component).
   * @param year year
   * @param fp date format
   * @return reduced year
   */
  private long year(final long year, final DateFormat fp) {
    int n = fp.max;
    if(n == Integer.MAX_VALUE && zeroes(fp.first) != -1) {
      // decimal-digit-pattern: number of mandatory and optional digit signs, if 2 or more
      final int w = digits(fp.primary, false) + digits(fp.primary, true);
      if(w > 1) n = w;
    }
    if(n < 1 || n > 18) return year;
    long p = 1;
    while(n-- > 0) p *= 10;
    return year % p;
  }

  /**
   * Counts the digit signs of a presentation modifier.
   * @param primary primary format token
   * @param optional count optional ({@code #}) instead of mandatory digit signs
   * @return number of digit signs
   */
  private int digits(final byte[] primary, final boolean optional) {
    int count = 0;
    for(final TokenParser tp = new TokenParser(primary); tp.more();) {
      final int cp = tp.next();
      if(optional ? cp == '#' : zeroes(cp) != -1) ++count;
    }
    return count;
  }

  /**
   * Returns the formatted fractional part of a decimal number.
   * @param num number
   * @param fp date format
   * @return the formatted number
   */
  private byte[] formatFrac(final BigDecimal num, final DateFormat fp) {
    String s = num.toString().replace("0.", "").replaceAll("0+$", "");

    // count optional and mandatory digit signs
    int od = digits(fp.primary, true);
    final int md = digits(fp.primary, false);

    // calculate number of target digits, including trailing zeroes
    final int sl = s.length();
    int fl = md + od;
    if(fl == 1) fl = sl;

    // adjust min/max with mandatory digit count
    fp.max = Math.max(fp.max, md);
    fp.min = Math.max(fp.min, md);

    // adjust number of target digits
    fl = Math.max(fl, fp.min);
    if(fp.max != Integer.MAX_VALUE) {
      od = fp.max - fp.min;
      fl = fp.max;
    }

    // force calculated length
    if(fl != sl) {
      final String scaled = num.setScale(fl, RoundingMode.DOWN).toPlainString();
      final int d = scaled.indexOf('.');
      s = d == -1 ? scaled : scaled.substring(d + 1);
    }

    // format number
    byte[] number = number(token(s), fp, fp.first);

    // truncate trailing zeroes
    if(od > 0 && s.endsWith("0")) {
      final String ns = string(number);
      int nsi = ns.length();
      for(int dc = 0; dc <= od;) {
        final int cp = ns.charAt(--nsi);
        final int zero = zeroes(cp);
        if(zero != -1) {
          ++dc;
          if(cp != zero) break;
        }
      }
      if(nsi + 1 != ns.length()) number = token(ns.substring(0, nsi + 1));
    }
    return number;
  }

  /**
   * Returns a formatted integer.
   * @param num integer to be formatted
   * @param fp format parser
   * @return string representation
   */
  public final byte[] formatInt(final long num, final FormatParser fp) {
    // prepend minus sign to negative values
    final boolean sign = num < 0;
    final long n = sign ? -num : num;

    final TokenBuilder tb = new TokenBuilder();
    final int ch = fp.first;
    if(ch == 'w') {
      tb.add(word(n, fp.numType, fp.modifier));
    } else if(ch == KANJI[1]) {
      if(n == 0) tb.add(KANJI[0]);
      else japanese(tb, n, false);
    } else if(ch == 'i') {
      roman(tb, n, fp.min);
    } else if(ch == '\u2460' || ch == '\u2474' || ch == '\u2488') {
      // circled, parenthesized and dotted digits are limited to the range 1-20
      if(n < 1 || n > 20) tb.addLong(n);
      else tb.add((int) (ch + n - 1));
    } else {
      final String seq = sequence(ch);
      if(seq != null) alpha(tb, n, seq);
      else tb.add(number(n, fp, ch));
    }

    // finalize formatted string
    byte[] in = tb.finish();
    if(fp.cs == Case.LOWER) in = lc(in);
    else if(fp.cs == Case.UPPER) in = uc(in);
    return sign ? concat(cpToken('-'), in) : in;
  }

  /**
   * Returns a formatted timezone.
   * @param num timezone (minutes)
   * @param fp format parser
   * @param marker variable marker
   * @return string representation
   * @throws QueryException query exception
   */
  private byte[] formatZone(final int num, final FormatParser fp, final byte[] marker)
      throws QueryException {

    final boolean uc = ch(marker, 0) == 'Z', mil = uc && ch(marker, 1) == 'Z';
    // ignore values without timezone. exception: military timezone
    if(num == Short.MAX_VALUE) return mil ? cpToken('J') : EMPTY;
    // military timezone: single letter, restricted to full hours
    if(mil && num % 60 == 0 && num >= -720 && num <= 720) return cpToken(MIL[num / 60 + 12]);

    final TokenBuilder tb = new TokenBuilder();
    if(!uc) tb.add("GMT");
    if(fp.trad && num == 0) return tb.add('Z').finish();
    tb.add(num < 0 ? '-' : '+');

    // split digit pattern into hours, separator and minutes
    final int[] cps = cps(fp.primary);
    final int cl = cps.length;
    int digits = 0;
    while(digits < cl && zeroes(cps[digits]) != -1) ++digits;

    final TokenBuilder hours = new TokenBuilder(), minutes = new TokenBuilder();
    int sep = 0;
    if(digits == 0) {
      // no digits: use default pattern
      hours.add(ZONE_PATTERN);
      minutes.add(ZONE_PATTERN);
      sep = ':';
    } else if(digits > 2) {
      // three or more digits: the last two ones represent the minutes
      final int hl = Math.min(digits - 2, 2);
      for(int c = 0; c < hl; c++) hours.add(cps[c]);
      minutes.add(cps[hl]).add(cps[hl + 1]);
    } else {
      for(int c = 0; c < digits; c++) hours.add(cps[c]);
      if(digits < cl) {
        // digits are followed by a separator and by up to two minute digits
        sep = cps[digits];
        for(int c = digits + 1; c <= digits + 2 && c < cl; c++) {
          final int zero = zeroes(cps[c]);
          if(zero == -1) break;
          minutes.add(zero);
        }
        if(minutes.isEmpty()) minutes.add('0');
      } else if(num % 60 != 0) {
        // no separator: add minutes only if required
        minutes.add(ZONE_PATTERN);
        sep = ':';
      }
    }

    tb.add(zoneValue(num, true, hours.finish()));
    if(!minutes.isEmpty()) {
      if(sep != 0) tb.add(sep);
      tb.add(zoneValue(num, false, minutes.finish()));
    }
    return tb.finish();
  }

  /**
   * Returns a timezone component.
   * @param num timezone (minutes)
   * @param hours format hours (otherwise, minutes)
   * @param pattern digit pattern
   * @return timezone component
   * @throws QueryException query exception
   */
  private byte[] zoneValue(final int num, final boolean hours, final byte[] pattern)
      throws QueryException {
    return number(Math.abs(hours ? num / 60 : num % 60), new IntFormat(pattern, null),
        ch(pattern, 0));
  }

  /**
   * Returns a character sequence based on the specified alphabet.
   * @param tb token builder
   * @param n number to be formatted
   * @param a alphabet
   */
  private static void alpha(final TokenBuilder tb, final long n, final String a) {
    final int al = a.length();
    final long m = n - 1;
    if(m >= al) alpha(tb, m / al, a);
    if(m >= 0) tb.add(a.charAt((int) (m % al)));
    else tb.add('0');
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
   * Recursively adds a Japanese character sequence.
   * @param tb token builder
   * @param n number to be formatted
   * @param initial initial call
   */
  private static void japanese(final TokenBuilder tb, final long n, final boolean initial) {
    if(n == 0) return;
    if(n <= 9) {
      if(n != 1 || !initial) tb.add(KANJI[(int) n]);
    } else {
      final int jl = JAPANESE.length - 1;
      int j = 0;
      while(j < jl && n >= JAPANESE[j + 1]) ++j;
      if(j == jl) {
        tb.addLong(n);
      } else {
        final long f = JAPANESE[j];
        japanese(tb, n / f, true);
        tb.add(KANJI[10 + j]);
        japanese(tb, n % f, false);
      }
    }
  }

  /**
   * Returns the timezone of a date, or the system timezone if the date has none.
   * @param date date
   * @return timezone
   */
  private static ZoneId zoneId(final ADate date) {
    return date.hasTz() ? ZoneOffset.ofTotalSeconds(date.tz() * 60) : ZoneId.systemDefault();
  }

  /**
   * Returns the timezone with the specified IANA name.
   * @param place place
   * @return timezone, or {@code null} if the place is no supported IANA timezone name
   */
  private static ZoneId zoneId(final byte[] place) {
    if(contains(place, '/')) {
      try {
        return ZoneId.of(string(place));
      } catch(final ZoneRulesException ex) {
        Util.debug(ex);
      }
    }
    return null;
  }

  /**
   * Creates a number character sequence.
   * @param num number to be formatted
   * @param fp format parser
   * @param first first digit
   * @return number character sequence
   */
  private byte[] number(final long num, final FormatParser fp, final int first) {
    final byte[] n = number(token(num, fp.radix), fp, first);
    return concat(n, suffix(num, fp.numType));
  }

  /**
   * Creates a number character sequence.
   * @param num number to be formatted
   * @param fp format parser
   * @param first first digit
   * @return number character sequence
   */
  private static byte[] number(final byte[] num, final FormatParser fp, final int first) {
    final int zero = fp.zeroes(first);

    // cache characters of presentation modifier
    final int[] mod = cps(fp.primary);
    final int modSize = mod.length;
    int modStart = 0;
    while(modStart < modSize && mod[modStart] == '#') modStart++;

    // try to find regular separator pattern
    int sepPos = -1, sepChar = -1, digitPos = 0;
    boolean regSep = false;
    for(int mp = modSize - 1; mp >= modStart; --mp) {
      final int ch = mod[mp];
      if(fp.digit(ch, zero)) {
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
          if(ch == '#' || fp.digit(ch, zero)) {
            final int n = num[inPos--];
            ch = fp.radix == 10 ? zero + n - '0' : n;
          }
        } else {
          // add remaining modifiers
          if(ch == '#') break;
          if(fp.digit(ch, zero)) ch = zero;
          if(modPos + 1 < digitPos) break;
        }
      } else if(inPos >= 0) {
        // add remaining numbers
        if(sep) reverse.add(sepChar);
        final int n = num[inPos--];
        ch = fp.radix == 10 ? zero + n - '0' : n;
      } else {
        // add minimum number of digits
        ch = zero;
      }
      reverse.add(ch);
    }
    while(min-- >= 0) reverse.add(zero);

    // reverse result
    final TokenBuilder result = new TokenBuilder();
    for(int rs = reverse.size() - 1; rs >= 0; --rs) result.add(reverse.get(rs));
    return result.finish();
  }

  /**
   * Formats a token.
   * @param token token
   * @param min minimum size
   * @param max maximum size
   * @return resulting token
   */
  static byte[] format(final byte[] token, final int min, final int max) {
    if(min == 0 && max == Integer.MAX_VALUE) return token;

    final int mx = Math.max(3, max);
    final TokenBuilder tb = new TokenBuilder(mx);
    final TokenParser tp = new TokenParser(token);
    int p = -1;
    while(++p < mx && tp.more()) tb.add(tp.next());
    while(p++ < min) tb.add(' ');
    return tb.finish();
  }
}
