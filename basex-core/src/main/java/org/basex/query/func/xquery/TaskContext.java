package org.basex.query.func.xquery;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Context for XQuery tasks.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class TaskContext {
  /** Functions to evaluate in parallel. */
  final ArrayList<FItem> funcs;
  /** Input info (can be {@code null}). */
  final InputInfo info;
  /** Query context. */
  final QueryContext qc;
  /** Handle errors. */
  final boolean errors;
  /** Collect results. */
  final boolean results;

  /**
   * Constructor.
   * @param funcs functions to evaluate
   * @param options task options
   * @param qc query context
   * @param info input info (can be {@code null})
   */
  TaskContext(final ArrayList<FItem> funcs, final TaskOptions options, final QueryContext qc,
      final InputInfo info) {
    this.funcs = funcs;
    this.info = info;
    this.qc = qc;
    this.errors = options.get(TaskOptions.ERRORS);
    this.results = options.get(TaskOptions.RESULTS);
  }
}
