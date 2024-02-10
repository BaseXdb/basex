package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnParseInteger extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toToken(arg(0), qc);
    final Item radix = arg(1).atomItem(qc, info);

    final long rdx = radix.isEmpty() ? 10 : toLong(radix);
    if(rdx < 2 || rdx > 36) throw INTRADIX_X.get(info, rdx);

    String string = Token.string(value).replaceAll("[_\\s]", "");
    final Boolean neg = string.startsWith("+") ? Boolean.FALSE : string.startsWith("-") ?
      Boolean.TRUE : null;
    if(neg != null) string = string.substring(1);
    if(string.isEmpty()) throw INTINVALID_X_X.get(info, rdx, value);

    long res = 0;
    for(final byte b : Token.token(string)) {
      final int num = b <= '9' ? b - 0x30 : (b & 0xDF) - 0x37;
      if(!(b >= '0' && b <= '9' || b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z') || num >= rdx)
        throw INTINVALID_X_X.get(info, rdx, value);

      res = res * rdx + num;
      if(res < 0) throw INTRANGE_X.get(info, value);
    }
    return Int.get(neg == Boolean.TRUE ? -res : res);
  }
}
