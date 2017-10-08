package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and {@link Step}.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Preds extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs predicates
   */
  protected Preds(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final QueryFocus focus = cc.qc.focus;
    final Value init = focus.value;
    try {
      final int pl = exprs.length;
      for(int p = 0; p < pl; ++p) {
        try {
          exprs[p] = exprs[p].compile(cc);
        } catch(final QueryException ex) {
          // replace original expression with error
          exprs[p] = cc.error(ex, this);
        }
      }
    } finally {
      focus.value = init;
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // number of predicates may change in loop
    for(int e = 0; e < exprs.length; e++) {
      Expr expr = exprs[e].optimizeEbv(cc);
      exprs[e] = expr;

      if(expr instanceof CmpG || expr instanceof CmpV) {
        final Cmp cmp = (Cmp) expr;
        final Expr e1 = cmp.exprs[0], e2 = cmp.exprs[1];
        if(e1.isFunction(Function.POSITION)) {
          // position() = last()  ->  last()
          // position() = $n (xs:numeric)  ->  $n
          if(num(e2)) {
            if(cmp instanceof CmpG && ((CmpG) cmp).op == OpG.EQ ||
               cmp instanceof CmpV && ((CmpV) cmp).op == OpV.EQ) {
              cc.info(OPTSIMPLE_X, expr);
              exprs[e] = e2;
            }
          }
        }
      } else if(expr instanceof And) {
        if(!expr.has(Flag.POS)) {
          // replace AND expression with predicates (don't swap position tests)
          cc.info(OPTPRED_X, expr);
          final Expr[] ands = ((Arr) expr).exprs;
          final int m = ands.length - 1;
          final ExprList el = new ExprList(exprs.length + m);
          for(final Expr ex : Arrays.asList(exprs).subList(0, e)) el.add(ex);
          for(final Expr and : ands) {
            // wrap test with boolean() if the result is numeric
            el.add(cc.function(Function.BOOLEAN, info, and).optimizeEbv(cc));
          }
          for(final Expr ex : Arrays.asList(exprs).subList(e + 1, exprs.length)) el.add(ex);
          exprs = el.finish();
        }
      } else if(expr instanceof ANum) {
        final ANum it = (ANum) expr;
        final long l = it.itr();
        // example: ....[1.2]
        if(l != it.dbl()) return cc.emptySeq(this);
        expr = ItrPos.get(l, info);
        // example: ....[0]
        if(!(expr instanceof ItrPos)) return cc.emptySeq(this);
        exprs[e] = expr;
      } else if(expr.isValue()) {
        // always false: ....[false()]
        if(!expr.ebv(cc.qc, info).bool(info)) return cc.emptySeq(this);
        // always true: ....[true()]
        cc.info(OPTREMOVE_X_X, description(), expr);
        exprs = Array.delete(exprs, e--);
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

    // check positional predicates
    for(final Expr pred : exprs) {
      if(pred.isFunction(Function.LAST)) {
        // use minimum of old value and 1
        max = Math.min(max, 1);
      } else if(pred instanceof ItrPos) {
        final ItrPos pos = (ItrPos) pred;
        // subtract start position. example: ...[1 to 2][2]  ->  (2 ->) 1
        if(max != Long.MAX_VALUE) max = Math.max(0, max - pos.min + 1);
        // use minimum of old value and range. example: ...[1 to 5] - >  5
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
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;
    qf.value = item;
    try {
      double s = qc.scoring ? 0 : -1;
      for(final Expr pred : exprs) {
        final Item test = pred.test(qc, info);
        if(test == null) return false;
        if(s != -1) s += test.score();
      }
      if(s > 0) item.score(Scoring.avg(s, exprs.length));
      return true;
    } finally {
      qf.value = cv;
    }
  }

  /**
   * Simplifies the predicates.
   * @param cc compilation context
   * @param root root expression
   */
  public final void simplify(final CompileContext cc, final Expr root) {
    final ExprList list = new ExprList();
    final SeqType st = root.seqType();
    for(final Expr expr : exprs) {
      Expr e = expr;
      if(expr instanceof ContextValue && st.instanceOf(SeqType.NOD_ZM)) {
        // E [ . ]  ->  E
        cc.info(OPTSIMPLE_X, this);
        continue;
      } else if(e instanceof SimpleMap) {
        // E [ . ! ... ]  ->  E [ ... ]
        // E [ E ! ... ]  ->  E [ ... ]
        final SimpleMap map = (SimpleMap) e;
        final Expr first = map.exprs[0];
        if(first instanceof ContextValue && !map.has(Flag.POS) ||
            root.equals(first) && root.isSimple() && st.one()) {
          final int ml = map.exprs.length;
          final ExprList el = new ExprList(ml - 1);
          for(int m = 1; m < ml; m++) el.add(map.exprs[m]);
          e = SimpleMap.get(map.info, el.finish());
        }
      } else if(e instanceof Path) {
        // E [ . / ... ]  ->  E [ ... ]
        // E [ E / ... ]  ->  E [ ... ]
        final Path path = (Path) e;
        final Expr first = path.root;
        if(st.type instanceof NodeType && (first instanceof ContextValue && !path.has(Flag.POS) ||
            root.equals(first) && root.isSimple() && st.one())) {
          e = Path.get(path.info, null, path.steps);
        }
      }
      list.add(cc.replaceWith(expr, e));
    }
    exprs = list.finish();
  }

  /**
   * Optimizes the predicates for boolean evaluation.
   * Drops solitary context values, flattens nested predicates.
   * @param cc compilation context
   * @param root root expression
   * @return expression
   * @throws QueryException query exception
   */
  public final Expr optimizeEbv(final Expr root, final CompileContext cc) throws QueryException {
    // only single predicate can be rewritten; root expression must yield nodes
    if(exprs.length != 1 || !(root.seqType().type instanceof NodeType)) return this;

    // skip positional predicates
    final Expr pred = exprs[0];
    if(pred.seqType().mayBeNumber()) return this;

    // a[b]  ->  a/b
    if(pred instanceof Path) return Path.get(info, root, pred).optimize(cc);

    if(pred instanceof CmpG) {
      // not applicable to value/node comparisons, as cardinality of expression might change
      final CmpG cmp = (CmpG) pred;
      final Expr expr = cmp.exprs[0], expr2 = cmp.exprs[1];
      // right operand must not depend on context
      if(!expr2.has(Flag.CTX)) {
        Expr expr1 = null;
        // a[. = 'x']  ->  a = 'x'
        if(expr instanceof ContextValue) expr1 = root;
        // a[text() = 'x']  ->  a/text() = 'x'
        if(expr instanceof Path) expr1 = Path.get(info, root, expr).optimize(cc);
        if(expr1 != null) return new CmpG(expr1, expr2, cmp.op, cmp.coll, cmp.sc, cmp.info);
      }
    }

    if(pred instanceof FTContains) {
      final FTContains cmp = (FTContains) pred;
      final FTExpr ftexpr = cmp.ftexpr;
      // right operand must not depend on context
      if(!ftexpr.has(Flag.CTX)) {
        final Expr expr = cmp.expr;
        Expr expr1 = null;
        // a[. contains text 'x']  ->  a contains text 'x'
        if(expr instanceof ContextValue) expr1 = root;
        // a[text() contains text 'x']  ->  a/text() contains text 'x'
        if(expr instanceof Path) expr1 = Path.get(info, root, expr).optimize(cc);

        if(expr1 != null) return new FTContains(expr1, ftexpr, cmp.info);
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
    return st.type.isNumber() && st.zeroOrOne() && expr.isSimple();
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Expr pred : exprs) {
      if(flag == Flag.POS && pred.seqType().mayBeNumber()) return true;
    }
    return super.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    for(final Expr p : exprs) if(p.uses(var)) return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr e : exprs) sb.append('[').append(e).append(']');
    return sb.toString();
  }
}
