package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class UtilArrayMember extends StandardFunc {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return XQArray.singleton(arg(0).value(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    exprType.assign(ArrayType.get(arg(0).seqType()));
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.STRING, Simplify.NUMBER, Simplify.DATA)) {
      expr = arg(0).simplify(mode, cc);
    }
    return cc.simplify(this, expr, mode);
  }
}
