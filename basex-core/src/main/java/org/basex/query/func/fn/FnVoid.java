package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnVoid extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr input = arg(0);
    final boolean skip = toBooleanOrFalse(arg(1), qc);

    // ensure that deterministic input will be evaluated
    if(!skip || input.has(Flag.NDT)) {
      final Iter iter = input.iter(qc);
      if(iter.valueIter()) {
        iter.value(qc, null).cache(false, info);
      } else {
        for(Item item; (item = qc.next(iter)) != null;) {
          item.cache(false, info);
        }
      }
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    if(input.has(Flag.NDT)) {
      if(input.size() == 0) return input;
    } else if(defined(1) && arg(1) instanceof Value && toBoolean(arg(1), cc.qc)) {
      return Empty.VALUE;
    }
    return this;
  }
}
