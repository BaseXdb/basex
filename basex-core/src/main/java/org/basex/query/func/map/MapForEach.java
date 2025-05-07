package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
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
    final FItem action = toFunction(arg(1), 2, MapForEach.this instanceof UpdateMapForEach, qc);
    final BasicIter<Item> keys = map.keys().iter();

    return new Iter() {
      final HofArgs args = new HofArgs(2);
      Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = iter.next();
          if(item != null) return item;
          final Item key = keys.next();
          if(key == null) return null;
          iter = invoke(action, args.set(0, key).set(1, map.get(key)), qc).iter();
        }
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final FItem action = toFunction(arg(1), 2, this instanceof UpdateMapForEach, qc);

    final HofArgs args = new HofArgs(2);
    final ValueBuilder vb = new ValueBuilder(qc, map.structSize());
    map.forEach((key, value) -> vb.add(invoke(action, args.set(0, key).set(1, value), qc)));
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0);
    if(map == XQMap.empty()) return Empty.VALUE;

    final Type type = map.seqType().type;
    if(type instanceof final MapType mt) {
      final SeqType declType = SeqType.get(mt.keyType(), Occ.EXACTLY_ONE);
      arg(1, arg -> refineFunc(arg, cc, declType, mt.valueType()));
    }

    final FuncType ft = arg(1).funcType();
    if(ft != null) exprType.assign(ft.declType.type);

    return this;
  }

  @Override
  public final int hofIndex() {
    return 1;
  }
}
