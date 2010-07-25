package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import static org.basex.util.Token.*;
import org.w3c.dom.ProcessingInstruction;

/**
 * PI node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FPI extends FNode {
  /** PI name. */
  private final QNm name;

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

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param pi DOM node
   * @param parent parent reference
   */
  FPI(final ProcessingInstruction pi, final Nod parent) {
    this(new QNm(token(pi.getTarget())), token(pi.getData()), parent);
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
    return Main.info("<?% %?>", name.str(), val);
  }
}
