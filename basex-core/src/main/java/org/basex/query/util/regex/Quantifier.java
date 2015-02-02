package org.basex.query.util.regex;

/**
 * A quantifier, like {@code ?}, {@code *} or {@code &#x7b;17,123&#x7d;}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class Quantifier extends RegExp {
  /** Minimum occurrence. */
  private final int min;
  /** Maximum occurrence, {@code -1} for infinity. */
  private final int max;
  /** Reluctance flag. */
  private final boolean lazy;

  /**
   * Constructor.
   * @param min minimum occurrences
   * @param max maximum occurrences
   * @param lazy reluctance flag
   */
  public Quantifier(final int min, final int max, final boolean lazy) {
    this.min = min;
    this.max = max;
    this.lazy = lazy;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    return sb.append(string()).append(lazy ? "?" : "");
  }

  /**
   * Translates the occurrence bounds to a regex string.
   * @return regex representation
   */
  private String string() {
    if(min == 0) {
      if(max == 1) return "?";
      if(max == -1) return "*";
    } else if(min == 1 && max == -1) return "+";
    return "{" + min + (min == max ? "" : "," + (max == -1 ? "" : max)) + '}';
  }
}
