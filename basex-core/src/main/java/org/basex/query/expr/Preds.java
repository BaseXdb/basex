package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
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
   * @param seqType sequence type
   * @param exprs predicates
   */
  protected Preds(final InputInfo info, final SeqType seqType, final Expr... exprs) {
    super(info, seqType, exprs);
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
    final int es = exprs.length;
    final ExprList list = new ExprList(es);
    boolean pos = false;
    for(int e = 0; e < es; e++) {
      Expr expr = exprs[e].optimizeEbv(cc);
      if(expr instanceof CmpG || expr instanceof CmpV) {
        final Cmp cmp = (Cmp) expr;
        final Expr e1 = cmp.exprs[0], e2 = cmp.exprs[1];
        if(e1.isFunction(Function.POSITION)) {
          // position() = last()  ->  last()
          // position() = $n (xs:numeric)  ->  $n
          if(numeric(e2)) {
            if(cmp instanceof CmpG && ((CmpG) cmp).op == OpG.EQ ||
               cmp instanceof CmpV && ((CmpV) cmp).op == OpV.EQ) {
              expr = cc.replaceWith(expr, e2);
            }
          }
        }
      } else if(expr instanceof And) {
        if(!expr.has(Flag.POS)) {
          // replace AND expression with predicates (don't rewrite position tests)
          cc.info(OPTPRED_X, expr);
          final Expr[] ands = ((Arr) expr).exprs;
          final int m = ands.length;
          for(int a = 0; a < m; a++) {
            // wrap test with boolean() if the result is numeric
            expr = ands[a];
            if(expr.seqType().mayBeNumber()) expr = cc.function(Function.BOOLEAN, info, expr);
            if(a + 1 < m) pos = add(expr, list, pos, cc);
          }
        }
      } else if(expr instanceof ANum) {
        final ANum it = (ANum) expr;
        final long l = it.itr();
        // example: ....[1.2]
        if(l != it.dbl()) return cc.emptySeq(this);
        expr = ItrPos.get(l, info);
        // example: ....[0]
        if(!(expr instanceof ItrPos)) return cc.emptySeq(this);
      } else if(expr instanceof Value) {
        expr = Bln.get(expr.ebv(cc.qc, info).bool(info));
      }

      // predicate will not yield any results
      if(expr == Bln.FALSE) return cc.emptySeq(this);
      // skip expression yielding true
      if(exprs[e] != expr) cc.replaceWith(exprs[e], expr);
      pos = add(expr, list, pos, cc);
    }
    exprs = list.finish();
    return this;
  }

  /**
   * Adds an expression to the new expression list.
   * @param expr expression
   * @param list expression list
   * @param pos positional access flag
   * @param cc compilation context
   * @return this, or a previous expression, uses positional access
   */
  private boolean add(final Expr expr, final ExprList list, final boolean pos,
      final CompileContext cc) {

    final boolean ps = pos || expr.seqType().mayBeNumber() || expr.has(Flag.POS);
    if(expr == Bln.TRUE) {
      cc.info(OPTREMOVE_X_X, expr, description());
    } else if(ps || !list.contains(expr) || expr.has(Flag.NDT)) {
      list.add(expr);
    }
    return ps;
  }

  /**
   * Assigns the sequence type and {@link #size} value.
   * @param st sequence type of input
   * @param s size of input ({@code -1} if unknown)
   * @return if predicate will yield any results
   */
  protected final boolean exprType(final SeqType st, final long s) {
    boolean exact = s != -1;
    long max = exact ? s : Long.MAX_VALUE;

    // check positional predicates
    for(final Expr expr : exprs) {
      if(expr.isFunction(Function.LAST)) {
        // use minimum of old value and 1
        max = Math.min(max, 1);
      } else if(expr instanceof ItrPos) {
        final ItrPos pos = (ItrPos) expr;
        // subtract start position. example: ...[1 to 2][2] -> 2 -> 1
        if(max != Long.MAX_VALUE) max = Math.max(0, max - pos.min + 1);
        // use minimum of old value and range. example: ...[1 to 5] ->  5
        max = Math.min(max, pos.max - pos.min + 1);
      } else {
        // resulting size will be unknown for any other filter
        exact = false;
      }
    }

    if(exact || max == 0) {
      exprType.assign(st.type, max);
    } else {
      // we only know if there will be at most 1 result
      exprType.assign(st.type, max == 1 ? Occ.ZERO_ONE : Occ.ZERO_MORE);
    }
    return max != 0;
  }

  /**
   * Checks if the predicates are successful for the specified item.
   * @param it item to be checked
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean preds(final Item it, final QueryContext qc) throws QueryException {
    // set context value and position
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;
    qf.value = it;
    try {
      double s = qc.scoring ? 0 : -1;
      for(final Expr expr : exprs) {
        final Item test = expr.test(qc, info);
        if(test == null) return false;
        if(s != -1) s += test.score();
      }
      if(s > 0) it.score(Scoring.avg(s, exprs.length));
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
      Expr ex = expr;
     if(expr instanceof ContextValue && st.instanceOf(SeqType.NOD_ZM)) {
        // E [ . ]  ->  E
        cc.info(OPTREMOVE_X_X, ex, description());
        continue;
      } else if(ex instanceof SimpleMap) {
        // E [ . ! ... ]  ->  E [ ... ]
        // E [ E ! ... ]  ->  E [ ... ]
        final SimpleMap map = (SimpleMap) ex;
        final Expr first = map.exprs[0], second = map.exprs[1];
        if(!second.has(Flag.POS) && (first instanceof ContextValue ||
            root.equals(first) && root.isSimple() && st.one())) {
          final int ml = map.exprs.length;
          final ExprList el = new ExprList(ml - 1);
          for(int m = 1; m < ml; m++) el.add(map.exprs[m]);
          ex = SimpleMap.get(map.info, el.finish());
        }
      } else if(ex instanceof Path) {
        // E [ . / ... ]  ->  E [ ... ]
        // E [ E / ... ]  ->  E [ ... ]
        final Path path = (Path) ex;
        final Expr first = path.root;
        if(st.type instanceof NodeType && (first instanceof ContextValue ||
            root.equals(first) && root.isSimple() && st.one())) {
          ex = Path.get(path.info, null, path.steps);
        }
      }
      list.add(cc.replaceWith(expr, ex));
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
  protected static boolean numeric(final Expr expr) {
    final SeqType st = expr.seqType();
    return st.type.isNumber() && st.zeroOrOne() && expr.isSimple();
  }

  /**
   * Checks if at least one of the predicates contains a positional access.
   * @return result of check
   */
  public boolean positional() {
    return positional(exprs);
  }

  /**
   * Checks if some of the specified expressions are positional.
   * @param exprs expressions
   * @return result of check
   */
  public static boolean positional(final Expr[] exprs) {
    for(final Expr expr : exprs) {
      if(expr.seqType().mayBeNumber() || expr.has(Flag.POS)) return true;
    }
    return false;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Expr expr : exprs) if(expr.uses(var)) return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr expr : exprs) sb.append('[').append(expr).append(']');
    return sb.toString();
  }
}
