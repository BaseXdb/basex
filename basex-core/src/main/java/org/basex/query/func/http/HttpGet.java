package org.basex.query.func.http;

import java.time.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.map.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HttpGet extends StandardFunc {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final String href = toString(arg(0), qc);
    final HttpOptions options = toOptions(arg(1), new HttpOptions(), qc);

    final Request request = new Request();
    request.method = Method.GET.name();
    request.followRedirect = options.get(HttpOptions.FOLLOW_REDIRECT);
    final Integer timeout = options.get(HttpOptions.TIMEOUT);
    if(timeout != null) request.timeout = Duration.ofSeconds(timeout);

    return new Client(info, qc.context.options).send(href, request, qc.resources);
  }
}
