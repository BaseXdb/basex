package org.basex.tests.w3c.qt3api;

import java.io.IOException;

import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.query.item.ANode;
import org.basex.query.item.SeqType;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Wrapper for representing an XQuery node.
 */
final class XQNode extends XQItem {
  /** Wrapped node. */
  final ANode node;

  /**
   * Constructor.
   * @param n node
   */
  XQNode(final ANode n) {
    node = n;
  }

  @Override
  public String getBaseURI() {
    return Token.string(node.base());
  }

  @Override
  public String getName() {
    return Token.string(node.nname());
  }

  @Override
  public String getString() {
    return Token.string(node.atom());
  }

  @Override
  public SeqType getType() {
    return node.type();
  }

  /**
   * Returns the Java representation.
   * @return node name
   */
  public Object getJava() {
    return node.toJava();
  }

  @Override
  public boolean getBoolean() {
    return node.bool(null);
  }

  @Override
  public ANode internal() {
    return node;
  }

  @Override
  public String toString() {
    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = Serializer.get(ao);
      node.serialize(ser);
      ser.close();
      return ao.toString();
    } catch(final IOException ex) {
      throw Util.notexpected(ex.getMessage());
    }
  }
}
