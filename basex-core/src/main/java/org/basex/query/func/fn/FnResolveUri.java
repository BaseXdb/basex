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
 * @author BaseX Team 2005-17, BSD License
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

    // check base uri: reject invalid, relative, and non-hierarchical URIs and fragment identifiers
    final Uri base = bs == null ? sc.baseURI() : Uri.uri(bs);
    final byte[] string = base.string();
    if(!base.isValid() || !base.isAbsolute() || contains(string, '#') || !contains(string, '/'))
      throw URIARG_X.get(info, base);

    return base.resolve(rel, info);
  }
}
