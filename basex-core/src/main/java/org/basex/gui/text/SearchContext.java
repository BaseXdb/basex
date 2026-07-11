package org.basex.gui.text;

import java.util.regex.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class summarizes the result of a search.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SearchContext {
  /** Maximum number of hits. */
  private static final int MAX = 10000000;
  /** Word character: matches {@link Character#isLetterOrDigit(int)}. */
  private static final String WORD = "[\\p{L}\\p{Nd}]";

  /** Search bar. */
  final SearchBar bar;
  /** Search string. */
  final String string;
  /** Compiled pattern; {@code null} if the search string is empty or an invalid expression. */
  final Pattern pattern;
  /** Error message of an invalid regular expression ({@code null} otherwise). */
  final String error;

  /**
   * Constructor.
   * @param bar search bar
   * @param text search string
   */
  SearchContext(final SearchBar bar, final String text) {
    this(bar, text, bar.mcase.isSelected(), bar.word.isSelected(), bar.regex.isSelected(),
        bar.dotall.isSelected());
  }

  /**
   * Constructor.
   * @param bar search bar (can be {@code null} if the results are never displayed)
   * @param text search string
   * @param mcase match case
   * @param word whole word
   * @param regex regular expression
   * @param dotall dot matches all
   */
  SearchContext(final SearchBar bar, final String text, final boolean mcase, final boolean word,
      final boolean regex, final boolean dotall) {
    this.bar = bar;
    string = text;

    // compile pattern once; remember error message for invalid expressions
    Pattern pt = null;
    String err = null;
    if(!string.isEmpty()) {
      try {
        pt = pattern(string, mcase, word, regex, dotall);
      } catch(final PatternSyntaxException ex) {
        err = ex.getDescription();
      }
    }
    pattern = pt;
    error = err;
  }

  /**
   * Compiles the search string as a regular expression; literal input is quoted.
   * @param string search string
   * @param mcase match case
   * @param word whole word
   * @param regex regular expression
   * @param dotall dot matches all
   * @return compiled pattern
   */
  public static Pattern pattern(final String string, final boolean mcase, final boolean word,
      final boolean regex, final boolean dotall) {
    String pat;
    if(regex) {
      // anchor greedy leading wildcards to avoid quadratic backtracking; lazy ones would drop hits
      // only a leading wildcard may be anchored: in (.*a)?b, ^ would bind to the whole expression
      pat = (string.startsWith(".*") || string.startsWith(".+")) &&
          !string.startsWith(".*?") && !string.startsWith(".+?") ? '^' + string : string;
    } else {
      pat = Pattern.quote(string);
    }
    // the non-capturing group binds the boundaries to the whole expression, not to (a|b) branches
    // the optional low surrogate widens the lookbehind: a single char would be a non-word surrogate
    if(word) pat = "(?<!" + WORD + "[\\uDC00-\\uDFFF]?)(?:" + pat + ")(?!" + WORD + ")";
    // ^/$ anchor per line; . matches newlines only in dot-all mode
    int flags = Pattern.MULTILINE;
    if(dotall) flags |= Pattern.DOTALL;
    // UNICODE_CASE: fold non-ASCII characters as well
    if(!mcase) flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    return Pattern.compile(pat, flags);
  }

  /**
   * Performs the search.
   * @param txt text to be searched
   * @param str decoded text (must be equivalent to {@code txt})
   * @return result positions
   */
  IntList[] search(final byte[] txt, final String str) {
    final IntList start = new IntList(), end = new IntList();
    if(pattern != null) {
      final TextCursor cursor = new TextCursor(txt);
      final Matcher m = pattern.matcher(new StoppableString(str));
      while(m.find()) {
        start.add(cursor.advance(m.start()));
        end.add(cursor.advance(m.end()));
        if(start.size() >= MAX) break;
      }
    }
    StoppableString.checkStop();
    return new IntList[] { start, end };
  }

  /**
   * Checks if the specified string matches the search string.
   * @param str string to be checked
   * @return result of check
   */
  boolean matches(final String str) {
    // ignore empty strings and others that stretch over multiple lines
    if(str.isEmpty() || str.contains("\n")) return true;
    return pattern != null && pattern.matcher(str).matches();
  }
}
