package org.basex.query.util.format;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Formatter for decimal numbers.
 *
 * @author BaseX Team 2005-14, BSD License
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
          if(k.equals(DF_DEC)) decimal = cp;
          else if(k.equals(DF_GRP)) grouping = cp;
          else if(k.equals(DF_EXP)) exponent = cp;
          else if(k.equals(DF_PAT)) pattern = cp;
          else if(k.equals(DF_MIN)) minus = cp;
          else if(k.equals(DF_DIG)) optional = cp;
          else if(k.equals(DF_PC)) percent = cp;
          else if(k.equals(DF_PM)) permille = cp;
          else if(k.equals(DF_ZG)) {
            z = zeroes(cp);
            if(z == -1) throw INVDECFORM_X_X.get(info, k, v);
            if(z != cp) throw INVDECZERO_X.get(info, (char)  cp);
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
    final int[] ss = { decimal, grouping, exponent, percent, permille, zero, optional, pattern };
    for(final int s : ss) if(!is.add(s)) throw DUPLDECFORM_X.get(info, (char) s);

    // create auxiliary strings
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) tb.add(zero + i);
    digits = tb.toArray();
    actives = tb.add(decimal).add(grouping).add(optional).add(exponent).finish();
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
    final int i = indexOf(pic, pattern);
    if(i == -1) {
      tl.add(pic);
    } else {
      tl.add(substring(pic, 0, i));
      pic = substring(pic, i + cl(pic, i));
      if(contains(pic, pattern)) throw PICNUM_X.get(info, picture);
      tl.add(pic);
    }
    final byte[][] patterns = tl.finish();

    // check and analyze patterns
    if(!check(patterns)) throw PICNUM_X.get(info, picture);
    final Picture[] pics = analyze(patterns);

    // return formatted string
    return format(number, pics, info);
  }

  /**
   * Checks the syntax of the specified patterns.
   * @param patterns patterns
   * @return result of check
   */
  private boolean check(final byte[][] patterns) {
    for(final byte[] pt : patterns) {
      boolean frac = false, pas = false, act = false, exp = false;
      boolean dg = false, opt1 = false, opt2 = false;
      int cl, pc = 0, pm = 0, ls = 0;

      // loop through all characters
      final int pl = pt.length;
      for(int i = 0; i < pl; i += cl) {
        final int ch = ch(pt, i);
        cl = cl(pt, i);
        boolean active = contains(actives, ch);
        final boolean digit = contains(digits, ch);
        if(exp && !digit) return false;

        if(ch == decimal) {
          // more than 1 decimal sign?
          if(frac) return false;
          frac = true;
        } else if(ch == grouping) {
          // adjacent decimal sign?
          if(i == 0 && frac || ls == decimal || i + cl < pl ? ch(pt, i + cl) == decimal : !frac)
            return false;
        } else if(ch == exponent) {
          if(contains(actives, ls) && contains(actives, ch(pt, i + cl))) {
            // more than one exponent sign
            if(exp) return false;
            exp = true;
          } else {
            active = false;
          }
        } else if(ch == percent) {
          ++pc;
        } else if(ch == permille) {
          ++pm;
        } else if(ch == optional) {
          if(frac) {
            opt2 = true;
          } else {
            // integer part, and optional sign after digit?
            if(dg) return false;
            opt1 = true;
          }
        } else if(digit) {
          // fractional part, and digit after optional sign?
          if(frac && opt2) return false;
          dg = true;
        }

        // passive character with preceding and following active character?
        if(active && pas && act) return false;
        // will be assigned if active characters were found
        if(act) pas |= !active;
        act |= active;
        // cache last character
        ls = ch;
      }

      // percent and permille sign: more than 1, or exponent sign?
      if(pc + pm > (exp ? 0 : 1)) return false;
      // no optional sign or digit?
      if(!opt1 && !opt2 && !dg) return false;
    }
    return true;
  }

  /**
   * Analyzes the specified patterns.
   * @param patterns patterns
   * @return picture variables
   */
  private Picture[] analyze(final byte[][] patterns) {
    // pictures
    final int pl = patterns.length;
    final Picture[] pics = new Picture[pl];

    // analyze patterns
    for(int p = 0; p < pl; p++) {
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
      final int ptl = pt.length;
      for(int i = 0, cl; i < ptl; i += cl) {
        final int ch = ch(pt, i);
        cl = cl(pt, i);
        boolean active = contains(actives, ch);

        if(ch == decimal) {
          ++pos;
          act = false;
        } else if(ch == optional) {
          opt[pos]++;
        } else if(ch == exponent) {
          // check if following characters are all digits
          boolean e = true;
          for(int c = i + cl; c < ptl && e; c += cl(pt, c)) e = contains(digits, ch(pt, c));
          if(e && i + cl < ptl) {
            // test succeeds, exponent sign found
            exp = 0;
          } else {
            // passive character: add to prefixes/suffixes
            pic.xyzfix[pos == 0 && act ? pos + 1 : pos].add(ch);
            active = false;
          }
        } else if(ch == grouping) {
          if(pos == 0) pic.group[pos] = Array.add(pic.group[pos], pic.min[pos] + opt[pos]);
        } else if(contains(digits, ch)) {
          if(exp == -1) pic.min[pos]++;
          else exp++;
        } else {
          // passive characters
          pic.pc |= ch == percent;
          pic.pm |= ch == permille;
          // prefixes/suffixes
          pic.xyzfix[pos == 0 && act ? pos + 1 : pos].add(ch);
        }
        act |= active;
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
   * Formats the specified number and returns a string representation.
   * @param it item
   * @param pics pictures
   * @param ii input info
   * @return picture variables
   * @throws QueryException query exception
   */
  private byte[] format(final ANum it, final Picture[] pics, final InputInfo ii)
      throws QueryException {

    // return results for NaN
    final double d = it.dbl(ii);
    if(Double.isNaN(d)) return nan;

    // return infinite results
    final boolean neg = d < 0 || d == 0 && Double.doubleToLongBits(d) == Long.MIN_VALUE;
    final Picture pic = pics[neg && pics.length == 2 ? 1 : 0];
    final TokenBuilder res = new TokenBuilder();
    final TokenBuilder intgr = new TokenBuilder();
    final TokenBuilder fract = new TokenBuilder();
    int exp = 0;

    if(d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY) {
      intgr.add(inf);
    } else {
      // convert and round number
      ANum num = it;
      if(pic.pc) num = (ANum) Calc.MULT.ev(ii, num, Int.get(100));
      if(pic.pm) num = (ANum) Calc.MULT.ev(ii, num, Int.get(1000));
      if(pic.minExp != 0) {
        final String s = (num instanceof Dbl || num instanceof Flt ?
          Dec.get(num.dbl(ii)) : num).abs().toString();
        final int sep = s.indexOf('.');
        final int i = sep == -1 ? s.length() : sep;
        final int m = pic.min[0];
        double n = 1;
        exp = i - m;
        if(exp > 0) {
          for(int a = exp; a-- > 0;) n *= 10;
          num = (ANum) Calc.DIV.ev(ii, num, Dec.get(n));
        } else {
          for(int a = -exp; a-- > 0;) n *= 10;
          num = (ANum) Calc.MULT.ev(ii, num, Dec.get(n));
        }
      }
      num = num.round(pic.maxFrac, true).abs();

      // convert positive number to string, chop leading zero
      final String s = (num instanceof Dbl || num instanceof Flt ?
          Dec.get(num.dbl(ii)) : num).toString();

      // integer/fractional separator
      final int sep = s.indexOf('.');

      // create integer part
      final int sl = s.length();
      final int il = sep == -1 ? sl : sep;
      for(int i = il; i < pic.min[0]; ++i) intgr.add(zero);
      for(int i = 0; i < il; i++) intgr.add(zero + s.charAt(i) - '0');

      // squeeze in grouping separators
      if(pic.group[0].length == 1) {
        // regular pattern with repeating separators
        final int pos = pic.group[0][0];
        for(int p = intgr.size() - (neg ? 2 : 1); p > 0; --p) {
          if(p % pos == 0) intgr.insert(intgr.size() - p, grouping);
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
    res.add(pic.xyzfix[0].toArray()).add(intgr.finish());
    // add fractional part
    if(!fract.isEmpty()) res.add(decimal).add(fract.finish());
    // add suffix
    res.add(pic.xyzfix[1].toArray());
    // add exponent
    if(pic.minExp != 0) {
      res.add(exponent);
      if(exp < 0) res.add(minus);
      final String s = Integer.toString(Math.abs(exp));
      final int sl = s.length();
      for(int i = sl; i < pic.minExp; i++) res.add(zero);
      for(int i = 0; i < sl; i++) res.add(zero + s.charAt(i) - '0');
    }
    return res.finish();
  }

  /** Picture variables. */
  static final class Picture {
    /** Prefix/suffix. */
    final TokenBuilder[] xyzfix = { new TokenBuilder(), new TokenBuilder() };
    /** Integer/fractional-part-grouping-positions. */
    final int[][] group = { {}, {} };
    /** Minimum-integer/fractional-part-size. */
    final int[] min = { 0, 0 };
    /** Maximum-fractional-part-size. */
    int maxFrac;
    /** Minimum-exponent-size. */
    int minExp;
    /** Percent flag. */
    boolean pc;
    /** Per-mille flag. */
    boolean pm;
  }
}
