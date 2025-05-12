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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectStaticContext extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item func = arg(0).item(qc, info);
    final String name = toString(arg(1), qc);
    final StaticContext sctx;
    if(func.isEmpty()) {
      sctx = sc();
    } else if(func instanceof final FuncItem fi) {
      sctx = fi.info().sc();
    } else {
      throw INVFUNCITEM_X_X.get(info, func.type, func);
    }

    return switch(name) {
      case BASE_URI ->
        sctx.baseURI();
      case NAMESPACES -> {
        final MapBuilder mb = new MapBuilder();
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
        yield mb.map();
      }
      case ELEMENT_NAMESPACE ->
        sctx.elemNS == null ? Empty.VALUE : Uri.get(sctx.elemNS);
      case FUNCTION_NAMESPACE ->
        sctx.funcNS == null ? Empty.VALUE : Uri.get(sctx.funcNS);
      case COLLATION ->
        Uri.get(sctx.collation == null ? COLLATION_URI : sctx.collation.uri());
      case ORDERING ->
        Str.get(sctx.ordered ? ORDERED : UNORDERED);
      case CONSTRUCTION ->
        Str.get(sctx.strip ? STRIP : PRESERVE);
      case DEFAULT_ORDER_EMPTY ->
        Str.get(sctx.orderGreatest ? GREATEST : LEAST);
      case BOUNDARY_SPACE ->
        Str.get(sctx.spaces ? PRESERVE : STRIP);
      case COPY_NAMESPACES -> {
        final TokenList tl = new TokenList(2);
        tl.add(sctx.preserveNS ? PRESERVE : NO_PRESERVE);
        tl.add(sctx.inheritNS ? INHERIT : NO_INHERIT);
        yield StrSeq.get(tl);
      }
      case DECIMAL_FORMATS -> {
        // enforce creation of default formatter
        sctx.decFormat(QNm.EMPTY, info);
        // loop through all formatters
        final MapBuilder mb = new MapBuilder();
        for(final byte[] format : sctx.decFormats) {
          mb.put(Str.get(format), sctx.decFormats.get(format).toMap());
        }
        yield mb.map();
      }
      default ->
        throw INSPECT_UNKNOWN_X.get(info, name);
    };
  }
}
