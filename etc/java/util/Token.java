package main.util;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This class provides convenience operations for handling so-called
 * 'Tokens'. Tokens in this project are nothing else than UTF8 encoded strings,
 * stored in a byte array.
 *
 * Note that, to guarantee a consistent string representation, all string
 * conversions should be done via the methods of this class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Token {
  /** Maximum length for hash calculation and index terms. */
  public static final byte MAXLEN = 96;

  /** Empty token. */
  public static final byte[] EMPTY = {};
  /** XML Token. */
  public static final byte[] XML = token("xml");
  /** XMLNS Token. */
  public static final byte[] XMLNS = token("xmlns");
  /** XMLNS Token with colon. */
  public static final byte[] XMLNSC = token("xmlns:");
  /** True token. */
  public static final byte[] TRUE = token("true");
  /** False token. */
  public static final byte[] FALSE = token("false");
  /** Not available number. */
  public static final byte[] NAN = token("NaN");
  /** Positive infinity. */
  public static final byte[] INF = token("INF");
  /** Negative infinity. */
  public static final byte[] NINF = token("-INF");
  /** Space token. */
  public static final byte[] SPACE = { ' ' };
  /** Zero token. */
  public static final byte[] ZERO = { '0' };
  /** Zero token. */
  public static final byte[] MZERO = { '-', '0' };
  /** One token. */
  public static final byte[] ONE = { '1' };

  /** Quote Entity. */
  public static final byte[] QU = token("\"");
  /** Ampersand Entity. */
  public static final byte[] AMP = token("&");
  /** Apostrophe Entity. */
  public static final byte[] APOS = token("'");
  /** GreaterThan Entity. */
  public static final byte[] GT = token(">");
  /** LessThan Entity. */
  public static final byte[] LT = token("<");

  /** Hex codes. */
  public static final byte[] HEX = token("0123456789ABCDEF");
  /** Reserved characters. */
  private static final byte[] IRIRES = token("!#$%&*'()+,-./:;=?@[]~_");
  /** Reserved characters. */
  private static final byte[] RES = token("-._~");

  /** UTF8 encoding string. */
  public static final String UTF8 = "UTF-8";
  /** UTF8 encoding string (variant). */
  public static final String UTF82 = "UTF8";
  /** UTF16 encoding string. */
  public static final String UTF16 = "UTF-16";
  /** UTF16 encoding string. */
  public static final String UTF162 = "UTF16";
  /** UTF16BE (=UTF16) encoding string. */
  public static final String UTF16BE = "UTF-16BE";
  /** UTF16 encoding string. */
  public static final String UTF16LE = "UTF-16LE";

  /** Hidden constructor. */
  private Token() { }

  /**
   * Returns the specified token as string.
   * @param text token
   * @return string
   */
  public static String string(final byte[] text) {
    return string(text, 0, text.length);
  }

  /**
   * Returns the specified token as string.
   * @param text token
   * @param s start position
   * @param l length
   * @return string
   */
  public static String string(final byte[] text, final int s, final int l) {
    if(l <= 0) return "";
    final char[] str = new char[l];
    for(int i = 0; i < l; i++) {
      if(text[s + i] < 0) return utf8(text, s, l);
      str[i] = (char) text[s + i];
    }
    return new String(str);
  }

  /**
   * Returns a string of the specified UTF8 token.
   * @param text token
   * @param s start position
   * @param l length
   * @return string
   */
  public static String utf8(final byte[] text, final int s, final int l) {
    try {
      return new String(text, s, l, UTF8);
    } catch(final Exception ex) {
      ex.printStackTrace();
      return "";
    }
  }

  /**
   * Checks if the specified token only consists of ASCII characters.
   * @param text token
   * @return result of check
   */
  public static boolean ascii(final byte[] text) {
    for(final byte t : text) if(t < 0) return false;
    return true;
  }

  /**
   * Checks if the specified UTF-8 characters are valid.
   * @param text UTF-8 characters
   * @return result of check
   */
  public static boolean isValidUTF8(final byte[] text) {
    final int l = text.length;
    int i = 0;
    while(i < l) {
      int cl = cl2(text[i]);
      if(cl <= 0 || cl > l - i++) return false;
      if(l == i) return true;
      final byte b = text[i];
      if(b >= 0 && b < ' ' && !ws(b)) return false; // control character
      while(--cl > 0) if(cl2(text[i++]) != 0) return false;
    }
    return true;
  }

  /**
   * Removes invalid characters from the UTF-8 sequence.
   * @param text the UTF-8 sequence to remove the invalid chars from
   * @param chop if true, all leading and trailing whitespaces are removed
   * @return the cleaned UTF-8 sequence
   */
  public static byte[] removeNonUTF8(final byte[] text, final boolean chop) {
    final int l = text.length;
    if(l == 0) return EMPTY;
    final byte[] t = new byte[l];
    int i = 0, p = 0;
    if(chop) while(i < l && ws(text[i])) ++i;
    if(i == l) return EMPTY;
    out: while(i < l) {
      final int cl = cl2(text[i]);
      if(cl <= 0) { ++i; continue; } // invalid ... ignore this one
      if(cl > l - i) break; // not enough bytes left, ignore everything behind
      final byte b = text[i];
      if(b >= 0 && b < ' ' && !ws(b)) { ++i; continue; } // ignore control chars
      t[p++] = text[i++]; // byte is valid .. copy to new array
      for(int j = 1; j < cl; j++) { // process all following bytes
        // all following bytes must have a codepoint length of zero.
        if(cl2(text[i]) != 0) {
          --p; // drop the already added first byte
          i += cl - j; // skip all bytes of this sequence
          continue out; // continue with the next UTF-8 character
        }
      }
      // all bytes are valid .. add them to the array
      for(int j = 1; j < cl; j++) t[p++] = text[i++];
    }
    if(chop) while(p > 0 && ws(t[p - 1])) --p;
    return p == 0 ? EMPTY : Arrays.copyOf(t, p);
  }

  /**
   * Converts a string to a byte array.
   * All strings should be converted by this function to guarantee
   * a consistent character conversion.
   * @param s string to be converted
   * @return byte array
   */
  public static byte[] token(final String s) {
    final int l = s.length();
    if(l == 0) return EMPTY;
    final byte[] bytes = new byte[l];
    for(int i = 0; i < l; i++) {
      final char c = s.charAt(i);
      if(c > 0x7F) return utf8(s);
      bytes[i] = (byte) c;
    }
    return bytes;
  }

  /**
   * Converts a string to a UTF8 byte array.
   * @param s string to be converted
   * @return byte array
   */
  private static byte[] utf8(final String s) {
    try {
      return s.getBytes(UTF8);
    } catch(final Exception ex) {
      ex.printStackTrace();
      return EMPTY;
    }
  }

  /**
   * Converts a token from the input encoding to UTF8.
   * @param s token to be converted
   * @return byte array
   * @param enc input encoding
   */
  public static byte[] utf8(final byte[] s, final String enc) {
    // no UTF8 (comparison by ref.) & no special characters: return input string
    if(enc == UTF8 || ascii(s)) return s;
    // convert to utf8
    try {
      return new String(s, enc).getBytes(UTF8);
    } catch(final Exception ex) {
      return EMPTY;
    }
  }

  /**
   * Returns a unified representation of the specified encoding.
   * @param enc input encoding
   * @param old (optional) old encoding
   * @return encoding
   */
  public static String enc(final String enc, final String old) {
    final String e = enc.toUpperCase();
    if(e.equals(UTF8) || e.equals(UTF82)) return UTF8;
    if(e.equals(UTF16BE)) return UTF16BE;
    if(e.equals(UTF16LE)) return UTF16LE;
    if(e.equals(UTF16) || e.equals(UTF162))
      return old == UTF16BE || old == UTF16LE ? old : UTF16BE;
    return enc;
  }

  /**
   * Returns the codepoint (unicode value) of the specified token,
   * starting at the specified position.
   * @param t token
   * @param p character position
   * @return current character
   */
  public static int cp(final byte[] t, final int p) {
    // 0xxxxxxx
    final int v = t[p] & 0xFF;
    if(v < 192) return v;

    // 110xxxxx 10xxxxxx
    final int c = v >> 4;
    int l = p;
    if(c == 12 || c == 13) return (v & 0x1F) << 6 | t[++l] & 0x3F;
    // 1110xxxx 10xxxxxx 10xxxxxx
    if(c == 14) return (v & 0x0F) << 12 | (t[++l] & 0x3F) << 6 | t[++l] & 0x3F;
    // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
    return (v & 0x07) << 18 | (t[++l] & 0x3F) << 12 |
      (t[++l] & 0x3F) << 6 | t[++l] & 0x3F;
  }

  /**
   * Returns the expected codepoint length of the specified byte.
   * @param v first character byte
   * @return character length
   */
  public static int cl(final byte v) {
    return v >= 0 ? 1 : CHLEN[v >> 4 & 0xF];
  }

  /*** Character lengths. */
  private static final int[] CHLEN = {
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4
  };

  /**
   * Checks if the byte is part of a valid UTF-8 character. Returns the expected
   * codepoint length of the specified byte, if it is the first byte of the
   * sequence. If the given byte is the second, third or fourth byte of the
   * sequence, zero is returned. A return value of -1 indicates an invalid UTF-8
   * character.
   * @param v first character byte
   * @return character length, if the byte is the first byte;
   *          zero if not; -1 if invalid
   */
  private static int cl2(final byte v) {
    final int i = v & 0xFF;
    return i == 0xC0 || i == 0xC1 || i > 0xF4 ? -1 : CHLEN2[i >> 4];
  }

  /*** Character lengths. */
  private static final int[] CHLEN2 = {
    1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 2, 3, 4
  };

  /**
   * Returns the token length.
   * @param text token
   * @return length
   */
  public static int len(final byte[] text) {
    int l = 0;
    for(int t = 0; t < text.length; t += cl(text[t])) l++;
    return l;
  }

  /**
   * Creates a byte array representation of the specified boolean value.
   * @param b boolean value to be converted
   * @return boolean value in byte array
   */
  public static byte[] token(final boolean b) {
    return b ? TRUE : FALSE;
  }

  /**
   * Creates a byte array representation of the specified integer value.
   * @param i int value to be converted
   * @return integer value in byte array
   */
  public static byte[] token(final int i) {
    if(i == 0) return ZERO;
    if(i == Integer.MIN_VALUE) return MININT;

    int n = i;
    final boolean m = n < 0;
    if(m) n = -n;
    int j = numDigits(n);
    if(m) j++;
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
   * @param x number to be checked
   * @return number of digits
   */
  public static int numDigits(final int x) {
    for(int i = 0;; i++) if(x <= INTSIZE[i]) return i + 1;
  }

  /** Minimum integer. */
  private static final byte[] MININT = token("-2147483648");
  /** Table with integer sizes. */
  private static final int[] INTSIZE = { 9, 99, 999, 9999, 99999, 999999,
      9999999, 99999999, 999999999, Integer.MAX_VALUE };

  /**
   * Creates a byte array representation from the specified long value,
   * using Java's standard method.
   * @param i int value to be converted
   * @return byte array
   */
  public static byte[] token(final long i) {
    return token(Long.toString(i));
  }

  /** US charset. */
  private static final java.text.DecimalFormatSymbols LOC =
    new java.text.DecimalFormatSymbols(java.util.Locale.US);
  /** Scientific double output. */
  private static final java.text.DecimalFormat SD =
    new java.text.DecimalFormat("0.0################E0##", LOC);
  /** Decimal double output. */
  private static final java.text.DecimalFormat DD =
    new java.text.DecimalFormat("#####0.0################", LOC);
  /** Scientific float output. */
  private static final java.text.DecimalFormat SF =
    new java.text.DecimalFormat("0.0####E0##", LOC);
  /** Decimal float output. */
  private static final java.text.DecimalFormat DF =
    new java.text.DecimalFormat("#####0.0######", LOC);

  /**
   * Creates a byte array representation from the specified double value;
   * inspired by Xavier Franc's Qizx.
   * @param d double value to be converted
   * @return byte array
   */
  public static byte[] token(final double d) {
    final byte[] b = dt(d);
    if(b != null) return b;

    final double a = Math.abs(d);
    return chopNumber(token(a >= 1e-6 && a < 1e6 ?
        DD.format(d) : SD.format(d)));
  }

  /**
   * Creates a byte array representation from the specified float value.
   * @param f float value to be converted
   * @return byte array
   */
  public static byte[] token(final float f) {
    final byte[] b = dt(f);
    if(b != null) return b;

    // not that brilliant here.. no chance for elegant code either
    // due to the nifty differences between Java and XQuery
    for(int i = 0; i < FLT.length; i++) if(f == FLT[i]) return FLTSTR[i];
    final float a = Math.abs(f);
    final boolean small = a >= 1e-6f && a < 1e6f;
    String s1 = small ? DF.format(f) : SF.format(f);
    final String s2 = Float.toString(f);
    if(s2.length() < s1.length() && (!s2.contains("E") || !small)) s1 = s2;
    return chopNumber(token(s1));
  }

  /**
   * Checks if the specified value equals a constant token.
   * @param d value to be converted
   * @return byte array or zero
   */
  private static byte[] dt(final double d) {
    if(d == 1 / 0d) return INF;
    if(d == -1 / 0d) return NINF;
    if(d == 0) return 1 / d > 0 ? ZERO : MZERO;
    if(d != d) return NAN;
    final double a = Math.abs(d);
    if(a < 1e6) {
      final int i = (int) d;
      if(i == d) return token(i);
    }
    return null;
  }

  /**
   * Finishes the numeric token, removing trailing zeroes.
   * @param t token to be modified
   * @return token
   */
  public static byte[] chopNumber(final byte[] t) {
    if(!contains(t, '.') || contains(t, 'e') || contains(t, 'E')) return t;
    // remove trailing zeroes
    int l = t.length;
    while(--l > 0 && t[l] == '0');
    return substring(t, 0, t[l] == '.' ? l : l + 1);
  }

  /** Constant float values. */
  private static final float[] FLT = { 1.0E17f, 1.0E15f, 1.0E13f, 1.0E11f,
    -1.0E17f, -1.0E15f, -1.0E13f, -1.0E11f };
  /** String representations of float values. */
  private static final byte[][] FLTSTR = { token("1.0E17"), token("1.0E15"),
    token("1.0E13"), token("1.0E11"), token("-1.0E17"), token("-1.0E15"),
    token("-1.0E13"), token("-1.0E11") };

  /**
   * Converts the specified token into a double value.
   * {@link Double#NaN} is returned if the input is invalid.
   * @param to character array to be converted
   * @return converted double value
   */
  public static double toDouble(final byte[] to) {
    final int tl = to.length;
    boolean f = false;
    for(final int t : to) {
      if(t >= 0 && t <= ' ' || digit(t)) continue;
      f = t == 'e' || t == 'E' || t == '.' || t == '-';
      if(!f) return Double.NaN;
    }
    if(f || tl > 9) return dbl(to);
    final int d = toInt(to);
    return d == Integer.MIN_VALUE ? Double.NaN : d;
  }

  /**
   * Converts the specified string into a double value.
   * {@link Double#NaN} is returned when the input is invalid.
   * @param to character array to be converted
   * @return converted double value
   */
  private static double dbl(final byte[] to) {
    try {
      return Double.parseDouble(string(to));
    } catch(final Exception ex) {
      return Double.NaN;
    }
  }

  /**
   * Converts the specified string into an long value.
   * {@link Long#MIN_VALUE} is returned when the input is invalid.
   * @param to character array to be converted
   * @return converted long value
   */
  public static long toLong(final String to) {
    return toLong(token(to));
  }

  /**
   * Converts the specified token into an long value.
   * {@link Long#MIN_VALUE} is returned when the input is invalid.
   * @param to character array to be converted
   * @return converted long value
   */
  public static long toLong(final byte[] to) {
    return toLong(to, 0, to.length);
  }

  /**
   * Converts the specified token into an long value.
   * {@link Long#MIN_VALUE} is returned when the input is invalid.
   * @param to character array to be converted
   * @param ts first byte to be parsed
   * @param te last byte to be parsed - exclusive
   * @return converted long value
   */
  public static long toLong(final byte[] to, final int ts, final int te) {
    int t = ts;
    while(t < te && to[t] <= ' ') t++;
    if(t == te) return Long.MIN_VALUE;
    boolean m = false;
    if(to[t] == '-' || to[t] == '+') m = to[t++] == '-';
    if(t == te) return Long.MIN_VALUE;
    long v = 0;
    for(; t < te; t++) {
      final byte c = to[t];
      if(c < '0' || c > '9') break;
      final long w = (v << 3) + (v << 1) + c - '0';
      if(w < v) return Long.MIN_VALUE;
      v = w;
    }
    while(t < te && to[t] <= ' ') t++;
    return t < te ? Long.MIN_VALUE : m ? -v : v;
  }

  /**
   * Converts the specified string into an integer value.
   * {@link Integer#MIN_VALUE} is returned when the input is invalid.
   * @param to character array to be converted
   * @return converted integer value
   */
  public static int toInt(final String to) {
    return toInt(token(to));
  }

  /**
   * Converts the specified token into an integer value.
   * {@link Integer#MIN_VALUE} is returned when the input is invalid.
   * @param to character array to be converted
   * @return converted integer value
   */
  public static int toInt(final byte[] to) {
    return toInt(to, 0, to.length);
  }

  /**
   * Converts the specified token into an integer value.
   * {@link Integer#MIN_VALUE} is returned when the input is invalid.
   * @param to character array to be converted
   * @param ts first byte to be parsed
   * @param te last byte to be parsed (exclusive)
   * @return converted integer value
   */
  public static int toInt(final byte[] to, final int ts, final int te) {
    int t = ts;
    while(t < te && to[t] <= ' ') t++;
    if(t == te) return Integer.MIN_VALUE;
    boolean m = false;
    if(to[t] == '-' || to[t] == '+') m = to[t++] == '-';
    if(t == te) return Integer.MIN_VALUE;
    int v = 0;
    for(; t < te; t++) {
      final byte c = to[t];
      if(c < '0' || c > '9') break;
      v = (v << 3) + (v << 1) + c - '0';
    }
    while(t < te && to[t] <= ' ') t++;
    return t < te ? Integer.MIN_VALUE : m ? -v : v;
  }

  /**
   * Converts the specified token into a positive integer value.
   * {@link Integer#MIN_VALUE} is returned if non-digits are found
   * or if the input is longer than nine characters.
   * @param to character array to be converted
   * @return converted integer value
   */
  public static int toSimpleInt(final byte[] to) {
    final int te = to.length;
    if(te >= 10 || te == 0) return Integer.MIN_VALUE;
    if(to[0] == '0') return te == 1 ? 0 : Integer.MIN_VALUE;

    int v = 0;
    for(int ts = 0; ts < te; ts++) {
      final byte c = to[ts];
      if(c < '0' || c > '9') return Integer.MIN_VALUE;
      v = (v << 3) + (v << 1) + c - '0';
    }
    return v;
  }

  /**
   * Calculates a hash code for the specified token.
   * @param tok specified token
   * @return hash code
   */
  public static int hash(final byte[] tok) {
    int h = 0;
    final int l = Math.min(tok.length, MAXLEN);
    for(int i = 0; i != l; i++) h = (h << 5) - h + tok[i];
    return h;
  }

  /**
   * Compares two character arrays for equality.
   * @param tok token to be compared
   * @param tok2 second token to be compared
   * @return true if the arrays are equal
   */
  public static boolean eq(final byte[] tok, final byte[] tok2) {
    final int tl = tok2.length;
    if(tl != tok.length) return false;
    for(int t = 0; t != tl; t++) if(tok2[t] != tok[t]) return false;
    return true;
  }

  /**
   * Calculates the difference of two character arrays.
   * @param tok token to be compared
   * @param tok2 second token to be compared
   * @return 0 if tokens are equal, negative if first token is smaller,
   *   positive if first token is bigger
   */
  public static int diff(final byte[] tok, final byte[] tok2) {
    final int l = Math.min(tok.length, tok2.length);
    for(int i = 0; i != l; i++) {
      final int c = (tok[i] & 0xFF) - (tok2[i] & 0xFF);
      if(c != 0) return c;
    }
    return tok.length - tok2.length;
  }

  /**
   * Calculates the difference of two characters.
   * @param c1 first character to be compared
   * @param c2 second character to be compared
   * @return 0 if characters are equal, negative if first token is smaller,
   *   positive if first character is bigger
   */
  public static int diff(final byte c1, final byte c2) {
    return (c1 & 0xFF) - (c2 & 0xFF);
  }

  /**
   * Checks if the first token contains the second token.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public static boolean contains(final byte[] tok, final byte[] sub) {
    return indexOf(tok, sub) != -1;
  }

  /**
   * Checks if the first token contains the specified character.
   * @param tok first token
   * @param c character
   * @return result of test
   */
  public static boolean contains(final byte[] tok, final int c) {
    return indexOf(tok, c) != -1;
  }

  /**
   * Returns the position of the specified character or -1.
   * @param tok first token
   * @param c character
   * @return result of test
   */
  public static int indexOf(final byte[] tok, final int c) {
    final int tl = tok.length;
    for(int t = 0; t < tl; t++) if(tok[t] == c) return t;
    return -1;
  }

  /**
   * Returns the position of the specified token or -1.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public static int indexOf(final byte[] tok, final byte[] sub) {
    return indexOf(tok, sub, 0);
  }

  /**
   * Returns the position of the specified token or -1.
   * @param tok first token
   * @param sub second token
   * @param p start position
   * @return result of test
   */
  public static int indexOf(final byte[] tok, final byte[] sub, final int p) {
    final int sl = sub.length;
    if(sl == 0) return 0;
    final int tl = tok.length - sl;
    if(p > tl) return -1;

    // compare tokens character wise
    for(int t = p; t <= tl; t++) {
      int s = 0;
      while(sub[s] == tok[t + s]) if(++s == sl) return t;
    }
    return -1;
  }

  /**
   * Checks if the first token starts with the specified character.
   * @param tok first token
   * @param c character
   * @return result of test
   */
  public static boolean startsWith(final byte[] tok, final int c) {
    return tok.length != 0 && tok[0] == c;
  }

  /**
   * Checks if the first token starts with the second token.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public static boolean startsWith(final byte[] tok, final byte[] sub) {
    final int sl = sub.length;
    if(sl > tok.length) return false;
    for(int s = 0; s < sl; s++) if(sub[s] != tok[s]) return false;
    return true;
  }

  /**
   * Checks if the first token starts with the specified character.
   * @param tok first token
   * @param c character
   * @return result of test
   */
  public static boolean endsWith(final byte[] tok, final int c) {
    return tok.length != 0 && tok[tok.length - 1] == c;
  }

  /**
   * Checks if the first token ends with the second token.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public static boolean endsWith(final byte[] tok, final byte[] sub) {
    final int sl = sub.length;
    final int tl = tok.length;
    if(sl > tl) return false;
    for(int s = sl; s > 0; s--) if(sub[sl - s] != tok[tl - s]) return false;
    return true;
  }

  /**
   * Returns a substring of the specified token.
   * @param tok token
   * @param s start position
   * @return substring
   */
  public static byte[] substring(final byte[] tok, final int s) {
    return substring(tok, s, tok.length);
  }

  /**
   * Returns a substring of the specified token.
   * @param tok token
   * @param s start position
   * @param e end position
   * @return substring
   */
  public static byte[] substring(final byte[] tok, final int s, final int e) {
    if(s == 0 && e == tok.length) return tok;
    return s >= e ? EMPTY : Arrays.copyOfRange(tok, s, e);
  }

  /**
   * Splits the token at all whitespaces and returns a array with all tokens.
   * @param tok token to be split
   * @param sep separation character
   * @return array
   */
  public static byte[][] split(final byte[] tok, final int sep) {
    final int l = tok.length;
    final byte[][] split = new byte[l][];

    int s = 0;
    final TokenBuilder sb = new TokenBuilder();
    for(int i = 0; i < l; i++) {
      final byte c = tok[i];
      if(c == sep) {
        if(sb.size() != 0) {
          split[s++] = sb.finish();
          sb.reset();
        }
      } else {
        sb.add(c);
      }
    }
    if(sb.size() != 0) split[s++] = sb.finish();
    return Arrays.copyOf(split, s);
  }

  /**
   * Checks if the specified token has only whitespaces.
   * @param tok token
   * @return true if all characters are whitespaces
   */
  public static boolean ws(final byte[] tok) {
    final int tl = tok.length;
    for(int i = 0; i < tl; i++) if(tok[i] < 0 || tok[i] > ' ') return false;
    return true;
  }

  /**
   * Replaces the specified character and returns the result token.
   * @param t token to be checked
   * @param s the character to be replaced
   * @param r the new character
   * @return resulting token
   */
  public static byte[] replace(final byte[] t, final int s, final int r) {
    final int tl = t.length;
    final byte[] tok = new byte[tl];
    for(int i = 0; i < tl; i++) tok[i] = t[i] == s ? (byte) r : t[i];
    return tok;
  }

  /**
   * Removes leading and trailing whitespaces from the specified token.
   * @param t token to be trimmed
   * @return trimmed token
   */
  public static byte[] trim(final byte[] t) {
    int s = -1;
    int e = t.length;
    while(++s < e) if(t[s] > ' ' || t[s] < 0) break;
    while(--e > s) if(t[e] > ' ' || t[e] < 0) break;
    if(++e == t.length && s == 0) return t;
    return s == e ? EMPTY : Arrays.copyOfRange(t, s, e);
  }

  /**
   * Chops a token to the specified length and adds dots.
   * @param t token to be chopped
   * @param l maximum length
   * @return chopped token
   */
  public static byte[] chop(final byte[] t, final int l) {
    if(t.length <= l) return t;
    final byte[] tt = Arrays.copyOf(t, l);
    tt[l - 3] = '.';
    tt[l - 2] = '.';
    tt[l - 1] = '.';
    return tt;
  }

  /**
   * Concatenates the specified tokens.
   * @param t tokens
   * @return resulting array
   */
  public static byte[] concat(final byte[]... t) {
    int s = 0;
    for(final byte[] tt : t) s += tt.length;
    final byte[] tmp = new byte[s];
    int l = 0;
    for(final byte[] tt : t) {
      System.arraycopy(tt, 0, tmp, l, tt.length);
      l += tt.length;
    }
    return tmp;
  }

  /**
   * Deletes the specified character out of the token.
   * @param t token to be checked
   * @param c character to be removed
   * @return new instance
   */
  public static byte[] delete(final byte[] t, final int c) {
    final TokenBuilder sb = new TokenBuilder(t.length);
    for(final byte b : t) if(b != c) sb.add(b);
    return sb.finish();
  }

  /**
   * Normalizes all whitespace occurrences from the specified token.
   * @param tok token
   * @return normalized token
   */
  public static byte[] norm(final byte[] tok) {
    final int l = tok.length;
    final byte[] tmp = new byte[l];
    int c = 0;
    boolean ws1 = true;
    for(int i = 0; i < l; i++) {
      final boolean ws2 = ws(tok[i]);
      if(ws2 && ws1) continue;
      tmp[c++] = ws2 ? (byte) ' ' : tok[i];
      ws1 = ws2;
    }
    if(c > 0 && ws(tmp[c - 1])) c--;
    return c == l ? tmp : Arrays.copyOf(tmp, c);
  }

  /**
   * Checks if the specified character is a whitespace.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean ws(final int ch) {
    return ch == 0x09 || ch == 0x0A || ch == 0x0D || ch == 0x20;
  }

  /**
   * Checks if the specified character is a computer letter (A - Z, a - z, _).
   * @param c the letter to be checked
   * @return result of comparison
   */
  public static boolean letter(final int c) {
    return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '_';
  }

  /**
   * Checks if the specified character is a digit (0 - 9).
   * @param c the letter to be checked
   * @return result of comparison
   */
  public static boolean digit(final int c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Checks if the specified character is a computer letter or digit.
   * @param c the letter to be checked
   * @return result of comparison
   */
  public static boolean letterOrDigit(final int c) {
    return letter(c) || digit(c);
  }

  /**
   * Returns true if the specified character is a full-text letter or digit.
   * @param ch character to be tested
   * @return result of check
   */
  public static boolean ftChar(final int ch) {
    if(ch < '0') return false;
    if(ch < 128) return LOD[ch - '0'];
    return Character.isLetterOrDigit(ch);
  }

  /** Letter-or-digit table for ASCII codes larger than '0'. */
  private static final boolean[] LOD = {
    true,  true,  true,  true,  true,  true,  true,  true,
    true,  true,  false, false, false, false, false, false,
    false, true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  false, false, false, false, false,
    false, true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  false, false, false, false, false
  };

  /**
   * Converts the specified token to upper case.
   * @param t token to be converted
   * @return the converted token
   */
  public static byte[] uc(final byte[] t) {
    if(ascii(t)) {
      final byte[] tok = new byte[t.length];
      for(int i = 0; i < t.length; i++) tok[i] = (byte) uc(t[i]);
      return tok;
    }
    return token(string(t).toUpperCase());
  }

  /**
   * Converts a character to upper case.
   * @param ch character to be converted
   * @return converted character
   */
  public static int uc(final int ch) {
    return ch >= 'a' && ch <= 'z' ? ch - 32 :
      ch > 0x7F ? Character.toUpperCase(ch) : ch;
  }

  /**
   * Converts the specified token to lower case.
   * @param t token to be converted
   * @return the converted token
   */
  public static byte[] lc(final byte[] t) {
    if(ascii(t)) {
      final byte[] tok = new byte[t.length];
      for(int i = 0; i < t.length; i++) tok[i] = (byte) lc(t[i]);
      return tok;
    }
    return token(string(t).toLowerCase());
  }

  /**
   * Converts a character to lower case.
   * @param ch character to be converted
   * @return converted character
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
  public static byte[] pref(final byte[] name) {
    final int i = indexOf(name, ':');
    return i == -1 ? EMPTY : substring(name, 0, i);
  }

  /**
   * Returns a md5 hash.
   * @param pw String
   * @return String
   */
  public static String md5(final String pw) {
    try {
      final MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(Token.token(pw));
      final TokenBuilder tb = new TokenBuilder();
      for(final byte b : md.digest()) {
        final int h = b >> 4 & 0x0F;
        tb.add((byte) (h + (h > 9 ? 0x57 : 0x30)));
        final int l = b & 0x0F;
        tb.add((byte) (l + (l > 9 ? 0x57 : 0x30)));
      }
      return tb.toString();
    } catch(final Exception ex) {
      return pw;
    }
  }

  /**
   * Returns a URI encoded token.
   * @param tok token
   * @param iri input
   * @return encoded token
   */
  public static byte[] uri(final byte[] tok, final boolean iri) {
    final int tl = tok.length;
    final TokenBuilder tb = new TokenBuilder();
    for(int t = 0; t < tl; t++) {
      final byte b = tok[t];
      if(letterOrDigit(b) || contains(iri ? IRIRES : RES, b)) tb.add(b);
      else hex(tb, b);
    }
    return tb.finish();
  }

  /**
   * Escapes the specified token.
   * @param tok token
   * @return escaped token
   */
  public static byte[] escape(final byte[] tok) {
    final int tl = tok.length;
    final TokenBuilder tb = new TokenBuilder();
    for(int t = 0; t < tl; t++) {
      final byte b = tok[t];
      if(b >= 32 && b <= 126) tb.add(b);
      else hex(tb, b);
    }
    return tb.finish();
  }

  /**
   * Adds the specified byte in hex code.
   * @param tb token builder
   * @param b byte to be added
   */
  private static void hex(final TokenBuilder tb, final byte b) {
    tb.add('%');
    tb.add(HEX[(b & 0xFF) >> 4]);
    tb.add(HEX[b & 0xFF & 15]);
  }

  /**
   * Returns the local name of the specified name.
   * @param name name
   * @return local name
   */
  public static byte[] ln(final byte[] name) {
    final int i = indexOf(name, ':');
    return i == -1 ? name : substring(name, i + 1);
  }

  /**
   * Returns a normalized character without diacritics.
   * This method supports all latin1 characters, including supplements.
   * @param ch character to be converted
   * @return normalized character
   */
  public static int norm(final int ch) {
    if(norm == null) initNorm();
    return ch < 0x80 ? ch : norm[ch];
  }

  /**
   * Initializes the array of normalized characters.
   */
  private static synchronized void initNorm() {
    norm = new char[1 << 16];
    for(int n = 0; n < norm.length; n++) norm[n] = (char) n;
    for(int n = 0; n < NC.length; n++) norm[NC[n][0]] = NC[n][1];
  }

  /** Normed characters. */
  private static char[] norm;

  /** Normalized special characters. */
  private static final char[][] NC = {
    { '\u00C0', 'A' }, { '\u00C1', 'A' }, { '\u00C2', 'A' }, { '\u00C3', 'A' },
    { '\u00C4', 'A' }, { '\u00C5', 'A' }, { '\u00C6', 'A' }, { '\u00C7', 'C' },
    { '\u00C8', 'E' }, { '\u00C9', 'E' }, { '\u00CA', 'E' }, { '\u00CB', 'E' },
    { '\u00CC', 'I' }, { '\u00CD', 'I' }, { '\u00CE', 'I' }, { '\u00CF', 'I' },
    { '\u00D0', 'D' }, { '\u00D1', 'N' }, { '\u00D2', 'O' }, { '\u00D3', 'O' },
    { '\u00D4', 'O' }, { '\u00D5', 'O' }, { '\u00D6', 'O' }, { '\u00D8', 'O' },
    { '\u00D9', 'U' }, { '\u00DA', 'U' }, { '\u00DB', 'U' }, { '\u00DC', 'U' },
    { '\u00DD', 'Y' }, { '\u00DE', 'd' }, { '\u00DF', 's' }, { '\u00E0', 'a' },
    { '\u00E1', 'a' }, { '\u00E2', 'a' }, { '\u00E3', 'a' }, { '\u00E4', 'a' },
    { '\u00E5', 'a' }, { '\u00E6', 'a' }, { '\u00E7', 'c' }, { '\u00E8', 'e' },
    { '\u00E9', 'e' }, { '\u00EA', 'e' }, { '\u00EB', 'e' }, { '\u00EC', 'i' },
    { '\u00ED', 'i' }, { '\u00EE', 'i' }, { '\u00EF', 'i' }, { '\u00F0', 'd' },
    { '\u00F1', 'n' }, { '\u00F2', 'o' }, { '\u00F3', 'o' }, { '\u00F4', 'o' },
    { '\u00F5', 'o' }, { '\u00F6', 'o' }, { '\u00F8', 'o' }, { '\u00F9', 'u' },
    { '\u00FA', 'u' }, { '\u00FB', 'u' }, { '\u00FC', 'u' }, { '\u00FD', 'y' },
    { '\u00FE', 'd' }, { '\u00FF', 'y' }, { '\u0100', 'A' }, { '\u0101', 'a' },
    { '\u0102', 'A' }, { '\u0103', 'a' }, { '\u0104', 'A' }, { '\u0105', 'a' },
    { '\u0106', 'C' }, { '\u0107', 'c' }, { '\u0108', 'C' }, { '\u0109', 'c' },
    { '\u010A', 'C' }, { '\u010B', 'c' }, { '\u010C', 'C' }, { '\u010D', 'c' },
    { '\u010E', 'D' }, { '\u010F', 'd' }, { '\u0110', 'D' }, { '\u0111', 'd' },
    { '\u0112', 'E' }, { '\u0113', 'e' }, { '\u0114', 'E' }, { '\u0115', 'e' },
    { '\u0116', 'E' }, { '\u0117', 'e' }, { '\u0118', 'E' }, { '\u0119', 'e' },
    { '\u011A', 'E' }, { '\u011B', 'e' }, { '\u011C', 'G' }, { '\u011D', 'g' },
    { '\u011E', 'G' }, { '\u011F', 'g' }, { '\u0120', 'G' }, { '\u0121', 'g' },
    { '\u0122', 'G' }, { '\u0123', 'g' }, { '\u0124', 'H' }, { '\u0125', 'h' },
    { '\u0126', 'H' }, { '\u0127', 'h' }, { '\u0128', 'I' }, { '\u0129', 'i' },
    { '\u012A', 'I' }, { '\u012B', 'i' }, { '\u012C', 'I' }, { '\u012D', 'i' },
    { '\u012E', 'I' }, { '\u012F', 'i' }, { '\u0130', 'I' }, { '\u0131', 'i' },
    { '\u0132', 'I' }, { '\u0133', 'i' }, { '\u0134', 'J' }, { '\u0135', 'j' },
    { '\u0136', 'K' }, { '\u0137', 'k' }, { '\u0138', 'k' }, { '\u0139', 'L' },
    { '\u013A', 'l' }, { '\u013B', 'L' }, { '\u013C', 'l' }, { '\u013D', 'L' },
    { '\u013E', 'l' }, { '\u013F', 'L' }, { '\u0140', 'l' }, { '\u0141', 'L' },
    { '\u0142', 'l' }, { '\u0143', 'N' }, { '\u0144', 'n' }, { '\u0145', 'N' },
    { '\u0146', 'n' }, { '\u0147', 'N' }, { '\u0148', 'n' }, { '\u0149', 'n' },
    { '\u014A', 'N' }, { '\u014B', 'n' }, { '\u014C', 'O' }, { '\u014D', 'o' },
    { '\u014E', 'O' }, { '\u014F', 'o' }, { '\u0150', 'O' }, { '\u0151', 'o' },
    { '\u0152', 'O' }, { '\u0153', 'o' }, { '\u0154', 'R' }, { '\u0155', 'r' },
    { '\u0156', 'R' }, { '\u0157', 'r' }, { '\u0158', 'R' }, { '\u0159', 'r' },
    { '\u015A', 'S' }, { '\u015B', 's' }, { '\u015C', 'S' }, { '\u015D', 's' },
    { '\u015E', 'S' }, { '\u015F', 's' }, { '\u0160', 'S' }, { '\u0161', 's' },
    { '\u0162', 'T' }, { '\u0163', 't' }, { '\u0164', 'T' }, { '\u0165', 't' },
    { '\u0166', 'T' }, { '\u0167', 't' }, { '\u0168', 'U' }, { '\u0169', 'u' },
    { '\u016A', 'U' }, { '\u016B', 'u' }, { '\u016C', 'U' }, { '\u016D', 'u' },
    { '\u016E', 'U' }, { '\u016F', 'u' }, { '\u0170', 'U' }, { '\u0171', 'u' },
    { '\u0172', 'U' }, { '\u0173', 'u' }, { '\u0174', 'W' }, { '\u0175', 'w' },
    { '\u0176', 'Y' }, { '\u0177', 'y' }, { '\u0178', 'Y' }, { '\u0179', 'Z' },
    { '\u017A', 'z' }, { '\u017B', 'Z' }, { '\u017C', 'z' }, { '\u017D', 'Z' },
    { '\u017E', 'z' }, { '\u01FA', 'A' }, { '\u01FB', 'a' }, { '\u01FC', 'A' },
    { '\u01FD', 'a' }, { '\u01FE', 'O' }, { '\u01FF', 'o' }
  };
}
