package org.basex.query.expr;

import org.basex.data.ExprInfo;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.FunDef;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.path.AxisPath;
import org.basex.query.path.MixedPath;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Abstract expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /** Usage flags. */
  public enum Use {
    /** Context.   */ CTX,
    /** Fragment.  */ FRG,
    /** Position.  */ POS,
    /** Updates.   */ UPD,
    /** Variable.  */ VAR,
    /** XQuery 11. */ X11,
  }

  /**
   * Optimizes and compiles the expression.
   * @param ctx query context
   * @return optimized Expression
   * @throws QueryException query exception
   */
  public abstract Expr comp(final QueryContext ctx) throws QueryException;

  /**
   * Evaluates the expression and returns an iterator on the resulting items.
   * If this method is not overwritten, {@link #item} must be implemented
   * by an expression, as it will be called by this method.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  public abstract Iter iter(final QueryContext ctx) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting item or
   * a {@code null} reference, if the expression yields an empty sequence.
   * If this method is not overwritten, {@link #iter} must be implemented
   * by an expression, as it will be called by this method.
   * @param ctx query context
   * @param ii input info
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Evaluates the expression and returns the resulting value.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Value value(final QueryContext ctx) throws QueryException;

  /**
   * Checks if the iterator can be dissolved into an effective boolean value.
   * If not, returns an error. If yes, returns the first value - which can be
   * also be e.g. an integer, which is later evaluated as position predicate.
   * @param ctx query context
   * @param ii input info
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Performs a predicate test and returns the item if test was successful.
   * @param ctx query context
   * @param ii input info
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Tests if this is an empty sequence.
   * @return result of check
   */
  public boolean empty() {
    return false;
  }

  /**
   * Tests if this is a value.
   * @return result of check
   */
  public boolean value() {
    return false;
  }

  /**
   * Tests if this is an item.
   * @return result of check
   */
  public boolean item() {
    return false;
  }

  /**
   * Tests if this is a vacuous expression (empty sequence or error function).
   * This check is needed for updating queries.
   * @return result of check
   */
  public boolean vacuous() {
    return false;
  }

  /**
   * Returns the sequence size or 1.
   * @return result of check
   */
  public abstract long size();

  /**
   * Indicates if an expression uses the specified type/operation.
   * Called by the compiler to test properties of sub-expressions.
   * {@code true} is returned as default.
   * @param u use type to be checked
   * @return result of check
   */
  public abstract boolean uses(final Use u);

  /**
   * Checks if the specified variable is used by an expression.
   * This method is called by {@link GFLWOR#comp} to rewrite where clauses
   * as predicates. {@code true} is returned as default.
   * @param v variable to be replaced
   * @return result of check
   */
  public abstract boolean uses(final Var v);

  /**
   * Checks if the specified variable is replaceable by a context item.
   * The following tests might return false:
   * <ul>
   * <li>{@link Preds#removable}, if one of the variables is used within
   * a predicate.</li>
   * <li>{@link MixedPath#removable}, if the variable occurs within
   * the path.</li>
   * <li>{@link Group#removable}, as the group by expression depends on
   * variable references.</li>
   * </ul>
   * This method is called by {@link GFLWOR#comp} to rewrite where clauses
   * as predicates.
   * @param v variable to be replaced
   * @return result of check
   */
  public abstract boolean removable(final Var v);

  /**
   * Substitutes all {@link VarRef} expressions for the given variable
   * by a {@link Context} reference. This method is called by
   * {@link GFLWOR#comp} to rewrite where clauses as predicates.
   * @param v variable to be replaced
   * @return new expression
   */
  public abstract Expr remove(final Var v);

  /**
   * Compiles and simplifies effective boolean values tests.
   * @param ctx query context
   * @return optimized expression
   */
  @SuppressWarnings("unused")
  public Expr compEbv(final QueryContext ctx) {
    return this;
  }

  /**
   * Returns the sequence type of the evaluated value. For simplicity,
   * some types have been summarized. E.g., all numbers are treated as integers.
   * @return result of check
   */
  public abstract SeqType type();

  /**
   * Returns true if the expression might yield duplicates and unsorted
   * results. This method is called e.g. by {@link Union} or {@link AxisPath}.
   * @return result of check
   */
  public boolean duplicates() {
    return !type().zeroOrOne();
  }

  /**
   * Checks if an expression can be rewritten to an index access.
   * If this method is implemented, {@link #indexEquivalent} must be
   * implemented as well.
   * @param ic index context
   * @return true if an index can be used
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    return false;
  }

  /**
   * Returns an equivalent expression which accesses an index structure.
   * Will be called if {@link #indexAccessible} is returns true for an
   * expression.
   * @param ic index context
   * @return equivalent index-expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    return null;
  }

  /**
   * Compares the current and specified expression for equality.
   * @param cmp expression to be compared
   * @return result of check
   */
  public boolean sameAs(final Expr cmp) {
    return this == cmp;
  }

  /**
   * Checks if this expression is a certain function.
   * @param f function definition
   * @return function, or {@code null}
   */
  @SuppressWarnings("unused")
  public boolean isFun(final FunDef f) {
    return false;
  }

  /**
   * Optionally adds a text node to an expression for potential index rewriting.
   * @param ctx query context
   * @return expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr addText(final QueryContext ctx) throws QueryException {
    return this;
  }

  /**
   * Returns a copy of the expression. {@code null} is returned if the
   * expression cannot be copied.
   * @return copy, or {@code null}
   */
  public Expr copy() {
    return null;
  }
}
