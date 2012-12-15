package org.basex.gui.editor;

import static org.basex.util.Token.*;

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
final class SearchContext {
  /** Mode: match case. */
  final boolean mcase;
  /** Mode: regular expression. */
  final boolean regex;
  /** Mode: multi-line. */
  final boolean multi;
  /** Mode: whole word. */
  final boolean word;
  /** Search string. */
  final String search;
  /** Number of results. */
  int nr;

  /**
   * Constructor.
   * @param panel search panel
   * @param srch search text
   */
  SearchContext(final SearchPanel panel, final String srch) {
    mcase = panel.mcase.isSelected();
    word = panel.word.isSelected();
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
   * @return result positions
   */
  IntList[] search(final byte[] txt) {
    final IntList start = new IntList();
    final IntList end = new IntList();
    if(search.isEmpty()) return new IntList[] { start, end };

    final byte[] text = txt;
    if(regex) {
      int flags = Pattern.DOTALL;
      if(!mcase) flags |= Pattern.CASE_INSENSITIVE;
      final Pattern pattern = Pattern.compile(search, flags);
      if(multi) {
        int c = 0, p = 0;
        final Matcher m = pattern.matcher(string(text));
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
        }
      } else {
        final int os = text.length;
        final TokenBuilder tb = new TokenBuilder(os);
        for(int t = 0, o = 0; o <= os; o++) {
          if(o < os ? text[o] == '\n' : o != t) {
            int c = 0, p = t;
            final Matcher m = pattern.matcher(string(text, t, o - t));
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
            }
            if(o < os) tb.add('\n');
            t = o + 1;
          }
        }
      }
    } else {
      final byte[] srch = token(search);
      final int ss = srch.length, os = text.length;
      boolean s = true;
      for(int o = 0; o < os;) {
        int sp = 0;
        if(o + ss <= os && s) {
          if(mcase) {
            while(sp < ss && txt[o + sp] == srch[sp]) sp++;
          } else {
            while(sp < ss && lc(cp(txt, o + sp)) == cp(srch, sp)) sp += cl(srch, sp);
          }
        }
        if(sp == ss && (!word || o + ss == os ||
            !Character.isLetterOrDigit(cp(txt, o + ss)))) {
          start.add(o);
          end.add(o + ss);
          o += ss;
          s = !word;
        } else if(word) {
          s = !Character.isLetterOrDigit(cp(txt, o));
          o += cl(txt, o);
        } else {
          o++;
        }
      }
    }
    nr = start.size();
    return new IntList[] { start, end };
  }

  /**
   * Checks if the specified string matches the search string.
   * @param string string to be checked
   * @return result of check
   */
  boolean matches(final String string) {
    // ignore empty strings and others that stretch over multiple lines
    if(string.isEmpty() || string.contains("\n")) return true;

    if(regex) {
      try {
        int flags = Pattern.DOTALL;
        if(!mcase) flags |= Pattern.CASE_INSENSITIVE;
        final Pattern pattern = Pattern.compile(search, flags);
        return pattern.matcher(string).matches();
      } catch(final Exception ex) {
        return false;
      }
    }
    return mcase ? search.equals(string) : search.equalsIgnoreCase(string);
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
    if(!(obj instanceof SearchContext)) return false;
    final SearchContext s = (SearchContext) obj;
    return mcase == s.mcase && word == s.word && regex == s.regex && multi == s.multi &&
        eq(search, s.search);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
