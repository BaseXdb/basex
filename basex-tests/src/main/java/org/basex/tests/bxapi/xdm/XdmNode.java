package org.basex.tests.bxapi.xdm;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Wrapper for representing an XQuery node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class XdmNode extends XdmItem {
  /** Wrapped node. */
  private final ANode node;

  /**
   * Constructor.
   * @param node node
   */
  XdmNode(final ANode node) {
    this.node = node;
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
    return node.seqType();
  }

  @Override
  public ANode internal() {
    return node;
  }

  @Override
  public String toString() {
    try {
      return node.serialize().toString();
    } catch(final QueryIOException ex) {
      return node.toString();
    }
  }
}
