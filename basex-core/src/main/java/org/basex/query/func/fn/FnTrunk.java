package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
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
public class FnTrunk extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // retrieve and decrement iterator size
    final Iter input = arg(0).iter(qc);
    final long size = input.size();

    // return empty iterator if iterator yields 0 or 1 items, or if result is an empty sequence
    if(size == 0 || size == 1) return Empty.ITER;

    // check if iterator is value-based
    if(input.valueIter()) return input.value(qc, null).subsequence(0, size - 1, qc).iter();

    // return optimized iterator if result size is known
    if(size != -1) return new Iter() {
      int c;

      @Override
      public Item next() throws QueryException {
        return ++c < size ? qc.next(input) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return input.get(i);
      }
      @Override
      public long size() {
        return size - 1;
      }
    };

    // otherwise, return standard iterator
    return new Iter() {
      Item last = input.next();

      @Override
      public Item next() throws QueryException {
        final Item item = last;
        if(item != null) {
          last = input.next();
          if(last == null) return null;
        }
        return item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // return empty sequence if value has 0 or 1 items
    final Value input = arg(0).value(qc);
    final long size = input.size();
    return size < 1 ? input : input.subsequence(0, size - 1, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values to speed up evaluation of result
    final Expr input = arg(0);
    if(input instanceof Value) return value(cc.qc);

    final SeqType st = input.seqType();
    if(st.zeroOrOne()) return Empty.VALUE;

    final long size = input.size();
    if(size != -1) {
      // util:init(TWO-RESULTS)  ->  head(TWO-RESULTS)
      if(size == 2) return cc.function(HEAD, info, input);
      // util:init(util:init(E))  ->  subsequence(E, 1, size - 2)
      if(TRUNK.is(input))
        return cc.function(SUBSEQUENCE, info, input.arg(0), Int.ONE, Int.get(size - 1));
    }
    // util:init(subsequence(E, pos, length))  ->  subsequence(E, pos, length - 1)
    if(SUBSEQUENCE.is(input) || _UTIL_RANGE.is(input)) {
      final SeqRange r = SeqRange.get(input, cc);
      if(r != null) return cc.function(SUBSEQUENCE, info, input.arg(0),
          Int.get(r.start + 1), Int.get(r.length - 1));
    }
    // util:init(replicate(I, count))  ->  replicate(I, count - 1)
    if(REPLICATE.is(input)) {
      final Expr[] args = input.args().clone();
      if(args[1] instanceof Int && args[0].seqType().zeroOrOne()) {
        args[1] = Int.get(((Int) args[1]).itr() - 1);
        return cc.function(REPLICATE, info, args);
      }
    }

    // util:init(1, (2 to 5)))  ->  1, util:init(2 to 5))
    if(input instanceof List) {
      final Expr[] args = input.args();
      final Expr last = args[args.length - 1];
      if(last.seqType().oneOrMore()) {
        args[args.length - 1] = cc.function(TRUNK, info, last);
        return List.get(cc, info, args);
      }
    }

    exprType.assign(st.union(Occ.ZERO), size - 1).data(input);
    return embed(cc, false);
  }
}
