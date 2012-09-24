package org.basex.gui.editor;

import java.util.regex.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class summarizes the result of a replacement.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ReplaceContext {
  /** Search context. */
  SearchContext search;
  /** Replace string. */
  String replace;
  /** Text. */
  byte[] text;

  /**
   * Constructor.
   * @param rplc replacement text
   */
  ReplaceContext(final String rplc) {
    replace = rplc;
  }

  /**
   * Replaces text.
   * @param sc search context
   * @param txt text
   */
  void replace(final SearchContext sc, final byte[] txt) {
    search = sc;
    if(!sc.start.isEmpty()) {
      if(sc.regex) {
        // regular expressions, ignoring position arrays
        int flags = Pattern.DOTALL;
        if(!sc.mcase) flags |= Pattern.CASE_INSENSITIVE;
        final Pattern p = Pattern.compile(sc.search, flags);
        if(sc.multi) {
          text = Token.token(p.matcher(Token.string(txt)).replaceAll(replace));
        } else {
          final int os = txt.length;
          final TokenBuilder tb = new TokenBuilder(os);
          for(int s = 0, o = 0; o <= os; o++) {
            if(o < os ? txt[o] == '\n' : o != s) {
              tb.add(p.matcher(Token.string(txt, s, o - s)).replaceAll(replace));
              if(o < os) tb.add('\n');
              s = o + 1;
            }
          }
          text = tb.finish();
        }
      } else {
        // standard replacement, using existing position arrays
        final int ss = sc.start.size();
        final byte[] rplc = Token.token(replace);
        final ByteList bl = new ByteList();
        int s1 = 0;
        for(int p = 0; p < ss; p++) {
          final int s2 = sc.start.get(p);
          bl.add(txt, s1, s2).add(rplc);
          s1 = sc.end.get(p);
        }
        bl.add(txt, s1, txt.length);
        text = bl.toArray();
      }
    }
  }
}
