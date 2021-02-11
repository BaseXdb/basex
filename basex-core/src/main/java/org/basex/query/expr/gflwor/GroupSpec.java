package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Grouping spec.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class GroupSpec extends Single {
  /** Grouping variable. */
  public final Var var;
  /** Occlusion flag, {@code true} if another grouping variable shadows this one. */
  public boolean occluded;
  /** Collation (can be {@code null}). */
  final Collation coll;

  /**
   * Constructor.
   * @param info input info
   * @param var grouping variable
   * @param expr grouping expression
   * @param coll collation (can be {@code null})
   */
  public GroupSpec(final InputInfo info, final Var var, final Expr expr, final Collation coll) {
    super(info, expr, SeqType.ITEM_ZM);
    this.var = var;
    this.coll = coll;
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    return expr.atomItem(qc, ii);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final GroupSpec spec = new GroupSpec(info, cc.copy(var, vm), expr.copy(cc, vm), coll);
    spec.occluded = occluded;
    return copyType(spec);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public GroupSpec optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.DATA, cc);

    exprType.assign(expr);
    final AtomType type = expr.seqType().type.atomic();
    if(type != null) var.refineType(SeqType.get(type, seqType().occ), cc);
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && visitor.declared(var);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof GroupSpec)) return false;
    final GroupSpec gs = (GroupSpec) obj;
    return var.equals(gs.var) && occluded == gs.occluded && Objects.equals(coll, gs.coll) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.attachVariable(plan.create(this), var, false), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(var).token(ASSIGN).token(expr);
    if(coll != null) qs.token(COLLATION).token("\"").token(coll.uri()).token('"');
  }
}