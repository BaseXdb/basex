package org.basex.query.func.util;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilReplicate extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Expr expr = exprs[0];
    final long count = toLong(exprs[1], qc);
    final boolean single = exprs.length < 3 || !toBoolean(exprs[2], qc);

    if(count <= 0) return Empty.VALUE;
    if(count == 1) return expr.value(qc);
    if(single) return SingletonSeq.get(expr.value(qc), count);

    // repeated evaluations
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long c = 0; c < count; c++) vb.add(expr.value(qc));
    return vb.value(this);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Expr expr = exprs[0];
    final long count = toLong(exprs[1], qc);
    final boolean single = exprs.length < 3 || !toBoolean(exprs[2], qc);

    if(count <= 0) return Empty.ITER;
    if(count == 1) return expr.iter(qc);
    if(single) return SingletonSeq.get(expr.value(qc), count).iter();

    // repeated evaluations
    return new Iter() {
      long c = count;
      Iter iter;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(iter == null) {
            if(c-- == 0) return null;
            iter = expr.iter(qc);
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
    final Expr expr = exprs[0], count = exprs[1];
    final boolean single = singleEval();

    // merge replicate functions
    if(_UTIL_REPLICATE.is(expr) && single == ((UtilReplicate) expr).singleEval()) {
      final ExprList args = new ExprList(2).add(expr.arg(0));
      args.add(new Arith(info, count, expr.arg(1), Calc.MULT).optimize(cc));
      if(!single) args.add(Bln.TRUE);
      return cc.function(_UTIL_REPLICATE, info, args.finish());
    }

    // pre-evaluate static multipliers
    long sz = -1, c = -1;
    if(count instanceof Value) {
      c = toLong(count, cc.qc);
      // util:replicate(<a/>, 0)  ->  ()
      if(c <= 0) return Empty.VALUE;
      // util:replicate(<a/>, 1)  ->  <a/>
      if(c == 1) return expr;
      sz = expr.size();
      if(sz != -1) sz *= c;
    }
    // util:replicate(prof:void(<a/>), 2)  ->  prof:void(<a/>)
    if(expr == Empty.VALUE || sz == 0 && single) return expr;

    // adopt sequence type
    exprType.assign(expr.seqType().union(c > 0 ? Occ.ONE_OR_MORE : Occ.ZERO_OR_MORE), sz);
    data(expr.data());
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];

    if(mode == Simplify.DISTINCT) {
      // ensure that input argument will be evaluated exactly once
      // util:replicate($node, 2)  ->  $node
      final long count = exprs[1] instanceof Int ? ((Int) exprs[1]).itr() : -1;
      final boolean single = singleEval();
      if(count > 0 && (single || !expr.has(Flag.NDT))) {
        return cc.replaceWith(this, expr);
      }
    } else if(mode == Simplify.STRING || mode == Simplify.NUMBER) {
      exprs[0] = expr.simplifyFor(mode, cc);
    }
    return super.simplifyFor(mode, cc);
  }

  /**
   * Indicates if the input argument will be evaluated at most once.
   * @return result of check
   */
  public boolean singleEval() {
    return exprs.length < 3 || exprs[2] == Bln.FALSE;
  }

  /**
   * Indicates if the input argument will be evaluated exactly once.
   * @return result of check, {@code false} if unknown at compile time
   */
  public boolean once() {
    // static integer will always be greater than 1
    return singleEval() && exprs[1] instanceof Int;
  }
}
