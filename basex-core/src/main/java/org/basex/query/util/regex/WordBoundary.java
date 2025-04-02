package org.basex.query.util.regex;

import org.basex.util.*;

/**
 * Word-boundary ({@code \b}) or non-word-boundary ({@code \B}) assertion.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class WordBoundary extends RegExp {
  /** Image. */
  private final String img;
  /** Cached instances. */
  private static final WordBoundary[] INSTANCES = new WordBoundary[4];

  /**
   * Constructor.
   * @param positive true for word-boundary, false for non-word-boundary
   * @param multi multi-line flag
   */
  private WordBoundary(final boolean positive, final boolean multi) {
    final String eol = LineBorder.eos(multi);
    if(positive) {
      img = "(?:(?<=["      + Escape.WORD     + "])(?:(?=[" + Escape.NOT_WORD + "])|" + eol + ")"
          +   "|(?<=(?:^|[" + Escape.NOT_WORD + "]))(?=["   + Escape.WORD     + "])"
          + ")";
    } else {
      img = "(?:(?<=["      + Escape.WORD     + "])(?=["     + Escape.WORD     + "])"
          +   "|(?<=(?:^|[" + Escape.NOT_WORD + "]))(?:(?=[" + Escape.NOT_WORD + "])|" + eol + ")"
          +   "|^" + eol
          + ")";
    }
  }

  /**
   * Creates a regular expression from the given word boundary escape sequence.
   * @param esc escape sequence
   * @param multi multi-line flag
   * @return regular expression
   */
  public static WordBoundary get(final String esc, final boolean multi) {
    final boolean positive;
    switch(esc.charAt(1)) {
      case 'b': positive = true;  break;
      case 'B': positive = false; break;
      default: throw Util.notExpected();
    }
    final int pos = (positive ? 2 : 0) + (multi ? 1 : 0);
    if(INSTANCES[pos] == null) INSTANCES[pos] = new WordBoundary(positive, multi);
    return INSTANCES[pos];
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(img);
  }
}
