package org.basex.util;

import static org.basex.util.Token.*;

import java.util.*;

/**
 * This class serves as an efficient constructor for {@link Token Tokens}.
 * It bears some resemblance to Java's {@link StringBuilder}.
 *
 * @author BaseX Team 2005-15, BSD License
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
  /** Underline flag. */
  public static final byte ULINE = 0x05;
  /** New line. */
  public static final byte NLINE = 0x0A;

  /** Byte array, storing all characters as UTF8. */
  private byte[] chars;
  /** Current token size. */
  private int size;

  /**
   * Empty constructor.
   */
  public TokenBuilder() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial internal array size.
   * @param capacity initial array capacity
   */
  public TokenBuilder(final int capacity) {
    chars = new byte[capacity];
  }

  /**
   * Constructor, specifying an initial string.
   * @param string initial string
   */
  public TokenBuilder(final String string) {
    this(Token.token(string));
  }

  /**
   * Constructor, specifying an initial token.
   * @param token initial token
   */
  public TokenBuilder(final byte[] token) {
    this(token.length + Array.CAPACITY);
    size = token.length;
    System.arraycopy(token, 0, chars, 0, size);
  }

  /**
   * Returns the number of bytes.
   * @return number of bytes
   */
  public int size() {
    return size;
  }

  /**
   * Sets the number of bytes. Note that no bound check is performed by this method.
   * @param sz number of bytes
   */
  public void size(final int sz) {
    size = sz;
  }

  /**
   * Tests if the token is empty.
   * @return result of check
   */
  public boolean isEmpty() {
    return size == 0;
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
   * Adds an underline toggle flag. This method should only be called to control text
   * rendering in the visual front end.
   * @return self reference
   */
  public TokenBuilder uline() {
    return addByte(ULINE);
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
   * Adds the specified UTF8 codepoint.
   * @param cp the codepoint to be added
   * @return self reference
   */
  public TokenBuilder add(final int cp) {
    if(cp <= 0x7F) {
      addByte((byte) cp);
    } else if(cp <= 0x7FF) {
      addByte((byte) (cp >>  6 & 0x1F | 0xC0));
      addByte((byte) (cp & 0x3F | 0x80));
    } else if(cp <= 0xFFFF) {
      addByte((byte) (cp >> 12 & 0x0F | 0xE0));
      addByte((byte) (cp >>  6 & 0x3F | 0x80));
      addByte((byte) (cp & 0x3F | 0x80));
    } else {
      addByte((byte) (cp >> 18 & 0x07 | 0xF0));
      addByte((byte) (cp >> 12 & 0x3F | 0x80));
      addByte((byte) (cp >>  6 & 0x3F | 0x80));
      addByte((byte) (cp & 0x3F | 0x80));
    }
    return this;
  }

  /**
   * Inserts the specified UTF8 character.
   * @param pos insertion position
   * @param cp the character to be added
   * @return self reference
   */
  public TokenBuilder insert(final int pos, final int cp) {
    final int s = size;
    final int cl = chars.length;
    final int l = cp <= 0x7F ? 1 : cp <= 0x7FF ? 2 : cp <= 0xFFF ? 3 : 4;
    if(s + l > cl) chars = Arrays.copyOf(chars, Math.max(s + l, (int) (cl * Array.RESIZE)));
    Array.move(chars, pos, l, size - pos);
    size = pos;
    add(cp);
    size = s + l;
    return this;
  }

  /**
   * Returns the codepoint stored at the specified position.
   * @param pos position
   * @return character
   */
  public int cp(final int pos) {
    return Token.cp(chars, pos);
  }

  /**
   * Returns the length of the codepoints stored at the specified position.
   * @param pos position
   * @return character
   */
  public int cl(final int pos) {
    return Token.cl(chars, pos);
  }

  /**
   * Returns the byte stored at the specified position.
   * @param pos position
   * @return byte
   */
  public byte get(final int pos) {
    return chars[pos];
  }

  /**
   * Sets a byte at the specified position.
   * @param value byte to be set
   * @param pos position
   */
  public void set(final int pos, final byte value) {
    chars[pos] = value;
  }

  /**
   * Deletes bytes from the token.
   * @param pos position
   * @param length number of bytes to be removed
   */
  public void delete(final int pos, final int length) {
    Array.move(chars, pos + length, -length, size - pos - length);
    size -= length;
  }

  /**
   * Adds a single byte to the token.
   * @param value the byte to be added
   * @return self reference
   */
  public TokenBuilder addByte(final byte value) {
    byte[] chrs = chars;
    final int s = size;
    if(s == chrs.length) chrs = Arrays.copyOf(chrs, Array.newSize(s));
    chrs[s] = value;
    chars = chrs;
    size = s + 1;
    return this;
  }

  /**
   * Adds an integer value to the token.
   * @param value value to be added
   * @return self reference
   */
  public TokenBuilder addInt(final int value) {
    return add(Token.token(value));
  }

  /**
   * Adds a number to the token.
   * @param value value to be added
   * @return self reference
   */
  public TokenBuilder addLong(final long value) {
    return add(Token.token(value));
  }

  /**
   * Adds a byte array to the token.
   * @param value the byte array to be added
   * @return self reference
   */
  public TokenBuilder add(final byte[] value) {
    return add(value, 0, value.length);
  }

  /**
   * Adds part of a byte array to the token.
   * @param value the byte array to be added
   * @param start start position
   * @param end end position
   * @return self reference
   */
  public TokenBuilder add(final byte[] value, final int start, final int end) {
    byte[] chrs = chars;
    final int cl = chrs.length, l = end - start, s = size, ns = s + l;
    if(ns > cl) chrs = Arrays.copyOf(chrs, Array.newSize(ns));
    System.arraycopy(value, start, chrs, s, l);
    chars = chrs;
    size = ns;
    return this;
  }

  /**
   * Adds a string to the token.
   * @param string the string to be added
   * @return self reference
   */
  public TokenBuilder add(final String string) {
    return add(Token.token(string));
  }

  /**
   * Adds multiple strings to the token, separated by the specified string.
   * @param objects the object to be added
   * @param sep separator string
   * @return self reference
   */
  public TokenBuilder addSep(final Object[] objects, final String sep) {
    final int ol = objects.length;
    for(int o = 0; o < ol; o++) {
      if(o != 0) add(sep);
      addExt(objects[o]);
    }
    return this;
  }

  /**
   * Adds the string representation of an object.
   * The specified string may contain {@code "%"} characters as place holders.
   * All place holders will be replaced by the specified extensions. If a digit is
   * specified after the place holder character, it will be interpreted as insertion
   * position.
   *
   * @param object object to be extended
   * @param ext optional extensions
   * @return self reference
   */
  public TokenBuilder addExt(final Object object, final Object... ext) {
    final byte[] t = token(object);
    final int tl = t.length, el = ext.length;
    for(int i = 0, e = 0; i < tl; ++i) {
      if(t[i] != '%' || e == el) {
        addByte(t[i]);
      } else {
        final byte c = i + 1 < tl ? t[i + 1] : 0;
        final boolean d = c >= '1' && c <= '9';
        if(d) ++i;
        final int n = d ? c - '1' : e++;
        final Object o = n < el ? ext[n] : null;
        addExt(o);
      }
    }
    return this;
  }

  /**
   * Returns a token representation of the specified object.
   * <ul>
   *   <li> byte arrays are returns as is.</li>
   *   <li> {@code null} references are replaced by the string "{@code null}".</li>
   *   <li> objects of type {@link Throwable} are converted to a string representation via
   *        {@link Util#message}.</li>
   *   <li> objects of type {@link Class} are converted via {@link Util#className(Class)}.</li>
   *   <li> for all other typer, {@link Object#toString} is called.</li>
   * </ul>
   * @param object object
   * @return token
   */
  public static byte[] token(final Object object) {
    if(object instanceof byte[]) return (byte[]) object;

    final String s;
    if(object == null) {
      s = "null";
    } else if(object instanceof Throwable) {
      s = Util.message((Throwable) object);
    } else if(object instanceof Class<?>) {
      s = Util.className((Class<?>) object);
    } else {
      s = object.toString();
    }
    return Token.token(s);
  }


  /**
   * Trims leading and trailing whitespaces.
   * @return self reference
   */
  public TokenBuilder trim() {
    final byte[] chrs = chars;
    int s = size;
    while(s > 0 && ws(chrs[s - 1])) --s;
    int c = -1;
    while(++c < s && ws(chrs[c]));
    if(c != 0 && c != s) Array.move(chrs, c, -c, s - c);
    size = s - c;
    return this;
  }

  /**
   * Returns the token as byte array.
   * @return token
   */
  public byte[] toArray() {
    final int s = size;
    return s == 0 ? EMPTY : Arrays.copyOf(chars, s);
  }

  /**
   * Returns the token as byte array and resets the token buffer.
   * The call of this function is identical to calling {@link #toArray} and {@link #reset}.
   * @return token
   */
  public byte[] next() {
    final int s = size;
    if(s == 0) return EMPTY;
    size = 0;
    return Arrays.copyOf(chars, s);
  }

  /**
   * Returns the token as byte array, and invalidates the internal array.
   * Warning: the function must only be called if the builder is discarded afterwards.
   * @return token
   */
  public byte[] finish() {
    final byte[] chrs = chars;
    chars = null;
    final int s = size;
    return s == 0 ? EMPTY : s == chrs.length ? chrs : Arrays.copyOf(chrs, s);
  }

  @Override
  public String toString() {
    return string(chars, 0, size);
  }
}
