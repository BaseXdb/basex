package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.Function;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Simple map operator.
 *
 * @author BaseX Team 2005-20, BSD License
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
        list.add(((SimpleMap) expr).exprs);
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
    exprType.assign(exprs[exprs.length - 1].seqType().type, new long[] { min, max });

    // no results, deterministic expressions: return empty sequence
    if(size() == 0 && !has(Flag.NDT)) return cc.emptySeq(this);

    // merge paths
    Expr ex = mergePaths(cc);
    if(ex != null) return ex;

    // simplify static expressions
    final int el = exprs.length;
    int e = 0;
    boolean pushed = false;
    for(int n = 1; n < el; n++) {
      final Expr expr = exprs[e], next = exprs[n];
      if(e > 0) {
        if(pushed) {
          cc.updateFocus(expr);
        } else {
          cc.pushFocus(expr);
          pushed = true;
        }
      }

      final long es = expr.size();
      ex = null;
      if(next instanceof Filter) {
        final Filter filter = (Filter) next;
        if(filter.root instanceof ContextValue && !filter.mayBePositional()) {
          // merge filter with context value as root
          // A ! .[B]  ->  A[B]
          ex = Filter.get(cc, info, expr, ((Filter) next).exprs);
        }
      }

      if(ex == null && es != -1 && !expr.has(Flag.NDT) && !next.has(Flag.POS)) {
        // check if deterministic expressions with known result size can be removed
        // expression size is never 0 (empty expressions have no followers, see above)
        if(es == 1) {
          final InlineContext ic = new InlineContext(null, expr, cc);
          if(ic.inlineable(next, v -> next.count(v))) {
            // inline values
            //   'a' ! (. = 'a')  ->  'a'  = 'a'
            //   map {} ! ?*      ->  map {}?*
            //   123 ! number()   ->  number(123)
            // inline context reference
            // . ! number() = 2  ->  number() = 2
            // inline variable references
            //   $a ! (. + .)  ->  $a + $a
            // inline any other expression
            //   ($a + $b) ! (. * 2)  ->  ($a + $b) * 2
            //   ($n + 2) ! abs(.) ->  abs(. + 2)
            // skip nested node constructors
            //   <X/> ! <X xmlns='x'>{ . }</X>
            try {
              ex = ic.inline(next);
            } catch(final QueryException qe) {
              // replace original expression with error
              ex = cc.error(qe, next);
            }
          }
        } else if(!next.has(Flag.CTX)) {
          // merge expressions if next expression does not rely on the context
          if(next instanceof Value) {
            // (1 to 2) ! 3  ->  (3, 3)
            ex = SingletonSeq.get((Value) next, es);
          } else if(next.has(Flag.NDT, Flag.CNS)) {
            // (1 to 2) ! <x/>  ->  util:replicate('', 2) ! <x/>
            exprs[e] = cc.replaceWith(exprs[e], SingletonSeq.get(Str.ZERO, es));
          } else {
            // (1 to 2) ! 'ok'  ->  util:replicate('ok', 2)
            ex = cc.function(Function._UTIL_REPLICATE, info, next, Int.get(es));
          }
        }
      }

      if(ex == null && expr.seqType().zeroOrOne()) {
        boolean inline = false;
        if(next instanceof Cast) {
          // $node/@id ! xs:integer(.)  ->  xs:integer($node/@id)
          final Cast cast = (Cast) next;
          inline = cast.expr instanceof ContextValue && cast.seqType.occ == Occ.ZERO_ONE;
        } else if(next instanceof ContextFn) {
          // $node/.. ! base-uri(.)  ->  base-uri($node/..)
          inline = ((ContextFn) next).inlineable();
        }
        if(inline) {
          try {
            final InlineContext ic = new InlineContext(null, expr, cc);
            ex = next.inline(ic);
          } catch(final QueryException qe) {
            // replace original expression with error
            ex = cc.error(qe, next);
          }
        }
      }

      if(ex != null) {
        cc.info(OPTMERGE_X, ex);
        exprs[e] = ex;
      } else if(!(next instanceof ContextValue)) {
        // context item expression can be ignored
        exprs[++e] = next;
      }
    }
    if(pushed) cc.removeFocus();

    // single expression: return this expression
    if(e == 0) return exprs[0];
    if(++e != el) exprs = Arrays.copyOf(exprs, e);

    boolean cached = false;
    for(final Expr expr : exprs) cached = cached || expr.has(Flag.POS);
    boolean dual = exprs.length == 2 && exprs[1].seqType().zeroOrOne();

    // choose best map implementation
    return copyType(
      cached ? new CachedMap(info, exprs) :
      item ? new ItemMap(info, exprs) :
      dual ? new DualMap(info, exprs) :
      new IterMap(info, exprs)
    );
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
    /* Context dependency: Only check first expression.
     * Examples: . ! abc */
    if(Flag.CTX.in(flags) && exprs[0].has(Flag.CTX)) return true;
    /* Positional access: only check root node (steps will refer to result of root node).
     * Example: position()/a */
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
    if(ic.expr instanceof ContextValue) {
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
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SimpleMap && super.equals(obj);
  }

  @Override
  public String description() {
    return "map operator";
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, " ! ");
  }
}
