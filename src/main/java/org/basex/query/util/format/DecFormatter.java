package org.basex.query.util.format;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.HashMap;
import java.util.Map.Entry;

import org.basex.query.QueryException;
import org.basex.query.expr.Calc;
import org.basex.query.func.FNNum;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.hash.IntSet;
import org.basex.util.list.TokenList;

/**
 * Formatter for decimal numbers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DecFormatter extends FormatUtil {
  /** Decimal-digit-family (mandatory-digit-sign). */
  private final String digits;
  /** Active characters. */
  private final String active;

  /** Infinity. */
  private String inf = "Infinity";
  /** NaN. */
  private String nan = "NaN";
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
  public DecFormatter(final InputInfo ii, final HashMap<String, String> map)
      throws QueryException {

    // assign map values
    /* Zero-digit sign. */
    int zero = '0';
    if(map != null) {
      for(final Entry<String, String> e : map.entrySet()) {
        final String key = e.getKey(), val = e.getValue();
        int cp = val.length() == 0 ? 0 : val.codePointAt(0);
        if(Character.charCount(cp) != val.length()) cp = 0;

        if(key.equals(DF_INF)) {
          inf = val;
        } else if(key.equals(DF_NAN)) {
          nan = val;
        } else if(cp != 0) {
          if(key.equals(DF_DEC)) decimal = cp;
          else if(key.equals(DF_GRP)) grouping = cp;
          else if(key.equals(DF_PAT)) pattern = cp;
          else if(key.equals(DF_MIN)) minus = cp;
          else if(key.equals(DF_DIG)) optional = cp;
          else if(key.equals(DF_PC)) percent = cp;
          else if(key.equals(DF_PM)) permille = cp;
          else if(key.equals(DF_ZG)) {
            zero = zeroes(cp);
            if(zero == -1) INVDECFORM.thrw(ii, key, val);
          }
        } else {
          INVDECFORM.thrw(ii, key, val);
        }
      }
    }

    // check for duplicate characters
    final IntSet is = new IntSet();
    for(final int i : new int[] { decimal, grouping, percent, permille,
            zero, optional, pattern }) {
      if(is.add(i) < 0) DUPLDECFORM.thrw(ii, (char) i);
    }

    // create auxiliary strings
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) tb.add(zero + i);
    digits = tb.toString();
    active = tb.add(decimal).add(grouping).add(optional).toString();
  }

  /**
   * Returns a formatted number.
   * @param ii input info
   * @param number number to be formatted
   * @param picture picture
   * @return string representation
   * @throws QueryException query exception
   */
  public byte[] format(final InputInfo ii, final Item number,
      final String picture) throws QueryException {

    // find pattern separator and sub-patterns
    final TokenList tl = new TokenList();
    String pic = picture;
    final int i = pic.indexOf(pattern);
    if(i == -1) {
      tl.add(pic);
    } else {
      tl.add(pic.substring(0, i));
      pic = pic.substring(i + 1);
      if(pic.indexOf(pattern) != -1) PICNUM.thrw(ii, picture);
      tl.add(pic);
    }
    final byte[][] patterns = tl.toArray();

    // check and analyze patterns
    if(!check(patterns)) PICNUM.thrw(ii, picture);
    final Picture[] pics = analyze(patterns);

    // return formatted string
    return token(format(number, pics, ii));
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
        final boolean a = active.indexOf(ch) != -1;

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
        } else if(digits.indexOf(ch) != -1) {
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
        final boolean a = active.indexOf(ch) != -1;

        if(ch == decimal) {
          ++p;
          act = false;
        } else if(ch == optional) {
          opt[p]++;
        } else if(ch == grouping) {
          if(p == 0) {
            pic.group[p] = Array.add(pic.group[p], pic.min[p] + opt[p]);
          }
        } else if(digits.indexOf(ch) != -1) {
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
  private String format(final Item it, final Picture[] pics,
      final InputInfo ii) throws QueryException {

    // return results for NaN
    final double d = it.dbl(ii);
    if(Double.isNaN(d)) return nan;

    // return infinite results
    final Picture pic = pics[d < 0 && pics.length == 2 ? 1 : 0];
    if(d == Double.POSITIVE_INFINITY) return pic.fix[0] + inf + pic.fix[1];
    if(d == Double.NEGATIVE_INFINITY) return new TokenBuilder(
        pic.fix[0].finish()).add(minus) + inf + pic.fix[1];

    // convert and round number
    Item num = it;
    if(pic.pc) num = Calc.MULT.ev(ii, num, Int.get(100));
    if(pic.pm) num = Calc.MULT.ev(ii, num, Int.get(1000));
    num = FNNum.round(num, num.dbl(ii), pic.maxFrac, true, ii);
    // remove sign: num = FNNum.abs(num);

    // convert to string representation
    String str = num.toString();
    if(str.startsWith("0.")) str = str.substring(1);

    // integer/fractional separator
    final int sp = str.indexOf(decimal);

    // create integer part
    final TokenBuilder pre = new TokenBuilder();
    final int il = sp == -1 ? str.length() : sp;
    for(int i = il; i < pic.min[0]; ++i) pre.add('0');
    pre.add(str.substring(0, il));

    // squeeze in grouping separators
    if(pic.group[0].length == 1) {
      // regular pattern with repeating separators
      final int pos = pic.group[0][0];
      for(int p = pre.size() - 1; p > 0; --p) {
        if(p % pos == 0) pre.insert(pre.size() - p, grouping);
      }
    } else {
      // irregular pattern, or no separators at all
      for(int i = 0; i < pic.group[0].length; ++i) {
        final int pos = pre.size() - pic.group[0][i];
        if(pos > 0) pre.insert(pos, grouping);
      }
    }

    // create fractional part
    final TokenBuilder suf = new TokenBuilder();
    final int fl = sp == -1 ? 0 : str.length() - il - 1;
    if(fl != 0) suf.add(str.substring(sp + 1));
    for(int i = fl; i < pic.min[1]; ++i) suf.add('0');

    // squeeze in grouping separators in a reverse manner
    final int sl = suf.size();
    for(int i = pic.group[1].length - 1; i >= 0; i--) {
      final int pos = pic.group[1][i];
      if(pos < sl) suf.insert(pos, grouping);
    }

    final TokenBuilder res = new TokenBuilder(pic.fix[0].finish());
    res.add(pre.finish());
    if(suf.size() != 0) res.add(decimal).add(suf.finish());
    return res.add(pic.fix[1].finish()).toString();
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
