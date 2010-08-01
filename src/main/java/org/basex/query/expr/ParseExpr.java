package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.core.Text;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.Token;

/**
 * Abstract parse expression, containing information on the original query.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class ParseExpr extends Expr {
  /** Query info. */
  protected final QueryInfo info;

  /**
   * Constructor.
   * @param i query info
   */
  public ParseExpr(final QueryInfo i) {
    info = i;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = atomic(ctx);
    return it != null ? it.iter() : Iter.EMPTY;
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Iter ir = iter(ctx);
    final long s = ir.size();
    if(s == 1) return ir.next();

    final Item it = ir.next();
    if(it == null) return null;

    final Item n = ir.next();
    if(n != null) error(XPSEQ, "(" + it + ", " + n +
        (ir.next() != null ? ", ..." : "") + ")");
    return it;
  }

  @Override
  public final Item ebv(final QueryContext ctx) throws QueryException {
    final Iter ir = iter(ctx);
    final Item it = ir.next();
    if(it == null) return Bln.FALSE;
    if(!it.node() && ir.next() != null) error(CONDTYPE, this);
    return it;
  }

  @Override
  public final Item test(final QueryContext ctx) throws QueryException {
    final Item it = ebv(ctx);
    return (it.num() ? it.dbl() == ctx.pos : it.bool()) ? it : null;
  }

  @Override
  public final boolean item() {
    return false;
  }

  // VALIDITY CHECKS ==========================================================

  /**
   * Checks if the specified expressions is no updating expression.
   * @param e expression
   * @param ctx query context
   * @return the specified expression
   * @throws QueryException query exception
   */
  public final Expr checkUp(final Expr e, final QueryContext ctx)
      throws QueryException {
    if(e != null && ctx.updating && e.uses(Use.UPD, ctx)) error(UPNOT, info());
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
      if(e.vacuous()) continue;
      final boolean u = e.uses(Use.UPD, ctx);
      if(u && s == 2 || !u && s == 1) error(UPNOT, info());
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
    if(it == null) error(XPEMPTYPE, info(), Type.DBL);
    if(!it.unt() && !it.num()) numError(info(), it);
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
    if(it == null) error(XPEMPTYPE, info(), Type.ITR);
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
    if(!it.unt() && !it.type.instance(Type.ITR)) errType(Type.ITR, it);
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
    if(!it.str() && !it.unt()) errType(Type.STR, it);
    return it.atom();
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  public final Item checkCtx(final QueryContext ctx) throws QueryException {
    final Item it = ctx.item;
    if(it == null) error(XPNOCTX, this);
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
    if(it == null) emptyError();
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

    if(it == null) emptyError();
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
    if(!io.exists()) error(DOCERR, name);
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
      error(Text.PERMNO, CmdPerm.ADMIN);
  }

  /**
   * Returns a type error.
   * @param t expected type
   * @param it item
   * @throws QueryException query exception
   */
  public final void errType(final Type t, final Item it) throws QueryException {
    typeError(info(), t, it);
  }

  // ERRORS ===================================================================

  
  /**
   * Decorates the specified query exception with information on the.
   * @param err error definition
   * @param x extended info
   * @throws QueryException query exception
   */
  public final void error(final Object[] err, final Object... x)
      throws QueryException {
    throw new QueryException(info, err, x);
  }
  
  /**
   * Decorates the specified query exception with information on the.
   * @param err error message
   * @param x extended info
   * @throws QueryException query exception
   */
  public final void error(final Object err, final Object... x)
      throws QueryException {
    throw new QueryException(info, err, x);
  }

  /**
   * Throws a type exception.
   * @param inf expression info
   * @param t expected type
   * @param it item
   * @throws QueryException query exception
   */
  public void typeError(final String inf, final Type t, final Item it)
      throws QueryException {
    error(XPTYPE, inf, t, it.type);
  }

  /**
   * Throws a node exception.
   * @param ex expression
   * @param it item
   * @throws QueryException query exception
   */
  public void nodeError(final Expr ex, final Item it) throws QueryException {
    typeError(ex.info(), Type.NOD, it);
  }

  /**
   * Throws a numeric type exception.
   * @param inf expression info
   * @param it item
   * @throws QueryException query exception
   */
  public void numError(final String inf, final Item it)
      throws QueryException {
    error(XPTYPENUM, inf, it.type);
  }

  /**
   * Throws a empty sequence exception.
   * @throws QueryException query exception
   */
  public void emptyError() throws QueryException {
    error(XPEMPTY, info());
  }

  /**
   * Throws a comparison exception.
   * @param it1 first item
   * @param it2 second item
   * @throws QueryException query exception
   */
  public void diffError(final Item it1, final Item it2)
      throws QueryException {
    if(it1 == it2) error(TYPECMP, it1.type);
    else error(XPTYPECMP, it1.type, it2.type);
  }
}
