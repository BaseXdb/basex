package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnInScopePrefixes extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Atts atts = toElem(arg(0), qc).nsScopeXML(qc);

    final int as = atts.size();
    final TokenList tl = new TokenList();
    for(int a = 0; a < as; a++) {
      final byte[] key = atts.name(a);
      if(key.length + atts.value(a).length != 0) tl.add(key);
    }
    return StrSeq.get(tl);
  }
}
