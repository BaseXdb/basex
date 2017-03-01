package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnStringToCodepoints extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final int[] cps = cps(toEmptyToken(exprs[0], qc));
    return new BasicIter<Int>(cps.length) {
      @Override
      public Int get(final long i) {
        return Int.get(cps[(int) i]);
      }
      @Override
      public Value value() {
        return FnStringToCodepoints.value(cps);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(cps(toEmptyToken(exprs[0], qc)));
  }

  /**
   * Returns the specified codepoints as value.
   * @param cps codepoints
   * @return value
   */
  private static Value value(final int[] cps) {
    final int tl = cps.length;
    final long[] vals = new long[tl];
    for(int t = 0; t < tl; t++) vals[t] = cps[t];
    return IntSeq.get(vals, AtomType.ITR);
  }
}
