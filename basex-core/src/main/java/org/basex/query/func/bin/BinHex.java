package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinHex extends BinFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    byte[] value = toDigits(qc);
    if(value == null) return Empty.VALUE;

    // add leading zero
    if((value.length & 1) != 0) value = concat(cpToken('0'), value);
    try {
      return B64.get(Hex.parse(value, info));
    } catch(final QueryException ex) {
      throw BIN_NNC.get(info).cause(ex);
    }
  }
}
