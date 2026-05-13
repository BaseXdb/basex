package org.basex.query.func.web;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebError extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long status = toLong(arg(0), qc);
    final String message = toString(arg(1), qc);
    if(status <= 0 || status > 999) throw WEB_STATUS_X.get(info, status);

    final QNm qname = new QNm(Token.concat(STATUS, status), REST_URI);
    throw new QueryException(info, qname, message).value(Itr.get(status));
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
