package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Atm;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.Str;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Abstract function definition.
 *
 * @author BaseX Team 2005-11, BSD License
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

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // compile all arguments
    super.comp(ctx);
    // skip functions based on context or with non-values as arguments
    if(uses(Use.CTX) || !values()) return optPre(cmp(ctx), ctx);
    // pre-evaluate function
    return optPre(def.ret.zeroOrOne() ?
        item(ctx, input) : value(ctx), ctx);
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
   * @throws QueryException query exception
   */
  protected Item atom(final Item it) throws QueryException {
    return it.node() ? it.type == NodeType.PI || it.type == NodeType.COM ?
        Str.get(it.atom(input)) : new Atm(it.atom(input)) : it;
  }

  @Override
  public boolean isFun(final FunDef f) {
    return def == f;
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
    return new TokenBuilder().add(def.toString().replaceAll(
        "\\(.*\\)", "") + PAR1).addSep(expr, SEP).add(PAR2).toString();
  }
}
