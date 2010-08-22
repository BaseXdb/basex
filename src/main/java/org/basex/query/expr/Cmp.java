package org.basex.query.expr;

import org.basex.query.QueryException;
import org.basex.query.expr.CmpV.Op;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.path.AxisPath;
import org.basex.util.InputInfo;

/**
 * Abstract comparison.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
abstract class Cmp extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   */
  public Cmp(final InputInfo ii, final Expr e1, final Expr e2) {
    super(ii, e1, e2);
  }

  /**
   * Swaps the operands of the expression, if better performance is expected.
   * The operator itself needs to be swapped by the calling expression.
   * @return resulting expression
   */
  protected boolean swap() {
    final boolean swap = expr[0].value() && !expr[1].value() ||
      expr[0] instanceof AxisPath && ((AxisPath) expr[0]).root != null &&
      expr[1] instanceof AxisPath;

    if(swap) {
      final Expr tmp = expr[0];
      expr[0] = expr[1];
      expr[1] = tmp;
    }
    return swap;
  }

  /**
   * Rewrites a {@code count()} function.
   * @param o comparison operator
   * @return resulting expression
   * @throws QueryException query exception
   */
  protected Expr count(final Op o) throws QueryException {
    // evaluate argument
    final Expr a = expr[1];
    if(!a.item()) return this;
    final Item it = (Item) a;
    if(!it.num() && !it.unt()) return this;

    final double d = it.dbl(input);
    // x > (d<0), x >= (d<=0), x != (d<=0), x != not-int(d)
    if(o == Op.GT && d < 0 || (o == Op.GE || o == Op.NE) && d <= 0 ||
       o == Op.NE && d != (int) d) return Bln.TRUE;
    // x < (d<=0), x <= (d<0), x = (d<0), x = not-int(d)
    if(o == Op.LT && d <= 0 || (o == Op.LE || o == Op.EQ) && d < 0 ||
       o == Op.EQ && d != (int) d) return Bln.FALSE;
    // x > (d<1), x >= (d<=1),  x != (d=0)
    if(o == Op.GT && d < 1 || o == Op.GE && d <= 1 || o == Op.NE && d == 0)
      return FunDef.EXISTS.newInstance(input, ((Fun) expr[0]).expr);
    // x < (d<=1), x <= (d<1),  x = (d=0)
    if(o == Op.LT && d <= 1 || o == Op.LE && d < 1 || o == Op.EQ && d == 0)
      return FunDef.EMPTY.newInstance(input, ((Fun) expr[0]).expr);

    return this;
  }
}
