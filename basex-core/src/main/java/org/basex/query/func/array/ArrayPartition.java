package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ArrayPartition extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = exprs[0].iter(qc);
      final FItem breakWhen = toFunction(exprs[1], 2, qc);
      Value value = Empty.VALUE;

      @Override
      public Item next() throws QueryException {
        while(value != null) {
          qc.checkStop();
          final Item item = input.next();
          if(item == null || toBoolean(breakWhen.invoke(qc, info, value, item), qc)) {
            final Item member = member(value);
            value = item;
            if(member != Empty.VALUE) return member;
          } else {
            value = ValueBuilder.concat(value, item, qc);
          }
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  /**
   * Returns a new array member or a empty sequence.
   * @param value value to be wrapped
   * @return new entry
   */
  private Item member(final Value value) {
    return value != Empty.VALUE ? XQArray.member(value) : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st =  input.seqType();
    if(st.zero()) return input;

    exprs[1] = coerceFunc(exprs[1], cc, SeqType.BOOLEAN_O,
        st.with(Occ.ZERO_OR_MORE), st.with(Occ.EXACTLY_ONE));
    exprType.assign(ArrayType.get(st.union(Occ.ONE_OR_MORE)));
    return this;
  }
}
