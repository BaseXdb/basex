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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DecFormatter extends FormatUtil {
  /** Decimal-digit-family (mandatory-digit-sign). */
  private final byte[] digits;
  /** Active characters. */
  private final byte[] actives;
  /** Zero digit sign. */
  private final int zero;

  /** Infinity. */
  private byte[] inf = token("Infinity");
  /** NaN. */
  private byte[] nan = token("NaN");
  /** Pattern-separator sign. */
  private int pattern = ';';

  /** Decimal-separator sign. */
  private int decimal = '.';
  /** Grouping-separator sign. */
  private int grouping = ',';
  /** Exponent-separator sign. */
  private int exponent = 'e';
  /** Optional-digit sign. */
  private int optional = '#';

  /** Minus sign. */
  private int minus = '-';
  /** Percent sign. */
  private int percent = '%';
  /** Permille sign. */
  private int permille = '\u2030';

  /**
   * Default constructor.
   * @throws QueryException query exception
   */
  public DecFormatter() throws QueryException {
    this(null, null);
  }

  /**
   * Constructor.
   * @param info input info
   * @param map decimal format
   * @throws QueryException query exception
   */
  public DecFormatter(final InputInfo info, final TokenMap map) throws QueryException {
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
          switch (k) {
            case DF_DEC: decimal  = cp; break;
            case DF_GRP: grouping = cp; break;
            case DF_EXP: exponent = cp; break;
            case DF_PAT: pattern  = cp; break;
            case DF_MIN: minus    = cp; break;
            case DF_DIG: optional = cp; break;
            case DF_PC:  percent  = cp; break;
            case DF_PM:  permille = cp; break;
            case DF_ZG:
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
    for(final int s : ss) if(!is.add(s)) throw DUPLDECFORM_X.get(info, (char) s);

    // create auxiliary strings
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) tb.add(zero + i);
    digits = tb.toArray();
    // "decimal-separator-sign, exponent-separator-sign, grouping-sign, decimal-digit-family,
    // optional-digit-sign and pattern-separator-sign are classified as active characters"
    // -> decimal-digit-family: added above. pattern-separator-sign: will never occur at this stage
    actives = tb.add(decimal).add(exponent).add(grouping).add(optional).finish();
    // "all other characters (including the percent-sign and per-mille-sign) are classified
    // as passive characters."
  }

  /**
   * Returns a formatted number.
   * @param info input info
   * @param number number to be formatted
   * @param picture picture
   * @return string representation
   * @throws QueryException query exception
   */
  public byte[] format(final InputInfo info, final ANum number, final byte[] picture)
      throws QueryException {

    // find pattern separator and sub-patterns
    final TokenList tl = new TokenList();
    byte[] pic = picture;
    // "A picture-string consists either of a sub-picture, or of two sub-pictures separated by
    // a pattern-separator-sign"
    final int i = indexOf(pic, pattern);
    if(i == -1) {
      tl.add(pic);
    } else {
      tl.add(substring(pic, 0, i));
      pic = substring(pic, i + cl(pic, i));
      // "A picture-string must not contain more than one pattern-separator-sign"
      if(contains(pic, pattern)) throw PICNUM_X.get(info, picture);
      tl.add(pic);
    }
    final byte[][] patterns = tl.finish();

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
        final boolean digit = contains(digits, ch);
        boolean active = contains(actives, ch), expon = false;

        if(ch == decimal) {
          // "A sub-picture must not contain more than one decimal-separator-sign."
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

      // position (integer/fractional)
      int pos = 0;
      // active character found
      boolean act = false;
      // number of characters after exponent
      int exp = -1;
      // number of optional characters
      final int[] opt = new int[2];

      // loop through all characters
      final int pl = pt.length;
      for(int i = 0, cl; i < pl; i += cl) {
        final int ch = ch(pt, i);
        cl = cl(pt, i);
        boolean active = contains(actives, ch);

        if(ch == decimal) {
          ++pos;
          act = false;
        } else if(ch == optional) {
          opt[pos]++;
        } else if(ch == exponent) {
          if(act && containsActive(pt, i + cl)) {
            exp = 0;
          } else {
            active = false;
          }
        } else if(ch == grouping) {
          if(pos == 0) pic.group[pos] = Array.add(pic.group[pos], pic.min[pos] + opt[pos]);
        } else if(contains(digits, ch)) {
          if(exp == -1) pic.min[pos]++;
          else exp++;
        }

        if(active) {
          act = true;
        } else {
          // passive characters
          pic.pc |= ch == percent;
          pic.pm |= ch == permille;
          // prefixes/suffixes
          pic.prefSuf[pos == 0 && act ? pos + 1 : pos].add(ch);
        }
      }
      // finalize integer-part-grouping-positions
      final int[] igp = pic.group[0];
      final int igl = igp.length;
      for(int g = 0; g < igl; ++g) igp[g] = pic.min[0] + opt[0] - igp[g];

      // check if integer-part-grouping-positions are regular
      // if yes, they are replaced with a single position
      if(igl > 1) {
        boolean reg = true;
        final int i = igp[igl - 1];
        for(int g = igl - 2; g >= 0; --g) reg &= i * igl == igp[g];
        if(reg) pic.group[0] = new int[] { i };
      }

      pic.maxFrac = pic.min[1] + opt[1];
      pic.minExp = Math.max(0, exp);
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
    for(int p = i; p < pt.length; p += cl(pt, p)) {
      if(contains(actives, ch(pt, p))) return true;
    }
    return false;
  }

  /**
   * Formats the specified number and returns a string representation.
   * @param it item
   * @param pics pictures
   * @param ii input info
   * @return picture variables
   * @throws QueryException query exception
   */
  private byte[] format(final ANum it, final Picture[] pics, final InputInfo ii)
      throws QueryException {

    // Rule 1: return results for NaN
    final double d = it.dbl(ii);
    if(Double.isNaN(d)) return nan;

    // Rule 2: check if value if negative (smaller than zero or -0)
    final boolean neg = d < 0 || d == 0 && Double.doubleToLongBits(d) == Long.MIN_VALUE;
    final Picture pic = pics[neg && pics.length == 2 ? 1 : 0];
    final IntList res = new IntList(), intgr = new IntList(), fract = new IntList();
    int exp = 0;

    if(Double.isInfinite(d)) {
      // Rule 3
      intgr.add(new TokenParser(inf).toArray());
    } else {
      // convert and round number
      ANum num = it;
      // Rule 4
      if(pic.pc) num = (ANum) Calc.MULT.ev(ii, num, Int.get(100));
      if(pic.pm) num = (ANum) Calc.MULT.ev(ii, num, Int.get(1000));
      // Rule 5
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

        exp = scl - pic.min[0];
        if(exp != 0) {
          final BigDecimal n = BigDecimal.TEN.pow(Math.abs(exp));
          num = (ANum) Calc.MULT.ev(ii, num, Dec.get(exp > 0 ? BigDecimal.ONE.divide(n) : n));
        }
      }
      num = num.round(pic.maxFrac, true).abs();

      // convert positive number to string
      final String s = (num instanceof Dbl || num instanceof Flt ?
          Dec.get(BigDecimal.valueOf(num.dbl(ii))) : num).toString();

      // integer/fractional separator
      final int sep = s.indexOf('.');

      // create integer part
      final int sl = s.length();
      final int il = sep == -1 ? sl : sep;
      for(int i = il; i < pic.min[0]; ++i) intgr.add(zero);
      // fractional number: skip leading 0
      if(!s.startsWith("0.")) for(int i = 0; i < il; i++) intgr.add(zero + s.charAt(i) - '0');

      // squeeze in grouping separators
      if(pic.group[0].length == 1 && pic.group[0][0] > 0) {
        // regular pattern with repeating separators
        for(int p = intgr.size() - (neg ? 2 : 1); p > 0; --p) {
          if(p % pic.group[0][0] == 0) intgr.insert(intgr.size() - p, grouping);
        }
      } else {
        // irregular pattern, or no separators at all
        final int gl = pic.group[0].length;
        for(int g = 0; g < gl; ++g) {
          final int pos = intgr.size() - pic.group[0][g];
          if(pos > 0) intgr.insert(pos, grouping);
        }
      }

      // create fractional part
      final int fl = sep == -1 ? 0 : sl - il - 1;
      if(fl != 0) for(int i = sep + 1; i < sl; i++) fract.add(zero + s.charAt(i) - '0');
      for(int i = fl; i < pic.min[1]; ++i) fract.add(zero);

      // squeeze in grouping separators in a reverse manner
      final int ul = fract.size();
      for(int p = pic.group[1].length - 1; p >= 0; p--) {
        final int pos = pic.group[1][p];
        if(pos < ul) fract.insert(pos, grouping);
      }
    }

    // add minus sign
    if(neg && pics.length != 2) res.add(minus);
    // add prefix and integer part
    res.add(pic.prefSuf[0].toArray()).add(intgr.finish());
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
    res.add(pic.prefSuf[1].toArray());
    return new TokenBuilder(res.finish()).finish();
  }

  /** Picture variables. */
  private static final class Picture {
    /** Prefix/suffix. */
    private final IntList[] prefSuf = { new IntList(), new IntList() };
    /** Integer/fractional-part-grouping-positions. */
    private final int[][] group = { {}, {} };
    /** Minimum-integer/fractional-part-size. */
    private final int[] min = { 0, 0 };
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
