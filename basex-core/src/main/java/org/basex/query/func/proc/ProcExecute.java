package org.basex.query.func.proc;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcExecute extends ProcFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final ProcResult result = exec(qc, false);
    final TokenBuilder error = new TokenBuilder(error(result));
    final boolean ex = result.exception != null;
    if(ex) error.add(Util.message(result.exception));

    return XQMap.get(Records.PROC_RESULT.get(), output(result), Str.get(error.finish()),
      ex ? Empty.VALUE : Itr.get(result.code));
  }
}
