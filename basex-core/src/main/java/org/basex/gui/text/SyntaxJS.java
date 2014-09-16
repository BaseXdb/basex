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
public final class SyntaxJS extends Syntax {
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

  // initialize xquery keys
  static {
    // add keywords
    final String[] keywords = {
      "break", "case", "catch", "continue", "default", "delete", "do", "else", "false", "for",
      "function", "if", "in", "instanceof", "new", "null", "return", "super", "switch",
      "this", "throw", "true", "try", "typeof", "var", "while", "with"
    };
    for(final String key : keywords) Collections.addAll(KEYWORDS, key);
  }

  @Override
  public void init(final Color color) {
    super.init(color);
    quote = 0;
    var = false;
    comment1 = 0;
    comment2 = 0;
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();

    // opened quote
    if(quote != 0) {
      if(ch == quote) quote = 0;
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
    if(ch == '"' || ch == '\'') {
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

    // check for keywords and function names
    final String word = iter.nextString();
    if(KEYWORDS.contains(word)) return KEYWORD;

    // letters and numbers
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
