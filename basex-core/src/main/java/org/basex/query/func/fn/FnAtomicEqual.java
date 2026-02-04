package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAtomicEqual extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value1 = toAtomItem(arg(0), qc);
    final Item value2 = toAtomItem(arg(1), qc);
    return Bln.get(value1.atomicEqual(value2));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value1 = arg(0), value2 = arg(1);
    final SeqType st1 = value1.seqType(), st2 = value2.seqType();
    Type tp1 = st1.type.atomic(), tp2 = st2.type.atomic();

    // convert function call to general comparison (covers the most common cases)
    // atomic-equal(2, 3) → 2 = 3
    if(sc().collation == null && st1.one() && st2.one() && !st1.mayBeFunction() &&
        !st2.mayBeFunction() && tp1 != null && tp2 != null) {
      if(tp1.oneOf(BasicType.UNTYPED_ATOMIC, BasicType.ANY_URI)) tp1 = BasicType.STRING;
      if(tp2.oneOf(BasicType.UNTYPED_ATOMIC, BasicType.ANY_URI)) tp2 = BasicType.STRING;
      if(tp1 == tp2 && (tp1.oneOf(BasicType.QNAME, BasicType.BOOLEAN) ||
          tp1.instanceOf(BasicType.DECIMAL) || tp1.instanceOf(BasicType.STRING))) {
        return new CmpG(info, value1, value2, CmpOp.EQ).optimize(cc);
      }
    }
    return this;
  }
}
