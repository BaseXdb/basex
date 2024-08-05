package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.text.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.FnRound.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Formatter for decimal numbers.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DecFormatter extends FormatUtil {
  /** Decimal Format. */
  private final DecFormatOptions options;

  /** Decimal-digit-family characters (mandatory-digit-sign). */
  private final byte[] digits;
  /** Active characters. */
  private final byte[] actives;

  /** Zero-digit character. */
  public final int zero;

  /** Infinity. */
  public byte[] inf = Token.INFINITY;
  /** NaN. */
  public byte[] nan = Token.NAN;
  /** Pattern-separator character. */
  public int pattern = ';';

  /** Decimal-separator character. */
  public int decimal = '.';
  /** Exponent-separator character. */
  public int exponent = 'e';
  /** Grouping-separator character. */
  public int grouping = ',';
  /** Optional-digit character. */
  public int digit = '#';

  /** Minus character. */
  public int minus = '-';
  /** Percent character. */
  public int percent = '%';
  /** Permille character. */
  public int permille = '\u2030';

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
    this.options = options;

    // assign map values
    int z = '0';
    for(final Option<?> option : options) {
      if(!options.contains(option)) continue;
      final String k = option.name();
      final byte[] v = token(options.get((StringOption) option));
      if(option == DecFormatOptions.INFINITY) {
        inf = v;
      } else if(option == DecFormatOptions.NAN) {
        nan = v;
      } else if(v.length != 0 && cl(v, 0) == v.length) {
        final int cp = cp(v, 0);
        if(option == DecFormatOptions.DECIMAL_SEPARATOR) {
          decimal = cp;
        } else if(option == DecFormatOptions.GROUPING_SEPARATOR) {
          grouping = cp;
        } else if(option == DecFormatOptions.EXPONENT_SEPARATOR) {
          exponent = cp;
        } else if(option == DecFormatOptions.PATTERN_SEPARATOR) {
          pattern = cp;
        } else if(option == DecFormatOptions.MINUS_SIGN) {
          minus = cp;
        } else if(option == DecFormatOptions.DIGIT) {
          digit = cp;
        } else if(option == DecFormatOptions.PERCENT) {
          percent = cp;
        } else if(option == DecFormatOptions.PER_MILLE) {
          permille = cp;
        } else if(option == DecFormatOptions.ZERO_DIGIT) {
          z = zeroes(cp);
          if(z == -1) throw INVDECFORM_X_X.get(info, k, v);
          if(z != cp) throw INVDECZERO_X.get(info, (char) cp);
        }
      } else if(option != DecFormatOptions.FORMAT_NAME) {
        // signs must have single character
        throw INVDECSINGLE_X_X.get(info, k, v);
      }
    }

    // check for duplicate characters
    zero = z;
    final IntSet is = new IntSet();
    for(int i = 0; i < 10; i++) is.add(zero + i);
    final int[] ss = { decimal, grouping, exponent, percent, permille, digit, pattern };
    for(final int s : ss) {
      if(!is.add(s)) throw DUPLDECFORM_X.get(info, (char) s);
    }

    // create auxiliary strings
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) tb.add(zero + i);
    digits = tb.toArray();
    // decimal-separator, exponent-separator, ... , are classified as active characters.
    // decimal-digit-family: added above. pattern-separator: will never occur at this stage
    actives = tb.add(decimal).add(exponent).add(grouping).add(digit).finish();
    // "all other characters (...) are classified as passive characters."
  }

  /**
   * Returns a decimal formatter for the given language.
   * @param languageTag language tag
   * @param info input info (can be {@code null})
   * @return a decimal formatter, or {@code null} if the language is not supported
   * @throws QueryException query exception
   */
  public static DecFormatter forLanguage(final byte[] languageTag, final InputInfo info)
      throws QueryException {
    final String l = string(languageTag);
    final DecFormatOptions dfo = Prop.ICU ? IcuFormatter.decFormat(l) : decFormatSymbols(l);
    return dfo != null ? new DecFormatter(dfo, info) : null;
  }

  /**
   * Returns a decimal format symbols for the given language.
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
        dfo.put(DecFormatOptions.EXPONENT_SEPARATOR, dfs.getExponentSeparator());
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
   * Convert these properties to an XQuery map.
   * @return map
   * @throws QueryException query exception
   */
  public XQMap toMap() throws QueryException {
    final MapBuilder map = new MapBuilder();
    map.put(DecFormatOptions.DECIMAL_SEPARATOR.name(), cpToken(decimal));
    map.put(DecFormatOptions.EXPONENT_SEPARATOR.name(), cpToken(exponent));
    map.put(DecFormatOptions.GROUPING_SEPARATOR.name(), cpToken(grouping));
    map.put(DecFormatOptions.PERCENT.name(), cpToken(percent));
    map.put(DecFormatOptions.PER_MILLE.name(), cpToken(permille));
    map.put(DecFormatOptions.ZERO_DIGIT.name(), cpToken(zero));
    map.put(DecFormatOptions.DIGIT.name(), cpToken(digit));
    map.put(DecFormatOptions.PATTERN_SEPARATOR.name(), cpToken(pattern));
    map.put(DecFormatOptions.INFINITY.name(), inf);
    map.put(DecFormatOptions.NAN.name(), nan);
    map.put(DecFormatOptions.MINUS_SIGN.name(), cpToken(minus));
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

    // find pattern separator and sub-patterns
    final TokenList tl = new TokenList();
    byte[] pic = picture;
    // "A picture-string consists either of a sub-picture, or of two sub-pictures separated by
    // the pattern-separator"
    final int i = indexOf(pic, pattern);
    if(i != -1) {
      tl.add(substring(pic, 0, i));
      pic = substring(pic, i + cl(pic, i));
      // "A picture-string must not contain more than one instance of the pattern-separator"
      if(contains(pic, pattern)) throw PICNUM_X.get(info, picture);
    }
    final byte[][] patterns = tl.add(pic).finish();

    // check and analyze patterns
    if(!checkSyntax(patterns)) throw PICNUM_X.get(info, picture);
    final Picture[] pics = analyze(patterns);

    // return formatted string
    return format(number, pics, info);
  }

  /**
   * Checks the syntax of the specified patterns.
   * @param patterns patterns
   * @return result of check
   */
  private boolean checkSyntax(final byte[][] patterns) {
    for(final byte[] pt : patterns) {
      boolean frac = false, act = false, expAct = false, exp = false, digMant = false;
      boolean optInt = false, optFrac = false, per = false;
      int cl, last = 0;

      // loop through all characters
      final int pl = pt.length;
      for(int i = 0; i < pl; i += cl) {
        final int ch = ch(pt, i);
        cl = cl(pt, i);
        final boolean containsDigit = contains(digits, ch);
        boolean active = contains(actives, ch), expon = false;

        if(ch == decimal) {
          // "A sub-picture must not contain more than one instance of the decimal-separator."
          if(frac) return false;
          frac = true;
        } else if(ch == grouping) {
          // "A sub-picture must not contain a grouping-separator-sign that appears adjacent to a
          // decimal-separator-sign, or in the absence of a decimal-separator-sign, at the end of
          // the integer part."
          if(i == 0 && frac || last == decimal || (i + cl < pl ? ch(pt, i + cl) == decimal : !frac))
            return false;
          // "A sub-picture must not contain two adjacent grouping-separator-signs."
          if(last == grouping) return false;
        } else if(ch == exponent) {
          // "A character that matches the chosen exponent-separator-sign is treated as an
          // exponent-separator-sign if it is both preceded and followed within the sub-picture by
          // an active character."
          if(act && containsActive(pt, i + cl)) {
            // "A sub-picture must not contain more than one character that is treated as an
            // exponent-separator-sign."
            if(exp) return false;
            expon = true;
          } else {
            // "Otherwise, it is treated as a passive character."
            active = false;
          }
        } else if(ch == percent || ch == permille) {
          // "A sub-picture must not contain more than one percent-sign or per-mille-sign,
          // and it must not contain one of each."
          if(per) return false;
          per = true;
        } else if(ch == digit) {
          if(frac) {
            optFrac = true;
          } else {
            // "The integer part of a sub-picture must not contain a member of the decimal-digit-
            // family that is followed by an optional-digit-sign."
            if(digMant) return false;
            optInt = true;
          }
        } else if(containsDigit) {
          if(!exp) {
            // "The fractional part of a sub-picture must not contain an optional-digit-sign that
            // is followed by a member of the decimal-digit-family."
            if(optFrac) return false;
            digMant = true;
          }
        }

        if(active) {
          // "If a sub-picture contains a character treated as an exponent-separator-sign then
          // this must be followed by one or more characters that are members of the
          // decimal-digit-family, and it must not be followed by any active character that is not
          // a member of the decimal-digit-family." (*)
          if(exp) {
            if(!containsDigit) return false;
            expAct = true;
          }
          act = true;
        } else if(act && containsActive(pt, i + cl)) {
          // "A sub-picture must not contain a passive character that is preceded by an active
          // character and that is followed by another active character."
          return false;
        }

        // cache last character
        last = ch;
        if(expon) exp = true;
      }

      // "The mantissa part of a sub-picture must contain at least one character that is an
      // optional-digit-sign or a member of the decimal-digit-family."
      if(!optInt && !optFrac && !digMant) return false;

      // "A sub-picture that contains a percent-sign or per-mille-sign must not contain a character
      // treated as an exponent-separator-sign."
      if(exp && (per || !expAct)) return false;
    }

    // everything ok
    return true;
  }

  /**
   * Analyzes the specified patterns.
   * @param patterns patterns
   * @return picture variables
   */
  private Picture[] analyze(final byte[][] patterns) {
    // pictures
    final int picL = patterns.length;
    final Picture[] pics = new Picture[picL];

    // analyze patterns
    for(int p = 0; p < picL; p++) {
      final byte[] pt = patterns[p];
      final Picture pic = new Picture();

      // fractional part
      boolean frac = false;
      // active character found
      boolean act = false;
      // exponential flag
      boolean exp = false;
      // number of optional-digit-characters
      int optInt = 0, optFrac = 0;

      // loop through all characters
      final int pl = pt.length;
      for(int i = 0, cl; i < pl; i += cl) {
        final int ch = ch(pt, i);
        cl = cl(pt, i);
        boolean active = contains(actives, ch);

        if(ch == decimal) {
          frac = true;
          act = false;
        } else if(ch == digit) {
          if(frac) optFrac++;
          else optInt++;
        } else if(ch == exponent) {
          if(act && containsActive(pt, i + cl)) {
            exp = true;
          } else {
            active = false;
          }
        } else if(ch == grouping) {
          if(frac) pic.groupFrac.add(pic.minFrac + optFrac);
          else pic.groupInt.add(pic.minInt + optInt);
        } else if(contains(digits, ch)) {
          if(exp) {
            pic.minExp++;
          } else if(frac) {
            pic.minFrac++;
          } else {
            pic.minInt++;
          }
        }

        if(active) {
          act = true;
        } else {
          // passive characters
          pic.pc |= ch == percent;
          pic.pm |= ch == permille;
          // add to prefix or suffix
          (frac || act ? pic.suffix : pic.prefix).add(ch);
        }
      }
      // finalize integer-part-grouping-positions
      final IntList ipgp = pic.groupInt;
      final int igl = ipgp.size();
      for(int g = 0; g < igl; ++g) ipgp.set(g, pic.minInt + optInt - ipgp.get(g));

      // check if integer-part-grouping-positions are regular
      // if yes, they are replaced with a single position
      if(igl >= 1) {
        final int i = ipgp.get(igl - 1);
        // make sure that there is no digit where the first non-present grouping separator goes
        pic.isRegular = ipgp.get(0) + i >= pic.minInt + optInt;
        for(int g = igl - 2; g >= 0; --g) pic.isRegular &= i * (igl - g) == ipgp.get(g);
        if(pic.isRegular) {
          pic.groupInt.reset();
          pic.groupInt.add(i);
        }
      }
      pic.scaling = pic.minInt;

      // The maximum-fractional-part-size is set to ...
      pic.maxFrac = optFrac + pic.minFrac;
      // If the effect of the above rules is that...
      if(pic.minInt == 0 && pic.maxFrac == 0) {
        if(exp) {
          pic.minFrac = 1;
          pic.maxFrac = 1;
        } else {
          pic.minInt = 1;
        }
      }
      // if all the following conditions are true...
      if(exp && pic.minInt == 0 && optInt > 0) {
        pic.minInt = 1;
      }
      // if (after making the above adjustments)...
      if(pic.minInt == 0 && pic.minFrac == 0) {
        pic.minFrac = 1;
      }
      pics[p] = pic;
    }
    return pics;
  }

  /**
   * Checks if the specified pattern contains active characters after the specified index.
   * @param pt pattern
   * @param i index
   * @return result of check
   */
  private boolean containsActive(final byte[] pt, final int i) {
    final int pl = pt.length;
    for(int p = i; p < pl; p += cl(pt, p)) {
      if(contains(actives, ch(pt, p))) return true;
    }
    return false;
  }

  /**
   * Formats the specified number and returns a string representation.
   * @param item item
   * @param pics pictures
   * @param info input info (can be {@code null})
   * @return picture variables
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
    final IntList res = new IntList(), intgr = new IntList(), fract = new IntList();
    int exp = 0;

    // Rule 3: percent/permille
    ANum num = item;
    if(pic.pc) num = (ANum) Calc.MULTIPLY.eval(num, Int.get(100), info);
    if(pic.pm) num = (ANum) Calc.MULTIPLY.eval(num, Int.get(1000), info);

    if(Double.isInfinite(num.dbl(info))) {
      // Rule 4: infinity
      intgr.add(new TokenParser(inf).toArray());
    } else {
      // Rule 5: exponent
      if(pic.minExp != 0 && d != 0) {
        BigDecimal dec = num.dec(info).abs().stripTrailingZeros();
        int scl = 0;
        if(dec.compareTo(BigDecimal.ONE) >= 0) {
          scl = dec.setScale(0, RoundingMode.HALF_DOWN).precision();
        } else {
          while(dec.compareTo(BigDecimal.ONE) < 0) {
            dec = dec.multiply(BigDecimal.TEN);
            scl--;
          }
          scl++;
        }
        exp = scl - pic.scaling;
        if(exp != 0) {
          final BigDecimal n = BigDecimal.TEN.pow(Math.abs(exp));
          num = (ANum) Calc.MULTIPLY.eval(num, Dec.get(
              exp > 0 ? BigDecimal.ONE.divide(n, MathContext.DECIMAL64) : n), info);
        }
      }
      num = num.round(pic.maxFrac, RoundMode.HALF_TO_EVEN).abs();

      // convert positive number to string; chop leading 0
      String s = (num instanceof Dbl || num instanceof Flt ?
          Dec.get(BigDecimal.valueOf(num.dbl(info))) : num).toString();
      if(Strings.startsWith(s, '0')) s = s.substring(1);

      // integer/fractional separator
      final int fracSep = s.indexOf('.');

      // create integer part
      final int sl = s.length();
      final int il = fracSep == -1 ? sl : fracSep;
      for(int i = il; i < pic.minInt; ++i) intgr.add(zero);
      for(int i = 0; i < il; i++) intgr.add(zero + s.charAt(i) - '0');

      // squeeze in grouping separators
      if(pic.isRegular && pic.groupInt.get(0) > 0) {
        // regular pattern with repeating separators
        for(int i = intgr.size() - 1; i > 0; --i) {
          if(i % pic.groupInt.get(0) == 0) intgr.insert(intgr.size() - i, grouping);
        }
      } else {
        // irregular pattern, or no separators at all
        final int gil = pic.groupInt.size();
        for(int g = 0; g < gil; g++) {
          final int pos = intgr.size() - pic.groupInt.get(g);
          if(pos > 0) intgr.insert(pos, grouping);
        }
      }

      // create fractional part
      final int fl = fracSep == -1 ? 0 : sl - il - 1;
      if(fl != 0) for(int i = fracSep + 1; i < sl; i++) fract.add(zero + s.charAt(i) - '0');
      for(int i = fl; i < pic.minFrac; ++i) fract.add(zero);

      // squeeze in grouping separators in a reverse manner
      final int ul = fract.size();
      for(int p = pic.groupFrac.size() - 1; p >= 0; p--) {
        final int pos = pic.groupFrac.get(p);
        if(pos < ul) fract.insert(pos, grouping);
      }
    }

    // add minus sign
    if(neg && pics.length != 2) res.add(minus);
    // add prefix and integer part
    res.add(pic.prefix.toArray()).add(intgr.finish());
    // add fractional part
    if(!fract.isEmpty()) res.add(decimal).add(fract.finish());
    // add exponent
    if(pic.minExp != 0) {
      res.add(exponent);
      if(exp < 0) res.add(minus);
      final String s = Integer.toString(Math.abs(exp));
      final int sl = s.length();
      for(int i = sl; i < pic.minExp; i++) res.add(zero);
      for(int i = 0; i < sl; i++) res.add(zero + s.charAt(i) - '0');
    }
    // add suffix
    res.add(pic.suffix.toArray());

    final TokenBuilder tb = new TokenBuilder(res.size());
    for(final int r : res.finish()) tb.add(r);
    return tb.finish();
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
    /** Percent flag. */
    private boolean pc;
    /** Per-mille flag. */
    private boolean pm;
  }
}
