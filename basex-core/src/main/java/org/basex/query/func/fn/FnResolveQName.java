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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnResolveQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] qname = toTokenOrNull(arg(0), qc);
    final ANode element = toElem(arg(1), qc);
    if(qname == null) return Empty.VALUE;
    if(!XMLToken.isQName(qname)) throw valueError(AtomType.QNAME, qname, info);

    final byte[] prefix = Token.prefix(qname);
    byte[] uri = element.uri(prefix);
    if(uri == null) uri = sc.ns.uri(prefix);
    if(uri == null) throw NSDECL_X.get(info, prefix);
    return qc.shared.qnm(qname, uri);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
