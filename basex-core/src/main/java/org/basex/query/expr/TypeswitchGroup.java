package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Group of type switch cases.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param value value to be bound
   * @param cc compilation context
   * @throws QueryException query exception
   */
  void inline(final Value value, final CompileContext cc) throws QueryException {
    if(var != null) {
      expr = new InlineContext(var, var.checkType(value, cc.qc, true), cc).inline(expr);
    }
  }

  /**
   * Rewrites the group expression to a standalone expression.
   * @param cond condition
   * @param cc compilation context
   * @return new expression
   * @throws QueryException query exception
   */
  Expr rewrite(final Expr cond, final CompileContext cc) throws QueryException {
    if(var == null) return cc.merge(cond, expr, info);

    final IntObjMap<Var> vm = new IntObjMap<>();
    final LinkedList<Clause> clauses = new LinkedList<>();
    clauses.add(new Let(cc.copy(var, vm), cond).optimize(cc));
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
  public Expr inline(final InlineContext ic) {
    try {
      return super.inline(ic);
    } catch(final QueryException qe) {
      expr = ic.cc.error(qe, this);
      return this;
    }
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    Expr ex;
    try {
      ex = tc.check(expr, cc);
    } catch(final QueryException qe) {
      ex = cc.error(qe, expr);
    }
    // returned expression will be handled Typeswitch#typeCheck
    if(ex == null) return null;
    expr = ex;
    return optimize(cc);
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
  boolean noMatches(final SeqType seqType) {
    for(final SeqType st : seqTypes) {
      if(st.intersect(seqType) != null) return false;
    }
    return true;
  }

  /**
   * Checks if the given value matches this group.
   * @param value value to be matched
   * @param qc query context (if {@code null}, value will not be assigned to the variable)
   * @return result of check ({@code true} is returned for the default case
   * @throws QueryException query exception
   */
  boolean match(final Value value, final QueryContext qc) throws QueryException {
    final int sl = seqTypes.length;
    boolean found = sl == 0;
    for(int s = 0; !found && s < sl; s++) found = seqTypes[s].instance(value);
    if(found && var != null && qc != null) qc.set(var, value);
    return found;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return (var != null ? expr.value(qc) : expr).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return expr.value(qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return expr.item(qc, info);
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

  /**
   * Simplifies all expressions for requests of the specified type.
   * @param mode mode of simplification
   * @param cc compilation context
   * @return {@code true} if expression has changed
   * @throws QueryException query exception
   */
  public boolean simplify(final Simplify mode, final CompileContext cc) throws QueryException {
    final Expr ex = expr.simplifyFor(mode, cc);
    if(ex == expr) return false;
    expr = ex;
    return true;
  }

  @Override
  public Data data() {
    return expr.data();
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
  public void plan(final QueryString qs) {
    final boolean cases = seqTypes.length > 0;
    qs.token(cases ? CASE : DEFAULT);
    if(var != null) {
      qs.token(var);
      if(cases) qs.token(AS);
    }
    if(cases) qs.tokens(seqTypes, "|");
    qs.token(RETURN).token(expr);
  }
}
