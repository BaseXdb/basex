package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.CmpN.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class defines syntax highlighting for XQuery files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SyntaxXQuery extends Syntax {
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
  /** Element flag. */
  private boolean elem;

  // initialize keywords
  static {
    try {
      // add query tokens
      for(final Field f : QueryText.class.getFields()) {
        if("IGNORE".equals(f.getName())) break;
        Collections.addAll(KEYWORDS, ((String) f.get(null)).split("-"));
      }
      for(final QNm name : Functions.BUILT_IN) {
        Collections.addAll(KEYWORDS, string(name.local()).split("-"));
      }
      for(final Axis a : Axis.values()) Collections.addAll(KEYWORDS, a.name);
      for(final OpV o : OpV.values()) Collections.addAll(KEYWORDS, o.name);
      for(final OpN o : OpN.values()) Collections.addAll(KEYWORDS, o.names[0]);
      final Atts ns = NSGlobal.NS;
      for(int n = 0; n < ns.size(); n++) KEYWORDS.add(string(ns.name(n)));
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  @Override
  public void init(final Color color) {
    super.init(color);
    quote = 0;
    var = false;
    elem = false;
    comment = 0;
  }

  @Override
  public Color getColor(final TextIterator iter) {
    final int ch = iter.curr();

    // opened quote
    if(quote != 0) {
      if(ch == quote) quote = 0;
      return DGRAY;
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
      return GRAY;
    }

    // quotes
    if(ch == '"' || ch == '\'' || ch == '`') {
      quote = ch;
      return DGRAY;
    }

    // variables
    if(ch == '$') {
      var = true;
      return GREEN;
    }
    if(var) {
      var = XMLToken.isChar(ch);
      return GREEN;
    }

    // integers
    if(digit(ch) && !Double.isNaN(toDouble(token(iter.currString())))) return PURPLE;

    // special characters
    if(!XMLToken.isNCChar(ch)) {
      elem = ch == '<' || ch == '%';
      return GRAY;
    }

    // check for keywords
    if(!elem && KEYWORDS.contains(iter.currString())) return BLUE;

    // standard text
    elem = false;
    return plain;
  }

  @Override
  public byte[] commentOpen() {
    return DataText.XQCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return DataText.XQCOMM_C;
  }

  @Override
  public byte[] format(final byte[] text, final byte[] spaces) {
    final TokenBuilder tb = new TokenBuilder();
    final int tl = text.length;
    int quoted = 0, comments = 0, indents = 0;
    for(int t = 0; t < tl; t++) {
      final byte curr = text[t];
      final int open = OPENING.indexOf(curr), close = CLOSING.indexOf(curr);
      int next = t + 1 < tl ? text[t + 1] : 0, prev = t > 0 ? text[t - 1] : 0;
      if(quoted != 0) {
        if(curr == quoted) quoted = 0;
      } else if("\"'`".indexOf(curr) != -1) {
        quoted = curr;
      } else if(curr == '(' && next == ':') {
        comments++;
      } else if(prev == ':' && curr == ')') {
        comments--;
      } else if(comments == 0) {
        if(open != -1) {
          indents++;
          tb.addByte(curr);
          if(next != '\n' && !matches(CLOSING.charAt(open), t, text, true)) {
            tb.add('\n');
            for(int i = 0; i < indents; i++) tb.add(spaces);
          }
          continue;
        } else if(close != -1) {
          indents--;
          if(!endingWithWs(tb) && !matches(OPENING.charAt(close), t, text, false)) {
            tb.add('\n');
            for(int i = 0; i < indents; i++) tb.add(spaces);
          }
        }
      }
      tb.addByte(curr);
    }
    return tb.finish();
  }

  /**
   * Checks if the previous line contains only spaces.
   * @param text text
   * @return result of check
   */
  private static boolean endingWithWs(final TokenBuilder text) {
    for(int t = text.size() - 1; t >= 0; t--) {
      final byte c = text.get(t);
      if(c == '\n') break;
      if(!ws(c)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified character is found near the current position.
   * @param ch character to be found
   * @param pos current position
   * @param text text
   * @param after search after or before the current position
   * @return result of check
   */
  private static boolean matches(final char ch, final int pos, final byte[] text,
      final boolean after) {
    final int dist = after ? 3 : -3;
    for(int d = 0; after ? d < dist : d > dist; d += after ? 1 : -1) {
      final int p = pos + d;
      if(p >= 0 && p < text.length && text[p] == ch) return true;
    }
    return false;
  }
}
