package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
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
public final class JobsEval extends StandardFunc {
  /** Eval options. */
  public static class EvalOptions extends Options {
    /** Query base-uri. */
    public static final StringOption BASE_URI = new StringOption("base-uri");
    /** Cache result. */
    public static final BooleanOption CACHE = new BooleanOption("cache", false);
  }

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] query = toToken(exprs[0], qc);
    final HashMap<String, Value> bindings = toBindings(1, qc);

    final EvalOptions opts = new EvalOptions();
    if(exprs.length > 2) toOptions(2, null, opts, qc);

    final String uri = opts.get(EvalOptions.BASE_URI);
    final boolean cache = opts.get(EvalOptions.CACHE);

    final Context ctx = qc.context;
    final QueryProcessor qp = new QueryProcessor(string(query), ctx);
    qp.http(qc.http);
    for(final Entry<String, Value> it : bindings.entrySet()) {
      final String key = it.getKey();
      final Value val = CachedXQuery.copy(it.getValue().iter(), ctx);
      if(key.isEmpty()) qp.context(val);
      else qp.bind(key, val);
    }
    qp.parse(uri);

    // check if number of maximum queries has been reached
    if(ctx.jobs.jobs.size() >= JobPool.MAXQUERIES) throw JOBS_OVERFLOW.get(info);

    final CachedXQuery job = new CachedXQuery(qp, cache, info);
    new Thread(job).start();

    return Str.get(job.job().id());
  }
}
