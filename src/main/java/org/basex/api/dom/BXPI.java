package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.ProcessingInstruction;

/**
 * DOM - Processing instruction implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BXPI extends BXNode implements ProcessingInstruction {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXPI(final Nod n) {
    super(n);
  }

  @Override
  public String getNodeValue() {
    return Token.string(node.atom());
  }

  @Override
  public String getNodeName() {
    return Token.string(node.nname());
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
    error();
  }
}
