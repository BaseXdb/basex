package org.basex.index;

import org.basex.data.Data.IndexType;

/**
 * This class defines access to index text tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ValuesToken implements IndexToken {
  /** Index type. */
  private final IndexType type;
  /** Text. */
  private final byte[] text;

  /**
   * Constructor.
   * @param t index type
   * @param tok token
   */
  public ValuesToken(final IndexType t, final byte[] tok) {
    type = t;
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
