package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertIntegersToHex extends ConvertIntegersToBase64 {
  @Override
  public Hex value(final QueryContext qc) throws QueryException {
    return new Hex(bytesToB64(qc).binary(info));
  }
}
