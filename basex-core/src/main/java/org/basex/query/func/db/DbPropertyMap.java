package org.basex.query.func.db;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class DbPropertyMap extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);

    final MapBuilder map = new MapBuilder();
    for(final MetaProp prop : MetaProp.ENUMS) {
      map.put(prop.name().toLowerCase(Locale.ENGLISH), item(prop.value(data.meta)));
    }
    return map.map();
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    return cc.dynamic && values(true, cc) ? value(cc.qc) : compileData(cc);
  }

  /**
   * Converts a property value to an XQuery item.
   * @param value value
   * @return item, or {@code null} for empty sequence
   */
  final Item item(final Object value) {
    if(value instanceof Boolean) return Bln.get((Boolean) value);
    if(value instanceof Integer) return Int.get((Integer) value);
    if(value instanceof Long)    return Int.get((Long)    value);
    return Str.get(value.toString());
  }
}
