package org.basex.query.func.fn;

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
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDuplicateValues extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter values = arg(0).atomIter(qc, info);
    final Collation collation = toCollation(arg(1), qc);
    return new Iter() {
      final IntSet ints1 = new IntSet(), ints2 = new IntSet();
      ItemSet set1, set2;

      @Override
      public Item next() throws QueryException {
        if(set1 == null) {
          // try to parse input as 32-bit integer sequence
          for(Item item; (item = qc.next(values)) != null;) {
            if(item.type == AtomType.INTEGER) {
              final long l = item.itr(info);
              final int i = (int) l;
              if(i == l) {
                if(!ints1.add(i) && ints2.add(i)) return item;
                continue;
              }
            }
            set1 = ItemSet.get(collation, info);
            set2 = ItemSet.get(collation, info);
            for(final int i : ints1.toArray()) set1.add(Int.get(i));
            for(final int i : ints2.toArray()) set2.add(Int.get(i));
            if(!set1.add(item) && set2.add(item)) return item;
            break;
          }
        }
        // generic fallback
        for(Item item; (item = qc.next(values)) != null;) {
          if(!set1.add(item) && set2.add(item)) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter values = arg(0).atomIter(qc, info);
    final Collation collation = toCollation(arg(1), qc);

    // try to parse input as 32-bit integer sequence
    final LongList list = new LongList();
    final IntSet ints1 = new IntSet(), ints2 = new IntSet();
    Item item = null;
    while((item = qc.next(values)) != null) {
      if(item.type != AtomType.INTEGER) break;
      final long l = item.itr(info);
      final int i = (int) l;
      if(i != l) break;
      if(!ints1.add(i) && ints2.add(i)) list.add(i);
    }
    final Value intseq = IntSeq.get(list.finish());
    if(item == null) return intseq;

    // generic fallback
    final ValueBuilder vb = new ValueBuilder(qc).add(intseq);
    final ItemSet set1 = ItemSet.get(collation, info), set2 = ItemSet.get(collation, info);
    for(final int i : ints1.toArray()) set1.add(Int.get(i));
    for(final int i : ints2.toArray()) set2.add(Int.get(i));
    do {
      if(!set1.add(item) && set2.add(item)) vb.add(item);
    } while((item = qc.next(values)) != null);
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr values = arg(0);
    final SeqType st = values.seqType();
    if(st.zero()) return values;

    final AtomType type = st.type.atomic();
    if(type != null) {
      // assign atomic type of argument
      exprType.assign(type);

      if(!defined(1)) {
        // util:duplicates(1 to 10)  ->  ()
        if(values instanceof RangeSeq || values instanceof Range || st.zeroOrOne())
          return Empty.VALUE;
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
