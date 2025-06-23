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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbPropertyMap extends DbAccessFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);

    final MapBuilder map = new MapBuilder();
    for(final MetaProp prop : MetaProp.values()) {
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
  static Item item(final Object value) {
    if(value instanceof final Boolean bln) return Bln.get(bln);
    if(value instanceof final Integer itr) return Itr.get(itr);
    if(value instanceof final Long lng)    return Itr.get(lng);
    return Str.get(value.toString());
  }
}
