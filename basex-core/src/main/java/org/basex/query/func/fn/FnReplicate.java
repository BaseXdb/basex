package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class FnReplicate extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Expr input = exprs[0];
    final long count = toLong(exprs[1], qc);
    if(count <= 0) return Empty.VALUE;
    if(count == 1) return input.value(qc);

    // check if expression must be evaluated only once
    final boolean once = input instanceof Value || exprs.length < 3 || !toBoolean(exprs[2], qc);
    if(once) return SingletonSeq.get(input.value(qc), count);

    // repeated evaluations
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long c = 0; c < count; c++) vb.add(input.value(qc));
    return vb.value(this);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Expr input = exprs[0];
    final long count = toLong(exprs[1], qc);
    if(count <= 0) return Empty.ITER;
    if(count == 1) return input.iter(qc);

    // check if expression must be evaluated only once
    final boolean once = exprs.length < 3 || !toBoolean(exprs[2], qc);
    if(once) return SingletonSeq.get(input.value(qc), count).iter();

    // repeated evaluations
    if(input.seqType().one()) {
      // replication of single item
      return new Iter() {
        long c = count;

        @Override
        public Item next() throws QueryException {
          return --c >= 0 ? input.item(qc, info) : null;
        }
        @Override
        public Item get(final long i) throws QueryException {
          return input.item(qc, info);
        }
        @Override
        public long size() {
          return count;
        }
      };
    }

    // standard evaluation
    return new Iter() {
      long c = count;
      Iter iter;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(iter == null) {
            if(--c < 0) return null;
            iter = input.iter(qc);
          }
          final Item item = iter.next();
          if(item != null) return item;
          iter = null;
        }
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0], count = exprs[1];
    final boolean single = singleEval(true);

    // merge replicate functions
    if(REPLICATE.is(input) && single == ((FnReplicate) input).singleEval(true)) {
      final ExprList args = new ExprList(2).add(input.arg(0));
      args.add(new Arith(info, count, input.arg(1), Calc.MULT).optimize(cc));
      if(!single) args.add(Bln.TRUE);
      return cc.function(REPLICATE, info, args.finish());
    }

    // pre-evaluate static multipliers
    long sz = -1, c = -1;
    if(count instanceof Value) {
      c = toLong(count, cc.qc);
      // replicate(<a/>, 0)  ->  ()
      if(c <= 0) return Empty.VALUE;
      // replicate(<a/>, 1)  ->  <a/>
      if(c == 1) return input;
      sz = input.size();
      if(sz != -1) sz *= c;
    }
    // replicate(prof:void(<a/>), 2)  ->  prof:void(<a/>)
    if(input == Empty.VALUE || sz == 0 && single) return input;

    // adopt sequence type
    final Occ occ = c > 0 ? Occ.ONE_OR_MORE : Occ.ZERO_OR_MORE;
    exprType.assign(input.seqType().union(occ), sz).data(input);
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // data(replicate(<a>1</a>, 2))  ->  data(replicate(xs:untypedAtomic('1'), 2))
    // distinct-values(replicate(('a', 'a'), 2))  ->  distinct-values('a', 2)
    final Expr input = mode.oneOf(Simplify.STRING, Simplify.NUMBER, Simplify.DATA, Simplify.COUNT,
        Simplify.DISTINCT) ? exprs[0].simplifyFor(mode, cc) : exprs[0];

    final long count = exprs[1] instanceof Int ? ((Int) exprs[1]).itr() : -1;
    if(count > 0 && (singleEval(true) || !input.has(Flag.NDT))) {
      // distinct-values(replicate(NODES, 2))  ->  distinct-values(NODES)
      // VALUE[replicate(NODES, 2)]  ->  VALUE[NODES]
      if(mode == Simplify.DISTINCT ||
          mode == Simplify.PREDICATE && input.seqType().instanceOf(SeqType.NODE_ZM)) {
        return cc.simplify(this, input, mode);
      }
    }

    // create function with new input argument
    if(input != exprs[0]) {
      final Expr[] args = exprs.clone();
      args[0] = input;
      return cc.function(REPLICATE, info, args);
    }
    return this;
  }

  /**
   * Indicates if the input argument will be evaluated once or (if requested) zero times.
   * @param zero allow zero evaluations
   * @return result of check
   */
  public boolean singleEval(final boolean zero) {
    /* accepted:
     * - replicate(<a/>, 2)
     * - replicate(1 to 10, <_>5</_>)
     * - replicate(reverse(1 to 10), 5, false())
     * - replicate(1, 2, true())
     * rejected:
     * - replicate(random:uuid(), 2, true()) */
    return exprs[0] instanceof Value ||
      // if second argument is a static integer, it is >= 1
      (exprs.length < 3 || exprs[2] == Bln.FALSE) && (zero || exprs[1] instanceof Int);
  }
}
