package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Text;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Abstract function definition.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Fun extends Arr {
  /** Function description. */
  public FunDef func;

  /**
   * Creates a function with the specified arguments.
   * @param f function description
   * @param e expression array
   * @return function
   */
  public static final Fun create(final FunDef f, final Expr... e) {
    try {
      final Fun fun = f.func.newInstance();
      fun.func = f;
      fun.expr = e;
      return fun;
    } catch(final Exception ex) {
      // not expected to occur at all
      Main.debug(ex);
      return null;
    }
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    final Expr e = c(ctx);
    if(e != this) ctx.compInfo(OPTPRE, this);
    return e;
  }

  /**
   * Compiles the function.
   * @param ctx query context
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr c(final QueryContext ctx) throws QueryException {
    return this;
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return func.ret;
  }

  /**
   * Checks the data type and throws an exception, if necessary.
   * @param i item to be checked
   * @param t type to be checked
   * @return specified item
   * @throws QueryException query exception
   */
  protected final Item check(final Item i, final Type t) throws QueryException {
    if(i == null) Err.empty(this);
    if(i.type != t) Err.type(info(), t, i);
    return i;
  }

  /**
   * Checks if the specified expression yields a string.
   * Returns a token representation or an exception.
   * @param e expression to be checked
   * @param ctx query context
   * @return item
   * @throws QueryException query exception
   */
  protected final byte[] checkStr(final Expr e, final QueryContext ctx)
      throws QueryException {
    return checkStr(e.atomic(ctx));
  }

  /**
   * Checks if the specified item is a string.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final byte[] checkStr(final Item it) throws QueryException {
    if(it == null) return Token.EMPTY;
    if(!it.s() && !it.u()) Err.type(info(), Type.STR, it);
    return it.str();
  }

  /**
   * Checks if the specified item is a node.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final Nod checkNode(final Item it) throws QueryException {
    if(!it.node()) Err.type(info(), Type.NOD, it);
    return (Nod) it;
  }

  /**
   * Checks if the specified collation is supported.
   * @param e expression to be checked
   * @param ctx query context
   * @throws QueryException query exception
   */
  protected final void checkColl(final Expr e, final QueryContext ctx)
      throws QueryException {

    final Item it = e.atomic(ctx);
    if(it == null) Err.empty(this);
    if(!it.s() || !Token.eq(URLCOLL, it.str())) Err.or(IMPLCOL, e);
  }

  /**
   * Checks if the current user has admin permissions. If negative, an
   * exception is thrown.
   * @param ctx query context
   * @throws QueryException query exception
   */
  protected final void checkAdmin(final QueryContext ctx)
      throws QueryException {

    if(!ctx.context.user.perm(User.ADMIN))
      throw new QueryException(Text.PERMNO, CmdPerm.ADMIN);
  }

  @Override
  public final String info() {
    return func.toString();
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(func.desc));
    for(final Expr arg : expr) arg.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(func.toString().replaceAll("\\(.*\\)", "") + "(");
    for(int a = 0; a < expr.length; a++) {
      sb.append((a != 0 ? ", " : "") + expr[a]);
    }
    sb.append(')');
    return sb.toString();
  }
}
