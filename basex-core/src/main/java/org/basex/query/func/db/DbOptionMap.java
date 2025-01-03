package org.basex.query.func.db;

import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbOptionMap extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder map = new MapBuilder();
    for(final Entry<String, Object> option : qc.context.options().entrySet()) {
      map.put(option.getKey(), item(option.getValue()));
    }
    return map.map();
  }

  /**
   * Converts an option value to an XQuery item.
   * @param value value
   * @return item, or {@code null} for empty sequence
   * @throws QueryException query exception
   */
  final Item item(final Object value) throws QueryException {
    if(value == null) return Empty.VALUE;
    if(value instanceof Boolean) return Bln.get((Boolean) value);
    if(value instanceof Integer) return Int.get((Integer) value);
    if(value instanceof Options) {
      final Options options = (Options) value;
      final MapBuilder mb = new MapBuilder();
      for(final Option<?> opt : options) {
        final Item item = item(options.get(opt));
        if(item != null) mb.put(Str.get(opt.name()), item);
      }
      return mb.map();
    }
    // string or enumeration
    return Str.get(value.toString());
  }
}
