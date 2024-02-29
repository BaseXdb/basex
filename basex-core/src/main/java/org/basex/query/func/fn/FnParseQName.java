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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnParseQName extends StandardFunc {
  @Override
  public QNm item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toToken(arg(0), qc);

    final QNm qnm = qc.shared.parseQName(value, true, sc());
    if(qnm == null) throw valueError(AtomType.QNAME, value, info);
    if(!qnm.hasURI() && qnm.hasPrefix()) throw NSDECL_X.get(info, qnm.prefix());
    return qnm;
  }
}
