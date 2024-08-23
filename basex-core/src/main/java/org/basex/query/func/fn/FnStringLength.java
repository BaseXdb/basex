package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int length;
    if(defined(0)) {
      length = toZeroStr(arg(0), qc).length(info);
    } else {
      final Item item = context(qc).item(qc, info);
      if(item.isEmpty()) return Int.ZERO;
      if(item instanceof FItem && !(item instanceof XQJava)) throw FISTRING_X.get(info, item);
      length = item instanceof AStr ? ((AStr) item).length(info) : Token.length(item.string(info));
    }
    return Int.get(length);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.EBV) {
      // if(string-length(E))  ->  if(string(E))
      expr = cc.function(STRING, info, exprs);
    }
    return cc.simplify(this, expr, mode);
  }
}
