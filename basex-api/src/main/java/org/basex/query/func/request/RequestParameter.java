package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestParameter extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final AStr name = toStr(arg(0), qc);

    final RequestContext requestCtx = requestContext(qc);
    try {
      final ValueBuilder vb = new ValueBuilder(qc);
      vb.add(requestCtx.queryValues().get(name));
      vb.add(requestCtx.formValues(qc.context.options).get(name));
      final Value value = vb.value();
      return value.isEmpty() && defined(1) ? arg(1).value(qc) : value;
    } catch(final IOException ex) {
      Util.debug(ex);
      throw REQUEST_PARAMETER.get(info, requestCtx.queryString());
    }
  }
}
