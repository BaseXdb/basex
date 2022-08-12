package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.format.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class InspectStaticContext extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Item func = exprs[0].item(qc, info);
    final String name = toString(exprs[1], qc);
    final StaticContext sctx;
    if(func == Empty.VALUE) {
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
        MapBuilder mb = new MapBuilder(info);
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
        mb = new MapBuilder(info);
        // enforce creation of default formatter
        sctx.decFormat(EMPTY);
        // loop through all formatters
        for(final byte[] format : sctx.decFormats) {
          final DecFormatter df = sctx.decFormats.get(format);
          mb.put(Str.get(format), new MapBuilder(info).
            put(DF_DEC, cpToken(df.decimal)).
            put(DF_EXP, cpToken(df.exponent)).
            put(DF_GRP, cpToken(df.grouping)).
            put(DF_PC, cpToken(df.percent)).
            put(DF_PM, cpToken(df.permille)).
            put(DF_ZD, cpToken(df.zero)).
            put(DF_DIG, cpToken(df.optional)).
            put(DF_PAT, cpToken(df.pattern)).
            put(DF_INF, df.inf).
            put(DF_NAN, df.nan).
            put(DF_MIN, cpToken(df.minus)).map());
        }
        return mb.map();
      default:
        throw INSPECT_UNKNOWN_X.get(info, name);
    }
  }
}
