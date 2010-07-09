package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.core.Main;
import org.basex.core.Text;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.ExprInfo;
import org.basex.io.IO;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.Token;

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
    /** Root flag. */ ELM,
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
   * If this method is not overwritten, {@link #atomic} must be implemented
   * by an expression, as it will be called by this method.
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
   * a {@code null} reference, if the expression yields an empty sequence.
   * If this method is not overwritten, {@link #iter} must be implemented
   * by an expression, as it will be called by this method.
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
   * Tests if this is an empty sequence.
   * @return result of check
   */
  public final boolean e() {
    return this == Seq.EMPTY;
  }

  /**
   * Tests if this is a vacuous expression (empty sequence or error function).
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

  // CHECK DATATYPES ==========================================================

  /**
   * Checks if the specified expressions is no updating expression.
   * @param e expression
   * @param ctx query context
   * @return the specified expression
   * @throws QueryException query exception
   */
  public final Expr checkUp(final Expr e, final QueryContext ctx)
      throws QueryException {
    if(e != null && ctx.updating && e.uses(Use.UPD, ctx)) Err.or(UPNOT, info());
    return e;
  }

  /**
   * Tests if the specified expressions are updating or vacuous.
   * @param ctx query context
   * @param expr expression array
   * @throws QueryException query exception
   */
  public void checkUp(final QueryContext ctx, final Expr[] expr)
      throws QueryException {

    if(!ctx.updating) return;
    int s = 0;
    for(final Expr e : expr) {
      if(e.v()) continue;
      final boolean u = e.uses(Use.UPD, ctx);
      if(u && s == 2 || !u && s == 1) Err.or(UPNOT, info());
      s = u ? 1 : 2;
    }
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
    if(it == null) Err.or(XPEMPTYPE, info(), Type.DBL);
    if(!it.u() && !it.n()) Err.num(info(), it);
    return it.dbl();
  }

  /**
   * Checks if the specified expression is an integer.
   * Returns a token representation or an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return integer value
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
  public final long checkItr(final Item it) throws QueryException {
    if(!it.u() && !it.type.instance(Type.ITR)) errType(Type.ITR, it);
    return it.itr();
  }

  /**
   * Checks if the specified item is a node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final Nod checkNode(final Item it) throws QueryException {
    if(!it.node()) errType(Type.NOD, it);
    return (Nod) it;
  }

  /**
   * Checks if the specified expression yields a non-empty string.
   * Returns a token representation or an exception.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final byte[] checkEmptyStr(final Expr e, final QueryContext ctx)
      throws QueryException {
    return checkStr(checkEmpty(e, ctx));
  }

  /**
   * Checks if the specified item is a string.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  public final byte[] checkStr(final Item it) throws QueryException {
    if(it == null) return Token.EMPTY;
    if(!it.s() && !it.u()) errType(Type.STR, it);
    return it.str();
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  public final Item checkCtx(final QueryContext ctx) throws QueryException {
    final Item it = ctx.item;
    if(it == null) Err.or(XPNOCTX, this);
    return it;
  }

  /**
   * Checks if the specified expression yields a non-empty item.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final Item checkEmpty(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = e.atomic(ctx);
    if(it == null) Err.empty(this);
    return it;
  }

  /**
   * Checks the data type and throws an exception, if necessary.
   * @param it item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  public final Item checkType(final Item it, final Type t)
      throws QueryException {

    if(it == null) Err.empty(this);
    if(it.type != t) errType(t, it);
    return it;
  }

  /**
   * Checks if the specified expression yields a string.
   * Returns a token representation or an exception.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  public final byte[] checkStr(final Expr e, final QueryContext ctx)
      throws QueryException {
    return checkStr(e.atomic(ctx));
  }

  /**
   * Checks if an expression yields a valid {@link IO} instance.
   * Returns the instance or an exception.
   * @param e expression to be evaluated
   * @param ctx query context
   * @return io instance
   * @throws QueryException query exception
   */
  public IO checkIO(final Expr e, final QueryContext ctx)
      throws QueryException {

    checkAdmin(ctx);
    final byte[] name = checkStr(e, ctx);
    final IO io = IO.get(string(name));
    if(!io.exists()) Err.or(DOCERR, name);
    return io;
  }

  /**
   * Checks if the current user has admin permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public final void checkAdmin(final QueryContext ctx) throws QueryException {
    if(!ctx.context.user.perm(User.ADMIN))
      throw new QueryException(Text.PERMNO, CmdPerm.ADMIN);
  }

  /**
   * Returns a type error.
   * @param t expected type
   * @param it item
   * @throws QueryException query exception
   */
  public final void errType(final Type t, final Item it) throws QueryException {
    Err.type(info(), t, it);
  }
}
