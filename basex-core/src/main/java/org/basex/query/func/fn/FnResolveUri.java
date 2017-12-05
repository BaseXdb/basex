package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
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
    final Item item = exprs[0].atomItem(qc, info);
    final byte[] base = exprs.length > 1 ? toToken(exprs[1], qc) : null;
    if(item == null) return null;

    // check relative uri
    final Uri rel = Uri.uri(toToken(item));
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
