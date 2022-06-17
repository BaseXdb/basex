package org.basex.query.func.store;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class StoreWrite extends StoreFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = exprs.length > 0 ? toName(0, qc) : "";
    try {
      store(qc).write(name);
    } catch(final IOException ex) {
      throw STORE_IO_X.get(info, ex);
    }
    return Empty.VALUE;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(null, false) && super.accept(visitor);
  }
}
