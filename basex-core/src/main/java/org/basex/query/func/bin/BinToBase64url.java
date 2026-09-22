package org.basex.query.func.bin;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BinToBase64url extends BinFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Bin value = toBinOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;
    return Str.get(Base64.getUrlEncoder().withoutPadding().encode(value.binary(info)));
  }
}
