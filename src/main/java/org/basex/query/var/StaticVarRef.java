package org.basex.query.var;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Reference to a static variable.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class StaticVarRef extends ParseExpr {
  /** Variable name. */
  private final QNm name;
  /** Referenced variable. */
  StaticVar var;
  /** URI of the enclosing module. */
  private final StaticContext sc;

  /**
   * Constructor.
   * @param ii input info
   * @param nm variable name
   * @param sctx static context
   */
  public StaticVarRef(final InputInfo ii, final QNm nm, final StaticContext sctx) {
    super(ii);
    name = nm;
    sc = sctx;
  }

  @Override
  public void checkUp() throws QueryException {
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope o) throws QueryException {
    var.compile(ctx);
    return var.value != null ? var.value : this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return var.value(ctx);
  }

  @Override
  public boolean uses(final Use u) {
    return var.expr != null && var.expr.uses(u);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.staticVar(var);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final StaticVarRef ref = new StaticVarRef(info, name, sc);
    ref.var = var;
    return ref;
  }

  @Override
  public int exprSize() {
    // should always be inlined
    return 0;
  }

  @Override
  public String toString() {
    return '$' + Token.string(name.string());
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.NEVER;
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp, final Var v,
      final Expr e) throws QueryException {
    return null;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.VAR, name));
  }

  /**
   * Initializes this reference with the given variable.
   * @param vr variable
   * @throws QueryException query exception
   */
  public void init(final StaticVar vr) throws QueryException {
    if(vr.ann.contains(Ann.Q_PRIVATE) && !sc.baseURI().eq(info, vr.sc.baseURI()))
      throw Err.VARPRIVATE.thrw(info, vr);
    var = vr;
  }
}
