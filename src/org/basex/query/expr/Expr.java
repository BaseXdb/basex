package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.BaseX;
import org.basex.query.ExprInfo;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
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
   * If this method is not overwritten, {@link #atomic} must be implemented.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException evaluation exception
   */
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = atomic(ctx);
    return it != null ? it.iter() : Iter.EMPTY;
  }

  /**
   * Evaluates the expression and returns the resulting item or
   * a null reference.
   * If this method is not overwritten, {@link #iter} must be implemented.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  public Item atomic(final QueryContext ctx) throws QueryException {
    return iter(ctx).atomic();
  }

  /**
   * Checks if the iterator can be dissolved into an effective boolean value.
   * If not, returns an error. If yes, returns the first value - which can be
   * also be e.g. an integer, which is later evaluated as position predicate.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final Item ebv(final QueryContext ctx) throws QueryException {
    final Iter ir = iter(ctx);
    final Item it = ir.next();
    if(it == null) return Bln.FALSE;
    if(!it.node() && ir.next() != null) Err.or(FUNSEQ, this);
    return it;
  }

  /**
   * Performs a predicate test and returns the item if test was successful.
   * @param ctx query context
   * @return item
   * @throws QueryException evaluation exception
   */
  public final Item test(final QueryContext ctx) throws QueryException {
    final Item it = ebv(ctx);
    return (it.n() ? it.dbl() == ctx.pos : it.bool()) ? it : null;
  }

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
   * Returns the sequence size or 1.
   * @param ctx query context
   * @return result of check
   * @throws QueryException Exception
   */
  @SuppressWarnings("unused")
  public long size(final QueryContext ctx) throws QueryException {
    return -1;
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
   * Counts how often a variable is used by an expression and its children.
   * If the argument is <code>null</code>, all variables are counted.
   * Called by the compiler to perform certain optimizations.
   * {@link Integer#MAX_VALUE} is returned by default and thus
   * assumed as "worst-case".
   * @param v variable to be checked
   * @return result of check
   */
  @SuppressWarnings("unused")
  public int countVar(final Var v) {
    return Integer.MAX_VALUE;
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
  public final double checkDbl(final Iter iter) throws QueryException {
    final Item it = iter.atomic();
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
    final Item it = iter.atomic();
    if(it == null) Err.or(XPEMPTYPE, info(), Type.ITR);
    return checkItr(it);
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx query context
   * @return item if everything is ok
   * @throws QueryException evaluation exception
   */
  public final Iter checkCtx(final QueryContext ctx) throws QueryException {
    final Item it = ctx.item;
    if(it == null) Err.or(XPNOCTX, this);
    return it.iter();
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
