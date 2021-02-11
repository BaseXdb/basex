package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FetchContentType extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] uri = toToken(exprs[0], qc);
    final IO io = IO.get(Token.string(uri));

    final String path = io.path();
    MediaType mt = null;
    if(io instanceof IOUrl) {
      try {
        final String ct = ((IOUrl) io).connection().getContentType();
        if(ct != null) mt = new MediaType(ct);
      } catch(final IOException ex) {
        throw FETCH_OPEN_X.get(info, ex);
      }
    } else if(io instanceof IOContent) {
      mt = MediaType.APPLICATION_XML;
    } else if(io.exists()) {
      mt = MediaType.get(path);
    }
    if(mt == null) throw FETCH_OPEN_X.get(info, new FileNotFoundException(path));
    return Str.get(mt.toString());
  }
}
