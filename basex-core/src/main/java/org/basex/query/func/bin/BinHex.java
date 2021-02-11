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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BinHex extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    byte[] token = token(0, qc);
    if(token == null) return Empty.VALUE;

    // add leading zero
    if((token.length & 1) != 0) token = concat(ZERO, token);
    try {
      return B64.get(Hex.parse(token, info));
    } catch(final QueryException ex) {
      Util.debug(ex);
      throw BIN_NNC.get(info);
    }
  }
}
