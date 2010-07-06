package org.basex.query.func;

import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.util.TokenBuilder;

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
      case FORMINT:
        return formatInt(ctx);
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FORMINT:
        return expr[0].e() ? atomic(ctx) : this;
      default:
        return this;
    }
  }

  // PRIVATE METHODS ==========================================================
  
  /**
   * Returns a formatted integer.
   * @param ctx query context
   * @return string
   * @throws QueryException query exception
   */
  private Str formatInt(final QueryContext ctx) throws QueryException {
    if(expr[0].e()) return Str.ZERO;

    long num = checkItr(expr[0], ctx);
    final byte[] pic = checkStr(expr[1], ctx);
    if(pic.length == 0 || num == 0) return Str.get(token(num));

    // choose ordinal characters
    byte[] ord = ord(pic, 1);

    // choose sign
    final boolean sign = num < 0;
    if(sign) num = -num;

    // choose first character and case
    final byte ch = pic[0];
    Case cs = (ch & 0x20) != 0 ? Case.LOWER : Case.STANDARD;

    final TokenBuilder tb = new TokenBuilder();
    if((ch & 0xDF) == 'A') {
      latin(tb, num);
    } else if((ch & 0xDF) == 'I') {
      roman(tb, num);
    } else if((ch & 0xDF) == 'W') {
      if(pic.length == 1 || pic[1] != 'w') cs = Case.UPPER;
      else ord = ord(pic, 2);
      
      word(tb, num, ord != null);
      ord = null;
    } else if(digit(ch)) {
      number(tb, ch);
    }

    if(ord != null) {
      
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
  private byte[] ord(final byte[] pic, final int p) {
    return p >= pic.length || pic[p] != 'o' ? null : substring(pic, p + 1);
  }

  /** Cases */
  private enum Case {
    /** Lower case. */ LOWER,
    /** Upper case. */ UPPER,
    /** Standard.   */ STANDARD;
  }
  
  /**
   * Returns a Latin character sequence.
   * @param tb token builder
   * @param n number to be formatted
   */
  private void latin(final TokenBuilder tb, final long n) {
    if(n > 26) latin(tb, (n - 1) / 26);
    tb.add((char) ('A' + (n - 1) % 26));
  }

  /**
   * Returns a Roman character sequence.
   * @param tb token builder
   * @param n number to be formatted
   */
  private void roman(final TokenBuilder tb, final long n) {
    if(n < 4000) {
      tb.add(ROMAN1000[(int) n / 1000]);
      tb.add(ROMAN100 [(int) n / 100 % 10]);
      tb.add(ROMAN10  [(int) n / 10 % 10]);
      tb.add(ROMAN    [(int) n % 10]);
    } else {
      tb.add(n);
    }
  }

  /** Roman numbers (1-10). */
  private static final byte[][] ROMAN =
    tokens("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX");
  /** Roman numbers (10-100). */
  private static final byte[][] ROMAN10 =
    tokens("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC");
  /** Roman numbers (100-1000). */
  private static final byte[][] ROMAN100 =
    tokens("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM");
  /** Roman numbers (1000-3000). */
  private static final byte[][] ROMAN1000 =
    tokens("", "M", "MM", "MMM");
  
  /**
   * Returns an word character sequence (English; language specific).
   * @param tb token builder
   * @param n number to be formatted
   * @param ord ordinal flag
   */
  private void word(final TokenBuilder tb, final long n, final boolean ord) {
    if(n < 20) {
      tb.add((ord ? ORDINALS : WORDS)[(int) n]);
    } else if(n < 100) {
      int r = (int) (n % 10);
      if(r == 0) {
        tb.add((ord ? ORDINALS10 : WORDS10)[(int) n / 10]);
      } else {
        tb.add(WORDS10[(int) n / 10]);
        tb.add(' ').add((ord ? ORDINALS : WORDS)[r]);
      }
    } else {
      for(int w = 0; w < WORDS100.length; w++) {
        if(addWord(tb, n, UNITS100[w], WORDS100[w], ord)) break;
      }
    }
  }
  
  /**
   * Adds a unit if the number is large enough (English; language specific).
   * @param tb token builder
   * @param n number
   * @param f factor
   * @param unit unit
   * @param ord ordinal flag
   * @return true if word was added
   */
  private boolean addWord(final TokenBuilder tb, final long n, final long f,
      final byte[] unit, final boolean ord) {
    
    final boolean ge = n >= f;
    if(ge) {
      word(tb, n / f, false);
      long r = n % f;
      tb.add(' ').add(unit);
      if(ord) tb.add(TH);
      if(r > 0) {
        tb.add(' ');
        if(r < 100) tb.add(AND).add(' ');
      }
      word(tb, r, ord);
    }
    return ge;
  }

  /** Words (1-20). */
  private static final byte[][] WORDS = tokens("", "One", "Two", "Three",
      "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
      "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
      "Seventeen", "Eighteen", "Nineteen");
  /** Words (20-100). */
  private static final byte[][] WORDS10 = tokens("", "Ten", "Twenty", "Thirty",
      "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety");
  /** Words (1-20). */
  private static final byte[][] ORDINALS = tokens("", "First", "Second",
      "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth",
      "Tenth", "Eleventh", "Twelfth", "Thirteenth", "Fourteenth", "Fifteenth",
      "Sixteenth", "Seventeenth", "Eighteenth", "Nineteenth");
  /** Words (20-100). */
  private static final byte[][] ORDINALS10 = tokens("", "Tenth", "Twentieth",
      "Thirtieth", "Fortieth", "Fiftieth", "Sixtieth", "Seventieth",
      "Eightieth", "Ninetieth");
  /** Words (100, 1000, ...). */
  private static final byte[][] WORDS100 = tokens("Quintillion",
      "Quadrillion", "Trillion", "Billion", "Million", "Thousand", "Hundred");
  /** Units (100, 1000, ...). */
  private static final long[] UNITS100 = { 1000000000000000000L,
    1000000000000000L, 1000000000000L, 1000000000, 1000000, 1000, 100 };
  /** And. */
  private static final byte[] AND = token("and");
  /** Ordinal suffix (th). */
  private static final byte[] TH = token("th");

  /**
   * Returns an number character sequence.
   * @param tb token builder
   * @param n number to be formatted
   */
  private void number(final TokenBuilder tb, final long n) {
    tb.add(n);
  }
}
