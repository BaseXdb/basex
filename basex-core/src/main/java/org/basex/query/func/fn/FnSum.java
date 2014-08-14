package org.basex.query.func.fn;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnSum extends Aggr {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // partial sum calculation (Little Gauss)
    if(exprs[0] instanceof RangeSeq) {
      final RangeSeq rs = (RangeSeq) exprs[0];
      final long s = rs.start();
      if(s == 0 || s == 1) {
        final long n = rs.size();
        return Int.get(n < 3037000500L ? n * (n + 1) / 2 : BigInteger.valueOf(n).multiply(
            BigInteger.valueOf(n + 1)).divide(BigInteger.valueOf(2)).longValue());
      }
    }

    final Iter iter = exprs[0].atomIter(qc, ii);
    final Item it = iter.next();
    return it != null ? sum(iter, it, false) :
      exprs.length == 2 ? exprs[1].atomItem(qc, ii) : Int.get(0);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    final Expr e1 = exprs[0], e2 = exprs.length == 2 ? exprs[1] : null;
    final Type st1 = e1.seqType().type, st2 = e2 != null ? e2.seqType().type : st1;
    if(st1.isNumberOrUntyped() && st2.isNumberOrUntyped()) seqType = Calc.type(st1, st2).seqType();

    // 0 results: skip non-deterministic and variable expressions
    final long c = e1.size();
    return c != 0 || e1.has(Flag.NDT) || e1.has(Flag.UPD) || e1 instanceof VarRef ? this :
      e2 != null ? e2 : Int.get(0);
  }
}
