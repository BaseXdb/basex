package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnQName extends StandardFunc {
  @Override
  public QNm value(final QueryContext qc) throws QueryException {
    final byte[] uri = toZeroToken(arg(0), qc), qname = toToken(arg(1), qc);
    if(!XMLToken.isQName(qname)) throw valueError(BasicType.QNAME, qname, info);

    final QNm qnm = qc.shared.qName(qname, uri);
    if(qnm.hasPrefix() && uri.length == 0) throw valueError(BasicType.ANY_URI, qnm.uri(), info);
    return qnm;
  }
}
