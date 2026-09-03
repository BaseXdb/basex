package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
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
    public static final ValueOption TIMEOUT =
        new ValueOption("timeout", BasicType.DECIMAL.seqType(), Dec.ZERO);
    /** Maximum amount of megabytes that may be allocated by the query. */
    public static final NumberOption MEMORY = new NumberOption("memory", 0);
    /** Query base-uri. */
    public static final StringOption BASE_URI = new StringOption(CommonOptions.BASE_URI);
    /** Pass on error info. */
    public static final BooleanOption PASS = new BooleanOption("pass", false);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return eval(false, qc);
  }

  /**
   * Evaluates the first argument as XQuery expression or invokes it as function.
   * @param updating updating query
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final Value eval(final boolean updating, final QueryContext qc) throws QueryException {
    // allow limited number of nested calls
    QueryContext qcAnc = qc;
    for(int c = 5; qcAnc != null && c > 0; c--) qcAnc = qcAnc.parent;
    if(qcAnc != null) throw XQUERY_NESTED.get(info);

    // resolve query or function to be invoked
    final Item input = arg(0).unwrappedItem(qc, info);
    final FuncItem function = toInvocable(input, qc);
    final IOContent query = function != null ? null : toContent(input, qc);

    // parse options
    final XQueryOptions options = new XQueryOptions();
    final User user = qc.user;
    options.put(XQueryOptions.PERMISSION, user.permission(""));
    toOptions(arg(2), options, qc);

    final Perm perm = Enums.get(Perm.class, options.get(XQueryOptions.PERMISSION).toString());
    if(!user.has(perm)) throw XQUERY_NOPERM_X.get(info, perm);

    // bind variables and context value, or resolve the arguments of the invoked function
    final HashMap<String, Value> bindings = function != null ? null : toBindings(arg(1), qc);
    final Value[] args = function != null ? toArguments(arg(1), function, qc) : null;

    Timer to = null;
    try(QueryContext qctx = new QueryContext(qc, null)) {
      qctx.user = new User(user).permission(perm);

      // limit memory consumption
      final long mb = options.get(XQueryOptions.MEMORY);
      if(mb != 0) qc.context.jobs.watchMemory(qctx, mb);

      // timeout
      final long ms = ((ANum) options.get(XQueryOptions.TIMEOUT)).dec(info).
          multiply(BigDecimal.valueOf(1000)).longValue();
      if(ms > 0) {
        to = new Timer(true);
        to.schedule(new TimerTask() {
          @Override
          public void run() { qctx.timeout(); }
        }, ms);
      }

      // evaluate query; a function has no path to report
      final boolean pass = options.get(XQueryOptions.PASS) && query != null;
      try {
        if(function != null) {
          qctx.assign(function, args);
        } else {
          final StaticContext sctx = new StaticContext(qctx);
          sctx.baseURI(toBaseUri(query.url(), options, XQueryOptions.BASE_URI));
          for(final Entry<String, Value> binding : bindings.entrySet()) {
            qctx.bind(binding.getKey(), binding.getValue(), null, sctx);
          }
          qctx.parseMain(string(query.read()), null, sctx);
        }

        if(updating != qctx.updating) {
          if(!updating) throw XQUERY_NOUPDATES.get(info);
          if(!qctx.main.expr.vacuous()) throw XQUERY_UPDATEEXPECTED.get(info);
        }

        final Iter iter = qctx.iter();
        Value value = iter.eagerValue();
        if(value == null) {
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
        final QueryError error = ex.error();
        final QueryException qe = error(ex, error == BASEX_PERMISSION_X_X ? XQUERY_PERM_X :
          error == BASEX_OVERFLOW ? XQUERY_UNEXPECTED_X : null);
        // pass on error info: assign (possibly empty) path of module which caused the error
        InputInfo ii = ex.info();
        if(pass && ii == null) ii = new InputInfo(query.path(), 1, 1);
        throw qe.info(pass ? ii.path().equals(info.path()) ?
          new InputInfo(query.path(), ii.line(), ii.column(), ii.decl()) : ii : info);
      } catch(final StackOverflowError er) {
        // pass on error info: assign (possibly empty) path of module which caused the error
        throw XQUERY_UNEXPECTED_X.get(info, er);
      } finally {
        qc.context.jobs.unwatchMemory(qctx);
      }
    } finally {
      if(to != null) to.cancel();
    }
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    // databases cannot be detected statically; the nested query is updating or read-only
    return visitor.lock((String) null, hasUPD()) && super.accept(visitor);
  }
}
