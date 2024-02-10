package org.basex.query.func.proc;

import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ProcPropertyMap extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder map = new MapBuilder(info);
    for(final Entry<String, String> entry : Prop.entries()) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map.map();
  }
}
