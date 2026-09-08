package org.basex.query.func.db;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbPropertyMap extends DbAccessFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
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
   * @return item
   */
  static Item item(final Object value) {
    return switch(value) {
      case final Boolean bln -> Bln.get(bln);
      case final Integer itr -> Itr.get(itr);
      case final Long lng -> Itr.get(lng);
      default -> Str.get(value.toString());
    };
  }
}
