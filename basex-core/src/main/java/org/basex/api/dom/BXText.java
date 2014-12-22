package org.basex.api.dom;

import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Text implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class BXText extends BXChar implements Text {
  /**
   * Constructor.
   * @param node node reference
   */
  BXText(final ANode node) {
    super(node);
  }

  @Override
  public String getWholeText() {
    return getNodeValue();
  }

  @Override
  public boolean isElementContentWhitespace() {
    return Token.ws(nd.string());
  }

  @Override
  public BXText replaceWholeText(final String value) {
    throw readOnly();
  }

  @Override
  public BXText splitText(final int offset) {
    throw readOnly();
  }
}
