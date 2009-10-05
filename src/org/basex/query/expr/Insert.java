package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
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
    super(trg, src);
    as = a;
    last = la;
    into = in;
    after = af;
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    if(!into) Err.or(UPIMPL, "only insert into");
    final Iter ti = expr[0].iter(ctx);
    Item i = ti.next();
    if(i == null) Err.or(UPSEQEMP, ti);
    if(ti.next() != null) Err.or(UPTRGMULT, ti);
    if(!(i instanceof Nod)) Err.or(UPTRGMULT, ti);
    final Nod trgtN = (Nod) i;
    // [LK] check nodes for type
    final Iter r = SeqIter.get(expr[1].iter(ctx));
    i = r.next();
    if(i == null) Err.or(UPSEQEMP, i);
    final boolean insAttr = Nod.kind(i.type) == Data.ATTR;
    if(insAttr && Nod.kind(trgtN.type) != Data.ELEM) Err.or(UPDATE, r);
    i = r.next();
    while(i != null) {
      if((Nod.kind(i.type) == Data.ATTR) ^ insAttr) Err.or(UPDATE, r);
      i = r.next();
    }
    r.reset();
    ctx.updates.addPrimitive(new InsertPrimitive(trgtN, r, insAttr));
    return Iter.EMPTY;
  }

  @Override
  public String toString() {
    return null;
  }

}
