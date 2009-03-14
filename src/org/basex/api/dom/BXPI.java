package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.ProcessingInstruction;

/**
 * DOM - ProcessingInstruction Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
    return Token.string(node.str());
  }

  @Override
  public String getNodeName() {
    return Token.string(node.nname());
  }

  public String getData() {
    return getNodeValue();
  }

  public String getTarget() {
    return getNodeName();
  }

  public void setData(final String dat) {
    error();
  }
}
