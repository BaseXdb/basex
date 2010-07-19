package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.query.iter.NodIter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Document node fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FDoc extends FNode {
  /** Base URI. */
  private final byte[] base;

  /**
   * Constructor.
   * @param ch children
   * @param b base uri
   */
  public FDoc(final NodIter ch, final byte[] b) {
    super(Type.DOC);
    children = ch;
    base = b;
  }

  /**
   * Constructor for DOM nodes (partial).
   * Provided by Erdal Karaca.
   * @param node DOM node
   * @param b base uri
   */
  FDoc(final Node node, final byte[] b) {
    this(new NodIter(), b);
    final NodeList nl = node.getChildNodes();
    if(nl.getLength() != 0) children.add(new FElem(nl.item(0), this));
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < children.size(); c++) children.get(c).serialize(ser);
  }

  @Override
  public byte[] base() {
    return base;
  }

  @Override
  public FDoc copy() {
    return new FDoc(children, base);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, BASE, base);
  }

  @Override
  public String toString() {
    return Main.info("%(%)", name(), base);
  }
}
