package org.basex.util;

import static org.basex.util.Token.*;
import java.util.Arrays;

import org.basex.util.list.ByteList;
import org.basex.util.list.ElementList;

/**
 * This class serves as an efficient constructor for byte arrays.
 * It bears some resemblance to Java's {@link java.lang.StringBuilder}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TokenBuilder {
  /** Half new line. */
  public static final byte HLINE = 0x01;
  /** Bold flag. */
  public static final byte BOLD = 0x02;
  /** Standard flag. */
  public static final byte NORM = 0x03;
  /** Mark flag. */
  public static final byte MARK = 0x04;
  /** New line. */
  public static final byte NLINE = 0x0a;

  /** Character array. */
  private byte[] chars;
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
   * Sets the number of entries.
   * @param s number of entries
   */
  public void size(final int s) {
    size = s;
  }

  /**
   * Resets the token buffer.
   * @return self reference
   */
  public TokenBuilder reset() {
    size = 0;
    return this;
  }

  /**
   * Adds a bold flag. This method should only be called to control text
   * rendering in the visual front end.
   * @return self reference
   */
  public TokenBuilder bold() {
    return addByte(BOLD);
  }

  /**
   * Adds a norm flag. This method should only be called to control text
   * rendering in the visual front end.
   * @return self reference
   */
  public TokenBuilder norm() {
    return addByte(NORM);
  }

  /**
   * Adds a new line. This method should only be called to control text
   * rendering in the visual front end.
   * @return self reference
   */
  public TokenBuilder nline() {
    return addByte(NLINE);
  }

  /**
   * Adds a half new line. This method should only be called to control text
   * rendering in the visual front end.
   * @return self reference
   */
  public TokenBuilder hline() {
    return addByte(HLINE);
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
   * Inserts the specified UTF8 character.
   * @param pos insertion index
   * @param ch the character to be added
   * @return self reference
   */
  public TokenBuilder insert(final int pos, final int ch) {
    final int s = size;
    final int cl = chars.length;
    final int l = ch <= 0x7F ? 1 : ch <= 0x7FF ? 2 : ch <= 0xFFF ? 3 : 4;

    if(s + l > cl) {
      final int ns = Math.max(s + l, (int) (cl * Array.RESIZE));
      chars = Arrays.copyOf(chars, ns);
    }
    Array.move(chars, pos, l, size - pos);
    size = pos;
    add(ch);
    size = s + l;
    return this;
  }

  /**
   * Returns the codepoint at the specified position.
   * @param p position
   * @return character
   */
  public int cp(final int p) {
    return Token.cp(chars, p);
  }

  /**
   * Returns the codepoint length of the specified byte.
   * @param p position
   * @return character
   */
  public int cl(final int p) {
    return Token.cl(chars, p);
  }

  /**
   * Returns the byte at the specified position.
   * @param p position
   * @return byte
   */
  public byte get(final int p) {
    return chars[p];
  }

  /**
   * Sets a byte at the specified position.
   * @param b byte to be set
   * @param p position
   */
  public void set(final byte b, final int p) {
    chars[p] = b;
  }

  /**
   * Deletes bytes from the token.
   * @param p position
   * @param s number of bytes to be removed
   */
  public void delete(final int p, final int s) {
    Array.move(chars, p + s, -s, size - p - s);
    size -= s;
  }

  /**
   * Adds a byte to the token. {@link ByteList} instances should be preferred
   * for the construction of pure byte arrays.
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
  public TokenBuilder addLong(final long i) {
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
   * Adds the string representation of an object to the token:
   * <ul>
   * <li> If extensions are specified, all characters {@code "%"} in the object
   *      string are replaced by the specified extensions.</li>
   * <li> Extensions can either be tokens (byte arrays) or any other objects,
   *      which will be represented as strings.</li>
   * <li> If a digit is found after the character {@code "%"}, it is interpreted
   *      as insertion position.</li>
   * </ul>
   * @param str string to be extended
   * @param ext optional extensions
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
   * Trims leading and trailing whitespaces.
   * @return self reference
   */
  public TokenBuilder trim() {
    while(size > 0 && ws(chars[size - 1])) --size;
    int s = -1;
    while(++s < size && ws(chars[s]));
    if(s != 0 && s != size) Array.move(chars, s, -s, size - s);
    size -= s;
    return this;
  }

  /**
   * Checks if the token only contains whitespaces.
   * @return true if all characters are whitespaces
   */
  public boolean whitespaces() {
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
