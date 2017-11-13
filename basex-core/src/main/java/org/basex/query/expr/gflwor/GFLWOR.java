package org.basex.query.expr.gflwor;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General FLWOR expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class GFLWOR extends ParseExpr {
  /** FLWOR clauses. */
  public final LinkedList<Clause> clauses;
  /** Return expression. */
  public Expr ret;

  /**
   * Constructor.
   * @param info input info
   * @param clauses FLWOR clauses
   * @param ret return expression
   */
  public GFLWOR(final InputInfo info, final LinkedList<Clause> clauses, final Expr ret) {
    super(info, SeqType.ITEM_ZM);
    this.clauses = clauses;
    this.ret = ret;
  }

  /**
   * Creates a new evaluator for this FLWOR expression.
   * @return the evaluator
   */
  private Eval newEval() {
    Eval e = new StartEval();
    for(final Clause cls : clauses) e = cls.eval(e);
    return e;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item out = null;
    for(final Eval eval = newEval(); eval.next(qc);) {
      final Item it = ret.item(qc, info);
      if(it != null) {
        if(out != null) throw QueryError.SEQFOUND_X.get(info, ValueBuilder.concat(out, it));
        out = it;
      }
    }
    return out;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Eval eval = newEval();
    if(!eval.next(qc)) return Empty.SEQ;
    final Value v1 = qc.value(ret);
    if(!eval.next(qc)) return v1;

    final ValueBuilder vb = new ValueBuilder().add(v1);
    do {
      vb.add(qc.value(ret));
    } while(eval.next(qc));
    return vb.value();
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      private final Eval ev = newEval();
      private Iter sub = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item it = sub.next();
          if(it != null) return it;
          if(!ev.next(qc)) {
            sub = null;
            return null;
          }
          sub = qc.iter(ret);
          qc.checkStop();
        }
      }
    };
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final ListIterator<Clause> iter = clauses.listIterator();
    try {
      while(iter.hasNext()) iter.next().compile(cc);
    } catch(final QueryException qe) {
      iter.remove();
      clauseError(qe, iter, cc);
    }
    try {
      ret = ret.compile(cc);
    } catch(final QueryException qe) {
      clauseError(qe, iter, cc);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
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
      changed = forToLet(cc);
      // slide let clauses out to avoid repeated evaluation
      changed |= slideLetsOut(cc);
      // inline let expressions if they are used only once (and not in a loop)
      changed |= inlineLets(cc);
      // remove unused variables
      changed |= removeVars(cc);
      // clean unused variables from group-by and order-by expression
      changed |= cleanDeadVars();
      // include the clauses of nested FLWR expressions into this one
      changed |= unnestFLWR(cc);
      // float where expressions upwards to filter earlier
      changed |= optimizeWhere(cc);
      // rewrite positional variables to predicates
      changed |= optimizePos(cc);

      // remove FLWOR expressions when all clauses were removed
      if(clauses.isEmpty()) {
        cc.info(QueryText.OPTSIMPLE_X, description());
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
          // flat nested FLWOR expressions
          if(fst.expr instanceof GFLWOR) {
            // example: for $a at $p in for $x in (1 to 2) return $x + 1 return $p
            cc.info(QueryText.OPTFLAT_X_X, description(), fst.var);
            final GFLWOR sub = (GFLWOR) fst.expr;
            clauses.set(0, new For(fst.var, null, fst.score, sub.ret, false));
            if(fst.pos != null) clauses.add(1, new Count(fst.pos));
            clauses.addAll(0, sub.clauses);
            changed = true;
          } else if(clauses.size() > 1 && clauses.get(1) instanceof Count) {
            final Count cnt = (Count) clauses.get(1);
            if(fst.pos != null) {
              final VarRef vr = new VarRef(cnt.info, fst.pos);
              final Let lt = new Let(cnt.var, vr.optimize(cc), false);
              clauses.set(1, lt.optimize(cc));
            } else {
              final For fr = new For(fst.var, cnt.var, fst.score, fst.expr, false);
              clauses.set(0, fr.optimize(cc));
              clauses.remove(1);
            }
            changed = true;
          }
        }
      }

      if(!clauses.isEmpty()) {
        if(ret instanceof GFLWOR) {
          final GFLWOR sub = (GFLWOR) ret;
          if(sub.isFLW()) {
            // flatten nested FLWOR expressions
            // example: for $a in (1 to 2) return let $f := <a>1</a> return $f + 1
            final Clause c = sub.clauses.getFirst();
            final ExprInfo var = c instanceof ForLet ? ((ForLet) c).var : c;
            cc.info(QueryText.OPTFLAT_X_X, description(), var);
            clauses.addAll(sub.clauses);
            ret = sub.ret;
            changed = true;
          }
        }

        final TypeCheck tc = ret instanceof TypeCheck ? (TypeCheck) ret : null;
        if(ret instanceof GFLWOR || tc != null && tc.expr instanceof GFLWOR) {
          final GFLWOR sub = (GFLWOR) (tc == null ? ret : tc.expr);
          final Clause c = sub.clauses.getFirst();
          if(c instanceof Let) {
            // example: ?
            cc.info(QueryText.OPTFLAT_X_X, description(), ((Let) c).var);
            final LinkedList<Clause> cls = sub.clauses;
            // propagate all leading let bindings into outer clauses
            do {
              clauses.add(cls.removeFirst());
            } while(!cls.isEmpty() && cls.getFirst() instanceof Let);
            if(tc != null) tc.expr = sub.optimize(cc);
            ret = ret.optimize(cc);
            changed = true;
          }
        }
      }

      /* [LW] not safe:
       * for $x in 1 to 4 return
       *   for $y in 1 to 4 count $c return $c */
    } while(changed);

    mergeWheres();

    calcSize();
    if(exprType.isEmpty() && !has(Flag.NDT, Flag.UPD)) return cc.emptySeq(this);

    if(clauses.getFirst() instanceof Where) {
      final Where where = (Where) clauses.removeFirst();
      final Expr branch = clauses.isEmpty() ? ret : this;
      return cc.replaceWith(where, new If(info, where.expr, branch, Empty.SEQ).optimize(cc));
    }

    return this;
  }

  /**
   * Computes the number of results of this FLWOR expression.
   */
  private void calcSize() {
    final long s = ret.size();
    final Occ o = ret.seqType().occ;
    final long[] minMax = { s >= 0 ? s : o.min, s >= 0 ? s : o.max > 1 ? -1 : o.max };
    for(final Clause clause : clauses) {
      if(minMax[1] != 0) clause.calcSize(minMax);
    }
    exprType.assign(ret.seqType(), minMax);
  }

  /**
   * Tries to convert for clauses that iterate ver a single item into let bindings.
   * @param cc compilation context
   * @return change flag
   */
  private boolean forToLet(final CompileContext cc) {
    boolean changed = false;
    for(int i = clauses.size(); --i >= 0;) {
      final Clause clause = clauses.get(i);
      if(clause instanceof For && ((For) clause).asLet(clauses, i)) {
        cc.info(QueryText.OPTFORTOLET_X, clause);
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Remove unused variables.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean removeVars(final CompileContext cc) throws QueryException {
    boolean changed = false;
    final ListIterator<Clause> iter = clauses.listIterator();
    while(iter.hasNext()) {
      final int pos = iter.nextIndex();
      final Clause clause = iter.next();
      if(clause instanceof Let) {
        final Let lt = (Let) clause;
        if(count(lt.var, pos + 1) == VarUsage.NEVER && !lt.has(Flag.NDT, Flag.UPD)) {
          cc.info(QueryText.OPTVAR_X, lt.var);
          // check type before removing variable (see {@link FuncType})
          lt.var.checkType(lt.expr);
          iter.remove();
          changed = true;
        }
      } else if(clause instanceof For) {
        final For f = (For) clause;
        if(f.score != null && count(f.score, pos) == VarUsage.NEVER) {
          cc.info(QueryText.OPTVAR_X, f.score);
          f.score = null;
          changed = true;
        }
        if(f.pos != null && count(f.pos, pos) == VarUsage.NEVER) {
          cc.info(QueryText.OPTVAR_X, f.pos);
          f.pos = null;
          changed = true;
        }
      }
    }
    return changed;
  }

  /**
   * Inline let expressions if they are used only once (and not in a loop).
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean inlineLets(final CompileContext cc) throws QueryException {
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
          if(expr.has(Flag.NDT, Flag.UPD)) continue;

          if(
            // inline simple values
            expr.isValue()
            // inline variable references without type checks
            || expr instanceof VarRef && !lt.var.checksType()
            // inline expressions that occur once, but don't...
            // - access context  (e.g. let $x := . return <a/>[$x = 1]), or
            // - construct nodes (e.g. let $x := <X/> return <X xmlns='xx'>{ $x/self::X }</X>)
            || count(lt.var, next) == VarUsage.ONCE && !expr.has(Flag.CTX, Flag.CNS)
            // inline only cheap axis paths
            || expr instanceof AxisPath && ((AxisPath) expr).cheap()) {

            cc.info(QueryText.OPTINLINE_X, lt.var);
            inline(cc, lt.var, lt.inlineExpr(cc), iter);
            clauses.remove(lt);
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
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean unnestFLWR(final CompileContext cc) throws QueryException {
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
            if(fl.isFLW()) {
              cc.info(QueryText.OPTFLAT_X_X, description(), fr.var);
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
          final Expr ex = isFor ? ((For) clause).expr : ((Let) clause).expr;
          if(ex instanceof GFLWOR) {
            final GFLWOR fl = (GFLWOR) ex;
            final LinkedList<Clause> cls = fl.clauses;
            if(cls.getFirst() instanceof Let) {
              // remove the binding from the outer clauses
              iter.remove();

              // propagate all leading let bindings into outer clauses
              do iter.add(cls.removeFirst());
              while(!cls.isEmpty() && cls.getFirst() instanceof Let);

              // re-add the binding with new, reduced expression at the end
              final Expr rest = fl.clauses.isEmpty() ? fl.ret : fl.optimize(cc);
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
   * @return change flag
   */
  private boolean cleanDeadVars() {
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
      changed |= clause.clean(decl, used);
      clause.accept(marker);
      for(final Var v : clause.vars()) used.clear(v.id);
    }
    return changed;
  }

  /**
   * Optimization pass which tries to slide let expressions out of loops. Care is taken
   * that no unnecessary relocations are done.
   * @param cc compilation context
   * @return {@code true} if there were relocations, {@code false} otherwise
   */
  private boolean slideLetsOut(final CompileContext cc) {
    boolean changed = false;
    for(int c = 1; c < clauses.size(); c++) {
      final Clause clause = clauses.get(c);
      // do not move node constructors. example: for $x in 1 to 2 let $a := <x/> return $a
      if(!(clause instanceof Let) || clause.has(Flag.NDT, Flag.CNS, Flag.UPD)) continue;
      final Let let = (Let) clause;

      // find insertion position
      int insert = -1;
      for(int d = c; --d >= 0;) {
        final Clause curr = clauses.get(d);
        if(!curr.skippable(let)) break;
        // insert directly above the highest skippable for or window clause
        // this guarantees that no unnecessary swaps occur
        if(curr instanceof For || curr instanceof Window) insert = d;
      }

      if(insert >= 0) {
        cc.info(QueryText.OPTLET_X, let);
        clauses.add(insert, clauses.remove(c));
        changed = true;
        // it's safe to go on because clauses below the current one are never touched
      }
    }
    return changed;
  }

  /**
   * Slides where clauses upwards and removes those that do not filter anything.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean optimizeWhere(final CompileContext cc) throws QueryException {
    boolean changed = false;
    final HashSet<For> fors = new HashSet<>();
    for(int i = 0; i < clauses.size(); i++) {
      final Clause clause = clauses.get(i);
      if(!(clause instanceof Where) || clause.has(Flag.NDT, Flag.UPD)) continue;
      final Where where = (Where) clause;

      if(where.expr.isValue()) {
        if(!(where.expr instanceof Bln))
          where.expr = Bln.get(where.expr.ebv(cc.qc, where.info).bool(where.info));

        // predicate is always false: no results possible
        if(!((Item) where.expr).bool(null)) break;

        // condition is always true
        clauses.remove(i--);
        changed = true;
      } else {
        // find insertion position
        int insert = -1;
        for(int j = i; --j >= 0;) {
          final Clause curr = clauses.get(j);
          if(curr.has(Flag.NDT, Flag.UPD) || !curr.skippable(where)) break;
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
          if(before instanceof For) {
            final For f = (For) before;
            if(f.toPredicate(cc, where.expr)) {
              fors.add((For) before);
              clauses.remove(newPos);
              i--;
              changed = true;
            }
          } else if(before instanceof Where) {
            continue;
          }
          break;
        }
      }
    }
    // trigger optimizations on rewritten expressions
    for(final For f : fors) f.expr = f.expr.optimize(cc);
    if(changed) cc.info(QueryText.OPTWHERE);
    return changed;
  }

  /**
   * Rewrite positional variables to predicates.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean optimizePos(final CompileContext cc) throws QueryException {
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
          // stop if clause is no for or let expression, is non-deterministic, or updating
          if(!(cl instanceof For || cl instanceof Let) || cl.has(Flag.NDT, Flag.UPD))
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
          pos.addPredicate(ItrPos.get(cmp));
          pos.expr = pos.expr.optimize(cc);
          cc.info(QueryText.OPTPRED_X, cmp);
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
          final Expr ex = before.expr;
          if(ex instanceof And) {
            final And and = (And) ex;
            and.exprs = ExprList.concat(and.exprs, wh.expr);
          } else {
            before.expr = new And(before.info, ex, wh.expr);
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
  public boolean has(final Flag... flags) {
    for(final Clause clause : clauses) if(clause.has(flags)) return true;
    return ret.has(flags);
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
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    return inline(cc, var, ex, clauses.listIterator()) ? optimize(cc) : null;
  }

  /**
   * Inlines an expression bound to a given variable, starting at a specified clause.
   * @param cc compilation context
   * @param var variable
   * @param ex expression to inline
   * @param iter iterator at the position of the first clause to inline into
   * @return if changes occurred
   * @throws QueryException query exception
   */
  private boolean inline(final CompileContext cc, final Var var, final Expr ex,
      final ListIterator<Clause> iter) throws QueryException {

    boolean changed = false;
    while(iter.hasNext()) {
      final Clause clause = iter.next();
      try {
        final Clause cl = clause.inline(var, ex, cc);
        if(cl != null) {
          changed = true;
          iter.set(cl);
        }
      } catch(final QueryException qe) {
        iter.remove();
        return clauseError(qe, iter, cc);
      }
    }

    try {
      final Expr rt = ret.inline(var, ex, cc);
      if(rt != null) {
        changed = true;
        ret = rt;
      }
    } catch(final QueryException qe) {
      return clauseError(qe, iter, cc);
    }

    return changed;
  }

  /**
   * Tries to recover from a compile-time exception inside a FLWOR clause.
   * @param qe thrown exception
   * @param iter iterator positioned where the failing clause was before
   * @param cc compilation context
   * @return {@code true} if the GFLWOR expression has to stay
   * @throws QueryException query exception if the whole expression fails
   */
  private boolean clauseError(final QueryException qe, final ListIterator<Clause> iter,
      final CompileContext cc) throws QueryException {

    // check if an outer clause can prevent the error
    while(iter.hasPrevious()) {
      final Clause b4 = iter.previous();
      if(b4 instanceof For || b4 instanceof Window || b4 instanceof Where) {
        iter.next();
        while(iter.hasNext()) {
          iter.next();
          iter.remove();
        }
        ret = cc.error(qe, ret);
        return true;
      }
    }

    // error will always be thrown
    throw qe;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final LinkedList<Clause> cls = new LinkedList<>();
    for(final Clause clause : clauses) cls.add(clause.copy(cc, vm));
    return copyType(new GFLWOR(info, cls, ret.copy(cc, vm)));
  }

  /**
   * Checks if this FLWOR expression only uses for, let and where clauses.
   * @return result of check
   */
  private boolean isFLW() {
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
  public void markTailCalls(final CompileContext cc) {
    final long[] minMax = { 1, 1 };
    for(final Clause clause : clauses) {
      clause.calcSize(minMax);
      if(minMax[1] < 0 || minMax[1] > 1) return;
    }
    ret.markTailCalls(cc);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Clause clause : clauses) sz += clause.exprSize();
    return ret.exprSize() + sz;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    if(tc.seqType().occ != Occ.ZERO_MORE) return null;
    ret = tc.check(ret, cc);
    return optimize(cc);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof GFLWOR)) return false;
    final GFLWOR g = (GFLWOR) obj;
    return clauses.equals(g.clauses) && ret.equals(g.ret);
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
   * @author BaseX Team 2005-17, BSD License
   * @author Leo Woerteler
   */
  abstract static class Eval {
    /**
     * Makes the next evaluation step if available. This method is guaranteed
     * to not be called again if it has once returned {@code false}.
     * @param qc query context
     * @return {@code true} if step was made, {@code false} if no more results exist
     * @throws QueryException evaluation exception
     */
    abstract boolean next(QueryContext qc) throws QueryException;
  }

  /** Start evaluator, doing nothing, once. */
  private static final class StartEval extends Eval {
    /** First-evaluation flag. */
    private boolean first = true;
    @Override
    public boolean next(final QueryContext q) {
      if(!first) return false;
      first = false;
      return true;
    }
  }

  /**
   * A FLWOR clause.
   *
   * @author BaseX Team 2005-17, BSD License
   * @author Leo Woerteler
   */
  public abstract static class Clause extends ParseExpr {
    /** All variables declared in this clause. */
    final Var[] vars;

    /**
     * Constructor.
     * @param info input info
     * @param seqType sequence type
     * @param vars declared variables
     */
    Clause(final InputInfo info, final SeqType seqType, final Var... vars) {
      super(info, seqType);
      this.vars = vars;
    }

    /**
     * Cleans unused variables from this clause.
     * @param decl variables declared by this FLWOR expression
     * @param used list of the IDs of all variables used in the following clauses
     * @return {@code true} if something changed, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    boolean clean(final IntObjMap<Var> decl, final BitArray used) {
      return false;
    }

    /**
     * Evaluates the clause.
     * @param sub wrapped evaluator
     * @return evaluator
     */
    abstract Eval eval(Eval sub);

    @Override
    public abstract Clause compile(CompileContext cc) throws QueryException;

    @Override
    public abstract Clause optimize(CompileContext cc) throws QueryException;

    @Override
    public abstract Clause inline(Var var, Expr ex, CompileContext cc) throws QueryException;

    @Override
    public abstract Clause copy(CompileContext cc, IntObjMap<Var> vm);

    /**
     * Checks if the given clause (currently: let or where) can be slid over this clause.
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
