package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.path.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract comparison.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   */
  Cmp(final InputInfo ii, final Expr e1, final Expr e2) {
    super(ii, e1, e2);
  }

  /**
   * Swaps the operands of the expression if better performance is expected.
   * The operator itself needs to be swapped by the calling expression.
   * @return resulting expression
   */
  final boolean swap() {
    // move value or path without root to second position
    final boolean swap = expr[0].isValue() && !expr[1].isValue() ||
      expr[0] instanceof AxisPath && ((AxisPath) expr[0]).root != null &&
      expr[1] instanceof AxisPath && ((AxisPath) expr[1]).root == null;

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
    final Type ip = it.type;
    if(!ip.isNumber() && !ip.isUntyped()) return this;

    final double v = it.dbl(info);
    // TRUE: c > (v<0), c != (v<0), c >= (v<=0), c != not-int(v)
    if((o == OpV.GT || o == OpV.NE) && v < 0 || o == OpV.GE && v <= 0 ||
       o == OpV.NE && v != (int) v) return Bln.TRUE;
    // FALSE: c < (v<=0), c <= (v<0), c = (v<0), c = not-int(v)
    if(o == OpV.LT && v <= 0 || (o == OpV.LE || o == OpV.EQ) && v < 0 ||
       o == OpV.EQ && v != (int) v) return Bln.FALSE;
    // EXISTS: c > (v<1), c >= (v<=1), c != (v=0)
    if(o == OpV.GT && v < 1 || o == OpV.GE && v <= 1 || o == OpV.NE && v == 0)
      return Function.EXISTS.get(info, ((StandardFunc) expr[0]).expr);
    // EMPTY: c < (v<=1), c <= (v<1), c = (v=0)
    if(o == OpV.LT && v <= 1 || o == OpV.LE && v < 1 || o == OpV.EQ && v == 0)
      return Function.EMPTY.get(info, ((StandardFunc) expr[0]).expr);

    return this;
  }
}
