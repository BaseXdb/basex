package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import org.basex.data.*;

/**
 * This class defines syntax highlighting for XML files.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXML extends Syntax {
  /** Last quote. */
  private int quote;
  /** Element name flag. */
  private boolean name;
  /** Flag for printing element name. */
  private boolean elem;
  /** Current state of comment. */
  private int comment;
  /** Current state of processing instruction. */
  private int pi;

  @Override
  public void init(final Color color) {
    super.init(color);
    quote = 0;
    name = false;
    elem = false;
    comment = 0;
    pi = 0;
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();
    if(comment > 0) return comment(ch);
    if(pi > 0) return pi(ch);

    // last token was an opening angle bracket (<)
    if(name) {
      if(quote != 0) {
        if(quote == ch) quote = 0;
        return VALUE;
      }
      if(ch == '"' || ch == '\'') {
        quote = ch;
        return VALUE;
      }
      if(ch == '>') {
        name = false;
        return KEYWORD;
      }
      if(ch == '=' || ch == '/') {
        return KEYWORD;
      }
      if(ch == '!') {
        comment = 1;
        name = false;
        return COMMENT;
      }
      if(ch == '?') {
        pi = 1;
        name = false;
        return VARIABLE;
      }

      if(elem) {
        if(ch <= ' ') elem = false;
        return KEYWORD;
      }
      return VARIABLE;
    }

    // start of a new element, comment or processing instruction
    if(ch == '<') {
      name = true;
      elem = true;
      return KEYWORD;
    }
    return plain;
  }

  /**
   * Processes a comment or doctype declaration.
   * @param ch current character
   * @return color
   */
  private Color comment(final int ch) {
    switch(comment) {
      // "<!", "<!-"
      case 1:
      case 2: comment = ch == '-' ? comment + 1 : 6; break;
      // "<!--"
      case 3: if(ch == '-') comment = 4; break;
      // "<!-- ... -"
      case 4: comment = ch == '-' ? comment + 1 : 3; break;
      // "<!-- ... --"
      case 5: comment = ch == '>' ? 0 : 3; break;
      // "<! ... >"
      case 6: if(ch == '>') comment = 0; break;
    }
    return comment > 0 ? COMMENT : KEYWORD;
  }

  /**
   * Processes a processing instruction.
   * @param ch current character
   * @return color
   */
  private Color pi(final int ch) {
    switch(pi) {
      // "<?"
      case 1: if(ch == '?') pi = 2; break;
      // "<!? ... ?"
      case 2: pi = ch == '>' ? 0 : 1; break;
    }
    return pi > 0 ? VARIABLE : KEYWORD;
  }

  @Override
  public byte[] commentOpen() {
    return DataText.COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return DataText.COMM_C;
  }
}
