package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemCache;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Function call.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FuncCall extends Arr {
  /** Function name. */
  private final QNm name;
  /** Function reference. */
  private Func func;

  /**
   * Function constructor.
   * @param ii input info
   * @param nm function name
   * @param arg arguments
   */
  public FuncCall(final InputInfo ii, final QNm nm, final Expr[] arg) {
    super(ii, arg);
    name = nm;
  }

  /**
   * Initializes the function call after all functions have been declared.
   * @param f function reference
   */
  public void init(final Func f) {
    func = f;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    // Inline if result and arguments are all values.
    // Currently, only functions with values as
    // return expressions are supported; otherwise, recursive functions
    // might not be correctly evaluated
    if(func.expr.value() && values() && !func.uses(Use.CTX)) {
      // evaluate arguments to catch cast exceptions
      for(int a = 0; a < expr.length; ++a) func.args[a].bind(expr[a], ctx);
      ctx.compInfo(OPTINLINE, func.name.atom());
      return func.value(ctx);
    }
    // User-defined functions are not pre-evaluated to avoid various issues
    // with recursive functions
    type = func.type();
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final int al = expr.length;
    final Value[] args = new Value[al];
    // evaluate arguments
    for(int a = 0; a < al; ++a) args[a] = expr[a].value(ctx);
    // move variables to stack
    final int s = ctx.vars.size();
    for(int a = 0; a < al; ++a) {
      ctx.vars.add(func.args[a].bind(args[a], ctx).copy());
    }
    // evaluate function and reset variable scope
    final ItemCache ir = ItemCache.get(ctx.iter(func));
    ctx.vars.reset(s);
    return ir;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.UPD ? func.updating : super.uses(u);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(toString()));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return FUNC;
  }

  @Override
  public String toString() {
    return new TokenBuilder(name.atom()).add(PAR1).add(
        toString(SEP)).add(PAR2).toString();
  }
}
