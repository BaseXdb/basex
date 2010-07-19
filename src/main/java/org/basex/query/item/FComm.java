package org.basex.query.item;

import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.util.Token;
import org.w3c.dom.Node;

/**
 * Comment node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FComm extends FNode {
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

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param node DOM node
   * @param parent parent reference
   */
  FComm(final Node node, final Nod parent) {
    this(Token.token(node.getNodeValue()), parent);
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
    return Main.info("<!--%-->", val);
  }
}
