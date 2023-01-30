package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class UtilDuplicates extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter values = exprs[0].atomIter(qc, info);
    final Collation coll = toCollation(1, qc);

    final ItemSet set = coll == null ? new HashItemSet(false) : new CollationItemSet(coll);
    final ItemSet dupl = coll == null ? new HashItemSet(false) : new CollationItemSet(coll);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(values)) != null;) {
          if(!set.add(item, info) && dupl.add(item, info)) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr values = exprs[0];
    final SeqType st = values.seqType();
    if(st.zero()) return values;

    final AtomType type = st.type.atomic();
    if(type != null) {
      // assign atomic type of argument
      exprType.assign(type);

      if(exprs.length < 2) {
        // util:duplicates(1 to 10)  ->  ()
        if(values instanceof RangeSeq || values instanceof Range ||
            st.zeroOrOne()) return Empty.VALUE;
        // util:duplicates((1 to 3) ! 1)  ->  1
        if(values instanceof SingletonSeq && !st.mayBeArray()) {
          final SingletonSeq ss = (SingletonSeq) values;
          if(ss.singleItem()) {
            return type == st.type ? ss.itemAt(0) : cc.function(Function.DATA, info, ss.itemAt(0));
          }
        }
      }
    }
    return this;
  }
}
