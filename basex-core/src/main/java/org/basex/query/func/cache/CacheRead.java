package org.basex.query.func.cache;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class CacheRead extends CacheFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = exprs.length > 0 ? toName(0, qc) : "";
    try {
      if(!cache(qc).read(name, qc)) throw CACHE_NOTFOUND_X.get(info, name);
    } catch(final IOException ex) {
      throw CACHE_IO_X.get(info, ex);
    }
    return Empty.VALUE;
  }
}
