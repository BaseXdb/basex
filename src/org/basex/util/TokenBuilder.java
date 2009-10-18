package org.basex.util;

import static org.basex.util.Token.*;

import java.util.Arrays;

/**
 * This class serves as an efficient constructor for byte arrays.
 * It bears some resemblance to Java's {@link java.lang.StringBuilder}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class TokenBuilder {
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
    this(8);
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
    add((byte) 0x02);
    return this;
  }

  /**
   * Adds a norm flag.
   * This flag is evaluated by the text renderer in the frontend.
   * @return self reference
   */
  public TokenBuilder norm() {
    add((byte) 0x03);
    return this;
  }

  /**
   * Adds a new line.
   * This flag is evaluated by the text renderer in the frontend.
   * @return self reference
   */
  public TokenBuilder nl() {
    add((byte) 0x0a);
    return this;
  }

  /**
   * Adds a half new line.
   * This flag is evaluated by the text renderer in the frontend.
   * @return self reference
   */
  public TokenBuilder hl() {
    add((byte) 0x0b);
    return this;
  }

  /**
   * Adds a single character to the token.
   * @param ch the character to be added
   * @return self reference
   */
  public TokenBuilder add(final char ch) {
    addUTF(ch);
    return this;
  }

  /**
   * Adds a single character to the token.
   * @param b the character to be added
   * @return self reference
   */
  public TokenBuilder add(final byte b) {
    if(size == chars.length) chars = Arrays.copyOf(chars, size << 1);
    chars[size++] = b;
    return this;
  }

  /**
   * Adds the specified UTF8 character.
   * @param c the character to be added
   */
  public void addUTF(final int c) {
    if(c <= 0x7F) {
      add((byte) c);
    } else if(c <= 0x7FF) {
      add((byte) (c >>  6 & 0x1F | 0xC0));
      add((byte) (c >>  0 & 0x3F | 0x80));
    } else if(c <= 0xFFFF) {
      add((byte) (c >> 12 & 0x0F | 0xE0));
      add((byte) (c >>  6 & 0x3F | 0x80));
      add((byte) (c >>  0 & 0x3F | 0x80));
    } else {
      add((byte) (c >> 18 & 0x07 | 0xF0));
      add((byte) (c >> 12 & 0x3F | 0x80));
      add((byte) (c >>  6 & 0x3F | 0x80));
      add((byte) (c >>  0 & 0x3F | 0x80));
    }
  }

  /**
   * Adds an integer to the token.
   * @param i the integer to be added
   * @return self reference
   */
  public TokenBuilder add(final int i) {
    add(token(i));
    return this;
  }

  /**
   * Adds a byte array to the token.
   * @param b the character array to be added
   * @return self reference
   */
  public TokenBuilder add(final byte[] b) {
    add(b, 0, b.length);
    return this;
  }

  /**
   * Adds a partial byte array to the token.
   * @param b the character array to be added
   * @param s start position
   * @param e end position
   */
  public void add(final byte[] b, final int s, final int e) {
    final int bs = e - s;
    if(size + bs > chars.length) {
      int ns = chars.length << 1;
      while(size + bs > ns) ns <<= 1;
      chars = Arrays.copyOf(chars, size + ns);
    }
    System.arraycopy(b, s, chars, size, bs);
    size += bs;
  }

  /**
   * Adds a string to the token.
   * @param s the string to be added
   * @return self reference
   */
  public TokenBuilder add(final String s) {
    add(token(s));
    return this;
  }

  /**
   * Replaces all % characters in the input string by the specified extension
   * objects, which can be byte arrays or any other object.
   * If a digit is found after %, it is interpreted as insertion position.
   * @param str query information
   * @param ext text text extensions
   * @return self reference
   */
  public TokenBuilder add(final Object str, final Object... ext) {
    final byte[] t = str instanceof byte[] ? (byte[]) str :
      token(str == null ? "null" : str.toString());

    for(int i = 0, e = 0; i < t.length; i++) {
      if(t[i] != '%' || e == ext.length) {
        add(t[i]);
      } else {
        final byte c = i + 1 < t.length ? t[i + 1] : 0;
        final boolean d = c >= '1' && c <= '9';
        if(d) i++;
        final int n = d ? c - '1' : e++;
        final Object o = n < ext.length ? ext[n] : null;
        add(o instanceof byte[] ? (byte[]) o : o == null ? null : o.toString());
      }
    }
    return this;
  }

  /**
   * Adds a string with the specified left indentation.
   * @param m maximum indentation
   * @param s string
   */
  public void add(final int m, final String s) {
    add(s);
    final int is = m - s.length();
    for(int i = 0; i < is; i++) add(' ');
  }

  /**
   * Chops leading and trailing whitespaces.
   */
  public void chop() {
    while(size > 0 && ws(chars[size - 1])) size--;
    int s = -1;
    while(++s < size && ws(chars[s]));
    if(s != 0 && s != size) Array.move(chars, s, -s, size - s);
    size -= s;
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
