package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * XQuery functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNXQuery extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNXQuery(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _XQUERY_EVAL:     return eval(ctx, true);
      case _XQUERY_EVALUATE: return eval(ctx, false);
      case _XQUERY_INVOKE:   return invoke(ctx);
      case _XQUERY_TYPE:     return value(ctx).iter();
      default:               return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _XQUERY_EVAL:     return eval(ctx, true).value();
      case _XQUERY_EVALUATE: return eval(ctx, false).value();
      case _XQUERY_INVOKE:   return invoke(ctx).value();
      case _XQUERY_TYPE:     return type(ctx).value(ctx);
      default:               return super.value(ctx);
    }
  }

  @Override
  protected Expr opt(final QueryContext ctx, final VarScope scp) {
    return sig == _XQUERY_TYPE ? type(ctx) : this;
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @param openDB allow opening new databases
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder eval(final QueryContext ctx, final boolean openDB) throws QueryException {
    return eval(ctx, checkStr(expr[0], ctx), null, openDB);
  }

  /**
   * Evaluates the specified string.
   * @param ctx query context
   * @param qu query string
   * @param path path to query file (may be {@code null})
   * @param openDB allow opening new databases
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder eval(final QueryContext ctx, final byte[] qu, final String path,
      final boolean openDB) throws QueryException {

    final QueryContext qc = new QueryContext(ctx);
    qc.resource.openDB = openDB;
    final StaticContext sctx = new StaticContext(qc.context.options.get(MainOptions.XQUERY3));

    // bind variables and context item
    for(final Map.Entry<String, Value> it : bindings(1, ctx).entrySet()) {
      final String k = it.getKey();
      final Value v = it.getValue();
      if(k.isEmpty()) qc.context(v, null, sctx);
      else qc.bind(k, v, null);
    }
    // evaluate query
    try {
      qc.parseMain(string(qu), path, sctx);
      if(qc.updating) throw BXXQ_UPDATING.get(info);
      qc.compile();

      final ValueBuilder vb = new ValueBuilder();
      final Iter iter = qc.iter();
      if(openDB) {
        cache(iter, vb, ctx);
      } else {
        for(Item it; (it = iter.next()) != null;) {
          if(it instanceof FItem) throw FIVALUE.get(info, it);
          vb.add(it);
        }
      }
      return vb;
    } finally {
      qc.close();
    }
  }

  /**
   * Performs the invoke function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder invoke(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final IO io = checkPath(expr[0], ctx);
    try {
      return eval(ctx, io.read(), io.path(), true);
    } catch(final IOException ex) {
      throw IOERR.get(info, ex);
    }
  }

  /**
   * Dumps the argument's type and size and returns it unchanged.
   * @param ctx query context
   * @return the argument expression
   */
  private Expr type(final QueryContext ctx) {
    FNInfo.dump(Util.inf("{ type: %, size: %, exprSize: % }", expr[0].type(), expr[0].size(),
        expr[0].exprSize()), token(expr[0].toString()), ctx);
    return expr[0];
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(oneOf(sig, _XQUERY_EVAL, _XQUERY_INVOKE) && !visitor.lock(null)) &&
      super.accept(visitor);
  }
}
