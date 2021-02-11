package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InspectFunctionAnnotations extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = XQMap.EMPTY;
    for(final Ann ann : toFunc(exprs[0], qc).annotations()) {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item arg : ann.args()) vb.add(arg);
      map = map.put(ann.name(), vb.value(), info);
    }
    return map;
  }
}
