package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.CmpG.OpG;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and {@link Step}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Preds extends ParseExpr {
  /** Predicates. */
  public Expr[] preds;
  /** Compilation: first predicate uses last function. */
  public boolean last;
  /** Compilation: first predicate uses position. */
  public Pos pos;

  /**
   * Constructor.
   * @param info input info
   * @param preds predicates
   */
  protected Preds(final InputInfo info, final Expr[] preds) {
    super(info);
    this.preds = preds;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(preds);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final Value init = qc.value;
    // never compile predicates with empty sequence as context value (#1016)
    if(init != null && init.isEmpty()) qc.value = null;
    try {
      final int pl = preds.length;
      for(int p = 0; p < pl; ++p) preds[p] = preds[p].compile(qc, scp).optimizeEbv(qc, scp);
      return this;
    } finally {
      qc.value = init;
    }
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // number of predicates may change in loop
    for(int p = 0; p < preds.length; p++) {
      final Expr pr = preds[p];
      if(pr instanceof CmpG || pr instanceof CmpV) {
        final Cmp cmp = (Cmp) pr;
        if(cmp.exprs[0].isFunction(Function.POSITION)) {
          final Expr e2 = cmp.exprs[1];
          final SeqType st2 = e2.seqType();
          // position() = last() -> last()
          // position() = $n (numeric) -> $n
          if(e2.isFunction(Function.LAST) || st2.one() && st2.type.isNumber()) {
            if(cmp instanceof CmpG && ((CmpG) cmp).op == OpG.EQ ||
               cmp instanceof CmpV && ((CmpV) cmp).op == OpV.EQ) {
              qc.compInfo(OPTWRITE, pr);
              preds[p] = e2;
            }
          }
        }
      } else if(pr instanceof And) {
        if(!pr.has(Flag.FCS)) {
          // replace AND expression with predicates (don't swap position tests)
          qc.compInfo(OPTPRED, pr);
          final Expr[] and = ((Arr) pr).exprs;
          final int m = and.length - 1;
          final ExprList el = new ExprList(preds.length + m);
          for(final Expr e : Arrays.asList(preds).subList(0, p)) el.add(e);
          for(final Expr a : and) {
            // wrap test with boolean() if the result is numeric
            el.add(Function.BOOLEAN.get(null, info, a).optimizeEbv(qc, scp));
          }
          for(final Expr e : Arrays.asList(preds).subList(p + 1, preds.length)) el.add(e);
          preds = el.finish();
        }
      } else if(pr instanceof ANum) {
        final ANum it = (ANum) pr;
        final long i = it.itr();
        if(i == it.dbl()) {
          preds[p] = Pos.get(i, info);
        } else {
          qc.compInfo(OPTREMOVE, this, pr);
          return Empty.SEQ;
        }
      } else if(pr.isValue()) {
        if(pr.ebv(qc, info).bool(info)) {
          qc.compInfo(OPTREMOVE, this, pr);
          preds = Array.delete(preds, p--);
        } else {
          // handle statically known predicates
          qc.compInfo(OPTREMOVE, this, pr);
          return Empty.SEQ;
        }
      }
    }
    return this;
  }

  /**
   * Prepares this expression for iterative evaluation. The expression can be iteratively
   * evaluated if no predicate or only the first is positional.
   * @return result of check
   */
  protected final boolean posIterator() {
    // check if first predicate is numeric
    if(preds.length == 1) {
      Expr p = preds[0];
      if(p instanceof Int) p = Pos.get(((Int) p).itr(), info);
      pos = p instanceof Pos ? (Pos) p : null;
      last = p.isFunction(Function.LAST);
      preds[0] = p;
    }
    return pos != null || last;
  }

  /**
   * Checks if the predicates are successful for the specified item.
   * @param it item to be checked
   * @param qc query context
   * @param scoring scoring flag
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean preds(final Item it, final QueryContext qc, final boolean scoring)
      throws QueryException {

    if(preds.length == 0) return true;

    // set context value and position
    final Value cv = qc.value;
    try {
      for(final Expr p : preds) {
        qc.value = it;
        final Item i = p.test(qc, info);
        if(i == null) return false;
        if(scoring) it.score(i.score());
      }
      return true;
    } finally {
      qc.value = cv;
    }
  }

  /**
   * Merges a single predicate with the root expression and returns the resulting expression,
   * or returns a self reference if the expression cannot be merged.
   * This function is e.g. called by {@link Where#optimize}.
   * @param qc query context
   * @param scp variable scope
   * @param root root expression
   * @return expression
   * @throws QueryException query exception
   */
  public Expr merge(final Expr root, final QueryContext qc, final VarScope scp)
      throws QueryException {

    // only one predicate can be rewritten; root expression must yield nodes
    if(preds.length != 1 || !(root.seqType().type instanceof NodeType)) return this;

    final Expr pred = preds[0];
    // a[.] -> a
    if(pred instanceof Context) return root;

    if(!pred.seqType().mayBeNumber()) {
      // a[b] -> a/b
      if(pred instanceof Path) return Path.get(info, root, pred).optimize(qc, scp);

      if(pred instanceof CmpG) {
        final CmpG cmp = (CmpG) pred;
        final Expr expr1 = cmp.exprs[0], expr2 = cmp.exprs[1];
        // only first operand can depend on context
        if(!expr2.has(Flag.CTX)) {
          Expr path = null;
          // a[. = 'x'] -> a = 'x'
          if(expr1 instanceof Context) path = root;
          // a[text() = 'x'] -> a/text() = 'x'
          if(expr1 instanceof Path) path = Path.get(info, root, expr1).optimize(qc, scp);
          if(path != null) return new CmpG(path, expr2, cmp.op, cmp.coll, cmp.sc, cmp.info);
        }
      }

      if(pred instanceof FTContains) {
        final FTContains cmp = (FTContains) pred;
        final FTExpr ftexpr = cmp.ftexpr;
        // only first operand can depend on context
        if(!ftexpr.has(Flag.CTX)) {
          final Expr expr = cmp.expr;
          Expr path = null;
          // a[. contains text 'x'] -> a contains text 'x'
          if(expr instanceof Context) path = root;
          // [text() contains text 'x'] -> a/text() contains text 'x'
          if(expr instanceof Path) path = Path.get(info, root, expr).optimize(qc, scp);
          if(path != null) return new FTContains(path, ftexpr, cmp.info);
        }
      }
    }
    return this;
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Expr pred : preds) {
      if(flag == Flag.FCS && pred.seqType().mayBeNumber() || pred.has(flag)) return true;
    }
    return false;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Expr p : preds) if(p.uses(var)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, preds);
  }

  /**
   * Copies fields to the given object.
   * @param <T> object type
   * @param p copy
   * @return the copy
   */
  protected final <T extends Preds> T copy(final T p) {
    p.last = last;
    p.pos = pos;
    return copyType(p);
  }

  @Override
  public void plan(final FElem plan) {
    for(final Expr p : preds) p.plan(plan);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr e : preds) sb.append('[').append(e).append(']');
    return sb.toString();
  }
}
