package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.util.Token;
import org.w3c.dom.Node;

/**
 * Text node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param node DOM node
   * @param parent parent reference
   */
  public FTxt(final Node node, final Nod parent) {
    this(Token.token(node.getNodeValue()), parent);
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
