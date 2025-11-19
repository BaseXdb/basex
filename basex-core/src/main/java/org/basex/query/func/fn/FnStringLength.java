package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Itr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = context(qc).item(qc, info);
    // optimization to return pre-computed string length
    return item.isEmpty() ? Itr.ZERO :
      Itr.get(item instanceof final AStr str ? str.length(info) : Token.length(item.string(info)));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.EBV) {
      // if(string-length(E)) → if(string(E))
      expr = cc.function(STRING, info, exprs);
    }
    return cc.simplify(this, expr, mode);
  }
}
