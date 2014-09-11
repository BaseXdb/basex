package org.basex.query.func.sql;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class SqlInit extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    final String driver = string(toToken(exprs[0], qc));
    if(Reflect.find(driver) == null) throw BXSQ_DRIVER_X.get(info, driver);
    return null;
  }
}
