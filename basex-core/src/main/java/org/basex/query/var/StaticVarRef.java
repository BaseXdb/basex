package org.basex.query.var;

import static org.basex.query.util.Err.*;

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
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class StaticVarRef extends ParseExpr {
  /** Variable name. */
  private final QNm name;
  /** Referenced variable. */
  private StaticVar var;
  /** URI of the enclosing module. */
  private final StaticContext sc;

  /**
   * Constructor.
   * @param info input info
   * @param name variable name
   * @param sc static context
   */
  public StaticVarRef(final InputInfo info, final QNm name, final StaticContext sc) {
    super(info);
    this.name = name;
    this.sc = sc;
  }

  @Override
  public void checkUp() {
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope o) throws QueryException {
    var.compile(qc);
    seqType = var.seqType();
    return var.val != null ? var.val : this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return var.value(qc);
  }

  @Override
  public boolean has(final Flag flag) {
    return var != null && var.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.staticVar(var);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
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
  public Expr inline(final QueryContext qc, final VarScope scp, final Var v, final Expr ex) {
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
    if(vr.ann.contains(Ann.Q_PRIVATE) && !Token.eq(sc.baseURI().string(),
       vr.sc.baseURI().string())) throw VARPRIVATE_X.get(info, vr);
    var = vr;
  }
}
