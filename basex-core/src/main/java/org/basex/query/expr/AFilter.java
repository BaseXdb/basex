package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class AFilter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param root root expression
   * @param preds predicate expressions
   */
  protected AFilter(final InputInfo info, final SeqType seqType, final Expr root,
      final Expr... preds) {
    super(info, seqType, preds);
    this.root = root;
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    root = root.compile(cc);
    return super.compile(cc);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final boolean has(final Flag... flags) {
    if(Flag.FCS.in(flags) || root.has(flags)) return true;
    final Flag[] flgs = Flag.FCS.remove(Flag.POS.remove(Flag.CTX.remove(flags)));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    return root.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public final VarUsage count(final Var var) {
    // context reference check: only consider root
    final VarUsage inRoot = root.count(var);
    if(var == null) return inRoot;

    final VarUsage inPreds = super.count(var);
    return inPreds == VarUsage.NEVER ? inRoot :
      root.seqType().zeroOrOne() ? inRoot.plus(inPreds) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    final Expr inlined = root.inline(ic);
    boolean changed = inlined != null;
    if(changed) root = inlined;

    // do not inline context reference in predicates
    changed |= ic.var != null && ic.cc.ok(root, true, () -> ic.inline(exprs));

    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    for(final Expr expr : exprs) {
      visitor.enterFocus();
      if(!expr.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return root.accept(visitor);
  }

  @Override
  public final boolean ddo() {
    return root.ddo();
  }

  @Override
  public final int exprSize() {
    return root.exprSize() + super.exprSize();
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), root, exprs);
  }
}
