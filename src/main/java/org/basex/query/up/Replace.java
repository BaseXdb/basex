package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.FComm;
import org.basex.query.item.FPI;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.up.primitives.ReplaceElemContent;
import org.basex.query.up.primitives.ReplacePrimitive;
import org.basex.query.up.primitives.ReplaceValue;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    final Constr c = new Constr(ctx, expr[1]);
    if(c.errAtt) Err.or(input, UPNOATTRPER);
    if(c.duplAtt != null) Err.or(input, UPATTDUPL, c.duplAtt);

    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();
    // check target constraints
    if(i == null) Err.or(input, UPSEQEMP, Util.name(this));
    final Type tp = i.type;
    if(!(i instanceof Nod) || tp == Type.DOC || t.next() != null)
      Err.or(input, UPTRGMULT);
    final Nod targ = (Nod) i;

    // replace node
    final NodIter aList = c.ats;
    NodIter list = c.children;
    if(value) {
      // replace value of node
      final byte[] txt = list.size() < 1 ? EMPTY : list.get(0).atom();
      if(tp == Type.COM) FComm.parse(txt, input);
      if(tp == Type.PI) FPI.parse(txt, input);

      ctx.updates.add(tp == Type.ELM ?
          new ReplaceElemContent(input, targ, txt) :
          new ReplaceValue(input, targ, new QNm(txt)), ctx);
    } else {
      final Nod par = targ.parent();
      if(par == null) Err.or(input, UPNOPAR, i);
      if(tp == Type.ATT) {
        // replace attribute node
        if(list.size() > 0) Err.or(input, UPWRATTR);
        list = checkNS(aList, par, ctx);
      } else {
        // replace non-attribute node
        if(aList.size() > 0) Err.or(input, UPWRELM);
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
