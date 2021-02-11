package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BinOctal extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token = token(0, qc);
    if(token == null) return Empty.VALUE;
    final int tl = token.length;
    if(tl == 0) return B64.EMPTY;

    try {
      byte[] bin = Token.token(new BigInteger(Token.string(token), 8).toString(2));
      final int expl = tl * 3, binl = bin.length;
      if(binl != expl) {
        // add leading zeroes
        final byte[] tmp = new byte[expl];
        Arrays.fill(tmp, 0, expl - binl, (byte) '0');
        Array.copyFromStart(bin, binl, tmp, expl - binl);
        bin = tmp;
      }
      return B64.get(binary2bytes(bin));
    } catch(final NumberFormatException ex) {
      Util.debug(ex);
      throw BIN_NNC.get(info);
    }
  }
}
