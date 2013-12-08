package org.basex.gui.text;

import java.awt.*;

/**
 * This class defines syntax highlighting for JSON files.
 *
 * @author BaseX Team 2005-13, BSD License
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
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();
    final boolean quote = ch == '"';
    Color color = quoted || quote ? KEYWORD : TEXT;
    if(!quoted) {
      if("{}[]".indexOf(ch) != -1) color = STRING;
      if(":,".indexOf(ch) != -1) color = FUNCTION;
    }
    if(quote) quoted ^= true;
    return color;
  }
}
