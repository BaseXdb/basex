package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnScan extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(2), 3, qc);
    final HofArgs args = new HofArgs(3, action).set(0, arg(1).value(qc));

    return new Iter() {
      boolean first = true;

      @Override
      public Item next() throws QueryException {
        if(first) {
          first = false;
        } else {
          final Item item = input.next();
          if(item == null) return null;
          args.set(0, invoke(action, args.set(1, item).inc(), qc));
        }
        return XQArray.get(args.get(0));
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), init = arg(1), action = arg(2);
    // scan((), INIT, ACTION) → util:array-member(INIT)
    if(input.seqType().zero()) {
      return cc.voidAndReturn(input, cc.function(_UTIL_ARRAY_MEMBER, info, init), info);
    }

    SeqType st = init.seqType();
    if(action instanceof FuncItem || action instanceof Closure) {
      final SeqType itemType = input.seqType().with(Occ.EXACTLY_ONE);
      SeqType ost;
      do {
        final SeqType[] types = { st, itemType, Types.INTEGER_O };
        arg(2, arg -> refineFunc(action, cc, types));
        ost = st;
        st = st.union(arg(2).funcType().refinedType);
      } while(!st.eq(ost));
    } else {
      final FuncType ft = action.funcType();
      if(ft != null) st = st.union(ft.refinedType);
    }
    exprType.assign(ArrayType.get(st).seqType(Occ.ONE_OR_MORE));
    return this;
  }
}
