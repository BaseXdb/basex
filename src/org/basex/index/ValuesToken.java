package org.basex.index;

/**
 * This class defines access to index text tokens.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ValuesToken extends IndexToken {
  /**
   * Constructor.
   * @param t index type
   * @param tok token
   */
  public ValuesToken(final boolean t, final byte[] tok) {
    super(t ? Type.TXT : Type.ATV);
    text = tok;
  }
}
