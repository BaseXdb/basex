package org.basex.query.expr.gflwor;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
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
 * General FLWOR expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class GFLWOR extends ParseExpr {
  /** FLWOR clauses. */
  public final LinkedList<Clause> clauses;
  /** Return expression. */
  public Expr rtrn;

  /**
   * Constructor.
   * @param info input info
   * @param clauses FLWOR clauses
   * @param rtrn return expression
   */
  public GFLWOR(final InputInfo info, final LinkedList<Clause> clauses, final Expr rtrn) {
    super(info, SeqType.ITEM_ZM);
    this.clauses = clauses;
    this.rtrn = rtrn;
  }

  /**
   * Creates a new evaluator for this FLWOR expression.
   * @return the evaluator
   */
  private Eval newEval() {
    Eval eval = new StartEval();
    for(final Clause clause : clauses) eval = clause.eval(eval);
    return eval;
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      private final Eval eval = newEval();
      private Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = qc.next(iter);
          if(item != null) return item;
          if(!eval.next(qc)) {
            iter = null;
            return null;
          }
          iter = rtrn.iter(qc);
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Eval eval = newEval();
    final ValueBuilder vb = new ValueBuilder(qc);
    while(eval.next(qc)) vb.add(rtrn.value(qc));
    return vb.value(this);
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
      rtrn = rtrn.compile(cc);
    } catch(final QueryException qe) {
      clauseError(qe, iter, cc);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    flattenAnd();

    // apply all optimizations in a row until nothing changes anymore
    while(flattenReturn(cc) | flattenFor(cc) | unnestFLWR(cc) | unnestLets(cc) | ifToWhere(cc) |
        forToLet(cc) | slideLetsOut(cc) | inlineForLet(cc) | unusedClauses(cc) | unusedVars(cc) |
        cleanDeadVars() | optimizeWhere(cc) | optimizePos(cc) | optimizeOrderBy(cc));

    mergeWheres();

    final Expr expr = simplify(cc);
    if(expr != null) {
      cc.info(QueryText.OPTSIMPLE_X_X, (Supplier<?>) this::description, expr);
      return expr;
    }

    exprType.assign(rtrn.seqType(), calcSize(true));
    return this;
  }

  /**
   * Simplifies a FLWOR expression.
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr simplify(final CompileContext cc) throws QueryException {
    // replace with expression of 'return' clause if all clauses were removed
    if(clauses.isEmpty()) return rtrn;

    // replace with 'if' expression if FLWOR starts with 'where'
    final Expr first = clauses.getFirst();
    if(first instanceof Where) {
      final Where where = (Where) clauses.removeFirst();
      final Expr branch = clauses.isEmpty() ? rtrn : this;
      return new If(info, where.expr, branch).optimize(cc);
    }

    if(first instanceof For) {
      // replace allowing empty with empty sequence
      //   for $_ allowing empty in () return $_  ->  ()
      final For fr = (For) first;
      if(clauses.size() == 1 && fr.size() == 0 && !fr.has(Flag.NDT) && rtrn instanceof VarRef &&
          ((VarRef) rtrn).var.is(fr.var)) return Empty.VALUE;

      // rewrite group by to distinct-values
      //   for $e in E group by $g := G return R
      //   ->  for $g in distinct-values(for $e in E return G)) return R
      if(clauses.size() == 2 && clauses.get(1) instanceof GroupBy) {
        final GroupSpec grp = ((GroupBy) clauses.get(1)).group();
        if(grp != null) {
          final LinkedList<Clause> cls = new LinkedList<>();
          cls.add(clauses.removeFirst());
          final Expr flwor = new GFLWOR(info, cls, grp.expr).optimize(cc);
          final Expr expr = cc.function(Function.DISTINCT_VALUES, info, flwor);
          clauses.set(0, new For(grp.var, expr).optimize(cc));
          return optimize(cc);
        }
      }
    }

    // checks if clauses have side effects
    final Checks<Clause> ndt = clause -> clause.has(Flag.NDT);
    // checks if the return expression references the variable of a clause
    final Checks<Clause> varrefs = clause -> {
      for(final Var var : clause.vars()) {
        if(rtrn.count(var) != VarUsage.NEVER) return true;
      }
      return false;
    };

    // calculate exact number of iterated items
    final long[] minMax = calcSize(false);
    final long min = minMax[0], max = minMax[1];
    if(min == max) {
      if(min == 0) {
        // zero iterations, no side effects
        //   for $_ in () return <x/>  ->  ()
        if(!has(Flag.NDT)) return Empty.VALUE;
      } else if(!varrefs.any(clauses) && !ndt.any(clauses)) {
        // no referenced variables in return clause
        //   let $_ := 1 return <x/>  ->  <x/>
        //   for $_ in 1 to 2 return 3  ->  util:replicate(3, 2)
        return min == 1 ? rtrn : cc.replicate(rtrn, Int.get(min), info);
      }
    }

    // for $_ in 1 to 2 return ()  ->  ()
    return rtrn == Empty.VALUE && !ndt.any(clauses) ? rtrn : null;
  }

  /**
   * Computes the number of results of this FLWOR expression.
   * @param ret include return clause
   * @return min/max values (min: 0 or more, max: -1 or more)
   */
  private long[] calcSize(final boolean ret) {
    final long[] minMax = { 1, 1 };
    for(final Clause clause : clauses) {
      if(minMax[1] != 0) clause.calcSize(minMax);
    }
    if(ret && minMax[1] != 0) {
      final long size = rtrn.size();
      minMax[0] *= Math.max(size, 0);
      if(minMax[1] > 0) minMax[1] = size >= 0 ? minMax[1] * size : -1;
      else if(size == 0) minMax[1] = 0;
    }
    return minMax;
  }

  /**
   * Rewrites and expressions in predicates to where clauses.
   */
  private void flattenAnd() {
    // rewrite and operators in where clauses to single clauses
    final ListIterator<Clause> iter = clauses.listIterator();
    while(iter.hasNext()) {
      final Clause clause = iter.next();
      if(clause instanceof Where) {
        final Where where = (Where) clause;
        if(where.expr instanceof And) {
          iter.remove();
          for(final Expr expr : where.expr.args()) iter.add(new Where(expr, where.info));
        }
      }
    }
  }

  /**
   * Tries to convert 'for' clauses that iterate over a single item into 'let' bindings.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean forToLet(final CompileContext cc) throws QueryException {
    boolean changed = false;
    for(int i = clauses.size(); --i >= 0;) {
      final Clause clause = clauses.get(i);
      if(clause instanceof For && ((For) clause).asLet(clauses, i, cc)) {
        cc.info(QueryText.OPTFORTOLET_X, clause);
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Removes unused variables or simplifies their expressions.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean unusedVars(final CompileContext cc) throws QueryException {
    boolean changed = false;
    final ListIterator<Clause> iter = clauses.listIterator();
    while(iter.hasNext()) iter.next();

    // loop backwards (removed clauses may contain references to variables of outer clauses)
    while(iter.hasPrevious()) {
      final int pos = iter.previousIndex();
      final Clause clause = iter.previous();
      if(clause instanceof Let) {
        // completely remove 'let' clause
        final Let lt = (Let) clause;
        if(count(lt.var, pos + 1) == VarUsage.NEVER && !lt.has(Flag.NDT)) {
          cc.info(QueryText.OPTVAR_X, lt.var);
          // check type before removing variable (see {@link FuncType})
          lt.var.checkType(lt.expr);
          iter.remove();
          changed = true;
        }
      } else if(clause instanceof For) {
        final For fr = (For) clause;
        final long fs = fr.expr.size();
        if(fs > 1 && fr.var.declType == null && !(fr.expr instanceof SingletonSeq) &&
            !fr.has(Flag.NDT) && count(fr.var, pos + 1) == VarUsage.NEVER) {
          if(fr.pos != null && count(fr.pos, pos) != VarUsage.NEVER) {
            // replace with positional variable
            //   for $i at $p in ('a', 'b') return $p  ->  for $p in 1 to 2 return $p
            fr.expr = cc.replaceWith(fr.expr, RangeSeq.get(1, fs, true));
            fr.exprType.assign(AtomType.INTEGER);
            fr.var = fr.pos;
            fr.remove(cc, fr.pos);
          } else {
            // replace with singleton sequence (will never be accessed)
            //   for $i in 1 to 3 return <a/>  ->  for $i in util:replicate('', 3) return $i
            fr.expr = cc.replaceWith(fr.expr, SingletonSeq.get(Str.EMPTY, fs));
            fr.exprType.assign(AtomType.STRING);
          }
          changed = true;
        }
        // remove scoring variable (include current iteration in count):
        //   for $i score $s in $nodes return $i  ->  for $i in $nodes return $i
        // number of iterations is considered
        //   for $i score $s in () return $s  ->  for $i in () return $s  ->  ()
        if(fr.score != null && count(fr.score, pos) == VarUsage.NEVER) {
          fr.remove(cc, fr.score);
          changed = true;
        }
        // remove positional variable
        //   for $i at $p in (1,2,3) return $i  ->  for $i in (1,2,3) return $i
        // number of iterations is considered
        //   for $i at $p in () return $p  ->  for $i in () return $p  ->  ()
        if(fr.pos != null && count(fr.pos, pos) == VarUsage.NEVER) {
          fr.remove(cc, fr.pos);
          changed = true;
        }
      }
    }
    return changed;
  }

  /**
   * Removes clauses that will never be executed.
   * @param cc compilation context
   * @return change flag
   */
  private boolean unusedClauses(final CompileContext cc) {
    // for $_ in () return 1  ->  return ()
    boolean changed = false;
    for(int c = clauses.size() - 1; c >= 0; c--) {
      final Clause clause = clauses.get(c);
      if(clause instanceof For) {
        final For fr = (For) clause;
        if(fr.expr.size() == 0 && !fr.empty) {
          for(int d = clauses.size() - 1; d >= c; d--) clauses.remove(c);
          rtrn = fr.expr;
          cc.info(QueryText.OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
          changed = true;
        }
      }
    }
    return changed;
  }

  /**
   * Inlines for/let expressions.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean inlineForLet(final CompileContext cc) throws QueryException {
    boolean changed = false;
    int cs = clauses.size();
    for(int c = cs - 1; c >= 0; c--) {
      final Clause clause = clauses.get(c);
      if(!(clause instanceof ForLet)) continue;

      final ForLet fl = (ForLet) clause;
      final Expr inline = fl.inlineExpr(cc);
      if(inline == null) continue;

      // inline for/let expression
      //   let $a := 123 return $a  ->  return 123
      boolean changing = false;
      if(fl instanceof Let && !fl.has(Flag.NDT)) {
        final InlineContext ic = new InlineContext(fl.var, inline, cc);
        final ExprList exprs = new ExprList(cs - c);
        for(int d = c + 1; d < cs; d++) exprs.add(clauses.get(d));
        if(ic.inlineable(exprs.add(rtrn).finish())) {
          inline(ic, clauses.listIterator(c));
          changing = true;
        }
      }
      // merge for/let expression with subsequent expression
      final boolean last = c + 1 == cs;
      if(!changing && (last || clauses.get(c + 1) instanceof ForLet &&
          count(fl.var, c + 2) == VarUsage.NEVER)) {
        if(last) {
          // merge with return expression
          //   for $c in 1 to 3 return $c  ->  1 to 3
          final Expr expr = inline(inline, rtrn, fl, cc);
          if(expr != null) {
            rtrn = expr;
            changing = true;
          }
        } else {
          // merge with next for/let expression
          //   for $a in $seq for $b in 1 to $a  ->  for $b in $seq ! (1 to .)
          //   for $a in $seq let $b := $a  ->  for $b in $seq
          ForLet next = (ForLet) clauses.get(c + 1);
          if(fl instanceof Let || next.size() == 1) {
            final Expr expr = inline(inline, next.expr, fl, cc);
            if(expr != null) {
              next.expr = expr;
              if(fl instanceof For && next instanceof Let) {
                next = For.fromLet((Let) next, cc);
                clauses.set(c + 1, next);
              }
              next.optimize(cc);
              changing = true;
            }
          }
        }
      }

      if(changing) {
        cc.info(QueryText.OPTINLINE_X, fl);
        clauses.remove(c);
        cs = clauses.size();
        changed = true;
      }
    }

    return changed;
  }

  /**
   * Tries to inline an expression.
   * @param inline expression to inline
   * @param expr target expression
   * @param fl for/let clause
   * @param cc compilation context
   * @return inlined expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr inline(final Expr inline, final Expr expr, final ForLet fl,
      final CompileContext cc) throws QueryException {

    if(expr instanceof VarRef && ((VarRef) expr).var.is(fl.var)) {
      // replace return clause with expression
      //   for $c in (1, 2) return $c  ->  (1, 2)
      //   let $c := <a/> return $c  ->  <a/>
      return inline;
    }
    if(fl instanceof Let && expr.count(fl.var) == VarUsage.NEVER) {
      // rewrite let clause with unused variable
      //   let $_ := file:write(...) return ()  ->  file:write(...)
      //   let $_ := prof:void(1) return 2  ->  prof:void(1), 2
      return cc.merge(inline, expr, info);
    }
    if(fl.size() == 1) {
      // rewrite for/let clause to simple map
      //   for $c in (1, 2, 3) return ($c + $c)  ->  (1, 2, 3) ! (. + .)
      //   let $c := <_/> return (name($c), $c)  ->  <_/> ! (name(.), .)
      // skip expressions with context reference
      //   <_/>[for $c in (1, 2) return (., $c)]
      final InlineContext ic = new InlineContext(fl.var, new ContextValue(info), cc);
      if(ic.inlineable(expr) && !expr.has(Flag.CTX)) {
        return SimpleMap.get(cc, info, inline, cc.get(inline, () -> ic.inline(expr)));
      }
    }
    return null;
  }

  /**
   * Unnests basic FLWR expressions in 'for' or 'let' clauses.
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
          // for $x in (for $y in A (...) return B)  ->  for $y in A (...) for $x in B
          final For fr = (For) clause;
          if(!fr.empty && fr.pos == null && fr.expr instanceof GFLWOR) {
            final GFLWOR fl = (GFLWOR) fr.expr;
            if(fl.isFLW()) {
              cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) this::description, fr.var);
              iter.remove();
              for(final Clause cls : fl.clauses) iter.add(cls);
              fr.expr = fl.rtrn;
              iter.add(fr);
              thisRound = changed = true;
            }
          }
        }

        if(!thisRound && (isFor || isLet)) {
          // let $x := (let $y := E return F)  ->  let $y := E let $x := F
          final Expr expr = isFor ? ((For) clause).expr : ((Let) clause).expr;
          if(expr instanceof GFLWOR) {
            final GFLWOR fl = (GFLWOR) expr;
            final LinkedList<Clause> cls = fl.clauses;
            if(cls.getFirst() instanceof Let) {
              // remove the binding from the outer clauses
              iter.remove();

              // propagate all leading 'let' bindings into outer clauses
              do iter.add(cls.removeFirst());
              while(!cls.isEmpty() && cls.getFirst() instanceof Let);

              // re-add the binding with new, reduced expression at the end
              final Expr rest = fl.clauses.isEmpty() ? fl.rtrn : fl.optimize(cc);
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
    for(final Clause clause : clauses) {
      for(final Var var : clause.vars()) decl.put(var.id, var);
    }

    final BitArray used = new BitArray();
    final ASTVisitor marker = new ASTVisitor() {
      @Override
      public boolean used(final VarRef ref) {
        final int id = ref.var.id;
        if(decl.get(id) != null) used.set(id);
        return true;
      }
    };

    rtrn.accept(marker);
    boolean changed = false;
    for(int c = clauses.size(); --c >= 0;) {
      final Clause clause = clauses.get(c);
      changed |= clause.clean(decl, used);
      clause.accept(marker);
      for(final Var var : clause.vars()) used.clear(var.id);
    }
    return changed;
  }

  /**
   * Tries to slide 'let' expressions out of loops.
   * Care is taken that no unnecessary relocations are done.
   * @param cc compilation context
   * @return {@code true} if there were relocations, {@code false} otherwise
   */
  private boolean slideLetsOut(final CompileContext cc) {
    boolean changed = false;
    for(int c = 1; c < clauses.size(); c++) {
      final Clause clause = clauses.get(c);
      // do not move node constructors. example: for $x in 1 to 2 let $a := <x/> return $a
      if(!(clause instanceof Let) || clause.has(Flag.NDT, Flag.CNS)) continue;
      final Let let = (Let) clause;

      // find insertion position
      int insert = -1;
      for(int d = c; --d >= 0;) {
        final Clause curr = clauses.get(d);
        if(!curr.skippable(let)) break;
        // insert directly above the highest skippable 'for' or 'window' clause
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
    final HashSet<ForLet> forLets = new HashSet<>();
    for(int c = 0; c < clauses.size(); c++) {
      final Clause clause = clauses.get(c);
      if(!(clause instanceof Where)) continue;

      final Where where = (Where) clause;
      if(where.expr instanceof Value) {
        // if test is always false, skip remaining tests (no results possible)
        if(!where.expr.ebv(cc.qc, where.info).bool(where.info)) {
          where.expr = Bln.FALSE;
          break;
        }
        // test is always true: remove it
        cc.info(QueryText.OPTREMOVE_X_X, where.expr, (Supplier<?>) this::description);
        clauses.remove(c--);
        changed = true;
      } else if(!clause.has(Flag.NDT)) {
        // find insertion position
        int insert = -1;
        for(int j = c; --j >= 0;) {
          final Clause curr = clauses.get(j);
          if(curr.has(Flag.NDT) || !curr.skippable(where)) break;
          // where clauses are always moved to avoid unnecessary computations,
          // but skipping only other where clauses can cause infinite loops
          if(!(curr instanceof Where)) insert = j;
        }

        if(insert >= 0) {
          clauses.add(insert, clauses.remove(c));
          cc.info(QueryText.OPTMOVE_X, where.expr);
          changed = true;
          // it's safe to go on because clauses below the current one are never touched
        }

        // rewrite where clause to predicate:
        //   for $b in /a/b where $b/c  ->  for $b in /a/b[c]
        //   let $a := 1 to 3 where $a > 1 return $a  ->  let $a := (1 to 3)[. > 1] ...
        //   let $a := <a/> where $a[. = ''] return $a/self::a  ->  let $a := <a/>[. == ''] ...
        if(!clause.has(Flag.CTX)) {
          final int newPos = insert < 0 ? c : insert;
          for(int b4 = newPos; --b4 >= 0;) {
            final Clause before = clauses.get(b4);
            // skip where clauses
            if(before instanceof Where) continue;
            // analyze for/let clauses, abort otherwise
            if(before instanceof ForLet) {
              final ForLet fl = (ForLet) before;
              final Predicate<Expr> var = expr ->
                expr instanceof VarRef && ((VarRef) expr).var.is(fl.var);
              if(before instanceof For || c + 1 == clauses.size() && (
                var.test(rtrn) ||
                rtrn instanceof Filter && var.test(((Filter) rtrn).root) ||
                rtrn instanceof Path && var.test(((Path) rtrn).root)
              )) {
                if(fl.toPredicate(cc, where.expr)) {
                  forLets.add(fl);
                  clauses.remove(newPos);
                  cc.info(QueryText.OPTPRED_X, where.expr);
                  changed = true;
                  c--;
                }
              }
            }
            break;
          }
        }
      }
    }
    // optimize on rewritten expressions (only once per clause)
    for(final ForLet fl : forLets) fl.expr = fl.expr.optimize(cc);
    return changed;
  }

  /**
   * Rewrites positional variables to predicates.
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
      for(int d = c + 1; d < clauses.size(); d++) {
        final Clause cl = clauses.get(d);
        if(!(cl instanceof Where)) {
          // stop if clause is no 'for' or 'let' expression or non-deterministic
          if(!(cl instanceof For || cl instanceof Let) || cl.has(Flag.NDT)) break;
          continue;
        }

        Expr expr = ((Where) cl).expr;
        if(expr instanceof CmpG) expr = CmpIR.get((CmpG) expr, true, cc);
        if(!(expr instanceof CmpIR)) continue;

        final CmpIR cmp = (CmpIR) expr;
        if(!(cmp.expr instanceof VarRef)) continue;

        // remove clause and ensure that the positional variable is only used once
        clauses.remove(d);
        if(count(pos.pos, c) == VarUsage.NEVER) {
          // for $e at $p in E where $p = P ...  ->  for $e in E[position() = P] ...
          pos.addPredicate(cc, ItrPos.get(cmp.min, cmp.max, cmp.info));
          cc.info(QueryText.OPTPRED_X, expr);
          changed = true;
        } else {
          // re-add variable, give up
          clauses.add(d, cl);
        }
        break;
      }
    }
    return changed;
  }

  /**
   * Simplify order by clauses.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean optimizeOrderBy(final CompileContext cc) throws QueryException {
    final ListIterator<Clause> iter = clauses.listIterator();
    For fr = null;
    while(fr == null && iter.hasNext()) {
      final Clause clause = iter.next();
      if(clause instanceof For) fr = (For) clause;
    }
    if(fr != null && fr.vars.length == 1 && iter.hasNext()) {
      final Clause clause = iter.next();
      if(clause instanceof OrderBy && ((OrderBy) clause).merge(fr, cc)) {
        iter.remove();
        return true;
      }
    }
    return false;
  }

  /**
   * Rewrites if expressions to where clauses.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean ifToWhere(final CompileContext cc) throws QueryException {
    final QueryFunction<Expr, If> rewritableIf = expr -> {
      if(expr instanceof If) {
        final If iff = (If) expr;
        if(iff.exprs[0] == Empty.VALUE) {
          iff.cond = cc.function(Function.NOT, info, iff.cond);
          iff.swap();
        }
        if(iff.exprs[1] == Empty.VALUE) return iff;
      }
      return null;
    };

    boolean changed = false;
    for(int c = clauses.size(); --c >= 0;) {
      final Clause clause = clauses.get(c);
      if(clause instanceof For) {
        final For fr = (For) clause;
        final If iff = rewritableIf.apply(fr.expr);
        if(iff != null && !fr.empty) {
          fr.expr = iff.exprs[0];
          clauses.add(c, new Where(iff.cond, iff.info));
          changed = true;
        }
      }
    }

    final If iff = rewritableIf.apply(rtrn);
    if(iff != null) {
      clauses.add(new Where(iff.cond, iff.info));
      rtrn = iff.exprs[0];
      changed = true;
    }
    return changed;
  }

  /**
   * Flatten FLWOR expressions in 'for' clauses.
   * @param cc compilation context
   * @return change flag
   * @throws QueryException query exception
   */
  private boolean flattenFor(final CompileContext cc) throws QueryException {
    if(!clauses.isEmpty() && clauses.getFirst() instanceof For) {
      final For fst = (For) clauses.getFirst();
      if(!fst.empty) {
        // flat nested FLWOR expressions
        if(fst.expr instanceof GFLWOR) {
          // for $a in (for $b in ... return $b + 1) ...  ->  for $b in ... for $a in $b + 1 ...
          cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) this::description, fst.var);
          final GFLWOR sub = (GFLWOR) fst.expr;
          clauses.set(0, new For(fst.var, null, fst.score, sub.rtrn, false));
          if(fst.pos != null) clauses.add(1, new Count(fst.pos));
          clauses.addAll(0, sub.clauses);
          return true;
        }
        if(clauses.size() > 1 && clauses.get(1) instanceof Count) {
          final Count cnt = (Count) clauses.get(1);
          if(fst.pos != null) {
            // for $a at $b in ... count $c ...  ->  for $a at $b in ... let $c := $b ...
            final VarRef ref = new VarRef(cnt.info, fst.pos).optimize(cc);
            clauses.set(1, new Let(cnt.var, ref).optimize(cc));
          } else {
            // for $a in 1 to 3 count $c ...  -> for $a at $c in 1 to 3 ...
            clauses.set(0, new For(fst.var, cnt.var, fst.score, fst.expr, false).optimize(cc));
            clauses.remove(1);
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Flatten FLWOR expressions in 'return' clauses.
   * @param cc compilation context
   * @return change flag
   */
  private boolean flattenReturn(final CompileContext cc) {
    if(!clauses.isEmpty() && rtrn instanceof GFLWOR) {
      final GFLWOR sub = (GFLWOR) rtrn;
      if(sub.isFLW()) {
        // flatten nested FLWOR expressions
        // for $a in (1 to 2) return let $f := ...  ->  for $a in (1 to 2) let $f := ...
        final Clause clause = sub.clauses.getFirst();
        final ExprInfo ei = clause instanceof ForLet ? ((ForLet) clause).var : clause;
        cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) this::description, ei);
        clauses.addAll(sub.clauses);
        rtrn = sub.rtrn;
        return true;
      }
    }
    return false;
  }

  /**
   * Tries to slide 'let' expressions out of loops.
   * Care is taken that no unnecessary relocations are done.
   * @param cc compilation context
   * @return {@code true} if there were relocations, {@code false} otherwise
   * @throws QueryException query exception
   */
  private boolean unnestLets(final CompileContext cc) throws QueryException {
    if(!clauses.isEmpty()) {
      final TypeCheck tc = rtrn instanceof TypeCheck ? (TypeCheck) rtrn : null;
      if(rtrn instanceof GFLWOR || tc != null && tc.expr instanceof GFLWOR) {
        final GFLWOR sub = (GFLWOR) (tc == null ? rtrn : tc.expr);
        final Clause clause = sub.clauses.getFirst();
        if(clause instanceof Let) {
          /* example:
          function($a) as xs:integer+ {
            function($c) { ($c, if($a) then $a else $c) }(1[. = 1])
          }(2[. = 1])

          OLD:
          let $a := 2[. = 1]
          return (
            let $c := 1[. = 1]
            return ($c, if($a) then $a else $c)
          )

          NEW:
          let $a := 2[. = 1]
          let $c := 1[. = 1]
          return ($c, if($a) then $a else $c))
          */
          cc.info(QueryText.OPTFLAT_X_X, (Supplier<?>) this::description, ((Let) clause).var);

          // propagate all leading 'let' bindings into outer clauses
          final LinkedList<Clause> cls = sub.clauses;
          do {
            clauses.add(cls.removeFirst());
          } while(!cls.isEmpty() && cls.getFirst() instanceof Let);
          if(tc != null) tc.expr = sub.optimize(cc);
          rtrn = rtrn.optimize(cc);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Merges consecutive {@code where} clauses.
   */
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
          final Expr expr = before.expr;
          if(expr instanceof And) {
            final And and = (And) expr;
            and.exprs = ExprList.concat(and.exprs, wh.expr);
          } else {
            before.expr = new And(before.info, expr, wh.expr);
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
  public boolean vacuous() {
    return rtrn.vacuous();
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final Clause clause : clauses) {
      if(clause.has(flags)) return true;
    }
    return rtrn.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final Clause clause : clauses) {
      if(!clause.inlineable(ic)) return false;
    }
    return rtrn.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return count(var, 0);
  }

  @Override
  public Data data() {
    return rtrn.data();
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
    for(final ListIterator<Clause> iter = clauses.listIterator(index); iter.hasNext();) {
      final Clause clause = iter.next();
      uses = uses.plus(clause.count(var).times(minMax[1]));
      clause.calcSize(minMax);
    }
    return uses.plus(rtrn.count(var).times(minMax[1]));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    return inline(ic, clauses.listIterator()) ? optimize(ic.cc) : null;
  }

  /**
   * Inlines an expression bound to a given variable, starting at a specified clause.
   * @param ic inlining context
   * @param iter iterator at the position of the first clause to inline into
   * @return if changes occurred
   * @throws QueryException query exception
   */
  private boolean inline(final InlineContext ic, final ListIterator<Clause> iter)
      throws QueryException {

    boolean changed = false;
    while(iter.hasNext()) {
      final Clause clause = iter.next();
      try {
        final Expr inlined = ic.inlineOrNull(clause);
        if(inlined != null) {
          iter.set((Clause) inlined);
          changed = true;
        }
      } catch(final QueryException qe) {
        iter.remove();
        return clauseError(qe, iter, ic.cc);
      }
    }

    try {
      final Expr inlined = ic.inlineOrNull(rtrn);
      if(inlined != null) {
        rtrn = inlined;
        changed = true;
      }
    } catch(final QueryException qe) {
      return clauseError(qe, iter, ic.cc);
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
        rtrn = cc.error(qe, rtrn);
        return true;
      }
    }

    // error will always be thrown
    throw qe;
  }

  /**
   * Removes order by clauses.
   * @param cc compilation context
   * @return rewritten expression or {@code null}
   * @throws QueryException query exception
   */
  public Expr removeOrderBy(final CompileContext cc) throws QueryException {
    return clauses.removeIf(clause -> clause instanceof OrderBy) ? optimize(cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final LinkedList<Clause> cls = new LinkedList<>();
    for(final Clause clause : clauses) cls.add(clause.copy(cc, vm));
    return copyType(new GFLWOR(info, cls, rtrn.copy(cc, vm)));
  }

  /**
   * Checks if this FLWOR expression has only 'for', 'let' and 'where' clauses.
   * @return result of check
   */
  private boolean isFLW() {
    for(final Clause clause : clauses)
      if(!(clause instanceof For || clause instanceof Let || clause instanceof Where)) return false;
    return true;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Clause clause : clauses) {
      if(!clause.accept(visitor)) return false;
    }
    return rtrn.accept(visitor);
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Clause clause : clauses) clause.checkUp();
    rtrn.checkUp();
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    final long[] minMax = { 1, 1 };
    for(final Clause clause : clauses) {
      clause.calcSize(minMax);
      if(minMax[1] < 0 || minMax[1] > 1) return;
    }
    rtrn.markTailCalls(cc);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Clause clause : clauses) size += clause.exprSize();
    return rtrn.exprSize() + size;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    if(tc.seqType().occ != Occ.ZERO_OR_MORE) return null;
    final Expr r = tc.check(rtrn, cc);
    if(r == null) return this;
    rtrn = r;
    return optimize(cc);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof GFLWOR)) return false;
    final GFLWOR g = (GFLWOR) obj;
    return clauses.equals(g.clauses) && rtrn.equals(g.rtrn);
  }

  @Override
  public String description() {
    return "FLWOR expression";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), clauses.toArray(new Clause[0]), rtrn);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token("(").tokens(clauses.toArray()).token(QueryText.RETURN).token(rtrn).token(')');
  }

  /** Start evaluator, doing nothing, once. */
  private static final class StartEval extends Eval {
    /** First-evaluation flag. */
    private boolean more;

    @Override
    public boolean next(final QueryContext qc) {
      if(more) return false;
      more = true;
      return true;
    }
  }
}
