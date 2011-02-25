package org.basex.query.up;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.FComm;
import org.basex.query.item.FPI;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.primitives.ReplaceElemContent;
import org.basex.query.up.primitives.ReplacePrimitive;
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace expression.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final Constr c = new Constr(ii, ctx, expr[1]);
    if(c.errAtt) UPNOATTRPER.thrw(input);
    if(c.duplAtt != null) UPATTDUPL.thrw(input, c.duplAtt);

    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();
    // check target constraints
    if(i == null) UPSEQEMP.thrw(input, Util.name(this));
    final Type tp = i.type;
    if(!(i instanceof ANode) || tp == NodeType.DOC || t.next() != null)
      UPTRGMULT.thrw(input);
    final ANode targ = (ANode) i;

    // replace node
    final NodeCache aList = c.atts;
    NodeCache list = c.children;
    if(value) {
      // replace value of node
      final byte[] txt = list.size() < 1 ? EMPTY : list.get(0).atom();
      if(tp == NodeType.COM) FComm.parse(txt, input);
      if(tp == NodeType.PI) FPI.parse(txt, input);

      ctx.updates.add(tp == NodeType.ELM ?
          new ReplaceElemContent(input, targ, txt) :
          new ReplaceValue(input, targ, new QNm(txt)), ctx);
    } else {
      final ANode par = targ.parent();
      if(par == null) UPNOPAR.thrw(input, i);
      if(tp == NodeType.ATT) {
        // replace attribute node
        if(list.size() > 0) UPWRATTR.thrw(input);
        list = checkNS(aList, par, ctx);
      } else {
        // replace non-attribute node
        if(aList.size() > 0) UPWRELM.thrw(input);
      }
      ctx.updates.add(new ReplacePrimitive(input, targ, list), ctx);
    }
    return null;
  }

  @Override
  public String toString() {
    return REPLACE + (value ? ' ' + VALUEE + ' ' + OF : "") +
      ' ' + NODE + ' ' + expr[0] + ' ' + WITH + ' ' + expr[1];
  }
}
