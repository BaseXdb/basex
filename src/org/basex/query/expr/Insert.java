package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.primitives.InsertAfterPrimitive;
import org.basex.query.up.primitives.InsertAttribute;
import org.basex.query.up.primitives.InsertBeforePrimitive;
import org.basex.query.up.primitives.InsertIntoFirstPrimitive;
import org.basex.query.up.primitives.InsertIntoLastPrimitive;
import org.basex.query.up.primitives.InsertIntoPrimitive;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;

/**
 * Insert expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Insert extends Arr {
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
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Constr c = new Constr(ctx, expr[1]);
    final NodIter seq = c.children;
    final NodIter aSeq = c.ats;
    if(c.errAtt) Err.or(UPNOATTRPER);
    if(c.duplAtt != null) Err.or(UPATTDUPL, c.duplAtt);

    // check target constraints
    final Iter t = SeqIter.get(expr[0].iter(ctx));

    Item i = t.next();
    if(i == null) Err.or(UPSEQEMP, this);
    if(!(i instanceof Nod) || t.size() > 1) Err.or(UPTRGTYP, this);

    final Nod n = (Nod) i;

    if(before || after) {
      if(n.type == Type.ATT) Err.or(UPTRGTYP2, this);
      if(n.parent() == null) Err.or(UPPAREMPTY, this);
    } else {
      if(n.type != Type.ELM && n.type != Type.DOC) Err.or(UPTRGTYP, this);
    }

    final Nod par = n.parent();
    if(aSeq.size() > 0) {
      if(before || after) {
        if(par == null) Err.or(UPDATE, this);
        if(par.type == Type.DOC) Err.or(UPWRTRGTYP2, this);
        ctx.updates.addPrimitive(new InsertAttribute(par, aSeq, -1));
      } else {
        if(n.type == Type.DOC) Err.or(UPWRTRGTYP2, this);
        ctx.updates.addPrimitive(new InsertAttribute(n, aSeq, -1));
      }
    }
    if(seq.size() > 0) {
      UpdatePrimitive up = null;
      if(before) {
        up = new InsertBeforePrimitive(n, seq, -1);
      } else if(after) {
        up = new InsertAfterPrimitive(n, seq, -1);
      } else {
        if(first) up = new InsertIntoFirstPrimitive(n, seq, -1);
        else if(last) up = new InsertIntoLastPrimitive(n, seq, -1);
        else up = new InsertIntoPrimitive(n, seq, -1);
      }
      ctx.updates.addPrimitive(up);
    }
    return Iter.EMPTY;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.UPD || super.uses(u, ctx);
  }

  @Override
  public String toString() {
    return INSERT + ' ' + NODE + ' ' + expr[1] + ' ' + INTO + ' ' + expr[0];
  }
}
