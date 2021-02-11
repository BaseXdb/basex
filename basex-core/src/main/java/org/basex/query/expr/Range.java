package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Range expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Range extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   */
  public Range(final InputInfo info, final Expr expr1, final Expr expr2) {
    super(info, SeqType.INTEGER_ZM, expr1, expr2);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    simplifyAll(Simplify.NUMBER, cc);

    Expr expr = emptyExpr();
    if(expr == this) {
      if(allAreValues(false)) return cc.preEval(this);

      final Expr expr1 = exprs[0], expr2 = exprs[1];
      if(expr1.equals(expr2)) {
        if(expr1.seqType().instanceOf(SeqType.INTEGER_O)) {
          expr = expr1;
        } else {
          exprType.assign(Occ.EXACTLY_ONE);
        }
      }
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return Empty.VALUE;
    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2 == Empty.VALUE) return Empty.VALUE;
    final long min = toLong(item1), max = toLong(item2);
    // min smaller than max: empty sequence
    if(min > max) return Empty.VALUE;
    // max smaller than min: create range
    final long size = max - min + 1;
    if(size > 0) return RangeSeq.get(min, size, true);
    // overflow of long value
    throw RANGE_X.get(info, max);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Range(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Range && super.equals(obj);
  }

  @Override
  public String description() {
    return "range expression";
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, ' ' + TO + ' ', true);
  }
}
