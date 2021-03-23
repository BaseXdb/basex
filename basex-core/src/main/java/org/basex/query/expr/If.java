package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /** Condition. */
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

    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      try {
        exprs[e] = exprs[e].compile(cc);
      } catch(final QueryException ex) {
        // replace original expression with error
        exprs[e] = cc.error(ex, exprs[e]);
      }
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    if(EMPTY.is(cond)) {
      // if(empty(A)) then B else C  ->  if(exists(A)) then C else B
      cond = cc.function(EXISTS, info, cond.arg(0));
      swap();
      cc.info(QueryText.OPTSWAP_X, this);
    } else if(NOT.is(cond)) {
      // if(not(A)) then B else C  ->  if(A) then C else B
      cond = cond.arg(0);
      swap();
      cc.info(QueryText.OPTSWAP_X, this);
    }
    // if(exists(nodes))  ->  if(nodes)
    cond = cond.simplifyFor(Simplify.EBV, cc);

    return cc.replaceWith(this, opt(cc));
  }

  /**
   * Optimizes the expression.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  private Expr opt(final CompileContext cc) throws QueryException {
    // choose static branch at compile time
    if(cond instanceof Value) return expr(cc.qc);

    // if(...empty sequence...) then A else B  ->  B
    final Expr br1 = exprs[0], br2 = exprs[1];
    final SeqType ct = cond.seqType();
    final boolean ndt = cond.has(Flag.NDT);
    if(ct.zero() && !ndt) return br2;

    // rewrite to elvis operator:
    //   if(exists(VALUE)) then VALUE else DEFAULT  ->  VALUE ?: DEFAULT
    //   if(NODES) then NODES else DEFAULT  ->  NODES ?: DEFAULT
    final Expr cmp = EXISTS.is(cond) ? cond.arg(0) :
      ct.type instanceof NodeType ? cond : null;
    if(!ndt && cmp != null && cmp.equals(br1)) return
        cc.function(_UTIL_OR, info, br1, br2);

    // if(A) then B else B  ->  prof:void(A), B
    if(br1.equals(br2)) return cc.merge(cond, br1, info);

    // determine type
    final SeqType st1 = br1.seqType(), st2 = br2.seqType();
    exprType.assign(st1.union(st2));

    // logical rewritings
    if(st1.eq(SeqType.BOOLEAN_O) && st2.eq(SeqType.BOOLEAN_O)) {
      if(br1 == Bln.TRUE) return br2 == Bln.FALSE ?
        // if(A) then true() else false()  ->  boolean(A)
        cc.function(BOOLEAN, info, cond) :
        // if(A) then true() else C  ->  A or C
        new Or(info, cond, br2).optimize(cc);
      if(br2 == Bln.TRUE) return br1 == Bln.FALSE ?
        // if(A) then false() else true()  ->  not(A)
        cc.function(NOT, info, cond) :
        // if(A) then B else true()  ->  not(A) or B
        new Or(info, cc.function(NOT, info, cond), br1).optimize(cc);
      // if(A) then false() else C  ->  not(A) and C
      if(br1 == Bln.FALSE) return
        new And(info, cc.function(NOT, info, cond), br2).optimize(cc);
      // if(A) then B else false()  ->  A and B
      if(br2 == Bln.FALSE) return
        new And(info, cond, br1).optimize(cc);

      if(contradict(br1, br2, false)) return new CmpG(
          cc.function(BOOLEAN, info, cond), br1, OpG.EQ, null, cc.sc(), info).optimize(cc);
      if(contradict(br2, br1, false)) return new CmpG(
          cc.function(BOOLEAN, info, cond), br2, OpG.NE, null, cc.sc(), info).optimize(cc);
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
    return expr(qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return expr(qc).value(qc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return expr(qc).item(qc, info);
  }

  /**
   * Tests the condition and returns the expression to evaluate.
   * @param qc query context
   * @return branch offset
   * @throws QueryException query exception
   */
  private Expr expr(final QueryContext qc) throws QueryException {
    return exprs[cond.ebv(qc, info).bool(info) ? 0 : 1];
  }

  @Override
  public boolean has(final Flag... flags) {
    return cond.has(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return cond.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return cond.count(var).plus(VarUsage.maximum(var, exprs));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = ic.inline(exprs, true);
    final Expr inlined = cond.inline(ic);
    if(inlined != null) {
      changed = true;
      cond = inlined;
    }
    return changed ? optimize(ic.cc) : null;
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
      if(expr != null) {
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
  public void plan(final QueryString qs) {
    qs.token("(").token(QueryText.IF).paren(cond).token(QueryText.THEN).token(exprs[0]);
    qs.token(QueryText.ELSE).token(exprs[1]).token(')');
  }
}
