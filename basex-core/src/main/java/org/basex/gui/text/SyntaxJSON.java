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

  /** Mode: value. */
  private static final int VALUE = 0;
  /** Mode: string. */
  private static final int STRING = 1;

  @Override
  Color color(final int mode) {
    return mode == STRING ? brown : plain;
  }

  @Override
  Color mode(final byte[] text, final int pos, final int end, final int ch, final int mode) {
    if(mode == VALUE) {
      if(ch == '"') {
        enter(STRING, 0);
        return brown;
      }
      if("{}[]:,".indexOf(ch) != -1) return cyan;
      if(ws(ch)) return plain;
      if(KEYWORDS.contains(token(string(text, pos, end - pos)))) return blue;
      // numbers are tokenized into several parts: '-', '1', '.', '5e', '-', '3'
      if(digit(ch) || (ch == '-' || ch == '+' || ch == '.') && digit(cp(text, pos + 1)))
        return purple;
      // any other character is invalid
      return red;
    }
    if(ch == '\\') state[SKIP] = 1;
    else if(ch == '"') close(0);
    return brown;
  }
}
