package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.Function;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and {@link Step}.
 *
 * @author BaseX Team 2005-20, BSD License
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
    type(cc.qc.focus.value, cc);

    final int pl = exprs.length;
    if(pl != 0) {
      cc.pushFocus(this);
      try {
        final QueryFocus focus = cc.qc.focus;
        final Value init = focus.value;
        for(int p = 0; p < pl; ++p) {
          try {
            exprs[p] = exprs[p].compile(cc);
          } catch(final QueryException ex) {
            // replace original expression with error
            exprs[p] = cc.error(ex, this);
          }
        }
        focus.value = init;
      } finally {
        cc.removeFocus();
      }
    }
    return optimize(cc);
  }

  /**
   * Assigns the expression type. Needs to be called before the predicates are compiled.
   * @param expr root expression
   * @param cc compilation context
   */
  protected abstract void type(Expr expr, CompileContext cc);

  /**
   * Assigns the sequence type and result size.
   * @param root root expression
   * @return whether expression may yield results
   */
  private boolean exprType(final Expr root) {
    long max = root.size();
    boolean exact = max != -1;
    if(!exact) max = Long.MAX_VALUE;

    // check positional predicates
    for(final Expr expr : exprs) {
      if(Function.LAST.is(expr)) {
        // use minimum of old value and 1
        max = Math.min(max, 1);
      } else if(expr instanceof ItrPos) {
        final ItrPos pos = (ItrPos) expr;
        // subtract start position. example: ...[1 to 2][2]  ->  2  ->  1
        if(max != Long.MAX_VALUE) max = Math.max(0, max - pos.min + 1);
        // use minimum of old value and range. example: ...[1 to 5]  ->  5
        max = Math.min(max, pos.max - pos.min + 1);
      } else {
        // resulting size will be unknown for any other filter
        exact = false;
      }
    }

    // choose exact result size; if not available, work with occurrence indicator
    final long size = exact || max == 0 ? max : -1;
    final Occ occ = max > 1 ? root.seqType().occ.union(Occ.ZERO) : Occ.ZERO_ONE;
    exprType.assign(root.seqType().type, occ, size);
    return max > 0;
  }

  /**
   * Checks if the specified item matches the predicates.
   * @param item item to be checked
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean match(final Item item, final QueryContext qc) throws QueryException {
    // set context value and position
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;
    qf.value = item;
    try {
      double s = qc.scoring ? 0 : -1;
      for(final Expr expr : exprs) {
        final Item test = expr.test(qc, info);
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
   * Optimizes all predicates.
   * @param cc compilation context
   * @param root root expression
   * @return {@code true} if expression may yield results
   * @throws QueryException query exception
   */
  protected final boolean optimize(final CompileContext cc, final Expr root) throws QueryException {
    cc.pushFocus(root);
    try {
      // optimize predicates
      final ExprList list = new ExprList(exprs.length);
      for(final Expr expr : exprs) {
        if(!optimize(expr, list, root, cc)) return false;
      }
      exprs = list.next();

      // remove duplicates, preserve entries after positional predicates
      boolean pos = false;
      for(final Expr expr : exprs) {
        if(!pos && list.contains(expr) && !expr.has(Flag.NDT)) {
          cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
        } else {
          list.add(expr);
          if(!pos) pos = mayBePositional(expr);
        }
      }
      exprs = list.finish();

      mergeEbv(false, false, cc);

    } finally {
      cc.removeFocus();
    }

    // check result size
    return exprType(root);
  }

  /**
   * Optimizes a predicate.
   * @param pred predicate
   * @param list expression list
   * @param root root expression
   * @param cc compilation context
   * @return {@code true} if expression may yield results
   * @throws QueryException query exception
   */
  private boolean optimize(final Expr pred, final ExprList list, final Expr root,
      final CompileContext cc) throws QueryException {

    // AND expression
    if(pred instanceof And && !pred.has(Flag.POS)) {
      // E[A and B]  ->  E[A][B]
      cc.info(OPTPRED_X, pred);
      for(final Expr expr : ((Arr) pred).exprs) {
        optimize(expr.seqType().mayBeNumber() ? cc.function(Function.BOOLEAN, info, expr) : expr,
          list, root, cc);
      }
      return true;
    }

    // comparisons
    Expr expr = pred;
    if(expr instanceof CmpG || expr instanceof CmpV) {
      // E[position() = 1]  ->  E[1]
      expr = ((Cmp) expr).optPred(root, cc);
    }

    // map operator
    final SeqType rst = root.seqType();
    if(expr instanceof SimpleMap) {
      // E[. ! ...]  ->  E[...]
      // E[E ! ...]  ->  E[...]
      final SimpleMap map = (SimpleMap) expr;
      final Expr[] mexprs = map.exprs;
      final Expr first = mexprs[0], second = mexprs[1];
      if((first instanceof ContextValue || root.equals(first) && root.isSimple() && rst.one()) &&
          !second.has(Flag.POS)) {
        final int ml = mexprs.length;
        expr = ml == 2 ? second : SimpleMap.get(map.info, Arrays.copyOfRange(mexprs, 1, ml));
      }
    }

    // paths
    if(expr instanceof Path) {
      // E[./...]  ->  E[...]
      // E[E/...]  ->  E[...]
      final Path path = (Path) expr;
      final Expr first = path.root;
      if(rst.type instanceof NodeType && (first instanceof ContextValue ||
          root.equals(first) && root.isSimple() && rst.one())) {
        expr = Path.get(path.info, null, path.steps);
      }
    }

    // E[exists(nodes)]  ->  E[nodes]
    expr = expr.simplifyFor(Simplify.EBV, cc);

    // inline root item (ignore nodes)
    // 1[. = 1]  ->  1[1 = 1]
    if(root instanceof Item && !(rst.type instanceof NodeType)) {
      final Expr inlined = expr.inline(null, root, cc);
      if(inlined != null) expr = inlined;
    }

    if(expr instanceof Path) {
      if(expr instanceof SingleIterPath) {
        final Step predStep = (Step) ((Path) expr).steps[0];
        if(predStep.axis == Axis.SELF && !predStep.mayBePositional()) {
          if(root instanceof Step && !mayBePositional()) {
            final Step rootStep = (Step) root;
            final Test test = rootStep.test.intersect(predStep.test);
            if(test != null) {
              // child::node()[self:*]  ->  child::*
              cc.info(OPTMERGE_X, predStep);
              rootStep.test = test;
              list.add(predStep.exprs);
              return true;
            }
          }
          if(predStep.test instanceof KindTest && predStep.exprs.length == 0 &&
              rst.type.instanceOf(predStep.test.type)) {
            // <a/>[self:*]  ->  <a/>
            cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
            return true;
          }
        }
      }
    }

    // context value
    if(expr instanceof ContextValue && rst.type instanceof NodeType) {
      // E[.]  ->  E
      cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      return true;
    }

    // evaluate values
    if(expr instanceof ANum) {
      expr = ItrPos.get(((ANum) expr).dbl(), info);
    } else if(expr instanceof Value) {
      expr = Bln.get(expr.ebv(cc.qc, info).bool(info));
    }

    // positional tests
    if(root instanceof Step && expr instanceof ItrPos) {
      // <a/>/.[1]  ->  <a/>/.[true()]
      // $child/..[2]  ->  $child/..[false()]
      final Axis axis = ((Step) root).axis;
      if(axis == Axis.SELF || axis == Axis.PARENT) expr = Bln.get(((ItrPos) expr).min == 1);
    }

    // cancel optimization, or skip or add predicate
    if(expr == Bln.FALSE) {
      return false;
    } else if(expr == Bln.TRUE) {
      cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
    } else {
      list.add(cc.replaceWith(pred, expr));
    }
    return true;
  }

  /**
   * Optimizes the predicates for boolean evaluation.
   * Drops solitary context values, flattens nested predicates.
   * @param root root expression
   * @param cc compilation context
   * @return expression
   * @throws QueryException query exception
   */
  public final Expr simplifyEbv(final Expr root, final CompileContext cc) throws QueryException {
    // only single predicate can be rewritten; root must yield nodes; no positional predicates
    final SeqType rst = root.seqType();
    final int el = exprs.length;
    if(!(rst.type instanceof NodeType) || el == 0 || mayBePositional()) return this;

    final Expr pred = exprs[el - 1];
    final QueryFunction<Expr, Expr> createRoot = r -> {
      return el == 1 ? r : Filter.get(info, r, Arrays.copyOfRange(exprs, 0, el - 1)).optimize(cc);
    };
    final QueryFunction<Expr, Expr> createExpr = e -> {
      return e instanceof ContextValue ? createRoot.apply(root) :
        e instanceof Path ? Path.get(info, createRoot.apply(root), e).optimize(cc) : null;
    };

    // rewrite to general comparison (right operand must not depend on context):
    // a[. = 'x']  ->  a = 'x'
    // a[text() = 'x']  ->  a/text() = 'x'
    if(pred instanceof CmpG) {
      // not applicable to value/node comparisons, as cardinality of expression might change
      final CmpG cmp = (CmpG) pred;
      final Expr expr1 = createExpr.apply(cmp.exprs[0]), expr2 = cmp.exprs[1];
      // right operand must not depend on context
      if(expr1 != null && !expr2.has(Flag.CTX)) {
        return new CmpG(expr1, expr2, cmp.op, cmp.coll, cmp.sc, cmp.info).optimize(cc);
      }
    }

    // rewrite to contains text expression (right operand must not depend on context):
    // a[. contains text 'x']  ->  a contains text 'x'
    // a[text() contains text 'x']  ->  a/text() contains text 'x'
    if(pred instanceof FTContains) {
      final FTContains cmp = (FTContains) pred;
      final Expr expr = createExpr.apply(cmp.expr);
      final FTExpr ftexpr = cmp.ftexpr;
      if(expr != null && !ftexpr.has(Flag.CTX)) {
        return new FTContains(expr, ftexpr, cmp.info).optimize(cc);
      }
    }

    // rewrite to path: root[path]  ->  root/path
    final Expr expr = createExpr.apply(pred);
    if(expr != null) return expr;

    // rewrite to simple map: $node[string()]  ->  $node ! string()
    if(rst.zeroOrOne()) return SimpleMap.get(info, createRoot.apply(root), pred).optimize(cc);

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
   * Checks if at least one of the predicates may be positional.
   * @return result of check
   */
  public boolean mayBePositional() {
    return mayBePositional(exprs);
  }

  /**
   * Checks if some of the specified expressions may be positional.
   * @param exprs expressions
   * @return result of check
   */
  protected static boolean mayBePositional(final Expr[] exprs) {
    for(final Expr expr : exprs) {
      if(mayBePositional(expr)) return true;
    }
    return false;
  }

  /**
   * Checks if the specified expression may be positional.
   * @param expr expression
   * @return result of check
   */
  protected static boolean mayBePositional(final Expr expr) {
    return expr.seqType().mayBeNumber() || expr.has(Flag.POS);
  }

  @Override
  public boolean inlineable(final Var var) {
    for(final Expr expr : exprs) {
      if(expr.uses(var)) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr expr : exprs) sb.append('[').append(expr).append(']');
    return sb.toString();
  }
}
