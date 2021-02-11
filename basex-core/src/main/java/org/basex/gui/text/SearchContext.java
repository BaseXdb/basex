package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class summarizes the result of a search.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class SearchContext {
  /** Maximum number of hits. */
  private static final int MAX = 10000000;

  /** Search bar. */
  final SearchBar bar;
  /** Mode: match case. */
  final boolean mcase;
  /** Mode: regular expression. */
  final boolean regex;
  /** Mode: multi-line. */
  final boolean multi;
  /** Mode: whole word. */
  final boolean word;
  /** Search string. */
  final String string;
  /** Number of results. */
  int nr;

  /**
   * Constructor.
   * @param bar search bar
   * @param text search string
   */
  SearchContext(final SearchBar bar, final String text) {
    this.bar = bar;
    mcase = bar.mcase.isSelected();
    word = bar.word.isSelected();
    regex = bar.regex.isSelected();
    multi = bar.multi.isSelected();
    String srch = mcase ? text : text.toLowerCase(Locale.ENGLISH);
    // speed up regular expressions starting with wildcards
    if(regex && (srch.startsWith(".*") || srch.startsWith("(.*") ||
        srch.startsWith(".+") || srch.startsWith("(.+"))) srch = '^' + srch;
    string = srch;
  }

  /**
   * Performs the search and refreshes the editor.
   * @param txt text to be searched
   * @param jump jump to next search result
   * @return result positions
   */
  IntList[] search(final byte[] txt, final boolean jump) {
    final IntList start = new IntList(), end = new IntList();
    if(!string.isEmpty()) {
      if(regex) searchRegEx(start, end, txt);
      else searchSimple(start, end, txt);
    }
    InterruptibleString.checkStop();
    nr = start.size();
    bar.refresh(this, jump);
    return new IntList[] { start, end };
  }

  /**
   * Runs a query using regular expressions.
   * @param start start positions
   * @param end end positions
   * @param text text to be searched
   */
  private void searchRegEx(final IntList start, final IntList end, final byte[] text) {
    int flags = Pattern.DOTALL;
    if(!mcase) flags |= Pattern.CASE_INSENSITIVE;
    final Pattern pattern = Pattern.compile(string, flags);
    if(multi) {
      int c = 0, p = 0;
      final Matcher m = pattern.matcher(new InterruptibleString(string(text)));
      while(m.find()) {
        final int s = m.start(), e = m.end();
        while(c < s) {
          p += cl(text, p);
          c++;
        }
        start.add(p);
        while(c < e) {
          p += cl(text, p);
          c++;
        }
        end.add(p);
        if(start.size() >= MAX) return;
      }
    } else {
      final int os = text.length;
      final TokenBuilder tb = new TokenBuilder(os);
      for(int t = 0, o = 0; o <= os; o++) {
        InterruptibleString.checkStop();
        if(o < os ? text[o] == '\n' : o != t) {
          int c = 0, p = t;
          final Matcher m = pattern.matcher(new InterruptibleString(string(text, t, o - t)));
          while(m.find()) {
            final int s = m.start(), e = m.end();
            while(c < s) {
              p += cl(text, p);
              c++;
            }
            start.add(p);
            while(c < e) {
              p += cl(text, p);
              c++;
            }
            end.add(p);
            if(start.size() >= MAX) return;
          }
          if(o < os) tb.add('\n');
          t = o + 1;
        }
      }
    }
  }

  /**
   * Runs a simple query.
   * @param start start positions
   * @param end end positions
   * @param text text to be searched
   */
  private void searchSimple(final IntList start, final IntList end, final byte[] text) {
    final byte[] srch = token(string);
    final int sl = srch.length, tl = text.length;
    boolean s = true;
    for(int t = 0; t < tl;) {
      InterruptibleString.checkStop();
      int sp = 0;
      if(t + sl <= tl && s) {
        if(mcase) {
          while(sp < sl && text[t + sp] == srch[sp]) sp++;
        } else {
          while(sp < sl && lc(cp(text, t + sp)) == cp(srch, sp)) sp += cl(srch, sp);
        }
      }
      if(sp == sl && (!word || t + sl == tl || !Character.isLetterOrDigit(cp(text, t + sl)))) {
        start.add(t);
        end.add(t + sl);
        if(start.size() >= MAX) return;
        t += sl;
        s = !word;
      } else if(word) {
        s = !Character.isLetterOrDigit(cp(text, t));
        t += cl(text, t);
      } else {
        t++;
      }
    }
  }

  /**
   * Checks if the specified string matches the search string.
   * @param str string to be checked
   * @return result of check
   */
  boolean matches(final String str) {
    // ignore empty strings and others that stretch over multiple lines
    if(str.isEmpty() || str.contains("\n")) return true;

    if(regex) {
      try {
        int flags = Pattern.DOTALL;
        if(!mcase) flags |= Pattern.CASE_INSENSITIVE;
        final Pattern pattern = Pattern.compile(string, flags);
        return pattern.matcher(str).matches();
      } catch(final Exception ex) {
        Util.debug(ex);
        return false;
      }
    }
    return mcase ? string.equals(str) : string.equalsIgnoreCase(str);
  }

  /**
   * Returns the number of results.
   * @return number of results
   */
  int nr() {
    return nr;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof SearchContext)) return false;
    final SearchContext sc = (SearchContext) obj;
    return mcase == sc.mcase && word == sc.word && regex == sc.regex &&
        multi == sc.multi && Strings.eq(string, sc.string);
  }
}
