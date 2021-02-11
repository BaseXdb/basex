package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.nio.charset.*;

import org.basex.query.*;
import org.basex.query.func.convert.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BinEncodeString extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] token = token(0, qc);
    final String encoding = toEncodingOrNull(1, BIN_UE_X, qc);
    if(token == null) return Empty.VALUE;
    try {
      return B64.get(encoding == null || encoding == Strings.UTF8 ? token :
        ConvertFn.toBinary(token, encoding));
    } catch(final CharacterCodingException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }
}
