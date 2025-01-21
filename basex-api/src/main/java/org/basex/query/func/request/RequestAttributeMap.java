package org.basex.query.func.request;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.java.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

import jakarta.servlet.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestAttributeMap extends ApiFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final HttpServletRequest request = request(qc);

    final MapBuilder map = new MapBuilder();
    for(final String name : Collections.list(request.getAttributeNames())) {
      map.put(name, JavaCall.toValue(request.getAttribute(name), qc, info));
    }
    return map.map();
  }

}
