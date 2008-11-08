package org.basex.query.xpath.expr;

import org.basex.query.ExprInfo;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.locpath.Step;

/**
 * Common interface for all expressions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public abstract class Expr extends ExprInfo {
  /** eval() arguments. */
  private static final Class<?>[] EVALARGS = { XPContext.class };
  
  /**
   * Optimizes the expression.
   * @param ctx expression context
   * @return optimized Expression
   * @throws QueryException evaluation exception
   */
  public abstract Expr comp(final XPContext ctx) throws QueryException;
  
  /**
   * Evaluates the expression with the specified context set. Additionally
   * provides a context
   * @param ctx query context
   * @return resulting XPathValue
   * @throws QueryException evaluation exception
   */
  public abstract Item eval(XPContext ctx) throws QueryException;

  /**
   * Gets the expected return type. This may be the value itself.
   * @return the expected class returned by the {@link #eval} method
   */
  public final Class<?> returnedValue() {
    try {
      return getClass().getMethod("eval", EVALARGS).getReturnType();
    } catch(final NoSuchMethodException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Checks current and specified expression for equivalence.
   * @param cmp expression to be compared
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean sameAs(final Expr cmp) {
    return false;
  }
  
  /**
   * Checks whether this Expression (or its children) make use of the setsize
   * parameter. If not this allows early predicate evaluation.
   * @return whether setsize is used
   */
  public abstract boolean usesSize();
  
  /**
   * Checks whether this Expression (or its children) make use of the position
   * parameter. If not this allows some nice optimizations.
   * @return whether position is used
   */
  public abstract boolean usesPos();
  
  /**
   * Returns an equivalent expression which accesses an index structure.
   * @param ctx root
   * @param step location step
   * @param seq flag for sequential evaluation
   * @return Equivalent index-expression or null
   * @throws QueryException evaluation exception
   */
  @SuppressWarnings("unused")
  public Expr indexEquivalent(final XPContext ctx, final Step step, 
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
  public int indexSizes(final XPContext ctx, final Step step, final int min) {
    return Integer.MAX_VALUE;
  }
}
