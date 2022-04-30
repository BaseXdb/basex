package org.basex.query.func.web;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
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
    final String message = toString(exprs[1], qc);
    if(code <= 0 || code > 999) throw WEB_STATUS_X.get(info, code);

    final QNm qname = new QNm(Token.concat(STATUS, code), REST_URI);
    throw new QueryException(info, qname, message).value(Int.get(code));
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
