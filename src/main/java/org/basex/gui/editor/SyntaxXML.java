package org.basex.gui.editor;

import static org.basex.data.DataText.*;
import java.awt.*;

/**
 * This class defines syntax highlighting for XML files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXML extends Syntax {
  /** Last quote. */
  private int quote;
  /** Tag flag. */
  private boolean tag;
  /** Flag for printing element name. */
  private boolean elem;
  /** Current state of comment. */
  private int comment;
  /** Current state of processing instruction. */
  private int pi;

  @Override
  public void init() {
    quote = 0;
    tag = false;
    elem = false;
    comment = 0;
    pi = 0;
  }

  @Override
  public Color getColor(final EditorText text) {
    final int ch = text.curr();
    if(comment > 0) return comment(ch);
    if(pi > 0) return pi(ch);

    // last token was an opening angle bracket (<)
    if(tag) {
      if(quote != 0) {
        if(quote == ch) quote = 0;
        return STRING;
      }
      if(ch == '"' || ch == '\'') {
        quote = ch;
        return STRING;
      }
      if(ch == '>') {
        tag = false;
        return KEYWORD;
      }
      if(ch == '=' || ch == '/') {
        return KEYWORD;
      }
      if(ch == '!') {
        comment = 1;
        tag = false;
        return COMMENT;
      }
      if(ch == '?') {
        pi = 1;
        tag = false;
        return COMMENT;
      }

      if(elem) {
        if(ch <= ' ') elem = false;
        return KEYWORD;
      }
      return FUNCTION;
    }

    // start of a new element, comment or processing instruction
    if(ch == '<') {
      tag = true;
      elem = true;
      return KEYWORD;
    }
    return TEXT;
  }

  /**
   * Processes a comment or doctype declaration.
   * @param ch current character
   * @return color
   */
  private Color comment(final int ch) {
    switch(comment) {
      // "<!"
      case 1: comment = ch == '-' ? comment + 1 : 6; break;
      // "<!-"
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
    return pi > 0 ? FUNCTION : KEYWORD;
  }

  @Override
  public byte[] commentOpen() {
    return COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return COMM_C;
  }
}
