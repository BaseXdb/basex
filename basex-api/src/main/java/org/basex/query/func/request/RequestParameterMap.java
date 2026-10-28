package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestParameterMap extends ApiFunc {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final XQMap queryValues = queryValues(qc), formValues = formValues(qc);
    final HashItemSet names = new HashItemSet(ItemSet.Mode.ATOMIC, info);
    for(final Item name : queryValues.keys()) names.add(name);
    for(final Item name : formValues.keys()) names.add(name);

    final MapBuilder map = new MapBuilder();
    for(final Item name : names) {
      map.put(name, new ValueBuilder(qc).add(queryValues.get(name)).
          add(formValues.get(name)).value());
    }
    return map.map();
  }
}
