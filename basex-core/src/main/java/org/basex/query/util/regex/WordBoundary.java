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
  private static final WordBoundary[] INSTANCES = new WordBoundary[2];

  /**
   * Constructor.
   * @param positive true for word-boundary, false for non-word-boundary
   */
  private WordBoundary(final boolean positive) {
    // boundaries are defined by the start and end of the string, not of lines
    if(positive) {
      img = "(?:(?<="      + Escape.WORD     + ")(?:(?=" + Escape.NOT_WORD + ")|\\z)"
          +   "|(?<=\\A|" + Escape.NOT_WORD + ")(?="    + Escape.WORD     + "))";
    } else {
      img = "(?:(?<="      + Escape.WORD     + ")(?="     + Escape.WORD     + ")"
          +   "|(?<=\\A|" + Escape.NOT_WORD + ")(?:(?=" + Escape.NOT_WORD + ")|\\z))";
    }
  }

  /**
   * Creates a regular expression from the given word boundary escape sequence.
   * @param esc escape sequence
   * @return regular expression
   */
  public static WordBoundary get(final String esc) {
    final boolean positive = switch(esc.charAt(1)) {
      case 'b' -> true;
      case 'B' -> false;
      default  -> throw Util.notExpected();
    };
    final int pos = positive ? 1 : 0;
    if(INSTANCES[pos] == null) INSTANCES[pos] = new WordBoundary(positive);
    return INSTANCES[pos];
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(img);
  }
}
