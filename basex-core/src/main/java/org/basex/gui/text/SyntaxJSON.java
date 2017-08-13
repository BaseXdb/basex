package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.util.*;

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

    if(!quoted) {
      if("-+0123456789".indexOf(ch) != -1) return DIGIT;
      if("{}[]:,".indexOf(ch) != -1) return COMMENT;
      if(KEYWORDS.contains(iter.nextString())) return KEYWORD;
    } else {
      back = !back && ch == '\\';
    }

    if(quote) quoted ^= true;
    return quote || quoted ? VALUE : RED;
  }
}
