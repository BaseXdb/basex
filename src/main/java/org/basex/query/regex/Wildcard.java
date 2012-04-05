package org.basex.query.regex;

/**
 * Wildcard for any character ({@code .}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Wildcard extends RegExp {
  /** Instance for the dot matching all characters. */
  private static Wildcard all;
  /** Instance for the dot matching everything except new-lines. */
  private static Wildcard noLf;

  /** If the {@code \n} character is matched. */
  private final boolean nl;

  /**
   * Private constructor.
   * @param a match-all flag
   */
  private Wildcard(final boolean a) {
    nl = a;
  }

  /**
   * Getter for the lazily initialized wildcard instances.
   * @param dotAll match-all flag
   * @return the instance
   */
  public static Wildcard get(final boolean dotAll) {
    if(dotAll) {
      if(all == null) all = new Wildcard(true);
      return all;
    }
    if(noLf == null) noLf = new Wildcard(false);
    return noLf;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    return sb.append(nl ? "." : "[^\r\n]");
  }
}
