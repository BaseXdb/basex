package org.basex.gui.text;

import static org.basex.data.DataText.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * This class defines syntax highlighting for XQuery files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXQuery extends Syntax {
  /** Opening brackets. */
  private static final String OPENING = "{(";
  /** Closing brackets. */
  private static final String CLOSING = "})";
  /** Keywords. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();

  /** Comment. */
  private int comment;
  /** Last quote. */
  private int quote;
  /** Variable flag. */
  private boolean var;

  // initialize xquery keys
  static {
    try {
      // add query tokens
      for(final Field f : QueryText.class.getFields()) {
        if("IGNORE".equals(f.getName())) break;
        Collections.addAll(KEYWORDS, ((String) f.get(null)).split("-"));
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  @Override
  public void init(final Color color) {
    super.init(color);
    quote = 0;
    var = false;
    comment = 0;
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
    if(comment == 0 && ch == '(') {
      comment++;
    } else if(comment == 1) {
      comment = ch == ':' ? 2 : 0;
    } else if(comment == 2 && ch == ':') {
      comment++;
    } else if(comment == 3 && ch != ':') {
      comment = ch == ')' ? 0 : 2;
    }
    if(comment != 0) {
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
      var = XMLToken.isChar(ch);
      return VARIABLE;
    }

    // digits
    if(Token.digit(ch)) return FUNCTION;
    // special characters
    if(!XMLToken.isNCChar(ch)) return COMMENT;
    // check for keywords
    if(KEYWORDS.contains(iter.nextString())) return KEYWORD;

    // standard text
    return plain;
  }

  @Override
  public byte[] commentOpen() {
    return XQCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XQCOMM_C;
  }

  @Override
  public byte[] format(final byte[] text, final byte[] spaces) {
    int ind = 0;
    final TokenBuilder tb = new TokenBuilder();
    final int tl = text.length;
    for(int t = 0; t < tl; t++) {
      final byte ch = text[t];
      final int open = OPENING.indexOf(ch);
      final int close = CLOSING.indexOf(ch);
      final int next = t + 1 < tl ? text[t + 1] : 0;
      final int prev = t > 0 ? text[t - 1] : 0;
      if(open != -1 && (next != ':' || ch != '(')) {
        ind++;
        tb.addByte(ch);
        if(next != '\n' && !matches(CLOSING.charAt(open), t, text, 3)) {
          tb.add('\n');
          for(int i = 0; i < ind; i++) tb.add(spaces);
        }
      } else if(close != -1 && (prev != ':' || ch != ')')) {
        ind--;
        if(!spaces(tb) && !matches(OPENING.charAt(close), t, text, -3)) {
          tb.add('\n');
          for(int i = 0; i < ind; i++) tb.add(spaces);
        }
        tb.addByte(ch);
      } else {
        tb.addByte(ch);
      }
    }
    return tb.finish();
  }

  /**
   * Checks if the last line contains only spaces.
   * @param text text
   * @return result of check
   */
  private static boolean spaces(final TokenBuilder text) {
    for(int t = text.size() - 1; t >= 0; t--) {
      final byte c = text.get(t);
      if(c == '\n') break;
      if(!Token.ws(c)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified character.
   * @param ch character to be found
   * @param pos current position
   * @param text text
   * @param dist maximum allowed distance
   * @return result of check
   */
  private static boolean matches(final char ch, final int pos, final byte[] text, final int dist) {
    for(int d = 0; dist > 0 ? d < dist : d > dist; d += dist > 0 ? 1 : -1) {
      final int p = pos + d;
      if(p < 0 || p >= text.length) break;
      if(text[p] == ch) return true;
    }
    return false;
  }
}
