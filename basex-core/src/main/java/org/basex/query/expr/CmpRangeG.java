package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General range-based comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpRangeG extends CmpG {
  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 range expression
   * @param op operator
   * @param info input info (can be {@code null})
   */
  CmpRangeG(final Expr expr1, final Expr expr2, final CmpOp op, final InputInfo info) {
    super(info, expr1, expr2, op);
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final RangeSeq rs = (RangeSeq) exprs[1].value(qc);
    final long min = rs.min(), max = rs.max();
    final Iter iter = exprs[0].atomIter(qc, info);
    for(Item item; (item = iter.next()) != null;) {
      final double value = item.dbl(info);
      if(value >= min && value <= max && value == (long) value) return true;
    }
    return false;
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CmpRangeG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, info));
  }

  @Override
  public String description() {
    return "range " + super.description();
  }
}
