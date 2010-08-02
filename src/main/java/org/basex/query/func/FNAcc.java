package org.basex.query.func;

import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.QNm;
import org.basex.query.item.Seq;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Accessor functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNAcc extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNAcc(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Expr e = expr.length != 0 ? expr[0] : checkCtx(ctx);

    switch(func) {
      case POS:
        return Itr.get(ctx.pos);
      case LAST:
        return Itr.get(ctx.size);
      case STRING:
        Item it = e.atomic(ctx);
        return it == null ? Str.ZERO : it.str() && !it.unt() ? it :
          Str.get(it.atom());
      case NUMBER:
        final Iter ir = ctx.iter(e);
        it = ir.next();
        return it == null || ir.next() != null ? Dbl.NAN : number(it);
      case STRLEN:
        return Itr.get(len(checkEStr(e, ctx)));
      case NORM:
        return Str.get(norm(checkEStr(e, ctx)));
      case URIQNAME:
        it = e.atomic(ctx);
        if(it == null) return null;
        return ((QNm) checkType(it, Type.QNM)).uri;
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    if(expr.length == 0) return this;
    final Item it = expr[0].item() ? (Item) expr[0] : null;

    switch(func) {
      case STRING:
      case NUMBER:
      case STRLEN:
      case NORM:
        return expr[0].empty() || it != null ? atomic(ctx) : this;
      case URIQNAME:
        return expr[0].empty() ? Seq.EMPTY : it != null ? atomic(ctx) : this;
      default:
        return this;
    }
  }

  /**
   * Converts the specified item to a double.
   * @param it input item
   * @return double iterator
   */
  private Item number(final Item it) {
    if(it.type == Type.DBL) return it;

    double d = Double.NaN;
    try {
      if(it.type != Type.URI && (it.str() || it.num() || it.unt()))
        d = it.dbl();
    } catch(final QueryException ex) { }

    return Dbl.get(d);
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    final boolean pos = func == FunDef.POS || func == FunDef.LAST;
    return u == Use.CTX && (pos || expr.length == 0) ||
      u == Use.POS && pos || super.uses(u, ctx);
  }
}
