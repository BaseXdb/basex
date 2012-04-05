package org.basex.query.regex;

/**
 * A quantifier, like {@code ?}, {@code *} or {@code &#x7b;17,123&#x7d;}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class Quantifier extends RegExp {
  /** Minimum occurrence. */
  public final int min;
  /** Maximum occurrence, {@code -1} for ∞. */
  public final int max;
  /** Reluctance flag. */
  public final boolean lazy;

  /**
   * Constructor.
   * @param mn minimum occurrences
   * @param mx maximum occurrences
   * @param lzy reluctance flag
   */
  public Quantifier(final int mn, final int mx, final boolean lzy) {
    min = mn;
    max = mx;
    lazy = lzy;
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
    return "{" + min + (min == max ? "" : "," + (max == -1 ? "" : max)) + "}";
  }
}
