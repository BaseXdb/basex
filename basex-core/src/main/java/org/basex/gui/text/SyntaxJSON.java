package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;

import org.basex.util.hash.*;

/**
 * This class defines syntax highlighting for JSON files.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
final class SyntaxJSON extends Syntax {
  /** Keywords. */
  private static final TokenSet KEYWORDS = new TokenSet();

  // initialize keywords
  static {
    KEYWORDS.add("false");
    KEYWORDS.add("true");
    KEYWORDS.add("null");
  }

  /** Quoted flag. */
  private boolean quoted;
  /** Backslash. */
  private boolean back;

  @Override
  public void init(final Color color) {
    super.init(color);
    quoted = false;
    back = false;
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();
    final boolean quote = !back && ch == '"';

    if(quoted) {
      back = !back && ch == '\\';
    } else {
      if("{}[]:,".indexOf(ch) != -1) return GRAY;
      final byte[] token = token(iter.currString());
      if(KEYWORDS.contains(token)) return BLUE;
      if(digit(ch) && !Double.isNaN(toDouble(token))) return PURPLE;
    }

    if(quote) quoted ^= true;
    return quote || quoted ? DGRAY : RED;
  }
}
