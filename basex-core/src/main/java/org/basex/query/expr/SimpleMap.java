package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map operator.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class SimpleMap extends Mapping {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  SimpleMap(final InputInfo info, final Expr... exprs) {
    super(info, Types.ITEM_ZM, exprs);
  }

  @Override
  final boolean items() {
    return true;
  }

  /**
   * Creates a new, optimized map expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param exprs expressions
   * @return list, single expression or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr... exprs)
      throws QueryException {
    final int el = exprs.length;
    return el > 1 ? new CachedMap(info, exprs).optimize(cc) : el > 0 ? exprs[0] : Empty.VALUE;
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    Expr ex = flattenMaps(cc);
    if(ex == null) ex = mergePaths(cc);
    if(ex == null) ex = dropOps(cc);
    if(ex != null) return ex;
    final Expr[] merged = merge(cc);
    if(merged != null) return merged.length == 1 ? merged[0] : get(cc, info, merged);

    // choose best implementation
    boolean value = true, cached = false, dual = exprs.length == 2;
    final boolean dualiter = dual;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      final Expr expr = exprs[e];
      final SeqType st = expr.seqType();
      value = value && (st.one() || e == el - 1);
      cached = cached || e > 0 && expr.has(Flag.POS);
      dual = dual && (st.zeroOrOne() || e == 0);
    }
    return copyType(
      value ? new Pipeline(info, exprs) :
      cached ? new CachedMap(info, exprs) :
      dual ? new DualMap(info, exprs) :
      dualiter ? new DualIterMap(info, exprs) :
      new IterMap(info, exprs)
    );
  }

  /**
   * Removes the specified operand.
   * @param cc compilation context
   * @param e operand to remove
   * @return new map expression
   * @throws QueryException query exception
   */
  public Expr remove(final CompileContext cc, final int e) throws QueryException {
    final ExprList list = new ExprList(exprs);
    list.remove(e);
    return get(cc, info, list.finish());
  }

  /**
   * Flattens nested map expressions.
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr flattenMaps(final CompileContext cc) throws QueryException {
    final ExprList list = new ExprList();
    for(final Expr expr : exprs) {
      if(expr instanceof SimpleMap && !(expr instanceof CachedMap)) {
        list.add(expr.args());
        cc.info(OPTFLAT_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    return list.size() != exprs.length ? get(cc, info, list.finish()) : null;
  }

  @Override
  Expr merge(final Expr expr, final Expr next, final CompileContext cc) throws QueryException {
    final long size = expr.size();
    if(!expr.has(Flag.NDT) && !next.has(Flag.POS)) {
      // merge expressions if next expression does not rely on the context
      if(!next.has(Flag.CTX)) {
        Expr count = null;
        if(size != -1) {
          count = Itr.get(size);
        } else if(expr instanceof Range && expr.arg(0) == Itr.ONE &&
            expr.arg(1).seqType().instanceOf(Types.INTEGER_O)) {
          count = expr.arg(1);
        }
        // (1 to 2) ! <x/>  ->  replicate(<x/>, 2, true())
        // (1 to $c) ! 'A'  ->  replicate('A', $c, false())
        if(count != null) return cc.replicate(next, count, info);
      }

      if(next instanceof StandardFunc && !next.has(Flag.NDT)) {
        // next operand relies on context and is a deterministic function call
        final Expr[] args = next.args();
        if(REPLICATE.is(next) && ((FnReplicate) next).singleEval(true) &&
            args[0] instanceof ContextValue && !args[1].has(Flag.CTX)) {
          if(REPLICATE.is(expr) && ((FnReplicate) expr).singleEval(true)) {
            // replicate(E, C) ! replicate(., D)  ->  replicate(E, C * D)
            final Expr cnt = new Arith(info, expr.arg(1), args[1], Calc.MULTIPLY).optimize(cc);
            return cc.function(REPLICATE, info, expr.arg(0), cnt);
          }
          if(expr instanceof final SingletonSeq ss && ss.singleItem()) {
            // SINGLETONSEQ ! replicate(., C)  ->  replicate(SINGLETONSEQ, C)
            return cc.function(REPLICATE, info, expr, args[1]);
          }
        } else if(ITEMS_AT.is(next) && !args[0].has(Flag.CTX) && args[1] instanceof ContextValue) {
          if(expr instanceof final RangeSeq rs) {
            // (A to B) ! items-at(E, .)  ->  util:range(E, A, B)
            // reverse(A to B) ! items-at(E, .)  ->  reverse(util:range(E, A, B))
            final Expr func = cc.function(_UTIL_RANGE, info, args[0],
                Itr.get(rs.min()), Itr.get(rs.max()));
            return rs.ascending() ? func : cc.function(REVERSE, info, func);
          }
          if(expr instanceof Range) {
            // (START to END) ! items-at(X, .)  ->  util:range(X, START, END)
            return cc.function(_UTIL_RANGE, info, args[0], expr.arg(0), expr.arg(1));
          }
          if(expr.seqType().instanceOf(Types.INTEGER_ZM)) {
            // INTEGERS ! items-at(X, .)  ->  items-at(X, INTEGERS)
            return cc.function(ITEMS_AT, info, args[0], expr);
          }
        } else if(DATA.is(next) && (((FnData) next).contextAccess() ||
            args[0] instanceof ContextValue)) {
          // E ! data(.)  ->  data(E)
          return cc.function(DATA, info, expr);
        } else if(STRING_TO_CODEPOINTS.is(expr) && CODEPOINTS_TO_STRING.is(next) &&
            args[0] instanceof ContextValue) {
          // string-to-codepoints(E) ! codepoints-to-string(.)  ->  characters(E)
          return cc.function(CHARACTERS, info, expr.args());
        }
      }

      // (1 to 5) ! (. + 1)  ->  2 to 6
      if(expr instanceof final RangeSeq rs && next instanceof final Arith arith) {
        final boolean plus = arith.calc == Calc.ADD, minus = arith.calc == Calc.SUBTRACT;
        if((plus || minus) && arith.arg(0) instanceof ContextValue &&
            arith.arg(1) instanceof final Itr itr) {
          final long diff = itr.itr(), start = rs.itemAt(0).itr() + (plus ? diff : -diff);
          return RangeSeq.get(start, rs.size(), rs.ascending());
        }
      }

      // try to merge deterministic expressions
      Expr input = expr;
      if(REPLICATE.is(expr) && ((FnReplicate) expr).singleEval(true)) {
        input = expr.arg(0);
      } else if(expr instanceof final SingletonSeq ss && ss.singleItem()) {
        input = ss.itemAt(0);
      }
      if(input.size() == 1) {
        final Expr inlined = inline(input, next, cc);
        if(inlined != null) {
          // replicate(1, 2) ! (. = 1)  ->  replicate(1 = 1, 2)
          return expr == input ? inlined : cc.replicate(inlined, Itr.get(size), info);
        }
      }

      // (1, 2) ! (. + 1)  ->  1 ! (. + 1), 2 ! (. + 1)
      final ExprList unroll = cc.unroll(expr, false);
      if(unroll != null) {
        final ExprList results = new ExprList(unroll.size());
        for(final Expr ex : unroll) {
          final Expr nxt = results.size() == size - 1 ? next : next.copy(cc, new IntObjectMap<>());
          results.add(get(cc, info, ex, nxt));
        }
        return List.get(cc, info, results.finish());
      }
    }

    if(expr.seqType().zeroOrOne()) {
      boolean inline = false;
      if(next instanceof final Cast cast) {
        // $node/@id ! xs:integer(.)  ->  xs:integer($node/@id)
        inline = cast.expr instanceof ContextValue && cast.seqType.occ == Occ.ZERO_OR_ONE;
      } else if(next instanceof final ContextFn ctxFn) {
        // $node/.. ! base-uri(.)  ->  base-uri($node/..)
        inline = ctxFn.inlineable();
      }
      if(inline) {
        try {
          return new InlineContext(null, expr, cc).inline(next);
        } catch(final QueryException ex) {
          // replace original expression with error
          return FnError.get(ex, next);
        }
      }
    }

    // merge filter with context value as root
    // A ! .[B]  ->  A[B]
    Preds preds = null;
    if(next instanceof final Filter filter) {
      if(filter.root instanceof ContextValue) preds = filter;
    } else if(next instanceof final SingleIterPath path) {
      final Step step = path.step(0);
      if(step.axis == Axis.SELF && step.test == NodeTest.NODE) preds = step;
    }
    if(preds != null && !preds.mayBePositional()) return Filter.get(cc, info, expr, preds.exprs);

    // A ! (if(B) then C else ()  ->  A[B] ! C
    if(next instanceof final If iff && iff.exprs[1] == Empty.VALUE && !iff.exprs[0].has(Flag.POS) &&
        !iff.cond.seqType().mayBeNumber()) {
      return get(cc, info, Filter.get(cc, info, expr, iff.cond), iff.exprs[0]);
    }
    return null;
  }

  /**
   * Rewrites adjacent paths to single path expressions.
   * @param cc compilation context
   * @return resulting expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr mergePaths(final CompileContext cc) throws QueryException {
    // skip optimization if first operand does not yield nodes in DDO
    if(!exprs[0].ddo()) return null;

    // first operand: determine root and optional steps
    Expr root = exprs[0];
    final ExprList steps = new ExprList().add();
    if(root instanceof final AxisPath path) {
      root = path.root;
      steps.add(path.steps);
    }

    // remaining operands: check for simple axis paths
    final int el = exprs.length;
    int e = 0;
    while(++e < el) {
      if(!(exprs[e] instanceof final AxisPath path2)) break;
      if(path2.root != null || !path2.simple()) break;
      steps.add(path2.steps);
    }
    if(e == 1) return null;

    // all operands are steps
    //   db:get('animals') ! xml  ->  db:get('animals')/xml
    //   a ! b ! c  ->  /a/b/c
    final Expr path = Path.get(cc, info, root, steps.finish());
    if(e == el) return path;

    // create expression with path and remaining operands
    //   a ! b ! string()  ->  a/b ! string()
    final ExprList list = new ExprList(el - e + 1).add(path);
    for(; e < el; e++) list.add(exprs[e]);
    return get(cc, info, list.finish());
  }

  /**
   * Determines the type and result size and drops expressions that will never be evaluated.
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr dropOps(final CompileContext cc) throws QueryException {
    final ExprList list = new ExprList(exprs.length);
    long min = 1, max = 1;
    for(final Expr expr : exprs) {
      // no results: skip remaining expressions
      if(max == 0) break;
      list.add(expr);
      final long es = expr.size();
      if(es == 0) {
        min = 0;
        max = 0;
      } else if(es > 0) {
        min *= es;
        if(max != -1) max *= es;
      } else {
        final Occ o = expr.seqType().occ;
        if(o.min == 0) min = 0;
        if(o.max > 1) max = -1;
      }
    }
    final int ls = list.size();
    if(ls != exprs.length) {
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
      return get(cc, info, list.finish());
    }

    exprType.assign(exprs[ls - 1], new long[] { min, max });
    return size() == 0 && !has(Flag.NDT, Flag.HOF) ? cc.emptySeq(this) : null;
  }

  /**
   * Converts the map to a path expression.
   * @param mode mode of simplification
   * @param cc compilation context
   * @return converted or original expression
   * @throws QueryException query context
   */
  private Expr toPath(final Simplify mode, final CompileContext cc) throws QueryException {
    final ExprList steps = new ExprList();

    final int el = exprs.length;
    final QueryFunction<Integer, Expr> simplify = e -> {
      final Expr expr = exprs[e];
      return mode == Simplify.DISTINCT || e + 1 == el ? expr.simplifyFor(mode, cc) : expr;
    };
    Expr root = simplify.apply(0);
    cc.pushFocus(root, true);
    if(root instanceof final AxisPath path) {
      root = path.root;
      steps.add(path.steps);
    }
    try {
      for(int e = 1; e < el; e++) {
        final Expr expr = simplify.apply(e);
        if(!(expr instanceof final AxisPath path)) return this;
        if(path.root != null) return this;
        steps.add(path.steps);
        cc.updateFocus(expr, true);
      }
    } finally {
      cc.removeFocus();
    }
    return Path.get(cc, info, root, steps.finish());
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    final int el = exprs.length;
    final Expr last = exprs[el - 1], prev = exprs[el - 2];
    if(mode.oneOf(Simplify.DATA, Simplify.NUMBER, Simplify.STRING, Simplify.COUNT,
        Simplify.DISTINCT)) {
      // distinct-values(@id ! data())  ->  distinct-values(@id)
      final Expr lst = cc.get(prev, true, () -> last.simplifyFor(mode, cc));
      if(lst != last) expr = get(cc, info, new ExprList(el).add(exprs).set(el - 1, lst).finish());
    }

    if(expr == this && mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      if(seqType().zeroOrOne() && prev.seqType().type instanceof NodeType && last instanceof Bln) {
        // boolean(@id ! true())  ->  boolean(@id)
        expr = last == Bln.FALSE ? Bln.FALSE : remove(cc, el - 1);
      } else {
        // $node[nodes ! text()]  ->  $node[nodes/text()]
        expr = toPath(mode, cc);
      }
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SimpleMap && super.equals(obj);
  }

  @Override
  public String description() {
    return "simple map";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, " ! ");
  }
}
