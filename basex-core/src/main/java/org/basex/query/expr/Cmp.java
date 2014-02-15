package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.path.*;
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
  final Collation collation;
  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param coll collation
   */
  Cmp(final InputInfo ii, final Expr e1, final Expr e2, final Collation coll) {
    super(ii, e1, e2);
    collation = coll;
  }

  /**
   * Swaps the operands of the expression if better performance is expected.
   * The operator itself needs to be swapped by the calling expression.
   * @return resulting expression
   */
  final boolean swap() {
    // move value or path without root to second position
    final boolean swap = expr[0].isValue() && !expr[1].isValue() ||
        expr[1] instanceof Path && ((Path) expr[1]).root == null &&
        (!(expr[0] instanceof Path) || ((Path) expr[0]).root != null);

    if(swap) {
      final Expr tmp = expr[0];
      expr[0] = expr[1];
      expr[1] = tmp;
    }
    return swap;
  }

  /**
   * If possible, inverts the operands of the expression.
   * @return original or modified expression
   */
  public abstract Cmp invert();

  /**
   * This method is called if the first operand of the comparison
   * expression is a {@code count()} function.
   * @param o comparison operator
   * @return resulting expression
   * @throws QueryException query exception
   */
  final Expr compCount(final OpV o) throws QueryException {
    // evaluate argument
    final Expr a = expr[1];
    if(!a.isItem()) return this;
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
      return Function.EXISTS.get(null, info, ((Arr) expr[0]).expr);
    // EMPTY: c < (v<=1), c <= (v<1), c = (v=0)
    if(o == OpV.LT && v <= 1 || o == OpV.LE && v < 1 || o == OpV.EQ && v == 0)
      return Function.EMPTY.get(null, info, ((Arr) expr[0]).expr);

    return this;
  }
}
