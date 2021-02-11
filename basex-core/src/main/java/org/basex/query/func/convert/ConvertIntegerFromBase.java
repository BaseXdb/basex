package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ConvertIntegerFromBase extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token = toToken(exprs[0], qc);
    final long base = toLong(exprs[1], qc);
    if(base < 2 || base > 36) throw CONVERT_BASE_X.get(info, base);

    long res = 0;
    for(final byte b : token) {
      final int num = b <= '9' ? b - 0x30 : (b & 0xDF) - 0x37;
      if(!(b >= '0' && b <= '9' || b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z') || num >= base)
        throw CONVERT_INTEGER_X_X.get(info, base, (char) (b & 0xff));

      res = res * base + num;
    }
    return Int.get(res);
  }
}
