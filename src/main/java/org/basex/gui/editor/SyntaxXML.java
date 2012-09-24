package org.basex.gui.editor;

import static org.basex.data.DataText.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;

/**
 * This class defines syntax highlighting for XML files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXML extends Syntax {
  /** Last quote. */
  private int quote;
  /** Tag flag. */
  private boolean tag;
  /** Comment flag. */
  private int comm;
  /** Flag for printing element name. */
  private boolean elem;

  @Override
  public void init() {
    quote = 0;
    tag = false;
    comm = 0;
    elem = false;
  }

  @Override
  public Color getColor(final EditorText text) {
    final int ch = text.curr();
    if(comm > 0) {
      if(ch == '<') {
        comm++;
      } else if(ch == '>') {
        comm--;
      }
      return comm > 0 ? LBLUE : BLUE;
    }

    if(tag) {
      if(quote != 0) {
        if(quote == ch) quote = 0;
        return RED;
      }
      if(ch == '"' || ch == '\'') {
        quote = ch;
        return RED;
      }
      if(ch == '>') {
        tag = false;
        return BLUE;
      }
      if(ch == '=' || ch == '/') {
        return BLUE;
      }
      if(ch == '!') {
        comm++;
        tag = false;
        return LBLUE;
      }

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
