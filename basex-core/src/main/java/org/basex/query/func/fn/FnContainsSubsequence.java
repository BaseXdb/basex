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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnContainsSubsequence extends StandardFunc {
  @Override
  public final Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public final boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Value input = arg(0).value(qc);
    final Value subsequence = arg(1).value(qc);
    final FItem compare = toFunctionOrNull(arg(2), 2, qc);

    final QueryBiFunction<Item, Item, Boolean> cmp;
    if(compare != null) {
      final HofArgs args = new HofArgs(2);
      cmp = (item1, item2) -> test(compare, args.set(0, item1).set(1, item2), qc);
    } else {
      cmp = new DeepEqual(info, null, qc)::equal;
    }
    return test(input, subsequence, cmp);
  }

  /**
   * Compares two sequences.
   * @param input input sequence
   * @param subsequence subsequence
   * @param cmp comparison function
   * @return result of comparison
   * @throws QueryException query exception
   */
  boolean test(final Value input, final Value subsequence,
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
      arg(2, arg -> refineFunc(arg, cc, ist.with(Occ.EXACTLY_ONE), sst.with(Occ.EXACTLY_ONE)));
    }
    return this;
  }

  @Override
  public final int hofIndex() {
    return 2;
  }
}
