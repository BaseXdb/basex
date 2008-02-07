package org.basex.query.xpath.locpath;

import org.basex.query.ExprInfo;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;

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
  abstract boolean eval(final XPContext ctx, final NodeSet nodes, final int pos)
      throws QueryException;

  /**
   * Whether this Predicate uses the set-size.
   * If it doesn't, early evaluation is possible.
   * @return whether setsize is used
   */
  abstract boolean usesSize();

  /**
   * Whether this Predicate makes use of the position of the node.
   * @return whether position is used
   */
  abstract boolean usesPos();

  /**
   * Returns the value of a position predicate. Possible return values:
   * <li>-1 - impossible number predicate</li>
   * <li> 0 - no number predicate</li>
   * <li>>0 - number predicate</li>
   * @return number of predicates
   */
  abstract int posPred();

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
   * For predicate optimization.
   * @param ctx root
   * @param step location step
   * @return Equivalent index-expression or null
   * @throws QueryException evaluation exception
   */
  abstract Expr indexEquivalent(final XPContext ctx, final Step step)
    throws QueryException;

  /**
   * For predicate optimization.
   * @param ctx root
   * @param curr the location step
   * @param min current minimum size
   * @return Equivalent index-expression or null
   */ 
  abstract int indexSizes(final XPContext ctx, final Step curr, final int min);

  /**
   * Compares the predicate for equality.
   * @param pred predicate to be compared
   * @return result of check
   */
  abstract boolean sameAs(final Pred pred);

  @Override
  public String color() {
    return "FF6666";
  }
}
