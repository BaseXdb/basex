package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Simple expression without arguments.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Simple extends ParseExpr {
  /**
   * Constructor.
   * @param info input info
   * @param seqType sequence type
   */
  protected Simple(final InputInfo info, final SeqType seqType) {
    super(info, seqType);
  }

  @Override
  public final void checkUp() {
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    return optimize(cc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return false;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.NEVER;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return true;
  }

  @Override
  public int exprSize() {
    return 1;
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this));
  }
}
