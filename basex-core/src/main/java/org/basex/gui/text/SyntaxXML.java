package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for XML files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXML extends Syntax {
  /** State index: last quote. */
  private static final int QUOTE = 0;
  /** State index: element name flag. */
  private static final int NAME = 1;
  /** State index: element printing flag. */
  private static final int ELEM = 2;
  /** State index: comment. */
  private static final int COMMENT = 3;
  /** State index: processing instruction. */
  private static final int PI = 4;

  @Override
  public void init(final Color color) {
    super.init(color);
    state = new int[5];
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();
    if(state[COMMENT] > 0) return comment(ch);
    if(state[PI] > 0) return pi(ch);

    // last token was an opening angle bracket (<)
    if(state[NAME] != 0) {
      if(state[QUOTE] != 0) {
        if(state[QUOTE] == ch) state[QUOTE] = 0;
        return brown;
      }
      if(ch == '"' || ch == '\'') {
        state[QUOTE] = ch;
        return brown;
      }
      if(ch == '>') {
        state[NAME] = 0;
        return blue;
      }
      if(ch == '=' || ch == '/') {
        return blue;
      }
      if(ch == '!') {
        state[COMMENT] = 1;
        state[NAME] = 0;
        return cyan;
      }
      if(ch == '?') {
        state[PI] = 1;
        state[NAME] = 0;
        return cyan;
      }

      if(state[ELEM] != 0) {
        if(ch <= ' ') state[ELEM] = 0;
        return blue;
      }
      return purple;
    }

    // start of a new element, comment or processing instruction
    if(ch == '<') {
      state[NAME] = 1;
      state[ELEM] = 1;
      return blue;
    }
    return plain;
  }

  /**
   * Processes a comment, doctype declaration or CDATA section.
   * @param ch current character
   * @return color
   */
  private Color comment(final int ch) {
    switch(state[COMMENT]) {
      // "<!"
      case 1 -> state[COMMENT] = ch == '-' ? 2 : ch == '[' ? 7 : 6;
      // "<!-"
      case 2 -> state[COMMENT] = ch == '-' ? 3 : 6;
      // "<!--"
      case 3 -> {
        if(ch == '-') state[COMMENT] = 4;
      }
      // "<!-- ... -"
      case 4 -> state[COMMENT] = ch == '-' ? state[COMMENT] + 1 : 3;
      // "<!-- ... --"
      case 5 -> state[COMMENT] = ch == '>' ? 0 : 3;
      // "<! ... >"
      case 6 -> {
        if(ch == '>') state[COMMENT] = 0;
      }
      // "<![ ... " (CDATA or marked section)
      case 7 -> {
        if(ch == ']') state[COMMENT] = 8;
      }
      // "<![ ... ]"
      case 8 -> state[COMMENT] = ch == ']' ? 9 : 7;
      // "<![ ... ]]"
      case 9 -> state[COMMENT] = ch == '>' ? 0 : ch == ']' ? 9 : 7;
      default -> { }
    }
    return state[COMMENT] > 0 ? cyan : blue;
  }

  /**
   * Processes a processing instruction.
   * @param ch current character
   * @return color
   */
  private Color pi(final int ch) {
    switch(state[PI]) {
      // "<?"
      case 1 -> {
        if(ch == '?') state[PI] = 2;
      }
      // "<!? ... ?"
      case 2 -> state[PI] = ch == '>' ? 0 : 1;
      default -> { }
    }
    return state[PI] > 0 ? cyan : blue;
  }

  @Override
  public byte[] commentOpen() {
    return XMLToken.COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.COMM_C;
  }
}
