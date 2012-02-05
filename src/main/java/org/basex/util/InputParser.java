package org.basex.util;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import org.basex.io.IO;

/**
 * Simple command and query parser; can be overwritten to support more complex
 * parsings.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class InputParser {
  /** Parsing exception. */
  private static final String FOUND = ", found '%'";

  /** Input query. */
  public final String query;
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
  protected InputParser(final String q) {
    this(q, null);
  }

  /**
   * Constructor.
   * @param q input query
   * @param f file
   */
  protected InputParser(final String q, final IO f) {
    query = q;
    ql = query.length();
    file = f;
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
    final int p = qp;
    return p < ql ? query.charAt(p) : 0;
  }

  /**
   * Checks if the current character equals the specified one.
   * @param ch character to be checked
   * @return result of check
   */
  protected final boolean curr(final int ch) {
    final int p = qp;
    return p < ql && ch == query.charAt(p);
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
    final int p = qp + 1;
    return p < ql ? query.charAt(p) : 0;
  }

  /**
   * Returns next character.
   * @return next character
   */
  protected final char consume() {
    return qp < ql ? query.charAt(qp++) : 0;
  }

  /**
   * Peeks forward and consumes the character if it equals the specified one.
   * @param ch character to consume
   * @return true if character was found
   */
  protected final boolean consume(final int ch) {
    final int p = qp;
    if(p >= ql || ch != query.charAt(p)) return false;
    ++qp;
    return true;
  }

  /**
   * Checks if the specified character is a quote.
   * @param ch character to be checked
   * @return result
   */
  protected static final boolean quote(final int ch) {
    return ch == '"' || ch == '\'';
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
    for(int s = 0; s < l; ++s) {
      if(query.charAt(p++) != str.charAt(s)) return false;
    }
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
    return query.substring(qp, e) + (e == ql ? "" : DOTS);
  }

  /**
   * Creates input information.
   * @return input information
   */
  protected final InputInfo input() {
    return new InputInfo(this);
  }
}
