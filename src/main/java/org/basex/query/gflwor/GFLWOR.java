package org.basex.query.gflwor;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General FLWOR expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class GFLWOR extends ParseExpr {
  /** Return expression. */
  Expr ret;
  /** FLWOR clauses. */
  private final LinkedList<Clause> clauses;
  /** XQuery 3.0 flag. */
  private boolean xq30;

  /**
   * Constructor.
   * @param ii input info
   * @param cls FLWOR clauses
   * @param rt return expression
   */
  public GFLWOR(final InputInfo ii, final LinkedList<Clause> cls, final Expr rt) {
    super(ii);
    clauses = cls;
    ret = rt;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // Start evaluator, doing nothing, once.
    Eval e = new Eval() {
      /** First-evaluation flag. */
      private boolean first = true;
      @Override
      public boolean next(final QueryContext c) {
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
          if(it != null) return it;
          if(!ev.next(ctx)) {
            drained = true;
            return null;
          }
          sub = ret.iter(ctx);
        }
      }
    };
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    int i = 0;
    InputInfo ii = info;
    try {
      for(final Clause c : clauses) {
        ii = c.info;
        c.compile(ctx, scp);
        // the first round of constant propagation is free
        if(c instanceof Let) ((Let) c).bindConst(ctx);
        i++;
      }
      ii = info;
      ret = ret.compile(ctx, scp);
    } catch(final QueryException qe) {
      clauseError(qe, i, ii);
    }
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // split combined where clauses
    final ListIterator<Clause> iter = clauses.listIterator();
    while(iter.hasNext()) {
      final Clause c = iter.next();
      if(c instanceof Where) {
        final Where wh = (Where) c;
        if(wh.pred instanceof And) {
          iter.remove();
          for(final Expr e : ((And) wh.pred).expr) iter.add(new Where(e, wh.info));
        }
      }
    }

    // the other optimizations are applied until nothing changes any more
    boolean changed;
    do {
      // rewrite singleton for clauses to let
      changed = forToLet(ctx);

      // inline let expressions if they are used only once (and not in a loop)
      changed |= inlineLets(ctx, scp);

      // clean unused variables from group-by and order-by expression
      changed |= cleanDeadVars(ctx);

      // include the clauses of nested FLWR expressions into this one
      changed |= unnestFLWR(ctx, scp);

      // slide let clauses out to avoid repeated evaluation
      changed |= slideLetsOut(ctx);

      // float where expressions upwards to filter earlier
      changed |= optimizeWhere(ctx, scp);

      // remove FLWOR expressions when all clauses were removed
      if(clauses.isEmpty()) {
        ctx.compInfo(QueryText.OPTFLWOR, this);
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
        if(!fst.empty && fst.expr instanceof GFLWOR) {
          ctx.compInfo(QueryText.OPTFLAT, fst);
          final GFLWOR sub = (GFLWOR) fst.expr;
          clauses.set(0, new For(fst.var, null, fst.score, sub.ret, false, fst.info));
          if(fst.pos != null) clauses.add(1, new Count(fst.pos, fst.info));
          clauses.addAll(0, sub.clauses);
          changed = true;
        }
      }

      if(!clauses.isEmpty() && ret instanceof GFLWOR) {
        final GFLWOR sub = (GFLWOR) ret;
        if(sub.isFLWR()) {
          // flatten nested FLWOR expressions
          ctx.compInfo(QueryText.OPTFLAT, this);
          clauses.addAll(sub.clauses);
          ret = sub.ret;
          changed = true;
        } else if(sub.clauses.getFirst() instanceof Let) {
          ctx.compInfo(QueryText.OPTFLAT, this);
          final LinkedList<Clause> cls = sub.clauses;
          // propagate all leading let bindings into outer clauses
          do clauses.add(cls.removeFirst());
          while(!cls.isEmpty() && cls.getFirst() instanceof Let);
          ret = ret.optimize(ctx, scp);
          changed = true;
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
    if(size == 0 && !(has(Flag.NDT) || has(Flag.UPD))) {
      ctx.compInfo(QueryText.OPTWRITE, this);
      return Empty.SEQ;
    }

    type = SeqType.get(ret.type().type, size);

    if(clauses.getFirst() instanceof Where) {
      // where A <...> return B  ===>  if(A) then <...> return B else ()
      final Where wh = (Where) clauses.removeFirst();
      return new If(info, wh.pred, clauses.isEmpty() ? ret : this, Empty.SEQ);
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

    long tuples = 1;
    for(final Clause c : clauses) if((tuples = c.calcSize(tuples)) <= 0) break;
    return tuples == 0 ? 0 : output < 0 || tuples < 0 ? -1 : tuples * output;
  }

  /**
   * Tries to convert for clauses that iterate ver a single item into let bindings.
   * @param ctx query context
   * @return change flag
   */
  private boolean forToLet(final QueryContext ctx) {
    boolean change = false;
    for(int i = clauses.size(); --i >= 0;) {
      final Clause c = clauses.get(i);
      if(c instanceof For && ((For) c).asLet(clauses, i)) {
        ctx.compInfo(QueryText.OPTFORTOLET);
        change = true;
      }
    }
    return change;
  }

  /**
   * Inline let expressions if they are used only once (and not in a loop).
   * @param ctx query context
   * @param scp variable scope
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean inlineLets(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    boolean change = false, thisRound;
    do {
      thisRound = false;
      final ListIterator<Clause> iter = clauses.listIterator();
      while(iter.hasNext()) {
        final Clause c = iter.next();
        final int next = iter.nextIndex();
        if(c instanceof Let) {
          final Let lt = (Let) c;
          if(lt.expr.has(Flag.NDT)) continue;
          final VarUsage uses = count(lt.var, next);
          if(uses == VarUsage.NEVER) {
            ctx.compInfo(QueryText.OPTVAR, lt.var);
            iter.remove();
            change = true;
          } else if(lt.expr.isValue() || lt.expr instanceof VarRef && !lt.var.checksType()
              || uses == VarUsage.ONCE && !lt.expr.has(Flag.CTX)
              || lt.expr instanceof AxisPath && ((AxisPath) lt.expr).cheap()) {
            ctx.compInfo(QueryText.OPTINLINE, lt);
            inline(ctx, scp, lt.var, lt.inlineExpr(ctx, scp), next);
            thisRound = change = true;
            // continue from the beginning as clauses below could have been deleted
            break;
          }
        }
      }
    } while(thisRound);
    return change;
  }

  /**
   * Flattens FLWR expressions in for or let clauses by including their clauses in this
   * expression.
   *
   * @param ctx query context
   * @param scp variable scope
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean unnestFLWR(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    boolean change = false, thisRound;
    do {
      thisRound = false;
      final ListIterator<Clause> iter = clauses.listIterator();
      while(iter.hasNext()) {
        final Clause cl = iter.next();
        final boolean isFor = cl instanceof For;
        if(isFor) {
          // for $x in (for $y in A (...) return B)  ===>  for $y in A (...) for $x in B
          final For fr = (For) cl;
          if(!fr.empty && fr.pos == null && fr.expr instanceof GFLWOR) {
            final GFLWOR fl = (GFLWOR) fr.expr;
            if(fl.isFLWR()) {
              ctx.compInfo(QueryText.OPTFLAT, this);
              iter.remove();
              for(final Clause c : fl.clauses) iter.add(c);
              fr.expr = fl.ret;
              iter.add(fr);
              thisRound = change = true;
            }
          }
        }

        if(!thisRound && (isFor || cl instanceof Let)) {
          // let $x := (let $y := E return F)  ===>  let $y := E let $x := F
          final Expr e = isFor ? ((For) cl).expr : ((Let) cl).expr;
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
              final Expr rest = fl.clauses.isEmpty() ? fl.ret : fl.optimize(ctx, scp);
              if(isFor) ((For) cl).expr = rest;
              else ((Let) cl).expr = rest;
              iter.add(cl);
              thisRound = change = true;
            }
          }
        }
      }
    } while(thisRound);
    return change;
  }

  /**
   * Cleans dead entries from the tuples that {@link GroupBy} and {@link OrderBy} handle.
   * @param ctx query context
   * @return change flag
   */
  private boolean cleanDeadVars(final QueryContext ctx) {
    final IntObjMap<Var> decl = new IntObjMap<Var>();
    final BitArray used = new BitArray();

    for(final Clause cl : clauses) for(final Var v : cl.vars()) decl.put(v.id, v);
    final ASTVisitor marker = new ASTVisitor() {
      @Override
      public boolean used(final VarRef ref) {
        final int id = ref.var.id;
        if(decl.get(id) != null) used.set(id);
        return true;
      }
    };

    ret.accept(marker);
    boolean change = false;
    for(int i = clauses.size(); --i >= 0;) {
      final Clause curr = clauses.get(i);
      change |= curr.clean(ctx, decl, used);
      curr.accept(marker);
      for(final Var v : curr.vars()) used.clear(v.id);
    }
    return change;
  }

  /**
   * Optimization pass which tries to slide let expressions out of loops. Care is taken
   * that no unnecessary relocations are done.
   * @param ctx query context
   * @return {@code true} if there were relocations, {@code false} otherwise
   */
  private boolean slideLetsOut(final QueryContext ctx) {
    boolean change = false;
    for(int i = 1; i < clauses.size(); i++) {
      final Clause l = clauses.get(i);
      if(!(l instanceof Let) || l.has(Flag.NDT) || l.has(Flag.CNS)) continue;
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
        if(!change) ctx.compInfo(QueryText.OPTFORLET);
        change = true;
        // it's safe to go on because clauses below the current one are never touched
      }
    }
    return change;
  }

  /**
   * Slides where clauses upwards and removes those that do not filter anything.
   * @param ctx query context
   * @param scp variable scope
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean optimizeWhere(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    boolean change = false;
    for(int i = 0; i < clauses.size(); i++) {
      final Clause c = clauses.get(i);
      if(!(c instanceof Where) || c.has(Flag.NDT)) continue;
      final Where wh = (Where) c;

      if(wh.pred.isValue()) {
        if(!(wh.pred instanceof Bln))
          wh.pred = Bln.get(wh.pred.ebv(ctx, wh.info).bool(wh.info));

        // predicate is always false: no results possible
        if(!((Bln) wh.pred).bool(null)) break;

        // condition is always true
        clauses.remove(i--);
        change = true;
      } else {
        // find insertion position
        int insert = -1;
        for(int j = i; --j >= 0;) {
          final Clause curr = clauses.get(j);
          if(!curr.skippable(wh)) break;
          // where clauses are always moved to avoid unnecessary computations,
          // but skipping only other where clauses can cause infinite loops
          if(!(curr instanceof Where)) insert = j;
        }

        if(insert >= 0) {
          clauses.add(insert, clauses.remove(i));
          change = true;
          // it's safe to go on because clauses below the current one are never touched
        }

        final int newPos = insert < 0 ? i : insert;
        for(int b4 = newPos; --b4 >= 0;) {
          final Clause before = clauses.get(b4);
          if(before instanceof For && ((For) before).toPred(ctx, scp, wh.pred)) {
            clauses.remove(newPos);
            i--;
            change = true;
          } else if(before instanceof Where) {
            continue;
          }
          break;
        }
      }
    }
    if(change) ctx.compInfo(QueryText.OPTWHERE2);
    return change;
  }

  /** Merges consecutive {@code where} clauses. */
  private void mergeWheres() {
    Where before = null;
    final Iterator<Clause> iter = clauses.iterator();
    while(iter.hasNext()) {
      final Clause cl = iter.next();
      if(cl instanceof Where) {
        final Where wh = (Where) cl;
        if(wh.pred == Bln.FALSE) return;
        if(before != null) {
          iter.remove();
          final Expr e = before.pred;
          if(e instanceof And) {
            final And and = (And) e;
            and.expr = Array.add(and.expr, wh.pred);
          } else {
            before.pred = new And(before.info, new Expr[] { e, wh.pred });
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
    if(flag == Flag.X30 && xq30) return true;
    for(final Clause cls : clauses) if(cls.has(flag)) return true;
    return ret.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Clause cl : clauses) if(!cl.removable(v)) return false;
    return ret.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return count(v, 0);
  }

  /**
   * Counts the number of usages of the given variable starting from the given clause.
   * @param v variable
   * @param p start position
   * @return usage count
   */
  private VarUsage count(final Var v, final int p) {
    long c = 1;
    VarUsage uses = VarUsage.NEVER;
    final ListIterator<Clause> iter = clauses.listIterator(p);
    while(iter.hasNext()) {
      final Clause cl = iter.next();
      uses = uses.plus(cl.count(v).times(c));
      c = cl.calcSize(c);
    }
    return uses.plus(ret.count(v).times(c));
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return inline(ctx, scp, v, e, 0) ? optimize(ctx, scp) : null;
  }

  /**
   * Inlines an expression bound to a given variable, starting at a specified clause.
   * @param ctx query context
   * @param scp variable scope
   * @param v variable
   * @param e expression to inline
   * @param p clause position
   * @return if changes occurred
   * @throws QueryException query exception
   */
  private boolean inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e, final int p) throws QueryException {
    boolean change = false;
    final ListIterator<Clause> iter = clauses.listIterator(p);
    while(iter.hasNext()) {
      final Clause cl = iter.next();
      try {
        final Clause c = cl.inline(ctx, scp, v, e);
        if(c != null) {
          change = true;
          iter.set(c);
        }
      } catch(final QueryException qe) {
        return clauseError(qe, iter.previousIndex() + 1, cl.info);
      }
    }

    try {
      final Expr rt = ret.inline(ctx, scp, v, e);
      if(rt != null) {
        change = true;
        ret = rt;
      }
    } catch(final QueryException qe) {
      return clauseError(qe, clauses.size(), info);
    }

    return change;
  }

  /**
   * Tries to recover from a compile-time exception inside a FLWOR clause.
   * @param qe thrown exception
   * @param idx index of the throwing clause, size of {@link #clauses} for return clause
   * @param ii input info
   * @return {@code true} if the GFLWOR expression has to stay
   * @throws QueryException query exception if the whole expression fails
   */
  private boolean clauseError(final QueryException qe, final int idx, final InputInfo ii)
      throws QueryException {
    final ListIterator<Clause> iter = clauses.listIterator(idx);
    while(iter.hasPrevious()) {
      final Clause b4 = iter.previous();
      if(b4 instanceof For || b4 instanceof Window || b4 instanceof Where) {
        iter.next();
        while(iter.hasNext()) {
          iter.next();
          iter.remove();
        }
        ret = FNInfo.error(qe, ii);
        return true;
      }
    }
    throw qe;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final LinkedList<Clause> cls = new LinkedList<Clause>();
    for(final Clause cl : clauses) cls.add(cl.copy(ctx, scp, vs));
    return copyType(new GFLWOR(info, cls, ret.copy(ctx, scp, vs)));
  }

  /**
   * Checks if this FLWOR expression only used for, let and where clauses.
   * @return result of check
   */
  private boolean isFLWR() {
    for(final Clause cl : clauses)
      if(!(cl instanceof For || cl instanceof Let || cl instanceof Where)) return false;
    return true;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Clause cl : clauses) if(!cl.accept(visitor)) return false;
    return ret.accept(visitor);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    for(final Clause cl : clauses) cl.plan(e);
    ret.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Clause cl : clauses) sb.append(cl).append(' ');
    return sb.append(QueryText.RETURN).append(' ').append(ret).toString();
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Clause cl : clauses) cl.checkUp();
    ret.checkUp();
  }

  @Override
  public Expr markTailCalls() {
    long n = 1;
    for(final Clause c : clauses) if((n = c.calcSize(n)) != 1) return this;
    ret.markTailCalls();
    return this;
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Clause cl : clauses) sz += cl.exprSize();
    return ret.exprSize() + sz;
  }

  /**
   * Evaluator for FLWOR clauses.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  interface Eval {
    /**
     * Makes the next evaluation step if available. This method is guaranteed
     * to not be called again if it has once returned {@code false}.
     * @param ctx query context
     * @return {@code true} if step was made, {@code false} if no more
     * results exist
     * @throws QueryException evaluation exception
     */
    boolean next(final QueryContext ctx) throws QueryException;
  }

  /**
   * A FLWOR clause.
   *
   * @author BaseX Team 2005-12, BSD License
   * @author Leo Woerteler
   */
  public abstract static class Clause extends ParseExpr {
    /** All variables declared in this clause. */
    final Var[] vars;
    /**
     * Constructor.
     * @param ii input info
     * @param vs declared variables
     */
    protected Clause(final InputInfo ii, final Var... vs) {
      super(ii);
      vars = vs;
    }

    /**
     * Cleans unused variables from this clause.
     * @param ctx query context
     * @param used list of the IDs of all variables used in the following clauses
     * @param decl variables declared by this FLWOR expression
     * @return {@code true} if something changed, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    boolean clean(final QueryContext ctx, final IntObjMap<Var> decl,
        final BitArray used) {
      return false;
    }

    /**
     * Evaluates the clause.
     * @param sub wrapped evaluator
     * @return evaluator
     */
    abstract Eval eval(final Eval sub);

    @Override
    public abstract Clause compile(QueryContext ctx, final VarScope scp)
        throws QueryException;

    @Override
    public abstract Clause optimize(final QueryContext ctx, final VarScope scp)
        throws QueryException;

    @Override
    public abstract Clause inline(QueryContext ctx, VarScope scp, Var v, Expr e)
        throws QueryException;

    @Deprecated
    @Override
    public Iter iter(final QueryContext ctx) throws QueryException {
      throw Util.notexpected();
    }

    @Deprecated
    @Override
    public Value value(final QueryContext ctx) throws QueryException {
      throw Util.notexpected();
    }

    @Deprecated
    @Override
    public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
      throw Util.notexpected();
    }

    @Override
    public abstract GFLWOR.Clause copy(QueryContext ctx, VarScope scp, IntObjMap<Var> vs);

    /**
     * Checks if the given clause can be slided over this clause.
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
     * @param v variable
     * @return {code true} if the variable was declared here, {@code false} otherwise
     */
    public final boolean declares(final Var v) {
      for(final Var decl : vars) if(v.is(decl)) return true;
      return false;
    }

    /**
     * Calculates the number of results.
     * @param count number of incoming tuples, must be greater than zero
     * @return number of outgoing tuples if known, {@code -1} otherwise
     */
    abstract long calcSize(long count);
  }
}
