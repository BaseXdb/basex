package org.basex.query.func.convert;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.bin.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ConvertBinaryToIntegers extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return BinToOctets.toIter(toBin(arg(0), qc).binary(info));
  }
}
