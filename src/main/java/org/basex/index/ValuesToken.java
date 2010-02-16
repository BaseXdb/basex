package org.basex.index;

import org.basex.data.Data.IndexType;
import org.basex.util.Token;

/**
 * This class defines access to index text tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ValuesToken implements IndexToken {
  /** Text. */
  private byte[] text = Token.EMPTY;
  /** Index type. */
  private final IndexType type;

  /**
   * Constructor.
   * @param t index type
   * @param tok token
   */
  public ValuesToken(final IndexType t, final byte[] tok) {
    type = t;
    text = tok;
  }

  public IndexType type() {
    return type;
  }

  public byte[] get() {
    return text;
  }
}
