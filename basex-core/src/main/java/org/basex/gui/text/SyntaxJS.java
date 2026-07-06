package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.util.*;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for Javascript files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SyntaxJS extends Syntax {
  /** Keywords. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();
  /** State index: comment. */
  private static final int COMMENT = 0;
  /** State index: quote. */
  private static final int QUOTE = 1;
  /** State index: variable flag. */
  private static final int VAR = 2;
  /** State index: backslash. */
  private static final int BACK = 3;

  // initialize keywords
  static {
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#Keywords
    Collections.addAll(KEYWORDS,
      "await", "break", "case", "catch", "class", "const", "continue", "debugger", "default",
      "delete", "do", "else", "enum", "export", "extends", "finally", "for", "function", "if",
      "implements", "import", "in", "instanceof", "interface", "let", "new", "package", "private",
      "protected", "public", "return", "static", "super", "switch", "this", "throw", "try",
      "typeof", "var", "void", "while", "with", "yield"
    );
  }

  @Override
  public void init(final Color color) {
    super.init(color);
    state = new int[4];
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();

    // opened quote
    if(state[QUOTE] != 0) {
      if(state[BACK] != 0) state[BACK] = 0;
      else if(ch == state[QUOTE]) state[QUOTE] = 0;
      else state[BACK] = ch == '\\' ? 1 : 0;
      return brown;
    }

    // comment: 1 = after '/', 2 = block, 3 = block after '*', 4 = line
    switch(state[COMMENT]) {
      case 1 -> state[COMMENT] = ch == '*' ? 2 : ch == '/' ? 4 : 0;
      case 2 -> { if(ch == '*') state[COMMENT] = 3; }
      case 3 -> state[COMMENT] = ch == '/' ? 0 : 2;
      case 4 -> { if(ch == '\n') state[COMMENT] = 0; }
      default -> { if(ch == '/') state[COMMENT] = 1; }
    }
    if(state[COMMENT] != 0) {
      state[VAR] = 0;
      return cyan;
    }

    // quotes
    if(state[BACK] != 0) {
      state[BACK] = 0;
    } else if(ch == '\\') {
      state[BACK] = 1;
    } else if(ch == '"' || ch == '\'') {
      state[QUOTE] = ch;
      return brown;
    }

    // variables
    if(ch == '$') {
      state[VAR] = 1;
      return green;
    }
    if(state[VAR] != 0) {
      state[VAR] = XMLToken.isNCStartChar(ch) ? 1 : 0;
      return green;
    }

    // digits (JS literals such as 0xFF are not full doubles, so match on the first digit)
    if(Token.digit(ch)) return purple;
    // special characters
    if(!XMLToken.isNCStartChar(ch)) return cyan;
    // check for keywords
    if(KEYWORDS.contains(iter.currString())) return blue;

    // standard text
    return plain;
  }

  @Override
  public byte[] commentOpen() {
    return XMLToken.JSCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.JSCOMM_C;
  }
}
