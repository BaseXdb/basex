package org.basex.gui.view.query;

import java.awt.Color;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXSyntax;
import org.basex.gui.layout.BaseXTextTokens;
import org.basex.util.Token;

/**
 * This abstract class defines syntax highlighting of text panels.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class QuerySyntax extends BaseXSyntax {
  /** Last quote. */
  private int quote;

  @Override
  public void init() {
    quote = 0;
  }

  @Override
  public Color getColor(final BaseXTextTokens text) {
    final int ch = text.curr();
    final boolean qu = ch == '"' || ch == '\'';
    if(quote != 0 || qu) {
      if(qu) quote = quote == ch ? 0 : ch;
      return GUIConstants.COLORERROR;
    }
    if(!Token.letterOrDigit(ch)) return GUIConstants.COLORQUOTE;
    return Color.black;
  }
}
