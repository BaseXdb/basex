package org.basex.query.func;

import org.basex.BaseX;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Atm;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;

/**
 * Generating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNGen extends Fun {
  @Override
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    final Iter iter = arg.length != 0 ? arg[0] : null;

    switch(func) {
      case DATA:
        final SeqIter seq = new SeqIter();
        Item it;
        while((it = iter.next()) != null) seq.add(atom(it));
        return seq;
      case COLLECT:
        it = iter != null ? iter.next() : null;
        if(iter != null && it == null) Err.empty(this);
        return ctx.coll(iter == null ? null : checkStr(it));
      case DOC:
        it = iter.next();
        if(it == null) return Iter.EMPTY;
        if(it.type == Type.DOC) return it.iter();
        return ctx.doc(checkStr(it), false).iter();
      case DOCAVAIL:
        it = iter.next();
        if(it == null) return Bln.FALSE.iter();
        final byte[] file = checkStr(it);
        try {
          ctx.doc(file, false);
          return Bln.TRUE.iter();
        } catch(final QueryException e) {
          return Bln.FALSE.iter();
        }
      default:
        BaseX.notexpected(func); return null;
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    // ..slows it down if the document will not be used, 
    // but allows more optimizations
    if(func == FunDef.DOC) {
      if(!args[0].i()) return this;
      final Item it = (Item) args[0];
      return it.type == Type.DOC ? it : ctx.doc(checkStr(it), false);
    }
    return this;
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
