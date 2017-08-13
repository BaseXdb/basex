package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.util.*;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for JSON files.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class SyntaxJSON extends Syntax {
  /** Keywords. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();

  // initialize keywords
  static {
    KEYWORDS.add("false");
    KEYWORDS.add("true");
    KEYWORDS.add("null");
  }

  /** Quoted flag. */
  private boolean quoted;

  @Override
  public void init(final Color color) {
    super.init(color);
    quoted = false;
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();
    if(!quoted) {
      if(Token.digit(ch)) return DIGIT;
      if("{}[]".indexOf(ch) != -1) return COMMENT;
      if(":,".indexOf(ch) != -1) return VALUE;
      if(KEYWORDS.contains(iter.nextString())) return KEYWORD;
    }

    final boolean quote = ch == '"';
    if(quote) quoted ^= true;
    return quote || quoted ? VALUE : RED;
  }
}
