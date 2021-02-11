package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnInScopePrefixes extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Atts atts = toElem(exprs[0], qc).nsScope(sc).add(XML, XML_URI);
    final int as = atts.size();
    final TokenList tl = new TokenList();
    for(int a = 0; a < as; ++a) {
      final byte[] key = atts.name(a);
      if(key.length + atts.value(a).length != 0) tl.add(key);
    }
    return StrSeq.get(tl);
  }
}
