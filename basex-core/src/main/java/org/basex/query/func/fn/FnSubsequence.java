package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.math.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnSubsequence extends StandardFunc {
  /** All values. */
  private static final SeqRange ALL = new SeqRange(0, Long.MAX_VALUE);
  /** No values. */
  private static final SeqRange EMPTY = new SeqRange(0, 0);

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    // value-based input: return subsequence
    if(eager()) return value(qc).iter();

    // no range: return empty sequence
    final SeqRange sr = range(qc);
    if(sr == EMPTY) return Empty.ITER;

    // return iterator if all results are returned, of it iterator yields no items
    final Iter iter = arg(0).iter(qc);
    if(sr == ALL) return iter;

    // return empty iterator if no items remain
    final long size = sr.adjust(iter.size());
    if(sr.length == 0) return Empty.ITER;

    // value-based iterator
    final Value value = iter.eagerValue();
    if(value != null) return value.subsequence(sr.start, sr.length, qc).iter();

    // size is known: create specific iterator
    if(size != -1) {
      if(sr.length == size) return iter;

      return new Iter() {
        long c = sr.start;

        @Override
        public Item next() throws QueryException {
          return c < sr.end ? iter.get(c++) : null;
        }
        @Override
        public Item get(final long i) throws QueryException {
          return iter.get(sr.start + i);
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
        for(Item item; c < sr.end && (item = qc.next(iter)) != null;) {
          if(++c > sr.start) return item;
        }
        return null;
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    // no range: return empty sequence
    final SeqRange sr = range(qc);
    if(sr == EMPTY) return Empty.VALUE;

    // return iterator if all results are returned, of it iterator yields no items
    final Expr input = arg(0);
    if(sr == ALL) return input.value(qc);

    // value-based input: return subsequence
    Value value = input.eagerValue(qc);
    if(value != null) {
      sr.adjust(value.size());
      return sr.length == 0 ? Empty.VALUE : value.subsequence(sr.start, sr.length, qc);
    }

    // return empty iterator if no items remain
    final Iter iter = input.iter(qc);
    final long size = sr.adjust(iter.size());
    if(sr.length == 0) return Empty.VALUE;

    // value-based iterator
    value = iter.eagerValue();
    if(value != null) return value.subsequence(sr.start, sr.length, qc);

    // size is known: collect by position
    if(size != -1) {
      if(sr.length == size) return iter.value(qc, this);

      final ValueBuilder vb = new ValueBuilder(qc, sr.length);
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

  @Override
  public final boolean eager() {
    return arg(0).eager();
  }

  /**
   * Returns the start position and length of the requested subsequence.
   * @param cc compilation context
   * @return range or {@code null}
   * @throws QueryException query exception
   */
  final SeqRange range(final CompileContext cc) throws QueryException {
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
    final ANum first = toNumber(toAtomItem(arg(1), qc), arg(1)).round(0, mode(true));
    final Item second = arg(2).atomItem(qc, info);
    final long s = index(first, info), e = second.isEmpty() ? Long.MAX_VALUE :
      index(end(first, toNumber(second, arg(2)).round(0, mode(false))), info);
    if(s == -1 || e == -1 || s >= e) return EMPTY;
    return s == 0 && e == Long.MAX_VALUE ? ALL : new SeqRange(s, e);
  }

  /**
   * Returns the rounding mode for positions.
   * @param first first position
   * @return rounding mode
   */
  protected FnRound.RoundMode mode(@SuppressWarnings("unused") final boolean first) {
    return FnRound.RoundMode.HALF_TO_CEILING;
  }

  /**
   * Returns the exclusive end position.
   * @param first rounded first argument
   * @param second rounded second argument
   * @return end position
   * @throws QueryException query exception
   */
  protected ANum end(final ANum first, final ANum second) throws QueryException {
    return add(first, second, info);
  }

  /**
   * Adds two positions, using the arithmetic of the promoted type.
   * @param pos1 first position
   * @param pos2 second position
   * @param info input info
   * @return sum
   * @throws QueryException query exception
   */
  protected static ANum add(final ANum pos1, final ANum pos2, final InputInfo info)
      throws QueryException {
    if(pos1 instanceof Dbl || pos2 instanceof Dbl) return Dbl.get(pos1.dbl() + pos2.dbl());
    if(pos1 instanceof Flt || pos2 instanceof Flt) {
      return Flt.get(pos1.flt(info) + pos2.flt(info));
    }
    if(pos1 instanceof final Itr itr1 && pos2 instanceof final Itr itr2) {
      // overflow (or minimum integer): fall back to decimal arithmetic
      final long sum = Util.add(itr1.itr(), itr2.itr());
      if(sum != Long.MIN_VALUE) return Itr.get(sum);
    }
    return Dec.get(pos1.dec(info).add(pos2.dec(info)));
  }

  /**
   * Converts a rounded one-based position to a zero-based index.
   * @param pos position
   * @param info input info
   * @return index between {@code 0} and {@link Long#MAX_VALUE}, or {@code -1} for NaN
   * @throws QueryException query exception
   */
  protected static long index(final ANum pos, final InputInfo info) throws QueryException {
    if(pos instanceof final Itr itr) return Math.max(itr.itr(), 1) - 1;
    if(pos.floating()) {
      final double d = pos.dbl();
      return Double.isNaN(d) ? -1 : (long) Math.max(d - 1, 0);
    }
    return pos.dec(info).subtract(BigDecimal.ONE).max(BigDecimal.ZERO).
        min(Dec.BD_MAXLONG).longValue();
  }

  /**
   * Checks if this is a range function.
   * @return result of check
   */
  protected boolean range() {
    return false;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values
    final Expr input = arg(0), first = arg(1), second = arg(2);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    long sz = -1;
    final SeqRange sr = range(cc);
    if(sr != null) {
      // no results
      if(sr == EMPTY) return cc.voidAndReturn(input, Empty.VALUE, info);
      // all values?
      if(sr == ALL) return input;
      // ignore standard limitation for large values to speed up evaluation of result
      if(input instanceof Value) return value(cc.qc);

      // check if result size is statically known
      final long size = sr.adjust(input.size());
      if(size != -1) {
        if(sr.length == size) return input;
        // subsequence(E, last) → foot(E)
        if(sr.start == size - 1) return cc.function(FOOT, info, input);
        // subsequence(E, 2) → tail(E)
        if(sr.start == 1 && sr.end == size) return cc.function(TAIL, info, input);
        // subsequence(E, 1, last - 1) → trunk(E)
        if(sr.start == 0 && sr.end == size - 1) return cc.function(TRUNK, info, input);
        sz = sr.length;
      } else if(st.zeroOrOne()) {
        // sr.length is always larger than 0 at this point
        return sr.start == 0 ? input : cc.voidAndReturn(input, Empty.VALUE, info);
      }

      if(sr.length == 1) {
        // subsequence(E, 1, 1) → head(E)
        // subsequence(E, pos, 1) → items-at(E, pos)
        return sr.start == 0 ? cc.function(HEAD, info, input) :
          cc.function(ITEMS_AT, info, input, Itr.get(sr.start + 1));
      }
      // subsequence(E, 2) → tail(E)
      if(sr.end == Long.MAX_VALUE && sr.start == 1)
        return cc.function(TAIL, info, input);
      // subsequence(file:read-text-lines(E), pos, length) → file:read-text-lines(E, pos, length)
      if(_FILE_READ_TEXT_LINES.is(input))
        return FileReadTextLines.merge(this, sr.start, sr.length, cc);
      // subsequence(replicate(I, count), pos, length) → replicate(I, length)
      if(REPLICATE.is(input)) {
        final Expr[] args = input.args().clone();
        if(args[0].size() == 1 && args[1] instanceof Itr) {
          args[1] = Itr.get(sr.length);
          return cc.function(REPLICATE, info, args);
        }
      }
      // subsequence((I1, I2, I3, I4), 2, 2) → (I2, I3)
      // subsequence((I, E1, E2), 2, 2) → subsequence((E1, E2), 1, 2)
      if(input instanceof List && sr.start > 0) {
        final Expr[] args = input.args();
        if(Checks.all(args, ex -> ex.seqType().one())) {
          return List.get(cc, info, Arrays.copyOfRange(args, (int) sr.start, (int) sr.end));
        }
        final int al = args.length;
        for(int a = 0; a < al; a++) {
          final boolean exact = a == sr.start, one = args[a].seqType().one();
          if(a > 0 && (exact || !one)) {
            final Expr list = List.get(cc, info, Arrays.copyOfRange(args, a, al));
            final long start = sr.start - a + 1, end = sr.end - start;
            return cc.function(SUBSEQUENCE, info, list, Itr.get(start), Itr.get(end));
          }
          if(!one) break;
        }
      }
    } else if(first instanceof final Itr itr) {
      final long start = itr.itr(), count = FnItemsAt.countInputDiff(input, second);
      final long diff = count + start;
      if(count != Long.MIN_VALUE && start == (int) start && diff == (int) diff) {
        if(start <= 1) {
          // subsequence(E, 1, count(E) - 1) → trunk(E)
          if(diff == 0) return cc.function(TRUNK, info, input);
          // subsequence(E, 1, count(E) + 10) → E
          if(diff >= 1) return input;
        } else if(start <= diff) {
          // subsequence(E, 3, count(E) - 1) → subsequence(E, 3)
          return cc.function(SUBSEQUENCE, info, input, first);
        }
      }
    } else if(second instanceof final Itr itr) {
      if(!range() && first.seqType().instanceOf(Types.INTEGER_O)) {
        final long length = itr.itr();
        // subsequence(EXPR, START, 1) → items-at(EXPR, START)
        if(length == 1) return cc.function(ITEMS_AT, info, input, first);
        // subsequence(EXPR, START, 0) → ()
        if(length <= 0) return Empty.VALUE;
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
