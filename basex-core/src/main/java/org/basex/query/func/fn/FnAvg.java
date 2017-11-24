package org.basex.query.func.fn;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class FnAvg extends FnSum {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr ex = exprs[0];
    if(ex instanceof RangeSeq || ex instanceof Range) return range(ex.value(qc));

    if(ex instanceof SingletonSeq) {
      final Item it = singleton((SingletonSeq) ex);
      if(it != null) return it;
    }
    final Iter iter = ex.atomIter(qc, info);
    final Item it = iter.next();
    return it == null ? null : sum(iter, it, true, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex = exprs[0];
    if(ex instanceof RangeSeq) return range((Value) ex);
    if(ex instanceof SingletonSeq) {
      final Item it = singleton((SingletonSeq) ex);
      if(it != null) return it;
    }

    // empty sequence: replace with default item
    final SeqType st = ex.seqType();
    if(st.zero()) return ex;

    if(!st.mayBeArray()) {
      // sequence is not empty: assign result type
      Type t = st.type;
      if(t.isUntyped()) t = AtomType.DBL;
      else if(t.instanceOf(AtomType.ITR)) t = AtomType.DEC;
      else if(!t.isNumber()) t = AtomType.AAT;
      exprType.assign(t, st.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE);
    }
    return optFirst();
  }

  /**
   * Compute result from singleton value.
   * @param seq singleton sequence
   * @return result, or {@code null} if value cannot be evaluated
   * @throws QueryException query exception
   */
  private Item singleton(final SingletonSeq seq) throws QueryException {
    Item it = seq.itemAt(0);
    if(it.type.isUntyped()) it = Dbl.get(it.dbl(info));
    return it.type.isNumber() ? it : null;
  }

  /**
   * Compute result from range value.
   * @param value sequence
   * @return result, or {@code null} if sequence is empty
   * @throws QueryException query exception
   */
  private Item range(final Value value) throws QueryException {
    if(value.isEmpty()) return null;
    final long min = value.itemAt(0).itr(info), max = value.itemAt(value.size() - 1).itr(info);
    final BigDecimal sum = BigDecimal.valueOf(min).add(BigDecimal.valueOf(max));
    return Dec.get(sum.divide(BigDecimal.valueOf(2)));
  }
}
