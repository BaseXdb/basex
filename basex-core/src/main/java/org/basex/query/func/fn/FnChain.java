package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnChain extends FnApply {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Value input = arg(0).value(qc);

    final Iter functions = arg(1).iter(qc);
    for(Item item; (item = functions.next()) != null;) {
      final FItem func = toFunction(item, qc);
      final ValueList args = new ValueList();
      if(func.arity() == 1) {
        args.add(input);
      } else if(input instanceof XQArray) {
        for(final Value arg : ((XQArray) input).members()) args.add(arg);
      } else {
        for(final Item it : input) args.add(it);
      }
      input = apply(func, args, qc);
    }
    return input;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return this;
  }
}
