package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.core.cmd.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbOption extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] name = toToken(exprs[0], qc);
    final Object value = Get.get(Token.string(Token.uc(name)), qc.context);
    if(value == null) throw DB_OPTION_X.get(info, name);
    return item(value);
  }

  /**
   * Converts an option value to an XQuery item.
   * @param value value
   * @return item, or {@code null} for empty sequence
   * @throws QueryException query exception
   */
  private Item item(final Object value) throws QueryException {
    if(value == null) return Empty.VALUE;
    if(value instanceof Boolean) return Bln.get((Boolean) value);
    if(value instanceof Integer) return Int.get((Integer) value);
    if(value instanceof Options) {
      XQMap map = XQMap.EMPTY;
      final Options opts = (Options) value;
      for(final Option<?> opt : opts) {
        final Item item = item(opt.value());
        if(item != null) map = map.put(Str.get(opt.name()), item, info);
      }
      return map;
    }
    // string or enumeration
    return Str.get(value.toString());
  }
}
