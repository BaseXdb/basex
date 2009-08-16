package org.basex.query.item;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;

/**
 * Comment node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  public FComm copy() {
    return new FComm(val, par);
  }

  @Override
  public String toString() {
    return BaseX.info("<!--%-->", val);
  }
}
