package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token;
    if(exprs.length == 0) {
      final Item item = ctxValue(qc).item(qc, info);
      if(item instanceof FItem) throw FISTRING_X.get(info, item.type);
      token = item == Empty.VALUE ? Token.EMPTY : item.string(info);
    } else {
      token = toZeroToken(exprs[0], qc);
    }
    return Int.get(Token.length(token));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    if(mode == Simplify.EBV) {
      // if(string-length(nodes))  ->  if(string(nodes))
      return cc.simplify(this, cc.function(STRING, info, exprs));
    }
    return this;
  }
}
