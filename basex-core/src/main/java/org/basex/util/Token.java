package org.basex.util;

import java.text.*;
import java.util.*;

/**
 * <p>This class provides convenience operations for handling 'Tokens'.
 * A token is a UTF-8 encoded string. It is represented as a byte array.</p>
 *
 * <p>In order to ensure a consistent representation of tokens in the project, all string
 * conversions should be done via the methods of this class.</p>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Token {
  /** Maximum length for hash calculation. */
  private static final byte MAXLENGTH = 96;

  /** Maximum values for converting tokens to integer values. */
  private static final int MAXINT = Integer.MAX_VALUE / 10;
  /** Maximum values for converting tokens to long values. */
  private static final long MAXLONG = Long.MAX_VALUE / 10;

  /** Empty token. */
  public static final byte[] EMPTY = {};
  /** XML token. */
  public static final byte[] XML = token("xml");
  /** XML token with colon. */
  public static final byte[] XMLC = token("xml:");
  /** XMLNS token. */
  public static final byte[] XMLNS = token("xmlns");
  /** XMLNS token with colon. */
  public static final byte[] XMLNSC = token("xmlns:");
  /** Token 'true'. */
  public static final byte[] TRUE = token("true");
  /** Token 'false'. */
  public static final byte[] FALSE = token("false");
  /** Token 'null'. */
  public static final byte[] NULL = token("null");
  /** Token 'NaN'. */
  public static final byte[] NAN = token("NaN");
  /** Token 'INF'. */
  public static final byte[] INF = token("INF");
  /** Token '-INF'. */
  public static final byte[] NINF = token("-INF");
  /** Minimum long value. */
  public static final byte[] MINLONG = token("-9223372036854775808");
  /** Space. */
  public static final byte[] SPACE = { ' ' };
  /** Number '0'. */
  public static final byte[] ZERO = { '0' };
  /** Number '-0'. */
  private static final byte[] MZERO = { '-', '0' };
  /** Number '1'. */
  public static final byte[] ONE = { '1' };
  /** Slash. */
  public static final byte[] SLASH = { '/' };
  /** Colon. */
  public static final byte[] COLON = { ':' };

  /** Hex codes. */
  public static final byte[] HEX = token("0123456789ABCDEF");
  /** Reserved characters. */
  private static final byte[] IRIRES = token("!#$%&*'()+,-./:;=?@[]~_");
  /** Reserved characters. */
  private static final byte[] RES = token("-._~");

  /** Comparator for byte arrays. */
  public static final Comparator<byte[]> COMP = new Comparator<byte[]>() {
    @Override
    public int compare(final byte[] o1, final byte[] o2) {
      return diff(o1, o2);
    }
  };
  /** Case-insensitive comparator for byte arrays. */
  public static final Comparator<byte[]> LC_COMP = new Comparator<byte[]>() {
    @Override
    public int compare(final byte[] o1, final byte[] o2) {
      return diff(lc(o1), lc(o2));
    }
  };

  /** Hidden constructor. */
  private Token() { }

  /**
   * Returns the specified token as string.
   * @param token token
   * @return string
   */
  public static String string(final byte[] token) {
    return string(token, 0, token.length);
  }

  /**
   * Returns the specified token as string.
   * @param token token
   * @param start start position
   * @param length length
   * @return string
   */
  public static String string(final byte[] token, final int start, final int length) {
    if(length <= 0) return "";
    /// check if string contains non-ascii characters
    final int e = start + length;
    for(int p = start; p < e; ++p) if(token[p] < 0) return utf8(token, start, length);
    /// copy ascii characters to character array
    final char[] str = new char[length];
    for(int p = 0; p < length; ++p) str[p] = (char) token[start + p];
    return new String(str);
  }

  /**
   * Returns a string of the specified UTF8 token.
   * @param token token
   * @param start start position
   * @param length length
   * @return string
   */
  private static String utf8(final byte[] token, final int start, final int length) {
    // input is assumed to be correct UTF8. if input contains codepoints
    // larger than Character.MAX_CODE_POINT, results might be unexpected.

    final StringBuilder sb = new StringBuilder(length << 1);
    final int il = Math.min(start + length, token.length);
    for(int i = start; i < il; i += cl(token, i)) {
      final int cp = cp(token, i);
      if(cp < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
        sb.append((char) cp);
      } else {
        final int o = cp - Character.MIN_SUPPLEMENTARY_CODE_POINT;
        sb.append((char) ((o >>> 10) + Character.MIN_HIGH_SURROGATE));
        sb.append((char) ((o & 0x3ff) + Character.MIN_LOW_SURROGATE));
      }
    }
    return sb.toString();
  }

  /**
   * Checks if the specified token only consists of ASCII characters.
   * @param token token
   * @return result of check
   */
  public static boolean ascii(final byte[] token) {
    for(final byte t : token) if(t < 0) return false;
    return true;
  }

  /**
   * Converts a string to a byte array.
   * All strings should be converted by this function to guarantee
   * a consistent character conversion.
   * @param string string to be converted
   * @return byte array
   */
  public static byte[] token(final String string) {
    final int l = string.length();
    if(l == 0) return EMPTY;
    final byte[] b = new byte[l];
    for(int i = 0; i < l; ++i) {
      final char c = string.charAt(i);
      if(c > 0x7F) return utf8(string);
      b[i] = (byte) c;
    }
    return b;
  }

  /**
   * Converts the specified strings to tokens.
   * @param strings strings
   * @return tokens
   */
  public static byte[][] tokens(final String... strings) {
    final byte[][] tokens = new byte[strings.length][];
    final int tl = tokens.length;
    for(int t = 0; t < tl; ++t) tokens[t] = token(strings[t]);
    return tokens;
  }

  /**
   * Converts a string to a UTF8 byte array.
   * @param string string to be converted
   * @return byte array
   */
  private static byte[] utf8(final String string) {
    final char[] arr = string.toCharArray();
    final int al = arr.length;
    final TokenBuilder tb = new TokenBuilder(al << 1);
    for(int c = 0; c < al; ++c) {
      final char ch = arr[c];
      tb.add(Character.isHighSurrogate(ch) && c < al - 1
          && Character.isLowSurrogate(arr[c + 1])
          ? Character.toCodePoint(ch, arr[++c]) : ch);
    }
    return tb.finish();
  }

  /**
   * Converts a token from the input encoding to UTF8.
   * @param token token to be converted
   * @param encoding input encoding
   * @return byte array
   */
  public static byte[] utf8(final byte[] token, final String encoding) {
    // UTF8 (comparison by ref.) or no special characters: return input
    if(encoding == Strings.UTF8 || ascii(token)) return token;

    // convert to utf8. if errors occur while converting, an empty is returned.
    try {
      return token(new String(token, encoding));
    } catch(final Exception ex) {
      Util.debug(ex);
      return EMPTY;
    }
  }

  /**
   * Returns the codepoint (unicode value) of the specified token, starting at
   * the specified position. Returns a unicode replacement character for invalid
   * values.
   * @param token token
   * @param pos character position
   * @return current character
   */
  public static int cp(final byte[] token, final int pos) {
    // 0xxxxxxx
    final byte v = token[pos];
    if((v & 0xFF) < 192) return v & 0xFF;
    // number of bytes to be read
    final int vl = cl(v);
    if(pos + vl > token.length) return 0xFFFD;
    // 110xxxxx 10xxxxxx
    if(vl == 2) return (v & 0x1F) << 6 | token[pos + 1] & 0x3F;
    // 1110xxxx 10xxxxxx 10xxxxxx
    if(vl == 3) return (v & 0x0F) << 12 | (token[pos + 1] & 0x3F) << 6 |
      token[pos + 2] & 0x3F;
    // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
    return (v & 0x07) << 18 | (token[pos + 1] & 0x3F) << 12 |
      (token[pos + 2] & 0x3F) << 6 | token[pos + 3] & 0x3F;
  }

  /** Character lengths. */
  private static final int[] CHLEN = { 1, 1, 1, 1, 2, 2, 3, 4 };

  /**
   * Returns the length of the specified UTF8 byte.
   * @param cp codepoint
   * @return character length
   */
  public static int cl(final byte cp) {
    return cp >= 0 ? 1 : CHLEN[cp >> 4 & 0x7];
  }

  /**
   * Returns the length of a UTF8 character at the specified position.
   * @param token token
   * @param pos position
   * @return character length
   */
  public static int cl(final byte[] token, final int pos) {
    return cl(token[pos]);
  }

  /**
   * Converts a token to a sequence of codepoints.
   * @param token token
   * @return codepoints
   */
  public static int[] cps(final byte[] token) {
    int pos = 0;
    final int len = token.length;
    final int[] cp = new int[len];
    for(int i = 0; i < len; i += cl(token, i)) cp[pos++] = cp(token, i);
    return pos < len ? Arrays.copyOf(cp, pos) : cp;
  }

  /**
   * Returns the number of codepoints in the token.
   * @param token token
   * @return number of codepoints
   */
  public static int length(final byte[] token) {
    int l = 0;
    final int tl = token.length;
    for(int t = 0; t < tl; t += cl(token, t)) ++l;
    return l;
  }

  /**
   * Creates a byte array representation of the specified boolean value.
   * @param bool boolean value to be converted
   * @return boolean value in byte array
   */
  public static byte[] token(final boolean bool) {
    return bool ? TRUE : FALSE;
  }

  /**
   * Creates a byte array representation of the specified integer value.
   * @param integer int value to be converted
   * @return integer value in byte array
   */
  public static byte[] token(final int integer) {
    if(integer == 0) return ZERO;
    if(integer == Integer.MIN_VALUE) return MININT;

    int n = integer;
    final boolean m = n < 0;
    if(m) n = -n;
    int j = numDigits(n);
    if(m) ++j;
    final byte[] num = new byte[j];

    // faster division by 10 for values < 81920 (see Integer.getChars)
    while(n > 81919) {
      final int q = n / 10;
      num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
      n = q;
    }
    while(n != 0) {
      final int q = n * 52429 >>> 19;
      num[--j] = (byte) (n - (q << 3) - (q << 1) + '0');
      n = q;
    }
    if(m) num[--j] = '-';
    return num;
  }

  /**
   * Checks number of digits of the specified integer.
   * @param integer number to be checked
   * @return number of digits
   */
  public static int numDigits(final int integer) {
    for(int i = 0;; ++i) if(integer <= INTSIZE[i]) return i + 1;
  }

  /** Minimum integer. */
  private static final byte[] MININT = token("-2147483648");
  /** Table with integer sizes. */
  private static final int[] INTSIZE = { 9, 99, 999, 9999, 99999, 999999,
      9999999, 99999999, 999999999, Integer.MAX_VALUE };

  /**
   * Creates a byte array representation from the specified long value,
   * using Java's standard method.
   * @param integer value to be converted
   * @return byte array
   */
  public static byte[] token(final long integer) {
    return integer >= Integer.MIN_VALUE && integer <= Integer.MAX_VALUE ?
        token((int) integer) : token(Long.toString(integer));
  }

  /** US charset. */
  private static final DecimalFormatSymbols LOC =
    new DecimalFormatSymbols(Locale.US);
  /** Scientific double output. */
  private static final DecimalFormat SD =
    new DecimalFormat("0.0##################E0", LOC);
  /** Decimal double output. */
  private static final DecimalFormat DD =
    new DecimalFormat("#####0.0################", LOC);
  /** Scientific float output. */
  private static final DecimalFormat SF =
    new DecimalFormat("0.0######E0", LOC);
  /** Decimal float output. */
  private static final DecimalFormat DF =
    new DecimalFormat("#####0.0######", LOC);

  /**
   * Creates a byte array representation from the specified double value;
   * inspired by Xavier Franc's Qizx/open processor.
   * @param dbl double value to be converted
   * @return byte array
   */
  public static byte[] token(final double dbl) {
    final byte[] b = tok(dbl);
    if(b != null) return b;

    final double a = Math.abs(dbl);
    final String s;
    if(a >= 1e-6 && a < 1e6) {
      synchronized(DD) { s = DD.format(dbl); }
    } else {
      synchronized(SD) { s = SD.format(dbl); }
    }
    return chopNumber(token(s));
  }

  /**
   * Creates a byte array representation from the specified float value.
   * @param flt float value to be converted
   * @return byte array
   */
  public static byte[] token(final float flt) {
    final byte[] b = tok(flt);
    if(b != null) return b;

    // not that brilliant here.. no chance for elegant code either
    // due to the nifty differences between Java and XQuery
    final int fl = FLT.length;
    for(int i = 0; i < fl; ++i) if(flt == FLT[i]) return FLTSTR[i];
    final float a = Math.abs(flt);
    final boolean small = a >= 1e-6f && a < 1e6f;
    String s1;
    if(small) {
      synchronized(DF) { s1 = DF.format(flt); }
    } else {
      synchronized(SF) { s1 = SF.format(flt); }
    }
    final String s2 = Float.toString(flt);
    if(s2.length() < s1.length() && (!s2.contains("E") || !small)) s1 = s2;
    return chopNumber(token(s1));
  }

  /**
   * Checks if the specified value equals a constant token.
   * @param dbl value to be converted
   * @return byte array or zero or {@code null}
   */
  private static byte[] tok(final double dbl) {
    if(dbl == Double.POSITIVE_INFINITY) return INF;
    if(dbl == Double.NEGATIVE_INFINITY) return NINF;
    if(dbl == 0) return 1 / dbl > 0 ? ZERO : MZERO;
    if(Double.isNaN(dbl)) return NAN;
    final double a = Math.abs(dbl);
    if(a < 1e6) {
      final int i = (int) dbl;
      if(i == dbl) return token(i);
    }
    return null;
  }

  /**
   * Finishes the numeric token, removing trailing zeroes.
   * @param token token to be modified
   * @return token
   */
  public static byte[] chopNumber(final byte[] token) {
    if(!contains(token, '.') || contains(token, 'e') || contains(token, 'E')) return token;
    // remove trailing zeroes
    int l = token.length;
    while(--l > 0 && token[l] == '0');
    return substring(token, 0, token[l] == '.' ? l : l + 1);
  }

  /** Constant float values. */
  private static final float[] FLT = { 1.0E17f, 1.0E15f, 1.0E13f, 1.0E11f,
    -1.0E17f, -1.0E15f, -1.0E13f, -1.0E11f };
  /** Token representations of float values. */
  private static final byte[][] FLTSTR = tokens("1.0E17", "1.0E15",
    "1.0E13", "1.0E11", "-1.0E17", "-1.0E15", "-1.0E13", "-1.0E11");

  /**
   * Converts the specified token into a double value.
   * @param token token to be converted
   * @return resulting double value, or {@link Double#NaN} is returned if the input is invalid
   */
  public static double toDouble(final byte[] token) {
    final int tl = token.length;
    int s = -1;
    while(++s < tl && ws(token[s]));
    if(s == tl) return Double.NaN;

    int e = s;
    boolean f = false;
    for(int p = s; p < tl; ++p) {
      final byte b = token[p];
      if(e == s) {
        if(digit(b) || b == '+') continue;
        if(ws(b)) {
          e = p + 1;
        } else {
          f = b == 'e' || b == 'E' || b == '.' || b == '-';
          if(!f) return Double.NaN;
        }
      } else if(!ws(b)) {
        return Double.NaN;
      }
    }
    if(e == s) e = tl;
    if(f || e - s > 9) return toDouble(token, s, e);

    final int d = toInt(token, s, e);
    return d == Integer.MIN_VALUE ? Double.NaN : d;
  }

  /**
   * Converts the specified token into a double value.
   * {@link Double#NaN} is returned if the input is invalid.
   * @param token token to be converted
   * @param start first byte to be parsed
   * @param end last byte to be parsed - exclusive
   * @return resulting double value
   */
  private static double toDouble(final byte[] token, final int start, final int end) {
    try {
      return Double.parseDouble(string(token, start, end - start));
    } catch(final NumberFormatException ex) {
      return Double.NaN;
    }
  }

  /**
   * Converts the specified token into an long value.
   * {@link Long#MIN_VALUE} is returned if the input is invalid.
   * Note that this may also be the actual value ({@link #MINLONG})..
   * @param token token to be converted
   * @return resulting long value
   */
  public static long toLong(final byte[] token) {
    return toLong(token, 0, token.length);
  }

  /**
   * Converts the specified token into an long value.
   * {@link Long#MIN_VALUE} is returned if the input is invalid.
   * Note that this may also be the actual value ({@link #MINLONG})..
   * @param token token to be converted
   * @param start first byte to be parsed
   * @param end last byte to be parsed - exclusive
   * @return resulting long value
   */
  public static long toLong(final byte[] token, final int start, final int end) {
    int p = start;
    while(p < end && ws(token[p])) ++p;
    if(p == end) return Long.MIN_VALUE;
    boolean m = false;
    if(token[p] == '-' || token[p] == '+') m = token[p++] == '-';
    if(p == end) return Long.MIN_VALUE;
    long v = 0;
    for(; p < end; ++p) {
      final byte b = token[p];
      if(b < '0' || b > '9') break;
      if(v >= MAXLONG && (b > '7' || v > MAXLONG)) return Long.MIN_VALUE;
      v = (v << 3) + (v << 1) + b - '0';
    }
    while(p < end && ws(token[p])) ++p;
    return p < end ? Long.MIN_VALUE : m ? -v : v;
  }

  /**
   * Converts the specified token into an integer value.
   * {@link Integer#MIN_VALUE} is returned if the input is invalid.
   * @param token token to be converted
   * @return resulting integer value
   */
  public static int toInt(final byte[] token) {
    return toInt(token, 0, token.length);
  }

  /**
   * Converts the specified token into an integer value.
   * {@link Integer#MIN_VALUE} is returned if the input is invalid.
   * @param token token to be converted
   * @param start first byte to be parsed
   * @param end last byte to be parsed (exclusive)
   * @return resulting integer value
   */
  private static int toInt(final byte[] token, final int start, final int end) {
    int p = start;
    while(p < end && ws(token[p])) ++p;
    if(p == end) return Integer.MIN_VALUE;
    boolean m = false;
    if(token[p] == '-' || token[p] == '+') m = token[p++] == '-';
    if(p == end) return Integer.MIN_VALUE;
    int v = 0;
    for(; p < end; ++p) {
      final byte b = token[p];
      if(b < '0' || b > '9') break;
      if(v >= MAXINT && (b > '7' || v > MAXINT)) return Integer.MIN_VALUE;
      v = (v << 3) + (v << 1) + b - '0';
    }
    while(p < end && ws(token[p])) ++p;
    return p < end || v < 0 ? Integer.MIN_VALUE : m ? -v : v;
  }

  /**
   * Converts the specified token into a positive integer value.
   * {@link Integer#MIN_VALUE} is returned if non-digits are found
   * or if the input is longer than nine characters.
   * @param token token to be converted
   * @return resulting integer value
   */
  public static int toSimpleInt(final byte[] token) {
    final int te = token.length;
    if(te >= 10 || te == 0) return Integer.MIN_VALUE;
    if(token[0] == '0') return te == 1 ? 0 : Integer.MIN_VALUE;

    int v = 0;
    for(final byte c : token) {
      if(c < '0' || c > '9') return Integer.MIN_VALUE;
      v = (v << 3) + (v << 1) + c - '0';
    }
    return v;
  }

  /**
   * Calculates a hash code for the specified token.
   * @param token specified token
   * @return hash code
   */
  public static int hash(final byte[] token) {
    int h = 0;
    final int l = Math.min(token.length, MAXLENGTH);
    for(int i = 0; i != l; ++i) h = (h << 5) - h + token[i];
    return h;
  }

  /**
   * Compares two tokens for equality.
   * @param token1 first token
   * @param token2 token to be compared
   * @return true if the arrays are equal
   */
  public static boolean eq(final byte[] token1, final byte[] token2) {
    final int tl = token2.length;
    if(tl != token1.length) return false;
    for(int t = 0; t != tl; ++t) if(token2[t] != token1[t]) return false;
    return true;
  }

  /**
   * Compares several tokens for equality.
   * @param token token
   * @param tokens tokens to be compared
   * @return true if one test is successful
   */
  public static boolean eq(final byte[] token, final byte[]... tokens) {
    for(final byte[] t : tokens) if(eq(token, t)) return true;
    return false;
  }

  /**
   * Compares two tokens lexicographically.
   * @param token first token
   * @param compare token to be compared
   * @return 0 if tokens are equal, negative if first token is smaller,
   *         positive if first token is bigger
   */
  public static int diff(final byte[] token, final byte[] compare) {
    final int tl = token.length;
    final int cl = compare.length;
    final int l = Math.min(tl, cl);
    for(int i = 0; i < l; ++i) {
      final int c = (token[i] & 0xFF) - (compare[i] & 0xFF);
      if(c != 0) return c;
    }
    return tl - cl;
  }

  /**
   * Returns the smaller token.
   * @param token first token
   * @param compare token to be compared
   * @return smaller token
   */
  public static byte[] min(final byte[] token, final byte[] compare) {
    return diff(token, compare) < 0 ? token : compare;
  }

  /**
   * Returns the bigger token.
   * @param token first token
   * @param compare token to be compared
   * @return bigger token
   */
  public static byte[] max(final byte[] token, final byte[] compare) {
    return diff(token, compare) > 0 ? token : compare;
  }

  /**
   * Checks if the first token contains the second token.
   * @param token token
   * @param sub token to be found
   * @return result of test
   */
  public static boolean contains(final byte[] token, final byte[] sub) {
    return contains(token, sub, 0);
  }

  /**
   * Checks if the first token contains the second token.
   * @param token token
   * @param sub token to be found
   * @param pos start position
   * @return result of test
   */
  public static boolean contains(final byte[] token, final byte[] sub, final int pos) {
    return indexOf(token, sub, pos) != -1;
  }

  /**
   * Checks if the first token contains the specified character.
   * @param token token
   * @param ch character to be found
   * @return result of test
   */
  public static boolean contains(final byte[] token, final int ch) {
    return indexOf(token, ch) != -1;
  }

  /**
   * Returns the position of the specified character or -1.
   * @param token token
   * @param ch character to be found
   * @return position or {@code -1}
   */
  public static int indexOf(final byte[] token, final int ch) {
    final int tl = token.length;
    if(ch < 0x80) {
      for(int t = 0; t < tl; t++) if(token[t] == ch) return t;
    } else {
      for(int t = 0; t < tl; t += cl(token, t)) if(cp(token, t) == ch) return t;
    }
    return -1;
  }

  /**
   * Returns the last position of the specified character or -1.
   * @param token token
   * @param ch character to be found
   * @return position or {@code -1}
   */
  public static int lastIndexOf(final byte[] token, final int ch) {
    final int tl = token.length;
    int p = -1;
    if(ch < 128) {
      for(int t = tl - 1; t >= 0; --t) if(token[t] == ch) return t;
    } else {
      for(int t = 0; t < tl; t += cl(token, t)) if(cp(token, t) == ch) p = t;
    }
    return p;
  }

  /**
   * Returns the position of the specified token or -1.
   * @param token token
   * @param sub token to be found
   * @return position or {@code -1}
   */
  public static int indexOf(final byte[] token, final byte[] sub) {
    return indexOf(token, sub, 0);
  }

  /**
   * Returns the position of the specified token or -1.
   * @param token token
   * @param sub token to be found
   * @param pos start position
   * @return result of test
   */
  public static int indexOf(final byte[] token, final byte[] sub, final int pos) {
    final int sl = sub.length;
    if(sl == 0) return pos;
    final int tl = token.length - sl;
    if(pos > tl) return -1;

    // compare tokens character wise
    for(int t = pos; t <= tl; ++t) {
      int s = 0;
      while(sub[s] == token[t + s]) if(++s == sl) return t;
    }
    return -1;
  }

  /**
   * Checks if the first token starts with the specified character.
   * @param token token
   * @param ch character to be found
   * @return result of test
   */
  public static boolean startsWith(final byte[] token, final int ch) {
    return startsWith(token, ch, 0);
  }

  /**
   * Checks if the first token starts with the specified character.
   * @param token token
   * @param ch character to be found
   * @param pos start position
   * @return result of test
   */
  private static boolean startsWith(final byte[] token, final int ch, final int pos) {
    return pos < token.length && token[pos] == ch;
  }

  /**
   * Checks if the first token starts with the second token.
   * @param token token
   * @param sub token to be found
   * @return result of test
   */
  public static boolean startsWith(final byte[] token, final byte[] sub) {
    return startsWith(token, sub, 0);
  }

  /**
   * Checks if the first token starts with the second token.
   * @param token token
   * @param sub token to be found
   * @param pos start position
   * @return result of test
   */
  public static boolean startsWith(final byte[] token, final byte[] sub, final int pos) {
    final int sl = sub.length;
    if(sl > token.length - pos) return false;
    for(int s = 0, p = pos; s < sl; ++s, ++p) {
      if(sub[s] != token[p]) return false;
    }
    return true;
  }

  /**
   * Checks if the first token starts with the specified character.
   * @param token token
   * @param ch character to be bound
   * @return result of test
   */
  public static boolean endsWith(final byte[] token, final int ch) {
    return token.length != 0 && token[token.length - 1] == ch;
  }

  /**
   * Checks if the first token ends with the second token.
   * @param token token
   * @param sub token to be found
   * @return result of test
   */
  public static boolean endsWith(final byte[] token, final byte[] sub) {
    final int sl = sub.length;
    final int tl = token.length;
    if(sl > tl) return false;
    for(int s = sl; s > 0; s--) if(sub[sl - s] != token[tl - s]) return false;
    return true;
  }

  /**
   * Returns a substring of the specified token.
   * Note that this method does not correctly split UTF8 character;
   * use {@link #subtoken} instead.
   * @param token input token
   * @param start start position
   * @return substring
   */
  public static byte[] substring(final byte[] token, final int start) {
    return substring(token, start, token.length);
  }

  /**
   * Returns a substring of the specified token.
   * Note that this method does not correctly split UTF8 character;
   * use {@link #subtoken} instead.
   * @param token input token
   * @param start start position
   * @param end end position
   * @return substring
   */
  public static byte[] substring(final byte[] token, final int start, final int end) {
    final int s = Math.max(0, start);
    final int e = Math.min(end, token.length);
    if(s == 0 && e == token.length) return token;
    return s >= e ? EMPTY : Arrays.copyOfRange(token, s, e);
  }

  /**
   * Returns a partial token.
   * @param token input token
   * @param start start position
   * @return resulting text
   */
  public static byte[] subtoken(final byte[] token, final int start) {
    return subtoken(token, start, token.length);
  }

  /**
   * Returns a partial token.
   * @param token input text
   * @param start start position
   * @param end end position
   * @return resulting text
   */
  public static byte[] subtoken(final byte[] token, final int start, final int end) {
    int s = Math.max(0, start);
    final int e = Math.min(end, token.length);
    if(s == 0 && e == token.length) return token;
    if(s >= e) return EMPTY;

    int t = Math.max(0, s - 4);
    for(; t != s && t < e; t += cl(token, t)) {
      if(t >= s) s = t;
    }
    for(; t < e; t += cl(token, t));
    return Arrays.copyOfRange(token, s, t);
  }

  /**
   * Splits a token around matches of the given separator.
   * @param token token to be split
   * @param sep separation character
   * @return array
   */
  public static byte[][] split(final byte[] token, final int sep) {
    final int l = token.length;
    final byte[][] split = new byte[l][];

    int s = 0;
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < l; i += cl(token, i)) {
      final int c = cp(token, i);
      if(c == sep) {
        if(!tb.isEmpty()) split[s++] = tb.next();
      } else {
        tb.add(c);
      }
    }
    if(!tb.isEmpty()) split[s++] = tb.finish();
    return Array.copyOf(split, s);
  }

  /**
   * Checks if the specified token has only whitespaces.
   * @param token token
   * @return true if all characters are whitespaces
   */
  public static boolean ws(final byte[] token) {
    for(final byte t : token) if(!ws(t)) return false;
    return true;
  }

  /**
   * Replaces the specified character and returns the result token.
   * @param token token to be checked
   * @param search the character to be replaced
   * @param replace the new character
   * @return resulting token
   */
  public static byte[] replace(final byte[] token, final int search, final int replace) {
    if(!contains(token, search)) return token;

    final TokenBuilder tb = new TokenBuilder(token.length);
    final int tl = token.length;
    for(int i = 0; i < tl; i += cl(token, i)) {
      final int c = cp(token, i);
      tb.add(c == search ? replace : c);
    }
    return tb.finish();
  }

  /**
   * Removes leading and trailing whitespaces from the specified token.
   * @param token token to be trimmed
   * @return trimmed token
   */
  public static byte[] trim(final byte[] token) {
    int s = -1, e = token.length;
    while(++s < e) if(!ws(token[s])) break;
    while(--e > s) if(!ws(token[e])) break;
    if(++e == token.length && s == 0) return token;
    return s == e ? EMPTY : Arrays.copyOfRange(token, s, e);
  }

  /**
   * Chops a token to the specified length and adds dots.
   * @param token token to be chopped
   * @param max maximum length
   * @return chopped token
   */
  public static byte[] chop(final byte[] token, final int max) {
    if(token.length <= max) return token;
    final byte[] tt = Arrays.copyOf(token, max);
    if(max > 2) tt[max - 3] = '.';
    if(max > 1) tt[max - 2] = '.';
    if(max > 0) tt[max - 1] = '.';
    return tt;
  }

  /**
   * Concatenates two tokens.
   * @param token1 first token
   * @param token2 second token
   * @return resulting array
   */
  public static byte[] concat(final byte[] token1, final byte[] token2) {
    final int t1 = token1.length;
    final int t2 = token2.length;
    final byte[] tmp = new byte[t1 + t2];
    System.arraycopy(token1, 0, tmp, 0, t1);
    System.arraycopy(token2, 0, tmp, t1, t2);
    return tmp;
  }

  /**
   * Concatenates three tokens. A {@link TokenBuilder} instance can be used to
   * concatenate more than three tokens.
   * @param token1 first token
   * @param token2 second token
   * @param token3 third token
   * @return resulting array
   */
  public static byte[] concat(final byte[] token1, final byte[] token2, final byte[] token3) {
    final int t1 = token1.length;
    final int t2 = token2.length;
    final int t3 = token3.length;
    final byte[] tmp = new byte[t1 + t2 + t3];
    System.arraycopy(token1, 0, tmp, 0, t1);
    System.arraycopy(token2, 0, tmp, t1, t2);
    System.arraycopy(token3, 0, tmp, t1 + t2, t3);
    return tmp;
  }

  /**
   * Deletes a character from the token.
   * @param token token
   * @param ch character to be removed
   * @return resulting token
   */
  public static byte[] delete(final byte[] token, final int ch) {
    // ascii character
    if(ch < 0x80) {
      // skip deletion if character is not found
      if(!contains(token, ch)) return token;

      final int tl = token.length;
      final TokenBuilder tb = new TokenBuilder(tl);
      for(final byte c : token) {
        if(c != ch) tb.add(c);
      }
      return tb.finish();
    }
    // remove character
    final int tl = token.length;
    final TokenBuilder tb = new TokenBuilder(tl);
    for(int i = 0; i < tl; i += cl(token, i)) {
      final int c = cp(token, i);
      if(c != ch) tb.add(c);
    }
    return tb.finish();
  }

  /**
   * Normalizes all whitespace occurrences from the specified token.
   * @param token token
   * @return normalized token
   */
  public static byte[] normalize(final byte[] token) {
    final int l = token.length;
    final byte[] tmp = new byte[l];
    int c = 0;
    boolean ws1 = true;
    for(final byte t : token) {
      final boolean ws2 = ws(t);
      if(ws2 && ws1) continue;
      tmp[c++] = ws2 ? (byte) ' ' : t;
      ws1 = ws2;
    }
    if(c > 0 && ws(tmp[c - 1])) --c;
    return c == l ? tmp : Arrays.copyOf(tmp, c);
  }

  /**
   * Checks if the specified character is a whitespace.
   * @param ch the letter to be checked
   * @return result of check
   */
  public static boolean ws(final int ch) {
    return ch == 0x09 || ch == 0x0A || ch == 0x0D || ch == 0x20;
  }

  /**
   * Checks if the specified character is a computer letter (A - Z, a - z, _).
   * @param ch the letter to be checked
   * @return result of check
   */
  public static boolean letter(final int ch) {
    return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '_';
  }

  /**
   * Checks if the specified character is a digit (0 - 9).
   * @param ch the letter to be checked
   * @return result of check
   */
  public static boolean digit(final int ch) {
    return ch >= '0' && ch <= '9';
  }

  /**
   * Checks if the specified character is a computer letter or digit.
   * @param ch the letter to be checked
   * @return result of check
   */
  public static boolean letterOrDigit(final int ch) {
    return letter(ch) || digit(ch);
  }

  /**
   * Converts the specified token to upper case.
   * @param token token to be converted
   * @return resulting token
   */
  public static byte[] uc(final byte[] token) {
    if(ascii(token)) {
      final int tl = token.length;
      final byte[] tok = new byte[tl];
      for(int t = 0; t < tl; t++) tok[t] = (byte) uc(token[t]);
      return tok;
    }
    return token(string(token).toUpperCase(Locale.ENGLISH));
  }

  /**
   * Converts a character to upper case.
   * @param ch character to be converted
   * @return resulting character
   */
  public static int uc(final int ch) {
    return ch >= 'a' && ch <= 'z' ? ch - 0x20 :
      ch > 0x7F ? Character.toUpperCase(ch) : ch;
  }

  /**
   * Converts the specified token to lower case.
   * @param token token to be converted
   * @return resulting token
   */
  public static byte[] lc(final byte[] token) {
    if(ascii(token)) {
      final int tl = token.length;
      final byte[] tok = new byte[tl];
      for(int t = 0; t < tl; t++) tok[t] = (byte) lc(token[t]);
      return tok;
    }
    return token(string(token).toLowerCase(Locale.ENGLISH));
  }

  /**
   * Converts a character to lower case.
   * @param ch character to be converted
   * @return resulting character
   */
  public static int lc(final int ch) {
    return ch >= 'A' && ch <= 'Z' ? ch | 0x20 :
      ch > 0x7F ? Character.toLowerCase(ch) : ch;
  }

  /**
   * Returns the prefix of the specified token.
   * @param name name
   * @return prefix or empty token if no prefix exists
   */
  public static byte[] prefix(final byte[] name) {
    final int i = indexOf(name, ':');
    return i == -1 ? EMPTY : substring(name, 0, i);
  }

  /**
   * Returns the local name of the specified name.
   * @param name name
   * @return local name
   */
  public static byte[] local(final byte[] name) {
    final int i = indexOf(name, ':');
    return i == -1 ? name : substring(name, i + 1);
  }

  /**
   * Returns a URI encoded token.
   * @param token token
   * @param iri input
   * @return encoded token
   */
  public static byte[] uri(final byte[] token, final boolean iri) {
    final TokenBuilder tb = new TokenBuilder();
    for(final byte t : token) {
      if(letterOrDigit(t) || contains(iri ? IRIRES : RES, t)) tb.addByte(t);
      else hex(tb, t);
    }
    return tb.finish();
  }

  /**
   * Escapes the specified token.
   * @param token token
   * @return escaped token
   */
  public static byte[] escape(final byte[] token) {
    final TokenBuilder tb = new TokenBuilder();
    for(final byte t : token) {
      if(t >= 0x20 && t <= 0x7e) tb.addByte(t);
      else hex(tb, t);
    }
    return tb.finish();
  }

  /**
   * Adds the specified byte in hex code.
   * @param tb token builder
   * @param value byte to be added
   */
  private static void hex(final TokenBuilder tb, final byte value) {
    tb.add('%');
    tb.addByte(HEX[(value & 0xFF) >> 4]);
    tb.addByte(HEX[value & 0xFF & 15]);
  }

  /**
   * Returns a hex representation of the specified byte array.
   * @param value values to be mapped
   * @param uc upper case
   * @return hex representation
   */
  public static byte[] hex(final byte[] value, final boolean uc) {
    final int vl = value.length, u = uc ? 0x37 : 0x57;
    final byte[] data = new byte[vl << 1];
    for(int v = 0, c = 0; v < vl; v++) {
      int b = value[v] >> 4 & 0x0F;
      data[c++] = (byte) (b + (b > 9 ? u : '0'));
      b = value[v] & 0x0F;
      data[c++] = (byte) (b + (b > 9 ? u : '0'));
    }
    return data;
  }
}
