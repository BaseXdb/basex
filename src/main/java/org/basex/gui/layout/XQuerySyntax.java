package org.basex.gui.layout;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashSet;
import org.basex.gui.GUIConstants;
import org.basex.query.QueryTokens;
import org.basex.query.func.FunDef;
import org.basex.util.Util;
import org.basex.util.XMLToken;

/**
 * This abstract class defines syntax highlighting of text panels.
 *
 * @author BaseX Team 2005-11, BSD License
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
      for(final Field f : QueryTokens.class.getFields()) {
        if(f.getName().equals("IGNORE")) break;
        final String s = (String) f.get(null);
        for(final String ss : s.split("-")) KEYS.add(ss);
      }
      for(final FunDef f : FunDef.values()) {
        final String s = f.toString();
        for(final String ss : s.substring(0, s.indexOf("(")).split("-"))
          FUNC.add(ss);
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
      return GUIConstants.COLORERROR;
    }
    if(quote != 0) {
      if(ch == quote) quote = 0;
      return GUIConstants.COLORERROR;
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
    if(KEYS.contains(word)) return GUIConstants.COLORS[16];
    // special characters
    if(FUNC.contains(word)) return FUNS;

    // special characters
    if(!XMLToken.isNCChar(ch)) return KEY;

    // letters and numbers
    return Color.black;
  }
}
