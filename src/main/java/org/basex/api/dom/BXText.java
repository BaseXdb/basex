package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.Text;

/**
 * DOM - Text implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class BXText extends BXChar implements Text {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXText(final Nod n) {
    super(n);
  }

  @Override
  public String getWholeText() {
    return getNodeValue();
  }

  @Override
  public boolean isElementContentWhitespace() {
    return Token.ws(node.str());
  }

  @Override
  public BXText replaceWholeText(final String content) {
    error();
    return null;
  }

  @Override
  public BXText splitText(final int off) {
    error();
    return null;
  }
}
