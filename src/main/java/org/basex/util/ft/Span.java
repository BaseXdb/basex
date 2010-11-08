package org.basex.util.ft;

import static org.basex.util.Token.*;
import org.basex.util.Util;

/**
 * Represents full-text token.
 * {@link #text} starts at [{@link #cpos}. Tokens may overlap.
 * Right-left must not be equal to {@link #text}.length.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public final class Span {
  /** Number of tokens parsed before the current token. */
  public int pos;
  /** Character position in original text. */
  public int cpos;
  /** Is the current token a special character? */
  public boolean special;
  /** Text. */
  public byte[] text;

  /**
   * Constructor.
   * @param t token text
   * @param c character position
   * @param p number of tokens parsed before the current token
   * @param sc is a special character
   */
  Span(final byte[] t, final int c, final int p, final boolean sc) {
    text = t;
    cpos = c;
    pos = p;
    special = sc;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + string(text) + ']';
  }
}
