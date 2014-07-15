package org.basex.index.query;

import org.basex.index.*;

/**
 * This class defines access to index text tokens.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class StringToken implements IndexToken {
  /** Index type. */
  private final IndexType type;
  /** Index string. */
  private final byte[] token;

  /**
   * Constructor.
   * @param text text index
   * @param token token
   */
  public StringToken(final boolean text, final byte[] token) {
    type = text ? IndexType.TEXT : IndexType.ATTRIBUTE;
    this.token = token;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] get() {
    return token;
  }
}
