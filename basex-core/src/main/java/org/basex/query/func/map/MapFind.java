package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class MapFind extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Array value(final QueryContext qc) throws QueryException {
    return find(qc.iter(exprs[0]), toAtomItem(exprs[1], qc), Array.empty());
  }

  /**
   * Finds map entries in the specified iterator.
   * @param ir iterator
   * @param key item to be found
   * @param array array
   * @return results
   * @throws QueryException query exception
   */
  private Array find(final Iter ir, final Item key, final Array array) throws QueryException {
    Array a = array;
    for(Item it; (it = ir.next()) != null;) {
      if(it instanceof Map) {
        final Map map = (Map) it;
        final Value value = map.get(key, info);
        if(value != Empty.SEQ) a = a.snoc(value);
        for(final Item item : map.keys()) {
          a = find(map.get(item, info).iter(), key, a);
        }
      } else if(it instanceof Array) {
        for(final Value value : ((Array) it).members()) {
          a = find(value.iter(), key, a);
        }
      }
    }
    return a;
  }
}
