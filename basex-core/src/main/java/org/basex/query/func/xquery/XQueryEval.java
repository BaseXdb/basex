package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class XQueryEval extends StandardFunc {
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(XQUERY_PREFIX, "options", XQUERY_URI);

  /** XQuery options. */
  public static class XQueryOptions extends Options {
    /** Permission. */
    public static final EnumOption<Permission> PERMISSION = new EnumOption<>(
        "permission", Permission.ADMIN);
    /** Timeout in seconds. */
    public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
    /** Maximum amount of megabytes that may be allocated by the query. */
    public static final NumberOption MEMORY = new NumberOption("memory", 0);
  }

  /** Permissions. */
  public enum Permission {
    /** Admin.  */ ADMIN,
    /** Create. */ CREATE,
    /** Write.  */ WRITE,
    /** Read.   */ READ,
    /** None.   */ NONE;

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }
  };

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return eval(qc, toToken(exprs[0], qc), null, false);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value();
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
  final ValueBuilder eval(final QueryContext qc, final byte[] qu, final String path,
      final boolean updating) throws QueryException {

    // bind variables and context value
    final HashMap<String, Value> bindings = toBindings(1, qc);
    final User user = qc.context.user();
    final Perm tmp = user.perm();
    Timer to = null;

    try(final QueryContext qctx = qc.proc(new QueryContext(qc))) {
      if(exprs.length > 2) {
        final Options opts = toOptions(2, Q_OPTIONS, new XQueryOptions(), qc);
        user.perm(Perm.get(opts.get(XQueryOptions.PERMISSION).name()));

        // initial memory consumption: perform garbage collection and calculate usage
        Performance.gc(2);
        final long mb = opts.get(XQueryOptions.MEMORY);
        if(mb != 0) {
          final long limit = Performance.memory() + (mb << 20);
          to = new Timer(true);
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
          if(to == null) to = new Timer(true);
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
          final String key = it.getKey();
          final Value val = it.getValue();
          if(key.isEmpty()) qctx.context(val, sctx);
          else qctx.bind(key, val, sctx);
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
        cache(qctx.iter(), vb, qctx);
        return vb;
      } catch(final ProcException ex) {
        throw BXXQ_STOPPED.get(info);
      } catch(final QueryException ex) {
        throw ex.error() == BASX_PERM_X ? BXXQ_PERM_X.get(info, ex.getLocalizedMessage()) :
          ex.info(info);
      }

    } finally {
      user.perm(tmp);
      qc.proc(null);
      if(to != null) to.cancel();
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(null) && super.accept(visitor);
  }
}
