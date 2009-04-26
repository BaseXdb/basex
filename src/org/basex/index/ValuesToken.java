package org.basex.index;

import org.basex.data.Data.Type;
import org.basex.util.Token;

/**
 * This class defines access to index text tokens.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ValuesToken extends IndexToken {
  /** Text. */
  private byte[] text = Token.EMPTY;

  /**
   * Constructor.
   * @param t index type
   * @param tok token
   */
  public ValuesToken(final boolean t, final byte[] tok) {
    super(t ? Type.TXT : Type.ATV);
    text = tok;
  }

  @Override
  public byte[] get() {
    return text;
  }
}
