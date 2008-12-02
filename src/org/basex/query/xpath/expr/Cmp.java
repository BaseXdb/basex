package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.func.Position;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Comp;
import org.basex.query.xpath.item.Dbl;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.path.LocPath;
import org.basex.util.Token;

/**
 * Abstract Compare Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Cmp extends Arr {
  /** Expression Type. */
  public Comp type;
  
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public Cmp(final Expr e1, final Expr e2) {
    super(e1, e2);
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);

    // pre-evaluate empty node sets
    final Item i1 = expr[0] instanceof Item ? (Item) expr[0] : null;
    final Item i2 = expr[1] instanceof Item ? (Item) expr[1] : null;
    if(i1 != null && i1.size() == 0 || i2 != null && i2.size() == 0) {
      ctx.compInfo(OPTEQ1);
      return Bln.FALSE;
    }

    // pre-evaluate constant comparison
    if(i1 != null && i2 != null) {
      ctx.compInfo(OPTCMP);
      return Bln.get(type.eval((Item) expr[0], (Item) expr[1]));
    }

    // rewrite position predicate
    if(expr[0] instanceof Position && expr[1] instanceof Dbl) {    
      final Expr ex = Pos.create(((Dbl) expr[1]).num(), type);
      if(ex != null) {
        ctx.compInfo(OPTPOS);
        return ex;
      }
    }

    XPOptimizer.addText(expr[0], ctx);
    XPOptimizer.addText(expr[1], ctx);
    return this;
  }

  @Override
  public final Bln eval(final XPContext ctx) throws QueryException {
    final Item v1 = ctx.eval(expr[0]);
    final Item v2 = ctx.eval(expr[1]);
   
    // don't evaluate empty node sets
    return Bln.get(v1.size() != 0 && v2.size() != 0 && type.eval(v1, v2));
  }
  
  /**
   * Checks if this expression has a location path and Item as arguments.
   * @return result of check
   */
  public final boolean standard() {
    return expr[0] instanceof LocPath && expr[1] instanceof Item;
  }
  
  @Override
  public final String toString() {
    return toString(type);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, Token.token(TYPE), Token.token(type.toString()));
    expr[0].plan(ser);
    expr[1].plan(ser);
    ser.closeElement();
  }
}
