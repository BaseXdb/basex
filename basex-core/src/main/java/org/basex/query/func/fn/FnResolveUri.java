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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnResolveUri extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] uri = toTokenOrNull(arg(0), qc);
    final byte[] base = toTokenOrNull(arg(1), qc);
    if(uri == null) return Empty.VALUE;

    // check relative uri
    final Uri u = Uri.get(uri);
    if(!u.isValid()) throw URIARG_X.get(info, u);
    if(u.isAbsolute()) return u;

    // check base uri: reject invalid, relative, and non-hierarchical URIs and fragment identifiers
    final Uri b = base == null ? info.sc().baseURI() : Uri.get(base);
    final byte[] string = b.string();
    if(!b.isValid() || !b.isAbsolute() || contains(string, '#') || !contains(string, '/'))
      throw URIARG_X.get(info, b);

    return b.resolve(u, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
