package org.basex.query.func.request;

import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.java.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestAttributeMap extends ApiFunc {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final MapBuilder map = new MapBuilder();
    for(final Entry<String, Object> entry : state(qc).attributes().entrySet()) {
      final Object value = entry.getValue();
      if(value != null) map.put(entry.getKey(), JavaCall.toValue(value, qc, info));
    }
    return map.map();
  }
}
