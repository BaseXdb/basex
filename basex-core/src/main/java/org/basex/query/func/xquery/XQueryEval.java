package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.*;

import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.io.*;
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
 * @author BaseX Team 2005-21, BSD License
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
    /** Pass on error info. */
    public static final BooleanOption PASS = new BooleanOption("pass", false);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return eval(toQuery(0, qc), false, qc);
  }

  /**
   * Evaluates the specified string.
   * @param query query
   * @param updating updating query
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final Value eval(final IOContent query, final boolean updating, final QueryContext qc)
      throws QueryException {

    // bind variables and context value
    final HashMap<String, Value> bindings = toBindings(1, qc);
    final Options opts = toOptions(2, new XQueryOptions(), qc);

    // allow limited number of nested calls
    QueryContext qcAnc = qc;
    for(int c = 5; qcAnc != null && c > 0; c--) qcAnc = qcAnc.parent;
    if(qcAnc != null) throw XQUERY_NESTED.get(info);

    final User user = qc.context.user();
    final Perm tmp = user.perm("");
    Timer to = null;

    final Perm perm = Perm.get(opts.get(XQueryOptions.PERMISSION).toString());
    if(!user.has(perm)) throw XQUERY_PERMISSION2_X.get(info, perm);
    user.perm(perm);

    try(QueryContext qctx = new QueryContext(qc)) {
      // limit memory consumption: enforce garbage collection and calculate usage
      final long mb = opts.get(XQueryOptions.MEMORY);
      if(mb != 0) {
        Performance.gc(2);
        final long limit = Performance.memory() + (mb << 20);
        to = new Timer(true);
        to.schedule(new TimerTask() {
          @Override
          // limit reached: stop query if garbage collection does not help
          public void run() {
            if(!qctx.stopped() && Performance.memory() > limit) {
              Performance.gc(1);
              if(Performance.memory() > limit) qctx.memory();
            }
          }
        }, 250, 250);
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

      // evaluate query
      try {
        final StaticContext sctx = new StaticContext(qctx);
        sctx.baseURI(toBaseUri(query.url(), opts));
        for(final Entry<String, Value> it : bindings.entrySet()) {
          final String key = it.getKey();
          final Value value = it.getValue();
          if(key.isEmpty()) qctx.context(value, sctx);
          else qctx.bind(key, value, sctx);
        }
        qctx.parseMain(string(query.read()), null, sctx);

        if(updating) {
          if(!sc.mixUpdates && !qctx.updating && !qctx.root.expr.vacuous())
            throw XQUERY_UPDATE2.get(info);
        } else {
          if(qctx.updating) throw XQUERY_UPDATE1.get(info);
        }

        final ValueBuilder vb = new ValueBuilder(qc);
        final Iter iter = qctx.iter();
        for(Item item; (item = qctx.next(iter)) != null;) {
          qc.checkStop();
          vb.add(item);
        }
        return vb.value();
      } catch(final JobException ex) {
        if(qctx.state == JobState.TIMEOUT) throw XQUERY_TIMEOUT.get(info);
        if(qctx.state == JobState.MEMORY)  throw XQUERY_MEMORY.get(info);
        throw ex;
      } catch(final QueryException ex) {
        final QueryError error = ex.error();
        if(error == BASEX_PERMISSION_X || error == BASEX_PERMISSION_X_X) {
          Util.debug(ex);
          throw XQUERY_PERMISSION1_X.get(info, ex.getLocalizedMessage());
        }
        if(!opts.get(XQueryOptions.PASS)) ex.info(info);
        throw ex;
      }
    } finally {
      if(to != null) to.cancel();
      user.perm(tmp, "");
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock(null, false) && super.accept(visitor);
  }
}
