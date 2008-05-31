package org.basex.gui.view.query;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashSet;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXSyntax;
import org.basex.query.xquery.XQTokens;
import org.basex.util.XMLToken;

/**
 * This abstract class defines syntax highlighting of text panels.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class QuerySyntax extends BaseXSyntax {
  /** Error color. */
  public static HashSet<String> keys = new HashSet<String>();
  /** Variable color. */
  public static final Color VAR = new Color(0, 160, 0);
  /** Keyword. */
  public static final Color KEY = new Color(0, 144, 144);

  /** Last quote. */
  private int quote;
  /** Variable flag. */
  private boolean var;

  // initialize xquery keys
  static {
    try {
      for(final Field f : XQTokens.class.getFields()) {
        final String name = f.getName();
        if(name.equals("SKIP")) break;
        keys.add((String) f.get(null));
      }
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void init() {
    quote = 0;
    var = false;
  }

  @Override
  public Color getColor(final String word) {
    final char ch = word.charAt(0);

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
      var &= XMLToken.isLetterOrDigit(ch);
      return VAR;
    }

    // special characters
    if(keys.contains(word)) return GUIConstants.COLORQUOTE;

    // special characters
    if(!XMLToken.isXMLLetterOrDigit(ch)) return KEY;

    // letters and numbers
    return Color.black;
  }
}
