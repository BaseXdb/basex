package org.basex.query.xquery.item;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.util.Token;

/**
 * Comment Node.
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
  public FComm(final byte[] t, final Node p) {
    super(Type.COM);
    val = t;
    par = p;
  }

  @Override
  public byte[] str() {
    return val;
  }
  
  @Override
  public void serialize(final Serializer ser,  final XQContext ctx,
      final int level) throws Exception {
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
