package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnPartition extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem splitWhen = toFunction(arg(1), 3, qc);
    return new Iter() {
      Value value = Empty.VALUE;
      final HofArgs args = new HofArgs(3, splitWhen);

      @Override
      public Item next() throws QueryException {
        while(value != null) {
          final Item item = input.next();
          if(item == null || test(splitWhen, args.set(0, value).set(1, item).inc(), qc)) {
            final Value val = value;
            value = item;
            if(!val.isEmpty()) {
              final ArrayBuilder ab = new ArrayBuilder(qc, val.size());
              for(final Item it : val) ab.add(it);
              return ab.array();
            }
          } else {
            value = value.append(item, qc);
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
    final SeqType st = input.seqType();
    if(st.zero()) return input;
    if(st.one() && arg(1) instanceof FuncItem) {
      return cc.function(_UTIL_ARRAY_MEMBER, info, input);
    }

    final SeqType mt = st.with(Occ.EXACTLY_ONE);
    arg(1, arg -> refineFunc(arg, cc, st.with(Occ.ZERO_OR_MORE), mt, Types.INTEGER_O));
    exprType.assign(ArrayType.get(mt));
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
