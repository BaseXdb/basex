package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnForEachPair extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FItem fun = checkArity(exprs[2], 2, qc);
    final Iter ir1 = exprs[0].iter(qc), ir2 = exprs[1].iter(qc);
    return new Iter() {
      Iter ir = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item item = ir.next();
          if(item != null) return item;
          final Item it1 = ir1.next(), it2 = ir2.next();
          if(it1 == null || it2 == null) return null;
          ir = fun.invokeValue(qc, info, it1, it2).iter();
        } while(true);
      }
    };
  }
}
