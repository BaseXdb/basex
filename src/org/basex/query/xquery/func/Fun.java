package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
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
  public final Expr comp(final XQContext ctx) throws XQException {
    for(int a = 0; a < args.length; a++) args[a] = args[a].comp(ctx);
    final Expr expr = comp(ctx, args);
    if(expr != this) ctx.compInfo(OPTPREEVAL, this);
    return expr;
  }

  /**
   * Compiles the function.
   * @param ctx xquery context
   * @param arg evaluated arguments
   * @return evaluated item
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused")
  public Expr comp(final XQContext ctx, final Expr[] arg) throws XQException {
    return this;
  }

  @Override
  public final Iter iter(final XQContext ctx) throws XQException {
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
   * @throws XQException evaluation exception
   */
  public abstract Iter iter(final XQContext ctx, final Iter[] arg)
    throws XQException;

  @Override
  public boolean uses(final Using u) {
    for(final Expr arg : args) if(arg.uses(u)) return true;
    return false;
  }
  
  @Override
  public final Type returned() {
    return func.ret;
  }

  /**
   * Checks the data type and throws an exception, if necessary.
   * @param i item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws XQException evaluation exception
   */
  protected final Item check(final Item i, final Type t) throws XQException {
    if(i.type != t) Err.type(info(), t, i);
    return i;
  }

  /**
   * Checks if the specified item is a string.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws XQException evaluation exception
   */
  protected final byte[] checkStr(final Item it) throws XQException {
    if(it == null) return Token.EMPTY;
    if(!it.s() && !it.u()) Err.type(info(), Type.STR, it);
    return it.str();
  }

  /**
   * Checks if the specified iterator is a string.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws XQException evaluation exception
   */
  protected final byte[] checkStr(final Iter iter) throws XQException {
    return checkStr(iter.atomic(this, true));
  }

  /**
   * Checks if the specified item is a node.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws XQException evaluation exception
   */
  protected final Nod checkNode(final Item it) throws XQException {
    if(!it.node()) Err.type(info(), Type.NOD, it);
    return (Nod) it;
  }

  /**
   * Checks if the specified collation is supported.
   * @param col collation
   * @throws XQException evaluation exception
   */
  protected final void checkColl(final Iter col) throws XQException {
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
  public void plan(final Serializer ser) throws Exception {
    if(args.length == 0) {
      ser.emptyElement(this, NAM, Token.token(func.desc));
    } else {
      ser.openElement(this, NAM, Token.token(func.desc));
      for(Expr arg : args) arg.plan(ser);
      ser.closeElement(this);
    }
  }

  @Override
  public final String color() {
    return "9999FF";
  }
}
