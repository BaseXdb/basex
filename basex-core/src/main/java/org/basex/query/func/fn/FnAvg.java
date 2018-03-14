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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class FnAvg extends FnSum {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr expr = exprs[0];
    if(expr instanceof RangeSeq || expr instanceof Range) return range(expr.value(qc));

    if(expr instanceof SingletonSeq) {
      final Item item = singleton((SingletonSeq) expr);
      if(item != null) return item;
    }
    final Iter iter = expr.atomIter(qc, info);
    final Item item = iter.next();
    return item == null ? null : sum(iter, item, true, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    if(expr instanceof RangeSeq) return range((Value) expr);
    if(expr instanceof SingletonSeq) {
      final Item item = singleton((SingletonSeq) expr);
      if(item != null) return item;
    }

    // empty sequence: replace with default item
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    if(!st.mayBeArray()) {
      // sequence is not empty: assign result type
      Type type = st.type;
      if(type.isUntyped()) type = AtomType.DBL;
      else if(type.instanceOf(AtomType.ITR)) type = AtomType.DEC;
      else if(!type.isNumber()) type = AtomType.AAT;
      exprType.assign(type, st.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE);
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
    Item item = seq.itemAt(0);
    if(item.type.isUntyped()) item = Dbl.get(item.dbl(info));
    return item.type.isNumber() ? item : null;
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
    return Dec.get(sum.divide(BigDecimal.valueOf(2), MathContext.DECIMAL64));
  }
}
