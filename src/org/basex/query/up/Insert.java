package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.core.Main;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
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
    final NodIter cList = c.children;
    final NodIter aList = c.ats;
    if(c.errAtt) Err.or(UPNOATTRPER);
    if(c.duplAtt != null) Err.or(UPATTDUPL, c.duplAtt);

    // check target constraints
    final Iter t = expr[0].iter(ctx);
    final Item i = t.next();
    if(i == null) Err.or(UPSEQEMP, Main.name(this));
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
    if(aList.size() > 0) {
      final Nod targ = before || after ? par : n;
      if(targ.type != Type.ELM)
        Err.or(before || after ? UPATTELM : UPATTELM2, this);

      for(int a = 0; a < aList.size(); a++) {
        final QNm name = aList.get(a).qname();
        final byte[] uri = targ.uri(name.pref(), ctx);
        if(uri != null && !eq(name.uri.str(), uri)) Err.or(UPNSCONFL);
      }
      up = new InsertAttribute(targ, aList);
      ctx.updates.add(up, ctx);
    }

    if(cList.size() > 0) {
      if(before) {
        up = new InsertBefore(n, cList);
      } else if(after) {
        up = new InsertAfter(n, cList);
      } else if(first) {
        up = new InsertIntoFirst(n, cList);
      } else {
        up = new InsertInto(n, cList, last);
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
