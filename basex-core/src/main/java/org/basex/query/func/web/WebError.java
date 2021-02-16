package org.basex.query.func.web;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebError extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        return item(qc, info);
      }
    };
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long code = toLong(exprs[0], qc);
    final String message = Token.string(toToken(exprs[1], qc));
    if(code <= 0 || code > 999) throw WEB_STATUS_X.get(info, code);

    throw new QueryException(info, QNm.REST_ERROR, message).value(Int.get(code));
  }

  @Override
  public boolean vacuous() {
    return true;
  }

  @Override
  protected Expr typeCheck(final TypeCheck tc, final CompileContext cc) {
    return this;
  }
}
