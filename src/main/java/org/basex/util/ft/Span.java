package org.basex.util.ft;

/**
 * Represents full-text token.
 * {@link #txt} is out of interval [{@link #start}, {@link #end}). Tokens may
 * overlap. Right-left must not be equal to {@link #txt}.length.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public final class Span {
  /** Number of tokens parsed before the current token. */
  public final int pos;
  /** Left border in original text, including. */
  public final int start;
  /** Right border in original text, excluding. */
  public final int end;
  /** Is the current token a special character? */
  public final boolean specialChar;
  /** Text. */
  public byte[] txt;

  /**
   * Constructor.
   * @param t token text
   * @param s left border
   * @param e right border
   * @param p number of tokens parsed before the current token
   */
  Span(final byte[] t, final int s, final int e, final int p) {
    this(t, s, e, p, false);
  }

  /**
   * Constructor.
   * @param t token text
   * @param s left border
   * @param e right border
   * @param p number of tokens parsed before the current token
   * @param sc is a special character
   */
  Span(final byte[] t, final int s, final int e, final int p,
      final boolean sc) {
    txt = t;
    start = s;
    end = e;
    pos = p;
    specialChar = sc;
  }
}
