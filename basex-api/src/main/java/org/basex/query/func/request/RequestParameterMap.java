package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestParameterMap extends ApiFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder map = new MapBuilder();
    final RequestContext requestCtx = requestContext(qc);
    try {
      // cache parameter names
      final XQMap queryValues = requestCtx.queryValues();
      final XQMap formValues = requestCtx.formValues(qc.context.options);
      final HashItemSet names = new HashItemSet(ItemSet.Mode.ATOMIC, info);
      for(final Item name : queryValues.keys()) names.add(name);
      for(final Item name : formValues.keys()) names.add(name);

      for(final Item name : names) {
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
      throw REQUEST_PARAMETER.get(info);
    }
  }
}
