package org.basex.query.util.format;

import static org.basex.util.Token.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /** Case. */
  public Case cs;
  /** Presentation modifier in lower-case. */
  public byte[] pres;
  /** Ordinal suffix; {@code null} if not specified. */
  public byte[] ordinal;
  /** Minimum width. */
  public int min;
  /** Maximum width. */
  public int max = Integer.MAX_VALUE;

  /**
   * Parses the input string.
   * @param in marker input
   * @param p (valid) presentation modifier
   * @param date flag flag, allowing width modifier
   * @return success flag
   */
  public boolean parse(final byte[] in, final byte[] p, final boolean date) {
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
   * Returns a presentation modifier, or {@code null} if the input was invalid.
   * @param in input
   * @param date flag flag, allowing width modifier
   * @return presentation modifier
   */
  private byte[] mod(final byte[] in, final boolean date) {
    final int l = in.length;
    int s = -1;

    final int ch = ch(in, 0);
    final int cu = ch | ' ';
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
    } else if(ch >= KANJI[1]) {
      // japanese numbering
      s = cl(in, 0);
    } else if(ch == '#') {
      // optional digits
      s = check(in, '0');
    } else {
      // optional digits
      s = zeroes(ch);
      if(s != -1) s = check(in, s);
    }
    if(s == -1) return null;

    byte[] pm = substring(in, 0, s);

    // find format modifier
    if(s < l) {
      if(ch(in, s) == 'o') {
        final TokenBuilder tb = new TokenBuilder();
        if(ch(in, ++s) == '(') {
          while(ch(in, ++s) != ')') {
            // ordinal isn't closed by a parenthesis
            if(s == l) return null;
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
      if(ch(in, s) != ',') return null;
      pm = concat(pm, substring(in, s));
    }
    return pm;
  }

  /**
   * Checks a decimal-digit-pattern.
   * @param input input
   * @param zero zero char
   * @return end position, or {@code -1} for error
   */
  private static int check(final byte[] input, final int zero) {
    int s = 0;
    boolean d = false, g = false;
    final int l = input.length;
    for(; s < l; s += cl(input, s)) {
      final int ch = ch(input, s);
      if(Character.isLetter(ch)) break;

      if(ch == '#') {
        // optional after decimal sign
        if(d) return -1;
        g = false;
      } else if(ch == '*') {
        g = false;
      } else if(ch >= zero && ch <= zero + 9) {
        d = true;
        g = false;
      } else if(zeroes(ch) != -1) {
        return -1;
      } else {
        // adjacent grouping separators
        if(g) return -1;
        g = true;
      }
    }
    // check if decimal was found, and if last one is no grouping separator
    return !d || g  ? -1 : s;
  }
}
