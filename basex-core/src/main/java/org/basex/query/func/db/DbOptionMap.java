package org.basex.query.func.db;

import java.util.Map.*;

import org.basex.core.users.*;
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
    final boolean admin = qc.has(Perm.ADMIN, null, info);
    for(final Entry<String, Object> option : qc.context.options(admin).entrySet()) {
      map.put(option.getKey(), toValue(option.getValue()));
    }
    return map.map();
  }

  /**
   * Converts an option value to an XQuery value.
   * @param value value (can be {@code null})
   * @return value
   * @throws QueryException query exception
   */
  static Value toValue(final Object value) throws QueryException {
    return switch(value) {
      case null -> Empty.VALUE;
      case final Value val -> val;
      case final Boolean bln -> Bln.get(bln);
      case final Integer itr -> Itr.get(itr);
      case final Long lng -> Itr.get(lng);
      case final int[] ints -> IntSeq.get(ints);
      case final Options options -> {
        final MapBuilder mb = new MapBuilder();
        for(final Option<?> opt : options) {
          mb.put(Str.get(opt.name()), toValue(options.get(opt)));
        }
        yield mb.map();
      }
      // string or enumeration
      default -> Str.get(value.toString());
    };
  }
}
