package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DbProperty extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String name = toString(arg(1), qc);
    final MetaProp prop = MetaProp.get(name);
    if(prop == null) throw DB_PROPERTY_X.get(info, name);

    final Object value = prop.value(data.meta);
    if(value instanceof Boolean) return Bln.get((Boolean) value);
    if(value instanceof Integer) return Int.get((Integer) value);
    if(value instanceof Long)    return Int.get((Long)    value);
    return Str.get(value.toString());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return cc.dynamic && allAreValues(true) ? value(cc.qc) : compileData(cc);
  }
}
