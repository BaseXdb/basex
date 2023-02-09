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
 * @author BaseX Team 2005-23, BSD License
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
    return eval(toContent(0, qc), false, qc);
  }

  /**
   * Evaluates the specified string as XQuery expression.
   * @param query query
   * @param updating updating query
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final Value eval(final IOContent query, final boolean updating, final QueryContext qc)
      throws QueryException {

    // allow limited number of nested calls
    QueryContext qcAnc = qc;
    for(int c = 5; qcAnc != null && c > 0; c--) qcAnc = qcAnc.parent;
    if(qcAnc != null) throw XQUERY_NESTED.get(info);

    final User user = qc.context.user();
    final Perm perm = user.perm("");
    Timer to = null;

    // bind variables and context value, parse options
    final HashMap<String, Value> bindings = toBindings(1, qc);
    final XQueryOptions options = new XQueryOptions();
    options.put(XQueryOptions.PERMISSION, perm);
    toOptions(2, options, true, qc);

    final Perm evalPerm = Perm.get(options.get(XQueryOptions.PERMISSION).toString());
    if(!user.has(evalPerm)) throw XQUERY_PERMISSION2_X.get(info, evalPerm);
    user.perm(evalPerm);

    try(QueryContext qctx = new QueryContext(qc)) {
      // limit memory consumption: enforce garbage collection and calculate usage
      final long mb = options.get(XQueryOptions.MEMORY);
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
      final long ms = options.get(XQueryOptions.TIMEOUT) * 1000L;
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
        sctx.baseURI(toBaseUri(query.url(), options, XQueryOptions.BASE_URI));
        for(final Entry<String, Value> binding : bindings.entrySet()) {
          qctx.bind(binding.getKey(), binding.getValue(), null, sctx);
        }
        qctx.parseMain(string(query.read()), null, sctx);

        if(!sc.mixUpdates && updating != qctx.updating) {
          if(!updating) throw XQUERY_UPDATE1.get(info);
          if(!qctx.main.expr.vacuous()) throw XQUERY_UPDATE2.get(info);
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
        if(!options.get(XQueryOptions.PASS)) ex.info(info);
        throw ex;
      }
    } finally {
      if(to != null) to.cancel();
      user.perm(perm, "");
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock((String) null) && super.accept(visitor);
  }

  @Override
  public boolean updating() {
    return sc.mixUpdates || super.updating();
  }
}
