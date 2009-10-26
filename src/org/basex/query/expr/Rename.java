package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.primitives.RenamePrimitive;
import org.basex.query.util.Err;
import org.basex.util.Token;

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
    final Iter t = SeqIter.get(expr[0].iter(ctx));
    Item i = t.next();
    
    // check target constraints
    if(i == null) Err.or(UPSEQEMP, this);
    if(!(i instanceof Nod) || t.size() != 1) Err.or(UPWRTRGTYP, this);
    final Nod n = (Nod) i;
    if(n.type != Type.ELM && n.type != Type.ATT && n.type != Type.PI)
      Err.or(UPWRTRGTYP, this);

    // check new name constraints
    final Iter na = SeqIter.get(expr[1].iter(ctx));
    i = na.next();
    // [LK] temporary solution, use parts of node constructor in parser?
    byte[] name = Token.token("");
    if(i == null) Err.or(UPDATE, this);
    if(i.type.num || i.type.str) name = i.str();
    if(name.length == 0) Err.or(UPDATE, this);
    ctx.updates.addPrimitive(new RenamePrimitive(n, name));
    
    return Iter.EMPTY;
  }
  
  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.UPD || super.uses(u, ctx);
  }

  @Override
  public String toString() {
    return null;
  }
}
