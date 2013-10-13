package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Processing instruction implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class BXPI extends BXNode implements ProcessingInstruction {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXPI(final ANode n) {
    super(n);
  }

  @Override
  public String getNodeValue() {
    return Token.string(node.string());
  }

  @Override
  public String getNodeName() {
    return Token.string(node.name());
  }

  @Override
  public String getData() {
    return getNodeValue();
  }

  @Override
  public String getTarget() {
    return getNodeName();
  }

  @Override
  public void setData(final String dat) {
    throw readOnly();
  }
}
