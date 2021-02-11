package org.basex.query.func.inspect;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InspectStaticContext extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    Item func = exprs[0].item(qc, info);
    final String name = Token.string(toToken(exprs[1], qc));
    final StaticContext sctx;
    if(func == Empty.VALUE) {
      sctx = sc;
    } else {
      func = toFunc(func, qc);
      if(!(func instanceof FuncItem)) throw INVFUNCITEM_X_X.get(info, func.type, func);
      sctx = ((FuncItem) func).sc;
    }

    switch(name) {
      case BASE_URI:
        return sctx.baseURI();
      case NAMESPACES:
        XQMap map = XQMap.EMPTY;
        Atts nsp = sctx.ns.list;
        int ns = nsp.size();
        for(int n = 0; n < ns; n++) {
          map = map.put(Str.get(nsp.name(n)), Str.get(nsp.value(n)), info);
        }
        nsp = NSGlobal.NS;
        ns = nsp.size();
        for(int n = 0; n < ns; n++) {
          final Str key = Str.get(nsp.name(n));
          if(!map.contains(key, info)) map = map.put(key, Str.get(nsp.value(n)), info);
        }
        return map;
      case ELEMENT_NAMESPACE:
        return sctx.elemNS == null ? Empty.VALUE : Uri.uri(sctx.elemNS);
      case FUNCTION_NAMESPACE:
        return sctx.funcNS == null ? Empty.VALUE : Uri.uri(sctx.funcNS);
      case COLLATION:
        return Uri.uri(sctx.collation == null ? COLLATION_URI : sctx.collation.uri());
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
        map = XQMap.EMPTY;
        // enforce creation of default formatter
        sctx.decFormat(Token.EMPTY);
        // loop through all formatters
        for(final byte[] format : sctx.decFormats) {
          final DecFormatter df = sctx.decFormats.get(format);
          map = map.put(Str.get(format), XQMap.EMPTY.
              put(Str.get(DF_DEC), Str.get(token(df.decimal)), info).
              put(Str.get(DF_EXP), Str.get(token(df.exponent)), info).
              put(Str.get(DF_GRP), Str.get(token(df.grouping)), info).
              put(Str.get(DF_PC), Str.get(token(df.percent)), info).
              put(Str.get(DF_PM), Str.get(token(df.permille)), info).
              put(Str.get(DF_ZD), Str.get(token(df.zero)), info).
              put(Str.get(DF_DIG), Str.get(token(df.optional)), info).
              put(Str.get(DF_PAT), Str.get(token(df.pattern)), info).
              put(Str.get(DF_INF), Str.get(df.inf), info).
              put(Str.get(DF_NAN), Str.get(df.nan), info).
              put(Str.get(DF_MIN), Str.get(token(df.minus)), info)
          , info);
        }
        return map;
      default:
        throw INSPECT_UNKNOWN_X.get(info, name);
    }
  }

  /**
   * Converts codepoints to a token.
   * @param cps codepoints
   * @return token
   */
  private static byte[] token(final int... cps) {
    final TokenBuilder tb = new TokenBuilder(cps.length);
    for(final int cp : cps) tb.add(cp);
    return tb.finish();
  }
}
