package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnSum extends Aggr {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // partial sum calculation (Little Gauss)
    if(exprs[0] instanceof RangeSeq) {
      final RangeSeq rs = (RangeSeq) exprs[0];
      final long s = rs.start(), e = s + rs.size() - 1;
      // range is small enough to be computed with long values
      if(e < 3037000500L) return Int.get((s + e) * (e - s + 1) / 2);
      // compute larger ranges
      final BigInteger bs = BigInteger.valueOf(s), be = BigInteger.valueOf(e);
      final BigInteger bi = bs.add(be).multiply(be.subtract(bs).add(BigInteger.ONE)).
          divide(BigInteger.valueOf(2));
      final long l = bi.longValue();
      // check if result is small enough to be represented as long value
      if(bi.equals(BigInteger.valueOf(l))) return Int.get(l);
      throw RANGE_X.get(info, bi);
    }

    final Iter iter = exprs[0].atomIter(qc, info);
    final Item it = iter.next();
    if(it != null) return sum(iter, it, false, qc);

    // return default item
    return exprs.length == 2 ? exprs[1].atomItem(qc, info) : Int.ZERO;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr e1 = exprs[0], e2 = exprs.length == 2 ? exprs[1] : Empty.SEQ;
    final Type st1 = e1.seqType().type, st2 = e2 != Empty.SEQ ? e2.seqType().type : st1;
    if(st1.isNumberOrUntyped() && st2.isNumberOrUntyped()) seqType = Calc.type(st1, st2).seqType();

    // pre-evaluate 0 results (skip non-deterministic and variable expressions)
    Expr expr = this;
    if(e1.size() == 0 && !e1.has(Flag.NDT) && !e1.has(Flag.UPD) && !(e1 instanceof VarRef)) {
      if(e2 == Empty.SEQ) expr = Int.ZERO;
      else if(e2 instanceof ANum || e2 instanceof Dur) expr = e2;
    }
    return expr;
  }
}
