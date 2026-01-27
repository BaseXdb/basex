package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public class MapForEach extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final FItem action = toFunction(arg(1), 3, this instanceof UpdateMapForEach, qc);

    return new Iter() {
      final long size = action.funcType().declType.one() ? map.structSize() : -1;
      final BasicIter<Item> keys = map.keys().iter();
      final HofArgs args = new HofArgs(3, action);
      Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = iter.next();
          if(item != null) return item;
          final Item key = keys.next();
          if(key == null) return null;
          iter = invoke(action, args.set(0, key).set(1, map.get(key)).inc(), qc).iter();
        }
      }

      @Override
      public Item get(final long i) throws QueryException {
        final Item key = keys.get((int) i);
        return invoke(action, args.set(0, key).set(1, map.get(key)).inc(), qc).item(qc, info);
      }

      @Override
      public long size() {
        return size;
      }
    };
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0);
    if(map == XQMap.empty()) return Empty.VALUE;

    if(map.seqType().type instanceof final MapType mt) {
      final SeqType declType = SeqType.get(mt.keyType(), Occ.EXACTLY_ONE);
      arg(1, arg -> refineFunc(arg, cc, declType, mt.valueType(), Types.INTEGER_O));
    }

    final FuncType ft = arg(1).funcType();
    if(ft != null) exprType.assign(ft.declType.type);

    return this;
  }
}
