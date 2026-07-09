package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.util.*;

/**
 * This class summarizes the result of a replacement.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ReplaceContext {
  /** Replace string. */
  private final String replace;
  /** Replace only the first hit in the range (regex mode). */
  private final boolean single;
  /** Text. */
  byte[] text;

  /**
   * Constructor.
   * @param replace replacement text
   */
  ReplaceContext(final String replace) {
    this(replace, false);
  }

  /**
   * Constructor.
   * @param replace replacement text
   * @param single replace only the first hit in the range (regex mode)
   */
  ReplaceContext(final String replace, final boolean single) {
    this.replace = replace;
    this.single = single;
  }

  /**
   * Replaces text.
   * @param sc search context
   * @param txt text
   * @param start start offset
   * @param end end offset
   * @return resulting end marker
   */
  int[] replace(final SearchContext sc, final byte[] txt, final int start, final int end) {
    final int os = txt.length;
    if(sc.string.isEmpty()) {
      text = txt;
    } else {
      final TokenBuilder tb = new TokenBuilder(os).add(txt, 0, start);
      if(sc.regex) {
        // regular expressions, applied to the whole range (ignoring position arrays)
        final Matcher matcher = sc.pattern.matcher(string(txt, start, end - start));
        tb.add(single ? matcher.replaceFirst(replace) : matcher.replaceAll(replace));
      } else {
        final byte[] srch = token(sc.string);
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
