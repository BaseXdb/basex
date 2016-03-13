package org.basex.query.func.proc;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ProcExecute extends ProcFn {
  /** Name: result. */
  private static final String RESULT = "result";
  /** Name: standard output. */
  private static final String OUTPUT = "output";
  /** Name: standard error. */
  private static final String ERROR = "error";
  /** Name: code. */
  private static final String CODE = "code";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Result result = exec(qc);

    final FElem root = new FElem(RESULT);
    root.add(new FElem(OUTPUT).add(result.output.normalize().finish()));
    root.add(new FElem(ERROR).add(result.error.normalize().finish()));
    root.add(new FElem(CODE).add(token(result.code)));
    return root;
  }
}
