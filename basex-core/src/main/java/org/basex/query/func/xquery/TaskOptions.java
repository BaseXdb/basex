package org.basex.query.func.xquery;

import org.basex.util.options.*;

/**
 * Options for running asynchronous queries.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class TaskOptions extends Options {
  /** Parallel threads. */
  public static final NumberOption PARALLEL = new NumberOption("parallel", 0);
  /** Handle errors. */
  public static final BooleanOption ERRORS = new BooleanOption("errors", true);
  /** Collect results. */
  public static final BooleanOption RESULTS = new BooleanOption("results", true);

  /**
   * Returns the normalized number of maximum parallel threads.
   * @return number of parallel threads
   */
  public int parallel() {
    final int p = get(PARALLEL);
    return p < 1 ? Runtime.getRuntime().availableProcessors() : Math.min(0x7FFF, p);
  }
}
