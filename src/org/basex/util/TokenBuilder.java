package org.basex.util;

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
   */
  public void add(final char ch) {
    add((byte) ch);
  }

  /**
   * Adds a single character to the token.
   * @param b the character to be added
   */
  public void add(final byte b) {
    if(size == chars.length) chars = Array.extend(chars);
    chars[size++] = b;
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
   * Inserts a single character at the beginning of the token.
   * Use it carefully; might get pretty slow.
   * @param b the character to be added
   */
  public void insert(final byte b) {
    if(size == chars.length) chars = Array.extend(chars);
    Array.move(chars, 0, 1, size);
    chars[0] = b;
    size++;
  }

  /**
   * Adds an integer to the token.
   * @param i the integer to be added
   */
  public void add(final int i) {
    add(Token.token(i));
  }

  /**
   * Adds a byte array to the token.
   * @param b the character array to be added
   */
  public void add(final byte[] b) {
    add(b, 0, b.length);
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
   */
  public void add(final String s) {
    add(Token.token(s));
  }

  /**
   * Adds some query information.
   * @param str query information
   * @param ext text text extensions
   */
  public void add(final Object str, final Object... ext) {
    int e = 0;
    for(final byte c : str instanceof byte[] ? (byte[]) str :
        Token.token(str.toString())) {
      if(e < ext.length && c == '%') {
        if(ext[e] instanceof byte[]) add((byte[]) ext[e++]);
        else if(ext[e] == null) add("null");
        else add(ext[e++].toString());
      } else {
        add(c);
      }
    }
  }
  
  /**
   * Chops trailing whitespaces.
   */
  public void trim() {
    while(size > 0 && Token.ws(chars[size - 1])) size--;
  }
  
  /**
   * Chops trailing whitespaces.
   */
  public void chop() {
    trim();
    int s = -1;
    while(++s < size && Token.ws(chars[s]));
    if(s != 0 && s != size) Array.move(chars, s, -s, size - s);
    size -= s;
  }

  /**
   * Replaces a character at the specified position.
   * @param b the character to be set
   * @param pos position
   */
  public void replace(final byte b, final int pos) {
    chars[pos] = b;
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
    return Token.string(chars, 0, size);
  }
}
