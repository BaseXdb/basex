package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnInScopeNamespaces extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Atts atts = toElem(arg(0), qc).nsScope(sc()).add(XML, XML_URI);

    final MapBuilder mb = new MapBuilder();
    final int as = atts.size();
    for(int a = 0; a < as; ++a) {
      final byte[] key = atts.name(a);
      if(key.length + atts.value(a).length != 0) {
        mb.put(Str.get(key, AtomType.NCNAME), Uri.get(atts.value(a)));
      }
    }
    return mb.map();
  }
}
