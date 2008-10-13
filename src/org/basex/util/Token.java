package org.basex.util;

/**
 * This class provides convenience operations for handling so-called
 * 'Tokens'. Tokens in BaseX are nothing else than UTF8 encoded strings,
 * stored in a byte array.
 *
 * Note that, to guarantee a consistent string representation, all string
 * conversions should be done via the methods of this class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Token {
  /** Maximum length for hash calculation and index terms. */
  public static final int MAXLEN = 64;

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
  /** Infinity. */
  public static final byte[] NAN = token("NaN");
  /** Infinity. */
  public static final byte[] INF = token("INF");
  /** Infinity. */
  public static final byte[] NINF = token("-INF");
  /** Dots. */
  public static final byte[] NULL = token("null");
  /** Empty token. */
  public static final byte[] EMPTY = {};
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

  /** UTF8 encoding string. */
  public static final String UTF8 = "UTF-8";
  /** UTF8 encoding string (variant). */
  public static final String UTF82 = "UTF8";
  /** UTF16 encoding string. */
  public static final String UTF16LE = "UTF-16LE";
  /** UTF16 encoding string. */
  public static final String UTF16BE = "UTF-16BE";

  /** Hidden Constructor. */
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
   * @return result of check
   */
  private static String utf8(final byte[] text, final int s, final int l) {
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
    final int l = text.length;
    for(int i = 0; i < l; i++) if(text[i] < 0) return false;
    return true;
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
    } catch(final Exception e) {
      e.printStackTrace();
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
    // no UTF8 (string constant..) & no special characters: return input string
    if(enc == UTF8 || ascii(s)) return s;
    // convert to utf8
    try {
      return new String(s, enc).getBytes(UTF8);
    } catch(final Exception e) {
      org.basex.BaseX.notexpected(e.getMessage());
      return EMPTY;
    }
  }

  /**
   * Returns the codepoint of the specified bytes, starting at the
   * specified position.
   * @param b byte array
   * @param i character position
   * @return current character
   */
  public static int cp(final byte[] b, final int i) {
    // calculate UTF8 character.
    int l = i;
    final int v = b[l] & 0xFF;
    final int c = v >> 4;

    // 0xxxxxxx
    if(c < 12) return v;
    // 110xxxxx 10xxxxxx
    if(c == 12 || c == 13)
      return (v & 0x1F) << 6 | (b[++l] & 0x3F);
    // 1110xxxx 10xxxxxx 10xxxxxx
    if(c == 14) return (v & 0x0F) << 12 |
      (b[++l] & 0x3F) << 6 | (b[++l] & 0x3F);
    // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
    return (v & 0x07) << 18 | (b[++l] & 0x3F) << 12 |
      (b[++l] & 0x3F) << 6 | (b[++l] & 0x3F);
  }

  /**
   * Returns the expected codepoint length of the specified byte.
   * @param v first character byte
   * @return character length
   */
  public static int cl(final byte v) {
    return CHLEN[(v & 0xFF) >> 4];
  }

  /*** Character lengths. */
  private static final int[] CHLEN = {
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4
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
      final int q = (n * 52429) >>> 19;
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
  public static final java.text.DecimalFormatSymbols LOC =
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

    // not that brilliant here.. no chance for elegant code either,
    // so let's see first how often this is used
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
    for(int t : to) {
      if(t >= 0 && t <= ' ' || digit(t)) continue;
      f = t == 'e' || t == 'E' || t == '.';
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
    } catch(final Exception e) {
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
   * positive if first token is bigger
   */
  public static int diff(final byte[] tok, final byte[] tok2) {
    final int l = Math.min(tok.length, tok2.length);
    for(int i = 0; i != l; i++) {
      if(tok[i] != tok2[i]) return (tok[i] & 0xFF) - (tok2[i] & 0xFF);
    }
    return tok.length - tok2.length;
  }

  /**
   * Checks if the first token contains the second token in lowercase.
   * @param tok first token
   * @param sub second token
   * @return result of test
   */
  public static boolean containslc(final byte[] tok, final byte[] sub) {
    final int sl = sub.length;
    final int tl = tok.length;
    // matching token is bigger than query token..
    if(sl > tl) return false;
    if(sl == 0) return true;

    // compare tokens character wise
    for(int t = 0; t <= tl - sl; t++) {
      int s = -1;
      while(++s != sl && lc(sub[s]) == lc(tok[t + s]));
      if(s == sl) return true;
    }
    return false;
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
   * Returns a subtoken of the specified token.
   * @param tok token
   * @param s start position
   * @return subtoken
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
    return s >= e ? EMPTY : Array.create(tok, s, e - s);
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
        if(sb.size != 0) {
          split[s++] = sb.finish();
          sb.reset();
        }
      } else {
        sb.add(c);
      }
    }
    if(sb.size != 0) split[s++] = sb.finish();
    return Array.finish(split, s);
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
   * @param t token to be checked
   * @return chopped array
   */
  public static byte[] trim(final byte[] t) {
    int s = -1;
    int e = t.length;
    while(++s < e) if(t[s] > ' ' || t[s] < 0) break;
    while(--e > s) if(t[e] > ' ' || t[e] < 0) break;
    if(++e == t.length && s == 0) return t;
    return s == e ? EMPTY : Array.create(t, s, e - s);
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
      Array.copy(tt, tmp, l);
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
    int e = t.length;
    for(int r = t.length - 1; r >= 0; r--)
      if(t[r] == c && r < --e) Array.move(t, r + 1, -1, e - r);
    return Array.finish(t, e);
  }

  /**
   * Deletes the specified characters out of the token.
   * @param t token to be checked
   * @param c characters to be removed
   * @return new instance
   */
  public static byte[] delete(final byte[] t, final byte[] c) {
    final int cl = c.length;
    byte[] res = t;
    int i;
    while((i = indexOf(res, c)) != -1) {
      final int rl = res.length;
      final byte[] tmp = new byte[rl - cl];
      System.arraycopy(res, 0, tmp, 0, i);
      System.arraycopy(res, i + cl, tmp, i, rl - cl - i);
      res = tmp;
    }
    return res;
  }

  /**
   * Normalizes all whitespace occurrences from the specified token.
   * @param tok token
   * @return normalized token.
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
    return c == l ? tmp : Array.finish(tmp, c);
  }

  /**
   * Performs a translation on the specified token.
   * @param tok token
   * @param srch characters to be found
   * @param rep characters to be replaced
   * @return translated token.
   */
  public static byte[] translate(final byte[] tok, final byte[] srch,
      final byte[] rep) {
    final int l = tok.length;
    final byte[] tmp = new byte[l];
    int c = 0;
    for(int i = 0; i < l; i++) {
      final byte b = tok[i];
      int j = -1;
      while(++j < srch.length && b != srch[j]);
      if(j < srch.length) {
        if(j >= rep.length) continue;
        tmp[c++] = rep[j];
      } else {
        tmp[c++] = tok[i];
      }
    }
    return c == l ? tmp : Array.finish(tmp, c);
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
   * Checks if the specified character is a letter.
   * Note that this method does not support unicode characters.
   * @param c the letter to be checked
   * @return result of comparison
   */
  public static boolean letter(final int c) {
    return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '_';
  }

  /**
   * Checks if the specified character is a digit.
   * @param c the letter to be checked
   * @return result of comparison
   */
  public static boolean digit(final int c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Checks if the specified character is a letter or digit.
   * Note that this method does not support unicode characters.
   * @param c the letter to be checked
   * @return result of comparison
   */
  public static boolean letterOrDigit(final int c) {
    return letter(c) || digit(c);
  }

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
   * Note that this method does not support unicode characters.
   * 
   * @param ch character to be converted
   * @return converted character
   */
  public static int uc(final int ch) {
    return ch < 'a' || ch > 'z' ? ch : ch - 32;
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
   * Note that this method does not support unicode characters.
   * 
   * @param ch character to be converted
   * @return converted character
   */
  public static int lc(final int ch) {
    return ch < 'A' || ch > 'Z' ? ch : ch | 0x20;
  }

  /**
   * Checks if the specified character is a letter; special characters are
   * converted to the standard ASCII charset.
   * Note that this method does not support unicode characters.
   * @param ch character to be converted
   * @return converted character
   */
  public static boolean ftChar(final byte ch) {
    return letterOrDigit(ft(ch));
  }
  
  /**
   * Returns a lowercase ASCII character of the specified fulltext character.
   * Note that this method does not support unicode characters.
   * @param ch character to be converted
   * @return converted character
   */
  public static int ftNorm(final int ch) {
    return lc(ft(ch));
  }

  /**
   * Returns a fulltext character of the specified character.
   * Note that this method does not support unicode characters.
   * @param ch character to be converted
   * @return converted character
   */
  private static int ft(final int ch) {
    return ch < 0 && ch > -64 ? NORM[ch + 64] : ch;
  }

  /**
   * Removes diacritics from the specified token.
   * Note that this method does only support ISO-8859-1.
   * @param t token to be converted
   * @return converted token
   */
  public static byte[] dc(final byte[] t) {
    if(ascii(t)) return t;

    final String s = utf8(t, 0, t.length);
    final StringBuilder sb = new StringBuilder();
    for(int j = 0; j < s.length(); j++) {
      final char c = s.charAt(j);
      sb.append(c < 192 || c > 255 ? c : (char) NORM[c - 192]);
    }
    return token(sb.toString());
  }
  
  /**
   * Returns the prefix of the specified token.
   * @param name name
   * @return prefix or empty token if no prefix exists
   */
  public static byte[] pre(final byte[] name) {
    final int i = indexOf(name, ':');
    return i == -1 ? EMPTY : substring(name, 0, i);
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
   * Normalize special characters.
   * To be extended for UTF8 support.
   */
  private static final byte[] NORM = { 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'C',
    'E', 'E', 'E', 'E', 'I', 'I', 'I', 'I', 'D', 'N', 'O', 'O', 'O', 'O',
    'O', ' ', 'O', 'U', 'U', 'U', 'U', 'Y', 'D', 'S', 'a', 'a', 'a', 'a', 'a',
    'a', 'a', 'c', 'e', 'e', 'e', 'e', 'i', 'i', 'i', 'i', 'd', 'n', 'o',
    'o', 'o', 'o', 'o', ' ', 'o', 'u', 'u', 'u', 'u', 'y', 'd', 's' };
}
