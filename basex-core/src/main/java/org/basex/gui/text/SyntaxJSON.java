package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;

import org.basex.util.hash.*;

/**
 * This class defines syntax highlighting for JSON files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SyntaxJSON extends Syntax {
  /** Keywords. */
  private static final TokenSet KEYWORDS = new TokenSet("false", "true", "null");
  /** State index: quoted flag. */
  private static final int QUOTED = 0;
  /** State index: backslash. */
  private static final int BACK = 1;

  @Override
  public void init(final Color color) {
    super.init(color);
    state = new int[2];
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();
    final boolean back = state[BACK] != 0;
    final boolean quote = !back && ch == '"';

    if(state[QUOTED] != 0) {
      state[BACK] = !back && ch == '\\' ? 1 : 0;
    } else {
      if("{}[]:,".indexOf(ch) != -1) return cyan;
      if(KEYWORDS.contains(token(iter.currString()))) return blue;
      if(number(iter)) return purple;
    }

    if(quote) state[QUOTED] ^= 1;
    return quote || state[QUOTED] != 0 ? brown : red;
  }
}
