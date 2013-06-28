package org.basex.util;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.list.*;

/**
 * This class serves as an efficient constructor for {@link Token Tokens}.
 * It bears some resemblance to Java's {@link StringBuilder}.
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
    this(token(string));
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
   * @param s number of bytes
   */
  public void size(final int s) {
    size = s;
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

    if(s + l > cl) {
      final int ns = Math.max(s + l, (int) (cl * Array.RESIZE));
      chars = Arrays.copyOf(chars, ns);
    }
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
   * Adds a byte to the token. {@link ByteList} instances should be preferred
   * for the construction of pure byte arrays.
   * @param value the byte to be added
   * @return self reference
   */
  public TokenBuilder addByte(final byte value) {
    if(size == chars.length) chars = Arrays.copyOf(chars, Array.newSize(size));
    chars[size++] = value;
    return this;
  }

  /**
   * Adds an integer value to the token.
   * @param value value to be added
   * @return self reference
   */
  public TokenBuilder addInt(final int value) {
    return add(token(value));
  }

  /**
   * Adds a number to the token.
   * @param value value to be added
   * @return self reference
   */
  public TokenBuilder addLong(final long value) {
    return add(token(value));
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
    final int l = end - start;
    final int cl = chars.length;
    if(size + l > cl) {
      final int ns = Math.max(size + l, (int) (cl * Array.RESIZE));
      chars = Arrays.copyOf(chars, ns);
    }
    System.arraycopy(value, start, chars, size, l);
    size += l;
    return this;
  }

  /**
   * Adds a string to the token.
   * @param string the string to be added
   * @return self reference
   */
  public TokenBuilder add(final String string) {
    return add(token(string));
  }

  /**
   * Adds multiple strings to the token, separated by the specified string.
   * @param objects the object to be added
   * @param sep separator string
   * @return self reference
   */
  public TokenBuilder addSep(final Object[] objects, final String sep) {
    for(int e = 0; e != objects.length; ++e) {
      if(e != 0) add(sep);
      addExt(objects[e]);
    }
    return this;
  }

  /**
   * Adds the string representation of an object:
   * <ul>
   * <li> objects of type {@link Throwable} are converted to a string representation
   *      via {@link Util#message}.</li>
   * <li> objects of type {@link Class} are converted via {@link Util#name(Class)}.</li>
   * <li> {@code null} references are replaced by the string {@code "null"}.</li>
   * <li> byte arrays are directly inserted as tokens.</li>
   * <li> for all other typed, {@link Object#toString} is called.</li>
   * </ul>
   * The specified string may contain {@code "%"} characters as place holders.
   * All place holders will be replaced by the specified extensions. If a digit is
   * specified after the place holder character, it will be interpreted as insertion
   * position.
   *
   * @param object string to be extended
   * @param ext optional extensions
   * @return self reference
   */
  public TokenBuilder addExt(final Object object, final Object... ext) {
    final byte[] t;
    if(object instanceof byte[]) {
      t = (byte[]) object;
    } else {
      final String s;
      if(object == null) {
        s = "null";
      } else if(object instanceof Throwable) {
        s = Util.message((Throwable) object);
      } else if(object instanceof Class<?>) {
        s = Util.name((Class<?>) object);
      } else {
        s = object.toString();
      }
      t = token(s);
    }

    for(int i = 0, e = 0; i < t.length; ++i) {
      if(t[i] != '%' || e == ext.length) {
        addByte(t[i]);
      } else {
        final byte c = i + 1 < t.length ? t[i + 1] : 0;
        final boolean d = c >= '1' && c <= '9';
        if(d) ++i;
        final int n = d ? c - '1' : e++;
        final Object o = n < ext.length ? ext[n] : null;
        addExt(o);
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
