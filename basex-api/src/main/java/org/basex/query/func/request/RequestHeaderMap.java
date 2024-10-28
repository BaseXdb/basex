package org.basex.query.func.request;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

import jakarta.servlet.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RequestHeaderMap extends ApiFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final HttpServletRequest request = request(qc);

    final MapBuilder map = new MapBuilder();
    for(final String name : Collections.list(request.getHeaderNames())) {
      map.put(name, Str.get(request.getHeader(name)));
    }
    return map.map();
  }

}
