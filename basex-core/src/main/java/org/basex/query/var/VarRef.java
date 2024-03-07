package org.basex.query.var;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Local Variable Reference expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class VarRef extends ParseExpr {
  /** Variable name. */
  public final Var var;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param var variable
   */
  public VarRef(final InputInfo info, final Var var) {
    super(info, SeqType.ITEM_ZM);
    this.var = var;
  }

  @Override
  public Expr compile(final CompileContext cc) {
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    return var.seqType().zero() ? Empty.VALUE : assignType();
  }

  @Override
  public Value value(final QueryContext qc) {
    return qc.get(var);
  }

  @Override
  public boolean ddo() {
    return var.ddo();
  }

  @Override
  public boolean inlineable(final InlineContext v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return var == v ? VarUsage.ONCE : VarUsage.NEVER;
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    // replace variable reference with expression
    return var == ic.var  ? ic.copy() : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Var nw = vm.get(var.id);
    return new VarRef(info, nw != null ? nw : var).assignType();
  }

  /**
   * Assigns the variable type to the expression.
   * @return self reference
   */
  private VarRef assignType() {
    exprType.assign(var.seqType(), var.size()).data(var.data());
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.used(this);
  }

  @Override
  public void checkUp() {
  }

  @Override
  public boolean has(final Flag... flags) {
    return false;
  }

  @Override
  public int exprSize() {
    return 1;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof VarRef && var.slot == ((VarRef) obj).var.slot;
  }

  @Override
  public String description() {
    return "variable";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    final FBuilder elem = plan.create(this);
    plan.addAttribute(elem, NAME, var.toErrorString());
    plan.addAttribute(elem, ID, var.id);
    plan.add(elem);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(var.id());
  }

  @Override
  public String toErrorString() {
    return var.toErrorString();
  }
}
