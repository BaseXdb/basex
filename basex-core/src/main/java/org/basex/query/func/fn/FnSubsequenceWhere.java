package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnSubsequenceWhere extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem from = toFunctionOrNull(arg(1), 2, qc);
    final FItem to = toFunctionOrNull(arg(2), 2, qc);

    // create standard iterator
    return new Iter() {
      boolean more = true;
      boolean found;
      int c;

      @Override
      public Item next() throws QueryException {
        for(Item item; more && (item = qc.next(input)) != null;) {
          final Int pos = Int.get(++c);
          if(!found && (from == null || toBoolean(qc, from, item, pos))) found = true;
          if(found) {
            if(to != null && toBoolean(qc, to, item, pos)) more = false;
            return item;
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
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final boolean from = defined(1), to = defined(2);
    final SeqType ist = input.seqType();
    if(!(from || to) || ist.zero()) return input;

    final SeqType[] types = { ist.with(Occ.EXACTLY_ONE), SeqType.INTEGER_O };
    if(from) arg(1, arg -> refineFunc(arg, cc, SeqType.BOOLEAN_O, types));
    if(to) arg(2, arg -> refineFunc(arg, cc, SeqType.BOOLEAN_O, types));

    exprType.assign(ist);
    return this;
  }
}
