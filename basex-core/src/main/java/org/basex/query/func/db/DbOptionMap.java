package org.basex.query.func.db;

import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbOptionMap extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final MapBuilder map = new MapBuilder();
    for(final Entry<String, Object> option : qc.context.options().entrySet()) {
      map.put(option.getKey(), item(option.getValue()));
    }
    return map.map();
  }

  /**
   * Converts an option value to an XQuery item.
   * @param value value (can be {@code null})
   * @return item
   * @throws QueryException query exception
   */
  static Item item(final Object value) throws QueryException {
    if(value == null) return Empty.VALUE;
    if(value instanceof final Boolean bln) return Bln.get(bln);
    if(value instanceof final Integer itr) return Itr.get(itr);
    if(value instanceof final Options options) {
      final MapBuilder mb = new MapBuilder();
      for(final Option<?> opt : options) {
        mb.put(Str.get(opt.name()), item(options.get(opt)));
      }
      return mb.map();
    }
    // string or enumeration
    return Str.get(value.toString());
  }
}
