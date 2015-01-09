package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnResolveUri extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    final byte[] bs = exprs.length > 1 ? toToken(exprs[1], qc) : null;
    if(it == null) return null;

    // check relative uri
    final Uri rel = Uri.uri(toToken(it));
    if(!rel.isValid()) throw URIARG_X.get(info, rel);
    if(rel.isAbsolute()) return rel;

    // check base uri
    final Uri base = bs == null ? sc.baseURI() : Uri.uri(bs);
    if(!base.isAbsolute()) throw URINOTABS_X.get(info, base);
    if(!base.isValid() || contains(base.string(), '#') || !contains(base.string(), '/'))
      throw URIARG_X.get(info, base);

    return base.resolve(rel, info);
  }
}
