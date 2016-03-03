package org.basex.query.func.util;

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
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class UtilFunctionAnnotations extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Map map = Map.EMPTY;
    for(final Ann ann : toFunc(exprs[0], qc).annotations()) {
      final ValueBuilder vb = new ValueBuilder();
      for(final Item item : ann.args()) vb.add(item);
      map = map.put(ann.name(), vb.value(), info);
    }
    return map;
  }
}
