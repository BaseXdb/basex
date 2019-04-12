package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
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
      public Value value(final QueryContext q) {
        return toValue(cps);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toValue(cps(toEmptyToken(exprs[0], qc)));
  }

  /**
   * Returns the specified codepoints as value.
   * @param cps codepoints
   * @return value
   */
  private static Value toValue(final int[] cps) {
    final LongList list = new LongList(cps.length);
    for(final int cp : cps) list.add(cp);
    return IntSeq.get(list.finish());
  }
}
