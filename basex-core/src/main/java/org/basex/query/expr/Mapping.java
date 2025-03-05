package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Mapping expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Mapping extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param exprs expressions
   */
  Mapping(final InputInfo info, final SeqType seqType, final Expr... exprs) {
    super(info, seqType, exprs);
  }

  @Override
  public final void checkUp() throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el - 1; e++) checkNoUp(exprs[e]);
    exprs[el - 1].checkUp();
  }

  @Override
  public final boolean vacuous() {
    return exprs[exprs.length - 1].vacuous();
  }

  @Override
  public final Expr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    final boolean items = items();
    for(int e = 0; e < el; e++) {
      final Expr expr = cc.compileOrError(exprs[e], e == 0);
      if(e == 0) cc.pushFocus(expr, items);
      else cc.updateFocus(expr, items);
      exprs[e] = expr;
    }
    cc.removeFocus();
    return optimize(cc);
  }

  /**
   * Indicates if the expression maps items or values.
   * @return result of check
   */
  abstract boolean items();

  @Override
  public final boolean has(final Flag... flags) {
    // Context dependency, positional access: only check first expression.
    // Examples: . ! abc, position() ! a
    return Flag.FCS.oneOf(flags) ||
           Flag.CTX.oneOf(flags) && exprs[0].has(Flag.CTX) ||
           Flag.POS.oneOf(flags) && exprs[0].has(Flag.POS) ||
           super.has(Flag.remove(flags, Flag.POS, Flag.CTX));
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
    VarUsage uses = exprs[0].count(var);
    // variable reference: check remaining operands
    if(var != null) {
      final boolean items = items();
      final int el = exprs.length;
      for(int e = 1; e < el && uses != VarUsage.MORE_THAN_ONCE; e++) {
        // EXPR -> ($a)  -> single use
        // EXPR ! ($a)  -> more than one use
        final VarUsage vu = exprs[e].count(var);
        if(vu != VarUsage.NEVER) uses = items ? VarUsage.MORE_THAN_ONCE : uses.plus(vu);
      }
    }
    return uses;
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    // do not replace $v with .:  EXPR ! $v
    if(ic.var != null && ic.expr.has(Flag.CTX)) {
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
    final boolean items = items();
    for(int e = 0; e < el; e++) {
      Expr inlined;
      try {
        inlined = exprs[e].inline(ic);
      } catch(final QueryException ex) {
        // replace original expression with error
        inlined = cc.error(ex, exprs[e]);
      }
      if(inlined != null) {
        exprs[e] = inlined;
        changed = true;
      } else {
        inlined = exprs[e];
      }
      if(e == 0) cc.pushFocus(inlined, items);
      else cc.updateFocus(inlined, items);
    }
    cc.removeFocus();

    return changed ? optimize(cc) : null;
  }

  @Override
  public final void markTailCalls(final CompileContext cc) {
    final int el = exprs.length - 1;
    if(items()) {
      for(int e = 0; e < el; e++) {
        if(!exprs[e].seqType().zeroOrOne()) return;
      }
    }
    exprs[el].markTailCalls(cc);
  }

  /**
   * Merges adjacent operands.
   * @param cc compilation context
   * @return resulting expression or {@code null}
   * @throws QueryException query exception
   */
  final Expr[] merge(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    final ExprList list = new ExprList(el).add(exprs[0]);

    boolean pushed = false;
    try {
      final boolean items = items();
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
            cc.updateFocus(expr, items);
          } else {
            cc.pushFocus(expr, items);
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

    cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
    return list.finish();
  }

  /**
   * Merges adjacent operands.
   * @param expr expression to inline
   * @param next target expression
   * @param cc compilation context
   * @return resulting expression or {@code null}
   */
  final Expr inline(final Expr expr, final Expr next, final CompileContext cc) {
    /* inline values:
     *   'a' ! (. = 'a')  ->  'a'  = 'a'
     *   map { } ! ?*      ->  map { }?*
     *   123 ! number()   ->  number(123)
     * inline context reference
     *   . ! number() = 2  ->  number() = 2
     * inline variable references
     *   $a ! (. + .)  ->  $a + $a
     * inline any other expression
     *   ($a + $b) ! (. * 2)  ->  ($a + $b) * 2
     *   ($n + 2) ! abs(.) ->  abs(. + 2)
     * values
     *   (<X/>, <Y/>) -> count(.)  ->  count((<X/>, <Y/>))
     * skip positional access and higher-order functions
     *   <X/> ! function-lookup#2(xs:QName('fn:name'), 0)()
     *   <X/> ! last#0()
     */
    if(!next.has(Flag.POS, Flag.HOF)) {
      final InlineContext ic = new InlineContext(null, expr, cc);
      if(ic.inlineable(next)) {
        Expr inlined;
        try {
          inlined = ic.inline(next);
        } catch(final QueryException ex) {
          // replace original expression with error
          inlined = cc.error(ex, next);
        }
        return inlined;
      }
    }
    return null;
  }

  /**
   * Tries to merge two adjacent map operands.
   * @param expr first operand
   * @param next second operand
   * @param cc compilation context
   * @return new expression or {@code null}
   * @throws QueryException query exception
   */
  abstract Expr merge(Expr expr, Expr next, CompileContext cc) throws QueryException;
}
