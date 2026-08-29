package org.basex.query.func.proc;

import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcPropertyMap extends StandardFunc {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final MapBuilder map = new MapBuilder();
    for(final Entry<String, String> entry : Prop.entries()) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map.map();
  }
}
