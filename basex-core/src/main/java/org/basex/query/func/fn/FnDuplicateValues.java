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

    final ItemSet set1 = ItemSet.get(collation, info);
    final ItemSet set2 = ItemSet.get(collation, info);
    final IntSet ints1 = new IntSet(), ints2 = new IntSet();

    return new Iter() {
      boolean intseq = seqType().eq(SeqType.INTEGER_ZM);

      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(values)) != null;) {
          if(intseq) {
            if(item.type == AtomType.INTEGER) {
              final long l = item.itr(info);
              final int i = (int) l;
              if(i == l) {
                if(!ints1.add(i) && ints2.add(i)) return item;
                continue;
              }
            }
            // fallback (input is no 32bit integer)
            intseq = false;
            for(final int i : ints1.toArray()) set1.add(Int.get(i));
            for(final int i : ints2.toArray()) set2.add(Int.get(i));
          }
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

    final ItemSet set1 = ItemSet.get(collation, info);
    final ItemSet set2 = ItemSet.get(collation, info);
    final IntSet ints1 = new IntSet(), ints2 = new IntSet();

    final ValueBuilder vb = new ValueBuilder(qc);
    final LongList list = new LongList();

    boolean intseq = seqType().eq(SeqType.INTEGER_ZM);
    for(Item item; (item = qc.next(values)) != null;) {
      if(intseq) {
        if(item.type == AtomType.INTEGER) {
          final long l = item.itr(info);
          final int i = (int) l;
          if(i == l) {
            if(!ints1.add(i) && ints2.add(i)) list.add(i);
            continue;
          }
        }
        // fallback (input is no 32bit integer)
        intseq = false;
        for(final int i : ints1.toArray()) set1.add(Int.get(i));
        for(final int i : ints2.toArray()) set2.add(Int.get(i));
        for(final long l : list.finish()) vb.add(Int.get(l));
      }
      if(!set1.add(item) && set2.add(item)) vb.add(item);
    }
    return intseq ? IntSeq.get(list.finish()) : vb.value(this);
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
