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
public class FnDuplicateValues extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter values = arg(0).atomIter(qc, info);
    final Collation collation = toCollation(arg(1), qc);
    return new Iter() {
      IntSet ints1 = new IntSet(), ints2 = new IntSet();
      ItemSet set1, set2;

      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(values)) != null;) {
          if(ints1 != null) {
            // try to treat items as 32-bit integers
            final int v = toInt(item);
            if(v != Integer.MIN_VALUE) {
              if(!ints1.add(v) && ints2.add(v)) return item;
              continue;
            }
            set1 = ItemSet.get(collation, info);
            for(final int i : ints1.keys()) set1.add(Int.get(i));
            ints1 = null;
            set2 = ItemSet.get(collation, info);
            for(final int i : ints2.keys()) set2.add(Int.get(i));
            ints2 = null;
          }
          // fallback
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

    // try to treat items as 32-bit integers
    final LongList list = new LongList();
    IntSet ints1 = new IntSet(), ints2 = new IntSet();
    Item item = null;
    while((item = qc.next(values)) != null) {
      final int v = toInt(item);
      if(v == Integer.MIN_VALUE) break;
      if(!ints1.add(v) && ints2.add(v)) list.add(v);
    }
    final Value intseq = IntSeq.get(list.finish());
    if(item == null) return intseq;

    // fallback
    final ValueBuilder vb = new ValueBuilder(qc).add(intseq);
    final ItemSet set1 = ItemSet.get(collation, info), set2 = ItemSet.get(collation, info);
    for(final int i : ints1.keys()) set1.add(Int.get(i));
    for(final int i : ints2.keys()) set2.add(Int.get(i));
    ints1 = null;
    ints2 = null;
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

  /**
   * Tries to convert the item to an integer (excluding {@link Integer#MIN_VALUE}).
   * @param item item
   * @return integer or {@link Integer#MIN_VALUE}
   */
  public static int toInt(final Item item) {
    if(item.type == AtomType.INTEGER) {
      final long l = ((Int) item).itr();
      final int i = (int) l;
      if(i == l) return i;
    }
    return Integer.MIN_VALUE;
  }
}
