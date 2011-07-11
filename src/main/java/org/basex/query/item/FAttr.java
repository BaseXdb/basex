package org.basex.query.item;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.util.Util;
import static org.basex.util.Token.*;
import org.w3c.dom.Attr;

/**
 * Attribute node fragment.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FAttr extends FNode {
  /** Attribute name. */
  private final QNm name;

  /**
   * Constructor, specifying a parent.
   * @param n name
   * @param v value
   * @param p parent
   */
  public FAttr(final QNm n, final byte[] v, final ANode p) {
    super(NodeType.ATT);
    name = n;
    val = v;
    par = p;
  }

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param attr DOM node
   * @param p parent reference
   */
  FAttr(final Attr attr, final ANode p) {
    this(new QNm(token(attr.getName())), token(attr.getValue()), p);
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] nname() {
    return name.atom();
  }

  @Override
  public FAttr copy() {
    return new FAttr(name, val, par);
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.attribute(name.atom(), val);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.atom(), VAL, val);
  }

  @Override
  public String toString() {
    return Util.info("%(%=\"%\")", name(), name.atom(), val);
  }
}
