package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import java.util.*;
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
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map operator.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class SimpleMap extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  SimpleMap(final InputInfo info, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
  }

  /**
   * Creates a new, optimized map expression.
   * @param cc compilation context
   * @param ii input info
   * @param exprs expressions
   * @return list, single expression or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo ii, final Expr... exprs)
      throws QueryException {
    final int el = exprs.length;
    return el > 1 ? new CachedMap(ii, exprs).optimize(cc) : el > 0 ? exprs[0] : Empty.VALUE;
  }

  @Override
  public final void checkUp() throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el - 1; e++) checkNoUp(exprs[e]);
    exprs[el - 1].checkUp();
  }

  @Override
  public boolean vacuous() {
    return exprs[exprs.length - 1].vacuous();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      final Expr expr = cc.compileOrError(exprs[e], e == 0);
      if(e == 0) cc.pushFocus(expr);
      else cc.updateFocus(expr);
      exprs[e] = expr;
    }
    cc.removeFocus();
    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // merge operands
    Expr ex = flattenMaps(cc);
    if(ex == null) ex = mergePaths(cc);
    if(ex == null) ex = mergeOps(cc);
    if(ex == null) ex = dropOps(cc);
    if(ex != null) return ex;

    // choose best map implementation
    boolean cached = false, item = true;
    for(final Expr expr : exprs) {
      cached = cached || expr.has(Flag.POS);
      item = item && expr.seqType().zeroOrOne();
    }
    final boolean dualiter = exprs.length == 2, dual = dualiter && exprs[1].seqType().zeroOrOne();

    return copyType(
      cached ? new CachedMap(info, exprs) :
      item ? new ItemMap(info, exprs) :
      dual ? new DualMap(info, exprs) :
      dualiter ? new DualIterMap(info, exprs) :
      new IterMap(info, exprs)
    );
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

  /**
   * Tries to merge two adjacent map operands.
   * @param expr first operand
   * @param next second operand
   * @param cc compilation context
   * @return new expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr merge(final Expr expr, final Expr next, final CompileContext cc)
      throws QueryException {

    final long size = expr.size();
    if(!expr.has(Flag.NDT) && !next.has(Flag.POS)) {
      // merge expressions if next expression does not rely on the context
      if(!next.has(Flag.CTX)) {
        Expr count = null;
        if(size != -1) {
          count = Int.get(size);
        } else if(expr instanceof Range && expr.arg(0) == Int.ONE &&
            expr.arg(1).seqType().instanceOf(SeqType.INTEGER_O)) {
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
            final Expr cnt = new Arith(info, expr.arg(1), args[1], Calc.MULT).optimize(cc);
            return cc.function(REPLICATE, info, expr.arg(0), cnt);
          }
          if(expr instanceof SingletonSeq && ((SingletonSeq) expr).singleItem()) {
            // SINGLETONSEQ ! replicate(., C)  ->  replicate(SINGLETONSEQ, C)
            return cc.function(REPLICATE, info, expr, args[1]);
          }
        } else if(ITEMS_AT.is(next) && !args[0].has(Flag.CTX) && args[1] instanceof ContextValue) {
          if(expr instanceof RangeSeq) {
            // (A to B) ! items-at(E, .)  ->  util:range(E, A, B)
            // reverse(A to B) ! items-at(E, .)  ->  reverse(util:range(E, A, B))
            final RangeSeq seq = (RangeSeq) expr;
            final long[] range = seq.range(false);
            final Expr func = cc.function(_UTIL_RANGE, info,
                args[0], Int.get(range[0]), Int.get(range[1]));
            return seq.asc ? func : cc.function(REVERSE, info, func);
          }
          if(expr instanceof Range) {
            // (START to END) ! items-at(X, .)  ->  util:range(X, START, END)
            return cc.function(_UTIL_RANGE, info, args[0], expr.arg(0), expr.arg(1));
          }
          if(expr.seqType().instanceOf(SeqType.INTEGER_ZM)) {
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
      if(expr instanceof RangeSeq && next instanceof Arith) {
        final Arith arith = (Arith) next;
        final boolean plus = arith.calc == Calc.PLUS, minus = arith.calc == Calc.MINUS;
        if((plus || minus) && next.arg(0) instanceof ContextValue && next.arg(1) instanceof Int) {
          final RangeSeq seq = (RangeSeq) expr;
          final long diff = ((Int) next.arg(1)).itr();
          return RangeSeq.get(seq.range(true)[0] + (plus ? diff : -diff), seq.size(), seq.asc);
        }
      }

      // try to merge deterministic expressions
      Expr input = expr;
      if(REPLICATE.is(expr) && ((FnReplicate) expr).singleEval(true)) {
        input = expr.arg(0);
      } else if(expr instanceof SingletonSeq && ((SingletonSeq) expr).singleItem()) {
        input = ((SingletonSeq) expr).itemAt(0);
      }
      if(input.size() == 1) {
        final InlineContext ic = new InlineContext(null, input, cc);
        if(ic.inlineable(next)) {
          // inline values
          //   'a' ! (. = 'a')  ->  'a'  = 'a'
          //   map {} ! ?*      ->  map {}?*
          //   123 ! number()   ->  number(123)
          // inline context reference
          //   . ! number() = 2  ->  number() = 2
          // inline variable references
          //   $a ! (. + .)  ->  $a + $a
          // inline any other expression
          //   ($a + $b) ! (. * 2)  ->  ($a + $b) * 2
          //   ($n + 2) ! abs(.) ->  abs(. + 2)
          // skip nested node constructors
          //   <X/> ! <X xmlns='x'>{ . }</X>
          Expr ex;
          try {
            ex = ic.inline(next);
          } catch(final QueryException qe) {
            // replace original expression with error
            ex = cc.error(qe, next);
          }
          // replicate(1, 2) ! (. = 1)  ->  replicate(1 = 1, 2)
          return expr == input ? ex : cc.replicate(ex, Int.get(size), info);
        }
      }

      // (1, 2) ! (. + 1)  ->  1 ! (. + 1), 2 ! (. + 1)
      final ExprList unroll = cc.unroll(expr, false);
      if(unroll != null) {
        final ExprList results = new ExprList(unroll.size());
        for(final Expr ex : unroll) {
          final Expr nxt = results.size() == size - 1 ? next : next.copy(cc, new IntObjMap<>());
          results.add(get(cc, info, ex, nxt));
        }
        return List.get(cc, info, results.finish());
      }
    }

    if(expr.seqType().zeroOrOne()) {
      boolean inline = false;
      if(next instanceof Cast) {
        // $node/@id ! xs:integer(.)  ->  xs:integer($node/@id)
        final Cast cast = (Cast) next;
        inline = cast.expr instanceof ContextValue && cast.seqType.occ == Occ.ZERO_OR_ONE;
      } else if(next instanceof ContextFn) {
        // $node/.. ! base-uri(.)  ->  base-uri($node/..)
        inline = ((ContextFn) next).inlineable();
      }
      if(inline) {
        try {
          return new InlineContext(null, expr, cc).inline(next);
        } catch(final QueryException qe) {
          // replace original expression with error
          return cc.error(qe, next);
        }
      }
    }

    // merge filter with context value as root
    // A ! .[B]  ->  A[B]
    if(next instanceof Filter) {
      final Filter filter = (Filter) next;
      if(filter.root instanceof ContextValue && !filter.mayBePositional()) {
        return Filter.get(cc, info, expr, filter.exprs);
      }
    }

    // A ! (if(B) then C else ()  ->  A[B] ! C
    if(next instanceof If) {
      final If iff = (If) next;
      if(iff.exprs[1] == Empty.VALUE && !iff.exprs[0].has(Flag.POS) &&
          !iff.cond.seqType().mayBeNumber()) {
        return get(cc, info, Filter.get(cc, info, expr, iff.cond), iff.exprs[0]);
      }
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
    if(root instanceof AxisPath) {
      final AxisPath ap = (AxisPath) root;
      root = ap.root;
      steps.add(ap.steps);
    }

    // remaining operands: check for simple axis paths
    final int el = exprs.length;
    int e = 0;
    while(++e < el) {
      if(!(exprs[e] instanceof AxisPath)) break;
      final AxisPath path2 = (AxisPath) exprs[e];
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
   * Merges the operands.
   * @param cc compilation context
   * @return resulting expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr mergeOps(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    final ExprList list = new ExprList(el).add(exprs[0]);

    boolean pushed = false;
    try {
      for(int e = 1; e < el; e++) {
        final Expr merged = merge(list.peek(), exprs[e], cc);
        if(merged != null) {
          list.set(list.size() - 1, merged);
        } else {
          list.add(exprs[e]);
        }
        if(list.size() > 1) {
          final Expr expr = list.get(list.size() - 2);
          if(pushed) {
            cc.updateFocus(expr);
          } else {
            cc.pushFocus(expr);
            pushed = true;
          }
        }
      }
    } finally {
      if(pushed) cc.removeFocus();
    }

    // remove context value references (ignore first expression)
    // (1 to 10) ! .  ->  (1 to 10)
    for(int n = list.size() - 1; n > 0; n--) {
      if(list.get(n) instanceof ContextValue) list.remove(n);
    }

    final int ls = list.size();
    if(ls == el) return null;
    if(ls == 1) return cc.replaceWith(this, list.peek());

    cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
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
    cc.pushFocus(root);
    if(root instanceof AxisPath) {
      final AxisPath path = (AxisPath) root;
      root = path.root;
      steps.add(path.steps);
    }
    try {
      for(int e = 1; e < el; e++) {
        final Expr expr = simplify.apply(e);
        if(!(expr instanceof AxisPath)) return this;
        final AxisPath path = (AxisPath) expr;
        if(path.root != null) return this;
        steps.add(path.steps);
        cc.updateFocus(expr);
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
      final Expr lst = cc.get(prev, () -> last.simplifyFor(mode, cc));
      if(lst != last) expr = get(cc, info, new ExprList(el).add(exprs).set(el - 1, lst).finish());
    }

    if(expr == this) {
      if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE, Simplify.DISTINCT)) {
        if(mode != Simplify.DISTINCT && seqType().zeroOrOne() &&
            prev.seqType().type instanceof NodeType && last instanceof Bln) {
          // boolean(@id ! true())  ->  boolean(@id)
          expr = last == Bln.FALSE ? Bln.FALSE : get(cc, info, Arrays.copyOf(exprs, el - 1));
        } else {
          // nodes ! text() = string  ->  nodes/text() = string
          expr = toPath(mode, cc);
        }
      }
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public final boolean has(final Flag... flags) {
    // Context dependency, positional access: only check first expression.
    // Examples: . ! abc, position() ! a
    if(Flag.FCS.in(flags) ||
       Flag.CTX.in(flags) && exprs[0].has(Flag.CTX) ||
       Flag.POS.in(flags) && exprs[0].has(Flag.POS)) return true;
    // check remaining flags
    final Flag[] flgs = Flag.POS.remove(Flag.CTX.remove(flags));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    visitor.enterFocus();
    if(!visitAll(visitor, exprs)) return false;
    visitor.exitFocus();
    return true;
  }

  @Override
  public final VarUsage count(final Var var) {
    VarUsage uses = VarUsage.NEVER;
    // context reference check: only consider first operand
    if(var != null) {
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        uses = uses.plus(exprs[e].count(var));
        if(uses == VarUsage.MORE_THAN_ONCE) break;
      }
    }
    // assume that remaining operands will be evaluated multiple times
    return uses == VarUsage.NEVER ? exprs[0].count(var) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    if(ic.expr instanceof ContextValue && ic.var != null) {
      final int el = exprs.length;
      for(int e = 1; e < el; e++) {
        if(exprs[e].uses(ic.var)) return false;
      }
    }
    return exprs[0].inlineable(ic);
  }

  @Override
  public final Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = false;
    // context inlining: only consider first expression
    final CompileContext cc = ic.cc;
    final int el = ic.var == null ? 1 : exprs.length;
    for(int e = 0; e < el; e++) {
      Expr inlined;
      try {
        inlined = exprs[e].inline(ic);
      } catch(final QueryException qe) {
        // replace original expression with error
        inlined = cc.error(qe, exprs[e]);
      }
      if(inlined != null) {
        exprs[e] = inlined;
        changed = true;
      } else {
        inlined = exprs[e];
      }
      if(e == 0) cc.pushFocus(inlined);
      else cc.updateFocus(inlined);
    }
    cc.removeFocus();

    return changed ? optimize(cc) : null;
  }

  @Override
  public void markTailCalls(final CompileContext cc) {
    final int el = exprs.length - 1;
    for(int e = 0; e < el; e++) {
      if(!exprs[e].seqType().zeroOrOne()) return;
    }
    exprs[el].markTailCalls(cc);
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
