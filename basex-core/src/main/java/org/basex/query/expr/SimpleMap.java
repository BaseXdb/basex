package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.util.*;
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
 * Simple map operator.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * Creates a new, optimized map expression, or the first expression if only one was specified.
   * @param cc compilation context
   * @param ii input info
   * @param exprs one or more expressions
   * @return filter root, path or filter expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo ii, final Expr... exprs)
      throws QueryException {
    return exprs.length == 1 ? exprs[0] : new CachedMap(ii, exprs).optimize(cc);
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
      Expr expr = exprs[e];
      try {
        expr = expr.compile(cc);
      } catch(final QueryException qe) {
        // replace original expression with error
        expr = cc.error(qe, expr);
      }
      if(e == 0) cc.pushFocus(expr);
      else cc.updateFocus(expr);
      exprs[e] = expr;
    }
    cc.removeFocus();
    return optimize(cc);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    // flatten nested expressions (unless result needs to be cached)
    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr instanceof SimpleMap && !(expr instanceof CachedMap)) {
        list.add(expr.args());
        cc.info(OPTFLAT_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    if(list.size() != exprs.length) return SimpleMap.get(cc, info, list.finish());
    exprs = list.next();

    // determine type and result size, drop expressions that will never be evaluated
    long min = 1, max = 1;
    boolean item = true;
    for(final Expr expr : exprs) {
      // no results: skip evaluation of remaining expressions
      if(max == 0) break;
      list.add(expr);
      final long es = expr.size();
      if(es == 0) {
        min = 0;
        max = 0;
      } else if(es > 0) {
        min *= es;
        if(max != -1) max *= es;
        if(es > 1) item = false;
      } else {
        final Occ o = expr.seqType().occ;
        if(o.min == 0) min = 0;
        if(o.max > 1) {
          max = -1;
          item = false;
        }
      }
    }
    if(exprs.length != list.size()) {
      exprs = list.next();
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
    }
    exprType.assign(exprs[exprs.length - 1].seqType(), new long[] { min, max });

    // no results, deterministic expressions: return empty sequence
    if(size() == 0 && !has(Flag.NDT)) return cc.emptySeq(this);

    // merge paths
    final Expr ex = mergePaths(cc);
    if(ex != null) return ex;

    // merge operands
    final int el = exprs.length;
    int e = 0;
    boolean pushed = false;
    for(int n = 1; n < el; n++) {
      final Expr next = exprs[n], merged = merge(exprs[e], next, cc);
      if(merged != null) {
        cc.info(OPTMERGE_X, merged);
        exprs[e] = merged;
      } else if(!(next instanceof ContextValue)) {
        // context item expression can be ignored
        exprs[++e] = next;
      }

      if(e > 0) {
        if(pushed) {
          cc.updateFocus(exprs[e - 1]);
        } else {
          cc.pushFocus(exprs[e - 1]);
          pushed = true;
        }
      }
    }
    if(pushed) cc.removeFocus();

    // single expression: return this expression
    if(e == 0) return exprs[0];
    if(++e != el) exprs = Arrays.copyOf(exprs, e);

    boolean cached = false;
    for(final Expr expr : exprs) cached = cached || expr.has(Flag.POS);
    final boolean dual = exprs.length == 2 && exprs[1].seqType().zeroOrOne();

    // choose best map implementation
    return copyType(
      cached ? new CachedMap(info, exprs) :
      item ? new ItemMap(info, exprs) :
      dual ? new DualMap(info, exprs) :
      new IterMap(info, exprs)
    );
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

    // merge filter with context value as root
    // A ! .[B]  ->  A[B]
    if(next instanceof Filter) {
      final Filter filter = (Filter) next;
      if(filter.root instanceof ContextValue && !filter.mayBePositional())
        return Filter.get(cc, info, expr, ((Filter) next).exprs);
    }

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
        // (1 to 2) ! <x/>  ->  util:replicate(<x/>, 2, true())
        // (1 to $c) ! 'A'  ->  util:replicate('A', $c, false())
        if(count != null) return cc.replicate(next, count, info);
      }

      if(next instanceof StandardFunc && !next.has(Flag.NDT)) {
        // next operand relies on context and is a deterministic function call
        final Expr[] args = next.args();
        if(_UTIL_REPLICATE.is(next) && ((UtilReplicate) next).singleEval() &&
            args[0] instanceof ContextValue && !args[1].has(Flag.CTX)) {
          if(_UTIL_REPLICATE.is(expr) && ((UtilReplicate) expr).singleEval()) {
            // util:replicate(E, C) ! util:replicate(., D)  ->  util:replicate(E, C * D)
            final Expr cnt = new Arith(info, expr.arg(1), args[1], Calc.MULT).optimize(cc);
            return cc.function(_UTIL_REPLICATE, info, expr.arg(0), cnt);
          }
          if(expr instanceof SingletonSeq && ((SingletonSeq) expr).singleItem()) {
            // SINGLETONSEQ ! util:replicate(., C)  ->  util:replicate(SINGLETONSEQ, C)
            return cc.function(_UTIL_REPLICATE, info, expr, args[1]);
          }
        } else if(_UTIL_ITEM.is(next) && !args[0].has(Flag.CTX) &&
            args[1] instanceof ContextValue) {
          if(expr instanceof RangeSeq) {
            // (3 to 4) ! util:item(X, .)  ->  util:range(X, 3, 4)
            // reverse(3 to 4) ! util:item(X, .)  ->  reverse(util:range(X, 3, 4))
            final RangeSeq seq = (RangeSeq) expr;
            final long[] range = seq.range(false);
            final Expr func = cc.function(_UTIL_RANGE, info,
                args[0], Int.get(range[0]), Int.get(range[1]));
            return seq.asc ? func : cc.function(REVERSE, info, func);
          }
          if(expr instanceof Range) {
            // (1 to $i) ! util:item(X, .)  ->  util:range(X, 1, $i)
            return cc.function(_UTIL_RANGE, info, args[0], expr.arg(0), expr.arg(1));
          }
        } else if(DATA.is(next) && (((FnData) next).contextAccess() ||
            args[0] instanceof ContextValue)) {
          // ITEMS ! data(.)  ->  data(ITEMS)
          return cc.function(DATA, info, expr);
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
      if(_UTIL_REPLICATE.is(expr) && ((UtilReplicate) expr).singleEval()) {
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
          // util:replicate(1, 2) ! (. = 1)  ->  util:replicate(1 = 1, 2)
          return expr == input ? ex : cc.replicate(ex, Int.get(size), info);
        }
      }

      final int limit = cc.qc.context.options.get(MainOptions.UNROLLLIMIT);
      if(expr instanceof Seq && size <= limit) {
        // (1, 2) ! (. + 1)  ->  1 ! (. + 1), 2 ! (. + 1)
        cc.info(QueryText.OPTUNROLL_X, this);
        final ExprList results = new ExprList((int) size);
        for(final Item item : (Value) expr) {
          final Expr nxt = results.size() == size - 1 ? next : next.copy(cc, new IntObjMap<>());
          results.add(SimpleMap.get(cc, info, item, nxt));
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
          final InlineContext ic = new InlineContext(null, expr, cc);
          return ic.inline(next);
        } catch(final QueryException qe) {
          // replace original expression with error
          return cc.error(qe, next);
        }
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
    //   db:open('animals') ! xml  ->  db:open('animals')/xml
    //   a ! b ! c  ->  /a/b/c
    final Expr path = Path.get(cc, info, root, steps.finish());
    if(e == el) return path;

    // create expression with path and remaining operands
    //   a ! b ! string()  ->  a/b ! string()
    final ExprList list = new ExprList(el - e + 1).add(path);
    for(; e < el; e++) list.add(exprs[e]);
    return get(cc, info, list.finish());
  }

  @Override
  public Data data() {
    return exprs[exprs.length - 1].data();
  }

  /**
   * Converts the map to a path expression.
   * @param cc compilation context
   * @return converted or original expression
   * @throws QueryException query context
   */
  public Expr toPath(final CompileContext cc) throws QueryException {
    Expr root = exprs[0];
    final ExprList steps = new ExprList();
    if(root instanceof AxisPath) {
      final AxisPath path = (AxisPath) root;
      root = path.root;
      steps.add(path.steps);
    }
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      if(!(exprs[e] instanceof AxisPath)) return this;
      final AxisPath path = (AxisPath) exprs[e];
      if(path.root != null) return this;
      steps.add(path.steps);
    }
    return cc.replaceWith(this, Path.get(cc, info, root, steps.finish()));
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Expr expr = this;
    if(mode == Simplify.EBV || mode == Simplify.PREDICATE || mode == Simplify.DISTINCT) {
      // nodes ! text() = string  ->  nodes/text() = string
      expr = toPath(cc);
    } else {
      final int el = exprs.length;
      final Expr old = exprs[el - 1];
      final Expr ex = cc.get(exprs[el - 2], () -> old.simplifyFor(mode, cc));
      if(ex != old) {
        final ExprList list = new ExprList(el).add(exprs).set(el - 1, ex);
        expr = SimpleMap.get(cc, info, list.finish());
      }
    }
    return expr != this ? expr : super.simplifyFor(mode, cc);
  }

  @Override
  public final boolean has(final Flag... flags) {
    // Context dependency, positional access: only check first expression.
    // Examples: . ! abc, position() ! a
    if(Flag.CTX.in(flags) && exprs[0].has(Flag.CTX)) return true;
    if(Flag.POS.in(flags) && exprs[0].has(Flag.POS)) return true;
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
  public void plan(final QueryString qs) {
    qs.tokens(exprs, " ! ");
  }
}
