package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class ConvertEncodeKey extends ConvertIntegersToBase64 {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] key = toToken(arg(0), qc);
    final boolean lax = toBooleanOrFalse(arg(1), qc);

    final byte[] string = XMLToken.encode(key, lax);
    return Str.get(string);
  }
}
