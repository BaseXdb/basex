package org.basex.query.func.proc;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcSystem extends ProcFn {
  @Override
  public Item value(final QueryContext qc) throws QueryException {
    final ProcResult result = exec(qc, false);
    if(result.exception != null) throw PROC_ERROR_X.get(info, result.exception);
    if(result.code == 0) return output(result);

    // create error message
    final QNm name = new QNm("code" + String.format("%04d", result.code), QueryText.PROC_URI);
    throw new QueryException(info, name, string(error(result)));
  }
}
