package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertStringToHex extends ConvertFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    return new Hex(stringToBinary(qc));
  }
}
