package org.basex.query.func.proc;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ProcExecute extends ProcFn {
  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ProcResult result = exec(qc, false);
    final boolean ex = result.exception != null;
    if(ex) result.error.add(result.exception.getMessage());
    final byte[] output = result.output.normalize().finish();
    final byte[] error = result.error.normalize().finish();

    final FBuilder root = FElem.build(RESULT);
    if(output.length != 0) root.add(FElem.build(OUTPUT).add(output));
    if(error.length != 0) root.add(FElem.build(ERROR).add(error));
    if(!ex) root.add(FElem.build(CODE).add(result.code));
    return root.finish();
  }
}
