package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnDuplicateValues extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter values = arg(0).atomIter(qc, info);
    final Collation collation = toCollation(arg(1), qc);
    final ItemSet set1 = ItemSet.get(collation, info), set2 = ItemSet.get(collation, info);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(values)) != null;) {
          if(!set1.add(item) && set2.add(item)) return item;
        }
        return null;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr values = arg(0);
    final SeqType st = values.seqType();
    // duplicate-values(()) → ()
    if(st.zero()) return values;

    final BasicType type = st.type.atomic();
    if(type != null) {
      // assign atomic type of argument
      exprType.assign(type);

      if(!defined(1)) {
        // util:duplicates(1 to 10) → ()
        if(values instanceof RangeSeq || values instanceof Range ||
            st.atomOcc().max <= 1)
          return cc.voidAndReturn(values, Empty.VALUE, info);
        // util:duplicates((1 to 3) ! 1) → 1
        if(values instanceof final SingletonSeq ss && !st.mayBeWrapped() && ss.singleItem()) {
          return type == st.type ? ss.itemAt(0) : cc.function(Function.DATA, info, ss.itemAt(0));
        }
      }
    }
    return this;
  }
}
