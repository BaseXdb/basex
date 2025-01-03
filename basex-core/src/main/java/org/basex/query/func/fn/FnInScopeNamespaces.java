package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnInScopeNamespaces extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Atts atts = toElem(arg(0), qc).nsScope(sc()).add(Token.XML, XML_URI);

    final MapBuilder mb = new MapBuilder();
    final int as = atts.size();
    for(int a = 0; a < as; ++a) {
      final byte[] name = atts.name(a), value = atts.value(a);
      final int nl = name.length, vl = value.length;
      if(nl + vl != 0) {
        final Type nt = nl != 0 ? AtomType.NCNAME : new EnumType(new TokenSet(Token.EMPTY));
        mb.put(Str.get(name, nt), Uri.get(value));
      }
    }
    return mb.map();
  }
}
