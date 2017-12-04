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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class MapFind extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Array value(final QueryContext qc) throws QueryException {
    final ArrayBuilder builder = new ArrayBuilder();
    find(exprs[0].iter(qc), toAtomItem(exprs[1], qc), builder, qc);
    return builder.freeze();
  }

  /**
   * Finds map entries in the specified iterator.
   * @param ir iterator
   * @param key item to be found
   * @param builder array builder
   * @param qc query context
   * @throws QueryException query exception
   */
  private void find(final Iter ir, final Item key, final ArrayBuilder builder,
      final QueryContext qc) throws QueryException {

    for(Item it; (it = qc.next(ir)) != null;) {
      if(it instanceof Map) {
        final Map map = (Map) it;
        final Value value = map.get(key, info);
        if(value != Empty.SEQ) builder.append(value);
        for(final Item it2 : map.keys()) {
          find(map.get(it2, info).iter(), key, builder, qc);
        }
      } else if(it instanceof Array) {
        for(final Value value : ((Array) it).members()) {
          find(value.iter(), key, builder, qc);
        }
      }
    }
  }
}
