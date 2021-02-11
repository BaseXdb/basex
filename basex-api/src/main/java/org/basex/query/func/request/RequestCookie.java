package org.basex.query.func.request;

import javax.servlet.http.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RequestCookie extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = Token.string(toToken(exprs[0], qc));
    final Cookie[] cookies = request(qc).getCookies();
    if(cookies != null) {
      for(final Cookie c : cookies) {
        if(c.getName().equals(name)) return Str.get(c.getValue());
      }
    }
    return exprs.length == 1 ? Empty.VALUE : Str.get(toToken(exprs[1], qc));
  }
}
