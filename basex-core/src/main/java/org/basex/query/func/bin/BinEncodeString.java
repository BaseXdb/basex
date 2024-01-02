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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class BinEncodeString extends BinFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toTokenOrNull(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), BIN_UE_X, qc);
    if(string == null) return Empty.VALUE;
    try {
      return B64.get(encoding == null || encoding == Strings.UTF8 ? string :
        ConvertFn.toBinary(string, encoding));
    } catch(final CharacterCodingException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }
}
