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
    final AStr value;
    if(defined(0)) {
      value = toZeroStr(arg(0), qc);
    } else {
      final Item item = context(qc).item(qc, info);
      if(item instanceof AStr) value = (AStr) item;
      else if(item instanceof FItem && !(item instanceof XQJava)) throw FISTRING_X.get(info, item);
      else if(item.isEmpty()) return Int.ZERO;
      else return Int.get(Token.length(item.string(info)));
    }
    return Int.get(value.length(info));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // if(string-length(E))  ->  if(string(E))
    return cc.simplify(this, mode == Simplify.EBV ? cc.function(STRING, info, exprs) : this, mode);
  }
}
