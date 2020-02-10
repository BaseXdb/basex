package org.basex.query.util.index;

import org.basex.query.expr.*;
import org.basex.query.expr.path.*;

/**
 * Index predicate.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
abstract class IndexPred {
  /**
   * Creates an index predicate instance.
   * @param expr predicate expression
   * @return index predicate or {@code null}
   */
  static IndexPred get(final Expr expr) {
    if(expr instanceof ContextValue) return new IndexContext();
    if(expr instanceof AxisPath) return new IndexPath((AxisPath) expr);
    return null;
  }

  /**
   * Returns the most specific step in the path that points to the index values.
   * @param ii index info
   * @return step or {@code null}
   */
  abstract Step step(IndexInfo ii);

  /**
   * Returns the step pointing to the element or attribute node.
   * @param ii index info
   * @return step with name
   */
  abstract Step qname(IndexInfo ii);

  /**
   * Rewrites an inverted path expression.
   * @param root new root expression
   * @param ii index info
   * @return path
   */
  abstract ParseExpr invert(ParseExpr root, IndexInfo ii);
}
