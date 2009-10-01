package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.iter.Iter;
import org.basex.query.up.RenamePrimitive;
import org.basex.query.util.Err;

/**
 * Rename expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Rename extends Arr {
  /**
   * Constructor.
   * @param tg target expression
   * @param n new name expression
   */
  public Rename(final Expr tg, final Expr n) {
    super(tg, n);
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter tgI = expr[0].iter(ctx);
    final Iter nI = expr[1].iter(ctx);
    Item i = tgI.next();
    if(i == null) Err.or(INCOMPLETE);
    if(tgI.size() > 1 || !(i instanceof Nod)) Err.or(INCOMPLETE, i);
    final int kind = Nod.kind(((Nod) i).type);
    if(kind != Data.ATTR && kind != Data.ELEM && kind != Data.PI)
      Err.or(INCOMPLETE, i);
    final Item nmItem = nI.next();
    if(!(nmItem instanceof Str || nmItem instanceof QNm)) 
      Err.or(IMPLCOL, nmItem);
    ctx.updates.addPrimitive(new RenamePrimitive((Nod) i, nmItem.str()));
    return Iter.EMPTY;
  }
  
  @Override
  public String toString() {
    return null;
  }
}
