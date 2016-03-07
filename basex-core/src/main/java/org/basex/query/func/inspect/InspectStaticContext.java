package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class InspectStaticContext extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item it = exprs[0].item(qc, ii);
    final String name = Token.string(toToken(exprs[1], qc));
    final StaticContext sctx;
    if(it == null) {
      sctx = sc;
    } else {
      it = toFunc(it, qc);
      if(!(it instanceof FuncItem)) throw INVFUNCITEM_X_X.get(info, it.type, it);
      sctx = ((FuncItem) it).sc;
    }

    switch(name) {
      case "base-uri":
        return sctx.baseURI();
      case "namespaces":
        Map map = Map.EMPTY;
        Atts nsp = sctx.ns.ns;
        int ns = nsp.size();
        for(int n = 0; n < ns; n++) {
          map = map.put(Str.get(nsp.name(n)), Str.get(nsp.value(n)), ii);
        }
        nsp = NSGlobal.NS;
        ns = nsp.size();
        for(int n = 0; n < ns; n++) {
          final Str key = Str.get(nsp.name(n));
          if(!map.contains(key, ii)) map = map.put(key, Str.get(nsp.value(n)), ii);
        }
        return map;
      case "element-namespace":
        return sctx.elemNS == null ? null : Uri.uri(sctx.elemNS);
      case "function-namespace":
        return sctx.funcNS == null ? null : Uri.uri(sctx.funcNS);
      case "collation":
        return Uri.uri(sctx.collation == null ? QueryText.COLLATION_URI : sctx.collation.uri());

      default:
        throw INSPECT_UNKNOWN_X.get(info, name);
    }
  }
}
