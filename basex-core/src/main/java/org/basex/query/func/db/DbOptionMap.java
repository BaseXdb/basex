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
    return switch(value) {
      case null -> Empty.VALUE;
      case final Boolean bln -> Bln.get(bln);
      case final Integer itr -> Itr.get(itr);
      case final Options options -> {
        final MapBuilder mb = new MapBuilder();
        for(final Option<?> opt : options) {
          mb.put(Str.get(opt.name()), item(options.get(opt)));
        }
        yield mb.map();
      }
      // string or enumeration
      default -> Str.get(value.toString());
    };
  }
}
