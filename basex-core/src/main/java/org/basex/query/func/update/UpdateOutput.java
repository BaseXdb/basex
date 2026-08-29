package org.basex.query.func.update;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdateOutput extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(qc.updates().mod instanceof TransformModifier) throw BASEX_UPDATE.get(info);

    qc.updates().addOutput(arg(0).value(qc), qc);
    return Empty.VALUE;
  }
}
