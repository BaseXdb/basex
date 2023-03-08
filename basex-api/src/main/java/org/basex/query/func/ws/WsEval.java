package org.basex.query.func.ws;

import java.util.*;
import java.util.function.*;

import org.basex.core.jobs.*;
import org.basex.http.ws.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class WsEval extends WsFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IOContent query = toContent(arg(0), qc);
    final HashMap<String, Value> bindings = toBindings(arg(1), qc);
    final WsOptions options = toOptions(arg(2), new WsOptions(), true, qc);

    final JobOptions jopts = new JobOptions();
    jopts.set(JobOptions.BASE_URI, toBaseUri(query.url(), options, WsOptions.BASE_URI));
    jopts.set(JobOptions.ID, options.get(WsOptions.ID));

    final QueryJobSpec spec = new QueryJobSpec(jopts, bindings, query);
    final WebSocket ws = ws(qc);
    final Consumer<QueryJobResult> notify = result -> {
      try {
        WsPool.send(result.value, ws.id);
      } catch(final Exception ex) {
        ws.error(ex);
      }
    };

    final QueryJob job = new QueryJob(spec, qc.context, info, notify, qc);
    return Str.get(job.jc().id());
  }
}
