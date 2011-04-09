package org.basex.gui.layout;

import java.awt.Color;
import org.basex.gui.GUIConstants;

/**
 * This abstract class defines syntax highlighting of text panels.
 *
 * @author BaseX Team 2005-11, BSD License
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
    tag = false;
  }

  @Override
  public Color getColor(final BaseXTextTokens text) {
    final int ch = text.curr();
    if(tag) {
      if(quote != 0) {
        if(quote == ch) quote = 0;
        return GUIConstants.COLORERROR;
      }
      if(ch == '"' || ch == '\'') {
        quote = ch;
        return GUIConstants.COLORERROR;
      }
      if(ch == '>') tag = false;
      return GUIConstants.COLORS[12];
    }
    if(ch == '<') {
      tag = true;
      return GUIConstants.COLORS[12];
    }
    return Color.black;
  }
}
