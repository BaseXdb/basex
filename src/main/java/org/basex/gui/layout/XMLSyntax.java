package org.basex.gui.layout;

import static org.basex.data.DataText.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.Color;

/**
 * This class defines syntax highlighting for XML files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XMLSyntax extends BaseXSyntax {
  /** Last quote. */
  private int quote;
  /** Tag flag. */
  private boolean tag;
  /** Flag for printing element name. */
  private boolean elem;

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
        return RED;
      }
      if(ch == '"' || ch == '\'') {
        quote = ch;
        return RED;
      }
      if(ch == '>') tag = false;

      if(ch == '=' || ch == '>' || ch == '/') return BLUE;
      if(elem) {
        if(ch <= ' ') elem = false;
        return BLUE;
      }
      return PINK;
    }
    if(ch == '<') {
      tag = true;
      elem = true;
      return BLUE;
    }
    return Color.black;
  }

  @Override
  public byte[] commentOpen() {
    return COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return COMM_C;
  }
}
