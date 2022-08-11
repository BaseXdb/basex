package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnInScopeNamespaces extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Atts atts = toElem(exprs[0], qc).nsScope(sc).add(XML, XML_URI);

    final MapBuilder builder = new MapBuilder(info);
    final int as = atts.size();
    for(int a = 0; a < as; ++a) {
      final byte[] key = atts.name(a);
      if(key.length + atts.value(a).length != 0) {
        builder.put(Str.get(key), Uri.get(atts.value(a)));
      }
    }
    return builder.finish();
  }
}
