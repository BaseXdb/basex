package org.basex.query.xpath.locpath;

import org.basex.query.ExprInfo;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.item.Nod;
import org.basex.query.xpath.item.NodeBuilder;

/**
 * XPath predicate.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public abstract class Pred extends ExprInfo {
  /** Remembers if following tests can yield true. */
  protected boolean more = true;

  /**
   * Evaluates a predicate.
   * @param ctx query context
   * @param set current node set
   * @return resulting node set
   * @throws QueryException evaluation exception
   */
  abstract NodeBuilder eval(final XPContext ctx, final NodeBuilder set)
      throws QueryException;

  /**
   * Early/position evaluation.
   * @param ctx query context
   * @param nodes nodes to be evaluated
   * @param pos position value
   * @return result of evaluation
   * @throws QueryException evaluation exception
   */
  abstract boolean eval(final XPContext ctx, final Nod nodes, final int pos)
      throws QueryException;

  /**
   * Whether this Predicate uses the set-size.
   * If it doesn't, early evaluation is possible.
   * @return whether size is used
   */
  abstract boolean usesSize();

  /**
   * Whether this Predicate makes use of the position of the node.
   * @return whether position is used
   */
  abstract boolean usesPos();

  /**
   * Returns the value of a position predicate. Possible return values:
   * <li>-1: impossible position predicate</li>
   * <li> 0: no position predicate</li>
   * <li>>0: position predicate</li>
   * @return predicate position
   */
  abstract double posPred();

  /**
   * Optimizes the Predicate.
   * @param ctx query context
   * @return optimized predicate
   * @throws QueryException evaluation exception
   */
  abstract Pred compile(final XPContext ctx) throws QueryException;

  /**
   * Checks whether the predicate can be fulfilled.
   * @return whether this predicate is always false
   */
  abstract boolean alwaysFalse();

  /**
   * Checks whether the predicate is always fulfilled.
   * @return whether this predicate is always true
   */
  abstract boolean alwaysTrue();

  /**
   * Returns an equivalent expression which accesses an index structure.
   * @param ctx root
   * @param step location step
   * @param seq flag for sequential evaluation
   * @return Equivalent index-expression or null
   * @throws QueryException evaluation exception
   */
  @SuppressWarnings("unused")
  Expr indexEquivalent(final XPContext ctx, final Step step, 
      final boolean seq) throws QueryException {
    return null;
  }

  /**
   * Returns the number of results if this query is evaluated by an index.
   * If {@link Integer#MAX_VALUE} is returned, no index access is possible.
   * @param ctx root
   * @param step the current location step
   * @param min current minimum index hits
   * @return number of expected results
   */ 
  @SuppressWarnings("unused")
  int indexSizes(final XPContext ctx, final Step step, final int min) {
    return Integer.MAX_VALUE;
  }

  /**
   * Compares the predicate for equality.
   * @param pred predicate to be compared
   * @return result of check
   */
  abstract boolean sameAs(final Pred pred);

  @Override
  public final String color() {
    return "FF6666";
  }
}
