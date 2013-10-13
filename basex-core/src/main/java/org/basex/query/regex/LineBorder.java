package org.basex.query.regex;

/**
 * Line start ({@code ^}) or line end ({@code $}).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class LineBorder extends RegExp {
  /** Start flag. */
  private final boolean start;
  /** Multi-line flag. */
  private final boolean multi;
  /** Cached instances. */
  private static final LineBorder[] INSTANCES = new LineBorder[4];

  /**
   * Constructor.
   * @param st start flag
   * @param mul multi-line flag
   */
  private LineBorder(final boolean st, final boolean mul) {
    start = st;
    multi = mul;
  }

  /**
   * Getter for the cached LineBorder instance.
   * @param start {@code ^} if {@code true}, {@code $} otherwise
   * @param multi multi-line flag
   * @return the instance
   */
  public static LineBorder get(final boolean start, final boolean multi) {
    final int pos = (start ? 2 : 0) + (multi ? 1 : 0);
    if(INSTANCES[pos] == null) INSTANCES[pos] = new LineBorder(start, multi);
    return INSTANCES[pos];
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    return sb.append(start ? "^" : multi ? "$" : "(?:$(?!\\s))");
  }
}
