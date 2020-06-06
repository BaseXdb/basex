package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * If expression.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /** If expression. */
  public Expr cond;

  /**
   * Constructor with empty 'else' branch.
   * @param info input info
   * @param cond condition
   * @param branch1 'then' branch
   */
  public If(final InputInfo info, final Expr cond, final Expr branch1) {
    this(info, cond, branch1, Empty.VALUE);
  }

  /**
   * Constructor.
   * @param info input info
   * @param cond condition
   * @param branch1 'then' branch
   * @param branch2 'else' branch
   */
  public If(final InputInfo info, final Expr cond, final Expr branch1, final Expr branch2) {
    super(info, SeqType.ITEM_ZM, branch1, branch2);
    this.cond = cond;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    checkAllUp(exprs);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    cond = cond.compile(cc);
    // choose branches to compile
    final int[] branches = cond instanceof Value ? new int[] { branch(cc.qc) } : new int[] { 0, 1 };
    for(final int b : branches) {
      try {
        exprs[b] = exprs[b].compile(cc);
      } catch(final QueryException ex) {
        // replace original expression with error
        exprs[b] = cc.error(ex, this);
      }
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // if A then B else B  ->  B (errors in A will be ignored)
    if(exprs[0].equals(exprs[1])) {
      if(!cond.has(Flag.NDT)) return cc.replaceWith(this, exprs[0]);
      cond = cc.function(Function._PROF_VOID, info, cond);
      return cc.replaceWith(this, new List(info, cond, exprs[0]).optimize(cc));
    }

    if(Function.EMPTY.is(cond)) {
      // if empty(A) then B else C  ->  if exists(A) then C else B
      cond = cc.function(Function.EXISTS, info, ((Arr) cond).exprs[0]);
      cc.info(OPTSWAP_X, this);
      swap();
    } else if(Function.NOT.is(cond)) {
      // if not(A) then B else C  ->  if A then C else B
      cond = ((Arr) cond).exprs[0];
      cc.info(OPTSWAP_X, this);
      swap();
    }

    // static condition: return branch in question
    cond = cond.simplifyFor(Simplify.EBV, cc);
    if(cond instanceof Value) return cc.replaceWith(this, exprs[branch(cc.qc)]);

    // rewritings for constant booleans
    final Expr br1 = exprs[0], br2 = exprs[1];
    final SeqType st1 = br1.seqType(), st2 = br2.seqType();
    exprType.assign(st1.union(st2));

    if(st1.eq(SeqType.BLN_O) && st2.eq(SeqType.BLN_O)) {
      if(br1 == Bln.TRUE) {
        return cc.replaceWith(this, br2 == Bln.FALSE ?
          // if(A) then true() else false()  ->  xs:boolean(A)
          cc.function(Function.BOOLEAN, info, cond) :
          // if(A) then true() else C  ->  A or C
          new Or(info, cond, br2).optimize(cc));
      }

      if(br2 == Bln.TRUE) {
        return cc.replaceWith(this, br1 == Bln.FALSE ?
        // if(A) then false() else true()  ->  not(A)
        cc.function(Function.NOT, info, cond) :
        // if(A) then B else true()  ->  not(A) or B
        new Or(info, cc.function(Function.NOT, info, cond), br1).optimize(cc));
      }

      // if(A) then false() else C  ->  not(A) and C
      if(br1 == Bln.FALSE) return cc.replaceWith(this,
          new And(info, cc.function(Function.NOT, info, cond), br2).optimize(cc));

      // if(A) then B else false()  ->  A and B
      if(br2 == Bln.FALSE) return cc.replaceWith(this, new And(info, cond, br1).optimize(cc));
    }

    return this;
  }

  /**
   * Swaps the arguments.
   */
  public void swap() {
    final Expr tmp = exprs[0];
    exprs[0] = exprs[1];
    exprs[1] = tmp;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return exprs[branch(qc)].iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs[branch(qc)].value(qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return exprs[branch(qc)].item(qc, info);
  }

  /**
   * Evaluates the condition and returns the offset of the resulting branch.
   * @param qc query context
   * @return branch offset
   * @throws QueryException query exception
   */
  private int branch(final QueryContext qc) throws QueryException {
    return cond.ebv(qc, info).bool(info) ? 0 : 1;
  }

  @Override
  public boolean has(final Flag... flags) {
    return cond.has(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final Var var) {
    return cond.inlineable(var) && super.inlineable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return cond.count(var).plus(VarUsage.maximum(var, exprs));
  }

  @Override
  public Expr inline(final ExprInfo ei, final Expr ex, final CompileContext cc)
      throws QueryException {
    boolean changed = false;
    Expr inlined = cond.inline(ei, ex, cc);
    if(inlined != null) {
      cond = inlined;
      changed = true;
    }
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      try {
        inlined = exprs[e].inline(ei, ex, cc);
      } catch(final QueryException qe) {
        inlined = cc.error(qe, this);
      }
      if(inlined != null) {
        exprs[e] = inlined;
        changed = true;
      }
    }
    return changed ? optimize(cc) : null;
  }

  @Override
  public If copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new If(info, cond.copy(cc, vm), exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean vacuous() {
    return exprs[0].vacuous() && exprs[1].vacuous();
  }

  @Override
  public boolean ddo() {
    return exprs[0].ddo() && exprs[1].ddo();
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    for(final Expr expr : exprs) expr.markTailCalls(cc);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return simplifyAll(mode, cc) ? optimize(cc) : super.simplifyFor(mode, cc);
  }

  @Override
  public Data data() {
    return data(exprs);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return cond.accept(visitor) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int size = cond.exprSize();
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      Expr expr = exprs[e];
      try {
        expr = tc.check(expr, cc);
      } catch(final QueryException qe) {
        expr = cc.error(qe, expr);
      }
      if(expr != exprs[e]) {
        changed = true;
        exprs[e] = expr;
      }
    }
    return changed ? optimize(cc) : this;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof If && cond.equals(((If) obj).cond) && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), cond, exprs);
  }

  @Override
  public String toString() {
    return IF + '(' + cond + ") " + THEN + ' ' + exprs[0] + ' ' + ELSE + ' ' + exprs[1];
  }
}
