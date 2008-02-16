package org.basex.gui.view.text;

import java.awt.Color;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXSyntax;

/**
 * This abstract class defines syntax highlighting of text panels.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XMLSyntax extends BaseXSyntax {
  /** Last quote. */
  private int quote;
  /** Tag flag. */
  private boolean tag;

  @Override
  public void init() {
    quote = 0;
  }

  @Override
  public Color getColor(final String word) {
    final char ch = word.charAt(0);
    final boolean qu = ch == '"' || ch == '\'';
    if(quote != 0 || qu) {
      if(qu) quote = quote == ch ? 0 : ch;
      return GUIConstants.COLORERROR;
    }
    if(tag) {
      if(ch == '>') tag = false;  
      return GUIConstants.COLORQUOTE;
    }
    if(ch == '<') {
      tag = true;
      return GUIConstants.COLORQUOTE;
    }
    return Color.black;
  }
}
