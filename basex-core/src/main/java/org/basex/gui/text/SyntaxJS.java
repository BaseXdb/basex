package org.basex.gui.text;

import static org.basex.data.DataText.*;

import java.awt.*;
import java.util.*;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for Javascript files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class SyntaxJS extends Syntax {
  /** Keywords. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();

  /** Comment. */
  private int comment1;
  /** Comment. */
  private int comment2;
  /** Quote. */
  private int quote;
  /** Variable flag. */
  private boolean var;
  /** Backslash. */
  private boolean back;

  // initialize xquery keys
  static {
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#Keywords
    for(final String key : new String[] {
      "await", "break", "case", "catch", "class", "const", "continue", "debugger", "default",
      "delete", "do", "else", "enum", "export", "extends", "finally", "for", "function", "if",
      "implements", "import", "in", "instanceof", "interface", "let", "new", "package", "private",
      "protected", "public", "return", "static", "super", "switch", "this", "throw", "try",
      "typeof", "var", "void", "while", "with", "yield"
    }) Collections.addAll(KEYWORDS, key);
  }

  @Override
  public void init(final Color color) {
    super.init(color);
    quote = 0;
    var = false;
    back = false;
    comment1 = 0;
    comment2 = 0;
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();

    // opened quote
    if(quote != 0) {
      if(back) {
        back = false;
      } else if(ch == quote) {
        quote = 0;
      } else {
        back = ch == '\\';
      }
      return STRING;
    }

    // comment
    if(comment1 == 0 && ch == '/') {
      comment1++;
    } else if(comment1 == 1) {
      comment1 = ch == '*' ? 2 : 0;
    } else if(comment1 == 2 && ch == '*') {
      comment1++;
    } else if(comment1 == 3) {
      comment1 = ch == '/' ? 0 : 2;
    }
    // comment
    if(comment2 == 0 && ch == '/') {
      comment2++;
    } else if(comment2 == 1) {
      comment2 = ch == '/' ? 2 : 0;
    } else if(comment2 == 2 && ch == '\n') {
      comment2 = 0;
    }
    if(comment1 != 0 || comment2 != 0) {
      var = false;
      return COMMENT;
    }

    // quotes
    if(back) {
      back = false;
    } else if(ch == '\\') {
      back = true;
    } else if(ch == '"' || ch == '\'') {
      quote = ch;
      return STRING;
    }

    // variables
    if(ch == '$') {
      var = true;
      return VARIABLE;
    }
    if(var) {
      var = XMLToken.isNCStartChar(ch);
      return VARIABLE;
    }

    // digits
    if(Token.digit(ch)) return FUNCTION;
    // special characters
    if(!XMLToken.isNCStartChar(ch)) return COMMENT;
    // check for keywords
    if(KEYWORDS.contains(iter.nextString())) return KEYWORD;

    // standard text
    return plain;
  }

  @Override
  public byte[] commentOpen() {
    return JSCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return JSCOMM_C;
  }
}
