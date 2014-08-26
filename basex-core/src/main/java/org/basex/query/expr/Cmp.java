package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract comparison.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
  /** Collation used for comparisons. */
  final Collation coll;
  /**
   * Constructor.
   * @param info input info
   * @param expr1 first expression
   * @param expr2 second expression
   * @param coll collation
   */
  Cmp(final InputInfo info, final Expr expr1, final Expr expr2, final Collation coll) {
    super(info, expr1, expr2);
    this.coll = coll;
  }

  /**
   * Swaps the operands of the expression if better performance is expected.
   * The operator itself needs to be swapped by the calling expression.
   * @return resulting expression
   */
  final boolean swap() {
    // move value or path without root to second position
    final boolean swap = exprs[0].isValue() && !exprs[1].isValue() ||
        exprs[1] instanceof Path && ((Path) exprs[1]).root == null &&
        (!(exprs[0] instanceof Path) || ((Path) exprs[0]).root != null);

    if(swap) {
      final Expr tmp = exprs[0];
      exprs[0] = exprs[1];
      exprs[1] = tmp;
    }
    return swap;
  }

  /**
   * If possible, inverts the operands of the expression.
   * @return original or modified expression
   */
  public abstract Cmp invert();

  /**
   * This method is called if the first operand of the comparison expression is a
   * {@code count()} function.
   * @param o comparison operator
   * @return resulting expression
   * @throws QueryException query exception
   */
  final Expr compCount(final OpV o) throws QueryException {
    // evaluate argument
    final Expr a = exprs[1];
    if(!(a instanceof Item)) return this;
    final Item it = (Item) a;
    if(!it.type.isNumberOrUntyped()) return this;

    final double v = it.dbl(info);
    // TRUE: c > (v<0), c != (v<0), c >= (v<=0), c != not-int(v)
    if((o == OpV.GT || o == OpV.NE) && v < 0 || o == OpV.GE && v <= 0 ||
       o == OpV.NE && v != (int) v) return Bln.TRUE;
    // FALSE: c < (v<=0), c <= (v<0), c = (v<0), c = not-int(v)
    if(o == OpV.LT && v <= 0 || (o == OpV.LE || o == OpV.EQ) && v < 0 ||
       o == OpV.EQ && v != (int) v) return Bln.FALSE;
    // EXISTS: c > (v<1), c >= (v<=1), c != (v=0)
    if(o == OpV.GT && v < 1 || o == OpV.GE && v <= 1 || o == OpV.NE && v == 0)
      return Function.EXISTS.get(null, info, ((Arr) exprs[0]).exprs);
    // EMPTY: c < (v<=1), c <= (v<1), c = (v=0)
    if(o == OpV.LT && v <= 1 || o == OpV.LE && v < 1 || o == OpV.EQ && v == 0)
      return Function.EMPTY.get(null, info, ((Arr) exprs[0]).exprs);

    return this;
  }
}
