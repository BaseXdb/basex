package org.basex.query.func;

import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.inspect.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Inspect functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class FNInspect extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNInspect(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _INSPECT_FUNCTIONS: return contextFunctions(ctx);
      default:                         return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _INSPECT_FUNCTION: return function(ctx);
      case _INSPECT_MODULE:   return module(ctx);
      case _INSPECT_CONTEXT:  return context(ctx);
      case _INSPECT_XQDOC:    return xqdoc(ctx);
      default:                return super.item(ctx, ii);
    }
  }

  /**
   * Performs the function function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item function(final QueryContext ctx) throws QueryException {
    final FItem f = checkFunc(expr[0], ctx);
    final StaticFunc sf = f instanceof FuncItem ? ((FuncItem) f).func : null;
    return new PlainDoc(ctx, info).function(f.fName(), sf, f.funcType(), null);
  }

  /**
   * Performs the context function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item context(final QueryContext ctx) throws QueryException {
    return new PlainDoc(ctx, info).context();
  }

  /**
   * Performs the module function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item module(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    return new PlainDoc(ctx, info).parse(IO.get(string(checkStr(expr[0], ctx))));
  }

  /**
   * Performs the xqdoc function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item xqdoc(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    return new XQDoc(ctx, info).parse(IO.get(string(checkStr(expr[0], ctx))));
  }

  /**
   * Performs the context-functions function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder contextFunctions(final QueryContext ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final StaticFunc sf : ctx.funcs.funcs()) vb.add(Functions.getUser(sf, ctx, info));
    return vb;
  }
}
