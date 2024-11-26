package org.basex.query.func.request;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

import jakarta.servlet.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RequestHeader extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final HttpServletRequest request = request(qc);
    final String name = toString(arg(0), qc);

    final TokenList list = new TokenList(1);
    for(final String value : Collections.list(request.getHeaders(name))) list.add(value);
    return !list.isEmpty() ? StrSeq.get(list) : defined(1) ? Str.get(toToken(arg(1), qc)) :
      Empty.VALUE;
  }
}
