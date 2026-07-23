package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreUpdate extends StoreFn {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem update = toFunction(arg(0), 1, qc);
    final String name = toName(arg(1), qc);

    return Bln.get(stores(qc).update(name, entries -> {
      final XQMap result = toMap(invoke(update, new HofArgs(1).set(0, entries), qc).item(qc, info));
      if(result == entries) return result;

      XQMap map = result;
      long same = 0;
      for(final XQMap.Entry entry : result.entries()) {
        final Item key = entry.key();
        final Value value = entry.value();
        // entries that are still stored have already been checked and compactified
        if(key.type == BasicType.STRING && value.equals(entries.getOrNull(key))) {
          same++;
        } else {
          final Item ky = key.type == BasicType.STRING ? key : Str.get(toToken(key));
          final Value compacted = compact(value, qc);
          if(ky != key) map = map.remove(key);
          map = compacted.isEmpty() ? map.remove(ky) : map.put(ky, compacted);
        }
      }
      // report no change if all entries are identical to the stored ones
      return same == result.structSize() && same == entries.structSize() ? entries : map;
    }, info, qc));
  }
}
