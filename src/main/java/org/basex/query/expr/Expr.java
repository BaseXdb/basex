package org.basex.query.expr;

import org.basex.core.Main;
import org.basex.data.ExprInfo;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;

/**
 * Abstract expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /** Usage flags. */
  protected enum Use {
    /** Context.   */ CTX,
    /** Root flag. */ ELM,
    /** Fragment.  */ FRG,
    /** Position.  */ POS,
    /** Updates.   */ UPD,
    /** Variable.  */ VAR,
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
   * If this method is not overwritten, {@link #atomic} must be implemented
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
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Item atomic(final QueryContext ctx) throws QueryException;

  /**
   * Checks if the iterator can be dissolved into an effective boolean value.
   * If not, returns an error. If yes, returns the first value - which can be
   * also be e.g. an integer, which is later evaluated as position predicate.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item ebv(final QueryContext ctx) throws QueryException;

  /**
   * Performs a predicate test and returns the item if test was successful.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item test(final QueryContext ctx) throws QueryException;

  /**
   * Checks if this is an item.
   * @return result of check
   */
  public abstract boolean item();

  /**
   * Tests if this is an empty sequence.
   * @return result of check
   */
  public final boolean empty() {
    return this == Seq.EMPTY;
  }

  /**
   * Tests if this is a vacuous expression (empty sequence or error function).
   * This check is needed for updating queries.
   * @return result of check
   */
  public boolean vacuous() {
    return empty();
  }

  /**
   * Returns the sequence size or 1.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public long size(final QueryContext ctx) throws QueryException {
    return returned(ctx).one() ? 1 : -1;
  }

  /**
   * Indicates if an expression uses the specified type.
   * Called by the compiler to perform certain optimizations.
   * {@code true} is returned by default and thus assumed as "worst-case".
   * @param u use type to be checked
   * @param ctx query context
   * @return result of check
   */
  public abstract boolean uses(final Use u, final QueryContext ctx);

  /**
   * Checks if the specified variable is removable (e.g., .
   * @param v variable to be removed
   * @param ctx query context
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean removable(final Var v, final QueryContext ctx) {
    return false;
  }

  /**
   * Substitutes all {@link VarCall} expressions for the given variable
   * by a {@link Context} reference. This method is initially called by the
   * {@link FLWR} class and passed on to all sub-expressions.
   * @param v variable to be replace
   * @return expression with removed variable
   */
  @SuppressWarnings("unused")
  public Expr remove(final Var v) {
    return this;
  }

  /**
   * Indicates the return type of an expression.
   * @param ctx query context
   * @return result of check
   */
  @SuppressWarnings("unused")
  public SeqType returned(final QueryContext ctx) {
    return SeqType.ITEM_ZM;
  }

  /**
   * Returns true if the expression might yield duplicates and unsorted
   * results.
   * @param ctx query context
   * @return result of check
   */
  public boolean duplicates(final QueryContext ctx) {
    return !returned(ctx).zeroOrOne();
  }

  /**
   * Checks if an index can be used for query evaluation.
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
   * Must be called if {@link #indexAccessible} is implemented for an
   * expression.
   * @param ic index context
   * @return Equivalent index-expression or null
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    Main.notexpected();
    return null;
  }

  /**
   * Checks the current and specified expression for equality.
   * @param cmp expression to be compared
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean sameAs(final Expr cmp) {
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
}
