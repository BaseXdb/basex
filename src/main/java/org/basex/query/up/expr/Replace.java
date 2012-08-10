package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Replace expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Replace extends Update {
  /** 'Value of' flag. */
  private final boolean value;

  /**
   * Constructor.
   * @param ii input info
   * @param t target expression
   * @param r source expression
   * @param v replace value of
   */
  public Replace(final InputInfo ii, final Expr t, final Expr r,
      final boolean v) {
    super(ii, t, r);
    value = v;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(ii, ctx).add(expr[1]);
    if(c.errAtt) UPNOATTRPER.thrw(info);
    if(c.duplAtt != null) UPATTDUPL.thrw(info, c.duplAtt);

    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();
    // check target constraints
    if(i == null) throw UPSEQEMP.thrw(info, Util.name(this));
    final Type tp = i.type;
    if(!(i instanceof ANode) || tp == NodeType.DOC || t.next() != null)
      UPTRGMULT.thrw(info);
    final ANode targ = (ANode) i;
    final DBNode dbn = ctx.updates.determineDataRef(targ, ctx);

    // replace node
    final ANodeList aList = c.atts;
    ANodeList list = c.children;
    if(value) {
      // replace value of node
      final byte[] txt = list.size() < 1 ? aList.size() < 1 ? EMPTY :
        aList.get(0).string() : list.get(0).string();
      // check validity of future comments or PIs
      if(tp == NodeType.COM) FComm.parse(txt, info);
      if(tp == NodeType.PI) FPI.parse(txt, info);

      ctx.updates.add(tp == NodeType.ELM ?
          new ReplaceElementContent(dbn.pre, dbn.data, info, txt) :
          new ReplaceValue(dbn.pre, dbn.data, info, txt), ctx);
    } else {
      final ANode par = targ.parent();
      if(par == null) UPNOPAR.thrw(info, i);
      if(tp == NodeType.ATT) {
        // replace attribute node
        if(list.size() > 0) UPWRATTR.thrw(info);
        list = checkNS(aList, par, ctx);
      } else {
        // replace non-attribute node
        if(aList.size() > 0) UPWRELM.thrw(info);
      }
      // conforms to specification: insertion sequence may be empty
      ctx.updates.add(new ReplaceNode(dbn.pre, dbn.data, info, list), ctx);
    }
    return null;
  }

  @Override
  public String toString() {
    return REPLACE + (value ? ' ' + VALUEE + ' ' + OF : "") +
      ' ' + NODE + ' ' + expr[0] + ' ' + WITH + ' ' + expr[1];
  }
}
