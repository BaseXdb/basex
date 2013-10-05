package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(ii, ctx).add(expr[1]);
    final ANodeList cList = c.children;
    final ANodeList aList = c.atts;
    if(c.errAtt) UPNOATTRPER.thrw(info);
    if(c.duplAtt != null) UPATTDUPL.thrw(info, new QNm(c.duplAtt));

    // check target constraints
    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();
    if(i == null) UPSEQEMP.thrw(info, Util.className(this));
    if(!(i instanceof ANode) || t.next() != null)
      (before || after ? UPTRGTYP2 : UPTRGTYP).thrw(info);

    final ANode n = (ANode) i;
    final ANode par = n.parent();
    if(before || after) {
      if(n.type == NodeType.ATT || n.type == NodeType.DOC)
        UPTRGTYP2.thrw(info);
      if(par == null) UPPAREMPTY.thrw(info);
    } else {
      if(n.type != NodeType.ELM && n.type != NodeType.DOC)
        UPTRGTYP.thrw(info);
    }

    UpdatePrimitive up;
    DBNode dbn;
    // no update primitive is created if node list is empty
    if(aList.size() > 0) {
      final ANode targ = before || after ? par : n;
      if(targ.type != NodeType.ELM)
        (before || after ? UPATTELM : UPATTELM2).thrw(info);

      dbn = ctx.updates.determineDataRef(targ, ctx);
      up = new InsertAttribute(dbn.pre, dbn.data, info, checkNS(aList, targ, ctx));
      ctx.updates.add(up, ctx);
    }

    // no update primitive is created if node list is empty
    if(cList.size() > 0) {
      dbn = ctx.updates.determineDataRef(n, ctx);
      if(before) up = new InsertBefore(dbn.pre, dbn.data, info, cList);
      else if(after) up = new InsertAfter(dbn.pre, dbn.data, info, cList);
      else if(first) up = new InsertIntoAsFirst(dbn.pre, dbn.data, info, cList);
      else if(last) up = new InsertIntoAsLast(dbn.pre, dbn.data, info, cList);
      else up = new InsertInto(dbn.pre, dbn.data, info, cList);
      ctx.updates.add(up, ctx);
    }
    return null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Insert(info, expr[1].copy(ctx, scp, vs), first, last, before, after,
        expr[0].copy(ctx, scp, vs));
  }

  @Override
  public String toString() {
    return INSERT + ' ' + NODE + ' ' + expr[1] + ' ' + INTO + ' ' + expr[0];
  }
}
