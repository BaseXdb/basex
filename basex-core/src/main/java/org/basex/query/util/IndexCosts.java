package org.basex.query.util;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Costs of index request.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class IndexCosts implements Comparable<IndexCosts> {
  /** Enforce index creation. */
  public static final IndexCosts ENFORCE = new IndexCosts(-1);
  /** No results. */
  public static final IndexCosts ZERO = new IndexCosts(0);

  /**
   * Number of expected results.
   * 0 = no results,
   * 1 = exactly one result,
   * -1 = unknown,
   * other values: estimates (the smaller, the better)
   */
  private int results = -1;

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
    if(ic1 == ENFORCE || ic2 == ENFORCE) return ENFORCE;
    final int r = (ic1 == null ? 0 : ic1.results) + (ic2 == null ? 0 : ic2.results);
    return get(r >= 0 ? r : Integer.MAX_VALUE);
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
    return this != ENFORCE && results > data.meta.size;
  }

  @Override
  public int compareTo(final IndexCosts ic) {
    return this == ENFORCE ? ic == ENFORCE ? 0 : -1 : ic == ENFORCE ? 1 : results - ic.results;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this)).append('[');
    if(this == ENFORCE) sb.append("always");
    else sb.append(results);
    return sb.append(']').toString();
  }
}
