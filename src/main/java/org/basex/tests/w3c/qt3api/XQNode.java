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
    return Token.string(node.baseURI());
  }

  @Override
  public String getName() {
    return Token.string(node.nname());
  }

  @Override
  public String getString() {
    return Token.string(node.string());
  }

  @Override
  public boolean getBoolean() {
    return node.bool(null);
  }

  @Override
  public SeqType getType() {
    return node.type();
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

  // PACKAGE PROTECTED METHODS ================================================

  @Override
  ANode internal() {
    return node;
  }
}
