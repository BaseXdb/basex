package org.basex.query.func.proc;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProcExecute extends ProcFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ProcResult result = exec(qc, false);
    final boolean ex = result.exception != null;
    if(ex) result.error.add(result.exception.getMessage());
    final byte[] output = result.output.normalize().finish();
    final byte[] error = result.error.normalize().finish();

    final FElem root = new FElem(RESULT);
    if(output.length != 0) root.add(new FElem(OUTPUT).add(output));
    if(error.length != 0) root.add(new FElem(ERROR).add(error));
    if(!ex) root.add(new FElem(CODE).add(token(result.code)));
    return root;
  }
}
