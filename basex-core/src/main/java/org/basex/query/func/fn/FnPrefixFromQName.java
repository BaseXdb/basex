package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnPrefixFromQName extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QNm nm = toQNm(exprs[0], qc, true);
    return nm == null || !nm.hasPrefix() ? null :
      AtomType.NCN.cast(Str.get(nm.prefix()), qc, sc, info);
  }
}
