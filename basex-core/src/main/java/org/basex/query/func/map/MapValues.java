package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class MapValues extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final BasicIter<Item> keys = map.keys().iter();

    return new Iter() {
      Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = iter.next();
          if(item != null) return item;
          final Item key = keys.next();
          if(key == null) return null;
          iter = map.get(key, info).iter();
        }
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    map.values(vb);
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof MapType) exprType.assign(ft.declType.with(Occ.ZERO_OR_MORE));
    return this;
  }
}
