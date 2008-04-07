package org.basex.api.dom;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.util.Token;
import org.w3c.dom.Text;

/**
 * DOM - Text Implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TextImpl extends CharImpl implements Text {
  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  public TextImpl(final Data d, final int p) {
    super(d, p, Data.TEXT);
  }
  
  public String getWholeText() {
    return getNodeValue();
  }

  public boolean isElementContentWhitespace() {
    return Token.ws(data.text(pre));
  }

  public Text replaceWholeText(final String content) {
    BaseX.noupdates();
    return null;
  }

  public Text splitText(final int off) {
    BaseX.noupdates();
    return null;
  }
}
