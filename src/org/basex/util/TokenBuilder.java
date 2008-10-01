package org.basex.util;

import static org.basex.util.Token.*;

/**
 * This class serves as an efficient constructor for byte arrays.
 * It bears some resemblance to Java's {@link java.lang.StringBuilder}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TokenBuilder {
  /** Current token size. */
  public int size;
  /** Entity flag. */
  public boolean ent;
  /** Character array. */
  public byte[] chars = new byte[8];
  
  /**
   * Empty constructor.
   */
  public TokenBuilder() { }
  
  /**
   * Constructor, specifying an initial character.
   * @param ch first character
   */
  public TokenBuilder(final char ch) { 
    add(ch);
  }
  
  /**
   * Constructor, specifying an initial string.
   * @param str initial string
   */
  public TokenBuilder(final String str) { 
    add(str);
  }
  
  /**
   * Constructor, specifying an initial array.
   * @param str initial string
   */
  public TokenBuilder(final byte[] str) { 
    add(str);
  }
  
  /**
   * Resets the token buffer.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Adds a single character to the token.
   * @param ch the character to be added
   * @return instance
   */
  public TokenBuilder add(final char ch) {
    addUTF(ch);
    return this;
  }

  /**
   * Adds a single character to the token.
   * @param b the character to be added
   * @return instance
   */
  public TokenBuilder add(final byte b) {
    if(size == chars.length) chars = Array.extend(chars);
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
   * @return instance
   */
  public TokenBuilder add(final int i) {
    add(token(i));
    return this;
  }

  /**
   * Adds a byte array to the token.
   * @param b the character array to be added
   * @return instance
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
      chars = Array.resize(chars, size, size + ns);
    }
    System.arraycopy(b, s, chars, size, bs);
    size += bs;
  }

  /**
   * Adds a string to the token.
   * @param s the string to be added
   * @return instance
   */
  public TokenBuilder add(final String s) {
    add(token(s));
    return this;
  }

  /**
   * Adds a string and fills up with spaces.
   * @param s the string to be added
   * @param l maximum string length
   */
  public void add(final String s, final int l) {
    add(s);
    for(int i = 0; i < l - s.length(); i++) add(' ');
  }

  /**
   * Replaces all % characters in the input string by the specified extension
   * objects, which can be byte arrays or any other object.
   * If a digit is found after %, it is interpreted as insertion position.
   * @param str query information
   * @param ext text text extensions
   */
  public void add(final Object str, final Object... ext) {
    final byte[] t = str == null ? NULL : str instanceof byte[] ?
        (byte[]) str : token(str.toString());

    for(int i = 0, e = 0; i < t.length; i++) {
      if(t[i] != '%' || e == ext.length) {
        add(t[i]);
      } else {
        final byte c = i + 1 < t.length ? t[i + 1] : 0;
        final boolean d = c >= '1' && c <= '9';
        if(d) i++;
        final int n = d ? c - '1' : e++;
        final Object o = n < ext.length ? ext[n] : null;
        add(o instanceof byte[] ? (byte[]) o : o == null ? NULL : o.toString());
      }
    }
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
   * Returns the token as a byte array. Unused array bytes are chopped
   * before the token is returned.
   * @return character array
   */
  public byte[] finish() {
    return Array.finish(chars, size);
  }

  @Override
  public String toString() {
    return string(chars, 0, size);
  }
}
