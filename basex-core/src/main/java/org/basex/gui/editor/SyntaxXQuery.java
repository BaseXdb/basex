package org.basex.gui.editor;

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
  public Color getColor(final EditorText text) {
    final int ch = text.curr();

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
    final String word = text.nextString();
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
  public byte[] format(final byte[] string) {
    int ind = 0;
    final TokenBuilder tb = new TokenBuilder();
    final int sl = string.length;
    for(int s = 0; s < sl; s++) {
      final byte ch = string[s];
      final int open = EditorText.OPENING.indexOf(ch);
      final int close = EditorText.CLOSING.indexOf(ch);
      final int next = s + 1 < sl ? string[s + 1] : '\n';
      final int prev = s > 0 ? string[s - 1] : '\n';
      if(open != -1 && next != '\n' && next != ':' && next != EditorText.CLOSING.charAt(open)) {
        ind++;
        tb.addByte(ch).add('\n');
        for(int i = 0; i < ind; i++) tb.add(EditorText.INDENT);
      } else if(close != -1 && next != '\n' && !spaces(s, tb) &&
          prev != EditorText.OPENING.charAt(close)) {
        ind = Math.max(0, ind - 1);
        tb.add('\n');
        for(int i = 0; i < ind; i++) tb.add(EditorText.INDENT);
        tb.addByte(ch);
      } else {
        tb.addByte(ch);
      }
    }
    return tb.finish();
  }

  /**
   * Checks if only spaces are found from the beginning of a line and the cursor position.
   * @param p cursor position
   * @param text text
   * @return result of check
   */
  private boolean spaces(final int p, final TokenBuilder text) {
    for(int i = p - 1; i >= 0; i--) {
      if(text.get(i) == '\n') break;
      if(!Token.ws(text.get(i))) return false;
    }
    return true;
  }
}
