package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnResolveQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] name = toTokenOrNull(exprs[0], qc);
    final ANode base = toElem(exprs[1], qc);
    if(name == null) return Empty.VALUE;
    if(!XMLToken.isQName(name)) throw valueError(AtomType.QNAME, name, info);

    final QNm qname = new QNm(name);
    final byte[] pref = qname.prefix();
    byte[] uri = base.uri(pref);
    if(uri == null) uri = sc.ns.uri(pref);
    if(uri == null) throw NSDECL_X.get(info, pref);
    qname.uri(uri);
    return qname;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
