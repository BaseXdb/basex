package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.core.Main;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
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

/**
 * Insert expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class Insert extends Update {
  /** First flag. */
  private final boolean first;
  /** Last flag. */
  private final boolean last;
  /** Before flag. */
  private final boolean before;
  /** After flag. */
  private final boolean after;

  /**
   * Constructor.
   * @param i query info
   * @param src source expression
   * @param f first flag
   * @param l last
   * @param b before
   * @param a after
   * @param trg target expression
   */
  public Insert(final QueryInfo i, final Expr src, final boolean f,
      final boolean l, final boolean b, final boolean a, final Expr trg) {
    super(i, trg, src);
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
    if(c.errAtt) error(UPNOATTRPER);
    if(c.duplAtt != null) error(UPATTDUPL, c.duplAtt);

    // check target constraints
    final Iter t = expr[0].iter(ctx);
    final Item i = t.next();
    if(i == null) error(UPSEQEMP, Main.name(this));
    if(!(i instanceof Nod) || t.next() != null)
      error(before || after ? UPTRGTYP2 : UPTRGTYP);

    final Nod n = (Nod) i;
    final Nod par = n.parent();
    if(before || after) {
      if(n.type == Type.ATT || n.type == Type.DOC) error(UPTRGTYP2);
      if(par == null) error(UPPAREMPTY);
    } else {
      if(n.type != Type.ELM && n.type != Type.DOC) error(UPTRGTYP);
    }

    UpdatePrimitive up = null;
    if(aList.size() > 0) {
      final Nod targ = before || after ? par : n;
      if(targ.type != Type.ELM) error(before || after ? UPATTELM : UPATTELM2);

      up = new InsertAttribute(this, targ, checkNS(aList, targ, ctx));
      ctx.updates.add(up, ctx);
    }

    if(cList.size() > 0) {
      if(before) {
        up = new InsertBefore(this, n, cList);
      } else if(after) {
        up = new InsertAfter(this, n, cList);
      } else if(first) {
        up = new InsertIntoFirst(this, n, cList);
      } else {
        up = new InsertInto(this, n, cList, last);
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
