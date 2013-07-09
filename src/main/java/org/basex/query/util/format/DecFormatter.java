package org.basex.query.util.format;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Formatter for decimal numbers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DecFormatter extends FormatUtil {
  /** Decimal-digit-family (mandatory-digit-sign). */
  public final byte[] digits;
  /** Active characters. */
  private final byte[] active;
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
   * @param ii input info
   * @param map decimal format
   * @throws QueryException query exception
   */
  public DecFormatter(final InputInfo ii, final TokenMap map) throws QueryException {
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
          else if(k.equals(DF_PAT)) pattern = cp;
          else if(k.equals(DF_MIN)) minus = cp;
          else if(k.equals(DF_DIG)) optional = cp;
          else if(k.equals(DF_PC)) percent = cp;
          else if(k.equals(DF_PM)) permille = cp;
          else if(k.equals(DF_ZG)) {
            z = zeroes(cp);
            if(z == -1) INVDECFORM.thrw(ii, k, v);
            if(z != cp) INVDECZERO.thrw(ii, (char)  cp);
          }
        } else {
          // signs must have single character
          INVDECSINGLE.thrw(ii, k, v);
        }
      }
    }

    // check for duplicate characters
    zero = z;
    final IntSet is = new IntSet();
    final int[] ss = { decimal, grouping, percent, permille, zero, optional, pattern };
    for(final int s : ss) if(!is.add(s)) DUPLDECFORM.thrw(ii, (char) s);

    // create auxiliary strings
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) tb.add(zero + i);
    digits = tb.finish();
    active = tb.add(decimal).add(grouping).add(optional).finish();
  }

  /**
   * Returns a formatted number.
   * @param ii input info
   * @param number number to be formatted
   * @param pict picture
   * @return string representation
   * @throws QueryException query exception
   */
  public byte[] format(final InputInfo ii, final Item number, final byte[] pict)
      throws QueryException {

    // find pattern separator and sub-patterns
    final TokenList tl = new TokenList();
    byte[] pic = pict;
    final int i = indexOf(pic, pattern);
    if(i == -1) {
      tl.add(pic);
    } else {
      tl.add(substring(pic, 0, i));
      pic = substring(pic, i + cl(pic, i));
      if(contains(pic, pattern)) PICNUM.thrw(ii, pict);
      tl.add(pic);
    }
    final byte[][] patterns = tl.toArray();

    // check and analyze patterns
    if(!check(patterns)) PICNUM.thrw(ii, pict);
    final Picture[] pics = analyze(patterns);

    // return formatted string
    return format(number, pics, ii);
  }

  /**
   * Checks the syntax of the specified patterns.
   * @param patterns patterns
   * @return result of check
   */
  private boolean check(final byte[][] patterns) {
    for(final byte[] pt : patterns) {
      boolean frac = false, pas = false, act = false;
      boolean dg = false, opt1 = false, opt2 = false;
      int cl, pc = 0, pm = 0, ls = 0;

      // loop through all characters
      for(int i = 0; i < pt.length; i += cl) {
        final int ch = ch(pt, i);
        cl = cl(pt, i);
        final boolean a = contains(active, ch);

        if(ch == decimal) {
          // more than 1 decimal sign?
          if(frac) return false;
          frac = true;
        } else if(ch == grouping) {
          // adjacent decimal sign?
          if(i == 0 && frac || ls == decimal || i + cl < pt.length ?
              ch(pt, i + cl) == decimal : !frac) return false;
        } else if(ch == percent) {
          if(++pc > 1) return false;
        } else if(ch == permille) {
          if(++pm > 1) return false;
        } else if(ch == optional) {
          if(!frac) {
            // integer part, and optional sign after digit?
            if(dg) return false;
            opt1 = true;
          } else {
            opt2 = true;
          }
        } else if(contains(digits, ch)) {
          // fractional part, and digit after optional sign?
          if(frac && opt2) return false;
          dg = true;
        }

        // passive character with preceding and following active character?
        if(a && pas && act) return false;
        // will be assigned if active characters were found
        if(act) pas |= !a;
        act |= a;
        // cache last character
        ls = ch;
      }

      // more than 1 percent and permille sign?
      if(pc + pm > 1) return false;
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
    final Picture[] pics = new Picture[patterns.length];

    // analyze patterns
    for(int s = 0; s < patterns.length; ++s) {
      final byte[] pt = patterns[s];
      final Picture pic = new Picture();

      // position (integer/fractional)
      int p = 0;
      // active character found
      boolean act = false;
      // number of optional characters
      final int[] opt = new int[2];

      // loop through all characters
      for(int i = 0; i < pt.length; i += cl(pt, i)) {
        final int ch = ch(pt, i);
        final boolean a = contains(active, ch);

        if(ch == decimal) {
          ++p;
          act = false;
        } else if(ch == optional) {
          opt[p]++;
        } else if(ch == grouping) {
          if(p == 0) {
            pic.group[p] = Array.add(pic.group[p], pic.min[p] + opt[p]);
          }
        } else if(contains(digits, ch)) {
          pic.min[p]++;
        } else {
          // passive characters
          pic.pc |= ch == percent;
          pic.pm |= ch == permille;
          // prefixes/suffixes
          pic.fix[p == 0 && act ? p + 1 : p].add(ch);
        }
        act |= a;
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
      pics[s] = pic;
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
  private byte[] format(final Item it, final Picture[] pics, final InputInfo ii)
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

    if(d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY) {
      intgr.add(inf);
    } else {
      // convert and round number
      Item num = it;
      if(pic.pc) num = Calc.MULT.ev(ii, num, Int.get(100));
      if(pic.pm) num = Calc.MULT.ev(ii, num, Int.get(1000));
      num = FNNum.abs(FNNum.round(num, num.dbl(ii), pic.maxFrac, true, ii), ii);

      // convert positive number to string, chop leading zero
      String s = (num instanceof Dbl || num instanceof Flt ? Dec.get(d) : num).toString();
      if(s.startsWith("0.")) s = s.substring(1);

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
        for(int i = 0; i < pic.group[0].length; ++i) {
          final int pos = intgr.size() - pic.group[0][i];
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
    res.add(pic.fix[0].finish()).add(intgr.finish());
    // add fractional part
    if(!fract.isEmpty()) res.add(decimal).add(fract.finish());
    // add suffix
    return res.add(pic.fix[1].finish()).finish();
  }

  /** Picture variables. */
  static final class Picture {
    /** prefix/suffix. */
    final TokenBuilder[] fix = { new TokenBuilder(), new TokenBuilder() };
    /** integer/fractional-part-grouping-positions. */
    final int[][] group = { {}, {} };
    /** minimum-integer/fractional-part-size. */
    final int[] min = { 0, 0 };
    /** maximum-fractional-part-size. */
    int maxFrac;
    /** percent flag. */
    boolean pc;
    /** per-mille flag. */
    boolean pm;
  }
}
