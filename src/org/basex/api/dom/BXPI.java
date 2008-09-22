package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.query.xquery.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.ProcessingInstruction;

/**
 * DOM - ProcessingInstruction Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    return "pi";
  }

  public String getData() {
    return getNodeValue();
  }

  public String getTarget() {
    return "pi";
  }

  public void setData(final String dat) {
    BaseX.notimplemented();
  }
}
