package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnParseInteger extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toToken(arg(0), qc);
    final long radix = toLong(arg(1), qc);
    if(radix < 2 || radix > 36) throw INTRADIX_X.get(info, radix);

    long res = 0;
    for(final byte b : value) {
      final int num = b <= '9' ? b - 0x30 : (b & 0xDF) - 0x37;
      if(!(b >= '0' && b <= '9' || b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z') || num >= radix)
        throw INTINVALID_X_X.get(info, radix, (char) (b & 0xff));

      res = res * radix + num;
    }
    return Int.get(res);
  }
}
