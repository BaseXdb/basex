package org.basex.query.func.xquery;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * Options for running parallelized queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TaskOptions extends Options {
  /** Parallel threads. */
  public static final NumberOption PARALLEL = new NumberOption("parallel", 0);
  /** Handle errors. */
  public static final BooleanOption ERRORS = new BooleanOption("errors", true);
  /** Collect results. */
  public static final BooleanOption RESULTS = new BooleanOption("results", true);
  /** Report results and errors. */
  public static final BooleanOption REPORT = new BooleanOption("report", false);
  /** Timeout in seconds. */
  public static final ValueOption TIMEOUT =
      new ValueOption("timeout", Types.DECIMAL_O, Dec.ZERO);

  /**
   * Returns the number of maximum parallel threads. A value of {@code 0} indicates that the
   * shared thread pool with the default level of parallelism is to be used.
   * @return number of parallel threads, or {@code 0} for the default
   */
  public int parallel() {
    final int p = get(PARALLEL);
    return p < 1 ? 0 : Math.min(0x7FFF, p);
  }

  /**
   * Returns the result type of a task.
   * @param ft type of the invoked functions (can be {@code null})
   * @param occ number of function calls
   * @return result type (can be {@code null})
   */
  SeqType type(final FuncType ft, final Occ occ) {
    // one report record per function call
    if(get(REPORT)) return Types.MAP_O.with(occ);
    if(!get(RESULTS)) return Types.EMPTY_SEQUENCE_Z;
    if(ft == null) return null;
    // failed function calls contribute no results if errors are ignored
    final SeqType type = ft.refinedType;
    final Occ total = type.occ.multiply(occ);
    return type.with(get(ERRORS) ? total : total.union(Occ.ZERO));
  }
}
