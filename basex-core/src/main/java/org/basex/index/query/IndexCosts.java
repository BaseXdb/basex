package org.basex.index.query;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Costs of index request.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexCosts implements Comparable<IndexCosts> {
  /** Enforce index creation (static query terms). */
  public static final IndexCosts ENFORCE_STATIC = new IndexCosts(-2, false);
  /** Enforce index creation (dynamic query terms). */
  public static final IndexCosts ENFORCE_DYNAMIC = new IndexCosts(-1, false);
  /** No results. */
  public static final IndexCosts ZERO = new IndexCosts(0, true);

  /**
   * Number of expected results.
   * 0 = no results,
   * 1 = one result (at most one if the number is not exact),
   * -1 = dynamic query, unknown (but enforced),
   * -2 = static query, unknown (but enforced),
   * other values: estimates (the smaller, the better)
   */
  private final int results;
  /** Indicates if the number of results is exact (otherwise, it is an estimate). */
  private final boolean exact;

  /**
   * Constructor.
   * @param results number of expected results
   * @param exact exact number of results
   */
  private IndexCosts(final int results, final boolean exact) {
    this.results = results;
    this.exact = exact;
  }

  /**
   * Returns costs with an estimated number of results (or an upper bound).
   * @param results number of expected results
   * @return costs
   */
  public static IndexCosts get(final int results) {
    return get(results, false);
  }

  /**
   * Returns costs with an exact number of results.
   * @param results number of results
   * @return costs
   */
  public static IndexCosts exact(final int results) {
    return get(results, true);
  }

  /**
   * Returns costs.
   * @param results number of expected results
   * @param exact exact number of results
   * @return costs
   */
  private static IndexCosts get(final int results, final boolean exact) {
    if(results < 0) throw Util.notExpected("Costs cannot be negative.");
    return results == 0 ? ZERO : new IndexCosts(results, exact);
  }

  /**
   * Adds index costs.
   * @param ic1 first costs (can be {@code null})
   * @param ic2 second costs (can be {@code null})
   * @return new costs
   */
  public static IndexCosts add(final IndexCosts ic1, final IndexCosts ic2) {
    final int r1 = ic1 == null ? 0 : ic1.results, r2 = ic2 == null ? 0 : ic2.results, r = r1 + r2;
    return r1 < 0 || r2 < 0 ? ENFORCE_DYNAMIC : get(r >= 0 ? r : Integer.MAX_VALUE,
      r >= 0 && (ic1 == null || ic1.exact) && (ic2 == null || ic2.exact));
  }

  /**
   * Number of expected results.
   * @return results
   */
  public int results() {
    return results;
  }

  /**
   * Returns the exact number of results.
   * @return number of results, or {@code -1} if it is unknown
   */
  public int size() {
    return exact ? results : -1;
  }

  /**
   * Checks if index access is too expensive.
   * @param data data reference
   * @return result of check
   */
  public boolean tooExpensive(final Data data) {
    return results > data.nodes();
  }

  @Override
  public int compareTo(final IndexCosts ic) {
    return Integer.signum(results - ic.results);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + results + ']';
  }
}
