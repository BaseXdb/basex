package org.basex.query.func.array;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
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
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = exprs[0].value(qc);
    final FItem breakWhen = toFunction(exprs[1], 2, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    final Consumer<Value> add = v -> {
      if(v != Empty.VALUE) vb.add(XQArray.member(v));
    };
    Value value = Empty.VALUE;
    for(final Item item : input) {
      if(toBoolean(breakWhen.invoke(qc, info, value, item), qc)) {
        add.accept(value);
        value = item;
      } else {
        value = ValueBuilder.concat(value, item, qc);
      }
    }
    add.accept(value);
    return vb.value(this);
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
