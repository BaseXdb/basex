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
    tag = false;
  }

  @Override
  public Color getColor(final String word) {
    final char ch = word.length() != 0 ?  word.charAt(0) : 0;
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
      return GUIConstants.COLORS[16];
    }
    if(ch == '<') {
      tag = true;
      return GUIConstants.COLORS[16];
    }
    return Color.black;
  }
}
