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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class InspectFunctionAnnotations extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder mb = new MapBuilder(info);
    for(final Ann ann : toFunction(exprs[0], qc).annotations()) {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item arg : ann.value()) vb.add(arg);
      mb.put(ann.name(), vb.value());
    }
    return mb.map();
  }
}
