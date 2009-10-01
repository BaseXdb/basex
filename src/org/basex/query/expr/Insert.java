package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.UpdateFunctions.*;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.InsertPrimitive;
import org.basex.query.util.Err;


/**
 * Insert expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Insert extends Arr {
  /** First flag. */
  final boolean as;
  /** Last flag. */
  final boolean last;
  /** After flag. */
  final boolean into;
  /** Before flag. */
  final boolean after;

  /**
   * Constructor.
   * @param src source expression
   * @param a as
   * @param la last
   * @param in into
   * @param af after
   * @param trg target expression
   */
  public Insert(final Expr src, final boolean a, final boolean la,
      final boolean in, final boolean af, final Expr trg) {
    super(src, trg);
    as = a;
    last = la;
    into = in;
    after = af;
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    if(!into) Err.or(UPIMPL, "only insert into");
    final Iter src = expr[1].iter(ctx);
    Item i = src.next();
    if(i == null) Err.or(UPSEQEMP, src);
    if(src.next() != null) Err.or(UPTRGMULT, src);
    if(!(i instanceof Nod)) Err.or(UPTRGMULT, src);
    final Nod trgtN = (Nod) i;
    // [LK] check nodes for type
    final Iter r = SeqIter.get(expr[0].iter(ctx));
    i = r.next();
    if(i instanceof Nod) {
      final Nod n = (Nod) i;
      if(Nod.kind(n.type) == Data.ATTR) Err.or(UPIMPL, "no attrs supported");
    }
    r.reset();
    final MemData m = buildDB(r,
        trgtN instanceof DBNode ? ((DBNode) trgtN).data : null);
    ctx.updates.addPrimitive(new InsertPrimitive(trgtN, m));
    return Iter.EMPTY;
  }

  @Override
  public String toString() {
    return null;
  }

}
