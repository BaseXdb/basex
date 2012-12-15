package org.basex.gui.editor;

import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.util.*;

/**
 * This class summarizes the result of a replacement.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class ReplaceContext {
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
   * @param start start offset
   * @param end end offset
   * @return resulting end marker
   */
  int[] replace(final SearchContext sc, final byte[] txt, final int start,
      final int end) {

    final int os = txt.length;
    search = sc;
    if(sc.search.isEmpty()) {
      text = txt;
    } else {
      final TokenBuilder tb = new TokenBuilder(os);
      tb.add(txt, 0, start);
      if(sc.regex) {
        // regular expressions, ignoring position arrays
        int flags = Pattern.DOTALL;
        if(!sc.mcase) flags |= Pattern.CASE_INSENSITIVE;
        final Pattern p = Pattern.compile(sc.search, flags);
        if(sc.multi) {
          tb.add(p.matcher(string(txt, start, end)).replaceAll(replace));
        } else {
          for(int e = start, s = start; e <= end; e++) {
            if(e < end ? txt[e] == '\n' : e != s) {
              tb.add(p.matcher(string(txt, s, e - s)).replaceAll(replace));
              if(e < end) tb.add('\n');
              s = e + 1;
            }
          }
        }
      } else {
        final byte[] srch = token(sc.search);
        final byte[] rplc = token(replace);
        final int ss = srch.length;
        boolean s = true;
        int s1 = start;
        for(int o = start; o < end;) {
          int sp = 0;
          if(o + ss <= end && s) {
            if(sc.mcase) {
              while(sp < ss && txt[o + sp] == srch[sp]) sp++;
            } else {
              while(sp < ss && lc(cp(txt, o + sp)) == cp(srch, sp)) sp += cl(srch, sp);
            }
          }
          if(sp == ss && (!sc.word || o + ss == os ||
              !Character.isLetterOrDigit(cp(txt, o + ss)))) {
            tb.add(txt, s1, o).add(rplc);
            o += ss;
            s1 = o;
            s = !sc.word;
          } else if(sc.word) {
            s = !Character.isLetterOrDigit(cp(txt, o));
            o += cl(txt, o);
          } else {
            o++;
          }
        }
        tb.add(txt, s1, end);
      }
      tb.add(txt, end, os);
      text = tb.finish();
    }
    return new int[] { start, end - os + text.length };
  }
}
