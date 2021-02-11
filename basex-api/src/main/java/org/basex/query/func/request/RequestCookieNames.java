package org.basex.query.func.request;

import javax.servlet.http.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RequestCookieNames extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    final Cookie[] cookies = request(qc).getCookies();
    if(cookies != null) {
      for(final Cookie c : cookies) tl.add(c.getName());
    }
    return StrSeq.get(tl);
  }
}
