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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value;
    if(defined(0)) {
      value = toZeroToken(exprs[0], qc);
    } else {
      final Item item = ctxValue(qc).item(qc, info);
      if(item instanceof FItem && !(item instanceof XQJava)) throw FISTRING_X.get(info, item);
      value = item.isEmpty() ? Token.EMPTY : item.string(info);
    }
    return Int.get(Token.length(value));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // if(string-length(E))  ->  if(string(E))
    return cc.simplify(this, mode == Simplify.EBV ? cc.function(STRING, info, exprs) : this, mode);
  }
}
