package org.basex.query.expr.gflwor;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General FLWOR expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class GFLWOR extends ParseExpr {
  /** FLWOR clauses. */
  private final LinkedList<Clause> clauses;
  /** Return expression. */
  public Expr ret;

  /**
   * Constructor.
   * @param info input info
   * @param clauses FLWOR clauses
   * @param ret return expression
   */
  public GFLWOR(final InputInfo info, final LinkedList<Clause> clauses, final Expr ret) {
    super(info);
    this.clauses = clauses;
    this.ret = ret;
  }

  @Override
  public Iter iter(final QueryContext qc) {
    // Start evaluator, doing nothing, once.
    Eval e = new Eval() {
      /** First-evaluation flag. */
      private boolean first = true;
      @Override
      public boolean next(final QueryContext q) {
        if(!first) return false;
        first = false;
        return true;
      }
    };

    for(final Clause cls : clauses) e = cls.eval(e);
    final Eval ev = e;

    return new Iter() {
      /** Return iterator. */
      private Iter sub = Empty.ITER;
      /** If the iterator has been emptied. */
      private boolean drained;
      @Override
      public Item next() throws QueryException {
        if(drained) return null;
        while(true) {
          final Item it = sub.next();
          qc.checkStop();
          if(it != null) return it;
          if(!ev.next(qc)) {
            drained = true;
            return null;
          }
          sub = ret.iter(qc);
        }
      }
    };
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    int i = 0;
    try {
      for(final Clause clause : clauses) {
        clause.compile(qc, scp);
        i++;
      }
      ret = ret.compile(qc, scp);
    } catch(final QueryException qe) {
      clauseError(qe, i);
    }
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // split combined where clauses
    final ListIterator<Clause> iter = clauses.listIterator();
    while(iter.hasNext()) {
      final Clause clause = iter.next();
      if(clause instanceof Where) {
        final Where wh = (Where) clause;
        if(wh.expr instanceof And) {
          iter.remove();
          for(final Expr e : ((Arr) wh.expr).exprs) iter.add(new Where(e, wh.info));
        }
      }
    }

    // the other optimizations are applied until nothing changes anymore
    boolean changed;
    do {
      // rewrite singleton for clauses to let
      changed = forToLet(qc);

      // remove unused variables
      changed |= removeVars(qc);

      // slide let clauses out to avoid repeated evaluation
      changed |= slideLetsOut(qc);

      // inline let expressions if they are used only once (and not in a loop)
      changed |= inlineLets(qc, scp);

      // clean unused variables from group-by and order-by expression
      changed |= cleanDeadVars(qc);

      // include the clauses of nested FLWR expressions into this one
      changed |= unnestFLWR(qc, scp);

      // float where expressions upwards to filter earlier
      changed |= optimizeWhere(qc, scp);

      // rewrite positional variables to predicates
      changed |= optimizePos(qc, scp);

      // remove FLWOR expressions when all clauses were removed
      if(clauses.isEmpty()) {
        qc.compInfo(QueryText.OPTFLWOR, this);
        return ret;
      }

      if(clauses.getLast() instanceof For && ret instanceof VarRef) {
        final For last = (For) clauses.getLast();
        // for $x in E return $x  ==>  return E
        if(!last.var.checksType() && last.var.is(((VarRef) ret).var)) {
          clauses.removeLast();
          ret = last.expr;
          changed = true;
        }
      }

      if(!clauses.isEmpty() && clauses.getFirst() instanceof For) {
        final For fst = (For) clauses.getFirst();
        if(!fst.empty) {
          if(fst.expr instanceof GFLWOR) {
            qc.compInfo(QueryText.OPTFLAT, fst);
            final GFLWOR sub = (GFLWOR) fst.expr;
            clauses.set(0, new For(fst.var, null, fst.score, sub.ret, false, fst.info));
            if(fst.pos != null) clauses.add(1, new Count(fst.pos, fst.info));
            clauses.addAll(0, sub.clauses);
            changed = true;
          } else if(clauses.size() > 1 && clauses.get(1) instanceof Count) {
            final Count cnt = (Count) clauses.get(1);
            if(fst.pos != null) {
              final Let lt = new Let(cnt.var,
                  new VarRef(cnt.info, fst.pos).optimize(qc, scp), false, cnt.info);
              clauses.set(1, lt.optimize(qc, scp));
            } else {
              clauses.set(0, new For(fst.var, cnt.var, fst.score,
                  fst.expr, false, fst.info).optimize(qc, scp));
              clauses.remove(1);
            }
            changed = true;
          }
        }
      }

      if(!clauses.isEmpty()) {
        if(ret instanceof GFLWOR) {
          final GFLWOR sub = (GFLWOR) ret;
          if(sub.isFLWR()) {
            // flatten nested FLWOR expressions
            qc.compInfo(QueryText.OPTFLAT, this);
            clauses.addAll(sub.clauses);
            ret = sub.ret;
            changed = true;
          }
        }

        final TypeCheck tc = ret instanceof TypeCheck ? (TypeCheck) ret : null;
        if(ret instanceof GFLWOR || tc != null && tc.expr instanceof GFLWOR) {
          final GFLWOR sub = (GFLWOR) (tc == null ? ret : tc.expr);
          if(sub.clauses.getFirst() instanceof Let) {
            qc.compInfo(QueryText.OPTFLAT, this);
            final LinkedList<Clause> cls = sub.clauses;
            // propagate all leading let bindings into outer clauses
            do {
              clauses.add(cls.removeFirst());
            } while(!cls.isEmpty() && cls.getFirst() instanceof Let);
            if(tc != null) tc.expr = sub.optimize(qc, scp);
            ret = ret.optimize(qc, scp);
            changed = true;
          }
        }
      }

      /*
       * [LW] not safe:
       * for $x in 1 to 4 return
       *   for $y in 1 to 4 count $index return $index
       * */
    } while(changed);

    mergeWheres();

    size = calcSize();
    if(size == 0 && !has(Flag.NDT) && !has(Flag.UPD)) {
      qc.compInfo(QueryText.OPTWRITE, this);
      return Empty.SEQ;
    }

    seqType = SeqType.get(ret.seqType().type, size);

    if(clauses.getFirst() instanceof Where) {
      // where A <...> return B  ===>  if(A) then <...> return B else ()
      final Where wh = (Where) clauses.removeFirst();
      return new If(info, wh.expr, clauses.isEmpty() ? ret : this, Empty.SEQ);
    }

    return this;
  }

  /**
   * Pre-calculates the number of results of this FLWOR expression.
   * @return result size if statically computable, {@code -1} otherwise
   */
  private long calcSize() {
    final long output = ret.size();
    if(output == 0) return 0;

    final long[] minMax = { 1, 1 };
    for(final Clause clause : clauses) {
      clause.calcSize(minMax);
      if(minMax[1] == 0) break;
    }
    return output >= 0 && minMax[1] >= 0 && minMax[0] == minMax[1] ? minMax[1] * output : -1;
  }

  /**
   * Tries to convert for clauses that iterate ver a single item into let bindings.
   * @param qc query context
   * @return change flag
   */
  private boolean forToLet(final QueryContext qc) {
    boolean changed = false;
    for(int i = clauses.size(); --i >= 0;) {
      final Clause clause = clauses.get(i);
      if(clause instanceof For && ((For) clause).asLet(clauses, i)) {
        qc.compInfo(QueryText.OPTFORTOLET);
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Remove unused variables.
   * @param qc query context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean removeVars(final QueryContext qc) throws QueryException {
    boolean changed = false;
    for(int c = 0; c < clauses.size(); c++) {
      final Clause clause = clauses.get(c);
      if(clause instanceof Let) {
        final Let lt = (Let) clause;
        if(removingVar(lt.var, c + 1, qc) && !lt.has(Flag.NDT) && !lt.has(Flag.UPD)) {
          // check type before removing variable (see {@link FuncType#funcConv})
          lt.var.checkType(lt.expr, lt.info);
          clauses.remove(c);
          changed = true;
        }
      } else if(clause instanceof For) {
        final For f = (For) clause;
        if(removingVar(f.score, c, qc)) {
          f.score = null;
          changed = true;
        }
        if(removingVar(f.pos, c, qc)) {
          f.pos = null;
          changed = true;
        }
      }
    }
    return changed;
  }

  /**
   * Remove unused variables.
   * Counts the number of usages of the given variable starting from the given clause.
   * @param var variable (may be {@code null})
   * @param index start position
   * @param qc query context
   * @return result of check
   */
  private boolean removingVar(final Var var, final int index, final QueryContext qc) {
    if(var != null && count(var, index) == VarUsage.NEVER) {
      qc.compInfo(QueryText.OPTVAR, var);
      return true;
    }
    return false;
  }

  /**
   * Inline let expressions if they are used only once (and not in a loop).
   * @param qc query context
   * @param scp variable scope
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean inlineLets(final QueryContext qc, final VarScope scp) throws QueryException {
    boolean changed = false, changing;
    do {
      changing = false;
      final ListIterator<Clause> iter = clauses.listIterator();
      while(iter.hasNext()) {
        final Clause clause = iter.next();
        final int next = iter.nextIndex();
        if(clause instanceof Let) {
          final Let lt = (Let) clause;
          final Expr expr = lt.expr;
          if(expr.has(Flag.NDT) || expr.has(Flag.UPD)) continue;

          if(
            // inline simple values
            expr.isValue()
            // inline variable references without type checks
            || expr instanceof VarRef && !lt.var.checksType()
            // inline expressions that occur once, but don't...
            // - access context (e.g. let $x:=. return <a/>[$x=1]), or
            // - construct nodes (e.g. let $x:=<X/> return <X xmlns='xx'>{$x/self::X}</X>)
            || count(lt.var, next) == VarUsage.ONCE && !expr.has(Flag.CTX) && !expr.has(Flag.CNS)
            // inline only cheap axis paths
            || expr instanceof AxisPath && ((AxisPath) expr).cheap()) {

            qc.compInfo(QueryText.OPTINLINE, lt.var);
            inline(qc, scp, lt.var, lt.inlineExpr(qc, scp), next);
            iter.remove();
            changing = changed = true;
            // continue from the beginning as clauses below could have been deleted
            break;
          }
        }
      }
    } while(changing);
    return changed;
  }

  /**
   * Flattens FLWR expressions in for or let clauses by including their clauses in this expression.
   * @param qc query context
   * @param scp variable scope
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean unnestFLWR(final QueryContext qc, final VarScope scp) throws QueryException {
    boolean changed = false, thisRound;
    do {
      thisRound = false;
      final ListIterator<Clause> iter = clauses.listIterator();
      while(iter.hasNext()) {
        final Clause clause = iter.next();
        final boolean isFor = clause instanceof For, isLet = clause instanceof Let;
        if(isFor) {
          // for $x in (for $y in A (...) return B)  ===>  for $y in A (...) for $x in B
          final For fr = (For) clause;
          if(!fr.empty && fr.pos == null && fr.expr instanceof GFLWOR) {
            final GFLWOR fl = (GFLWOR) fr.expr;
            if(fl.isFLWR()) {
              qc.compInfo(QueryText.OPTFLAT, this);
              iter.remove();
              for(final Clause cls : fl.clauses) iter.add(cls);
              fr.expr = fl.ret;
              iter.add(fr);
              thisRound = changed = true;
            }
          }
        }

        if(!thisRound && (isFor || isLet)) {
          // let $x := (let $y := E return F)  ===>  let $y := E let $x := F
          final Expr e = isFor ? ((For) clause).expr : ((Let) clause).expr;
          if(e instanceof GFLWOR) {
            final GFLWOR fl = (GFLWOR) e;
            final LinkedList<Clause> cls = fl.clauses;
            if(cls.getFirst() instanceof Let) {
              // remove the binding from the outer clauses
              iter.remove();

              // propagate all leading let bindings into outer clauses
              do iter.add(cls.removeFirst());
              while(!cls.isEmpty() && cls.getFirst() instanceof Let);

              // re-add the binding with new, reduced expression at the end
              final Expr rest = fl.clauses.isEmpty() ? fl.ret : fl.optimize(qc, scp);
              if(isFor) ((For) clause).expr = rest;
              else ((Let) clause).expr = rest;
              iter.add(clause);
              thisRound = changed = true;
            }
          }
        }
      }
    } while(thisRound);
    return changed;
  }

  /**
   * Cleans dead entries from the tuples that {@link GroupBy} and {@link OrderBy} handle.
   * @param qc query context
   * @return change flag
   */
  private boolean cleanDeadVars(final QueryContext qc) {
    final IntObjMap<Var> decl = new IntObjMap<>();
    final BitArray used = new BitArray();

    for(final Clause clause : clauses) for(final Var v : clause.vars()) decl.put(v.id, v);
    final ASTVisitor marker = new ASTVisitor() {
      @Override
      public boolean used(final VarRef ref) {
        final int id = ref.var.id;
        if(decl.get(id) != null) used.set(id);
        return true;
      }
    };

    ret.accept(marker);
    boolean changed = false;
    for(int c = clauses.size(); --c >= 0;) {
      final Clause clause = clauses.get(c);
      changed |= clause.clean(qc, decl, used);
      clause.accept(marker);
      for(final Var v : clause.vars()) used.clear(v.id);
    }
    return changed;
  }

  /**
   * Optimization pass which tries to slide let expressions out of loops. Care is taken
   * that no unnecessary relocations are done.
   * @param qc query context
   * @return {@code true} if there were relocations, {@code false} otherwise
   */
  private boolean slideLetsOut(final QueryContext qc) {
    boolean changed = false;
    for(int i = 1; i < clauses.size(); i++) {
      final Clause l = clauses.get(i);
      if(!(l instanceof Let) || l.has(Flag.NDT) || l.has(Flag.CNS) || l.has(Flag.UPD)) continue;
      final Let let = (Let) l;

      // find insertion position
      int insert = -1;
      for(int j = i; --j >= 0;) {
        final Clause curr = clauses.get(j);
        if(!curr.skippable(let)) break;
        // insert directly above the highest skippable for or window clause
        // this guarantees that no unnecessary swaps occur
        if(curr instanceof For || curr instanceof Window) insert = j;
      }

      if(insert >= 0) {
        clauses.add(insert, clauses.remove(i));
        if(!changed) qc.compInfo(QueryText.OPTFORLET);
        changed = true;
        // it's safe to go on because clauses below the current one are never touched
      }
    }
    return changed;
  }

  /**
   * Slides where clauses upwards and removes those that do not filter anything.
   * @param qc query context
   * @param scp variable scope
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean optimizeWhere(final QueryContext qc, final VarScope scp) throws QueryException {
    boolean changed = false;
    for(int i = 0; i < clauses.size(); i++) {
      final Clause clause = clauses.get(i);
      if(!(clause instanceof Where) || clause.has(Flag.NDT) || clause.has(Flag.UPD)) continue;
      final Where wh = (Where) clause;

      if(wh.expr.isValue()) {
        if(!(wh.expr instanceof Bln))
          wh.expr = Bln.get(wh.expr.ebv(qc, wh.info).bool(wh.info));

        // predicate is always false: no results possible
        if(!((Item) wh.expr).bool(null)) break;

        // condition is always true
        clauses.remove(i--);
        changed = true;
      } else {
        // find insertion position
        int insert = -1;
        for(int j = i; --j >= 0;) {
          final Clause curr = clauses.get(j);
          if(curr.has(Flag.NDT) || curr.has(Flag.UPD) || !curr.skippable(wh)) break;
          // where clauses are always moved to avoid unnecessary computations,
          // but skipping only other where clauses can cause infinite loops
          if(!(curr instanceof Where)) insert = j;
        }

        if(insert >= 0) {
          clauses.add(insert, clauses.remove(i));
          changed = true;
          // it's safe to go on because clauses below the current one are never touched
        }

        final int newPos = insert < 0 ? i : insert;
        for(int b4 = newPos; --b4 >= 0;) {
          final Clause before = clauses.get(b4);
          if(before instanceof For && ((For) before).toPredicate(qc, scp, wh.expr)) {
            clauses.remove(newPos);
            i--;
            changed = true;
          } else if(before instanceof Where) {
            continue;
          }
          break;
        }
      }
    }
    if(changed) qc.compInfo(QueryText.OPTWHERE2);
    return changed;
  }

  /**
   * Rewrite positional variables to predicates.
   * @param qc query context
   * @param scp variable scope
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean optimizePos(final QueryContext qc, final VarScope scp) throws QueryException {
    boolean changed = false;
    for(int c = 0; c < clauses.size(); c++) {
      final Clause clause = clauses.get(c);
      if(!(clause instanceof For)) continue;

      final For pos = (For) clause;
      if(pos.pos == null) continue;

      // find where clause ($c = 1)
      for(int i = c + 1; i < clauses.size(); i++) {
        final Clause cl = clauses.get(i);
        if(!(cl instanceof Where)) {
          // skip group by, count, ...
          if(!(cl instanceof For || cl instanceof Let) || cl.has(Flag.NDT) || cl.has(Flag.UPD))
            break;
          continue;
        }
        final Where w = (Where) cl;
        if(!(w.expr instanceof CmpR)) continue;
        final CmpR cmp = (CmpR) w.expr;
        if(!(cmp.expr instanceof VarRef)) continue;

        // remove clause and ensure that the positional variable is only used once
        clauses.remove(i);
        if(count(pos.pos, c) == VarUsage.NEVER) {
          pos.addPredicate(qc, scp, Pos.get(cmp));
          qc.compInfo(QueryText.OPTPRED, cmp);
          changed = true;
        } else {
          // re-add variable, give up
          clauses.add(i, cl);
        }
        break;
      }
    }
    return changed;
  }




  /** Merges consecutive {@code where} clauses. */
  private void mergeWheres() {
    Where before = null;
    final Iterator<Clause> iter = clauses.iterator();
    while(iter.hasNext()) {
      final Clause clause = iter.next();
      if(clause instanceof Where) {
        final Where wh = (Where) clause;
        if(wh.expr == Bln.FALSE) return;
        if(before != null) {
          iter.remove();
          final Expr e = before.expr;
          if(e instanceof And) {
            final And and = (And) e;
            and.exprs = Array.add(and.exprs, wh.expr);
          } else {
            before.expr = new And(before.info, e, wh.expr);
          }
        } else {
          before = wh;
        }
      } else {
        before = null;
      }
    }
  }

  @Override
  public boolean isVacuous() {
    return ret.isVacuous();
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Clause clause : clauses) if(clause.has(flag)) return true;
    return ret.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    for(final Clause clause : clauses) if(!clause.removable(var)) return false;
    return ret.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return count(var, 0);
  }

  /**
   * Counts the number of usages of the given variable starting from the given clause.
   * @param var variable
   * @param index start position
   * @return usage count
   */
  private VarUsage count(final Var var, final int index) {
    final long[] minMax = { 1, 1 };
    VarUsage uses = VarUsage.NEVER;
    final ListIterator<Clause> iter = clauses.listIterator(index);
    while(iter.hasNext()) {
      final Clause clause = iter.next();
      uses = uses.plus(clause.count(var).times(minMax[1]));
      clause.calcSize(minMax);
    }
    return uses.plus(ret.count(var).times(minMax[1]));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    return inline(qc, scp, var, ex, 0) ? optimize(qc, scp) : null;
  }

  /**
   * Inlines an expression bound to a given variable, starting at a specified clause.
   * @param qc query context
   * @param scp variable scope
   * @param var variable
   * @param ex expression to inline
   * @param pos clause position
   * @return if changes occurred
   * @throws QueryException query exception
   */
  private boolean inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex,
      final int pos) throws QueryException {

    boolean changed = false;
    final ListIterator<Clause> iter = clauses.listIterator(pos);
    while(iter.hasNext()) {
      final Clause clause = iter.next();
      try {
        final Clause cl = clause.inline(qc, scp, var, ex);
        if(cl != null) {
          changed = true;
          iter.set(cl);
        }
      } catch(final QueryException qe) {
        return clauseError(qe, iter.previousIndex() + 1);
      }
    }

    try {
      final Expr rt = ret.inline(qc, scp, var, ex);
      if(rt != null) {
        changed = true;
        ret = rt;
      }
    } catch(final QueryException qe) {
      return clauseError(qe, clauses.size());
    }

    return changed;
  }

  /**
   * Tries to recover from a compile-time exception inside a FLWOR clause.
   * @param qe thrown exception
   * @param idx index of the throwing clause, size of {@link #clauses} for return clause
   * @return {@code true} if the GFLWOR expression has to stay
   * @throws QueryException query exception if the whole expression fails
   */
  private boolean clauseError(final QueryException qe, final int idx) throws QueryException {
    final ListIterator<Clause> iter = clauses.listIterator(idx);
    while(iter.hasPrevious()) {
      final Clause b4 = iter.previous();
      if(b4 instanceof For || b4 instanceof Window || b4 instanceof Where) {
        iter.next();
        while(iter.hasNext()) {
          iter.next();
          iter.remove();
        }
        ret = FnError.get(qe, ret.seqType());
        return true;
      }
    }
    throw qe;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final LinkedList<Clause> cls = new LinkedList<>();
    for(final Clause clause : clauses) cls.add(clause.copy(qc, scp, vs));
    return copyType(new GFLWOR(info, cls, ret.copy(qc, scp, vs)));
  }

  /**
   * Checks if this FLWOR expression only uses for, let and where clauses.
   * @return result of check
   */
  private boolean isFLWR() {
    for(final Clause clause : clauses)
      if(!(clause instanceof For || clause instanceof Let || clause instanceof Where)) return false;
    return true;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Clause clause : clauses) if(!clause.accept(visitor)) return false;
    return ret.accept(visitor);
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Clause clause : clauses) clause.checkUp();
    ret.checkUp();
  }

  @Override
  public void markTailCalls(final QueryContext qc) {
    final long[] minMax = { 1, 1 };
    for(final Clause clause : clauses) {
      clause.calcSize(minMax);
      if(minMax[1] < 0 || minMax[1] > 1) return;
    }
    ret.markTailCalls(qc);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Clause clause : clauses) sz += clause.exprSize();
    return ret.exprSize() + sz;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final QueryContext qc, final VarScope scp)
      throws QueryException {
    if(tc.seqType().occ != Occ.ZERO_MORE) return null;
    ret = tc.check(ret, qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    for(final Clause clause : clauses) clause.plan(e);
    ret.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Clause clause : clauses) sb.append(clause).append(' ');
    return sb.append(QueryText.RETURN).append(' ').append(ret).toString();
  }

  /**
   * Evaluator for FLWOR clauses.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Leo Woerteler
   */
  interface Eval {
    /**
     * Makes the next evaluation step if available. This method is guaranteed
     * to not be called again if it has once returned {@code false}.
     * @param qc query context
     * @return {@code true} if step was made, {@code false} if no more results exist
     * @throws QueryException evaluation exception
     */
    boolean next(final QueryContext qc) throws QueryException;
  }

  /**
   * A FLWOR clause.
   *
   * @author BaseX Team 2005-14, BSD License
   * @author Leo Woerteler
   */
  public abstract static class Clause extends ParseExpr {
    /** All variables declared in this clause. */
    final Var[] vars;
    /**
     * Constructor.
     * @param info input info
     * @param vars declared variables
     */
    Clause(final InputInfo info, final Var... vars) {
      super(info);
      this.vars = vars;
    }

    /**
     * Cleans unused variables from this clause.
     * @param qc query context
     * @param used list of the IDs of all variables used in the following clauses
     * @param decl variables declared by this FLWOR expression
     * @return {@code true} if something changed, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    boolean clean(final QueryContext qc, final IntObjMap<Var> decl, final BitArray used) {
      return false;
    }

    /**
     * Evaluates the clause.
     * @param sub wrapped evaluator
     * @return evaluator
     */
    abstract Eval eval(final Eval sub);

    @Override
    public abstract Clause compile(QueryContext qc, final VarScope scp) throws QueryException;

    @Override
    public abstract Clause optimize(final QueryContext qc, final VarScope scp)
        throws QueryException;

    @Override
    public abstract Clause inline(QueryContext qc, VarScope scp, Var var, Expr ex)
        throws QueryException;

    @Deprecated
    @Override
    public Iter iter(final QueryContext qc) throws QueryException {
      throw Util.notExpected();
    }

    @Deprecated
    @Override
    public Value value(final QueryContext qc) throws QueryException {
      throw Util.notExpected();
    }

    @Deprecated
    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      throw Util.notExpected();
    }

    @Override
    public abstract Clause copy(QueryContext qc, VarScope scp, IntObjMap<Var> vs);

    /**
     * Checks if the given clause can be slid over this clause.
     * @param cl clause
     * @return result of check
     */
    boolean skippable(final Clause cl) {
      return cl.accept(new ASTVisitor() {
        @Override
        public boolean used(final VarRef ref) {
          for(final Var v : vars) if(v.is(ref.var)) return false;
          return true;
        }
      });
    }

    /**
     * All declared variables of this clause.
     * @return declared variables
     */
    public final Var[] vars() {
      return vars;
    }

    /**
     * Checks if the given variable is declared by this clause.
     * @param var variable
     * @return {code true} if the variable was declared here, {@code false} otherwise
     */
    public final boolean declares(final Var var) {
      for(final Var decl : vars) if(var.is(decl)) return true;
      return false;
    }

    /**
     * Calculates the minimum and maximum number of results.
     * @param minMax minimum and maximum number of incoming tuples
     */
    abstract void calcSize(long[] minMax);
  }
}
