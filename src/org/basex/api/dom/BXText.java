package org.basex.api.dom;

import org.basex.query.item.Nod;
import org.basex.util.Token;
import org.w3c.dom.Text;

/**
 * DOM - Text Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

  public String getWholeText() {
    return getNodeValue();
  }

  public boolean isElementContentWhitespace() {
    return Token.ws(node.str());
  }

  public Text replaceWholeText(final String content) {
    error();
    return null;
  }

  public Text splitText(final int off) {
    error();
    return null;
  }
}
