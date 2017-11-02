package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract comparison.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
  /** Collation (can be {@code null}). */
  final Collation coll;
  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   * @param coll collation (can be {@code null})
   * @param sc static context
   */
  Cmp(final InputInfo info, final Expr expr1, final Expr expr2, final Collation coll,
      final StaticContext sc) {
    super(info, expr1, expr2);
    this.coll = coll;
    this.sc = sc;
  }

  /**
   * Swaps the operands of the expression if better performance is expected.
   * The operator itself needs to be swapped by the calling expression.
   * @return resulting expression
   */
  final boolean swap() {
    // move value, or path without root, to second position
    final boolean swap = exprs[0].isValue() && !exprs[1].isValue() ||
        exprs[1] instanceof Path && ((Path) exprs[1]).root == null &&
        (!(exprs[0] instanceof Path) || ((Path) exprs[0]).root != null) ||
        exprs[1] instanceof FnPosition;

    if(swap) {
      final Expr tmp = exprs[0];
      exprs[0] = exprs[1];
      exprs[1] = tmp;
    }
    return swap;
  }

  /**
   * If possible, returns an optimized expression with inverted operands.
   * @param cc compilation context
   * @return original or modified expression
   * @throws QueryException query exception
   */
  public abstract Expr invert(CompileContext cc) throws QueryException;

  /**
   * This method is called if the first operand of the comparison expression is a
   * {@code count()} function.
   * @param comp comparator
   * @param cc compilation context
   * @return resulting expression
   * @throws QueryException query exception
   */
  final Expr compCount(final OpV comp, final CompileContext cc) throws QueryException {
    // evaluate argument
    final Expr a = exprs[1];
    if(!(a instanceof Item)) return this;
    final Item it = (Item) a;
    if(!it.type.isNumberOrUntyped()) return this;
    final Expr[] args = ((Arr) exprs[0]).exprs;

    // TRUE: c > (v<0), c != (v<0), c >= (v<=0), c != not-int(v)
    switch(check(comp, it)) {
      case  0: return Bln.TRUE;
      case  1: return Bln.FALSE;
      case  2: return cc.function(Function.EXISTS, info, args);
      case  3: return cc.function(Function.EMPTY, info, args);
      default: return this;
    }
  }

  /**
   * This method is called if the first operand of the comparison expression is a
   * {@code string-length()} function.
   * @param comp comparator
   * @param cc compilation context
   * @return resulting expression
   * @throws QueryException query exception
   */
  final Expr compStringLength(final OpV comp, final CompileContext cc) throws QueryException {
    // evaluate argument
    final Expr a = exprs[1];
    if(!(a instanceof Item)) return this;
    final Item it = (Item) a;
    if(!it.type.isNumberOrUntyped()) return this;
    final Expr[] args = ((Arr) exprs[0]).exprs;

    // TRUE: c > (v<0), c != (v<0), c >= (v<=0), c != not-int(v)
    switch(check(comp, it)) {
      case  0: return Bln.TRUE;
      case  1: return Bln.FALSE;
      case  2: return cc.function(Function.BOOLEAN, info, cc.function(Function.STRING, info, args));
      case  3: return cc.function(Function.NOT, info, cc.function(Function.STRING, info, args));
      default: return this;
    }
  }

  /**
   * Analyzes the comparison and returns its type. Possible types are:
   * <ul>
   *   <li>0: always true</li>
   *   <li>1: always false</li>
   *   <li>2: positive, non-zero check</li>
   *   <li>3: zero check</li>
   *   <li>-1: none of them</li>
   * </ul>
   * @param comp comparator
   * @param it input item
   * @return comparison type (0: always true, 1:
   * @throws QueryException query exception
   */
  private int check(final OpV comp, final Item it) throws QueryException {
    final double v = it.dbl(info);
    // > (v<0), != (v<0), >= (v<=0), != int(v)
    if((comp == OpV.GT || comp == OpV.NE) && v < 0 || comp == OpV.GE && v <= 0 ||
       comp == OpV.NE && v != (int) v) return 0;
    // < (v<=0), <= (v<0), = (v<0), != int(v)
    if(comp == OpV.LT && v <= 0 || (comp == OpV.LE || comp == OpV.EQ) && v < 0 ||
       comp == OpV.EQ && v != (int) v) return 1;
    // > (v<1), >= (v<=1), != (v=0)
    if(comp == OpV.GT && v < 1 || comp == OpV.GE && v <= 1 || comp == OpV.NE && v == 0) return 2;
    // < (v<=1), <= (v<1), = (v=0)
    if(comp == OpV.LT && v <= 1 || comp == OpV.LE && v < 1 || comp == OpV.EQ && v == 0) return 3;
    return -1;
  }
}
