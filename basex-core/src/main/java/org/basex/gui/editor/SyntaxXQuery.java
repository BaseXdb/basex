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
 * @author BaseX Team 2005-12, BSD License
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
        if(f.getName().equals("IGNORE")) break;
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
    for(final Option o : Options.options(opt)) {
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
}
