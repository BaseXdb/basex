package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.func.*;
import org.basex.query.func.Function;
import org.basex.query.func.file.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
public class FnItemsAt extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return seqType().zeroOrOne() ? evalItem(qc).iter() : evalIter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return seqType().zeroOrOne() ? evalItem(qc) : evalIter(qc).value(qc, this);
  }

  /**
   * Evaluates the function for a single position.
   * @param qc query context
   * @return item or empty value
   * @throws QueryException query exception
   */
  private Item evalItem(final QueryContext qc) throws QueryException {
    final Expr input = arg(0);
    final long at = toLong(arg(1), qc) - 1;

    // retrieve (possibly invalid) position
    if(at < 0) return Empty.VALUE;
    // if possible, retrieve single item
    if(input.seqType().zeroOrOne()) return at == 0 ? input.item(qc, info) : Empty.VALUE;

    // fast route if the size is known
    final Iter iter = input.iter(qc);
    final long size = iter.size();
    if(size >= 0) return at < size ? iter.get(at) : Empty.VALUE;

    // iterate until specified item is found
    long p = at;
    for(Item item; (item = qc.next(iter)) != null;) {
      if(p-- == 0) return item;
    }
    return Empty.VALUE;
  }

  /**
   * Evaluates the function for multiple positions.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter evalIter(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final Iter at = arg(1).iter(qc);
    final long size = input.size();
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(at)) != null;) {
          final long a = toLong(item) - 1;
          if(a >= 0 && a < size) return input.itemAt(a);
        }
        return null;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), at = arg(1);
    final SeqType ist = input.seqType(), ast = at.seqType();
    if(ist.zero()) return input;
    if(ast.zero()) return Empty.VALUE;

    Occ occ = ast.zeroOrOne() ? Occ.ZERO_OR_ONE : Occ.ZERO_OR_MORE;
    if(at instanceof Item && at.size() == 1) {
      final long ps = toLong(at, cc.qc) - 1;
      // negative position
      if(ps < 0) return Empty.VALUE;
      // single expression with static position
      if(ist.zeroOrOne()) return ps == 0 ? input : Empty.VALUE;

      final long size = input.size();
      if(size != -1) {
        // items-at(E, last)  ->  util:last(E)
        if(ps + 1 == size) return cc.function(FOOT, info, input);
        // items-at(E, too-large)  ->  ()
        if(ps + 1 > size) return Empty.VALUE;
        // items-at(reverse(E), pos)  ->  items-at(E, size - pos)
        if(REVERSE.is(input))
          return cc.function(ITEMS_AT, info, input.arg(0), Int.get(size - ps));
        occ = Occ.EXACTLY_ONE;
      }
      if(ps == 0) return cc.function(HEAD, info, input);

      // items-at(tail(E), pos)  ->  items-at(E, pos + 1)
      if(TAIL.is(input))
        return cc.function(ITEMS_AT, info, input.arg(0), Int.get(ps + 2));
      // items-at(replicate(I, count), pos)  ->  I
      if(REPLICATE.is(input)) {
        // static integer will always be greater than 1
        final Expr[] args = input.args();
        if(args[0].size() == 1 && args[1] instanceof Int) {
          final long count = ((Int) args[1]).itr();
          return ps > count ? Empty.VALUE : args[0];
        }
      }
      // items-at(file:read-text-lines(E), pos)  ->  file:read-text-lines(E, pos, 1)
      if(_FILE_READ_TEXT_LINES.is(input))
        return FileReadTextLines.opt(this, ps, 1, cc);

      // items-at((I1, I2, I3), 2)  ->  I2
      // items-at((I, E), 2)  ->  head(E)
      // items-at((I, E1, E2), 3)  ->  items-at((E1, E2), 2)
      if(input instanceof List) {
        final Expr[] args = input.args();
        final int al = args.length;
        for(int a = 0; a < al; a++) {
          final boolean exact = a == ps, one = args[a].seqType().one();
          if(exact || !one && a > 0) {
            if(exact && one) return args[a];
            final Expr list = List.get(cc, info, Arrays.copyOfRange(args, a, al));
            return exact ? cc.function(HEAD, info, list) :
              cc.function(ITEMS_AT, info, list, Int.get(ps - a + 1));
          }
          if(!one) break;
        }
      }
    }

    final long diff = countInputDiff(arg(0), arg(1));
    if(diff != Long.MIN_VALUE) {
      // items-at(E, count(E))  ->  util:last(E)
      if(diff == 0) return cc.function(FOOT, info, input);
      // items-at(E, count(E) + 1)  ->  ()
      if(diff > 0) return Empty.VALUE;
    }

    // items-at(E, start to end)  ->  util:range(E, start, end)
    if(at instanceof RangeSeq) {
      final RangeSeq seq = (RangeSeq) at;
      final long[] range = seq.range(false);
      Expr expr = cc.function(_UTIL_RANGE, info, input, Int.get(range[0]), Int.get(range[1]));
      if(!seq.asc) expr = cc.function(REVERSE, info, expr);
      return expr;
    }
    // items-at(E, S to E)  ->  util:range(E, S, E)
    if(at instanceof Range) {
      final Expr arg1 = at.arg(0), arg2 = at.arg(1);
      if(arg1.seqType().instanceOf(SeqType.INTEGER_O) &&
         arg2.seqType().instanceOf(SeqType.INTEGER_O)) {
        return cc.function(_UTIL_RANGE, info, input, at.arg(0), at.arg(1));
      }
    }
    // items-at(E, reverse(P))  ->  reverse(E, P))
    if(REVERSE.is(at))
      return cc.function(REVERSE, info, cc.function(ITEMS_AT, info, input, at.arg(0)));

    exprType.assign(ist.with(occ)).data(input);

    // ignore standard limitation for large values to speed up evaluation of result
    return allAreValues(false) ? value(cc.qc) : embed(cc, false);
  }

  /**
   * Returns the difference to the input length in an argument that counts the length
   * of an input expression.
   * @param input input expression
   * @param end end expression (can be {@code Empty#UNDEFINED})
   * @return length, or {@code Long#MIN_VALUE} if the value cannot be statically retrieved.
   */
  static long countInputDiff(final Expr input, final Expr end) {
    if(end != Empty.UNDEFINED) {
      final Predicate<Expr> countInput = e ->
        Function.COUNT.is(e) && e.arg(0).equals(input) && !e.has(Flag.NDT);
      // function(E, count(E))  ->  0
      if(countInput.test(end)) return 0;
      // function(E, count(E) - 1)  ->  -1
      if(end instanceof Arith && countInput.test(end.arg(0)) && end.arg(1) instanceof Int) {
        final Calc calc = ((Arith) end).calc;
        final long sum = ((Int) end.arg(1)).itr();
        if(calc == Calc.PLUS) return sum;
        if(calc == Calc.MINUS) return -sum;
      }
    }
    return Long.MIN_VALUE;
  }
}
