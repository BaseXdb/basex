package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Runtime expression, created by non-deterministic functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class RuntimeExpr extends ParseExpr {
  /** Arguments. */
  Var[] params;

  /**
   * Creates a new function item containing this expression as body.
   * @param expr expression
   * @param args number of arguments
   * @param sc static context
   * @param qc query context
   * @return function item
   */
  static FuncItem funcItem(final RuntimeExpr expr, final int args,
      final StaticContext sc, final QueryContext qc) {

    final VarScope vsc = new VarScope(sc);
    final Var[] params = new Var[args];
    for(int p = 0; p < args; p++) params[p] = vsc.newLocal(qc, null, null, true);
    expr.params = params;
    return new FuncItem(sc, new Ann(), null, expr.params, FuncType.ANY_FUN, expr,
        qc.value, qc.pos, qc.size, args);
  }

  /**
   * Constructor.
   * @param info input info
   */
  protected RuntimeExpr(final InputInfo info) {
    super(info);
  }

  @Override
  public void checkUp() throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public boolean has(final Flag flag) {
    throw Util.notExpected();
  }

  @Override
  public boolean removable(final Var var) {
    throw Util.notExpected();
  }

  @Override
  public VarUsage count(final Var var) {
    throw Util.notExpected();
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    throw Util.notExpected();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    throw Util.notExpected();
  }

  @Override
  public int exprSize() {
    throw Util.notExpected();
  }

  @Override
  public void plan(final FElem e) {
    throw Util.notExpected();
  }

  @Override
  public String toString() {
    return "Runtime function";
  }
}
