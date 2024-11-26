package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RequestParameterMap extends ApiFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder map = new MapBuilder();
    final RequestContext requestCtx = requestContext(qc);
    try {
      // cache parameter names
      final Map<String, Value> queryValues = requestCtx.queryValues();
      final Map<String, Value> formValues = requestCtx.formValues(qc.context.options);
      final HashSet<String> cache = new HashSet<>();
      cache.addAll(queryValues.keySet());
      cache.addAll(formValues.keySet());

      for(final String name : cache) {
        final ValueBuilder vb = new ValueBuilder(qc);
        final Value query = queryValues.get(name);
        final Value form = formValues.get(name);
        if(query != null) vb.add(query);
        if(form != null) vb.add(form);
        map.put(name, vb.value());
      }

      return map.map();
    } catch(final IOException ex) {
      Util.debug(ex);
      throw REQUEST_PARAMETER.get(info, requestCtx.queryString());
    }
  }
}
