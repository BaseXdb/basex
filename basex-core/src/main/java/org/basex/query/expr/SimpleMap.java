package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.Supplier;

import org.basex.query.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-19, BSD License
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
   * Returns a map instance.
   * @param ii input info
   * @param exprs two or more expressions
   * @return instance
   */
  public static SimpleMap get(final InputInfo ii, final Expr... exprs) {
    for(final Expr expr : exprs) {
      if(expr.has(Flag.POS)) return new CachedMap(ii, exprs);
    }
    return new IterMap(ii, exprs);
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
        expr = cc.error(qe, this);
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
    // compute result size
    final ExprList list = new ExprList(exprs.length);
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
      exprs = list.finish();
      cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
    }
    exprType.assign(exprs[exprs.length - 1].seqType().type, new long[] { min, max });

    // simplify static expressions
    int e = 0;
    final int el = exprs.length;
    for(int n = 1; n < el; n++) {
      final Expr expr = exprs[e], next = exprs[n];
      final long es = expr.size();
      Expr rep = null;
      // check if deterministic expressions with known result size can be removed
      // expression size is never 0 (empty expressions have no followers, see above)
      if(es != -1 && !expr.has(Flag.NDT)) {
        if(next instanceof Value) {
          // rewrite expression with next value as singleton sequence
          // (1 to 2) ! 3  ->  (3, 3)
          rep = SingletonSeq.get((Value) next, es);
        } else if(!next.has(Flag.CTX, Flag.POS)) {
          // skip expression that relies on the context
          if(es == 1) {
            // replace expression with next expression
            // <x/> ! 'ok'  ->  'ok'
            rep = next;
          } else if(!next.has(Flag.NDT, Flag.CNS)) {
            // replace expression with replicated expression
            // (1 to 2) ! 'ok'  ->  util:replicate('ok', 2)
            rep = cc.function(Function._UTIL_REPLICATE, info, next, Int.get(es));
          } else {
            // (1 to 2) ! <x/>  ->  util:replicate('ok', 2)
            exprs[e] = SingletonSeq.get(Str.ZERO, es);
          }
        }
      }

      if(rep != null) {
        exprs[e] = cc.replaceWith(expr, rep);
      } else if(!(next instanceof ContextValue)) {
        // context item expression can be ignored
        exprs[++e] = next;
      }
    }
    if(++e != el) exprs = Arrays.copyOf(exprs, e);

    // single expression: return this expression
    return e == 1 ? exprs[0] :
      // no results, deterministic expressions: return empty sequence
      size() == 0 && !has(Flag.NDT) ? cc.emptySeq(this) :
      // item-based iteration
      item ? copyType(new ItemMap(info, exprs)) :
      // default evaluation
      this;
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
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      uses = uses.plus(exprs[e].count(var));
      if(uses == VarUsage.MORE_THAN_ONCE) break;
    }
    return uses == VarUsage.NEVER ? exprs[0].count(var) : VarUsage.MORE_THAN_ONCE;
  }

  @Override
  public final boolean inlineable(final Var var) {
    final int el = exprs.length;
    for(int e = 1; e < el; e++) {
      if(exprs[e].uses(var)) return false;
    }
    return exprs[0].inlineable(var);
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
  public final String toString() {
    final StringBuilder sb = new StringBuilder().append('(');
    for(final Expr expr : exprs) {
      if(sb.length() != 1) sb.append(" ! ");
      sb.append(expr);
    }
    return sb.append(')').toString();
  }
}
