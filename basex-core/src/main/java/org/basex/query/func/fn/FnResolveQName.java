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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnResolveQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    final ANode element = toElem(arg(1), qc);
    if(value == null) return Empty.VALUE;
    if(!XMLToken.isQName(value)) throw valueError(AtomType.QNAME, value, info);

    final byte[] prefix = Token.prefix(value);
    byte[] uri = element.uri(prefix);
    if(uri == null) uri = info.sc().ns.uri(prefix);
    if(uri == null) throw NSDECL_X.get(info, prefix);
    return qc.shared.qName(value, uri);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
