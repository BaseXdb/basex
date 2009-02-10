package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Atm;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Generating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNGen extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DATA:
        return data(ctx.iter(expr[0]));
      case COLLECT:
        final Iter iter = expr.length != 0 ? ctx.iter(expr[0]) : null;
        final Item it = iter != null ? iter.next() : null;
        if(iter != null && it == null) Err.empty(this);
        return ctx.coll(iter == null ? null : checkStr(it));
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);

    switch(func) {
      case DOC:
        Item it = iter.next();
        return it == null ? null : it.type == Type.DOC ? it :
          ctx.doc(checkStr(it), false);
      case DOCAVAIL:
        it = iter.next();
        if(it != null) {
          final byte[] file = checkStr(it);
          try {
            ctx.doc(file, false);
            return Bln.TRUE;
          } catch(final QueryException e) { }
        }
        return Bln.FALSE;
      default:
        return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    // ..slows it down if the document will not be used, 
    // but allows more optimizations
    if(func == FunDef.DOC) {
      if(!expr[0].i()) return this;
      final Item it = (Item) expr[0];
      return it.type == Type.DOC ? it : ctx.doc(checkStr(it), false);
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param iter iterator.
   * @return resulting iterator
   */
  private Iter data(final Iter iter) {
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = iter.next();
        return it != null ? atom(it) : null;
      }
    };
  }
  
  /**
   * Atomizes the specified item.
   * @param it input item
   * @return atomized item
   */
  static Item atom(final Item it) {
    return it.node() ? it.type == Type.PI || it.type == Type.COM ?
        Str.get(it.str()) : new Atm(it.str()) : it;
  }
}
