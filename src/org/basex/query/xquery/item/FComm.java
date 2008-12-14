package org.basex.query.xquery.item;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.util.Token;

/**
 * Comment Node Fragment.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FComm extends FNode {
  /** Text value. */
  private final byte[] val;
  
  /**
   * Constructor.
   * @param t text value
   * @param p parent
   */
  public FComm(final byte[] t, final Nod p) {
    super(Type.COM);
    val = t;
    par = p;
  }

  @Override
  public byte[] str() {
    return val;
  }
  
  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.comment(val);
  }

  @Override
  public String toString() {
    return "<!--" + Token.string(val) + "-->";
  }
  
  @Override
  public FComm copy() {
    return new FComm(val, par);
  }
}
