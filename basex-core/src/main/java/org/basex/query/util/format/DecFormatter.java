package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Formatter for decimal numbers.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DecFormatter extends FormatUtil {
  /** Decimal-digit-family characters (mandatory-digit-sign). */
  private final byte[] digits;
  /** Active characters. */
  private final byte[] actives;

  /** Zero-digit character. */
  public final int zero;

  /** Infinity. */
  public byte[] inf = token("Infinity");
  /** NaN. */
  public byte[] nan = token("NaN");
  /** Pattern-separator character. */
  public int pattern = ';';

  /** Decimal-separator character. */
  public int decimal = '.';
  /** Exponent-separator character. */
  public int exponent = 'e';
  /** Grouping-separator character. */
  public int grouping = ',';
  /** Optional-digit character. */
  public int optional = '#';

  /** Minus character. */
  public int minus = '-';
  /** Percent character. */
  public int percent = '%';
  /** Permille character. */
  public int permille = '\u2030';

  /**
   * Constructor.
   * @param map decimal format (can be {@code null})
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DecFormatter(final TokenMap map, final InputInfo info) throws QueryException {
    // assign map values
    int z = '0';
    if(map != null) {
      for(final byte[] key : map) {
        final String k = string(key);
        final byte[] v = map.get(key);
        if(k.equals(DF_INF)) {
          inf = v;
        } else if(k.equals(DF_NAN)) {
          nan = v;
        } else if(v.length != 0 && cl(v, 0) == v.length) {
          final int cp = cp(v, 0);
          switch(k) {
            case DF_DEC: decimal  = cp; break;
            case DF_GRP: grouping = cp; break;
            case DF_EXP: exponent = cp; break;
            case DF_PAT: pattern  = cp; break;
            case DF_MIN: minus    = cp; break;
            case DF_DIG: optional = cp; break;
            case DF_PC:  percent  = cp; break;
            case DF_PM:  permille = cp; break;
            case DF_ZD:
              z = zeroes(cp);
              if(z == -1) throw INVDECFORM_X_X.get(info, k, v);
              if(z != cp) throw INVDECZERO_X.get(info, (char) cp);
              break;
          }
        } else {
          // signs must have single character
          throw INVDECSINGLE_X_X.get(info, k, v);
        }
      }
    }

    // check for duplicate characters
    zero = z;
    final IntSet is = new IntSet();
    for(int i = 0; i < 10; i++) is.add(zero + i);
    final int[] ss = { decimal, grouping, exponent, percent, permille, optional, pattern };
    for(final int s : ss) {
      if(!is.add(s)) throw DUPLDECFORM_X.get(info, (char) s);
    }

    // create auxiliary strings
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) tb.add(zero + i);
    digits = tb.toArray();
    // decimal-separator, exponent-separator, ... , are classified as active characters.
    // decimal-digit-family: added above. pattern-separator: will never occur at this stage
    actives = tb.add(decimal).add(exponent).add(grouping).add(optional).finish();
    // "all other characters (...) are classified as passive characters."
  }

  /**
   * Returns a formatted number.
   * @param number number to be formatted
   * @param picture picture
   * @param ii input info
   * @return string representation
   * @throws QueryException query exception
   */
  public byte[] format(final ANum number, final byte[] picture, final InputInfo ii)
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
      if(contains(pic, pattern)) throw PICNUM_X.get(ii, picture);
    }
    final byte[][] patterns = tl.add(pic).finish();

    // check and analyze patterns
    if(!checkSyntax(patterns)) throw PICNUM_X.get(ii, picture);
    final Picture[] pics = analyze(patterns);

    // return formatted string
    return format(number, pics, ii);
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
        final boolean digit = contains(digits, ch);
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
        } else if(ch == optional) {
          if(frac) {
            optFrac = true;
          } else {
            // "The integer part of a sub-picture must not contain a member of the decimal-digit-
            // family that is followed by an optional-digit-sign."
            if(digMant) return false;
            optInt = true;
          }
        } else if(digit) {
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
            if(!digit) return false;
            expAct = true;
          }
          act = true;
        } else {
          // "A sub-picture must not contain a passive character that is preceded by an active
          // character and that is followed by another active character."
          if(act && containsActive(pt, i + cl)) return false;
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
      if(per && exp) return false;

      // (*) continued
      if(exp && !expAct) return false;
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
        } else if(ch == optional) {
          if(frac) optFrac++;
          else optInt++;
        } else if(ch == exponent) {
          if(act && containsActive(pt, i + cl)) {
            exp = true;
          } else {
            active = false;
          }
        } else if(ch == grouping) {
          if(!frac) pic.groupInt.add(pic.minInt + optInt);
        } else if(contains(digits, ch)) {
          if(exp) {
            pic.minExp++;
          } else {
            if(frac) pic.minFrac++;
            else pic.minInt++;
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
      if(igl > 1) {
        boolean reg = true;
        final int i = ipgp.get(igl - 1);
        for(int g = igl - 2; g >= 0; --g) reg &= i * igl == ipgp.get(g);
        if(reg) {
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
      // If all the following conditions are true...
      if(exp && pic.minInt == 0 && optInt > 0) {
        pic.minInt = 1;
      }
      // If (after making the above adjustments)...
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
   * @param ii input info
   * @return picture variables
   * @throws QueryException query exception
   */
  private byte[] format(final ANum item, final Picture[] pics, final InputInfo ii)
      throws QueryException {

    // Rule 1: return results for NaN
    final double d = item.dbl(ii);
    if(Double.isNaN(d)) return nan;

    // Rule 2: check if value if negative (smaller than zero or -0)
    final boolean neg = d < 0 || d == 0 && Double.doubleToLongBits(d) == Long.MIN_VALUE;
    final Picture pic = pics[neg && pics.length == 2 ? 1 : 0];
    final IntList res = new IntList(), intgr = new IntList(), fract = new IntList();
    int exp = 0;

    // Rule 3: percent/permille
    ANum num = item;
    if(pic.pc) num = (ANum) Calc.MULT.eval(num, Int.get(100), ii);
    if(pic.pm) num = (ANum) Calc.MULT.eval(num, Int.get(1000), ii);

    if(Double.isInfinite(num.dbl(ii))) {
      // Rule 4: infinity
      intgr.add(new TokenParser(inf).toArray());
    } else {
      // Rule 5: exponent
      if(pic.minExp != 0 && d != 0) {
        BigDecimal dec = num.dec(ii).abs().stripTrailingZeros();
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
          num = (ANum) Calc.MULT.eval(num, Dec.get(
              exp > 0 ? BigDecimal.ONE.divide(n, MathContext.DECIMAL64) : n), ii);
        }
      }
      num = num.round(pic.maxFrac, true).abs();

      // convert positive number to string; chop leading 0
      String s = (num instanceof Dbl || num instanceof Flt ?
          Dec.get(BigDecimal.valueOf(num.dbl(ii))) : num).toString();
      if(Strings.startsWith(s, '0')) s = s.substring(1);

      // integer/fractional separator
      final int fracSep = s.indexOf('.');

      // create integer part
      final int sl = s.length();
      final int il = fracSep == -1 ? sl : fracSep;
      for(int i = il; i < pic.minInt; ++i) intgr.add(zero);
      for(int i = 0; i < il; i++) intgr.add(zero + s.charAt(i) - '0');

      // squeeze in grouping separators
      final int gil = pic.groupInt.size();
      if(gil == 1 && pic.groupInt.get(0) > 0) {
        // regular pattern with repeating separators
        for(int i = intgr.size() - 1; i > 0; --i) {
          if(i % pic.groupInt.get(0) == 0) intgr.insert(intgr.size() - i, grouping);
        }
      } else {
        // irregular pattern, or no separators at all
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
