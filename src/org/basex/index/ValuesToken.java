package org.basex.index;

import org.basex.data.Data.Type;
import org.basex.util.Token;

/**
 * This class defines access to index text tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ValuesToken implements IndexToken {
  /** Text. */
  private byte[] text = Token.EMPTY;
  /** Index type. */
  private final Type type;

  /**
   * Constructor.
   * @param t index type
   * @param tok token
   */
  public ValuesToken(final Type t, final byte[] tok) {
    type = t;
    text = tok;
  }

  public Type type() {
    return type;
  }

  public byte[] get() {
    return text;
  }
}
