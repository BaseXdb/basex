package org.basex.util.ft;

import static org.basex.util.Token.*;
import org.basex.util.Util;

/**
 * Represents full-text token.
 * {@link #text} starts at [{@link #epos}. Tokens may overlap.
 * Right-left must not be equal to {@link #text}.length.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public final class FTSpan {
  /** Token position. */
  public int pos;
  /** Character end position. */
  public int epos;
  /** Special character flag. */
  public boolean special;
  /** Text. */
  public byte[] text;

  /**
   * Constructor.
   * @param t token text
   * @param e end position
   * @param p number of tokens parsed before the current token
   * @param sc is a special character
   */
  FTSpan(final byte[] t, final int e, final int p, final boolean sc) {
    text = t;
    epos = e;
    pos = p;
    special = sc;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + string(text) + ']';
  }
}
