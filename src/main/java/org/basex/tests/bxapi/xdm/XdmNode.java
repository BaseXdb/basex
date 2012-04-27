package org.basex.tests.bxapi.xdm;

import java.io.*;

import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Wrapper for representing an XQuery node.
 */
final class XdmNode extends XdmItem {
  /** Wrapped node. */
  private final ANode node;

  /**
   * Constructor.
   * @param n node
   */
  XdmNode(final ANode n) {
    node = n;
  }

  @Override
  public String getBaseURI() {
    return Token.string(node.baseURI());
  }

  @Override
  public String getName() {
    return Token.string(node.name());
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
      return node.serialize().toString();
    } catch(final IOException ex) {
      throw Util.notexpected(ex.getMessage());
    }
  }

  // PACKAGE PROTECTED METHODS ================================================

  @Override
  public ANode internal() {
    return node;
  }
}
