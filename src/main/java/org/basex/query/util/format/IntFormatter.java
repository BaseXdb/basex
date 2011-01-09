package org.basex.query.util.format;

import static org.basex.util.Token.*;
import org.basex.query.util.format.FormatParser.Case;
import org.basex.util.TokenBuilder;

/**
 * Integer formatter.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class IntFormatter {
  /** Roman numbers (1-10). */
  private static final byte[][] ROMANI =
    tokens("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX");
  /** Roman numbers (10-100). */
  private static final byte[][] ROMANX =
    tokens("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC");
  /** Roman numbers (100-1000). */
  private static final byte[][] ROMANC =
    tokens("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM");
  /** Roman numbers (1000-3000). */
  private static final byte[][] ROMANM = tokens("", "M", "MM", "MMM");

  /** Private constructor. */
  private IntFormatter() { }

  /**
   * Returns a formatted number.
   * @param number number to be formatted
   * @param picture picture
   * @param lang language
   * @return string representation
   */
  public static byte[] format(final long number, final String picture,
      final String lang) {

    final Formatter form = Formatter.get(lang);
    final FormatParser fp = new FormatParser(picture, "1", false);
    return fp.error ? null : format(number, fp, form);
  }

  /**
   * Returns a formatted number.
   * @param number number to be formatted
   * @param mp marker parser
   * @param form formatter
   * @return string representation
   */
  public static byte[] format(final long number, final FormatParser mp,
      final Formatter form) {

    // choose sign
    long num = number;
    final boolean sign = num < 0;
    if(sign) num = -num;

    final TokenBuilder tb = new TokenBuilder();
    final int ch = cp(mp.pres, 0);
    final boolean single = mp.pres.length() == 1;
    if(ch == 'a' && single) {
      alpha(tb, num, 'a', 26);
    } else if(ch == '\u03b1' && single) {
      alpha(tb, num, '\u03b1', 25);
    } else if(ch == 'i' && single) {
      roman(tb, num);
    } else if(ch == 'w') {
      tb.add(form.word(num, mp.ord));
    } else if(ch >= '\u2460' && ch <= '\u249b') {
      tb.add((char) (ch + number - 1));
    } else if(ch == '#' || ch >= '0' && ch <= '9' ||
        ch >= '\u0660' && ch <= '\u0669') {
      number(tb, num, mp, form, ch & 0xFFFFFFF0);
    }

    // finalize formatted string
    byte[] in = tb.finish();
    if(mp.cs == Case.LOWER) in = lc(in);
    if(mp.cs == Case.UPPER) in = uc(in);
    return sign ? concat(new byte[] { '-' }, in) : in;
  }

  /**
   * Returns a Latin character sequence.
   * @param tb token builder
   * @param n number to be formatted
   * @param f start character
   * @param s alphabet size
   */
  private static void alpha(final TokenBuilder tb, final long n,
      final int f, final int s) {
    if(n > s) alpha(tb, (n - 1) / s, f, s);
    tb.add((char) (f + (n - 1) % s));
  }

  /**
   * Returns a Roman character sequence.
   * @param tb token builder
   * @param n number to be formatted
   */
  private static void roman(final TokenBuilder tb, final long n) {
    if(n < 4000) {
      final int v = (int) n;
      tb.add(ROMANM[v / 1000]);
      tb.add(ROMANC[v / 100 % 10]);
      tb.add(ROMANX[v / 10 % 10]);
      tb.add(ROMANI[v % 10]);
    } else {
      tb.addLong(n);
    }
  }

  /**
   * Returns a number character sequence.
   * @param tb token builder
   * @param n number to be formatted
   * @param mp marker parser
   * @param form language-dependent formatter
   * @param start start character
   */
  private static void number(final TokenBuilder tb, final long n,
      final FormatParser mp, final Formatter form, final int start) {

    // count optional-digit-signs
    final String pres = mp.pres;
    int o = 0;
    for(int i = 0; i < pres.length(); ++i) {
      if(pres.charAt(i) == '#') ++o;
    }
    // count digits
    int d = 0;
    for(int i = 0; i < pres.length(); ++i) {
      final char ch = pres.charAt(i);
      if(ch >= start && ch <= start + 9) ++d;
    }

    // create string representation and build string
    final String s = Long.toString(n);
    final StringBuilder tmp = new StringBuilder();
    final int r = o + d - s.length();
    for(int i = r; i > o; i--) tmp.append((char) start);
    for(int i = 0; i < s.length(); i++) {
      tmp.append((char) (s.charAt(i) - '0' + start));
    }

    for(int p = pres.length() - 1, t = tmp.length() - 1; p >= 0 && t >= 0;
        p--, t--) {
      final char ch = pres.charAt(p);
      if(ch < start && ch > start + 9 && ch != '#') tmp.insert(t, ch);
    }

    // add ordinal suffix
    tb.add(tmp.toString()).add(form.ordinal(n, mp.ord));
  }
}
