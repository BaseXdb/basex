package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Formatter for decimal numbers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DecFormatter extends FormatUtil {
  /** Percent factor (decimal, as integer multiplication may overflow). */
  private static final Dec HUNDRED = Dec.get(BigDecimal.valueOf(100));
  /** Per-mille factor (decimal, as integer multiplication may overflow). */
  private static final Dec THOUSAND = Dec.get(Dec.BD_1000);
  /** Cached decimal formatters for languages. */
  private static final ConcurrentHashMap<String, DecFormatter> LANGUAGES =
      new ConcurrentHashMap<>();

  /** Decimal Format. */
  private final DecFormatOptions options;

  /** Decimal-digit-family characters (mandatory-digit-sign). */
  private final byte[] digits;
  /** Active characters. */
  private final byte[] actives;

  /** Zero-digit character. */
  private final int zero;

  /** Infinity. */
  private byte[] inf = Token.POSITIVE_INFINITY;
  /** NaN. */
  private byte[] nan = Token.NAN;
  /** Pattern-separator character. */
  private int pattern = ';';
  /** Optional-digit character. */
  private int digit = '#';
  /** Minus rendition. */
  private byte[] minus = { '-' };

  /** Decimal-separator. */
  private Sign decimal = new Sign('.');
  /** Exponent-separator. */
  private Sign exponent = new Sign('e');
  /** Grouping-separator. */
  private Sign grouping = new Sign(',');
  /** Percent. */
  private Sign percent = new Sign('%');
  /** Per-mille. */
  private Sign permille = new Sign('‰');

  /**
   * Constructor.
   * @throws QueryException query exception
   */
  public DecFormatter() throws QueryException {
    this(new DecFormatOptions(), null);
  }

  /**
   * Constructor.
   * @param options decimal format options
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DecFormatter(final DecFormatOptions options, final InputInfo info) throws QueryException {
    this.options = options.seal();

    // assign map values
    int z = '0';
    for(final Option<?> option : options) {
      if(!options.contains(option) || option == DecFormatOptions.FORMAT_NAME) continue;
      final String k = option.name();
      final byte[] v = token(options.get((StringOption) option));
      if(option == DecFormatOptions.INFINITY) {
        inf = v;
      } else if(option == DecFormatOptions.NAN) {
        nan = v;
      } else if(option == DecFormatOptions.DECIMAL_SEPARATOR) {
        decimal = sign(k, v, info);
      } else if(option == DecFormatOptions.GROUPING_SEPARATOR) {
        grouping = sign(k, v, info);
      } else if(option == DecFormatOptions.EXPONENT_SEPARATOR) {
        exponent = sign(k, v, info);
      } else if(option == DecFormatOptions.PERCENT) {
        percent = sign(k, v, info);
      } else if(option == DecFormatOptions.PER_MILLE) {
        permille = sign(k, v, info);
      } else if(option == DecFormatOptions.MINUS_SIGN) {
        minus = v;
      } else if(v.length != 0 && cl(v, 0) == v.length) {
        final int cp = cp(v, 0);
        if(option == DecFormatOptions.PATTERN_SEPARATOR) {
          pattern = cp;
        } else if(option == DecFormatOptions.DIGIT) {
          digit = cp;
        } else if(option == DecFormatOptions.ZERO_DIGIT) {
          z = zeroes(cp);
          if(z == -1) throw INVDECFORM_X_X.get(info, k, v);
          if(z != cp) throw INVDECZERO_X.get(info, Strings.describe(cp));
        }
      } else {
        // signs must have single character
        throw INVDECSINGLE_X_X.get(info, k, v);
      }
    }

    // check for duplicate characters, create auxiliary strings
    zero = z;
    final IntSet is = new IntSet();
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) {
      is.add(zero + i);
      tb.add(zero + i);
    }
    for(final int s : new int[] { decimal.marker, grouping.marker, exponent.marker,
        percent.marker, permille.marker, digit, pattern }) {
      if(!is.add(s)) throw DUPLDECFORM_X.get(info, Strings.describe(s));
    }
    digits = tb.toArray();
    // "decimal-separator, exponent-separator, grouping-separator, digit, pattern-separator and
    // the members of the decimal digit family are classified as active characters"
    // (pattern-separator: will never occur at this stage)
    actives = tb.add(decimal.marker).add(exponent.marker).add(grouping.marker).add(digit).finish();
  }

  /**
   * Returns a sign from an option value matching the pattern '.|.:.*', where the first
   * character identifies the marker, and the optional part following a colon is the rendition.
   * @param name option name
   * @param value option value
   * @param info input info (can be {@code null})
   * @return sign
   * @throws QueryException query exception
   */
  private static Sign sign(final String name, final byte[] value, final InputInfo info)
      throws QueryException {
    final int[] cps = cps(value);
    if(cps.length == 1) return new Sign(cps[0]);
    if(cps.length > 1 && cps[1] == ':') {
      return new Sign(cps[0], Arrays.copyOfRange(cps, 2, cps.length));
    }
    throw INVDECFORM_X_X.get(info, name, value);
  }

  /**
   * Returns a decimal formatter for the given language.
   * @param languageTag language tag
   * @param info input info (can be {@code null})
   * @return decimal formatter, or {@code null} if the language is not supported
   * @throws QueryException query exception
   */
  public static DecFormatter forLanguage(final byte[] languageTag, final InputInfo info)
      throws QueryException {
    final String l = string(languageTag);
    DecFormatter df = LANGUAGES.get(l);
    if(df == null) {
      final DecFormatOptions dfo = ExternalLib.ICU.available() ? IcuFormatter.decFormat(l) :
        decFormatSymbols(l);
      if(dfo == null) return null;
      df = new DecFormatter(dfo, info);
      LANGUAGES.putIfAbsent(l, df);
    }
    return df;
  }

  /**
   * Returns the decimal format symbols for the given language.
   * @param languageTag language tag
   * @return format symbols, or {@code null} if the language is not supported
   */
  private static DecFormatOptions decFormatSymbols(final String languageTag) {
    for(final Locale locale : DecimalFormatSymbols.getAvailableLocales()) {
      if(locale.toLanguageTag().equals(languageTag)) {
        final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
        final DecFormatOptions dfo = new DecFormatOptions();
        dfo.put(DecFormatOptions.DECIMAL_SEPARATOR, String.valueOf(dfs.getDecimalSeparator()));
        dfo.put(DecFormatOptions.DIGIT, String.valueOf(dfs.getDigit()));
        dfo.put(DecFormatOptions.GROUPING_SEPARATOR, String.valueOf(dfs.getGroupingSeparator()));
        dfo.putMarker(DecFormatOptions.EXPONENT_SEPARATOR, dfs.getExponentSeparator(), 'e');
        dfo.put(DecFormatOptions.INFINITY, dfs.getInfinity());
        dfo.put(DecFormatOptions.MINUS_SIGN, String.valueOf(dfs.getMinusSign()));
        dfo.put(DecFormatOptions.NAN, dfs.getNaN());
        dfo.put(DecFormatOptions.PATTERN_SEPARATOR, String.valueOf(dfs.getPatternSeparator()));
        dfo.put(DecFormatOptions.PERCENT, String.valueOf(dfs.getPercent()));
        dfo.put(DecFormatOptions.PER_MILLE, String.valueOf(dfs.getPerMill()));
        dfo.put(DecFormatOptions.ZERO_DIGIT, String.valueOf(dfs.getZeroDigit()));
        return dfo;
      }
    }
    return null;
  }

  /**
   * Returns the decimal format options.
   * @return options
   */
  public DecFormatOptions options() {
    return options;
  }

  /**
   * Converts these properties to an XQuery map.
   * @return map
   * @throws QueryException query exception
   */
  public XQMap toMap() throws QueryException {
    final MapBuilder map = new MapBuilder();
    map.put(DecFormatOptions.DECIMAL_SEPARATOR.name(), decimal.token());
    map.put(DecFormatOptions.EXPONENT_SEPARATOR.name(), exponent.token());
    map.put(DecFormatOptions.GROUPING_SEPARATOR.name(), grouping.token());
    map.put(DecFormatOptions.PERCENT.name(), percent.token());
    map.put(DecFormatOptions.PER_MILLE.name(), permille.token());
    map.put(DecFormatOptions.ZERO_DIGIT.name(), cpToken(zero));
    map.put(DecFormatOptions.DIGIT.name(), cpToken(digit));
    map.put(DecFormatOptions.PATTERN_SEPARATOR.name(), cpToken(pattern));
    map.put(DecFormatOptions.INFINITY.name(), inf);
    map.put(DecFormatOptions.NAN.name(), nan);
    map.put(DecFormatOptions.MINUS_SIGN.name(), minus);
    return map.map();
  }

  /**
   * Returns a formatted number.
   * @param number number to be formatted
   * @param picture picture
   * @param info input info (can be {@code null})
   * @return string representation
   * @throws QueryException query exception
   */
  public byte[] format(final ANum number, final byte[] picture, final InputInfo info)
      throws QueryException {
    // "A picture-string consists either of a sub-picture, or of two sub-pictures separated by
    // the pattern-separator"
    final byte[][] patterns = split(picture, cpToken(pattern), true);
    final int pl = patterns.length;
    if(pl > 2) throw PICNUM_X.get(info, picture);

    final Picture[] pics = new Picture[pl];
    for(int p = 0; p < pl; p++) {
      pics[p] = analyze(patterns[p]);
      if(pics[p] == null) throw PICNUM_X.get(info, picture);
    }
    return format(number, pics, info);
  }

  /**
   * Checks and analyzes a sub-picture.
   * @param pt sub-picture
   * @return picture variables, or {@code null} if the sub-picture is invalid
   */
  private Picture analyze(final byte[] pt) {
    final Picture pic = new Picture();
    // fractional part, active character, exponent-separator found
    boolean frac = false, act = false, exp = false;
    // number of optional-digit characters in the integer and fractional part, last character
    int optInt = 0, optFrac = 0, last = 0;

    final int pl = pt.length;
    for(int i = 0, cl; i < pl; i += cl) {
      final int ch = ch(pt, i);
      cl = cl(pt, i);
      // "A character that matches the chosen exponent-separator-sign is treated as an
      // exponent-separator-sign if it is both preceded and followed within the sub-picture by
      // an active character. Otherwise, it is treated as a passive character."
      final boolean active = contains(actives, ch) &&
          (ch != exponent.marker || act && containsActive(pt, i + cl));

      if(!active) {
        // "A sub-picture must not contain a passive character that is preceded by an active
        // character and that is followed by another active character."
        if(act && containsActive(pt, i + cl)) return null;
        final IntList affix = act ? pic.suffix : pic.prefix;
        if(ch == percent.marker || ch == permille.marker) {
          // "A sub-picture must not contain more than one percent-sign or per-mille-sign,
          // and it must not contain one of each."
          if(pic.factor != null) return null;
          final boolean pc = ch == percent.marker;
          pic.factor = pc ? HUNDRED : THOUSAND;
          affix.add((pc ? percent : permille).rendition);
        } else {
          affix.add(ch);
        }
      } else if(exp) {
        // "If a sub-picture contains a character treated as an exponent-separator-sign then
        // this must be followed by one or more characters that are members of the
        // decimal-digit-family, and it must not be followed by any active character that is not
        // a member of the decimal-digit-family."
        if(!contains(digits, ch)) return null;
        pic.minExp++;
      } else if(ch == decimal.marker) {
        // "A sub-picture must not contain more than one instance of the decimal-separator."
        if(frac) return null;
        frac = true;
      } else if(ch == grouping.marker) {
        // "A sub-picture must not contain a grouping-separator-sign that appears adjacent to a
        // decimal-separator-sign, or in the absence of a decimal-separator-sign, at the end of
        // the integer part. [It] must not contain two adjacent grouping-separator-signs."
        final int next = nextActive(pt, i + cl);
        if(last == decimal.marker || last == grouping.marker || next == decimal.marker ||
            !frac && (next == -1 || next == exponent.marker)) return null;
        if(frac) pic.groupFrac.add(pic.minFrac + optFrac);
        else pic.groupInt.add(pic.minInt + optInt);
      } else if(ch == exponent.marker) {
        exp = true;
      } else if(ch == digit) {
        // "The integer part of a sub-picture must not contain a member of the decimal-digit-family
        // that is followed by an optional-digit-sign."
        if(frac) optFrac++;
        else if(pic.minInt == 0) optInt++;
        else return null;
      } else if(!frac) {
        pic.minInt++;
      } else {
        // "The fractional part of a sub-picture must not contain an optional-digit-sign that is
        // followed by a member of the decimal-digit-family."
        if(optFrac != 0) return null;
        pic.minFrac++;
      }
      act |= active;
      last = ch;
    }

    // "The mantissa part of a sub-picture must contain at least one character that is an
    // optional-digit-sign or a member of the decimal-digit-family."
    if(pic.minInt + pic.minFrac + optInt + optFrac == 0) return null;
    // "A sub-picture that contains a percent-sign or per-mille-sign must not contain a character
    // treated as an exponent-separator-sign."
    if(exp && pic.factor != null) return null;

    // finalize integer-part-grouping-positions
    final IntList ipgp = pic.groupInt;
    final int igl = ipgp.size(), intSize = pic.minInt + optInt;
    for(int g = 0; g < igl; g++) ipgp.set(g, intSize - ipgp.get(g));

    // if integer-part-grouping-positions are regular, replace them with a single position
    if(igl >= 1) {
      final int i = ipgp.get(igl - 1);
      // make sure that there is no digit where the first non-present grouping separator goes
      pic.isRegular = ipgp.get(0) + i >= intSize;
      for(int g = igl - 2; g >= 0; --g) pic.isRegular &= i * (igl - g) == ipgp.get(g);
      if(pic.isRegular) {
        ipgp.reset();
        ipgp.add(i);
      }
    }
    pic.scaling = pic.minInt;

    // "The maximum-fractional-part-size is set to ..."
    pic.maxFrac = optFrac + pic.minFrac;
    // "If the effect of the above rules is that ..."
    if(pic.minInt == 0 && pic.maxFrac == 0) {
      if(exp) {
        pic.minFrac = 1;
        pic.maxFrac = 1;
      } else {
        pic.minInt = 1;
      }
    }
    // "If all the following conditions are true ..."
    if(exp && pic.minInt == 0 && optInt > 0) pic.minInt = 1;
    // "If (after making the above adjustments) ..."
    if(pic.minInt == 0 && pic.minFrac == 0) pic.minFrac = 1;
    return pic;
  }

  /**
   * Checks if the specified pattern contains active characters other than the exponent-separator
   * at or after the specified index.
   * @param pt pattern
   * @param i index
   * @return result of check
   */
  private boolean containsActive(final byte[] pt, final int i) {
    return nextActive(pt, i) != -1;
  }

  /**
   * Returns the first character at or after the specified index that is treated as active.
   * @param pt pattern
   * @param i index
   * @return character, or {@code -1} if there is none
   */
  private int nextActive(final byte[] pt, final int i) {
    boolean exp = false;
    final int pl = pt.length;
    for(int p = i; p < pl; p += cl(pt, p)) {
      final int ch = ch(pt, p);
      if(ch == exponent.marker) exp = true;
      else if(contains(actives, ch)) return exp ? exponent.marker : ch;
    }
    return -1;
  }

  /**
   * Formats the specified number and returns a string representation.
   * @param item item
   * @param pics pictures
   * @param info input info (can be {@code null})
   * @return string representation
   * @throws QueryException query exception
   */
  private byte[] format(final ANum item, final Picture[] pics, final InputInfo info)
      throws QueryException {

    // Rule 1: return results for NaN
    final double d = item.dbl(info);
    if(Double.isNaN(d)) return nan;

    // Rule 2: check if value is negative (smaller than zero or -0)
    final boolean neg = d < 0 || d == 0 && Double.doubleToLongBits(d) == Long.MIN_VALUE;
    final Picture pic = pics[neg && pics.length == 2 ? 1 : 0];
    final IntList intgr = new IntList(), fract = new IntList();
    int exp = 0;

    // Rule 3: percent/permille
    ANum num = item;
    if(pic.factor != null) num = (ANum) Calc.MULTIPLY.eval(num, pic.factor, info);

    final boolean infinite = num.floating() && Double.isInfinite(num.dbl(info));
    if(infinite) {
      // Rule 4: infinity
      intgr.add(cps(inf));
    } else {
      // Rule 5: exponent (decimal representation is scaled to preserve all digits)
      BigDecimal mantissa = decimal(num);
      if(pic.minExp != 0 && mantissa.signum() != 0) {
        exp = mantissa.precision() - mantissa.scale() - pic.scaling;
        mantissa = mantissa.movePointLeft(exp);
      }
      // Rule 6: round decimal representation of mantissa
      final BigDecimal rounded = mantissa.setScale(pic.maxFrac, RoundingMode.HALF_EVEN);

      // convert number to string; chop sign and leading 0
      String s = rounded.abs().stripTrailingZeros().toPlainString();
      if(Strings.startsWith(s, '0')) s = s.substring(1);
      final int sl = s.length(), fracSep = s.indexOf('.'), il = fracSep == -1 ? sl : fracSep;

      // create integer part
      intgr.add(toDigits(s, 0, il, pic.minInt));

      // squeeze in grouping separators
      if(pic.isRegular) {
        // regular pattern with repeating separators
        final int g = pic.groupInt.get(0);
        for(int i = (intgr.size() - 1) / g * g; i > 0; i -= g) {
          intgr.insert(intgr.size() - i, grouping.rendition);
        }
      } else {
        // irregular pattern, or no separators at all
        final int gil = pic.groupInt.size();
        for(int g = 0; g < gil; g++) {
          final int pos = intgr.size() - pic.groupInt.get(g);
          if(pos > 0) intgr.insert(pos, grouping.rendition);
        }
      }

      // create fractional part
      if(fracSep != -1) fract.add(toDigits(s, fracSep + 1, sl, 0));
      for(int i = fract.size(); i < pic.minFrac; i++) fract.add(zero);

      // squeeze in grouping separators in a reverse manner
      final int ul = fract.size();
      for(int p = pic.groupFrac.size() - 1; p >= 0; p--) {
        final int pos = pic.groupFrac.get(p);
        if(pos < ul) fract.insert(pos, grouping.rendition);
      }
    }

    final TokenBuilder tb = new TokenBuilder();
    // add minus sign, prefix and integer part
    if(neg && pics.length != 2) tb.add(minus);
    add(add(tb, pic.prefix), intgr);
    // add fractional part
    if(!fract.isEmpty()) add(add(tb, decimal.rendition), fract);
    // add exponent
    if(pic.minExp != 0 && !infinite) {
      add(tb, exponent.rendition);
      if(exp < 0) tb.add(minus);
      final String s = Integer.toString(Math.abs(exp));
      add(tb, toDigits(s, 0, s.length(), pic.minExp));
    }
    // add suffix
    return add(tb, pic.suffix).finish();
  }

  /**
   * Converts a range of ASCII digits to characters of the decimal-digit-family.
   * @param s string with ASCII digits
   * @param start start index
   * @param end end index
   * @param min minimum number of digits (padded with leading zeros)
   * @return codepoints
   */
  private int[] toDigits(final String s, final int start, final int end, final int min) {
    final int pad = Math.max(0, min - (end - start));
    final int[] cps = new int[pad + end - start];
    Arrays.fill(cps, 0, pad, zero);
    for(int i = start; i < end; i++) cps[pad + i - start] = zero + s.charAt(i) - '0';
    return cps;
  }

  /**
   * Adds codepoints to a token builder.
   * @param tb token builder
   * @param cps codepoints
   * @return token builder
   */
  private static TokenBuilder add(final TokenBuilder tb, final int... cps) {
    for(final int cp : cps) tb.add(cp);
    return tb;
  }

  /**
   * Adds the codepoints of a list to a token builder.
   * @param tb token builder
   * @param list codepoints
   * @return token builder
   */
  private static TokenBuilder add(final TokenBuilder tb, final IntList list) {
    final int ls = list.size();
    for(int l = 0; l < ls; l++) tb.add(list.get(l));
    return tb;
  }

  /**
   * Returns a decimal representation of a number (shortest one for floating-point numbers).
   * @param num number
   * @return decimal
   * @throws QueryException query exception
   */
  private static BigDecimal decimal(final ANum num) throws QueryException {
    return num instanceof Flt ? new BigDecimal(Float.toString(num.flt(null))) :
      num instanceof Dbl ? BigDecimal.valueOf(num.dbl()) : num.dec(null);
  }

  /**
   * Marker character and rendition of a decimal-format property.
   * @param marker marker character in the picture string
   * @param rendition rendition in the result string
   */
  private record Sign(int marker, int[] rendition) {
    /**
     * Constructor for a property whose rendition is the marker character.
     * @param marker marker character
     */
    Sign(final int marker) {
      this(marker, new int[] { marker });
    }

    /**
     * Returns the property value in the form 'marker' or 'marker:rendition'.
     * @return token
     */
    byte[] token() {
      final TokenBuilder tb = new TokenBuilder().add(marker);
      if(rendition.length != 1 || rendition[0] != marker) add(tb.add(':'), rendition);
      return tb.finish();
    }
  }

  /** Picture variables. */
  private static final class Picture {
    /** Prefix. */
    private final IntList prefix = new IntList();
    /** Suffix. */
    private final IntList suffix = new IntList();
    /** Integer-part-grouping-positions. */
    private final IntList groupInt = new IntList();
    /** Fractional-part-grouping-positions. */
    private final IntList groupFrac = new IntList();
    /** Scaling factor. */
    private int scaling;
    /** Whether the integer part is regular. */
    private boolean isRegular;
    /** Minimum-integer-part-size. */
    private int minInt;
    /** Minimum-fractional-part-size. */
    private int minFrac;
    /** Maximum-fractional-part-size. */
    private int maxFrac;
    /** Minimum-exponent-size. */
    private int minExp;
    /** Percent or per-mille factor (can be {@code null}). */
    private Dec factor;
  }
}
