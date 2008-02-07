package org.basex.util;

/**
 * This class allows the iteration on tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TokenIterator {
  /** Text array to be written. */
  private byte[] text = {};
  /** Text length. */
  private final int size;
  /** Current start position. */
  private int pos1;
  /** Current end position. */
  private int pos2;
  /** Current mark position. */
  private int posm;
  /** Current mark position. */
  private int pose;

  /**
   * Constructor.
   * @param t text
   */
  public TokenIterator(final byte[] t) {
    text = t;
    size = t.length;
  }

  /**
   * Initializes the iterator.
   */
  public void init() {
    pos2 = 0;
    posm = -1;
  }

  /**
   * Checks if the text has more words to print.
   * @return true if the text has more words
   */
  public boolean more() {
    // quit if text has ended or if the text panel is full
    if(pos2 >= size) return false;

    // parse next token boundaries; quit if text reference has been reset
    pos1 = pos2;

    // find next boundary
    if(sep(text[pos1])) ++pos2;
    else while(++pos2 < size && !sep(text[pos2]));
    return true;
  }

  /**
   * Returns the current character type.
   * @param c character to be checked
   * @return true for a delimiter character
   */
  boolean sep(final int c) {
    return c >= 0 && c <= ' ' || c == '<' || c == '>' || c == '"' || c == '\'';
  }

  /**
   * Check if the current token contains the specified token.
   * @param tok token to be checked
   * @return result of check
   */
  public boolean contains(final byte[] tok) {
    int i = -1;
    final int l = tok.length;
    if(l == 0 || l > pos2 - pos1) return false;
    while(++i < l) if(Token.lc(text[pos1 + i]) != tok[i]) return false;
    return true;
  }

  /**
   * Remembers the current position.
   */
  public void mark() {
    if(posm == -1) posm = pos1;
    pose = pos2;
  }

  /**
   * Returns the marked string.
   * @return marked string
   */
  public String marked() {
    return posm == -1 ? "" : Token.string(text, posm, pose - posm);
  }

  /**
   * Returns the specified character, starting from the current position.
   * @param i character position
   * @return current character
   */
  public byte get(final int i) {
    return i >= 0 || pos1 + i >= 0 ? text[pos1 + i] : 0;
  }

  /**
   * Returns the specified character.
   * @param i character position
   * @return current character
   */
  public int cp(final int i) {
    return Token.cp(text, pos1 + i);
  }

  /**
   * Returns the specified character length.
   * @param i character position
   * @return character length
   */
  public int len(final int i) {
    return Token.cl(text[pos1 + i]);
  }

  /**
   * Returns the size of the current character.
   * @return current size
   */
  public int size() {
    return pos2 - pos1;
  }

  /**
   * Returns the next token.
   * @return next token
   */
  public String next() {
    return Token.string(text, pos1, pos2 - pos1);
  }
}
