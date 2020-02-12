package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Group of type switch cases.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class TypeswitchGroup extends Single {
  /** Matched sequence types (default switch if array is empty). */
  SeqType[] seqTypes;
  /** Variable (can be {@code null}). */
  private Var var;

  /**
   * Constructor.
   * @param info input info
   * @param var variable (can be {@code null})
   * @param seqTypes sequence types this case matches, the empty array means {@code default}
   * @param rtrn return expression
   */
  public TypeswitchGroup(final InputInfo info, final Var var, final SeqType[] seqTypes,
      final Expr rtrn) {
    super(info, rtrn, SeqType.ITEM_ZM);
    this.var = var;
    this.seqTypes = seqTypes;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    try {
      super.compile(cc);
    } catch(final QueryException ex) {
      // replace original expression with error
      expr = cc.error(ex, expr);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    if(var != null) {
      if(expr.count(var) == VarUsage.NEVER) {
        cc.info(OPTVAR_X, var);
        var = null;
      } else {
        refineType(cc);
      }
    }
    return adoptType(expr);
  }

  /**
   * Inlines the expression.
   * @param cc compilation context
   * @param value value to be bound
   * @throws QueryException query exception
   */
  void inline(final CompileContext cc, final Value value) throws QueryException {
    if(var == null) return;
    final Expr ex = expr.inline(var, var.checkType(value, cc.qc, true), cc);
    if(ex != null) expr = ex;
  }

  /**
   * Rewrites the group expression to a standalone expression.
   * @param cond condition
   * @param cc compilation context
   * @return new expression
   * @throws QueryException query exception
   */
  Expr rewrite(final Expr cond, final CompileContext cc) throws QueryException {
    if(var == null) return expr;
    final IntObjMap<Var> vm = new IntObjMap<>();
    final LinkedList<Clause> clauses = new LinkedList<>();
    clauses.add(new Let(cc.copy(var, vm), cond, false).optimize(cc));
    final Expr rtrn = expr.copy(cc, vm).optimize(cc);
    return new GFLWOR(info, clauses, rtrn).optimize(cc);
  }

  /**
   * Removes checks that will never match.
   * @param ct type of condition
   * @param cache types checked so far
   * @param cc compilation context
   * @return {@code true} if the group is here to stay
   * @throws QueryException query exception
   */
  boolean removeTypes(final SeqType ct, final ArrayList<SeqType> cache, final CompileContext cc)
      throws QueryException {

    // preserve default branch
    final int sl = seqTypes.length;
    if(sl == 0) return true;

    final Predicate<SeqType> remove = seqType -> {
      for(final SeqType st : cache) if(seqType.instanceOf(st)) return true;
      return seqType.intersect(ct) == null;
    };

    // remove specific types
    final ArrayList<SeqType> tmp = new ArrayList<>(sl);
    for(final SeqType st : seqTypes) {
      if(remove.test(st)) {
        cc.info(OPTREMOVE_X_X, st, (Supplier<?>) this::description);
      } else {
        tmp.add(st);
        cache.add(st);
      }
    }

    // replace types
    if(sl != tmp.size()) {
      if(tmp.isEmpty()) return false;
      seqTypes = tmp.toArray(new SeqType[0]);
      refineType(cc);
    }

    return true;
  }

  @Override
  public Expr inline(final Var v, final Expr ex, final CompileContext cc) {
    try {
      return super.inline(v, ex, cc);
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
      return this;
    }
  }

  @Override
  public TypeswitchGroup copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new TypeswitchGroup(info, cc.copy(var, vm), seqTypes.clone(),
        expr.copy(cc, vm)));
  }

  /**
   * Checks if the given sequence type matches this group.
   * @param seqType sequence type
   * @return result of check
   */
  boolean instance(final SeqType seqType) {
    for(final SeqType st : seqTypes) {
      if(seqType.instanceOf(st)) return true;
    }
    return false;
  }

  /**
   * Checks if the given type never matches this group at runtime.
   * @param seqType sequence type to be matched
   * @return result of check
   */
  boolean isNever(final SeqType seqType) {
    for(final SeqType st : seqTypes) {
      if(st.intersect(seqType) != null) return false;
    }
    return true;
  }

  /**
   * Checks if the given value matches this group.
   * @param value value to be matched
   * @return result of check ({@code true} is returned for the default case
   */
  boolean instance(final Value value) {
    if(seqTypes.length == 0) return true;
    for(final SeqType st : seqTypes) {
      if(st.instance(value)) return true;
    }
    return false;
  }

  /**
   * Evaluates the expression.
   * @param qc query context
   * @param value sequence to be checked
   * @return resulting iterator or {@code null}
   * @throws QueryException query exception
   */
  Iter iter(final QueryContext qc, final Value value) throws QueryException {
    if(!instance(value)) return null;
    if(var == null) return expr.iter(qc);

    // evaluate full expression if variable needs to be bound
    qc.set(var, value);
    return expr.value(qc).iter();
  }

  /**
   * Evaluates the expression.
   * @param qc query context
   * @param value sequence to be checked
   * @return resulting value or {@code null}
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc, final Value value) throws QueryException {
    if(!instance(value)) return null;

    if(var != null) qc.set(var, value);
    return expr.value(qc);
  }

  /**
   * Refines the variable type, based on the available sequence types.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  private void refineType(final CompileContext cc) throws QueryException {
    final int sl = seqTypes.length;
    if(var == null || sl == 0) return;

    SeqType st = seqTypes[0];
    for(int s = 1; s < sl; s++) st = st.union(seqTypes[s]);
    var.refineType(st, cc);
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    expr.markTailCalls(cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && (var == null || visitor.declared(var));
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof TypeswitchGroup)) return false;
    final TypeswitchGroup tg = (TypeswitchGroup) obj;
    return Array.equals(seqTypes, tg.seqTypes) && Objects.equals(var, tg.var) && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    final FElem elem = plan.attachVariable(plan.create(this), var, false);
    if(seqTypes.length == 0) {
      plan.addAttribute(elem, DEFAULT, true);
    } else {
      final TokenBuilder tb = new TokenBuilder();
      for(final SeqType st : seqTypes) {
        if(!tb.isEmpty()) tb.add('|');
        tb.add(st);
      }
      plan.addAttribute(elem, CASE, tb);
    }
    plan.add(elem, expr);
  }

  @Override
  public String toString() {
    final int sl = seqTypes.length;
    final TokenBuilder tb = new TokenBuilder().add(sl == 0 ? DEFAULT : CASE);
    if(var != null) {
      tb.add(' ').add(var);
      if(sl != 0) tb.add(' ').add(AS);
    }
    if(sl != 0) {
      for(int s = 0; s < sl; s++) {
        if(s > 0) tb.add(" |");
        tb.add(' ').add(seqTypes[s]);
      }
    }
    return tb.add(' ' + RETURN + ' ' + expr).toString();
  }
}
