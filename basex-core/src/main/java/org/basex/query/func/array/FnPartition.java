package org.basex.query.func.array;

import static org.basex.query.func.Function.*;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnPartition extends ArrayFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = arg(0).iter(qc);
      final FItem breakWhen = toFunction(arg(1), 2, qc);
      Value value = Empty.VALUE;

      @Override
      public Item next() throws QueryException {
        while(value != null) {
          final Item item = input.next();
          if(item == null || toBoolean(breakWhen.invoke(qc, info, value, item), qc)) {
            final Value v = value;
            value = item;
            if(!v.isEmpty()) return XQArray.member(v);
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

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st =  input.seqType();
    if(st.zero()) return input;
    if(st.one() && arg(1) instanceof FuncItem)
      return cc.function(_UTIL_ARRAY_MEMBER, info, input);

    arg(1, arg -> coerceFunc(arg, cc, SeqType.BOOLEAN_O, st.with(Occ.ZERO_OR_MORE),
        st.with(Occ.EXACTLY_ONE)));
    exprType.assign(ArrayType.get(st.union(Occ.ONE_OR_MORE)));
    return this;
  }
}
