package org.basex.query.expr;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.Function;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative evaluation of a recursive function body whose recursive call is replaced by a
 * recursion step.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Unfold extends Arr {
  /** State variables, bound to the initial expressions. */
  private final Var[] vars;
  /** Variable indicating that a recursion step was taken. */
  private final Var flag;
  /** Variable for the results that follow the recursive call (can be {@code null}). */
  private final Var suffix;
  /** Function body with the recursion step. */
  private Expr step;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param init initial expressions
   * @param vars state variables
   * @param flag variable indicating that a recursion step was taken
   * @param suffix variable for the results that follow the recursive call (can be {@code null})
   * @param step function body with the recursion step
   */
  Unfold(final InputInfo info, final Expr[] init, final Var[] vars, final Var flag,
      final Var suffix, final Expr step) {
    super(info, Types.ITEM_ZM, init);
    this.vars = vars;
    this.flag = flag;
    this.suffix = suffix;
    this.step = step;
  }

  @Override
  public void checkUp() throws QueryException {
    super.checkUp();
    checkNoUp(step);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    step = step.compile(cc);
    return super.compile(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr expr = iterate(cc);
    if(expr != null) return cc.replaceWith(this, expr);

    final SeqType sst = step.seqType();
    final SeqType st = suffix != null ? sst.union(suffix.seqType()) : sst;
    exprType.assign(st.type, st.zero() ? Occ.ZERO : Occ.get(sst.occ.min, Long.MAX_VALUE));
    return this;
  }

  /**
   * Rewrites a loop over an integer counter or over the items of a sequence to an iteration.
   * @param cc compilation context
   * @return rewritten expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr iterate(final CompileContext cc) throws QueryException {
    // a single recursion step in one of the branches
    final If iff = branches(step, cc);
    if(iff == null) return null;
    final boolean rec0 = iff.exprs[0] instanceof Recur, rec1 = iff.exprs[1] instanceof Recur;
    if(rec0 == rec1) return null;
    final Recur recur = (Recur) iff.exprs[rec0 ? 0 : 1];
    final Expr base = iff.exprs[rec0 ? 1 : 0];

    // a single variable is changed, the others are passed on unchanged
    final int vl = vars.length;
    int c = -1;
    for(int v = 0; v < vl; v++) {
      if(!refers(recur.exprs[v + 1], vars[v])) {
        if(c != -1) return null;
        c = v;
      }
    }
    if(c == -1) return null;
    final Expr arg = recur.exprs[c + 1];
    final boolean walk = Function.TAIL.is(arg) && refers(arg.arg(0), vars[c]);
    if(!walk && delta(arg, vars[c]) == 0) return null;

    // results after the call are evaluated in reverse order
    final Expr after = recur.after();
    if(base.uses(flag) || after != null && after.has(Flag.NDT, Flag.CNS)) return null;

    // variable is only registered if the rewrite succeeds
    final Var start = new Var(vars[c].name, null, cc.qc, info);
    final Expr result = walk ? walk(iff.cond, rec0, recur, base, c, start, cc) :
      count(iff.cond, rec0, recur, base, c, start, cc);
    if(result == null) return null;

    // let $v := INIT, let $start := INIT
    final LinkedList<Clause> clauses = new LinkedList<>();
    for(int v = 0; v < vl; v++) {
      if(v != c) clauses.add(new Let(vars[v], exprs[v]).optimize(cc));
    }
    clauses.add(new Let(cc.vs().add(start), exprs[c]).optimize(cc));
    return new GFLWOR(info, clauses, result).optimize(cc);
  }

  /**
   * Returns a step as conditional expression, with enclosing let clauses and pipelines moved into
   * its branches (the result is a template whose operands must only be copied).
   * @param expr step
   * @param cc compilation context
   * @return conditional expression (can be {@code null})
   * @throws QueryException query exception
   */
  private static If branches(final Expr expr, final CompileContext cc) throws QueryException {
    return expr instanceof final If iff ? iff :
      expr instanceof final GFLWOR gflwor ? gflwor.branches(cc) :
      expr instanceof final Pipeline pipeline ? pipeline.branches(cc) : null;
  }

  /**
   * Moves an enclosing expression into the branches of a conditional step.
   * @param body body of the enclosing expression
   * @param bound checks if an expression depends on the enclosing expression
   * @param wrap wraps an expression with the enclosing expression (without optimizing it)
   * @param cc compilation context
   * @return conditional expression (can be {@code null})
   * @throws QueryException query exception
   */
  public static If distribute(final Expr body, final Predicate<Expr> bound,
      final QueryFunction<Expr, Expr> wrap, final CompileContext cc) throws QueryException {
    final If iff = branches(body, cc);
    if(iff == null || bound.test(iff.cond)) return null;
    final Expr[] branches = new Expr[2];
    for(int b = 0; b < 2; b++) {
      final Expr branch = iff.exprs[b];
      branches[b] = branch instanceof final Recur recur ? recur.absorb(bound, wrap, cc) :
        wrap.apply(branch);
      if(branches[b] == null) return null;
    }
    // let $x := E return if(C) then recur(P, A) else B
    // → if(C) then recur(let $x := E return P, A) else let $x := E return B
    return new If(iff.info, iff.cond, branches[0], branches[1]);
  }

  /**
   * Rewrites a loop over the items of a sequence.
   * @param cond condition
   * @param rec0 indicates if the recursion step is found in the first branch
   * @param recur recursion step
   * @param base base expression
   * @param c index of the changed variable
   * @param start variable bound to the initial sequence
   * @param cc compilation context
   * @return rewritten expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr walk(final Expr cond, final boolean rec0, final Recur recur, final Expr base,
      final int c, final Var start, final CompileContext cc) throws QueryException {
    final Var var = vars[c];
    final Function func = rec0 ? Function.EXISTS : Function.EMPTY;
    if(!refers(cond.arg(0), var) || !func.is(cond)) return null;

    // for $i at $p in $s let $s := ... return P, let $s := () return E, reverse loop for S
    final ExprList results = new ExprList(3).add(items(recur.exprs[0], var, start, false, cc));
    results.add(loop(base, var, s -> new Clause[] { new Let(s, Empty.VALUE).optimize(cc) }, cc));
    final Expr after = recur.after();
    if(after != null) results.add(items(after, var, start, true, cc));
    // if(exists($s)) then (P($s), recur(tail($s))) else E($s)
    // → (for $i at $p in $s let $s := ($i, subsequence($s, $p + 1)) return P($s)), E(())
    return List.get(cc, info, results.finish());
  }

  /**
   * Creates a loop that binds the remaining items of a sequence in each step.
   * @param expr loop body
   * @param var changed variable
   * @param start variable bound to the initial sequence
   * @param reverse iterate in reverse order
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  private Expr items(final Expr expr, final Var var, final Var start, final boolean reverse,
      final CompileContext cc) throws QueryException {
    final Var item = cc.vs().addNew(new QNm("item"), null, cc.qc, info);
    final Var pos = cc.vs().addNew(new QNm("pos"), null, cc.qc, info);
    Expr seq = ref(start, 0, cc), next = ref(pos, 1, cc);
    if(reverse) {
      seq = cc.function(Function.REVERSE, info, seq);
      next = new Arith(info, cc.function(Function.COUNT, info, ref(start, 0, cc)),
          ref(pos, -2, cc), Calc.SUBTRACT).optimize(cc);
    }
    // the first item is bound separately: head($s) can be rewritten to $i
    final Expr rest = List.get(cc, info, ref(item, 0, cc),
        cc.function(Function.SUBSEQUENCE, info, ref(start, 0, cc), next));
    final Clause fr = new For(item, pos, null, seq, false).optimize(cc);
    // for $i at $p in $start let $s := ($i, subsequence($start, $p + 1)) return E
    return loop(expr, var, s -> new Clause[] { fr, new Let(s, rest).optimize(cc) }, cc);
  }

  /**
   * Rewrites a loop with an integer counter.
   * @param cond condition
   * @param rec0 indicates if the recursion step is found in the first branch
   * @param recur recursion step
   * @param base base expression
   * @param c index of the changed variable
   * @param start variable bound to the initial value
   * @param cc compilation context
   * @return rewritten expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr count(final Expr cond, final boolean rec0, final Recur recur, final Expr base,
      final int c, final Var start, final CompileContext cc) throws QueryException {

    // counter: integer, incremented or decremented by one
    final Var counter = vars[c];
    if(!exprs[c].seqType().instanceOf(Types.INTEGER_O)) return null;
    final long delta = delta(recur.exprs[c + 1], counter);

    // condition: comparison of the counter with a loop-invariant integer bound
    Expr bound = null;
    CmpOp op = null;
    if(cond instanceof final Cmp cmp) {
      for(int o = 0; o < 2 && bound == null; o++) {
        if(refers(cmp.exprs[o], counter)) {
          bound = cmp.exprs[1 - o];
          op = o == 0 ? cmp.cmpOp() : cmp.cmpOp().swap();
        }
      }
    } else if(cond instanceof final CmpIR cmp && refers(cmp.expr, counter)) {
      if(cmp.min == Long.MIN_VALUE) {
        bound = Itr.get(cmp.max);
        op = CmpOp.LE;
      } else if(cmp.max == Long.MAX_VALUE) {
        bound = Itr.get(cmp.min);
        op = CmpOp.GE;
      }
    }
    if(bound == null || bound.has(Flag.NDT) || bound.uses(counter)) return null;
    // unchanged variable: type of the initial value
    final int b = bound instanceof final VarRef ref ? Checks.index(vars, v -> v == ref.var) : -1;
    if(!(b != -1 ? exprs[b] : bound).seqType().instanceOf(Types.INTEGER_O)) return null;
    // continue while $n < B, $n <= B (counting up), $n > B, $n >= B (counting down)
    final boolean up = delta > 0;
    if(!rec0) op = op.invert();
    if(op == CmpOp.NE) op = up ? CmpOp.LT : CmpOp.GT;
    final boolean incl = op == (up ? CmpOp.LE : CmpOp.GE);
    if(!incl && op != (up ? CmpOp.LT : CmpOp.GT)) return null;

    // final value: max(($start, $end)), min(($start, $end - 1)), ...
    final Var end = cc.vs().addNew(new QNm("end"), null, cc.qc, info);
    final Expr fnl = cc.function(up ? Function.MAX : Function.MIN, info, List.get(cc, info,
        ref(start, 0, cc), ref(end, incl ? delta : 0, cc)));
    // if the base expression equals the step results, the final value is included in the range
    final Expr prefix = recur.exprs[0];
    final boolean same = prefix.equals(base);

    // for $n in RANGE return P, let $n := FINAL return E, for $n in reverse(RANGE) return S
    final ExprList results = new ExprList(3);
    final long last = incl ? 0 : -delta;
    final Expr range = range(start, same ? fnl : ref(end, last, cc), up, cc);
    results.add(loop(prefix, counter, n -> new Clause[] { new For(n, range).optimize(cc) }, cc));
    if(!same) {
      results.add(loop(base, counter, n -> new Clause[] { new Let(n, fnl).optimize(cc) }, cc));
    }
    final Expr after = recur.after();
    if(after != null) {
      final Expr reverse = cc.function(Function.REVERSE, info,
          range(start, ref(end, last, cc), up, cc));
      results.add(loop(after, counter, n -> new Clause[] { new For(n, reverse).optimize(cc) },
          cc));
    }
    // if($n < B) then (P($n), recur($n + 1)) else E($n)
    // → let $end := B return (for $n in $start to $end - 1 return P($n)), E(max(($start, $end)))
    return new GFLWOR(info, new Let(end, bound).optimize(cc),
        List.get(cc, info, results.finish())).optimize(cc);
  }

  /**
   * Checks if an expression is a reference to the specified variable.
   * @param expr expression
   * @param var variable
   * @return result of check
   */
  private static boolean refers(final Expr expr, final Var var) {
    return expr instanceof final VarRef ref && ref.var == var;
  }

  /**
   * Creates a reference to a variable, optionally with an added constant.
   * @param var variable
   * @param add constant to add
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  private Expr ref(final Var var, final long add, final CompileContext cc)
      throws QueryException {
    final Expr ref = new VarRef(info, var).optimize(cc);
    return add == 0 ? ref : new Arith(info, ref, Itr.get(add), Calc.ADD).optimize(cc);
  }

  /**
   * Creates the range of the counter values.
   * @param start variable with the initial value
   * @param last last value
   * @param up counting up or down
   * @param cc compilation context
   * @return range: {@code $start to LAST}, {@code reverse(LAST to $start)}
   * @throws QueryException query exception
   */
  private Expr range(final Var start, final Expr last, final boolean up, final CompileContext cc)
      throws QueryException {
    final Expr first = ref(start, 0, cc);
    return up ? new Range(info, first, last).optimize(cc) :
      cc.function(Function.REVERSE, info, new Range(info, last, first).optimize(cc));
  }

  /**
   * Creates a FLWOR expression for a copy of the results of the loop body.
   * @param expr results
   * @param var changed variable
   * @param clauses creates the clauses that bind the copy of the changed variable
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  private Expr loop(final Expr expr, final Var var, final QueryFunction<Var, Clause[]> clauses,
      final CompileContext cc) throws QueryException {
    final IntObjectMap<Var> vm = new IntObjectMap<>();
    final Clause[] cls = clauses.apply(cc.copy(var, vm));
    return new GFLWOR(info, new LinkedList<>(Arrays.asList(cls)), expr.copy(cc, vm)).optimize(cc);
  }

  /**
   * Returns the constant by which a variable is incremented.
   * @param expr expression
   * @param var variable
   * @return {@code 1}, {@code -1}, or {@code 0} if the expression has another shape
   */
  private static long delta(final Expr expr, final Var var) {
    if(expr instanceof final Arith arith && (arith.calc == Calc.ADD ||
        arith.calc == Calc.SUBTRACT)) {
      final Expr op1 = arith.exprs[0], op2 = arith.exprs[1];
      final boolean ref1 = refers(op1, var), ref2 = arith.calc == Calc.ADD && refers(op2, var);
      if(ref1 != ref2 && (ref1 ? op2 : op1) instanceof final Itr itr) {
        final long l = arith.calc == Calc.ADD ? itr.itr() : -itr.itr();
        if(l == 1 || l == -1) return l;
      }
    }
    return 0;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    if(suffix != null) return value(qc).iter();

    bind(vars, exprs, 0, qc);
    return new Iter() {
      /** Results of the current step. */
      private Value value = Empty.VALUE;
      /** Position of the next result. */
      private long pos;
      /** Indicates if the last step has been evaluated. */
      private boolean done;

      @Override
      public Item next() throws QueryException {
        while(pos == value.size()) {
          if(done) return null;
          start(qc);
          value = step.value(qc);
          pos = 0;
          done = !stepped(qc);
        }
        return value.itemAt(pos++);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final ValueBuilder sb = suffix != null ? new ValueBuilder(qc) : null;
    bind(vars, exprs, 0, qc);
    while(true) {
      start(qc);
      vb.add(step.value(qc));
      if(!stepped(qc)) break;
      // suffixes are emitted in reverse order: reverse each, then the whole sequence
      if(sb != null) sb.add(qc.get(suffix).reverse(qc));
    }
    if(sb != null) vb.add(sb.value(this).reverse(qc));
    return vb.value(this);
  }

  /**
   * Evaluates expressions and binds the results to variables.
   * @param vars variables
   * @param exprs expressions
   * @param offset offset of the first expression
   * @param qc query context
   * @throws QueryException query exception
   */
  static void bind(final Var[] vars, final Expr[] exprs, final int offset, final QueryContext qc)
      throws QueryException {
    final int vl = vars.length;
    if(vl == 1) {
      qc.set(vars[0], exprs[offset].value(qc));
    } else {
      final Value[] values = new Value[vl];
      for(int v = 0; v < vl; v++) values[v] = exprs[offset + v].value(qc);
      for(int v = 0; v < vl; v++) qc.set(vars[v], values[v]);
    }
  }

  /**
   * Prepares the evaluation of the next step.
   * @param qc query context
   * @throws QueryException query exception
   */
  private void start(final QueryContext qc) throws QueryException {
    qc.checkStop();
    qc.set(flag, Bln.FALSE);
    if(suffix != null) qc.set(suffix, Empty.VALUE);
  }

  /**
   * Checks if the last step was a recursion step.
   * @param qc query context
   * @return result of check
   */
  private boolean stepped(final QueryContext qc) {
    return qc.get(flag) == Bln.TRUE;
  }

  @Override
  public boolean eager() {
    return suffix != null;
  }

  @Override
  public boolean has(final Flag... flags) {
    return super.has(flags) || step.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return super.inlineable(ic) && step.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return super.count(var).plus(step.count(var).times(2));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final Expr[] st = { step };
    final boolean init = ic.inline(exprs), body = ic.inline(st, true);
    step = st[0];
    return init || body ? optimize(ic.cc) : null;
  }

  @Override
  public Unfold copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Expr[] init = copyAll(cc, vm, exprs);
    final int vl = vars.length;
    final Var[] vrs = new Var[vl];
    for(int v = 0; v < vl; v++) vrs[v] = cc.copy(vars[v], vm);
    return copyType(new Unfold(info, init, vrs, cc.copy(flag, vm), cc.copy(suffix, vm),
        step.copy(cc, vm)));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitor.declared(vars) && visitor.declared(flag) &&
        (suffix == null || visitor.declared(suffix)) && step.accept(visitor);
  }

  @Override
  public int exprSize() {
    return super.exprSize() + step.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Unfold unfold && Arrays.equals(vars, unfold.vars) &&
        flag.equals(unfold.flag) && Objects.equals(suffix, unfold.suffix) &&
        step.equals(unfold.step) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, "suffix", suffix != null ? true : null), vars, exprs, step);
  }

  @Override
  public void toString(final QueryString qs) {
    // self-applied closure: equivalent to the original recursive function
    final int vl = vars.length;
    final Var[] params = new Var[vl + 1];
    params[0] = flag;
    System.arraycopy(vars, 0, params, 1, vl);
    qs.open().token(QueryText.LET).token(flag).token(":=").token(QueryText.FN).params(params);
    qs.brace(step).token(QueryText.RETURN);
    Recur.call(qs, info, flag, exprs);
    qs.close();
  }
}
