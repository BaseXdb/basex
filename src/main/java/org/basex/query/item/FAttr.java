package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.util.Token;
import org.w3c.dom.Node;

/**
 * Attribute node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FAttr extends FNode {
  /** Attribute name. */
  private final QNm name;
  /** Attribute value. */
  private final byte[] val;

  /**
   * Constructor.
   * @param n name
   * @param v value
   * @param p parent
   */
  public FAttr(final QNm n, final byte[] v, final Nod p) {
    super(Type.ATT);
    name = n;
    val = v;
    par = p;
  }

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param node DOM node
   * @param p parent reference
   */
  FAttr(final Node node, final Nod p) {
    this(new QNm(Token.token(node.getNodeName())),
        Token.token(node.getNodeValue()), p);
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
  public FAttr copy() {
    return new FAttr(name, val, par);
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.attribute(name.str(), val);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.str(), VAL, val);
  }

  @Override
  public String toString() {
    return Main.info("%(%=\"%\")", name(), name.str(), val);
  }
}
