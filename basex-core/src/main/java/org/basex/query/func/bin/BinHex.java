package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinHex extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    byte[] string = toTokenOrNull(arg(0), qc);
    if(string == null) return Empty.VALUE;

    // add leading zero
    if((string.length & 1) != 0) string = concat(cpToken('0'), string);
    try {
      return B64.get(Hex.parse(string, info));
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw BIN_NNC.get(info);
    }
  }
}
