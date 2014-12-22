package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Processing instruction implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BXPI extends BXNode implements ProcessingInstruction {
  /**
   * Constructor.
   * @param node node reference
   */
  BXPI(final ANode node) {
    super(node);
  }

  @Override
  public String getNodeValue() {
    return Token.string(nd.string());
  }

  @Override
  public String getNodeName() {
    return Token.string(nd.name());
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
  public void setData(final String value) {
    throw readOnly();
  }
}
