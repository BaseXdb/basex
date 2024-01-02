package org.basex.query.util.index;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;

/**
 * Index predicate.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class IndexPred {
  /** Index info. */
  final IndexInfo info;

  /**
   * Constructor.
   * @param info index info
   */
  IndexPred(final IndexInfo info) {
    this.info = info;
  }

  /**
   * Creates an index predicate instance.
   * @param expr predicate expression
   * @param info index info
   * @return index predicate or {@code null}
   */
  static IndexPred get(final Expr expr, final IndexInfo info) {
    if(expr instanceof ContextValue) return new IndexContext(info);
    if(expr instanceof AxisPath) return new IndexPath((AxisPath) expr, info);
    return null;
  }

  /**
   * Returns the most specific step in the path that points to the index values.
   * @return step or {@code null}
   */
  abstract Step step();

  /**
   * Returns the step pointing to the element or attribute node.
   * @return step with name
   */
  abstract Step qname();

  /**
   * Rewrites an inverted path expression.
   * @param root new root expression
   * @return path
   * @throws QueryException query exception
   */
  abstract Expr invert(Expr root) throws QueryException;
}
