package org.basex.query.func.jobs;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
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
  /** Async options. */
  public static class AsyncOptions extends Options {
    /** Query base-uri. */
    public static final StringOption BASE_URI = new StringOption("base-uri");
    /** Cache result. */
    public static final BooleanOption CACHE = new BooleanOption("cache", true);
  }

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] query = toToken(exprs[0], qc);
    final HashMap<String, Value> bindings = toBindings(1, qc);

    final AsyncOptions opts = new AsyncOptions();
    if(exprs.length > 2) toOptions(2, null, opts, qc);

    final String uri = opts.get(AsyncOptions.BASE_URI);
    final boolean cache = opts.get(AsyncOptions.CACHE);

    final Context ctx = qc.context;
    final QueryPool queries = ctx.queries;
    final QueryProcessor qp = new QueryProcessor(string(query), ctx);
    qp.http(qc.http);
    for(final Entry<String, Value> it : bindings.entrySet()) {
      final String key = it.getKey();
      final Value val = queries.copy(it.getValue().iter(), ctx);
      if(key.isEmpty()) qp.context(val);
      else qp.bind(key, val);
    }

    // set base uri
    final String path = ctx.options.get(MainOptions.QUERYPATH);
    if(uri != null) ctx.options.set(MainOptions.QUERYPATH, uri);
    try {
      qp.parse();
    } finally {
      ctx.options.set(MainOptions.QUERYPATH, path);
    }
    return Str.get(queries.add(qp, cache, info));
  }
}
