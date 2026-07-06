package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
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
  /** State index: comment. */
  private static final int COMMENT = 0;
  /** State index: last quote. */
  private static final int QUOTE = 1;
  /** State index: variable flag. */
  private static final int VAR = 2;
  /** State index: element flag. */
  private static final int ELEM = 3;

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
      for(final CmpOp op : CmpOp.values()) {
        Collections.addAll(KEYWORDS, op.toString());
        Collections.addAll(KEYWORDS, op.toValueString());
        for(final String o : op.nodes) Collections.addAll(KEYWORDS, o);
      }
      final Atts ns = NSGlobal.NS;
      for(int n = 0; n < ns.size(); n++) KEYWORDS.add(string(ns.name(n)));
    } catch(final Exception ex) {
      Util.stack(ex);
    }
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
      if(ch == state[QUOTE]) state[QUOTE] = 0;
      return brown;
    }

    // comment
    if(state[COMMENT] == 0 && ch == '(') {
      state[COMMENT]++;
    } else if(state[COMMENT] == 1) {
      state[COMMENT] = ch == ':' ? 2 : 0;
    } else if(state[COMMENT] == 2 && ch == ':') {
      state[COMMENT]++;
    } else if(state[COMMENT] == 3 && ch != ':') {
      state[COMMENT] = ch == ')' ? 0 : 2;
    }
    if(state[COMMENT] != 0) {
      state[VAR] = 0;
      return cyan;
    }

    // quotes
    if(ch == '"' || ch == '\'' || ch == '`') {
      state[QUOTE] = ch;
      return brown;
    }

    // variables
    if(ch == '$') {
      state[VAR] = 1;
      return green;
    }
    if(state[VAR] != 0) {
      state[VAR] = XMLToken.isChar(ch) ? 1 : 0;
      return green;
    }

    // integers
    if(number(iter)) return purple;

    // special characters
    if(!XMLToken.isNCChar(ch)) {
      state[ELEM] = ch == '<' || ch == '%' ? 1 : 0;
      return cyan;
    }

    // check for keywords
    if(state[ELEM] == 0 && KEYWORDS.contains(iter.currString())) return blue;

    // standard text
    state[ELEM] = 0;
    return plain;
  }

  @Override
  public byte[] commentOpen() {
    return XMLToken.XQCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.XQCOMM_C;
  }

  @Override
  public byte[] format(final byte[] text, final byte[] spaces) {
    final TokenBuilder tb = new TokenBuilder();
    final int tl = text.length;
    int quoted = 0, comments = 0, indents = 0;
    for(int t = 0; t < tl; t++) {
      final byte curr = text[t];
      final int open = OPENING.indexOf(curr), close = CLOSING.indexOf(curr);
      final int next = t + 1 < tl ? text[t + 1] : 0, prev = t > 0 ? text[t - 1] : 0;
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
