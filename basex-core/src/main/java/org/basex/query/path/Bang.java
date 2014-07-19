package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Map ("bang") operator. Only occurs as argument of the {@link MixedPath} expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Bang extends Single {
  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   */
  public Bang(final InputInfo info, final Expr expr) {
    super(info, expr);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    seqType = expr.seqType();
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(expr);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return qc.value(expr);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Bang(info, expr.copy(qc, scp, vs));
  }

  @Override
  public boolean isVacuous() {
    return expr.isVacuous();
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public String description() {
    return "Map operator";
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
