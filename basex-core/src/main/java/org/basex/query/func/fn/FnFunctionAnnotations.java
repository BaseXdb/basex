package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionAnnotations extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem function = toFunction(arg(0), qc);

    final AnnList anns = function.annotations();
    if(anns.isEmpty()) return Empty.VALUE;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Ann ann : anns) {
      vb.add(XQMap.singleton(ann.name(), ann.value()));
    }
    return vb.value();
  }
}
