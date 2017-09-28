package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ConvertBytesToHex extends ConvertBytesToBase64 {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new Hex(bytesToB64(qc).binary(ii));
  }
}
