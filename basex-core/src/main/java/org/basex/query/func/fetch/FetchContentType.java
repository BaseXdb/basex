package org.basex.query.func.fetch;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions for fetching resources.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FetchContentType extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] uri = toToken(exprs[0], qc);
    final IO io = IO.get(Token.string(uri));

    final String path = io.path();
    final String mt;
    if(io instanceof IOUrl) {
      try {
        mt = ((IOUrl) io).connection().getContentType();
      } catch(final IOException ex) {
        throw BXFE_IO_X.get(info, ex);
      }
    } else if(io instanceof IOContent) {
      mt = MimeTypes.APP_XML;
    } else {
      mt = io.exists() ? MimeTypes.get(path) : null;
    }
    if(mt == null) throw BXFE_IO_X.get(info, new FileNotFoundException(path));
    return Str.get(mt);
  }
}
