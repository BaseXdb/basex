package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;

/**
 * Simple functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FNSimple extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case ONEMORE:
        final Iter ir = SeqIter.get(ctx.iter(args[0]));
        if(ir.size() < 1) Err.or(ONEMORE);
        return ir;
      case UNORDER:
        return ctx.iter(args[0]);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Expr e = args.length == 1 ? args[0] : null;
    switch(func) {
      case FALSE:   return Bln.FALSE;
      case TRUE:    return Bln.TRUE;
      case EMPTY:   return Bln.get(!e.i() && e.iter(ctx).next() == null);
      case EXISTS:  return Bln.get(e.i() || e.iter(ctx).next() != null);
      case BOOL:    return Bln.get((e.i() ? (Item) e : e.ebv(ctx)).bool());
      case NOT:     return Bln.get(!(e.i() ? (Item) e : e.ebv(ctx)).bool());
      case ZEROONE:
        Iter iter = e.iter(ctx);
        Item it = iter.next();
        if(it == null) return null;
        if(iter.next() != null) Err.or(ZEROONE);
        return it;
      case EXONE:
        iter = e.iter(ctx);
        it = iter.next();
        if(it == null || iter.next() != null) Err.or(EXONE);
        return it;
      default:
        return super.atomic(ctx);
    }
  }
  
  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    final boolean i = args.length == 1 && args[0].i();
    final boolean e = args.length == 1 && args[0].e();
    
    switch(func) {
      case FALSE:
      case TRUE:
        return atomic(ctx);
      case EMPTY:
      case EXISTS:
      case BOOL:
        return i ? atomic(ctx) : this;
      case NOT:
        if(i) return atomic(ctx);
        if(args[0] instanceof Fun) {
          final Fun fs = (Fun) args[0];
          if(fs.func == FunDef.EMPTY) {
            args = fs.args;
            func = FunDef.EXISTS;
          } else if(fs.func == FunDef.EXISTS) {
            args = fs.args;
            func = FunDef.EMPTY;
          }
        }
        return this;
      case ZEROONE:
        return e || i || args[0].returned(ctx).single ? args[0] : this;
      case EXONE:
      case ONEMORE:
        return i || args[0].returned(ctx).single ? args[0] : this;
      case UNORDER:
        return args[0];
      default:
        return this;
    }
  }
}
