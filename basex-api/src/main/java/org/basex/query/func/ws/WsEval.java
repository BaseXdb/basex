package org.basex.query.func.ws;

import java.nio.*;
import java.util.function.*;

import org.basex.core.jobs.*;
import org.basex.http.ws.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsEval extends WsFn {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final WsOptions options = toOptions(arg(2), new WsOptions(), qc);
    final QueryJobSpec spec = toJobSpec(arg(0), arg(1), options, false, qc);
    final WebSocket ws = ws(qc);
    final SerializerOptions sopts = options.get(WsOptions.SERIALIZER);
    final Consumer<QueryJobResult> notify = result -> {
      // a stopped job has neither a result nor an error: nothing is sent
      if(result.value == null && result.exception == null) return;
      try {
        // the outcome of a query is one message; a failed query is reported as error
        final Value value = result.get();
        final ArrayOutput ao = value.serialize(sopts);
        ws.send(value instanceof Bin ? ByteBuffer.wrap(ao.toArray()) : ao.toString());
      } catch(final Exception ex) {
        ws.error(ex);
      }
    };

    final QueryJob job = new QueryJob(spec, qc.context, info, notify, null);
    return Str.get(job.jc().id());
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitJobSpec(visitor);
  }
}
