package org.basex.gui.text;

import java.util.regex.*;

import org.basex.util.*;

/**
 * This class summarizes the result of a single replacement.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ReplaceContext {
  /** Replace string. */
  private final String replace;
  /** Replace only the first hit in the range. */
  private final boolean single;
  /** Text. */
  byte[] text;
  /** Number of replaced hits. */
  int count;
  /** Caret position: supplied by the caller, adjusted by the replacements. */
  int caret;

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
   * @param single replace only the first hit in the range
   */
  ReplaceContext(final String replace, final boolean single) {
    this.replace = replace;
    this.single = single;
  }

  /**
   * Replaces text.
   * @param sc search context
   * @param txt text
   * @param str decoded text (must be equivalent to {@code txt})
   * @param start start offset
   * @param end end offset
   * @return resulting end marker
   */
  int[] replace(final SearchContext sc, final byte[] txt, final String str, final int start,
      final int end) {
    final int os = txt.length;
    if(sc.pattern == null) {
      text = txt;
    } else {
      // hits outside the range are skipped: anchors and lookaround must see the whole text
      final Matcher matcher = sc.pattern.matcher(str);
      final TokenBuilder tb = new TokenBuilder(os);
      final StringBuilder sb = new StringBuilder();
      final TextCursor cursor = new TextCursor(txt);
      // pc/p: char/byte offset of the copied text, cr: new caret position ({@code -1}: unknown)
      int pc = 0, p = 0, cr = -1;
      while(matcher.find()) {
        final int ms = matcher.start(), me = matcher.end();
        final int bs = cursor.advance(ms);
        if(bs > end) break;
        final int be = cursor.advance(me);
        if(bs < start || be > end) continue;
        // appendReplacement prefixes the expanded replacement with the text since the last hit
        sb.setLength(0);
        matcher.appendReplacement(sb, replace);
        tb.add(txt, p, bs);
        // the caret is shifted by the preceding hits, or moved to the end of an enclosing one
        final int cs = tb.size();
        tb.add(sb.substring(ms - pc));
        if(cr == -1 && caret < be) cr = caret <= bs ? caret + cs - bs : tb.size();
        pc = me;
        p = be;
        count++;
        if(single) break;
      }
      text = tb.add(txt, p, os).finish();
      caret = cr != -1 ? cr : caret + text.length - os;
    }
    return new int[] { start, end - os + text.length };
  }
}
