package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import org.basex.core.Main;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CAttr;
import org.basex.query.expr.CElem;
import org.basex.query.expr.CFrag;
import org.basex.query.expr.CPI;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Seq;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.RenamePrimitive;
import org.basex.query.util.Err;
import org.basex.util.Atts;

/**
 * Rename expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class Rename extends Update {
  /**
   * Constructor.
   * @param tg target expression
   * @param n new name expression
   */
  public Rename(final Expr tg, final Expr n) {
    super(tg, n);
  }

  @Override
  public Seq atomic(final QueryContext ctx) throws QueryException {
    final Iter t = expr[0].iter(ctx);
    final Item i = t.next();

    // check target constraints
    if(i == null) Err.or(UPSEQEMP, Main.name(this));
    if(t.next() != null) Err.or(UPWRTRGTYP, this);

    CFrag ex = null;
    if(i.type == Type.ELM) {
      ex = new CElem(expr[1], new Expr[0], new Atts());
    } else if(i.type == Type.ATT) {
      ex = new CAttr(expr[1], new Expr[0], false);
    } else if(i.type == Type.PI) {
      ex = new CPI(expr[1], Seq.EMPTY);
    } else {
      Err.or(UPWRTRGTYP, this);
    }

    // check namespace conflicts...
    final QNm rename = ex.atomic(ctx).qname();
    final Nod targ = (Nod) i;
    final Nod test = i.type == Type.ELM ? targ :
      i.type == Type.ATT ? targ.parent() : null;

    if(test != null) {
      final byte[] uri = test.uri(rename.pref(), ctx);
      // [LK] add flag to RenamePrimitive for new namespace
      if(uri != null && !eq(rename.uri.str(), uri)) Err.or(UPNSCONFL);
    }
    ctx.updates.add(new RenamePrimitive(targ, rename), ctx);
    return Seq.EMPTY;
  }

  @Override
  public String toString() {
    return RENAME + ' ' + NODE + ' ' + expr[0] + ' ' + AS + ' ' + expr[1];
  }
}
