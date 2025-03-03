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
 * @author BaseX Team, BSD License
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
    return eval(toContent(arg(0), qc), false, qc);
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

    // bind variables and context value, parse options
    final HashMap<String, Value> bindings = toBindings(arg(1), qc);
    final XQueryOptions options = new XQueryOptions();
    final User user = qc.context.user();
    options.put(XQueryOptions.PERMISSION, user.permission(""));
    toOptions(arg(2), options, qc);

    final Perm perm = Perm.get(options.get(XQueryOptions.PERMISSION).toString());
    if(!user.has(perm)) throw XQUERY_PERMREQUIRED_X.get(info, perm);

    Timer to = null;
    try(QueryContext qctx = new QueryContext(qc)) {
      qctx.user = new User(user).permission(perm);

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
              if(Performance.memory() > limit) qctx.outOfMemory();
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
      final boolean pass = options.get(XQueryOptions.PASS);
      try {
        final StaticContext sctx = new StaticContext(qctx);
        sctx.baseURI(toBaseUri(query.url(), options, XQueryOptions.BASE_URI));
        for(final Entry<String, Value> binding : bindings.entrySet()) {
          qctx.bind(binding.getKey(), binding.getValue(), null, sctx);
        }
        qctx.parseMain(string(query.read()), null, sctx);

        if(!sc().mixUpdates && updating != qctx.updating) {
          if(!updating) throw XQUERY_NOUPDATES.get(info);
          if(!qctx.main.expr.vacuous()) throw XQUERY_UPDATEEXPECTED.get(info);
        }

        final Value value;
        final Iter iter = qctx.iter();
        if(iter.valueIter()) {
          // value-based iterator: return result unchanged
          value = iter.value(qctx, this);
        } else {
          // collect resulting items
          final ValueBuilder vb = new ValueBuilder(qc);
          for(Item item; (item = qctx.next(iter)) != null;) vb.add(item);
          value = vb.value();
        }
        // return cached result
        value.cache(false, info);
        return value;
      } catch(final JobException ex) {
        QueryError error = null;
        if(qctx.state == JobState.TIMEOUT) error = XQUERY_TIMEOUT;
        else if(qctx.state == JobState.MEMORY) error = XQUERY_MEMORY;
        if(error != null) throw error.get(pass ? new InputInfo(query.path(), 1, 1) : info);
        throw ex;
      } catch(final QueryException ex) {
        Util.debug(ex);
        final QueryError error = ex.error();
        final QueryException qe = error(ex, error == BASEX_PERMISSION_X ||
            error == BASEX_PERMISSION_X_X ? XQUERY_PERM_X : null);
        // pass on error info: assign (possibly empty) path of module which caused the error
        InputInfo ii = ex.info();
        if(pass && ii == null) ii = new InputInfo(query.path(), 1, 1);
        throw qe.info(pass ? ii.path().equals(info.path()) ?
          new InputInfo(query.path(), ii.line(), ii.column()) : ii : info);
      } catch(final StackOverflowError er) {
        // pass on error info: assign (possibly empty) path of module which caused the error
        throw XQUERY_UNEXPECTED_X.get(info, er);
      }
    } finally {
      if(to != null) to.cancel();
    }
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock((String) null) && super.accept(visitor);
  }

  @Override
  public final boolean hasUPD() {
    return sc().mixUpdates || super.hasUPD();
  }
}
