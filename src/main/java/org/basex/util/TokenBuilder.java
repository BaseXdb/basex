package org.basex.util;

import static org.basex.util.Token.*;
import java.util.Arrays;

/**
 * This class serves as an efficient constructor for byte arrays.
 * It bears some resemblance to Java's {@link java.lang.StringBuilder}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class TokenBuilder {
  /** Highlighting flag. */
  public static final byte HIGH = 0x02;
  /** Standard flag. */
  public static final byte NORM = 0x03;
  /** new line. */
  public static final byte NL = 0x0a;
  /** Half new line. */
  public static final byte HL = 0x0b;

  /** Character array. */
  private byte[] chars;
  /** Entity flag. */
  public boolean ent;
  /** Current token size. */
  private int size;

  /**
   * Empty constructor.
   */
  public TokenBuilder() {
    this(ElementList.CAP);
  }

  /**
   * Constructor, specifying an initial array size.
   * @param i size
   */
  public TokenBuilder(final int i) {
    chars = new byte[i];
  }

  /**
   * Constructor, specifying an initial string.
   * @param str initial string
   */
  public TokenBuilder(final String str) {
    this(token(str));
  }

  /**
   * Constructor, specifying an initial array.
   * @param str initial string
   */
  public TokenBuilder(final byte[] str) {
    chars = str;
    size = str.length;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Resets the token buffer.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Adds a highlight flag.
   * This flag is evaluated by the text renderer in the frontend.
   * @return self reference
   */
  public TokenBuilder high() {
    return addByte(HIGH);
  }

  /**
   * Adds a norm flag.
   * This flag is evaluated by the text renderer in the frontend.
   * @return self reference
   */
  public TokenBuilder norm() {
    return addByte(NORM);
  }

  /**
   * Adds a new line.
   * This flag is evaluated by the text renderer in the frontend.
   * @return self reference
   */
  public TokenBuilder nl() {
    return addByte(NL);
  }

  /**
   * Adds a half new line.
   * This flag is evaluated by the text renderer in the frontend.
   * @return self reference
   */
  public TokenBuilder hl() {
    return addByte(HL);
  }

  /**
   * Adds the specified UTF8 character.
   * @param ch the character to be added
   * @return self reference
   */
  public TokenBuilder add(final int ch) {
    if(ch <= 0x7F) {
      addByte((byte) ch);
    } else if(ch <= 0x7FF) {
      addByte((byte) (ch >>  6 & 0x1F | 0xC0));
      addByte((byte) (ch >>  0 & 0x3F | 0x80));
    } else if(ch <= 0xFFFF) {
      addByte((byte) (ch >> 12 & 0x0F | 0xE0));
      addByte((byte) (ch >>  6 & 0x3F | 0x80));
      addByte((byte) (ch >>  0 & 0x3F | 0x80));
    } else {
      addByte((byte) (ch >> 18 & 0x07 | 0xF0));
      addByte((byte) (ch >> 12 & 0x3F | 0x80));
      addByte((byte) (ch >>  6 & 0x3F | 0x80));
      addByte((byte) (ch >>  0 & 0x3F | 0x80));
    }
    return this;
  }

  /**
   * Adds a byte to the token.
   * @param b the byte to be added
   * @return self reference
   */
  public TokenBuilder addByte(final byte b) {
    if(size == chars.length) chars = Arrays.copyOf(chars, Array.newSize(size));
    chars[size++] = b;
    return this;
  }

  /**
   * Adds a number to the token.
   * @param i the integer to be added
   * @return self reference
   */
  public TokenBuilder addNum(final long i) {
    return add(token(i));
  }

  /**
   * Adds a byte array to the token.
   * @param b the character array to be added
   * @return self reference
   */
  public TokenBuilder add(final byte[] b) {
    return add(b, 0, b.length);
  }

  /**
   * Adds a partial byte array to the token.
   * @param b the character array to be added
   * @param s start position
   * @param e end position
   * @return self reference
   */
  public TokenBuilder add(final byte[] b, final int s, final int e) {
    final int l = e - s;
    final int cl = chars.length;
    if(size + l > cl) {
      final int ns = Math.max(size + l, (int) (cl * Array.RESIZE));
      chars = Arrays.copyOf(chars, ns);
    }
    System.arraycopy(b, s, chars, size, l);
    size += l;
    return this;
  }

  /**
   * Adds a string to the token.
   * @param s the string to be added
   * @return self reference
   */
  public TokenBuilder add(final String s) {
    return add(token(s));
  }

  /**
   * Adds multiple strings to the token, separated by the specified string.
   * @param s the string to be added
   * @param sep separator
   * @return self reference
   */
  public TokenBuilder addSep(final Object[] s, final String sep) {
    for(int e = 0; e != s.length; ++e) {
      if(e != 0) add(sep);
      addExt(s[e]);
    }
    return this;
  }


  /**
   * Replaces all % characters in the input string by the specified extension
   * objects, which can be byte arrays or any other object.
   * If a digit is found after %, it is interpreted as insertion position.
   * @param str string to be extended
   * @param ext extensions
   * @return self reference
   */
  public TokenBuilder addExt(final Object str, final Object... ext) {
    final byte[] t = str instanceof byte[] ? (byte[]) str :
      token(str == null ? "null" : str.toString());

    for(int i = 0, e = 0; i < t.length; ++i) {
      if(t[i] != '%' || e == ext.length) {
        addByte(t[i]);
      } else {
        final byte c = i + 1 < t.length ? t[i + 1] : 0;
        final boolean d = c >= '1' && c <= '9';
        if(d) ++i;
        final int n = d ? c - '1' : e++;
        final Object o = n < ext.length ? ext[n] : null;
        addExt(o instanceof byte[] ? (byte[]) o :
          o == null ? null : o.toString());
      }
    }
    return this;
  }

  /**
   * Chops leading and trailing whitespaces.
   */
  public void chop() {
    while(size > 0 && ws(chars[size - 1])) --size;
    int s = -1;
    while(++s < size && ws(chars[s]));
    if(s != 0 && s != size) Array.move(chars, s, -s, size - s);
    size -= s;
  }

  /**
   * Returns true if the token only contains whitespaces.
   * @return result of check
   */
  public boolean wsp() {
    for(int i = 0; i < size; ++i) if(!ws(chars[i])) return false;
    return true;
  }

  /**
   * Returns the token as byte array.
   * @return character array
   */
  public byte[] finish() {
    return Arrays.copyOf(chars, size);
  }

  @Override
  public String toString() {
    return string(chars, 0, size);
  }
}
