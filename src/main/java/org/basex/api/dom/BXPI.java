package org.basex.api.dom;

import org.basex.query.item.ANode;
import org.basex.util.Token;
import org.w3c.dom.ProcessingInstruction;

/**
 * DOM - Processing instruction implementation.
 *
 * @author BaseX Team 2005-11, BSD License
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
    readOnly();
  }
}
