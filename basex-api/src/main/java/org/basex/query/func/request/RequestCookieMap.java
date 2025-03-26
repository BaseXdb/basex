package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

import jakarta.servlet.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestCookieMap extends ApiFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder map = new MapBuilder();
    final Cookie[] cookies = request(qc).getCookies();
    if(cookies != null) {
      for(final Cookie c : cookies) {
        map.put(c.getName(), Str.get(c.getValue()));
      }
    }
    return map.map();
  }
}
