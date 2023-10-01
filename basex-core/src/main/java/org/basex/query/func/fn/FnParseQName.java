package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
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
public final class FnParseQName extends StandardFunc {
  @Override
  public QNm item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = trim(toToken(arg(0), qc));
    final QNm qnm;
    if(XMLToken.isQName(value)) {
      qnm = qc.shared.qname(value, sc.ns.uri(prefix(value)));
    } else {
      qnm = qc.shared.eqname(value);
    }
    if(qnm == null) throw valueError(AtomType.QNAME, value, info);
    if(!qnm.hasURI() && qnm.hasPrefix()) throw NSDECL_X.get(info, qnm.prefix());
    return qnm;
  }
}
