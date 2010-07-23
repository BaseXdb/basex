package org.basex.query.util.format;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.query.expr.Calc;
import org.basex.query.func.FNNum;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.util.Err;
import org.basex.util.Array;

/**
 * Number formatter.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class NumFormatter {
  /** Infinity. */
  private static final String INF = "Infinity";
  /** NaN. */
  private static final String NAN = "NaN";
  /** Pattern-separator-sign. */
  private static final char PATTERN = ';';

  // active characters

  /** Mandatory-digit-sign. */
  private static final String DIGITS = "0123456789";
  /** Decimal-separator-sign. */
  private static final char DECIMAL = '.';
  /** Grouping-separator-sign. */
  private static final char GROUP = ',';
  /** Optional-digit-sign. */
  private static final char OPTIONAL = '#';
  /** Active characters. */
  private static final String ACTIVE = DIGITS + DECIMAL + GROUP + OPTIONAL;

  // passive characters

  /** Minus-sign. */
  private static final char MINUS = '-';
  /** Percent-sign. */
  private static final char PERCENT = '%';
  /** Permille-sign. */
  private static final char PERMILLE = '\u2030';

  /** Private constructor. */
  private NumFormatter() { }

  /**
   * Returns a formatted number.
   * @param number number to be formatted
   * @param picture picture
   * @return string representation
   * @throws QueryException query exception
   */
  public static byte[] format(final Item number, final String picture)
      throws QueryException {

    // find pattern separator and sub-patterns
    final String[] sub = picture.split(String.valueOf(PATTERN));
    if(sub.length > 2) Err.or(PICNUM, picture);

    // check and analyze patterns
    check(sub);
    final Picture[] pics = analyze(sub);

    // return formatted string
    return token(format(number, pics));
  }

  /**
   * Checks the syntax of the specified patterns.
   * @param patterns patterns
   * @throws QueryException query exception
   */
  private static void check(final String[] patterns) throws QueryException {
    for(final String pat : patterns) {
      boolean frac = false, pas = false, act = false;
      boolean dig = false, opt1 = false, opt2 = false;
      int pc = 0, pm = 0;

      // loop through all characters
      for(int i = 0; i < pat.length(); i++) {
        final char ch = pat.charAt(i);
        final boolean a = ACTIVE.indexOf(ch) != -1;

        if(ch == DECIMAL) {
          // more than 1 decimal sign?
          if(frac) Err.or(PICNUM, pat);
          frac = true;
        } else if(ch == GROUP) {
          // adjacent decimal sign?
          if(cp(pat, i - 1) == DECIMAL || cp(pat, i + 1) == DECIMAL)
            Err.or(PICNUM, pat);
        } else if(ch == PERCENT) {
          pc++;
        } else if(ch == PERMILLE) {
          pm++;
        } else if(ch == OPTIONAL) {
          if(!frac) {
            // integer part, and optional sign after digit?
            if(dig) Err.or(PICNUM, pat);
            opt1 = true;
          } else {
            opt2 = true;
          }
        } else if(DIGITS.indexOf(ch) != -1) {
          // fractional part, and digit after optional sign?
          if(frac && opt2) Err.or(PICNUM, pat);
          dig = true;
        }

        // passive character with preceding and following active character?
        if(a && pas && act) Err.or(PICNUM, pat);
        // will be assigned if active characters were found
        if(act) pas |= !a;
        act |= a;
      }

      // more than 1 percent and permille sign?
      if(pc > 1 || pm > 1 || pc + pm > 1) Err.or(PICNUM, pat);
      // no optional sign or digit?
      if(!opt1 && !opt2 && !dig) Err.or(PICNUM, pat);
    }
  }

  /**
   * Analyzes the specified patterns.
   * @param patterns patterns
   * @return picture variables
   */
  private static Picture[] analyze(final String[] patterns) {
    // pictures
    final Picture[] pics = new Picture[patterns.length];

    // analyze patterns
    for(int s = 0; s < patterns.length; s++) {
      final String pat = patterns[s];
      final Picture pic = new Picture();

      // position (integer/fractional)
      int p = 0;
      // active character found
      boolean act = false;
      // number of optional characters
      final int[] opt = new int[2];

      // loop through all characters
      for(int i = 0; i < pat.length(); i++) {
        final char ch = pat.charAt(i);
        final boolean a = ACTIVE.indexOf(ch) != -1;

        if(ch == DECIMAL) {
          p++;
          act = false;
        } else if(ch == OPTIONAL) {
          opt[p]++;
        } else if(ch == GROUP) {
          pic.group[p] = Array.add(pic.group[p], pic.min[p] + opt[p]);
        } else if(DIGITS.indexOf(ch) != -1) {
          pic.min[p]++;
        } else {
          // passive characters
          pic.pc |= ch == PERCENT;
          pic.pm |= ch == PERMILLE;
          // prefixes/suffixes
          pic.fix[p == 0 && act ? p + 1 : p] += ch;
        }
        act |= a;
      }
      // finalize group positions
      for(int g = 0; g < pic.group[0].length; g++) {
        pic.group[0][g] = pic.min[0] + opt[0] - pic.group[0][g];
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
   * @return picture variables
   * @throws QueryException query exception
   */
  private static String format(final Item it, final Picture[] pics)
      throws QueryException {

    final double d = it.dbl();
    final Picture pic = pics[d < 0 && pics.length == 2 ? 1 : 0];
    if(d < 0 && pics.length == 1) pic.fix[0] = String.valueOf(MINUS);

    // return results for NaN and infinity
    if(Double.isNaN(d)) return NAN;
    if(Double.isInfinite(d)) return pic.fix[0] + INF + pic.fix[1];

    // convert and round number
    Item num = it;
    if(pic.pc) num = Calc.MULT.ev(num, Itr.get(100));
    if(pic.pm) num = Calc.MULT.ev(num, Itr.get(1000));
    num = FNNum.abs(FNNum.round(num, num.dbl(), pic.maxFrac, true));

    // convert to string representation
    final String str = num.toString();

    // integer/fractional separator
    final int sp = str.indexOf(DECIMAL);

    // create integer part
    final StringBuilder pre = new StringBuilder();
    final int il = sp == -1 ? str.length() : sp;
    for(int i = il; i < pic.min[0]; i++) pre.append('0');
    pre.append(str.substring(0, il));

    // squeeze in grouping separators
    final int pl = pre.length();
    for(int i = 0; i < pic.group[0].length; i++) {
      final int pos = pl - pic.group[0][i];
      if(pos > 0) pre.insert(pos, GROUP);
    }

    // create fractional part
    final StringBuilder suf = new StringBuilder();
    final int fl = sp == -1 ? 0 : str.length() - il - 1;
    if(fl != 0) suf.append(str.substring(sp + 1));
    for(int i = fl; i < pic.min[1]; i++) suf.append('0');

    // squeeze in grouping separators in a reverse manner
    final int sl = suf.length();
    for(int i = pic.group[1].length - 1; i >= 0; i--) {
      final int pos = pic.group[1][i];
      if(pos < sl) suf.insert(pos, GROUP);
    }

    final StringBuilder res = new StringBuilder(pic.fix[0]);
    res.append(pre);
    if(suf.length() != 0) res.append(DECIMAL).append(suf);
    return res.append(pic.fix[1]).toString();
  }

  /** Picture variables. */
  static class Picture {
    /** prefix/suffix. */
    String[] fix = { "", "" };
    /** integer/fractional-part-grouping-positions. */
    int[][] group = { {}, {} };
    /** minimum-integer/fractional-part-size. */
    int[] min = { 0, 0 };
    /** maximum-fractional-part-size. */
    int maxFrac;
    /** percent flag. */
    boolean pc;
    /** per-mille flag. */
    boolean pm;
  }
}
