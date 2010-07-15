package org.basex.query.util.format;

import static org.basex.util.Token.*;
import org.basex.query.util.format.FormatParser.Case;
import org.basex.util.TokenBuilder;
import org.basex.util.locale.Formatter;

/**
 * Integer formatter.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    return format(number, new FormatParser(picture, "1", false), form);
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

    // to be checked...
    if(number == 0) return token(number);

    // choose sign
    long num = number;
    final boolean sign = num < 0;
    if(sign) num = -num;

    final TokenBuilder tb = new TokenBuilder();
    if(mp.pres.equals("a")) {
      latin(tb, num);
    } else if(mp.pres.equals("i")) {
      roman(tb, num);
    } else if(mp.pres.startsWith("w")) {
      tb.add(form.word(num, mp.ord));
    } else if(digit(cp(mp.pres, 0)) || cp(mp.pres, 0) == '#') {
      number(tb, num, mp, form);
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
   */
  private static void latin(final TokenBuilder tb, final long n) {
    if(n > 26) latin(tb, (n - 1) / 26);
    tb.add((char) ('A' + (n - 1) % 26));
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
      tb.add(n);
    }
  }

  /**
   * Returns a number character sequence.
   * @param tb token builder
   * @param n number to be formatted
   * @param mp marker parser
   * @param form language-dependent formatter
   */
  private static void number(final TokenBuilder tb, final long n,
      final FormatParser mp, final Formatter form) {

    // count optional-digit-signs
    final String pres = mp.pres;
    int o = 0;
    for(int i = 0; i < pres.length(); i++) {
      if(pres.charAt(i) == '#') o++;
    }
    // count digits
    int d = 0;
    for(int i = 0; i < pres.length(); i++) {
      if(digit(pres.charAt(i))) d++;
    }

    // create string representation
    final String str = Long.toString(n);

    // build string
    final StringBuilder tmp = new StringBuilder();
    final int r = o + d - str.length();
    for(int i = r; i > o; i--) tmp.append('0');
    tmp.append(str);

    for(int p = pres.length() - 1, t = tmp.length() - 1; p >= 0 && t >= 0;
        p--, t--) {
      final char ch = pres.charAt(p);
      if(!digit(ch) && ch != '#') tmp.insert(t, ch);
    }
    
    // add ordinal suffix
    tb.add(tmp);
    tb.add(form.ordinal(n, mp.ord));
  }
}
