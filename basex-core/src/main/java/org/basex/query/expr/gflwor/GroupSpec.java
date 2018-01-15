package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Grouping spec.
 *
 * @author BaseX Team 2005-18, BSD License
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
   *
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
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return expr.item(qc, info);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final GroupSpec spec = copyType(new GroupSpec(info, cc.copy(var, vm), expr.copy(cc, vm), coll));
    spec.occluded = occluded;
    return spec;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public GroupSpec optimize(final CompileContext cc) throws QueryException {
    adoptType(expr);
    final AtomType type = expr.seqType().type.atomic();
    if(type != null) var.refineType(seqType().with(type), cc);
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
    final GroupSpec s = (GroupSpec) obj;
    return var.equals(s.var) && occluded == s.occluded && Objects.equals(coll, s.coll) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem elem = planElem();
    var.plan(elem);
    expr.plan(elem);
    plan.add(elem);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt(var).add(' ').add(ASSIGN).add(' ').addExt(expr);
    if(coll != null) tb.add(' ').add(COLLATION).add(" \"").add(coll.uri()).add('"');
    return tb.toString();
  }
}