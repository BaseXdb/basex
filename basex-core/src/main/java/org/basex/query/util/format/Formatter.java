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
  /** Components that are not available in times. */
  private static final byte[] DATE_COMPONENTS = token("YMDdFWwE");
  /** Components that are not available in dates. */
  private static final byte[] TIME_COMPONENTS = token("HhPmsf");
  /** Japanese factors (kanji offset: 10 + array index). */
  private static final long[] JAPANESE = { 10, 100, 1000, 10000, 100000000L, 1000000000000L,
    10000000000000000L };
  /** Roman numerals for each digit of thousands, hundreds, tens and ones. */
  private static final byte[][][] ROMAN = {
    tokens("", "M", "MM", "MMM"),
    tokens("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"),
    tokens("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"),
    tokens("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")
  };
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

  /** Months (January-December). */
  private final byte[][] months;
  /** Names of weekdays (Monday-Sunday), each ordered by length. */
  final byte[][][] days;
  /** AM/PM markers. */
  private final byte[][] ampm;
  /** Eras (BC, AD). */
  private final byte[][] eras;

  /**
   * Constructor.
   * @param months months (January-December)
   * @param days names of weekdays (Monday-Sunday), each ordered by length
   * @param ampm am/pm markers
   * @param eras eras (BC, AD)
   */
  Formatter(final byte[][] months, final byte[][][] days, final byte[][] ampm,
      final byte[][] eras) {
    this.months = months;
    this.days = days;
    this.ampm = ampm;
    this.eras = eras;
  }

  /**
   * Returns a formatter for the specified language.
   * @param languageTag language tag
   * @return formatter instance
   */
  public static Formatter get(final byte[] languageTag) {
    if(ExternalLib.ICU.available()) return IcuFormatter.get(languageTag);
    final Formatter form = getInternal(languageTag);
    return form != null ? form : getInternal(EN);
  }

  /**
   * Returns an internal formatter for the specified language.
   * @param languageTag language tag
   * @return formatter instance, or {@code null} if not implemented
   */
  protected static Formatter getInternal(final byte[] languageTag) {
    // language tags are case-insensitive
    final int i = indexOf(languageTag, '-');
    return MAP.get(lc(i < 0 ? languageTag : substring(languageTag, 0, i)));
  }

  /**
   * Checks whether a formatter is available for the specified language.
   * @param languageTag language tag
   * @return true if the language is supported
   */
  public static boolean available(final byte[] languageTag) {
    return ExternalLib.ICU.available() ? IcuFormatter.available(languageTag) :
      getInternal(languageTag) != null;
  }

  /**
   * Returns a word representation for the specified number.
   * @param n number to be formatted
   * @param numType numeral type
   * @param modifier format modifier (can be {@code null})
   * @return token, or {@code null} if the number is outside the supported range
   */
  protected abstract byte[] word(long n, NumeralType numType, byte[] modifier);

  /**
   * Indicates if ordinal words of this formatter are preferred to those of ICU.
   * @return result of check
   */
  protected boolean ordinalWords() {
    return false;
  }

  /**
   * Returns a suffix for the representation of the specified number.
   * @param n number to be formatted
   * @param numType numeral type
   * @return ordinal
   */
  protected abstract byte[] suffix(long n, NumeralType numType);


  /**
   * Formats the specified date.
   * @param dt date to be formatted
   * @param languageTag language tag
   * @param picture parsed picture
   * @param calendar calendar (can be {@code null})
   * @param place place
   * @param info input info (can be {@code null})
   * @return formatted string
   * @throws QueryException query exception
   */
  public final byte[] formatDate(final ADate dt, final byte[] languageTag,
      final DatePicture picture, final byte[] calendar, final byte[] place, final InputInfo info)
      throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    final boolean fallback = languageTag.length != 0 && !available(languageTag);
    final boolean english = languageTag.length == 0 || fallback;
    if(fallback) tb.add("[Language: en]");
    final int cal = calendar != null ? calendar(calendar, info) : 1;
    if(cal > 1) tb.add("[Calendar: AD]");

    // adopt IANA timezone, and remember its name (standard time for xs:time, otherwise DST-aware)
    ADate date = dt;
    byte[] zone = null;
    final ZoneId id = zoneId(place);
    if(id != null) {
      final ZoneRules rules = id.getRules();
      final boolean time = dt.type == BasicType.TIME;
      // values without timezone are interpreted as local time at the place
      final Instant instant = time ? Instant.now() : dt.toLocalDateTime().atZone(
        dt.hasTz() ? ZoneOffset.ofTotalSeconds(dt.tz() * 60) : id).toInstant();
      final ZoneOffset offset = time ? rules.getStandardOffset(instant) : rules.getOffset(instant);
      // historical offsets (local mean time) are truncated to minutes
      date = dt.timeZone(DTDur.get(offset.getTotalSeconds() / 60 * 60000L), false, info);
      final Locale locale = english ? Locale.ENGLISH : Locale.forLanguageTag(string(languageTag));
      zone = token(TimeZone.getTimeZone(id).getDisplayName(
        !time && rules.isDaylightSavings(instant), TimeZone.SHORT, locale));
    }

    final DateFormat[] formats = picture.formats;
    final int fl = formats.length;
    for(int f = 0; f < fl; f++) {
      tb.add(picture.literals[f]).add(component(formats[f], date, zone, cal == 0, info));
    }
    return tb.add(picture.literals[fl]).finish();
  }

  /**
   * Formats a variable marker of a date picture.
   * @param fp date format of the variable marker
   * @param date date
   * @param zone timezone name (can be {@code null})
   * @param iso ISO calendar
   * @param info input info (can be {@code null})
   * @return formatted component
   * @throws QueryException query exception
   */
  private byte[] component(final DateFormat fp, final ADate date, final byte[] zone,
      final boolean iso, final InputInfo info) throws QueryException {

    // reject components that are not available in the supplied value
    final int comp = fp.comp;
    final Type type = date.type;
    final byte[] rejected = type == BasicType.DATE ? TIME_COMPONENTS :
      type == BasicType.TIME ? DATE_COMPONENTS : EMPTY;
    if(indexOf(rejected, comp) != -1) throw PICINVCOMP_X_X_X.get(info, fp.marker, type, date);

    // evaluate component
    long num = 0;
    switch(comp) {
      case 'Y' -> {
        // AD calendar has no year zero: 0000 is 1 BC, -0001 is 2 BC
        final long year = date.yea();
        num = year > 0 ? year : iso ? -year : 1 - year;
      }
      case 'M' -> num = date.mon();
      case 'D' -> num = date.day();
      case 'd' -> {
        final long year = date.yea();
        for(int m = (int) date.mon(); --m >= 1;) num += ADate.daysOfMonth(year, m);
        num += date.day();
      }
      case 'F' -> num = date.toLocalDate().getDayOfWeek().getValue();
      case 'W' -> num = date.toLocalDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
      case 'w' -> {
        // week in month: the week belongs to the month of its Thursday
        final LocalDate thursday = date.toLocalDate().with(ChronoField.DAY_OF_WEEK, 4);
        num = (thursday.getDayOfMonth() + 6) / 7;
      }
      case 'H' -> num = date.hour();
      case 'h' -> {
        num = date.hour() % 12;
        if(num == 0) num = 12;
      }
      case 'P' -> num = date.hour() / 12;
      case 'm' -> num = date.minute();
      case 's' -> num = date.seconds().intValue();
      case 'Z', 'z' -> num = date.tz();
      case 'E' -> num = date.yea();
    }

    if(comp == 'Y' && fp.yearModulus != 0) num %= fp.yearModulus;
    // ISO calendar: era is a minus sign for negative years
    if(comp == 'E' && iso) return num < 0 ? cpToken('-') : EMPTY;

    if(comp == 'Z' || comp == 'z') {
      // output timezone (as name if requested via 'N' and a place is known)
      final byte[] tz = fp.first == 'n' && zone != null ? letterCase(zone, fp.cs) :
        formatZone((int) num, fp);
      // pad to minimum width; the representation is never shortened
      return tz.length == 0 ? tz : format(tz, fp.min, Integer.MAX_VALUE);
    }
    if(fp.first == 'n') {
      // output name representation
      final byte[] name = switch(comp) {
        case 'M' -> months[(int) num - 1];
        case 'F' -> abbreviation(days[(int) num - 1], fp.max);
        case 'P' -> ampm[num == 0 ? 0 : 1];
        case 'C' -> iso ? CALENDARS[0] : eras[1];
        default -> eras[num <= 0 ? 0 : 1];
      };
      // names are abbreviated to the maximum width and padded to the minimum width
      return format(letterCase(name, fp.cs), fp.min, fp.max);
    }
    return comp == 'f' ? formatFrac(date.seconds().remainder(BigDecimal.ONE), fp) :
      formatInt(num, fp);
  }

  /**
   * Converts a name to the requested case.
   * @param name name
   * @param cs case
   * @return converted name
   */
  private static byte[] letterCase(final byte[] name, final Case cs) {
    return cs == Case.LOWER ? lc(name) : cs == Case.UPPER ? uc(name) : tc(name);
  }

  /**
   * Returns the index of the specified calendar (only ISO and AD are supported).
   * @param calendar calendar
   * @param info input info
   * @return index ({@code 0}: ISO, {@code 1}: AD or calendar in a namespace, otherwise unsupported)
   * @throws QueryException query exception
   */
  private static int calendar(final byte[] calendar, final InputInfo info) throws QueryException {
    final QNm qnm;
    try {
      qnm = QNm.parse(trim(calendar), null, info.sc());
    } catch(final QueryException ex) {
      throw CALWHICH_X.get(info, calendar).cause(ex);
    }
    if(qnm.uri().length != 0) return 1;

    final byte[] local = qnm.local();
    int c = -1;
    final int cl = CALENDARS.length;
    while(++c < cl && !eq(CALENDARS[c], local));
    if(c == cl) throw CALWHICH_X.get(info, calendar);
    return c;
  }

  /**
   * Returns the formatted fractional part of a decimal number.
   * @param num number
   * @param fp date format
   * @return formatted number
   */
  private byte[] formatFrac(final BigDecimal num, final DateFormat fp) {
    // digits of the fraction, without trailing zeroes
    final String plain = num.stripTrailingZeros().toPlainString();
    final int dot = plain.indexOf('.');
    String s = dot == -1 ? "" : plain.substring(dot + 1);

    // calculate number of target digits (optional and mandatory digit signs), including zeroes
    int od = fp.optional;
    final int sl = s.length();
    int fl = fp.mandatory + od;
    if(fl == 1) fl = sl;

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
    byte[] number = number(token(s), fp);

    // truncate trailing zeroes
    if(od > 0 && s.endsWith("0")) {
      final int[] cps = cps(number);
      final int cl = cps.length;
      int c = cl;
      for(int dc = 0; dc <= od && c > 0;) {
        final int cp = cps[--c];
        final int zero = zeroes(cp);
        if(zero != -1) {
          ++dc;
          if(cp != zero) break;
        }
      }
      if(c + 1 != cl) number = token(new String(cps, 0, c + 1));
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
    // ordinal suffixes only depend on the last digits (Long.MIN_VALUE has no absolute value)
    final long ord = n < 0 ? -(num % 1000) : n;

    final int ch = fp.first;
    // the absolute value of the minimum integer can only be represented with digits
    if(n < 0 && fp.zeroes(ch) == -1) return decimal(num, ord, fp.numType);

    byte[] in, suffix = EMPTY;
    if(ch == 'w') {
      in = word(n, fp.numType, fp.modifier);
      // numbers outside the supported range
      if(in == null) return decimal(num, ord, fp.numType);
    } else {
      in = symbols(n, ch, fp.min);
      if(in == null) {
        in = number(n, fp);
        suffix = suffix(ord, fp.numType);
      }
    }

    // finalize formatted string; the ordinal suffix keeps its case
    if(fp.cs == Case.LOWER) in = lc(in);
    else if(fp.cs == Case.UPPER) in = uc(in);
    else if(ch == 'w') in = capitalize(in);
    return sign || suffix.length != 0 ? concat(sign ? cpToken('-') : EMPTY, in, suffix) : in;
  }

  /**
   * Returns a decimal representation, which is the fallback for unsupported numbers.
   * @param num integer to be formatted
   * @param ord value for choosing the ordinal suffix
   * @param numType numeral type
   * @return string representation
   */
  private byte[] decimal(final long num, final long ord, final NumeralType numType) {
    return concat(token(num), suffix(ord, numType));
  }

  /**
   * Returns the representation of a number in a sequence of symbols.
   * @param n non-negative number
   * @param ch first character of the format token
   * @param min minimum width
   * @return representation, or {@code null} if the format token denotes a digit pattern
   */
  private static byte[] symbols(final long n, final int ch, final int min) {
    final int[] numbering = numbering(ch);
    final String seq = numbering == null ? sequence(ch) : null;
    if(ch != KANJI[1] && ch != 'i' && numbering == null && seq == null) return null;

    final TokenBuilder tb = new TokenBuilder();
    if(ch == KANJI[1]) {
      if(n == 0) tb.add(KANJI[0]);
      else japanese(tb, n, false);
    } else if(ch == 'i') {
      roman(tb, n, min);
    } else if(numbering != null) {
      // numbering sequences are limited to small ranges; decimal output is used as fallback
      final int cp = numbered(numbering, n);
      if(cp != -1) tb.add(cp);
      else tb.addLong(n);
    } else {
      alpha(tb, n, seq);
    }
    return tb.finish();
  }

  /**
   * Capitalizes the first letter of each word (title case of format token {@code Ww}).
   * @param words words
   * @return resulting words
   */
  private static byte[] capitalize(final byte[] words) {
    final int wl = words.length;
    final TokenBuilder tb = new TokenBuilder(wl);
    boolean start = true;
    for(int w = 0; w < wl; w += cl(words, w)) {
      final int cp = cp(words, w);
      tb.add(start ? uc(cp) : cp);
      start = ws(cp);
    }
    return tb.finish();
  }

  /**
   * Returns a formatted timezone.
   * @param num timezone (minutes)
   * @param fp date format
   * @return string representation
   */
  private byte[] formatZone(final int num, final DateFormat fp) {
    final boolean mil = ch(fp.marker, 1) == 'Z';
    // ignore values without timezone. exception: military timezone
    if(num == Short.MAX_VALUE) return mil ? cpToken('J') : EMPTY;
    // military timezone: single letter, restricted to full hours
    if(mil && num % 60 == 0 && num >= -720 && num <= 720) return cpToken(MIL[num / 60 + 12]);

    final TokenBuilder tb = new TokenBuilder();
    if(fp.comp == 'z') tb.add("GMT");
    if(fp.trad && num == 0) return tb.add('Z').finish();
    tb.add(num < 0 ? '-' : '+');

    // split digit pattern into hours, separator and minutes
    final int[] cps = cps(fp.primary);
    final int cl = cps.length;
    int digits = 0;
    while(digits < cl && zeroes(cps[digits]) != -1) ++digits;

    // width of hours, separator and minutes (no digits: default pattern 00:00)
    final int zero = digits > 0 ? zeroes(cps[0]) : '0';
    int hours = 2, sep = ':';
    boolean minutes = true;
    if(digits > 2) {
      // three or more digits: the last two ones represent the minutes
      hours = Math.min(digits - 2, 2);
      sep = 0;
    } else if(digits > 0) {
      // minutes after the separator, or (if required) after a colon
      hours = digits;
      if(digits < cl) sep = cps[digits];
      else minutes = num % 60 != 0;
    }

    final int abs = Math.abs(num);
    digits(tb, abs / 60, hours, zero);
    if(minutes) {
      if(sep != 0) tb.add(sep);
      digits(tb, abs % 60, 2, zero);
    }
    return tb.finish();
  }

  /**
   * Adds the digits of a non-negative number, padded with leading zeroes.
   * @param tb token builder
   * @param num number
   * @param width minimum number of digits
   * @param zero zero digit of the digit family
   */
  private static void digits(final TokenBuilder tb, final int num, final int width,
      final int zero) {
    final String string = Integer.toString(num);
    for(int s = string.length(); s < width; s++) tb.add(zero);
    for(final char ch : string.toCharArray()) tb.add(zero + ch - '0');
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
      for(int r = 0, d = 1000; r < ROMAN.length; r++, d /= 10) {
        tb.add(ROMAN[r][(int) (n / d % 10)]);
      }
    } else {
      tb.addLong(n);
    }
    while(tb.size() - sz < min) tb.add(' ');
  }

  /**
   * Recursively adds a Japanese character sequence.
   * @param tb token builder
   * @param n number to be formatted
   * @param omitOne omit a single one (multiplier of a unit)
   */
  private static void japanese(final TokenBuilder tb, final long n, final boolean omitOne) {
    if(n == 0) return;
    if(n <= 9) {
      if(n != 1 || !omitOne) tb.add(KANJI[(int) n]);
    } else {
      final int jl = JAPANESE.length - 1;
      int j = 0;
      while(j < jl && n >= JAPANESE[j + 1]) ++j;
      if(j == jl) {
        tb.addLong(n);
      } else {
        final long f = JAPANESE[j];
        // a leading one is omitted for 十, 百 and 千, but not for 万 and larger units
        japanese(tb, n / f, j < 3);
        tb.add(KANJI[10 + j]);
        japanese(tb, n % f, false);
      }
    }
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
   * Creates a number character sequence for an integer.
   * @param num number to be formatted
   * @param fp format parser
   * @return number character sequence
   */
  private static byte[] number(final long num, final FormatParser fp) {
    if(num >= 0 && digit(fp.primary)) {
      // decimal digit pattern without separators: pad with leading zeroes
      return new TokenBuilder().addLong(num, Math.max(fp.primary.length, fp.min)).finish();
    }
    return number(token(num, fp.radix), fp);
  }

  /**
   * Creates a number character sequence for the digits of a number.
   * @param num digits (ASCII, or radix digits)
   * @param fp format parser
   * @return number character sequence
   */
  private static byte[] number(final byte[] num, final FormatParser fp) {
    final int zero = fp.zeroes(fp.first);

    // cache characters of presentation modifier
    final int[] mod = cps(fp.primary);
    final int modSize = mod.length;
    int modStart = 0;
    while(modStart < modSize && mod[modStart] == '#') modStart++;

    // try to find regular separator pattern
    int sepPos = -1, sepChar = -1, digitPos = 0, seps = 0;
    boolean regSep = false;
    for(int mp = modSize - 1; mp >= modStart; --mp) {
      final int ch = mod[mp];
      if(fp.digit(ch, zero)) {
        digitPos = mp;
        continue;
      }
      if(ch == '#') continue;
      seps++;
      if(sepPos == -1) {
        sepPos = modSize - mp;
        sepChar = ch;
        regSep = true;
      } else if(regSep) {
        regSep = (modSize - mp) % sepPos == 0 && ch == sepChar;
      }
    }
    // every multiple of the grouping size below the number of digit signs must be a separator
    if(regSep) regSep = sepPos > 1 && seps == (modSize - seps - 1) / (sepPos - 1);
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
   * Returns the longest abbreviation of a name that fits into the maximum size.
   * @param forms forms of the name, ordered by length
   * @param max maximum size
   * @return form
   */
  private static byte[] abbreviation(final byte[][] forms, final int max) {
    int f = forms.length;
    while(--f > 0 && max < length(forms[f]));
    return forms[f];
  }

  /**
   * Formats a token.
   * @param token token
   * @param min minimum size
   * @param max maximum size
   * @return resulting token
   */
  private static byte[] format(final byte[] token, final int min, final int max) {
    if(min == 0 && max == Integer.MAX_VALUE) return token;

    final TokenBuilder tb = new TokenBuilder();
    final TokenParser tp = new TokenParser(token);
    int p = -1;
    while(++p < max && tp.more()) tb.add(tp.next());
    while(p++ < min) tb.add(' ');
    return tb.finish();
  }
}
