package org.basex.gui.layout;

import static org.basex.data.DataText.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.gui.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * This class defines syntax highlighting for XQuery files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XQuerySyntax extends BaseXSyntax {
  /** Error color. */
  private static final HashSet<String> KEYS = new HashSet<String>();
  /** Error color. */
  private static final HashSet<String> FUNC = new HashSet<String>();
  /** Variable color. */
  private static final Color VAR = new Color(0, 160, 0);
  /** Keyword. */
  private static final Color KEY = new Color(0, 144, 144);
  /** Keyword. */
  private static final Color FUNS = new Color(160, 0, 160);

  /** Comment. */
  private int comment;
  /** Last quote. */
  private int quote;
  /** Variable flag. */
  private boolean var;

  // initialize xquery keys
  static {
    try {
      for(final Field f : QueryText.class.getFields()) {
        if(f.getName().equals("IGNORE")) break;
        final String s = (String) f.get(null);
        Collections.addAll(KEYS, s.split("-"));
      }
      for(final Function f : Function.values()) {
        final String s = f.toString();
        Collections.addAll(FUNC, s.substring(0, s.indexOf('(')).split("-"));
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  @Override
  public void init() {
    quote = 0;
    var = false;
    comment = 0;
  }

  @Override
  public Color getColor(final BaseXTextTokens text) {
    final int ch = text.curr();

    // opened quote
    if(quote != 0) {
      if(ch == quote) quote = 0;
      return GUIConstants.RED;
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
    if(comment != 0) return KEY;

    // quotes
    if(quote == 0 && (ch == '"' || ch == '\'')) {
      quote = ch;
      return GUIConstants.RED;
    }

    // variables
    if(ch == '$') {
      var = true;
      return VAR;
    }
    if(var) {
      var = XMLToken.isChar(ch);
      return VAR;
    }

    // special characters
    final String word = text.nextWord();
    if(KEYS.contains(word)) return GUIConstants.BLUE;
    // special characters
    if(FUNC.contains(word)) return FUNS;

    // special characters
    if(!XMLToken.isNCChar(ch)) return KEY;

    // letters and numbers
    return Color.black;
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
