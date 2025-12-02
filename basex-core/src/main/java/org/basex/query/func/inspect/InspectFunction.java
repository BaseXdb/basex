package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectFunction extends StandardFunc {
  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem function = toFunction(arg(0), qc);

    final QNm name = function.funcName();
    StaticFunc func = null;
    if(name != null) {
      final int arity = function.arity();
      func = qc.functions.get(ii.sc(), name, arity);
      if(func == null) {
        for(final StaticFunc sf : qc.functions) {
          if(!sf.annotations().contains(Annotation.PRIVATE) && sf.funcName().eq(name)
              && sf.minArity() <= arity && sf.arity() >= arity) {
            func = sf;
            break;
          }
        }
      }
    }
    return new PlainDoc(qc, info).function(name, func, function.funcType(), function.annotations());
  }
}
