package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnParseQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final QNm qnm = qc.shared.parseQName(value, true, sc());
    if(qnm == null) throw valueError(AtomType.QNAME, value, info);
    if(!qnm.hasURI() && qnm.hasPrefix()) throw NSDECL_X.get(info, qnm.prefix());
    return qnm;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
