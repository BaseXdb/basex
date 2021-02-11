package org.basex.query.value.type;

/**
 * Occurrence indicator (cardinality).
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param min minimal result size
   * @param max maximal result size
   * @param string string representation
   */
  Occ(final long min, final long max, final String string) {
    this.min = min;
    this.max = max;
    this.string = string;
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
    final long mn = Math.max(min, other.min), mx = Math.min(max, other.max);
    return mx < mn ? null : mx == 0 ? ZERO : mn == mx ? EXACTLY_ONE : mx == 1 ? ZERO_OR_ONE :
      mn == 0 ? ZERO_OR_MORE : ONE_OR_MORE;
  }

  /**
   * Computes the union between this occurrence indicator and the given one.
   * @param other other occurrence indicator
   * @return union
   */
  public Occ union(final Occ other) {
    final long mn = Math.min(min, other.min), mx = Math.max(max, other.max);
    return mx == 0 ? ZERO : mn == mx ? EXACTLY_ONE : mx == 1 ? ZERO_OR_ONE :
      mn == 0 ? ZERO_OR_MORE : ONE_OR_MORE;
  }

  /**
   * Adds two occurrence indicators.
   * @param other other occurrence indicator
   * @return union
   */
  public Occ add(final Occ other) {
    final long mn = min + other.min, mx = max + other.max;
    return mx == 0 ? ZERO : mx == 1 ? mn == 0 ? ZERO_OR_ONE : EXACTLY_ONE :
      mn == 0 ? ZERO_OR_MORE : ONE_OR_MORE;
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

