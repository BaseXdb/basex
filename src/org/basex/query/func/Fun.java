package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Return;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.Token;

/**
 * Abstract function definition.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Fun extends Expr {
  /** Function results. */
  public Expr[] args;
  /** Function description. */
  public FunDef func;

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    for(int a = 0; a < args.length; a++) args[a] = args[a].comp(ctx);
    final Expr e = c(ctx);
    if(e != this) ctx.compInfo(OPTPRE, this, e);
    return e;
  }

  /**
   * Compiles the function.
   * @param ctx xquery context
   * @return evaluated item
   * @throws QueryException evaluation exception
   */
  @SuppressWarnings("unused")
  public Expr c(final QueryContext ctx) throws QueryException {
    return this;
  }

  @Override
  public final Iter iter(final QueryContext ctx) throws QueryException {
    /** Function arguments. */
    final Iter[] arg = new Iter[args.length];
    for(int a = 0; a < args.length; a++) arg[a] = ctx.iter(args[a]);
    return iter(ctx, arg);
  }

  /**
   * Evaluates the function.
   * @param ctx xquery context
   * @param arg evaluated arguments
   * @return evaluated item
   * @throws QueryException evaluation exception
   */
  public abstract Iter iter(final QueryContext ctx, final Iter[] arg)
    throws QueryException;

  @Override
  public boolean usesPos(final QueryContext ctx) {
    for(final Expr a : args) if(a.usesPos(ctx)) return true;
    return false;
  }

  @Override
  public int countVar(final Var v) {
    int c = 0;
    for(final Expr a : args) c += a.countVar(v);
    return c;
  }

  @Override
  public Expr removeVar(final Var v) {
    for(int e = 0; e != args.length; e++) args[e] = args[e].removeVar(v);
    return this;
  }
  
  @Override
  public Return returned(final QueryContext ctx) {
    return func.ret;
  }

  /**
   * Checks the data type and throws an exception, if necessary.
   * @param i item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException evaluation exception
   */
  protected final Item check(final Item i, final Type t) throws QueryException {
    if(i.type != t) Err.type(info(), t, i);
    return i;
  }

  /**
   * Checks if the specified item is a string.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  protected final byte[] checkStr(final Item it) throws QueryException {
    if(it == null) return Token.EMPTY;
    if(!it.s() && !it.u()) Err.type(info(), Type.STR, it);
    return it.str();
  }

  /**
   * Checks if the specified iterator is a string.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  protected final byte[] checkStr(final Iter iter) throws QueryException {
    return checkStr(iter.atomic(this, true));
  }

  /**
   * Checks if the specified item is a node.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException evaluation exception
   */
  protected final Nod checkNode(final Item it) throws QueryException {
    if(!it.node()) Err.type(info(), Type.NOD, it);
    return (Nod) it;
  }

  /**
   * Checks if the specified collation is supported.
   * @param col collation
   * @throws QueryException evaluation exception
   */
  protected final void checkColl(final Iter col) throws QueryException {
    final Item it = col.atomic(this, false);
    if(!it.s() || !Token.eq(URLCOLL, it.str())) Err.or(IMPLCOL, col);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(func.toString().replaceAll("\\(.*\\)", "") + "(");
    for(int a = 0; a < args.length; a++) {
      sb.append((a != 0 ? ", " : "") + args[a]);
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public final String info() {
    return func.toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(args.length == 0) {
      ser.emptyElement(this, NAM, Token.token(func.desc));
    } else {
      ser.openElement(this, NAM, Token.token(func.desc));
      for(Expr arg : args) arg.plan(ser);
      ser.closeElement();
    }
  }
}
