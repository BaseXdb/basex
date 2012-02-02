package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.primitives.InsertAfter;
import org.basex.query.up.primitives.InsertAttribute;
import org.basex.query.up.primitives.InsertBefore;
import org.basex.query.up.primitives.InsertInto;
import org.basex.query.up.primitives.InsertIntoFirst;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Insert expression.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * @param ii input info
   * @param src source expression
   * @param f first flag
   * @param l last
   * @param b before
   * @param a after
   * @param trg target expression
   */
  public Insert(final InputInfo ii, final Expr src, final boolean f,
      final boolean l, final boolean b, final boolean a, final Expr trg) {
    super(ii, trg, src);
    first = f;
    last = l;
    before = b;
    after = a;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Constr c = new Constr(ii, ctx).add(expr[1]);
    final NodeCache cList = c.children;
    final NodeCache aList = c.atts;
    if(c.errAtt) UPNOATTRPER.thrw(input);
    if(c.duplAtt != null) UPATTDUPL.thrw(input, c.duplAtt);

    // check target constraints
    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();
    if(i == null) UPSEQEMP.thrw(input, Util.name(this));
    if(!(i instanceof ANode) || t.next() != null)
      (before || after ? UPTRGTYP2 : UPTRGTYP).thrw(input);

    final ANode n = (ANode) i;
    final ANode par = n.parent();
    if(before || after) {
      if(n.type == NodeType.ATT || n.type == NodeType.DOC)
        UPTRGTYP2.thrw(input);
      if(par == null) UPPAREMPTY.thrw(input);
    } else {
      if(n.type != NodeType.ELM && n.type != NodeType.DOC)
        UPTRGTYP.thrw(input);
    }

    UpdatePrimitive up;
    DBNode dbn;
    // no update primitive is created if node list is empty
    if(aList.size() > 0) {
      final ANode targ = before || after ? par : n;
      if(targ.type != NodeType.ELM)
        (before || after ? UPATTELM : UPATTELM2).thrw(input);

      dbn = ctx.updates.determineDataRef(targ, ctx);
      up = new InsertAttribute(dbn.pre, dbn.data, input,
          checkNS(aList, targ, ctx));
      ctx.updates.add(up, ctx);
    }

    // no update primitive is created if node list is empty
    if(cList.size() > 0) {
      dbn = ctx.updates.determineDataRef(n, ctx);
      if(before) up = new InsertBefore(dbn.pre, dbn.data, input, cList);
      else if(after) up = new InsertAfter(dbn.pre, dbn.data, input, cList);
      else if(first) up = new InsertIntoFirst(dbn.pre, dbn.data, input, cList);
      else up = new InsertInto(dbn.pre, dbn.data, input, cList, last);
      ctx.updates.add(up, ctx);
    }
    return null;
  }

  @Override
  public String toString() {
    return INSERT + ' ' + NODE + ' ' + expr[1] + ' ' + INTO + ' ' + expr[0];
  }
}
