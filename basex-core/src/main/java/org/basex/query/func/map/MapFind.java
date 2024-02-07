package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class MapFind extends StandardFunc {
  @Override
  public XQArray value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Item key = toAtomItem(arg(1), qc);

    final ArrayBuilder ab = new ArrayBuilder();
    find(input, key, ab, qc);
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    if(input.seqType().zero()) return cc.voidAndReturn(input, XQArray.empty(), info);

    return input == XQMap.empty() || input == XQArray.empty() ? XQArray.empty() : this;
  }

  /**
   * Finds map entries in the specified iterator.
   * @param iter iterator
   * @param key item to be found
   * @param builder array builder
   * @param qc query context
   * @throws QueryException query exception
   */
  private void find(final Iter iter, final Item key, final ArrayBuilder builder,
      final QueryContext qc) throws QueryException {

    for(Item item; (item = qc.next(iter)) != null;) {
      if(item instanceof XQMap) {
        final XQMap map = (XQMap) item;
        final Value value = map.get(key, info);
        if(!value.isEmpty()) builder.append(value);
        map.apply((k, val) -> find(val.iter(), key, builder, qc));
      } else if(item instanceof XQArray) {
        for(final Value value : ((XQArray) item).members()) {
          find(value.iter(), key, builder, qc);
        }
      }
    }
  }
}
