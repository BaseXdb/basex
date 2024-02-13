package org.basex.query.expr;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract filter expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class Filter extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
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

    // optimize predicates
    if(optimize(cc, root)) return cc.emptySeq(this);
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
    final QueryFunction<Expr, Expr> add = e -> preds.isEmpty() ? e : get(cc, info, e, preds.next());
    for(final Expr pred : exprs) {
      Expr ex = null;
      if(pred instanceof IntPos) {
        // E[pos: MIN, MAX]  ->  util:range(E, MIN, MAX)
        final IntPos pos = (IntPos) pred;
        ex = cc.function(_UTIL_RANGE, info, add.apply(expr), Int.get(pos.min), Int.get(pos.max));
      } else if(pred instanceof SimplePos) {
        if(((SimplePos) pred).exact()) {
          // E[pos: POS]  ->  items-at(E, POS)
          ex = cc.function(ITEMS_AT, info, add.apply(expr), pred.arg(0));
        } else {
          // E[pos: MIN, MAX]  ->  util:range(E, MIN, MAX)
          ex = cc.function(_UTIL_RANGE, info, add.apply(expr), pred.arg(0), pred.arg(1));
        }
      } else if(pred instanceof Pos) {
        final Expr pos = ((Pos) pred).expr;
        if(pos instanceof Range) {
          final Expr arg1 = pos.arg(0), arg2 = pos.arg(1);
          if(arg1.seqType().instanceOf(SeqType.INTEGER_O) && arg1.isSimple() && LAST.is(arg2)) {
            // E[pos: POS to last()]  ->  util:range(E, POS)
            ex = cc.function(_UTIL_RANGE, info, add.apply(expr), arg1);
          } else if(arg1 == Int.ONE && arg2 instanceof Arith && LAST.is(arg2.arg(0)) &&
              ((Arith) arg2).calc == Calc.SUBTRACT && arg2.arg(1) == Int.ONE) {
            // E[pos: 1 to last() - 1]  ->  trunk(E)
            ex = cc.function(TRUNK, info, add.apply(expr));
          }
        } else if(LAST.is(pos)) {
          // E[pos: last()]  ->  foot(E)
          ex = cc.function(FOOT, info, add.apply(expr));
        } else if(pos instanceof Arith && preds.isEmpty() && LAST.is(pos.arg(0))) {
          final long es = expr.size();
          // E[pos: last() - 1]  ->  items-at(E, size - 1)
          if(es != -1) ex = cc.function(ITEMS_AT, info, add.apply(expr),
              new Arith(info, Int.get(es), pos.arg(1), ((Arith) pos).calc).optimize(cc));
        } else if(pos.isSimple()) {
          // E[pos: RANGE]  ->  items-at(E, RANGE))
          ex = cc.function(ITEMS_AT, info, add.apply(expr), pos);
        }
      } else if(pred instanceof MixedPos) {
        // E[pos: POS1, POS2, ...]  ->  items-at(E, sort(distinct-values((POS1, POS2, ...)))
        final Expr pos = ((MixedPos) pred).expr;
        ex = cc.function(ITEMS_AT, info, add.apply(expr), pos instanceof Value ? pos :
          cc.function(SORT, info, cc.function(DISTINCT_VALUES, info, pos)));
      } else if(pred instanceof CmpG) {
        final Expr op1 = pred.arg(0), op2 = pred.arg(1);
        if(POSITION.is(op1) && ((Cmp) pred).opV() == OpV.NE &&
            op2.seqType().instanceOf(SeqType.INTEGER_O) && op2.isSimple()) {
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
    exprs = new ExprList(exprs.length + 1).add(exprs).add(pred).finish();
    return copyType(get(cc, info, root, exprs));
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
