package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnInScopePrefixes extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Atts ns = toElem(exprs[0], qc).nsScope().add(XML, XML_URI);
    final int as = ns.size();
    final ValueBuilder vb = new ValueBuilder(as);
    for(int a = 0; a < as; ++a) {
      final byte[] key = ns.name(a);
      if(key.length + ns.value(a).length != 0) vb.add(Str.get(key));
    }
    return vb;
  }
}
