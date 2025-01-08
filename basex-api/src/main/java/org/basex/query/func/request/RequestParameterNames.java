package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestParameterNames extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final RequestContext requestCtx = requestContext(qc);
    try {
      final HashItemSet cache = new HashItemSet(ItemSet.Mode.ATOMIC, info);
      for(final Item name : requestCtx.queryValues().keys()) cache.add(name);
      for(final Item name : requestCtx.formValues(qc.context.options).keys()) cache.add(name);
      return ItemSeq.get(cache.keys(), cache.size(), null);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw REQUEST_PARAMETER.get(info, requestCtx.queryString());
    }
  }
}
