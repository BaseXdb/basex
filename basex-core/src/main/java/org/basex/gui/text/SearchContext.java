package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class summarizes the result of a search.
 *
 * @author BaseX Team, BSD License
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
  /** Mode: dot matches newline. */
  final boolean dotall;
  /** Mode: whole word. */
  final boolean word;
  /** Search string. */
  final String string;
  /** Compiled pattern (regex mode only; {@code null} if absent or invalid). */
  final Pattern pattern;
  /** Error message of an invalid regular expression ({@code null} otherwise). */
  final String error;
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
    dotall = bar.dotall.isSelected();
    // do not case-fold regular expressions: the pattern may contain case-sensitive
    // metacharacters (\D, \S, \Q…\E, …); case-insensitivity is applied via the flag below
    string = mcase || regex ? text : text.toLowerCase(Locale.ENGLISH);

    // compile pattern once; remember error message for invalid expressions
    Pattern pt = null;
    String err = null;
    if(regex && !string.isEmpty()) {
      try {
        pt = pattern(string, mcase, dotall);
      } catch(final PatternSyntaxException ex) {
        err = ex.getDescription();
      }
    }
    pattern = pt;
    error = err;
  }

  /**
   * Compiles a regular expression as used by the search.
   * @param string search string
   * @param mcase match case
   * @param dotall dot matches newline
   * @return compiled pattern
   */
  static Pattern pattern(final String string, final boolean mcase, final boolean dotall) {
    // anchor greedy leading wildcards (.* / .+) to avoid quadratic backtracking on long lines;
    // skip lazy quantifiers (.*? / .+?), which would drop matches after the first.
    // only a wildcard at the very start may be anchored: inside a group, the anchor would also
    // bind to constructs that have no leading wildcard, such as (.*a)?b or (.*a|b)
    final String pat = (string.startsWith(".*") || string.startsWith(".+")) &&
        !string.startsWith(".*?") && !string.startsWith(".+?") ? '^' + string : string;
    // ^/$ anchor per line; . matches newlines only in dot-all mode
    int flags = Pattern.MULTILINE;
    if(dotall) flags |= Pattern.DOTALL;
    // UNICODE_CASE: fold non-ASCII characters as well, as the simple search does
    if(!mcase) flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    return Pattern.compile(pat, flags);
  }

  /**
   * Performs the search and refreshes the editor.
   * @param txt text to be searched
   * @param jump jump to next search result
   * @return result positions
   */
  IntList[] search(final byte[] txt, final boolean jump) {
    final IntList start = new IntList(), end = new IntList();
    if(!string.isEmpty() && error == null) {
      if(regex) searchRegEx(start, end, txt);
      else searchSimple(start, end, txt);
    }
    StoppableString.checkStop();
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
    // match against the whole text: ^/$ anchor per line, . spans lines only in dot-all mode.
    // c: char (UTF-16) index into the matched string; p: byte offset into text.
    // A 4-byte UTF-8 sequence is a supplementary code point, i.e. two UTF-16 chars.
    int c = 0, p = 0;
    final Matcher m = pattern.matcher(new StoppableString(string(text)));
    while(m.find()) {
      final int s = m.start(), e = m.end();
      while(c < s) {
        final int bl = cl(text, p);
        p += bl;
        c += bl == 4 ? 2 : 1;
      }
      start.add(p);
      while(c < e) {
        final int bl = cl(text, p);
        p += bl;
        c += bl == 4 ? 2 : 1;
      }
      end.add(p);
      if(start.size() >= MAX) return;
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
      StoppableString.checkStop();
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

    if(regex) return pattern != null && pattern.matcher(str).matches();
    return mcase ? string.equals(str) : string.equalsIgnoreCase(str);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    return obj instanceof final SearchContext sc && mcase == sc.mcase && word == sc.word &&
        regex == sc.regex && dotall == sc.dotall && Strings.eq(string, sc.string);
  }
}
