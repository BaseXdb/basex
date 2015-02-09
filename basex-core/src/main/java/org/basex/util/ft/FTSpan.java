package org.basex.util.ft;

import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * This class contains a single full-text token.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Jens Erat
 */
public final class FTSpan {
  /** Text. */
  public byte[] text;
  /** Token position. */
  public final int pos;
  /** Delimiter flag. */
  public final boolean del;

  /**
   * Constructor.
   * @param text token text
   * @param pos number of tokens parsed before the current token
   * @param del the token contains delimiters
   */
  FTSpan(final byte[] text, final int pos, final boolean del) {
    this.text = text;
    this.pos = pos;
    this.del = del;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + string(text) + ']';
  }
}
