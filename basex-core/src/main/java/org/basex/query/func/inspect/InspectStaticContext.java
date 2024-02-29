package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class InspectStaticContext extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Item func = arg(0).item(qc, info);
    final String name = toString(arg(1), qc);
    final StaticContext sctx;
    if(func.isEmpty()) {
      sctx = sc;
    } else {
      func = toFunction(func, qc);
      if(!(func instanceof FuncItem)) throw INVFUNCITEM_X_X.get(info, func.type, func);
      sctx = ((FuncItem) func).sc;
    }

    switch(name) {
      case BASE_URI:
        return sctx.baseURI();
      case NAMESPACES:
        MapBuilder mb = new MapBuilder();
        Atts nsp = sctx.ns.list;
        int ns = nsp.size();
        for(int n = 0; n < ns; n++) {
          mb.put(Str.get(nsp.name(n)), Str.get(nsp.value(n)));
        }
        nsp = NSGlobal.NS;
        ns = nsp.size();
        for(int n = 0; n < ns; n++) {
          final Str key = Str.get(nsp.name(n));
          if(!mb.contains(key)) mb.put(key, Str.get(nsp.value(n)));
        }
        return mb.map();
      case ELEMENT_NAMESPACE:
        return sctx.elemNS == null ? Empty.VALUE : Uri.get(sctx.elemNS);
      case FUNCTION_NAMESPACE:
        return sctx.funcNS == null ? Empty.VALUE : Uri.get(sctx.funcNS);
      case COLLATION:
        return Uri.get(sctx.collation == null ? COLLATION_URI : sctx.collation.uri());
      case ORDERING:
        return Str.get(sctx.ordered ? ORDERED : UNORDERED);
      case CONSTRUCTION:
        return Str.get(sctx.strip ? STRIP : PRESERVE);
      case DEFAULT_ORDER_EMPTY:
        return Str.get(sctx.orderGreatest ? GREATEST : LEAST);
      case BOUNDARY_SPACE:
        return Str.get(sctx.spaces ? PRESERVE : STRIP);
      case COPY_NAMESPACES:
        final TokenList tl = new TokenList(2);
        tl.add(sctx.preserveNS ? PRESERVE : NO_PRESERVE);
        tl.add(sctx.inheritNS ? INHERIT : NO_INHERIT);
        return StrSeq.get(tl);
      case DECIMAL_FORMATS:
        // enforce creation of default formatter
        sctx.decFormat(QNm.EMPTY);
        // loop through all formatters
        mb = new MapBuilder();
        for(final byte[] format : sctx.decFormats) {
          mb.put(Str.get(format), sctx.decFormats.get(format).toMap());
        }
        return mb.map();
      default:
        throw INSPECT_UNKNOWN_X.get(info, name);
    }
  }
}
