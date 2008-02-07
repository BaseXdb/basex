package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.query.ExprInfo;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Item;

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
   * Tries to optimize the Expression.
   * @param ctx expression context
   * @return optimized Expression
   * @throws QueryException evaluation exception
   */
  public abstract Expr compile(final XPContext ctx) throws QueryException;
  
  /**
   * Evaluates the expression with the specified context set. Additionally
   * provides a context
   * @param ctx query context
   * @return resulting XPathValue
   * @throws QueryException evaluation exception
   */
  public abstract Item eval(XPContext ctx) throws QueryException;

  /**
   * Gets the expected return type. This may be XPathValue itself.
   * @return the expected class returned by the eval() method
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
   * For predicate optimization.
   * If possible return an expression yielding the same results using the index.
   * This may not do any changes to the current expression.
   * @param ctx root
   * @param step the LocationStep this Expression is a predicate of
   * @return Equivalent index-expression or null
   * @throws QueryException evaluation exception
   */
  @SuppressWarnings("unused")
  public Expr indexEquivalent(final XPContext ctx, final Step step)
      throws QueryException {
    return null;
  }
  
  /**
   * For predicate optimization.
   * If possible return the number of entries an index returns.
   * This may not do any changes to the current expression.
   * @param ctx root
   * @param step the LocationStep this Expression is a predicate of
   * @param min current minimum size
   * @return Equivalent index-expression or null
   */
  @SuppressWarnings("unused")
  public int indexSizes(final XPContext ctx, final Step step,
      final int min) {
    return Integer.MAX_VALUE;
  }
  
  /**
   * Opens a query plan tag.
   * @param out print output
   * @param l current level
   * @param s tag string
   * @throws IOException I/O exception
   */
  protected void openPlan(final PrintOutput out, final String s, final int l)
      throws IOException {
    out.println("<" + s + ">", l);
  }
  
  /**
   * Opens a query plan tag.
   * @param out print output
   * @param l current level
   * @param s tag string
   * @throws IOException I/O exception
   */
  protected void closePlan(final PrintOutput out, final String s, final int l)
      throws IOException {
    out.println("</" + s + ">", l);
  }
}
