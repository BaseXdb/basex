package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.BaseX;
import org.basex.query.ExprInfo;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;

/**
 * Abstract Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /** Undefined value. */
  protected static final int UNDEF = Integer.MIN_VALUE;

  /**
   * Optimizes and compiles the expression.
   * @param ctx query context
   * @return optimized Expression
   * @throws QueryException evaluation exception
   */
  public abstract Expr comp(final QueryContext ctx) throws QueryException;

  /**
   * Evaluates the expression and returns an iterator on the resulting items.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException evaluation exception
   */
  public abstract Iter iter(final QueryContext ctx) throws QueryException;

  /**
   * Checks if this is an item.
   * @return result of check
   */
  public boolean i() {
    return this instanceof Item;
  }

  /**
   * Returns if this is an empty sequence.
   * @return result of check
   */
  public final boolean e() {
    return this == Seq.EMPTY;
  }

  /**
   * Indicates if an expression accesses the position of a context item.
   * Called by the compiler to perform certain optimizations.
   * <code>true</code> is returned by default and thus assumed as "worst-case".
   * @param ctx query context
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean usesPos(final QueryContext ctx) {
    return true;
  }

  /**
   * Indicates if an expression uses the specified variables. If the argument
   * is <code>null</code>, it is checked if any variable is used.
   * Called by the compiler to perform certain optimizations.
   * <code>true</code> is returned by default and thus assumed as "worst-case".
   * @param v variable to be checked
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean usesVar(final Var v) {
    return true;
  }

  /**
   * Removes the specified variable in a sub expression.
   * @param v variable to be removed
   * @return expression with removed variable
   */
  @SuppressWarnings("unused")
  public Expr removeVar(final Var v) {
    return this;
  }

  /**
   * Indicates the return type of an expression.
   * Called by the compiler to check if expressions can be reformulated.
   * null is returned by default.
   * @param ctx query context
   * @return result of check
   */
  @SuppressWarnings("unused")
  public Return returned(final QueryContext ctx) {
    return Return.SEQ;
  }
  
  /**
   * Checks if an index can be used for query evaluation.
   * @param ctx query context
   * @param ic index context
   * @throws QueryException Exception
   */
  @SuppressWarnings("unused")
  public void indexAccessible(final QueryContext ctx, final IndexContext ic)
    throws QueryException {
  }
  
  /**
   * Returns an equivalent expression which accesses an index structure. 
   * Must be called if {@link #indexAccessible} is implemented for an
   * expression.
   * @param ctx query context
   * @param ic index context
   * @return Equivalent index-expression or null
   * @throws QueryException Exception
   */
  @SuppressWarnings("unused")
  public Expr indexEquivalent(final QueryContext ctx, final IndexContext ic)
      throws QueryException {
    BaseX.notexpected();
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
   * Checks if the specified iterator is a number.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  protected final double checkDbl(final Iter iter) throws QueryException {
    final Item it = iter.atomic(this, true);
    if(it == null) Err.or(XPEMPTYNUM, info());
    if(!it.u() && !it.n()) Err.num(info(), it);
    return it.dbl();
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  protected final long checkItr(final Item it) throws QueryException {
    if(!it.u() && !it.type.instance(Type.ITR)) Err.type(info(), Type.ITR, it);
    return it.itr();
  }

  /**
   * Checks if the specified iterator is a number.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  protected final long checkItr(final Iter iter) throws QueryException {
    final Item it = iter.atomic(this, true);
    if(it == null) Err.or(XPEMPTYPE, info(), Type.ITR);
    return checkItr(it);
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx xquery context
   * @return item if everything is ok
   * @throws QueryException evaluation exception
   */
  public final Iter checkCtx(final QueryContext ctx) throws QueryException {
    final Item it = ctx.item;
    if(it == null) Err.or(XPNOCTX, this);
    return it.iter();
  }

  /**
   * Returns the specified expression as an atomic item.
   * Empty sequences are handled by the empty flag.
   * @param ctx xquery context
   * @param ex expression
   * @param e if set to true, empty sequences are returned as null.
   * Otherwise, an error is thrown
   * @return iterator
   * @throws QueryException query exception
   */
  protected Item atomic(final QueryContext ctx, final Expr ex, final boolean e)
      throws QueryException {
    return i() ? (Item) ex : ctx.iter(ex).atomic(this, e);
  }

  /**
   * Adds a text to an expression.
   * @param ctx query context
   * @return expression
   */
  @SuppressWarnings("unused")
  public Expr addText(final QueryContext ctx) {
    return this;
  }
}
