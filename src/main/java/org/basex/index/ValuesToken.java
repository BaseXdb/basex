package org.basex.index;

/**
 * This class defines access to index text tokens.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ValuesToken implements IndexToken {
  /** Index type. */
  private final IndexType ind;
  /** Text. */
  private final byte[] text;

  /**
   * Constructor.
   * @param i index type
   * @param tok token
   */
  public ValuesToken(final IndexType i, final byte[] tok) {
    ind = i;
    text = tok;
  }

  @Override
  public IndexType type() {
    return ind;
  }

  @Override
  public byte[] get() {
    return text;
  }
}
