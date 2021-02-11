package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbProperty extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String name = string(toToken(exprs[1], qc));
    final MetaProp prop = MetaProp.get(name);
    if(prop == null) throw DB_PROPERTY_X.get(info, name);

    final Object value = prop.value(data.meta);
    if(value instanceof Boolean) return Bln.get((Boolean) value);
    if(value instanceof Integer) return Int.get((Integer) value);
    if(value instanceof Long)    return Int.get((Long)    value);
    return Str.get(value.toString());
  }
}
