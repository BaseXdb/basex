package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;

/**
 * PI node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FPI extends FNode {
  /** PI name. */
  public final QNm name;
  /** PI value. */
  private final byte[] val;

  /**
   * Constructor.
   * @param n name
   * @param v value
   * @param p parent
   */
  public FPI(final QNm n, final byte[] v, final Nod p) {
    super(Type.PI);
    name = n;
    val = v;
    par = p;
  }

  @Override
  public byte[] str() {
    return val;
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] nname() {
    return name.str();
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.pi(name.str(), val);
  }

  @Override
  public FPI copy() {
    return new FPI(name, val, par);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.str(), VAL, val);
  }

  @Override
  public String toString() {
    return BaseX.info("<?% %?>", name.str(), val);
  }
}
