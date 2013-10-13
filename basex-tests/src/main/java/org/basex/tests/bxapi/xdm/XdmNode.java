package org.basex.tests.bxapi.xdm;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Wrapper for representing an XQuery node.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
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
  public QName getName() {
    return node.qname().toJava();
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
    } catch(final QueryIOException ex) {
      throw Util.notexpected(ex);
    }
  }

  // PACKAGE PROTECTED METHODS ================================================

  @Override
  public ANode internal() {
    return node;
  }
}
