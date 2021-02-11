package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnResolveUri extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] item = toTokenOrNull(exprs[0], qc);
    final byte[] base = exprs.length > 1 ? toToken(exprs[1], qc) : null;
    if(item == null) return Empty.VALUE;

    // check relative uri
    final Uri rel = Uri.uri(item);
    if(!rel.isValid()) throw URIARG_X.get(info, rel);
    if(rel.isAbsolute()) return rel;

    // check base uri: reject invalid, relative, and non-hierarchical URIs and fragment identifiers
    final Uri uri = base == null ? sc.baseURI() : Uri.uri(base);
    final byte[] string = uri.string();
    if(!uri.isValid() || !uri.isAbsolute() || contains(string, '#') || !contains(string, '/'))
      throw URIARG_X.get(info, uri);

    return uri.resolve(rel, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
