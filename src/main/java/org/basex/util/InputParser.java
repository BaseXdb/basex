package org.basex.util;

import static org.basex.util.Token.*;
import org.basex.io.IO;

/**
 * Simple command and query parser; can be overwritten to support more complex
 * parsings.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class InputParser {
  /** Parsing exception. */
  private static final String FOUND = ", found \"%\"";

  /** Input query. */
  public final String qu;
  /** Query length. */
  public final int ql;
  /** Optional reference to query input. */
  public IO file;
  /** Current query position. */
  public int qp;
  /** Marked query position. */
  public int qm;

  /**
   * Constructor.
   * @param q input query
   */
  public InputParser(final String q) {
    qu = q;
    ql = qu.length();
  }

  /**
   * Checks if the input is valid.
   * @return -1 if everything's OK, position of codepoint otherwise
   */
  protected final int valid() {
    int cp;
    for(int p = 0; p < ql; p += Character.charCount(cp)) {
      cp = qu.codePointAt(p);
      if(!XMLToken.valid(cp)) return p;
    }
    return -1;
  }

  /**
   * Checks if more characters are found.
   * @return current character
   */
  protected final boolean more() {
    return qp < ql;
  }

  /**
   * Returns the current character.
   * @return current character
   */
  protected final char curr() {
    return qp >= ql ? 0 : qu.charAt(qp);
  }

  /**
   * Checks if the current character equals the specified one.
   * @param ch character to be checked
   * @return result of check
   */
  protected final boolean curr(final int ch) {
    return curr() == ch;
  }

  /**
   * Remembers the current position.
   */
  protected final void mark() {
    qm = qp;
  }

  /**
   * Returns the next character.
   * @return result of check
   */
  protected final char next() {
    return qp + 1 >= ql ? 0 : qu.charAt(qp + 1);
  }

  /**
   * Returns next character.
   * @return next character
   */
  protected final char consume() {
    return qp >= ql ? 0 : qu.charAt(qp++);
  }

  /**
   * Peeks forward and consumes the character if it equals the specified one.
   * @param ch character to consume
   * @return true if character was found
   */
  protected final boolean consume(final int ch) {
    final boolean found = curr() == ch;
    if(found) ++qp;
    return found;
  }

  /**
   * Checks if the specified character is a quote.
   * @param ch character to be checked
   * @return result
   */
  protected final boolean quote(final char ch) {
    return ch == '"' || ch == '\'';
  }

  /**
   * Consumes all whitespace characters from the beginning of the remaining
   * query.
   */
  protected final void consumeWS() {
    while(qp < ql) {
      final char ch = qu.charAt(qp);
      if(ch <= 0 || ch > ' ') break;
      ++qp;
    }
    qm = qp - 1;
  }

  /**
   * Peeks forward and consumes the string if it equals the specified one.
   * @param str string to consume
   * @return true if string was found
   */
  protected final boolean consume(final String str) {
    int p = qp;
    final int l = str.length();
    if(p + l > ql) return false;
    for(int s = 0; s < l; ++s) if(qu.charAt(p++) != str.charAt(s)) return false;
    qp = p;
    return true;
  }

  /**
   * Returns a "found" string, containing the current character.
   * @return completion
   */
  protected final byte[] found() {
    return curr() == 0 ? EMPTY : Util.inf(FOUND, curr());
  }

  /**
   * Returns the remaining, unscanned query substring.
   * @return query substring
   */
  protected final String rest() {
    final int e = Math.min(ql, qp + 15);
    return qu.substring(qp, e) + (e == ql ? "" : "...");
  }

  /**
   * Creates input information.
   * @return input information
   */
  public InputInfo input() {
    return new InputInfo(this);
  }
}
