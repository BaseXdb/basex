package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class InspectFunction extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem fn = toFunc(exprs[0], qc);
    final QNm name = fn.funcName();
    final StaticFunc sf = name == null ? null : qc.funcs.get(name, fn.arity(), null, false);
    return new PlainDoc(qc, info).function(name, sf, fn.funcType(), fn.annotations(), null);
  }
}
