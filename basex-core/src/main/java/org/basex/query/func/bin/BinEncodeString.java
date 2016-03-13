package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;

import java.nio.charset.*;

import org.basex.query.*;
import org.basex.query.func.convert.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class BinEncodeString extends BinFn {
  @Override
  public B64 item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] str = str(0, qc);
    final String enc = toEncoding(1, BIN_UE_X, qc);
    if(str == null) return null;
    try {
      return new B64(enc == null || enc == Strings.UTF8 ? str : ConvertFn.toBinary(str, enc));
    } catch(final CharacterCodingException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }
}
