package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class TypeswitchGroup extends Single {
  /** Variable. */
  final Var var;
  /** Matched sequence types (default switch if array is empty). */
  private SeqType[] types;

  /**
   * Constructor.
   * @param info input info
   * @param var variable
   * @param types sequence types this case matches, the empty array means {@code default}
   * @param rtrn return expression
   */
  public TypeswitchGroup(final InputInfo info, final Var var, final SeqType[] types,
      final Expr rtrn) {
    super(info, rtrn, SeqType.ITEM_ZM);
    this.var = var;
    this.types = types;
  }

  @Override
  public Expr compile(final CompileContext cc) {
    try {
      super.compile(cc);
    } catch(final QueryException ex) {
      // replace original expression with error
      expr = cc.error(ex, expr);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
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
   * Removes redundant types.
   * @param cc compilation context
   * @param cache cached types
   * @return {@code true} if the group is here to stay
   */
  boolean removeTypes(final CompileContext cc, final ArrayList<SeqType> cache) {
    // default branch must be preserved
    if(types.length == 0) return true;
    // remove redundant types
    final ArrayList<SeqType> tmp = new ArrayList<>();
    for(final SeqType st : types) {
      if(cache.contains(st)) {
        cc.info(OPTREMOVE_X_X, st, (Supplier<?>) this::description);
      } else {
        tmp.add(st);
        cache.add(st);
      }
    }
    if(types.length != tmp.size()) types = tmp.toArray(new SeqType[0]);
    return types.length != 0;
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
    return copyType(new TypeswitchGroup(info, cc.copy(var, vm), types.clone(), expr.copy(cc, vm)));
  }

  /**
   * Checks if the given value matches this case.
   * @param val value to be matched
   * @return {@code true} if it matches, {@code false} otherwise
   */
  boolean matches(final Value val) {
    if(types.length == 0) return true;
    for(final SeqType st : types) {
      if(st.instance(val)) return true;
    }
    return false;
  }

  /**
   * Evaluates the expression.
   * @param qc query context
   * @param seq sequence to be checked
   * @return resulting iterator or {@code null}
   * @throws QueryException query exception
   */
  Iter iter(final QueryContext qc, final Value seq) throws QueryException {
    if(!matches(seq)) return null;
    if(var == null) return expr.iter(qc);

    // evaluate full expression if variable needs to be bound
    qc.set(var, seq);
    return expr.value(qc).iter();
  }

  /**
   * Evaluates the expression.
   * @param qc query context
   * @param seq sequence to be checked
   * @return resulting value or {@code null}
   * @throws QueryException query exception
   */
  Value value(final QueryContext qc, final Value seq) throws QueryException {
    if(!matches(seq)) return null;

    if(var != null) qc.set(var, seq);
    return expr.value(qc);
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
    return Array.equals(types, tg.types) && var.equals(tg.var) && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    final FElem elem = plan.attachVariable(plan.create(this), var, false);
    if(types.length == 0) {
      plan.addAttribute(elem, DEFAULT, true);
    } else {
      final TokenBuilder tb = new TokenBuilder();
      for(final SeqType st : types) {
        if(!tb.isEmpty()) tb.add('|');
        tb.add(st);
      }
      plan.addAttribute(elem, CASE, tb);
    }
    plan.add(elem, expr);
  }

  @Override
  public String toString() {
    final int tl = types.length;
    final TokenBuilder tb = new TokenBuilder().add(tl == 0 ? DEFAULT : CASE);
    if(var != null) {
      tb.add(' ').add(var);
      if(tl != 0) tb.add(' ').add(AS);
    }
    if(tl != 0) {
      for(int t = 0; t < tl; t++) {
        if(t > 0) tb.add(" |");
        tb.add(' ').add(types[t]);
      }
    }
    return tb.add(' ' + RETURN + ' ' + expr).toString();
  }
}
