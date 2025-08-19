package org.basex.query.func.request;

import java.util.Map.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestAttributeMap extends ApiFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder map = new MapBuilder();
    for(final Entry<String, Object> entry : HTTPConnection.getAttributes(request(qc)).entrySet()) {
      final Object object = entry.getValue();
      if(object instanceof Value value) map.put(entry.getKey(), value);
    }
    return map.map();
  }
}
