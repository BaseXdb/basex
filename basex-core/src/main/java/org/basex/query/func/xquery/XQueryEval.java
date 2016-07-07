package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public class XQueryEval extends StandardFunc {
  /** XQuery options. */
  public static class XQueryOptions extends Options {
    /** Permission. */
    public static final EnumOption<Perm> PERMISSION = new EnumOption<>("permission", Perm.ADMIN);
    /** Timeout in seconds. */
    public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
    /** Maximum amount of megabytes that may be allocated by the query. */
    public static final NumberOption MEMORY = new NumberOption("memory", 0);
    /** Query base-uri. */
    public static final StringOption BASE_URI = new StringOption("base-uri");
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return eval(qc).iter();
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return eval(qc).value();
  }

  /**
   * Evaluates a query.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  protected ItemList eval(final QueryContext qc) throws QueryException {
    return eval(qc, toToken(exprs[0], qc), null, false);
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
  final ItemList eval(final QueryContext qc, final byte[] qu, final String path,
      final boolean updating) throws QueryException {

    // bind variables and context value
    final HashMap<String, Value> bindings = toBindings(1, qc);
    final Options opts = new XQueryOptions();
    if(exprs.length > 2) toOptions(2, opts, qc);

    // allow limited number of nested calls
    QueryContext qcAnc = qc;
    for(int c = 5; qcAnc != null && c > 0; c--) qcAnc = qcAnc.parent;
    if(qcAnc != null) throw BXXQ_NESTED.get(info);

    final User user = qc.context.user();
    final Perm tmp = user.perm("");
    Timer to = null;

    final Perm perm = Perm.get(opts.get(XQueryOptions.PERMISSION).toString());
    if(!user.has(perm)) throw BXXQ_PERM2_X.get(info, perm);
    user.perm(perm);

    try(final QueryContext qctx = new QueryContext(qc)) {
      // limit memory consumption: enforce garbage collection and calculate usage
      final long mb = opts.get(XQueryOptions.MEMORY);
      if(mb != 0) {
        Performance.gc(2);
        final long limit = Performance.memory() + (mb << 20);
        to = new Timer(true);
        to.schedule(new TimerTask() {
          @Override
          // limit reached: stop query
          public void run() { if(Performance.memory() > limit) qctx.memory(); }
        }, 500, 500);
      }

      // timeout
      final long ms = opts.get(XQueryOptions.TIMEOUT) * 1000L;
      if(ms != 0) {
        if(to == null) to = new Timer(true);
        to.schedule(new TimerTask() {
          @Override
          public void run() { qctx.timeout(); }
        }, ms);
      }

      // base-uri: adopt specified uri, passed on uri, or uri from parent query
      final String bu = opts.get(XQueryOptions.BASE_URI);
      String uri = bu != null ? bu : path != null ? path : string(sc.baseURI().string());

      // evaluate query
      try {
        final StaticContext sctx = new StaticContext(qctx);
        for(final Entry<String, Value> it : bindings.entrySet()) {
          final String key = it.getKey();
          final Value val = it.getValue();
          if(key.isEmpty()) qctx.context(val, sctx);
          else qctx.bind(key, val, sctx);
        }
        qctx.parseMain(string(qu), uri, sctx);

        if(updating) {
          if(!sc.mixUpdates && !qctx.updating && !qctx.root.expr.isVacuous())
            throw BXXQ_NOUPDATE.get(info);
        } else {
          if(qctx.updating) throw BXXQ_UPDATING.get(info);
        }

        final ItemList cache = new ItemList();
        final Iter iter = qctx.iter();
        for(Item it; (it = iter.next()) != null;) {
          qctx.checkStop();
          qc.checkStop();
          cache.add(it);
        }
        return cache;
      } catch(final JobException ex) {
        if(qctx.state == JobState.TIMEOUT) throw BXXQ_TIMEOUT.get(info);
        if(qctx.state == JobState.MEMORY)  throw BXXQ_MEMORY.get(info);
        throw ex;
      } catch(final QueryException ex) {
        throw ex.error() == BASX_PERM_X ? BXXQ_PERM_X.get(info, ex.getLocalizedMessage()) :
          ex.info(info);
      }
    } finally {
      if(to != null) to.cancel();
      user.perm(tmp, "");
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(null) && super.accept(visitor);
  }
}
