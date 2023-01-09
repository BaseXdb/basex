package org.basex.query.value.type;

/**
 * Occurrence indicator (cardinality).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public enum Occ {
  /** Zero.         */ ZERO(0, 0, ""),
  /** Zero or one.  */ ZERO_OR_ONE(0, 1, "?"),
  /** Exactly one.  */ EXACTLY_ONE(1, 1, ""),
  /** One or more.  */ ONE_OR_MORE(1, Long.MAX_VALUE, "+"),
  /** Zero or more. */ ZERO_OR_MORE(0, Long.MAX_VALUE, "*");

  /** Minimal result size ({@code 0} or more). */
  public final long min;
  /** Maximal result size (equal to {@link #min} or more). */
  public final long max;
  /** String representation. */
  private final String string;

  /**
   * Constructor.
   * @param min minimal result size (0, 1 or larger than 1)
   * @param max maximal result size (0, 1 or larger than 1)
   * @param string string representation
   */
  Occ(final long min, final long max, final String string) {
    this.min = min;
    this.max = max;
    this.string = string;
  }

  /**
   * Returns a new occurrence.
   * @param min minimum occurrence
   * @param max maximum occurrence
   * @return occurrence
   */
  public static Occ get(final long min, final long max) {
    return max < min ? null :
      max == 0 ? ZERO :
      max == 1 ? min == 0 ? ZERO_OR_ONE : EXACTLY_ONE :
      min == 0 ? ZERO_OR_MORE : ONE_OR_MORE;
  }

  /**
   * Checks if the specified occurrence indicator is an instance of the current occurrence
   * indicator.
   * @param occ occurrence indicator to check
   * @return result of check
   */
  public boolean instanceOf(final Occ occ) {
    return min >= occ.min && max <= occ.max;
  }

  /**
   * Computes the intersection between this occurrence indicator and the given one.
   * If none exists (e.g. between {@link #ZERO} and {@link #EXACTLY_ONE}), {@code null} is returned.
   * @param other other occurrence indicator
   * @return intersection or {@code null}
   */
  public Occ intersect(final Occ other) {
    return get(Math.max(min, other.min), Math.min(max, other.max));
  }

  /**
   * Computes the union between this occurrence indicator and the given one.
   * @param other other occurrence indicator
   * @return union
   */
  public Occ union(final Occ other) {
    return get(Math.min(min, other.min), Math.max(max, other.max));
  }

  /**
   * Adds two occurrence indicators.
   * @param other other occurrence indicator
   * @return union
   */
  public Occ add(final Occ other) {
    return get(min + other.min, Math.min(2, max) + Math.min(2, other.max));
  }

  /**
   * Multiplies two occurrence indicators.
   * @param other other occurrence indicator
   * @return union
   */
  public Occ multiply(final Occ other) {
    return get(min * other.min, Math.min(2, max) * Math.min(2, other.max));
  }

  /**
   * Checks if the given cardinality is supported by this type.
   * @param card cardinality
   * @return result of check
   */
  public boolean check(final long card) {
    return min <= card && card <= max;
  }

  @Override
  public String toString() {
    return string;
  }
}

