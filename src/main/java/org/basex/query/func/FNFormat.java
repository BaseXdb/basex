package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;
import org.basex.util.locale.Formatter;

/**
 * Formatting functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNFormat extends Fun {
  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FORMINT: return formatInt(ctx);
      case FORMNUM: return formatNum(ctx);
      case FORMDTM:
      case FORMDAT:
      case FORMTIM: Err.or(NOTIMPL, func.desc); return null;
      default:      return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FORMINT: return expr[0].e() ? atomic(ctx) : this;
      default:      return this;
    }
  }

  // PRIVATE METHODS ==========================================================

  /** Cases */
  private enum Case {
    /** Lower case. */ LOWER,
    /** Upper case. */ UPPER,
    /** Standard.   */ STANDARD;
  }

  /**
   * Returns a formatted integer.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatInt(final QueryContext ctx) throws QueryException {
    final byte[] pic = checkStr(expr[1], ctx);
    if(expr[0].e()) return Str.ZERO;

    final Formatter f = Formatter.get(
        string(expr.length == 2 ? EMPTY : checkStr(expr[2], ctx)));

    long num = checkItr(expr[0], ctx);
    if(pic.length == 0 || num == 0) return Str.get(token(num));

    // choose sign
    final boolean sign = num < 0;
    if(sign) num = -num;

    // choose first character and case
    final int ch = cp(pic, 0);
    Case cs = (ch & 0x20) != 0 ? Case.LOWER : Case.STANDARD;

    final TokenBuilder tb = new TokenBuilder();
    if((ch & 0xDF) == 'A') {
      latin(tb, num);
    } else if((ch & 0xDF) == 'I') {
      roman(tb, num);
    } else if((ch & 0xDF) == 'W') {
      if(ch == 'W' && charAt(pic, 1) != 'w') cs = Case.UPPER;
      tb.add(f.word(num, ord(pic, cs == Case.STANDARD ? 2 : 1)));
    } else {
      number(tb, num, pic, f);
    }

    // finalize formatted string
    byte[] result = tb.finish();
    if(sign) result = concat(new byte[] { '-' }, result);
    if(cs == Case.LOWER) result = lc(result);
    if(cs == Case.UPPER) result = uc(result);
    return Str.get(result);
  }


  /**
   * Returns ordinal characters.
   * @param pic picture
   * @param p position
   * @return ordinal bytes
   */
  protected static byte[] ord(final byte[] pic, final int p) {
    return charAt(pic, p)  != 'o' ? null :
      charAt(pic, p + 1) != '(' || charAt(pic, pic.length - 1) != ')' ? EMPTY :
      substring(pic, p + 2, pic.length - 1);
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
   * @param pic picture
   * @param form language-dependent formatter
   */
  private static void number(final TokenBuilder tb, final long n,
      final byte[] pic, final Formatter form) {

    // find optional ordinal modifier
    int p = 0;
    for(; p < pic.length && pic[p] != 'o'; p += cl(pic, p));

    // find optional-digit-signs
    int o = 0;
    for(; o < pic.length && pic[o] == '#'; o += cl(pic, o));

    // create string representation
    final byte[] str = token(n);
    // ordinal
    final byte[] ord = form.ordinal(n, ord(pic, p));

    final int d = p - str.length - ord.length;
    for(int i = Math.min(o, d); i > 0; i--) tb.add(' ');
    for(int i = d; i > o; i--) tb.add('0');
    tb.add(str);
    tb.add(ord);
  }

  /**
   * Returns a formatted number.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatNum(final QueryContext ctx) throws QueryException {
    // evaluate arguments
    Item it = expr[0].atomic(ctx);
    if(it == null) it = Dbl.NAN;
    else if(!it.u() && !it.n()) Err.num(info(), it);

    final String pic = string(checkStr(expr[1], ctx));
    if(expr.length == 3) Err.or(FORMNUM, expr[2]);

    return new FNFormatNum(it, pic).format();
  }
}
