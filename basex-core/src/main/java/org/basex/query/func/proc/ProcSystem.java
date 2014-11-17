package org.basex.query.func.proc;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.util.Err.ErrType;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ProcSystem extends ProcFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Result result = exec(qc);
    if(result.code == 0) return Str.get(norm(result.output));

    // create error message
    final QNm name = new QNm(ErrType.BXPR + String.format("%04d", result.code));
    throw new QueryException(info, name, string(norm(result.error)));
  }
}
