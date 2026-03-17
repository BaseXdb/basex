package org.basex.query.func.proc;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
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

    final FBuilder root = FElem.build(Q_RESULT);
    if(output.length != 0) root.node(FElem.build(Q_OUTPUT).text(output));
    if(error.length != 0) root.node(FElem.build(Q_ERROR).text(error));
    if(!ex) root.node(FElem.build(Q_CODE).text(result.code));
    return root.finish();
  }
}
