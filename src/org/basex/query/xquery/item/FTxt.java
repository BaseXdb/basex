package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.util.Token;

/**
 * Text Node.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTxt extends FNode {
  /** Text value. */
  private final byte[] val;
  
  /**
   * Constructor.
   * @param t text value
   * @param p parent
   */
  public FTxt(final byte[] t, final Nod p) {
    super(Type.TXT);
    val = t;
    par = p;
  }

  @Override
  public byte[] str() {
    return val;
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.text(val);
  }

  @Override
  public FTxt copy() {
    return new FTxt(val, par);
  }

  @Override
  public String toString() {
    return Token.string(val);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, VAL, val);
  }
}
