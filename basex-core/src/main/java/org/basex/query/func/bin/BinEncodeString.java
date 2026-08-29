package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.nio.charset.*;

import org.basex.query.*;
import org.basex.query.func.convert.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinEncodeString extends BinFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), BIN_UE_X, qc);
    if(value == null) return Empty.VALUE;
    try {
      return B64.get(ConvertFn.toBinary(value, encoding));
    } catch(final CharacterCodingException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }
}
