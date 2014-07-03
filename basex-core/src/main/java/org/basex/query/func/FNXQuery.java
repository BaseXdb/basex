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
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNXQuery(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _XQUERY_EVAL:   return eval(qc, false);
      case _XQUERY_UPDATE: return eval(qc, true);
      case _XQUERY_INVOKE: return invoke(qc);
      case _XQUERY_TYPE:   return value(qc).iter();
      default:             return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _XQUERY_EVAL:   return eval(qc, false).value();
      case _XQUERY_UPDATE: return eval(qc, true).value();
      case _XQUERY_INVOKE: return invoke(qc).value();
      case _XQUERY_TYPE:   return type(qc).value(qc);
      default:             return super.value(qc);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    return func == _XQUERY_TYPE ? type(qc) : this;
  }

  /**
   * Performs the eval function.
   * @param qc query context
   * @param updating updating
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder eval(final QueryContext qc, final boolean updating) throws QueryException {
    return eval(qc, checkStr(exprs[0], qc), null, updating);
  }

  /**
   * Evaluates the specified string.
   * @param qc query context
   * @param qu query string
   * @param path path to query file (may be {@code null})
   * @param updating updating query
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder eval(final QueryContext qc, final byte[] qu, final String path,
      final boolean updating) throws QueryException {

    // bind variables and context item
    final HashMap<String, Value> bindings = bindings(1, qc);
    final QueryContext qctx = qc.proc(new QueryContext(qc));

    final Timer to = new Timer(true);
    final Perm tmp = qc.context.user.perm;
    if(exprs.length > 2) {
      final Options opts = checkOptions(2, Q_OPTIONS, new XQueryOptions(), qc);
      qc.context.user.perm = Perm.get(opts.get(XQueryOptions.PERMISSION));
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
              if(Performance.memory() > limit) qctx.stop();
            }
          }
        }, 500, 500);
      }
      final long ms = opts.get(XQueryOptions.TIMEOUT) * 1000L;
      if(ms != 0) {
        to.schedule(new TimerTask() {
          @Override
          public void run() { qctx.stop(); }
        }, ms);
      }
    }

    // evaluate query
    try {
      final StaticContext sctx = new StaticContext(qctx.context);
      for(final Map.Entry<String, Value> it : bindings.entrySet()) {
        final String k = it.getKey();
        final Value v = it.getValue();
        if(k.isEmpty()) qctx.context(v, null, sctx);
        else qctx.bind(k, v, null);
      }
      qctx.parseMain(string(qu), path, sctx);

      if(updating) {
        if(!sc.mixUpdates && !qctx.updating && !qctx.root.expr.isVacuous())
          throw BXXQ_NOUPDATE.get(info);
      } else {
        if(qctx.updating) throw BXXQ_UPDATING.get(info);
      }
      qctx.compile();

      final ValueBuilder vb = new ValueBuilder();
      cache(qctx.iter(), vb, qc);
      return vb;
    } catch(final ProcException ex) {
      throw BXXQ_STOPPED.get(info);
    } catch(final QueryException ex) {
      throw ex.err() == BASX_PERM ? BXXQ_PERM.get(info, ex.getLocalizedMessage()) : ex;
    } finally {
      qc.context.user.perm = tmp;
      qc.proc(null);
      qctx.close();
      to.cancel();
    }
  }

  /**
   * Performs the invoke function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private ValueBuilder invoke(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final IO io = checkPath(exprs[0], qc);
    try {
      return eval(qc, io.read(), io.path(), false);
    } catch(final IOException ex) {
      throw IOERR.get(info, ex);
    }
  }

  /**
   * Dumps the argument's type and size and returns it unchanged.
   * @param qc query context
   * @return the argument expression
   */
  private Expr type(final QueryContext qc) {
    FNInfo.dump(Util.inf("{ type: %, size: %, exprSize: % }", exprs[0].type(), exprs[0].size(),
        exprs[0].exprSize()), token(exprs[0].toString()), qc);
    return exprs[0];
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return !(oneOf(func, _XQUERY_EVAL, _XQUERY_UPDATE, _XQUERY_INVOKE) && !visitor.lock(null)) &&
      super.accept(visitor);
  }
}
