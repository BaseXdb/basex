package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnContainsSequence extends StandardFunc {
  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value input = arg(0).value(qc);
    final Value subsequence = arg(1).value(qc);
    final QueryBiFunction<Item, Item, Boolean> cmp;
    if(defined(2)) {
      final FItem compare = toFunction(arg(2), 2, qc);
      cmp = (item1, item2) -> toBoolean(eval(compare, qc, item1, item2).item(qc, info));
    } else {
      cmp = new DeepEqual(info, sc.collation, qc)::equal;
    }
    return Bln.get(compare(input, subsequence, cmp));
  }

  /**
   * Compares two sequences.
   * @param input input sequence
   * @param subsequence subsequence
   * @param cmp comparison function
   * @return result of comparison
   * @throws QueryException query exception
   */
  boolean compare(final Value input, final Value subsequence,
      final QueryBiFunction<Item, Item, Boolean> cmp) throws QueryException {

    final long is = input.size(), ss = subsequence.size(), ps = is - ss;
    if(is >= ss) {
      for(long p = 0; p <= ps; p++) {
        for(long s = 0; s <= ss; s++) {
          if(s == ss) return true;
          if(!cmp.apply(input.itemAt(p + s), subsequence.itemAt(s))) break;
        }
      }
    }
    return false;
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), subsequence = arg(1);
    final SeqType ist = input.seqType(), sst = subsequence.seqType();
    if(sst.zero()) return Bln.TRUE;

    if(defined(2)) {
      arg(2, arg -> coerceFunc(arg, cc, SeqType.BOOLEAN_O, ist.with(Occ.EXACTLY_ONE),
          sst.with(Occ.EXACTLY_ONE)));
    }
    return this;
  }
}
