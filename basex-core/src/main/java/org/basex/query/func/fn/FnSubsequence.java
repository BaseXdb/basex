package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnSubsequence extends StandardFunc {
  /** All values. */
  private static final SeqRange ALL = new SeqRange(0, Long.MAX_VALUE);
  /** No values. */
  private static final SeqRange EMPTY = new SeqRange(0, 0);

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // no range: return empty sequence
    final SeqRange sr = range(qc);
    if(sr == EMPTY) return Empty.ITER;

    // return iterator if all results are returned, of it iterator yields no items
    final Iter input = arg(0).iter(qc);
    if(sr == ALL) return input;

    // return empty iterator if no items remain
    final long size = sr.adjust(input.size());
    if(sr.length == 0) return Empty.ITER;

    // return subsequence if iterator is value-based
    if(input.valueIter()) {
      return input.value(qc, null).subsequence(sr.start, sr.length, qc).iter();
    }
    // size is known: create specific iterator
    if(size != -1) {
      if(sr.length == size) return input;

      return new Iter() {
        long c = sr.start;

        @Override
        public Item next() throws QueryException {
          return c < sr.end ? input.get(c++) : null;
        }
        @Override
        public Item get(final long i) throws QueryException {
          return input.get(sr.start + i);
        }
        @Override
        public long size() {
          return sr.length;
        }
      };
    }
    // otherwise, create standard iterator
    return new Iter() {
      long c;

      @Override
      public Item next() throws QueryException {
        for(Item item; c < sr.end && (item = qc.next(input)) != null;) {
          if(++c > sr.start) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // no range: return empty sequence
    final SeqRange sr = range(qc);
    if(sr == EMPTY) return Empty.VALUE;

    // return iterator if all results are returned, of it iterator yields no items
    final Expr input = arg(0);
    if(sr == ALL) return input.value(qc);

    // return empty iterator if no items remain
    final Iter iter = input.iter(qc);
    final long size = sr.adjust(iter.size());
    if(sr.length == 0) return Empty.VALUE;

    // return subsequence if iterator is value-based
    if(iter.valueIter()) {
      return iter.value(qc, null).subsequence(sr.start, sr.length, qc);
    }
    // size is known: collect by position
    if(size != -1) {
      if(sr.length == size) return iter.value(qc, this);

      final ValueBuilder vb = new ValueBuilder(qc);
      for(long i = sr.start; i < sr.end; i++) vb.add(iter.get(i));
      return vb.value(this);
    }
    // otherwise, collect via iterator
    final ValueBuilder vb = new ValueBuilder(qc);
    long c = 0;
    for(Item item; c < sr.end && (item = qc.next(iter)) != null; c++) {
      if(c >= sr.start) vb.add(item);
    }
    return vb.value(this);
  }

  /**
   * Returns the start position and length of the requested subsequence.
   * @param cc compilation context
   * @return range or {@code null}
   * @throws QueryException query exception
   */
  SeqRange range(final CompileContext cc) throws QueryException {
    return arg(1) instanceof Value && (!defined(2) || arg(2) instanceof Value) ?
      range(cc.qc) : null;
  }

  /**
   * Returns the start position and length of the requested subsequence.
   * @param qc query context
   * @return range (start, end, length)
   * @throws QueryException query exception
   */
  private SeqRange range(final QueryContext qc) throws QueryException {
    double start = toDouble(arg(1), qc);
    final Item end = arg(2).atomItem(qc, info);
    if(Double.isNaN(start)) return EMPTY;

    final long s = start(start);
    long e = Long.MAX_VALUE;
    if(!end.isEmpty()) {
      start = toDouble(end);
      if(Double.isNaN(start) || s == Long.MIN_VALUE && start == Double.POSITIVE_INFINITY) {
        return EMPTY;
      }
      e = end(s, start);
    }
    if(e == Long.MAX_VALUE && s <= 1) return ALL;
    if(s == Long.MIN_VALUE) return EMPTY;

    // return all values, no values, or the specified range
    final SeqRange sr = new SeqRange(Math.max(0, s - 1), e);
    return sr.length == 0 ? EMPTY : sr;
  }

  /**
   * Returns the start position.
   * @param value double value
   * @return long value
   */
  public long start(final double value) {
    return StrictMath.round(value);
  }

  /**
   * Computes the count of items to be returned.
   * @param first first argument
   * @param second second argument
   * @return length
   */
  public long end(final long first, final double second) {
    final long l = StrictMath.round(second);
    return l == Long.MAX_VALUE ? l : l + first - 1;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    long sz = -1;
    final SeqRange sr = range(cc);
    if(sr != null) {
      // no results
      if(sr == EMPTY) return Empty.VALUE;
      // all values?
      if(sr == ALL) return input;
      // ignore standard limitation for large values to speed up evaluation of result
      if(input instanceof Value) return value(cc.qc);

      // check if result size is statically known
      final long size = sr.adjust(input.size());
      if(size != -1) {
        if(sr.length == size) return input;
        // subsequence(E, last)  ->  foot(E)
        if(sr.start == size - 1) return cc.function(FOOT, info, input);
        // subsequence(E, 2)  ->  tail(E)
        if(sr.start == 1 && sr.end == size) return cc.function(TAIL, info, input);
        // subsequence(E, 1, last - 1)  ->  trunk(E)
        if(sr.start == 0 && sr.end == size - 1) return cc.function(TRUNK, info, input);
        sz = sr.length;
      } else if(st.zeroOrOne()) {
        // sr.length is always larger than 0 at this point
        return sr.start == 0 ? input : Empty.VALUE;
      }

      if(sr.length == 1) {
        // subsequence(E, 1, 1)  ->  head(E)
        // subsequence(E, pos, 1)  ->  items-at(E, pos)
        return sr.start == 0 ? cc.function(HEAD, info, input) :
          cc.function(ITEMS_AT, info, input, Int.get(sr.start + 1));
      }
      // subsequence(E, 2)  ->  tail(E)
      if(sr.length == Long.MAX_VALUE && sr.start == 1)
        return cc.function(TAIL, info, input);
      // subsequence(file:read-text-lines(E), pos, length)  ->  file:read-text-lines(E, pos, length)
      if(_FILE_READ_TEXT_LINES.is(input))
        return FileReadTextLines.opt(this, sr.start, sr.length, cc);
      // subsequence(replicate(I, count), pos, length)  ->  replicate(I, length)
      if(REPLICATE.is(input)) {
        final Expr[] args = input.args().clone();
        if(args[0].size() == 1 && args[1] instanceof Int) {
          args[1] = Int.get(sr.length);
          return cc.function(REPLICATE, info, args);
        }
      }
      // subsequence((I1, I2, I3, I4), 2, 2)  ->  (I2, I3)
      // subsequence((I, E1, E2), 2, 2)  ->  subsequence((E1, E2), 1, 2)
      if(input instanceof List && sr.start > 0) {
        final Expr[] args = input.args();
        if(((Checks<Expr>) ex -> ex.seqType().one()).all(args)) {
          return List.get(cc, info, Arrays.copyOfRange(args, (int) sr.start, (int) sr.end));
        }
        final int al = args.length;
        for(int a = 0; a < al; a++) {
          final boolean exact = a == sr.start, one = args[a].seqType().one();
          if(a > 0 && (exact || !one)) {
            final Expr list = List.get(cc, info, Arrays.copyOfRange(args, a, al));
            final long start = sr.start - a + 1, end = sr.end - start;
            return cc.function(SUBSEQUENCE, info, list, Int.get(start), Int.get(end));
          }
          if(!one) break;
        }
      }
    } else if(arg(1) instanceof Int) {
      final long start = ((Int) arg(1)).itr();
      final long diff = FnItemsAt.countInputDiff(arg(0), arg(2)) + start;
      if(diff == (int) diff) {
        if(start <= 1) {
          // subsequence(E, 1, count(E) - 1)  ->  trunk(E)
          if(diff == 0) return cc.function(TRUNK, info, input);
          // subsequence(E, 1, count(E) + 10)  ->  E
          if(diff >= 1) return input;
        } else if(start <= diff) {
          // subsequence(E, 3, count(E) - 1)  ->  subsequence(E, 3)
          return cc.function(SUBSEQUENCE, info, input, arg(1));
        }
      }
    }

    exprType.assign(st.union(Occ.ZERO), sz).data(input);
    return embed(cc, false);
  }

  @Override
  public final boolean ddo() {
    return arg(0).ddo();
  }
}
