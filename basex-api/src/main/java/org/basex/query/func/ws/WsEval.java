package org.basex.query.func.ws;

import java.nio.*;
import java.util.function.*;

import org.basex.core.jobs.*;
import org.basex.http.web.*;
import org.basex.http.ws.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsEval extends WsFn {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final JobOptions options = toOptions(arg(2), new JobOptions(), qc);
    // the result is serialized by the job
    if(options.get(JobOptions.SERIALIZER) == null)
      options.set(JobOptions.SERIALIZER, new SerializerOptions());
    final QueryJobSpec spec = toJobSpec(arg(0), arg(1), options, false, qc);
    final WebSocket ws = ws(qc);
    final Consumer<QueryJobResult> notify = result -> {
      // a stopped job has neither a result nor an error: nothing is sent
      if(result.value == null && result.exception == null) return;
      try {
        // the outcome of a query is one message; a failed query is reported as error
        final Value value = result.get();
        ws.send(value instanceof final Bin bin ? ByteBuffer.wrap(bin.binary(null)) :
          Token.string(((Item) value).string(null)));
      } catch(final Exception ex) {
        ws.error(ex);
      }
    };

    final QueryJob job = new QueryJob(spec, WebText.WEBSOCKET, qc.context.detach(), info, notify,
        null);
    return Str.get(job.jc().id());
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    evalFunc();
    return super.compile(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitJobSpec(visitor);
  }
}
