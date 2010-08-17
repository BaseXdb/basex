package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Atm;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Abstract function definition.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Fun extends Arr {
  /** Function definition. */
  public FunDef def;

  /**
   * Constructor.
   * @param ii input info
   * @param d function definition
   * @param e arguments
   */
  protected Fun(final InputInfo ii, final FunDef d, final Expr... e) {
    super(ii, e);
    def = d;
    type = def.ret;
  }

  /**
   * Creates a function with the specified arguments.
   * @param ii input info
   * @param f function description
   * @param e expression array
   * @return function
   */
  public static final Fun create(final InputInfo ii, final FunDef f,
      final Expr... e) {

    try {
      return f.func.getDeclaredConstructor(InputInfo.class, FunDef.class,
          Expr[].class).newInstance(ii, f, e);
    } catch(final Exception ex) {
      // not expected to occur at all
      ex.printStackTrace();
      Main.debug(ex);
      return null;
    }
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    // compile all arguments
    super.comp(ctx);
    // skip functions based on context or with non-values as arguments
    if(uses(Use.CTX) || !values()) return optPre(cmp(ctx), ctx);
    // pre-evaluate function
    return optPre(def.ret.zeroOrOne() ? atomic(ctx, input) :
      iter(ctx).finish(), ctx);
  }

  /**
   * Performs function specific compilations.
   * @param ctx query context
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr cmp(final QueryContext ctx) throws QueryException {
    return this;
  }

  /**
   * Atomizes the specified item.
   * @param it input item
   * @return atomized item
   */
  protected Item atom(final Item it) {
    return it.node() ? it.type == Type.PI || it.type == Type.COM ?
        Str.get(it.atom()) : new Atm(it.atom()) : it;
  }

  @Override
  public final String desc() {
    return def.toString();
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(def.desc));
    for(final Expr arg : expr) arg.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(def.toString().replaceAll("\\(.*\\)", "") + "(");
    for(int a = 0; a < expr.length; ++a) {
      sb.append((a != 0 ? ", " : "") + expr[a]);
    }
    sb.append(')');
    return sb.toString();
  }
}
