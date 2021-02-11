package org.basex.query.util.regex;

/**
 * Wildcard for any character ({@code .}).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class Wildcard extends RegExp {
  /** Instance for the dot matching all characters. */
  private static final Wildcard ALL = new Wildcard(true);
  /** Instance for the dot matching everything except new-lines. */
  private static final Wildcard NOLF  = new Wildcard(true);

  /** If the {@code \n} character is matched. */
  private final boolean nl;

  /**
   * Private constructor.
   * @param nl match-all flag
   */
  private Wildcard(final boolean nl) {
    this.nl = nl;
  }

  /**
   * Getter for the lazily initialized wildcard instances.
   * @param dotAll match-all flag
   * @return the instance
   */
  public static Wildcard get(final boolean dotAll) {
    return dotAll ? ALL : NOLF;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(nl ? "." : "[^\r\n]");
  }
}
