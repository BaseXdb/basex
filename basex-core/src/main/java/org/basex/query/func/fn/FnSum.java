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
      final long s = rs.itemAt(0).itr(ii);
      if(s == 0 || s == 1) {
        final long n = rs.size();
        return Int.get(n < 3037000500L ? n * (n + 1) / 2 : BigInteger.valueOf(n).multiply(
            BigInteger.valueOf(n + 1)).divide(BigInteger.valueOf(2)).longValue());
      }
    }

    final Iter iter = exprs[0].atomIter(qc, ii);
    Item it = iter.next();
    return it != null ? sum(iter, it, false) :
      exprs.length == 2 ? exprs[1].atomItem(qc, ii) : Int.get(0);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    // skip non-deterministic and variable expressions
    final Expr e = exprs[0];
    if(e.has(Flag.NDT) || e.has(Flag.UPD) || e instanceof VarRef) return this;

    final long c = e.size();
    if(c == 0) return exprs.length == 2 ? exprs[1] : Int.get(0);
    final Type a = e.seqType().type, b = exprs.length == 2 ? exprs[1].seqType().type : a;
    if(a.isNumberOrUntyped() && b.isNumberOrUntyped()) seqType = Calc.type(a, b).seqType();
    return this;
  }
}
