package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BinHex extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    byte[] bytes = str(0, qc);
    if(bytes == null) return null;

    // add leading zero
    if((bytes.length & 1) != 0) bytes = concat(ZERO, bytes);
    try {
      return new B64(Hex.decode(bytes, info));
    } catch(final QueryException ex) {
      throw BIN_NNC.get(info);
    }
  }
}
