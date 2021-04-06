package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
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
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param exprs predicate expressions
   */
  protected Filter(final InputInfo info, final Expr root, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
    this.root = root;
  }

  /**
   * Creates a new, optimized filter expression, or the root expression if no predicates exist.
   * @param cc compilation context
   * @param ii input info
   * @param root root expression
   * @param preds predicate expressions
   * @return filter root, path or filter expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo ii, final Expr root,
      final Expr... preds) throws QueryException {
    return preds.length == 0 ? root : new CachedFilter(ii, root, preds).optimize(cc);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(root);
    super.checkUp();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    root = root.compile(cc);
    return super.compile(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // flatten nested filters
    if(root instanceof Filter) {
      final Filter filter = (Filter) root;
      root = filter.root;
      exprs = new ExprList().add(filter.exprs).add(exprs).finish();
    }

    // return empty root
    final SeqType st = root.seqType();
    if(st.zero()) return cc.replaceWith(this, root);

    // simplify predicates
    if(simplify(cc, root)) return cc.emptySeq(this);
    // no predicates: return root
    if(exprs.length == 0) return root;

    // no positional access..
    if(!mayBePositional()) {
      // convert to axis path: (//x)[text() = 'a']  ->  //x[text() = 'a']
      if(root instanceof AxisPath) return ((AxisPath) root).addPredicates(cc, exprs);

      // rewrite filter with document nodes to path to possibly enable index rewritings
      // example: db:open('db')[.//text() = 'x']  ->  db:open('db')/.[.//text() = 'x']
      if(st.type == NodeType.DOCUMENT_NODE && root.ddo()) {
        final Expr step = Step.get(cc, root, info, exprs);
        return cc.replaceWith(this, Path.get(cc, info, root, step));
      }

      // rewrite independent deterministic single filter to if expression:
      // example: (1 to 10)[$boolean]  ->  if($boolean) then (1 to 10) else ()
      final Expr expr = exprs[0];
      if(exprs.length == 1 && expr.isSimple() && !expr.seqType().mayBeNumber()) {
        final Expr iff = new If(info, expr, root).optimize(cc);
        return cc.replaceWith(this, iff);
      }

      // unroll filters with few items
      // example: (1, 2)[. = 1]  ->  1[. = 1], 2[. = 1]
      final long size = root.size(), limit = cc.qc.context.options.get(MainOptions.UNROLLLIMIT);
      if(root instanceof Seq && size <= limit) {
        cc.info(QueryText.OPTUNROLL_X, this);
        final ExprList results = new ExprList((int) size);
        for(final Item item : (Value) root) {
          results.add(Filter.get(cc, info, item, results.size() == size - 1 ? exprs :
            Arr.copyAll(cc, new IntObjMap<>(), exprs)));
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
    final QueryFunction<Expr, Expr> prepare = ex ->
      preds.isEmpty() ? ex : get(cc, info, ex, preds.next());
    for(final Expr pred : exprs) {
      Expr ex = null;
      if(LAST.is(pred)) {
        // rewrite positional predicate to util:last
        ex = cc.function(_UTIL_LAST, info, prepare.apply(expr));
      } else if(pred instanceof ItrPos) {
        final ItrPos pos = (ItrPos) pred;
        if(pos.min != pos.max) {
          // expr[min..max]  ->  util:range(expr, min, max)
          ex = cc.function(_UTIL_RANGE, info, prepare.apply(expr),
              Int.get(pos.min), Int.get(pos.max));
        } else if(pos.min == 1) {
          // expr[1]  ->  head(expr)
          ex = cc.function(HEAD, info, prepare.apply(expr));
        } else {
          // expr[pos]  ->  util:item(expr, pos)
          ex = cc.function(_UTIL_ITEM, info, prepare.apply(expr), Int.get(pos.min));
        }
      } else if(pred instanceof Pos) {
        final Pos pos = (Pos) pred;
        if(pos.exact()) {
          // expr[pos]  ->  util:item(expr, pos.min)
          ex = cc.function(_UTIL_ITEM, info, prepare.apply(expr), pos.exprs[0]);
        } else {
          // expr[min..max]  ->  util:range(expr, pos.min, pos.max)
          ex = cc.function(_UTIL_RANGE, info, prepare.apply(expr), pos.exprs[0], pos.exprs[1]);
        }
      } else if(numeric(pred)) {
        /* - rewrite positional predicate to util:item
         *   expr[pos]  ->  util:item(expr, pos)
         * - only choose deterministic and context-independent offsets. illegal:
         *   (1 to 10)[random:integer(10)]  or  (1 to 10)[.]  or  $a[$a[.]] */
        if(pred.seqType().one()) {
          ex = cc.function(_UTIL_ITEM, info, prepare.apply(expr), pred);
        }
      } else if(pred instanceof Cmp) {
        // rewrite positional predicate to fn:remove
        final Cmp cmp = (Cmp) pred;
        final OpV opV = cmp.opV();
        if(cmp.positional() && opV != null) {
          final Expr e = cmp.exprs[1];
          if((opV == OpV.LT || opV == OpV.NE) && LAST.is(e)) {
            // expr[position() < last()]  ->  util:init(expr)
            ex = cc.function(_UTIL_INIT, info, prepare.apply(expr));
          } else if(opV == OpV.NE && e.seqType().instanceOf(SeqType.INTEGER_O) && e.isSimple()) {
            // expr[position() != INT]  ->  remove(expr, INT)
            ex = cc.function(REMOVE, info, prepare.apply(expr), e);
          } else if(opV == OpV.EQ && e instanceof Range) {
            final Expr arg1 = e.arg(0), arg2 = e.arg(1);
            if(LAST.is(arg2) && arg1.seqType().instanceOf(SeqType.INTEGER_O) && arg1.isSimple()) {
              // expr[position() = INT to last()]
              ex = cc.function(SUBSEQUENCE, info, prepare.apply(expr), arg1);
            } else if(arg1 == Int.ONE && arg2 instanceof Arith) {
              // expr[position() = 1 to last() - 1]
              final Expr arth1 = arg2.arg(0), arth2 = arg2.arg(1);
              if(LAST.is(arth1) && ((Arith) arg2).calc == Calc.MINUS && arth2 == Int.ONE) {
                ex = cc.function(_UTIL_INIT, info, prepare.apply(expr));
              }
            }
          }
        }
      } else if(pred instanceof Arith && LAST.is(pred.arg(0)) && preds.isEmpty()) {
        // expr[last() - 1]  ->  util:item(expr, count(expr) - 1)
        final long es = expr.size();
        if(es != -1) ex = cc.function(_UTIL_ITEM, info, prepare.apply(expr),
            new Arith(info, Int.get(es), pred.arg(1), ((Arith) pred).calc).optimize(cc));
      }
      // replace temporary result expression or add predicate to temporary list
      if(ex != null) {
        expr = ex;
        opt = true;
      } else {
        preds.add(pred);
      }
    }
    // return optimized expression
    if(opt) return cc.replaceWith(this, prepare.apply(expr));

    // choose best filter implementation
    return copyType(
      exprs.length == 1 && exprs[0].isSimple() ? new SimpleFilter(info, root, exprs) :
      new CachedFilter(info, root, exprs));
  }

  @Override
  protected final void type(final Expr expr) {
    exprType.assign(root.seqType().union(Occ.ZERO));
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
    exprs = new ExprList(exprs.length + 1).add(exprs).add(pred).finish();
    return copyType(get(cc, info, root, exprs));
  }

  /**
   * Rewrites a filter expression for count operations.
   * @param cc compilation context
   * @return optimized or original expression.
   * @throws QueryException query exception
   */
  public final Expr simplifyCount(final CompileContext cc) throws QueryException {
    if(exprs.length != 1) return this;

    // exists($nodes[@attr])  ->  exists($nodes ! @attr)
    final Expr pred = exprs[0];
    if(pred.seqType().instanceOf(SeqType.NODE_ZO)) return SimpleMap.get(cc, info, root, pred);

    // count($seq[. = 'x'])  ->  count(index-of($seq, 'x'))
    final Function<Expr, Integer> type = expr -> {
      final Type t = expr.seqType().type;
      return t.isStringOrUntyped() ? 1 : t.isNumber() ? 2 : 0;
    };
    final int rtype = type.apply(root);
    if(rtype != 0 && pred instanceof CmpG && ((CmpG) pred).opV() == OpV.EQ) {
      final Expr op2 = pred.arg(1);
      if(pred.arg(0) instanceof ContextValue && op2.seqType().one() && type.apply(op2) == rtype) {
        return cc.function(INDEX_OF, info, root, op2);
      }
    }
    return this;
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    if(mode == Simplify.EBV || mode == Simplify.PREDICATE) {
      final Expr expr = flattenEbv(root, true, cc);
      if(expr != this) return cc.simplify(this, expr);
    } else if(mode == Simplify.DISTINCT && !mayBePositional()) {
      final Expr expr = root.simplifyFor(mode, cc);
      if(expr != root) return Filter.get(cc, info, expr, exprs);
    }
    return super.simplifyFor(mode, cc);
  }

  @Override
  public final boolean has(final Flag... flags) {
    if(root.has(flags)) return true;
    final Flag[] flgs = Flag.POS.remove(Flag.CTX.remove(flags));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    return root.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public final VarUsage count(final Var var) {
    // context reference check: only consider root
    final VarUsage inRoot = root.count(var);
    if(var == null) return inRoot;

    final VarUsage inPreds = super.count(var);
    return inPreds == VarUsage.NEVER ? inRoot :
      root.seqType().zeroOrOne() ? inRoot.plus(inPreds) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    final Expr inlined = root.inline(ic);
    boolean changed = inlined != null;
    if(changed) root = inlined;

    // do not inline context reference in predicates
    changed |= ic.var != null && ic.cc.ok(root, () -> ic.inline(exprs));

    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public final Data data() {
    return root.data();
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    for(final Expr expr : exprs) {
      visitor.enterFocus();
      if(!expr.accept(visitor)) return false;
      visitor.exitFocus();
    }
    return root.accept(visitor);
  }

  @Override
  public boolean ddo() {
    return root.ddo();
  }

  @Override
  public final int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size + root.exprSize();
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof Filter && root.equals(((Filter) obj).root) &&
        super.equals(obj);
  }

  @Override
  public final void plan(final QueryPlan plan) {
    plan.add(plan.create(this), root, exprs);
  }

  @Override
  public final void plan(final QueryString qs) {
    qs.token(root);
    super.plan(qs);
  }
}
