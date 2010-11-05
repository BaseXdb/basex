package org.basex.util.ft;

/**
 * Represents full-text token.
 * {@link #txt} starts at [{@link #start}. Tokens may overlap.
 * Right-left must not be equal to {@link #txt}.length.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public final class Span {
  /** Number of tokens parsed before the current token. */
  public int pos;
  /** Left border in original text, including. */
  public int start;
  /** Is the current token a special character? */
  public boolean special;
  /** Text. */
  public byte[] txt;

  /**
   * Constructor.
   * @param t token text
   * @param s left border
   * @param p number of tokens parsed before the current token
   * @param sc is a special character
   */
  Span(final byte[] t, final int s, final int p, final boolean sc) {
    txt = t;
    start = s;
    pos = p;
    special = sc;
  }
}
