package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.up.primitives.InsertAfter;
import org.basex.query.up.primitives.InsertAttribute;
import org.basex.query.up.primitives.InsertBefore;
import org.basex.query.up.primitives.InsertInto;
import org.basex.query.up.primitives.InsertIntoFirst;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;

/**
 * Insert expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Insert extends Update {
  /** First flag. */
  final boolean first;
  /** Last flag. */
  final boolean last;
  /** After flag. */
  final boolean before;
  /** Before flag. */
  final boolean after;

  /**
   * Constructor.
   * @param src source expression
   * @param f first flag
   * @param l last
   * @param b before
   * @param a after
   * @param trg target expression
   */
  public Insert(final Expr src, final boolean f, final boolean l,
      final boolean b, final boolean a, final Expr trg) {
    super(trg, src);
    first = f;
    last = l;
    before = b;
    after = a;
  }
  
  @Override
  public Seq atomic(final QueryContext ctx) throws QueryException {
    final Constr c = new Constr(ctx, expr[1]);
    final NodIter seq = c.children;
    final NodIter aSeq = c.ats;
    if(c.errAtt) Err.or(UPNOATTRPER);
    if(c.duplAtt != null) Err.or(UPATTDUPL, c.duplAtt);

    // check target constraints
    final Iter t = expr[0].iter(ctx);
    Item i = t.next();
    if(i == null) Err.or(UPSEQEMP, this);
    if(!(i instanceof Nod) || t.next() != null)
      Err.or(before || after ? UPTRGTYP2 : UPTRGTYP, this);

    final Nod n = (Nod) i;
    final Nod par = n.parent();
    if(before || after) {
      if(n.type == Type.ATT || n.type == Type.DOC) Err.or(UPTRGTYP2, this);
      if(par == null) Err.or(UPPAREMPTY, this);
    } else {
      if(n.type != Type.ELM && n.type != Type.DOC) Err.or(UPTRGTYP, this);
    }

    UpdatePrimitive up = null;
    if(aSeq.size() > 0) {
      if(before || after) {
        if(par.type != Type.ELM) Err.or(UPATTDOC, this);
//        if(!UpdateFunctions.checkAttNames(par.attr(), aSeq, null))
//          Err.or(UPATTDUPL, n.nname());
        up = new InsertAttribute(par, aSeq);
      } else {
        if(n.type != Type.ELM) Err.or(UPWRTRGTYP2, this);
//        if(!UpdateFunctions.checkAttNames(n.attr(), aSeq, null))
//          Err.or(UPATTDUPL, n.nname());
        up = new InsertAttribute(n, aSeq);
      }
      ctx.updates.add(up, ctx);
    }
    if(seq.size() > 0) {
      if(before) {
        up = new InsertBefore(n, seq);
      } else if(after) {
        up = new InsertAfter(n, seq);
      } else if(first) {
        up = new InsertIntoFirst(n, seq);
      } else {
        up = new InsertInto(n, seq, last);
      }
      ctx.updates.add(up, ctx);
    }
    return Seq.EMPTY;
  }

  @Override
  public String toString() {
    return INSERT + ' ' + NODE + ' ' + expr[1] + ' ' + INTO + ' ' + expr[0];
  }
}
