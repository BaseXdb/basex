package org.basex.query.util.format;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.basex.query.QueryException;
import org.basex.query.expr.Calc;
import org.basex.query.func.FNNum;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.IntSet;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;

/**
 * Formatter for decimal numbers.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DecimalFormat {
  /** Zero digits. */
  private static final int[] ZEROES = {
    0x30, 0x660, 0x6F0, 0x7C0, 0x966, 0x9E6, 0xA66, 0xAE6, 0xB66, 0xBE6, 0xC66,
    0xCE6, 0xD66, 0xE50, 0xED0, 0xF20, 0x1040, 0x1090, 0x17E0, 0x1810, 0x1946,
    0x19D0, 0x1A80, 0x1A90, 0x1B50, 0x1BB0, 0x1C40, 0x1C50, 0xA620, 0xA8D0,
    0xA900, 0xA9D0, 0xAA50, 0xABF0, 0xFF10, 0x104A0, 0x11066, 0x1D7CE, 0x1D7D8,
    0x1D7E2, 0x1D7EC, 0x1D7F6
  };

  /** Infinity. */
  String inf = "Infinity";
  /** NaN. */
  String nan = "NaN";
  /** Pattern-separator sign. */
  int pattern = ';';

  /** Decimal-separator sign. */
  int decimal = '.';
  /** Grouping-separator sign. */
  int group = ',';
  /** Digit sign. */
  int digit = '#';

  /** Minus sign. */
  String minus = "-";
  /** Percent sign. */
  int percent = '%';
  /** Permille sign. */
  int permille = '\u2030';
  /** Zero-digit sign. */
  int zero = '0';

  /** Mandatory-digit-sign. */
  private final String digits;
  /** Active characters. */
  private final String active;;

  /**
   * Default constructor.
   * @throws QueryException query exception
   */
  public DecimalFormat() throws QueryException {
    this(null, null);
  }

  /**
   * Constructor.
   * @param ii input info
   * @param map decimal format
   * @throws QueryException query exception
   */
  public DecimalFormat(final InputInfo ii, final HashMap<String, String> map)
      throws QueryException {

    // assign map values
    if(map != null) {
      final Iterator<String> it = map.keySet().iterator();
      while(it.hasNext()) {
        final String key = it.next();
        final String val = map.get(key);
        int cp = val.length() == 0 ? 0 : val.codePointAt(0);
        if(Character.charCount(cp) != val.length()) cp = 0;

        if(key.equals(DF_INF)) {
          inf = val;
        } else if(key.equals(DF_NAN)) {
          nan = val;
        } else if(cp != 0) {
          if(key.equals(DF_DEC)) decimal = cp;
          else if(key.equals(DF_GRP)) group = cp;
          else if(key.equals(DF_PAT)) pattern = cp;
          else if(key.equals(DF_MIN)) minus = val;
          else if(key.equals(DF_DIG)) digit = cp;
          else if(key.equals(DF_PC)) percent = cp;
          else if(key.equals(DF_PM)) permille = cp;
          else if(key.equals(DF_ZG)) {
            if(Arrays.binarySearch(ZEROES, cp) >= 0) zero = cp;
            else INVDECFORM.thrw(ii, key, val);
          }
        } else {
          INVDECFORM.thrw(ii, key, val);
        }
      }
    }

    // check for duplicate characters
    final IntSet is = new IntSet();
    final int[] t = { decimal, group, percent, permille, zero, digit, pattern };
    for(final int i : t) if(is.add(i) < 0) DUPLDECFORM.thrw(ii, (char) i);

    // create auxiliary strings
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < 10; i++) tb.add(zero + i);
    digits = tb.toString();
    active = tb.add(decimal).add(group).add(digit).toString();
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
    int i = pic.indexOf(pattern);
    if(i == -1) {
      tl.add(pic);
    } else {
      tl.add(pic.substring(0, i));
      pic = pic.substring(i + 1);
      if(pic.indexOf(pattern) != -1) PICNUM.thrw(ii, picture);
      tl.add(pic);
    }
    final byte[][] sub = tl.toArray();

    // check and analyze patterns
    if(!check(sub)) PICNUM.thrw(ii, picture);
    final Picture[] pics = analyze(sub);

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
      boolean frc = false, pas = false, act = false;
      boolean dg = false, opt1 = false, opt2 = false;
      int pc = 0, pm = 0;

      // loop through all characters
      for(int i = 0; i < pt.length; i += cl(pt, i)) {
        int ch = cp(pt, i);
        final boolean a = active.indexOf(ch) != -1;

        if(ch == decimal) {
          // more than 1 decimal sign?
          if(frc) return false;
          frc = true;
        } else if(ch == group) {
          // adjacent decimal sign?
          if(i > 0 && cp(pt, i - 1) == decimal || i + 1 < pt.length &&
              cp(pt, i + 1) == decimal) return false;
        } else if(ch == percent) {
          ++pc;
        } else if(ch == permille) {
          ++pm;
        } else if(ch == digit) {
          if(!frc) {
            // integer part, and optional sign after digit?
            if(dg) return false;
            opt1 = true;
          } else {
            opt2 = true;
          }
        } else if(digits.indexOf(ch) != -1) {
          // fractional part, and digit after optional sign?
          if(frc && opt2) return false;
          dg = true;
        }

        // passive character with preceding and following active character?
        if(a && pas && act) return false;
        // will be assigned if active characters were found
        if(act) pas |= !a;
        act |= a;
      }

      // more than 1 percent and permille sign?
      if(pc > 1 || pm > 1 || pc + pm > 1) return false;
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
        int ch = cp(pt, i);
        final boolean a = active.indexOf(ch) != -1;

        if(ch == decimal) {
          ++p;
          act = false;
        } else if(ch == digit) {
          opt[p]++;
        } else if(ch == group) {
          pic.group[p] = Array.add(pic.group[p], pic.min[p] + opt[p]);
        } else if(digits.indexOf(ch) != -1) {
          pic.min[p]++;
        } else {
          // passive characters
          pic.pc |= ch == percent;
          pic.pm |= ch == permille;
          // prefixes/suffixes
          pic.fix[p == 0 && act ? p + 1 : p] += (char) ch;
        }
        act |= a;
      }
      // finalize group positions
      for(int g = 0; g < pic.group[0].length; ++g) {
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
   * @param ii input info
   * @return picture variables
   * @throws QueryException query exception
   */
  private String format(final Item it, final Picture[] pics,
      final InputInfo ii) throws QueryException {

    final double d = it.dbl(ii);
    final Picture pic = pics[d < 0 && pics.length == 2 ? 1 : 0];
    if(d < 0 && pics.length == 1) pic.fix[0] = minus;

    // return results for NaN and infinity
    if(Double.isNaN(d)) return nan;
    if(Double.isInfinite(d)) return pic.fix[0] + inf + pic.fix[1];

    // convert and round number
    Item num = it;
    if(pic.pc) num = Calc.MULT.ev(ii, num, Itr.get(100));
    if(pic.pm) num = Calc.MULT.ev(ii, num, Itr.get(1000));
    num = FNNum.abs(FNNum.round(num, num.dbl(ii), pic.maxFrac, true, ii), ii);

    // convert to string representation
    final String str = num.toString();

    // integer/fractional separator
    final int sp = str.indexOf(decimal);

    // create integer part
    final TokenBuilder pre = new TokenBuilder();
    final int il = sp == -1 ? str.length() : sp;
    for(int i = il; i < pic.min[0]; ++i) pre.add('0');
    pre.add(str.substring(0, il));

    // squeeze in grouping separators
    final int pl = pre.size();
    for(int i = 0; i < pic.group[0].length; ++i) {
      final int pos = pl - pic.group[0][i];
      if(pos > 0) pre.insert(pos, group);
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
      if(pos < sl) suf.insert(pos, group);
    }

    final TokenBuilder res = new TokenBuilder(pic.fix[0]);
    res.add(pre.finish());
    if(suf.size() != 0) res.add(decimal).add(suf.finish());
    return res.add(pic.fix[1]).toString();
  }

  /** Picture variables. */
  static final class Picture {
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
