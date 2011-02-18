package org.basex.util.ft;

import static org.basex.util.Token.*;
import org.basex.util.Util;

/**
 * This class contains a single full-text token.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public final class FTSpan {
  /** Text. */
  public byte[] text;
  /** Token position. */
  public int pos;
  /** Special character flag. */
  public boolean special;

  /**
   * Constructor.
   * @param t token text
   * @param p number of tokens parsed before the current token
   * @param sc is a special character
   */
  FTSpan(final byte[] t, final int p, final boolean sc) {
    text = t;
    pos = p;
    special = sc;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + string(text) + ']';
  }
}
