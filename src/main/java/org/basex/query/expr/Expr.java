package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.core.Main;
import org.basex.data.ExprInfo;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;

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
    /** Root Flag. */ ELM,
    /** Fragment.  */ FRG,
    /** Position.  */ POS,
    /** Updates.   */ UPD,
    /** Variable.  */ VAR,
  }
  /** Undefined value. */
  protected static final int UNDEF = Integer.MIN_VALUE;

  /**
   * Optimizes and compiles the expression.
   * @param ctx query context
   * @return optimized Expression
   * @throws QueryException query exception
   */
  public abstract Expr comp(final QueryContext ctx) throws QueryException;

  /**
   * Evaluates the expression and returns an iterator on the resulting items.
   * If this method is not overwritten, {@link #atomic} must be implemented.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = atomic(ctx);
    return it != null ? it.iter() : Iter.EMPTY;
  }

  /**
   * Evaluates the expression and returns the resulting item or
   * a <code>null</code> reference.
   * If this method is not overwritten, {@link #iter} must be implemented.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Iter ir = iter(ctx);
    final long s = ir.size();
    if(s == 1) return ir.next();

    final Item it = ir.next();
    if(it == null) return null;

    final Item n = ir.next();
    if(n != null) Err.or(XPSEQ, "(" + it + ", " + n +
        (ir.next() != null ? ", ..." : "") + ")");
    return it;
  }

  /**
   * Checks if the iterator can be dissolved into an effective boolean value.
   * If not, returns an error. If yes, returns the first value - which can be
   * also be e.g. an integer, which is later evaluated as position predicate.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public Item ebv(final QueryContext ctx) throws QueryException {
    final Iter ir = iter(ctx);
    final Item it = ir.next();
    if(it == null) return Bln.FALSE;
    if(!it.node() && ir.next() != null) Err.or(CONDTYPE, this);
    return it;
  }

  /**
   * Performs a predicate test and returns the item if test was successful.
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public Item test(final QueryContext ctx) throws QueryException {
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
   * Returns if this is a vacuous expression (empty sequence or error function).
   * @return result of check
   */
  public boolean v() {
    return e();
  }

  /**
   * Returns the sequence size or 1.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public long size(final QueryContext ctx) throws QueryException {
    return returned(ctx).occ == SeqType.OCC_1 ? 1 : -1;
  }

  /**
   * Indicates if an expression uses the specified type.
   * Called by the compiler to perform certain optimizations.
   * <code>true</code> is returned by default and thus assumed as "worst-case".
   * @param u use type to be checked
   * @param ctx query context
   * @return result of check
   */
  public abstract boolean uses(final Use u, final QueryContext ctx);

  /**
   * Checks if the specified variable is removable.
   * @param v variable to be removed
   * @param ctx query context
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean removable(final Var v, final QueryContext ctx) {
    return false;
  }

  /**
   * Removes the specified variable in a sub expression.
   * @param v variable to be removed
   * @return expression with removed variable
   */
  @SuppressWarnings("unused")
  public Expr remove(final Var v) {
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
  public SeqType returned(final QueryContext ctx) {
    return SeqType.ITEM_0M;
  }

  /**
   * Returns true if the expression might yield duplicates and unsorted
   * results.
   * @param ctx query context
   * @return result of check
   */
  public boolean duplicates(final QueryContext ctx) {
    return !returned(ctx).single();
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
   * Checks if the specified expressions is no updating expression.
   * @param e expression
   * @param ctx query context
   * @return the specified expression
   * @throws QueryException query exception
   */
  protected Expr checkUp(final Expr e, final QueryContext ctx)
      throws QueryException {
    if(e != null && ctx.updating && e.uses(Use.UPD, ctx)) Err.or(UPNOT);
    return e;
  }

  /**
   * Checks if the specified expression yields a string.
   * Returns a token representation or an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final double checkDbl(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = e.atomic(ctx);
    if(it == null) Err.or(XPEMPTYNUM, info());
    if(!it.u() && !it.n()) Err.num(info(), it);
    return it.dbl();
  }

  /**
   * Checks if the specified expression is an integer.
   * Returns a token representation or an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final long checkItr(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = e.atomic(ctx);
    if(it == null) Err.or(XPEMPTYPE, info(), Type.ITR);
    return checkItr(it);
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final long checkItr(final Item it) throws QueryException {
    if(!it.u() && !it.type.instance(Type.ITR)) Err.type(info(), Type.ITR, it);
    return it.itr();
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx query context
   * @return item if everything is ok
   * @throws QueryException query exception
   */
  public final Item checkCtx(final QueryContext ctx) throws QueryException {
    final Item it = ctx.item;
    if(it == null) Err.or(XPNOCTX, this);
    return it;
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
