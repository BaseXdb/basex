package org.basex.query.func;

import static org.basex.query.QueryText.*;
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
import org.basex.util.options.*;

/**
 * XQuery functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNXQuery extends StandardFunc {
  /** Module prefix. */
  private static final String PREFIX = "xquery";
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(PREFIX, "options", XQUERYURI);

  /** XQuery options. */
  public static class XQueryOptions extends Options {
    /** Permission. */
    public static final StringOption PERMISSION = new StringOption("permission", Perm.ADMIN.name());
    /** Timeout in seconds. */
    public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
    /** Maximum amount of megabytes that may be allocated by the query. */
    public static final NumberOption MEMORY = new NumberOption("memory", 0);
  }

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

    // bind variables and context item
    final HashMap<String, Value> bindings = bindings(1, ctx);

    final QueryContext qc = ctx.proc(new QueryContext(ctx));
    qc.resource.openDB = openDB;

    final Timer to = new Timer(true);
    final Perm tmp = ctx.context.user.perm;
    if(expr.length > 2) {
      final Options opts = checkOptions(2, Q_OPTIONS, new XQueryOptions(), ctx);
      ctx.context.user.perm = Perm.get(opts.get(XQueryOptions.PERMISSION));
      // initial memory consumption: perform garbage collection and calculate usage
      Performance.gc(2);
      final long mb = opts.get(XQueryOptions.MEMORY);
      if(mb != 0) {
        final long limit = Performance.memory() + (mb << 20);
        to.schedule(new TimerTask() {
          @Override
          public void run() {
            // limit reached: perform garbage collection and check again
            if(Performance.memory() > limit) {
              Performance.gc(1);
              if(Performance.memory() > limit) qc.stop();
            }
          }
        }, 500, 500);
      }
      final long ms = opts.get(XQueryOptions.TIMEOUT) * 1000L;
      if(ms != 0) {
        to.schedule(new TimerTask() {
          @Override
          public void run() { qc.stop(); }
        }, ms);
      }
    }

    // evaluate query
    try {
      final StaticContext sctx = new StaticContext(qc.context.options.get(MainOptions.XQUERY3));
      for(final Map.Entry<String, Value> it : bindings.entrySet()) {
        final String k = it.getKey();
        final Value v = it.getValue();
        if(k.isEmpty()) qc.context(v, null, sctx);
        else qc.bind(k, v, null);
      }

      qc.parseMain(string(qu), path, sctx);
      if(qc.updating) throw BXXQ_UPDATING.get(info);
      qc.compile();

      final ValueBuilder vb = new ValueBuilder();
      final Iter iter = qc.iter();
      if(openDB) {
        cache(iter, vb, ctx);
      } else {
        for(Item it; (it = iter.next()) != null;) {
          if(it instanceof FItem) throw FIVALUE.get(info, it.type);
          vb.add(it);
        }
      }
      return vb;
    } catch(final ProcException ex) {
      throw BXXQ_STOPPED.get(info);
    } catch(final QueryException ex) {
      throw ex.err() == BASX_PERM ? BXXQ_PERM.get(info, ex.getLocalizedMessage()) : ex;
    } finally {
      ctx.context.user.perm = tmp;
      ctx.proc(null);
      qc.close();
      to.cancel();
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
