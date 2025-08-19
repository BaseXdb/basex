package org.basex.query.func.request;

import java.util.Map.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestAttributeNames extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    for(final Entry<String, Object> entry : HTTPConnection.getAttributes(request(qc)).entrySet()) {
      if(entry.getValue() instanceof Value) tl.add(entry.getKey());
    }
    return StrSeq.get(tl);
  }
}
