package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class SqlInit extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final String driver = toString(arg(0), qc);
    try {
      Reflect.forName(driver);
    } catch(final Throwable th) {
      throw SQL_INIT_X.get(info, driver).cause(th);
    }
    return Empty.VALUE;
  }
}
