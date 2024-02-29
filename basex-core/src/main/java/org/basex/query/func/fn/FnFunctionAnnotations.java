package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnFunctionAnnotations extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem function = toFunction(arg(0), qc);

    final MapBuilder mb = new MapBuilder();
    for(final Ann ann : function.annotations()) {
      mb.put(ann.name(), ann.value());
    }
    return mb.map();
  }
}
