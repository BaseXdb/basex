package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnQName extends StandardFunc {
  @Override
  public QNm item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] uri = toZeroToken(arg(0), qc), qname = toToken(arg(1), qc);
    final byte[] name = !contains(qname, ':') && eq(uri, XML_URI)
        ? concat(XML_COLON, qname) : qname;
    if(!XMLToken.isQName(name)) throw valueError(AtomType.QNAME, qname, info);

    final QNm qnm = qc.qnmPool.get(name, uri);
    if(qnm.hasPrefix() && uri.length == 0) throw valueError(AtomType.ANY_URI, qnm.uri(), info);
    return qnm;
  }
}
