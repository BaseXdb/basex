package org.basex.query.func.prof;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
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
public final class ProfRuntime extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] name = toTokenOrNull(arg(0), qc);
    final Runtime rt = Runtime.getRuntime();

    final MapBuilder map = new MapBuilder();
    map.put("used", Itr.get(rt.totalMemory() - rt.freeMemory()));
    map.put("total", Itr.get(rt.totalMemory()));
    map.put("max", Itr.get(rt.maxMemory()));
    map.put("processors", Itr.get(rt.availableProcessors()));
    if(name == null) return map.map();

    final Value value = map.get(Str.get(name));
    if(value != null) return (Item) value;
    throw PROF_OPTION_X.get(info, name);
  }
}
