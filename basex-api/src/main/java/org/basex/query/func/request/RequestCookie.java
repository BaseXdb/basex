package org.basex.query.func.request;

import javax.servlet.http.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class RequestCookie extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toString(arg(0), qc);
    final Cookie[] cookies = request(qc).getCookies();
    if(cookies != null) {
      for(final Cookie c : cookies) {
        if(c.getName().equals(name)) return Str.get(c.getValue());
      }
    }
    return defined(1) ? Str.get(toToken(arg(1), qc)) : Empty.VALUE;
  }
}
