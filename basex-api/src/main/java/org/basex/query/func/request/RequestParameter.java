package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RequestParameter extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toString(arg(0), qc);

    final RequestContext requestCtx = requestContext(qc);
    try {
      final Value query = requestCtx.queryValues().get(name);
      final Value form = requestCtx.formValues(qc.context.options).get(name);
      if(query == null && form == null) {
        return defined(1) ? arg(1).value(qc) : Empty.VALUE;
      }

      final ValueBuilder vb = new ValueBuilder(qc);
      if(query != null) vb.add(query);
      if(form != null) vb.add(form);
      return vb.value(this);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw REQUEST_PARAMETER.get(info, requestCtx.queryString());
    }
  }
}
