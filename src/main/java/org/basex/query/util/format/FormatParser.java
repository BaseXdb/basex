package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Format parser for integers and dates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FormatParser extends FormatUtil {
  /** With pattern: ","  min-width ("-" max-width)?. */
  static final Pattern WIDTH = Pattern.compile("(\\*|\\d+)(-(\\*|\\d+))?");

  /** Input information. */
  public final InputInfo input;

  /** Case. */
  Case cs;
  /** Presentation modifier in lower-case. */
  byte[] pres;
  /** Ordinal suffix; {@code null} if not specified. */
  byte[] ordinal;
  /** Minimum width. */
  int min;
  /** Maximum width. */
  int max = Integer.MAX_VALUE;

  /**
   * Constructor.
   * @param ii input info
   */
  public FormatParser(final InputInfo ii) {
    input = ii;
  }

  /**
   * Parses the input string.
   * @param in marker input
   * @param p (valid) presentation modifier
   * @param date flag flag, allowing width modifier
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean parse(final byte[] in, final byte[] p, final boolean date)
      throws QueryException {

    // no marker specified - use default settings
    byte[] pm = in.length != 0 ? mod(in, date) : p;
    if(pm == null) return false;

    if(date) {
      // extract and check width modifier
      final int w = lastIndexOf(pm, ',');
      if(w != -1) {
        final byte[] wd = substring(pm, w + 1);
        pm = substring(pm, 0, w);

        final Matcher match = WIDTH.matcher(string(wd));
        if(!match.find()) return false;

        int m = toInt(match.group(1));
        if(m != Integer.MIN_VALUE) min = m;
        final String mc = match.group(3);
        m = mc != null ? toInt(mc) : Integer.MIN_VALUE;
        if(m != Integer.MIN_VALUE) max = m;
      }
    }

    // choose first character and case
    cs = pm.length > cl(pm, 0) ? Case.STANDARD :
      (ch(pm, 0) & ' ') != 0 ? Case.LOWER : Case.UPPER;
    pres = lc(pm);
    return true;
  }

  /**
   * Returns a presentation modifier.
   * @param in input
   * @param date flag flag, allowing width modifier
   * @return presentation modifier
   * @throws QueryException query exception
   */
  private byte[] mod(final byte[] in, final boolean date)
      throws QueryException {

    final int ch = ch(in, 0);
    final int cu = ch | ' ';
    int s;
    if(sequence(ch) != null) {
      // latin, greek and other alphabetics
      s = cl(in, 0);
    } else if(cu == 'i') {
      // roman sequence
      s = cl(in, 0);
    } else if(cu == 'w' || cu == 'n' && date) {
      // verbose, or name output
      s = ch(in, 1) == (ch | ' ') ? 2 : 1;
    } else if(ch >= '\u2460' && ch <= '\u249b') {
      // circled, parenthesized or full stop digits
      s = cl(in, 0);
    } else if(ch == KANJI[1]) {
      // japanese numbering
      s = cl(in, 0);
    } else {
      // grouping-separator, mandatory-digit, or optional-digit-sign
      s = check(in);
    }
    byte[] pm = substring(in, 0, s);

    // find format modifier
    final int l = in.length;
    if(s < l) {
      if(ch(in, s) == 'o') {
        final TokenBuilder tb = new TokenBuilder();
        if(ch(in, ++s) == '(') {
          while(ch(in, ++s) != ')') {
            // ordinal isn't closed by a parenthesis
            if(s == l) ORDCLOSED.thrw(input, in);
            tb.add(ch(in, s));
          }
          ++s;
        }
        ordinal = tb.finish();
      } else if(ch(in, s) == 't') {
        // traditional numbering (ignored)
        ++s;
      }
    }

    // find remaining modifier
    if(s < l) {
      // invalid remaining input
      if(ch(in, s) != ',') PICCOMP.thrw(input, in);
      pm = concat(pm, substring(in, s));
    }
    return pm;
  }

  /**
   * Parses a decimal-digit-pattern.
   * @param in input
   * @return end position
   * @throws QueryException query exception
   */
  private int check(final byte[] in) throws QueryException {
    int z = zeroes(ch(in, 0));
    if(z == -1) z = '0';
    int s = 0;
    boolean d = false, g = false;
    final int l = in.length;
    for(; s < l; s += cl(in, s)) {
      final int ch = ch(in, s);
      if(Character.isLetter(ch)) break;

      if(ch == '#') {
        // optional after decimal sign
        if(d) OPTAFTER.thrw(input, in);
        g = false;
      } else if(ch == '*') {
        g = false;
      } else if(ch >= z && ch <= z + 9) {
        d = true;
        g = false;
      } else if(zeroes(ch) != -1) {
        MANSAME.thrw(input, in);
      } else {
        // adjacent grouping separators
        if(g) GRPADJ.thrw(input, in);
        g = true;
      }
    }
    if(!d) NODEC.thrw(input, in);
    if(g) GRPSTART.thrw(input, in);
    return s;
  }
}
