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
public final class FnVoid extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr input = arg(0);
    final boolean evaluate = toBooleanOrFalse(arg(1), qc);

    // ensure that nondeterministic input will be evaluated
    if(evaluate || input.has(Flag.NDT)) {
      for(final Iter iter = input.iter(qc); qc.next(iter) != null;);
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    if(input.has(Flag.NDT)) {
      if(input.size() == 0) return input;
    } else if(!defined(1) || arg(1) instanceof Value && !toBoolean(arg(1), cc.qc)) {
      return Empty.VALUE;
    }
    return this;
  }
}
