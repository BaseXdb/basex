package org.basex.query.util.index;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Costs of index request.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IndexCosts implements Comparable<IndexCosts> {
  /** Enforce index creation (static query terms). */
  public static final IndexCosts ENFORCE_STATIC = new IndexCosts(-2);
  /** Enforce index creation (dynamic query terms). */
  public static final IndexCosts ENFORCE_DYNAMIC = new IndexCosts(-1);
  /** No results. */
  public static final IndexCosts ZERO = new IndexCosts(0);

  /**
   * Number of expected results.
   * 0 = no results,
   * 1 = exactly one result,
   * -1 = dynamic query, unknown (but enforced),
   * -2 = static query, unknown (but enforced),
   * other values: estimates (the smaller, the better)
   */
  private final int results;

  /**
   * Constructor.
   * @param results number of expected results
   */
  private IndexCosts(final int results) {
    this.results = results;
  }

  /**
   * Constructor.
   * @param results number of expected results
   * @return costs
   */
  public static IndexCosts get(final int results) {
    if(results < 0) throw Util.notExpected("Costs cannot be negative.");
    return results == 0 ? ZERO : new IndexCosts(results);
  }

  /**
   * Adds index costs.
   * @param ic1 first costs
   * @param ic2 second costs
   * @return new costs
   */
  public static IndexCosts add(final IndexCosts ic1, final IndexCosts ic2) {
    final int r1 = ic1 == null ? 0 : ic1.results, r2 = ic2 == null ? 0 : ic2.results, r = r1 + r2;
    return r1 < 0 || r2 < 0 ? ENFORCE_DYNAMIC : get(r >= 0 ? r : Integer.MAX_VALUE);
  }

  /**
   * Number of expected results.
   * @return results
   */
  public int results() {
    return results;
  }

  /**
   * Checks if index access is too expensive.
   * @param data data reference (can be {@code null})
   * @return result of check
   */
  public boolean tooExpensive(final Data data) {
    return results > data.meta.size;
  }

  @Override
  public int compareTo(final IndexCosts ic) {
    return results - ic.results;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + results + ']';
  }
}
