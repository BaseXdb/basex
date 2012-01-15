package org.basex.api.dom;

import org.basex.query.item.ANode;
import org.basex.util.Token;
import org.w3c.dom.Text;

/**
 * DOM - Text implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BXText extends BXChar implements Text {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXText(final ANode n) {
    super(n);
  }

  @Override
  public String getWholeText() {
    return getNodeValue();
  }

  @Override
  public boolean isElementContentWhitespace() {
    return Token.ws(node.string());
  }

  @Override
  public BXText replaceWholeText(final String content) {
    readOnly();
    return null;
  }

  @Override
  public BXText splitText(final int off) {
    readOnly();
    return null;
  }
}
