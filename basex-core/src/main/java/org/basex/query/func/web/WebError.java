package org.basex.query.func.web;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
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
    final Value message = arg(1).value(qc);
    final XQMap options = toEmptyMap(arg(2), qc);
    if(status <= 0 || status > 999) throw WEB_STATUS_X.get(info, status);

    // serialization parameters (default: plain text)
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, SerialMethod.TEXT);
    sopts.assign(options, info);

    // description: message text for a plain string, generic phrase for a structured body
    final String desc = message instanceof final AStr str ? Token.string(str.string(info)) :
      "HTTP " + status;
    final QNm qname = new QNm(Token.concat(STATUS, status), REST_URI);
    throw new QueryException(info, qname, desc).value(message).output(sopts);
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
