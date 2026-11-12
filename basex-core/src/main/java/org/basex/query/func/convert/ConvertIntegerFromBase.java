package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertIntegerFromBase extends StandardFunc {
  @Override
  public Itr value(final QueryContext qc) throws QueryException {
    final byte[] value = toToken(arg(0), qc);
    final long base = toLong(arg(1), qc);
    if(base < 2 || base > 36) throw CONVERT_BASE_X.get(info, base);

    long res = 0;
    final int vl = value.length;
    for(int v = 0; v < vl; v++) {
      final byte b = value[v];
      final int num = b <= '9' ? b - 0x30 : (b & 0xDF) - 0x37;
      if(!(b >= '0' && b <= '9' || b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z') || num >= base)
        throw CONVERT_INTEGER_X_X.get(info, base, Strings.describe(cp(value, v)));

      res = res * base + num;
    }
    return Itr.get(res);
  }
}
