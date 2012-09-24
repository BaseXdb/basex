package org.basex.gui.layout;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class contains data on search operations in the GUI.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class SearchContext {
  /** Search direction. */
  public enum SearchDir {
    /** Same position. */ SAME,
    /** Forward. */ FORWARD,
    /** Backward. */ BACKWARD
  }

  /** Mode: match case. */
  final boolean mcase;
  /** Mode: regular expression. */
  final boolean regex;
  /** Mode: multi-line. */
  final boolean multi;
  /** Search string. */
  final byte[] search;

  /** Start positions. */
  final IntList start = new IntList();
  /** End positions. */
  final IntList end = new IntList();

  /**
   * Constructor.
   * @param panel search panel
   * @param srch search text
   */
  SearchContext(final BaseXSearch panel, final String srch) {
    mcase = panel.mcase.isSelected();
    regex = panel.regex.isSelected();
    multi = panel.multi.isSelected();
    search = Token.token(mcase ? srch : srch.toLowerCase(Locale.ENGLISH));
  }

  /**
   * Performs the actual search.
   * @param text source text
   */
  void search(final byte[] text) {
    start.reset();
    end.reset();
    final byte[] srch = search;
    final int ss = srch.length, ts = text.length;
    if(ss == 0) return;

    for(int t = 0; t < ts;) {
      int s = 0;
      if(t + ss <= ts) {
        if(mcase) {
          for(; s < ss && text[t + s] == srch[s]; s++);
        } else {
          for(; s < ss && Token.lc(Token.cp(text, t + s)) == Token.cp(srch, s);
              s += Token.cl(srch, s));
        }
      }
      if(s == ss) {
        start.add(t);
        end.add(t + ss);
        t += ss;
      } else {
        t++;
      }
    }
  }

  /**
   * Replaces text.
   * @param text source text
   * @param replace replace text
   * @return new text
   */
  SearchResult replace(final byte[] text, final byte[] replace) {
    final SearchResult repl = new SearchResult();
    if(!start.isEmpty()) {
      final int ss = start.size();
      final ByteList bl = new ByteList();
      int s1 = 0;
      for(int p = 0; p < ss; p++) {
        final int s2 = start.get(p);
        bl.add(text, s1, s2);
        bl.add(replace);
        s1 = end.get(p);
      }
      bl.add(text, s1, text.length);
      repl.text = bl.toArray();
      repl.nr = ss;
    }
    return repl;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof SearchContext)) return false;
    final SearchContext s = (SearchContext) obj;
    return mcase == s.mcase && regex == s.regex && multi == s.multi &&
        Token.eq(search, s.search);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
