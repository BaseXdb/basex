package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CElem;
import org.basex.query.expr.CFrag;
import org.basex.query.expr.CPI;
import org.basex.query.expr.Expr;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.RenamePrimitive;
import org.basex.query.util.Err;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Rename expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class Rename extends Update {
  /**
   * Constructor.
   * @param ii input info
   * @param tg target expression
   * @param n new name expression
   */
  public Rename(final InputInfo ii, final Expr tg, final Expr n) {
    super(ii, tg, n);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();

    // check target constraints
    if(i == null) Err.or(input, UPSEQEMP, Util.name(this));
    if(t.next() != null) Err.or(input, UPWRTRGTYP);

    CFrag ex = null;
    if(i.type == Type.ELM) {
      ex = new CElem(input, expr[1], new Atts());
    } else if(i.type == Type.ATT) {
      ex = new CAttr(input, false, expr[1]);
    } else if(i.type == Type.PI) {
      ex = new CPI(input, expr[1], Empty.SEQ);
    } else {
      Err.or(input, UPWRTRGTYP);
    }

    // check namespace conflicts...
    final QNm rename = ex.atomic(ctx, input).qname();
    final Nod targ = (Nod) i;
    final Nod test = i.type == Type.ELM ? targ :
      i.type == Type.ATT ? targ.parent() : null;

    if(test != null) {
      final byte[] uri = test.uri(rename.pref(), ctx);
      if(uri != null && !eq(rename.uri().atom(), uri)) Err.or(input, UPNSCONFL);
    }
    ctx.updates.add(new RenamePrimitive(input, targ, rename), ctx);
    return null;
  }

  @Override
  public String toString() {
    return RENAME + ' ' + NODE + ' ' + expr[0] + ' ' + AS + ' ' + expr[1];
  }
}
