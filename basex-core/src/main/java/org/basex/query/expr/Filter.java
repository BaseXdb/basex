package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract value filter expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends AFilter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds predicate expressions
   */
  protected Filter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, SeqType.ITEM_ZM, root, preds);
  }

  /**
   * Creates a new, optimized filter expression, or the root expression if no predicates exist.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds predicate expressions
   * @return filter root, path or filter expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr root,
      final Expr... preds) throws QueryException {
    return preds.length == 0 ? root : new CachedFilter(info, root, preds).optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // flatten nested filters
    if(root instanceof final Filter filter) {
      root = filter.root;
      exprs = ExprList.concat(filter.exprs, exprs);
    }

    // return empty root
    if(root.seqType().zero()) return cc.replaceWith(this, root);

    // optimize predicates
    if(optimize(cc, root)) return cc.emptySeq(this);
    // no predicates: return root
    if(exprs.length == 0) return root;

    // no positional access...
    if(!mayBePositional()) {
      // convert to axis path: .[text()]  ->  self::node()[text()]
      if(root instanceof ContextValue && root.ddo()) {
        return Path.get(cc, info, null, Step.self(cc, root, info, exprs));
      }
      // convert to axis path: (//x)[text() = 'a']  ->  //x[text() = 'a']
      if(root instanceof final AxisPath path) return path.addPredicates(cc, exprs);

      // rewrite filter with document nodes to path to possibly enable index rewritings
      // example: db:get('db')[.//text() = 'x']  ->  db:get('db')/.[.//text() = 'x']
      if(root.seqType().type == NodeType.DOCUMENT_NODE && root.ddo()) {
        final Expr step = Step.self(cc, root, info, exprs);
        return cc.replaceWith(this, Path.get(cc, info, root, step));
      }

      // rewrite independent deterministic single filter to 'if' expression:
      // example: (1 to 10)[$boolean]  ->  if($boolean) then (1 to 10) else ()
      final Expr expr = exprs[0];
      if(exprs.length == 1 && expr.isSimple() && !expr.seqType().mayBeNumber()) {
        final Expr iff = new If(info, expr, root).optimize(cc);
        return cc.replaceWith(this, iff);
      }

      // unroll filters with few items
      // example: (1, 2)[. = 1]  ->  1[. = 1], 2[. = 1]
      final ExprList unroll = cc.unroll(root, false);
      if(unroll != null) {
        final long last = root.size() - 1;
        final ExprList results = new ExprList(unroll.size());
        for(final Expr ex : unroll) {
          results.add(get(cc, info, ex, results.size() == last ? exprs :
            Arr.copyAll(cc, new IntObjectMap<>(), exprs)));
        }
        return List.get(cc, info, results.finish());
      }

      // otherwise, return iterative filter
      return copyType(new IterFilter(info, root, exprs));
    }

    // rewrite positional predicates
    Expr expr = root;
    boolean opt = false;
    final ExprList preds = new ExprList(exprs.length);
    final QueryFunction<Expr, Expr> add = e -> preds.isEmpty() ? e : get(cc, info, e, preds.next());
    final Predicate<Expr> simpleInt = e -> e.seqType().eq(SeqType.INTEGER_O) && e.isSimple();
    for(final Expr pred : exprs) {
      Expr ex = null;
      if(pred instanceof final IntPos pos) {
        // E[pos: MIN, MAX]  ->  util:range(E, MIN, MAX)
        ex = cc.function(_UTIL_RANGE, info, add.apply(expr), Int.get(pos.min), Int.get(pos.max));
      } else if(pred instanceof final SimplePos pos) {
        if(pos.exact()) {
          // E[pos: POS]  ->  items-at(E, POS)
          ex = cc.function(ITEMS_AT, info, add.apply(expr), pred.arg(0));
        } else {
          // E[pos: MIN, MAX]  ->  util:range(E, MIN, MAX)
          ex = cc.function(_UTIL_RANGE, info, add.apply(expr), pred.arg(0), pred.arg(1));
        }
      } else if(pred instanceof final Pos pos) {
        final Expr posExpr = pos.expr;
        if(posExpr instanceof Range) {
          final Expr arg1 = posExpr.arg(0), arg2 = posExpr.arg(1);
          if(simpleInt.test(arg1) && LAST.is(arg2)) {
            // E[pos: INT to last()]  ->  util:range(E, INT)
            ex = cc.function(_UTIL_RANGE, info, add.apply(expr), arg1);
          } else if(arg1 == Int.ONE && arg2 instanceof final Arith arth2 && LAST.is(arth2.arg(0)) &&
              arth2.calc == Calc.SUBTRACT && arth2.arg(1) == Int.ONE) {
            // E[pos: 1 to last() - 1]  ->  trunk(E)
            ex = cc.function(TRUNK, info, add.apply(expr));
          } else if(arg1 instanceof final Arith arth1 && LAST.is(arth1.arg(0)) &&
              arth1.calc == Calc.SUBTRACT && simpleInt.test(arth1.arg(1)) && arg2 == Int.MAX) {
            // E[pos: last() - INT to MAX]  ->  reverse(subsequence(reverse(E), 1, INT + 1))
            ex = cc.function(REVERSE, info, cc.function(SUBSEQUENCE, info,
                cc.function(REVERSE, info, add.apply(expr)), Int.ONE,
                new Arith(info, arg1.arg(1), Int.ONE, Calc.ADD).optimize(cc)));
          }
        } else if(LAST.is(posExpr)) {
          // E[pos: last()]  ->  foot(E)
          ex = cc.function(FOOT, info, add.apply(expr));
        } else if(posExpr instanceof final Arith arth && LAST.is(arth.arg(0)) &&
            arth.calc == Calc.SUBTRACT && simpleInt.test(arth.arg(1))) {
          // E[pos: last() - INT]  ->  items-at(reverse(E), INT + 1)
          ex = cc.function(ITEMS_AT, info, cc.function(REVERSE, info, add.apply(expr)),
              new Arith(info, posExpr.arg(1), Int.ONE, Calc.ADD).optimize(cc));
        }
      } else if(pred instanceof final MixedPos pos) {
        final Expr posExpr = pos.expr;
        // Value instances are known to be sorted and duplicate-free (see Pos#get)
        final boolean sorted = posExpr instanceof Value;
        // E[pos: INT1, INT2, ...]  ->  items-at(E, INT1, INT2, ...)
        // E[pos: POSITIONS, ...]  ->  items-at(E, sort(distinct-values((POSITIONS)))
        ex = cc.function(ITEMS_AT, info, add.apply(expr), sorted ? posExpr :
          cc.function(SORT, info, cc.function(DISTINCT_VALUES, info, posExpr)), Bln.get(sorted));
      } else if(pred instanceof final CmpG cmp) {
        final Expr op1 = pred.arg(0), op2 = pred.arg(1);
        if(POSITION.is(op1) && cmp.opV() == OpV.NE &&
            op2.isSimple() && op2.seqType().instanceOf(SeqType.INTEGER_O)) {
          // E[position() != pos]  ->  remove(E, pos)
          ex = cc.function(REMOVE, info, add.apply(expr), op2);
        }
      }
      // replace temporary result expression or add predicate to temporary list
      if(ex != null) {
        expr = ex;
        opt = true;
      } else {
        preds.add(pred);
      }
    }
    // return optimized or filter expression
    return opt ? cc.replaceWith(this, add.apply(expr)) :
      copyType(new CachedFilter(info, root, exprs));
  }

  @Override
  protected final Expr type(final Expr expr) {
    exprType.assign(root.seqType().union(Occ.ZERO)).data(root);
    return root;
  }

  /**
   * Adds a predicate and returns the optimized expression.
   * This function is e.g. called by {@link For#addPredicate}.
   * @param cc compilation context
   * @param pred predicate to be added
   * @return new filter
   * @throws QueryException query exception
   */
  public final Expr addPredicate(final CompileContext cc, final Expr pred) throws QueryException {
    return copyType(get(cc, info, root, ExprList.concat(exprs, pred)));
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // E[a[. = 'x']]  ->  E[a = 'x']
      expr = flattenEbv(root, true, cc);
    } else if(mode == Simplify.DISTINCT && !mayBePositional()) {
      final Expr ex = root.simplifyFor(mode, cc);
      if(ex != root) expr = get(cc, info, ex, exprs);
    } else if(mode == Simplify.COUNT && exprs.length == 1 &&
        exprs[0].seqType().instanceOf(SeqType.NODE_ZO)) {
      // $nodes[@attr]  ->  $nodes ! @attr
      expr = SimpleMap.get(cc, info, root, exprs[0]);
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof final Filter fltr && root.equals(fltr.root) &&
        super.equals(obj);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(root);
    super.toString(qs);
  }
}
