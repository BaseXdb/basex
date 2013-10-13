package org.basex.index.query;

import org.basex.index.*;

/**
 * This class defines access to index text tokens.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class StringToken implements IndexToken {
  /** Index type. */
  private final IndexType type;
  /** Text. */
  private final byte[] text;

  /**
   * Constructor.
   * @param it index type
   * @param tok token
   */
  public StringToken(final IndexType it, final byte[] tok) {
    type = it;
    text = tok;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] get() {
    return text;
  }
}
