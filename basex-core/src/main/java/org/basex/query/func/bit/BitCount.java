package org.basex.query.func.bit;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitCount extends BitFn {
  @Override
  Value value(final long value, final QueryContext qc) {
    return Itr.get(Long.bitCount(value));
  }
}
