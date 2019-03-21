package org.basex.query.func.util;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class UtilChars extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final int[] cps = cps(toEmptyToken(exprs[0], qc));

    return new BasicIter<Str>(cps.length) {
      final TokenBuilder tb = new TokenBuilder(4);

      @Override
      public Str get(final long i) {
        return Str.get(tb.reset().add(cps[(int) i]).toArray());
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
    final TokenList list = new TokenList(cps.length);
    final TokenBuilder tb = new TokenBuilder(4);
    for(int cp : cps) list.add(tb.reset().add(cp).toArray());
    return StrSeq.get(list.finish());
  }
}
