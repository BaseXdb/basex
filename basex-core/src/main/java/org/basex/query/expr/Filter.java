package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicate expressions
   */
  protected Filter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, SeqType.ITEM_ZM, preds);
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
      // convert to axis path: .[text()]  ->  self::node()[text()]
      if(root instanceof ContextValue && st.type instanceof NodeType) {
        return Path.get(cc, info, null, Step.get(cc, root, info, exprs));
      }
      // convert to axis path: (//x)[text() = 'a']  ->  //x[text() = 'a']
      if(root instanceof AxisPath) return ((AxisPath) root).addPredicates(cc, exprs);

      // rewrite filter with document nodes to path to possibly enable index rewritings
      // example: db:get('db')[.//text() = 'x']  ->  db:get('db')/.[.//text() = 'x']
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
      final ExprList unroll = cc.unroll(root, false);
      if(unroll != null) {
        final long last = root.size() - 1;
        final ExprList results = new ExprList(unroll.size());
        for(final Expr ex : unroll) {
          results.add(get(cc, info, ex, results.size() == last ? exprs :
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
          // E[min .. max]  ->  util:range(E, min, max)
          ex = cc.function(_UTIL_RANGE, info, prepare.apply(expr),
              Int.get(pos.min), Int.get(pos.max));
        } else if(pos.min == 1) {
          // E[1]  ->  head(E)
          ex = cc.function(HEAD, info, prepare.apply(expr));
        } else {
          // E[pos]  ->  util:item(E, pos)
          ex = cc.function(_UTIL_ITEM, info, prepare.apply(expr), Int.get(pos.min));
        }
      } else if(pred instanceof Pos) {
        final Pos pos = (Pos) pred;
        if(pos.exact()) {
          // E[pos]  ->  util:item(E, pos.min)
          ex = cc.function(_UTIL_ITEM, info, prepare.apply(expr), pos.exprs[0]);
        } else {
          // E[min .. max]  ->  util:range(E, pos.min, pos.max)
          ex = cc.function(_UTIL_RANGE, info, prepare.apply(expr), pos.exprs[0], pos.exprs[1]);
        }
      } else if(pred instanceof RangePos) {
        final Expr range = ((RangePos) pred).expr, arg1 = range.arg(0), arg2 = range.arg(1);
        if(arg1.seqType().instanceOf(SeqType.INTEGER_O) && arg1.isSimple() && LAST.is(arg2)) {
          // E[pos .. last()]  ->  util:range(E, pos)
          ex = cc.function(_UTIL_RANGE, info, prepare.apply(expr), arg1);
        } else if(arg1 instanceof Int && arg2 instanceof Arith) {
          // E[1 .. last() - 1]  ->  util:init(E)
          if(((Int) arg1).itr() <= 1 && LAST.is(arg2.arg(0)) &&
              ((Arith) arg2).calc == Calc.MINUS && arg2.arg(1) == Int.ONE) {
            ex = cc.function(_UTIL_INIT, info, prepare.apply(expr));
          }
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
            // E[position() < last()]  ->  util:init(E)
            ex = cc.function(_UTIL_INIT, info, prepare.apply(expr));
          } else if(opV == OpV.NE && e.seqType().instanceOf(SeqType.INTEGER_O) && e.isSimple()) {
            // E[position() != pos]  ->  remove(E, pos)
            ex = cc.function(REMOVE, info, prepare.apply(expr), e);
          }
        }
      } else if(pred instanceof Arith && LAST.is(pred.arg(0)) && preds.isEmpty()) {
        // E[last() - 1]  ->  util:item(E, size - 1)
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
    exprs = new ExprList(exprs.length + 1).add(exprs).add(pred).finish();
    return copyType(get(cc, info, root, exprs));
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      expr = flattenEbv(root, true, cc);
    } else if(mode == Simplify.DISTINCT && !mayBePositional()) {
      final Expr ex = root.simplifyFor(mode, cc);
      if(ex != root) expr = get(cc, info, ex, exprs);
    } else if (mode == Simplify.COUNT && exprs.length == 1) {
      // $nodes[@attr]  ->  $nodes ! @attr
      final Expr pred = exprs[0];
      if(pred.seqType().instanceOf(SeqType.NODE_ZO)) expr = SimpleMap.get(cc, info, root, pred);
    }
    return expr != this ? expr.simplifyFor(mode, cc) : super.simplifyFor(mode, cc);
  }

  @Override
  public final boolean has(final Flag... flags) {
    if(Flag.FCS.in(flags) || root.has(flags)) return true;
    final Flag[] flgs = Flag.FCS.remove(Flag.POS.remove(Flag.CTX.remove(flags)));
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
  public final void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), root, exprs);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(root);
    super.toString(qs);
  }
}
