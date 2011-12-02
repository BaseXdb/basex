package org.basex.query.item;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
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
   * Default constructor.
   * @param n name
   * @param v value
   */
  public FAttr(final QNm n, final byte[] v) {
    super(NodeType.ATT);
    name = n;
    val = v;
  }

  /**
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param attr DOM node
   */
  public FAttr(final Attr attr) {
    this(new QNm(token(attr.getName())), token(attr.getValue()));
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] name() {
    return name.string();
  }

  @Override
  public FNode copy() {
    return new FAttr(name, val).parent(par);
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.attribute(name.string(), val);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name.string(), VAL, val);
  }

  @Override
  public String toString() {
    return Util.info(" %=\"%\"", name.string(), val);
  }
}
