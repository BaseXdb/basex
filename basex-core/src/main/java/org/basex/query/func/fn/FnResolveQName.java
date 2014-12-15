package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnResolveQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    if(it == null) return null;
    final ANode base = toElem(exprs[1], qc);

    final byte[] name = toToken(it);
    if(!XMLToken.isQName(name)) throw valueError(info, AtomType.QNM, name);

    final QNm nm = new QNm(name);
    final byte[] pref = nm.prefix();
    byte[] uri = base.uri(pref);
    if(uri == null) uri = sc.ns.uri(pref);
    if(uri == null) throw NSDECL_X.get(info, pref);
    nm.uri(uri);
    return nm;
  }
}
