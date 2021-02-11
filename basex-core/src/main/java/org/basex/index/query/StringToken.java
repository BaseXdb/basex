package org.basex.index.query;

import org.basex.index.*;

/**
 * This class defines access to index text tokens.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringToken implements IndexSearch {
  /** Index type. */
  private final IndexType type;
  /** Index string. */
  private final byte[] value;

  /**
   * Constructor.
   * @param type index type
   * @param value value to be found
   */
  public StringToken(final IndexType type, final byte[] value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public IndexType type() {
    return type;
  }

  @Override
  public byte[] token() {
    return value;
  }
}
