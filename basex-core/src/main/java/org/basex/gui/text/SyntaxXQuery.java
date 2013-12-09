package org.basex.gui.text;

import static org.basex.data.DataText.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class defines syntax highlighting for XQuery files.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXQuery extends Syntax {
  /** Error color. */
  private static final HashSet<String> KEYS = new HashSet<String>();
  /** Error color. */
  private static final HashSet<String> FUNC = new HashSet<String>();

  /** Comment. */
  private int comment;
  /** Last quote. */
  private int quote;
  /** Variable flag. */
  private boolean var;
  /** Function flag. */
  private boolean fun;

  // initialize xquery keys
  static {
    try {
      // add query tokens
      for(final Field f : QueryText.class.getFields()) {
        if("IGNORE".equals(f.getName())) break;
        final String s = (String) f.get(null);
        Collections.addAll(KEYS, s.split("-"));
      }
      // add function names
      for(final Function f : Function.values()) {
        final String s = f.toString();
        Collections.addAll(FUNC, s.substring(0, s.indexOf('(')).split(":|-"));
      }
      // add serialization parameters and database options
      addOptions(SerializerOptions.class);
      addOptions(GlobalOptions.class);
      addOptions(MainOptions.class);
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  /**
   * Adds the specified options.
   * @param opt option class
   * @throws Exception exception
   */
  private static void addOptions(final Class<? extends Options> opt) throws Exception {
    for(final Option<?> o : Options.options(opt)) {
      if(o instanceof Comment) continue;
      Collections.addAll(FUNC, o.name().toLowerCase(Locale.ENGLISH).split("-"));
    }
  }

  @Override
  public void init() {
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
    if(comment != 0) return COMMENT;

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

    // special characters
    if(!XMLToken.isNCChar(ch)) {
      fun = false;
      return COMMENT;
    }

    // check for keywords and function names
    final String word = iter.nextString();
    final boolean keys = KEYS.contains(word);
    final boolean func = FUNC.contains(word);
    if(fun && func) return FUNCTION;
    if(keys) return KEYWORD;
    if(func) {
      fun = true;
      return FUNCTION;
    }

    // letters and numbers
    return TEXT;
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
  public byte[] format(final byte[] text) {
    int ind = 0;
    final TokenBuilder tb = new TokenBuilder();
    final int tl = text.length;
    for(int t = 0; t < tl; t++) {
      final byte ch = text[t];
      final int open = TextEditor.OPENING.indexOf(ch);
      final int close = TextEditor.CLOSING.indexOf(ch);
      final int next = t + 1 < tl ? text[t + 1] : 0;
      final int prev = t > 0 ? text[t - 1] : 0;
      if(open != -1 && (next != ':' || ch != '(')) {
        ind++;
        tb.addByte(ch);
        if(next != '\n' && !matches(TextEditor.CLOSING.charAt(open), t, text, 3)) {
          tb.add('\n');
          for(int i = 0; i < ind; i++) tb.add(TextEditor.INDENT);
        }
      } else if(close != -1 && (prev != ':' || ch != ')')) {
        ind--;
        if(!spaces(tb) && !matches(TextEditor.OPENING.charAt(close), t, text, -3)) {
          tb.add('\n');
          for(int i = 0; i < ind; i++) tb.add(TextEditor.INDENT);
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
  private boolean spaces(final TokenBuilder text) {
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
  private boolean matches(final char ch, final int pos, final byte[] text, final int dist) {
    for(int d = 0; dist > 0 ? d < dist : d > dist; d += dist > 0 ? 1 : -1) {
      final int p = pos + d;
      if(p < 0 || p >= text.length) break;
      if(text[p] == ch) return true;
    }
    return false;
  }
}
