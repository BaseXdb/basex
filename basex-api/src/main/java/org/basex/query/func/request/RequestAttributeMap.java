package org.basex.query.func.request;

import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
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
      final Object object = entry.getValue();
      if(object instanceof final Value value) map.put(entry.getKey(), value);
    }
    return map.map();
  }
}
