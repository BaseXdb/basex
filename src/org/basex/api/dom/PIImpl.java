package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.util.Token;
import org.w3c.dom.ProcessingInstruction;

/**
 * DOM - ProcessingInstruction Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PIImpl extends NodeImpl implements ProcessingInstruction {
  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  public PIImpl(final Data d, final int p) {
    super(d, p, Data.PI);
  }
  
  @Override
  public String getNodeValue() {
    return Token.string(data.text(pre));
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
