package org.basex.query.func.util;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilChars extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final int[] cps = cps(toZeroToken(exprs[0], qc));

    return new BasicIter<Str>(cps.length) {
      @Override
      public Str get(final long i) {
        return Str.get(cpToken(cps[(int) i]));
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) {
        return toValue(cps);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toValue(cps(toZeroToken(exprs[0], qc)));
  }

  /**
   * Returns the specified characters as sequence of strings.
   * @param cps codepoints of characters
   * @return value
   */
  private static Value toValue(final int[] cps) {
    final TokenList list = new TokenList(cps.length);
    for(final int cp : cps) list.add(cpToken(cp));
    return StrSeq.get(list.finish());
  }
}
