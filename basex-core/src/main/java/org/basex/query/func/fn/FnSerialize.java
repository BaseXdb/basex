package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnSerialize extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final SerializerOptions options = toSerializerOptions(arg(1), qc);

    return Str.get(serialize(input, options, SERPARAM_X, qc));
  }
}
