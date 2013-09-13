package org.basex.gui.editor;

import java.awt.*;

/**
 * This class defines syntax highlighting for JSON files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SyntaxJSON extends Syntax {
  /** Quoted flag. */
  private boolean quoted;

  @Override
  public void init() {
    quoted = false;
  }

  @Override
  public Color getColor(final EditorText text) {
    final int ch = text.curr();
    final boolean quote = text.curr() == '"';
    Color color = quoted || quote ? KEYWORD : TEXT;
    if(!quoted) {
      if("{}[]".indexOf(ch) != -1) color = STRING;
      if(":,".indexOf(ch) != -1) color = FUNCTION;
    }
    if(quote) quoted ^= true;
    return color;
  }
}
