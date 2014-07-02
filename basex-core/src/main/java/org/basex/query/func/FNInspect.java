package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.inspect.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Inspect functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNInspect extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNInspect(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case _INSPECT_FUNCTIONS: return functions(ctx);
      default:                 return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
      case _INSPECT_FUNCTION: return function(ctx);
      case _INSPECT_MODULE:   return module(ctx);
      case _INSPECT_CONTEXT:  return context(ctx);
      case _INSPECT_XQDOC:    return xqdoc(ctx);
      default:                return super.item(ctx, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(func == Function._INSPECT_FUNCTIONS && exprs.length == 0) {
      for(final StaticFunc sf : ctx.funcs.funcs()) sf.compile(ctx);
      return functions(ctx).value();
    }
    return this;
  }

  /**
   * Performs the function function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item function(final QueryContext ctx) throws QueryException {
    final FItem fn = checkFunc(exprs[0], ctx);
    final QNm name = fn.funcName();
    final StaticFunc sf = name == null ? null : ctx.funcs.get(name, fn.arity(), null, false);
    return new PlainDoc(ctx, info).function(name, sf, fn.funcType(), fn.annotations(), null);
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
    return new PlainDoc(ctx, info).parse(checkPath(exprs[0], ctx));
  }

  /**
   * Performs the xqdoc function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item xqdoc(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    return new XQDoc(ctx, info).parse(checkPath(exprs[0], ctx));
  }

  /**
   * Performs the functions function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder functions(final QueryContext ctx) throws QueryException {
    // about to be updated in a future version
    final ArrayList<StaticFunc> old = new ArrayList<>();
    if(exprs.length > 0) {
      // cache existing functions
      for(final StaticFunc sf : ctx.funcs.funcs()) old.add(sf);
      try {
        final IO io = checkPath(exprs[0], ctx);
        ctx.parse(Token.string(io.read()), io.path(), sc);
        ctx.compile();
      } catch(final IOException ex) {
        throw IOERR.get(info, ex);
      } finally {
        ctx.close();
      }
    }

    final ValueBuilder vb = new ValueBuilder();
    for(final StaticFunc sf : ctx.funcs.funcs()) {
      if(old.contains(sf)) continue;
      final FuncItem fi = Functions.getUser(sf, ctx, sf.sc, info);
      if(sc.mixUpdates || !fi.annotations().contains(Ann.Q_UPDATING)) vb.add(fi);
    }
    return vb;
  }

  @Override
  public boolean has(final Flag flag) {
    // do not relocate function, as it introduces new code
    return flag == Flag.NDT && func == Function._INSPECT_FUNCTIONS && exprs.length == 1 ||
        super.has(flag);
  }
}
