package org.basex.gui.editor;

import java.util.*;
import java.util.regex.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class summarizes the result of a search.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class SearchContext {
  /** Mode: match case. */
  final boolean mcase;
  /** Mode: regular expression. */
  final boolean regex;
  /** Mode: multi-line. */
  final boolean multi;
  /** Search string. */
  final String search;

  /** Start positions. */
  IntList start = new IntList();
  /** End positions. */
  IntList end = new IntList();

  /**
   * Constructor.
   * @param panel search panel
   * @param srch search text
   */
  SearchContext(final SearchPanel panel, final String srch) {
    mcase = panel.mcase.isSelected();
    regex = panel.regex.isSelected();
    multi = panel.multi.isSelected();
    String s = mcase ? srch : srch.toLowerCase(Locale.ENGLISH);
    // speed up regular expressions starting with wildcards
    if(regex && (s.startsWith(".*") || s.startsWith("(.*") ||
        s.startsWith(".+") || s.startsWith("(.+"))) s = "^" + s;
    search = s;
  }

  /**
   * Performs the search.
   * @param txt text to be searched
   */
  void search(final byte[] txt) {
    start.reset();
    end.reset();

    final byte[] text = txt;
    if(regex) {
      int flags = Pattern.DOTALL;
      if(!mcase) flags |= Pattern.CASE_INSENSITIVE;
      final Pattern pattern = Pattern.compile(search, flags);
      if(multi) {
        int c = 0, p = 0;
        final Matcher m = pattern.matcher(Token.string(text));
        while(m.find()) {
          final int s = m.start(), e = m.end();
          while(c < s) {
            p += Token.cl(text, p);
            c++;
          }
          start.add(p);
          while(c < e) {
            p += Token.cl(text, p);
            c++;
          }
          end.add(p);
        }
      } else {
        final int os = text.length;
        final TokenBuilder tb = new TokenBuilder(os);
        for(int t = 0, o = 0; o <= os; o++) {
          if(o < os ? text[o] == '\n' : o != t) {
            int c = 0, p = t;
            final Matcher m = pattern.matcher(Token.string(text, t, o - t));
            while(m.find()) {
              final int s = m.start(), e = m.end();
              while(c < s) {
                p += Token.cl(text, p);
                c++;
              }
              start.add(p);
              while(c < e) {
                p += Token.cl(text, p);
                c++;
              }
              end.add(p);
            }
            if(o < os) tb.add('\n');
            t = o + 1;
          }
        }
      }
    } else {
      final byte[] srch = Token.token(search);
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
  }

  /**
   * Returns the number of results.
   * @return number of results
   */
  int nr() {
    return start.size();
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
