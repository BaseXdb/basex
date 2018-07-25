package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class UtilIf extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return new If(info, exprs[0], exprs[1], exprs.length == 2 ? Empty.SEQ : exprs[2]).optimize(cc);
  }
}
