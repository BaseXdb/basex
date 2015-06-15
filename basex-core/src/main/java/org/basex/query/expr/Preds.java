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
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and {@link Step}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class Preds extends ParseExpr {
  /** Predicates. */
  public Expr[] preds;

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
      for(int p = 0; p < pl; ++p) {
        try {
          preds[p] = preds[p].compile(qc, scp).optimizeEbv(qc, scp);
        } catch(final QueryException ex) {
          // replace original expression with error
          preds[p] = FnError.get(ex, seqType);
        }
      }
      return this;
    } finally {
      qc.value = init;
    }
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // number of predicates may change in loop
    for(int p = 0; p < preds.length; p++) {
      Expr pred = preds[p];
      if(pred instanceof CmpG || pred instanceof CmpV) {
        final Cmp cmp = (Cmp) pred;
        final Expr e1 = cmp.exprs[0], e2 = cmp.exprs[1];
        if(e1.isFunction(Function.POSITION)) {
          final SeqType st2 = e2.seqType();
          // position() = last() -> last()
          // position() = $n (xs:numeric) -> $n
          if(e2.isFunction(Function.LAST) || st2.one() && st2.type.isNumber()) {
            if(cmp instanceof CmpG && ((CmpG) cmp).op == OpG.EQ ||
               cmp instanceof CmpV && ((CmpV) cmp).op == OpV.EQ) {
              qc.compInfo(OPTWRITE, pred);
              preds[p] = e2;
            }
          }
        }
      } else if(pred instanceof And) {
        if(!pred.has(Flag.POS)) {
          // replace AND expression with predicates (don't swap position tests)
          qc.compInfo(OPTPRED, pred);
          final Expr[] and = ((Arr) pred).exprs;
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
      } else if(pred instanceof ANum) {
        final ANum it = (ANum) pred;
        final long i = it.itr();
        // example: ....[position() = 1.2]
        if(i != it.dbl()) return optPre(qc);
        pred = Pos.get(i, info);
        // example: ....[position() = 0]
        if(!(pred instanceof Pos)) return optPre(qc);
        preds[p] = pred;
      } else if(pred.isValue()) {
        if(pred.ebv(qc, info).bool(info)) {
          // example: ....[true()]
          qc.compInfo(OPTREMOVE, this, pred);
          preds = Array.delete(preds, p--);
        } else {
          // example: ....[false()]
          return optPre(qc);
        }
      }
    }
    return this;
  }

  /**
   * Assigns the sequence type and {@link #size} value.
   * @param st sequence type of input
   * @param s size of input ({@code -1} if unknown)
   */
  protected final void seqType(final SeqType st, final long s) {
    boolean exact = s != -1;
    long max = exact ? s : Long.MAX_VALUE;

    // evaluate positional predicates
    for(final Expr pred : preds) {
      if(pred.isFunction(Function.LAST)) {
        // use minimum of old value and 1
        max = Math.min(max, 1);
      } else if(pred instanceof Pos) {
        final Pos pos = (Pos) pred;
        // subtract start position. example: ...[1 to 2][2] -> (2 ->) 1
        if(max != Long.MAX_VALUE) max = Math.max(0, max - pos.min + 1);
        // use minimum of old value and range. example: ...[1 to 5] -> 5
        max = Math.min(max, pos.max - pos.min + 1);
      } else {
        // resulting size will be unknown for any other filter
        exact = false;
      }
    }

    if(exact || max == 0) {
      seqType = st.withSize(max);
      size = max;
    } else {
      // we only know if there will be at most 1 result
      seqType = st.withOcc(max == 1 ? Occ.ZERO_ONE : Occ.ZERO_MORE);
      size = -1;
    }
  }

  /**
   * Checks if the predicates are successful for the specified item.
   * @param item item to be checked
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean preds(final Item item, final QueryContext qc) throws QueryException {
    // set context value and position
    final Value cv = qc.value;
    qc.value = item;
    try {
      double s = qc.scoring ? 0 : -1;
      for(final Expr pred : preds) {
        final Item test = pred.test(qc, info);
        if(test == null) return false;
        if(s != -1) s += test.score();
      }
      if(s > 0) item.score(Scoring.avg(s, preds.length));
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
    if(pred instanceof ContextValue) return root;

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
          if(expr1 instanceof ContextValue) path = root;
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
          if(expr instanceof ContextValue) path = root;
          // [text() contains text 'x'] -> a/text() contains text 'x'
          if(expr instanceof Path) path = Path.get(info, root, expr).optimize(qc, scp);
          if(path != null) return new FTContains(path, ftexpr, cmp.info);
        }
      }
    }
    return this;
  }

  /**
   * Checks if the specified expression returns an empty sequence or a deterministic numeric value.
   * @param expr expression
   * @return result of check
   */
  protected static boolean num(final Expr expr) {
    final SeqType st = expr.seqType();
    return st.type.isNumber() && st.zeroOrOne() && !expr.has(Flag.CTX) && !expr.has(Flag.NDT);
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Expr pred : preds) {
      if(flag == Flag.POS && pred.seqType().mayBeNumber() || pred.has(flag)) return true;
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
